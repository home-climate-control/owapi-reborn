package com.dalsemi.onewire.adapter;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.utils.Convert;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wrapper for the {@link SerialPort}.
 *
 * @author Original implementation &copy; Dallas Semiconductor
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 */
public class SerialService {

    protected static final Logger logger = LogManager.getLogger(SerialService.class);

    /**
     * The serial port name of this object (e.g. COM1, /dev/ttyS0).
     */
    private final String portName;

    /**
     * The serial port object for setting serial port parameters.
     */
    private SerialPort serialPort = null;

    /**
     * The input stream, for reading data from the serial port.
     */
    private InputStream serialInputStream = null;

    /**
     * The output stream, for writing data to the serial port.
     */
    private OutputStream serialOutputStream = null;

    /**
     * The lock.
     */
    private final ReentrantLock theLock = new ReentrantLock();

    /**
     * Temporary array, used for converting characters to bytes.
     */
    private byte[] tempArray = new byte[128];

    /**
     * Flag to indicate byte banging on read.
     * */
    private final boolean byteBang;

    private final Clock clock = Clock.systemUTC();

    /**
     * Create an instance.
     *
     * @param portName Port name to use. Validity is only checked in {@link #openPort()}.
     */
    protected SerialService(String portName) {

        this.portName = portName;

        // check to see if need to byte-bang the reads
        String prop = OneWireAccessProvider.getProperty("onewire.serial.bytebangread");

        byteBang = prop != null && prop.contains("true");
    }

    public synchronized void openPort() throws IOException {
        ThreadContext.push("openPort");

        try {

            if(isPortOpen()) {
                throw new IllegalStateException(portName + ": already open");
            }

            CommPortIdentifier portId;

            try {
                portId = CommPortIdentifier.getPortIdentifier(portName);
            } catch(NoSuchPortException ex) {
                throw new IOException(portName + ": no such port", ex);
            }

            // check if the port is currently used
            if (portId.isCurrentlyOwned()) {
                throw new IOException("Port In Use (" + portName + ")");
            }

            // try to acquire the port
            try {

                // get the port object
                serialPort = (SerialPort) portId.open("Dallas Semiconductor", 2000);

                //serialPort.setInputBufferSize(4096);
                //serialPort.setOutputBufferSize(4096);

                logger.debug("getInputBufferSize = {}", serialPort.getInputBufferSize());
                logger.debug("getOutputBufferSize = {}", serialPort.getOutputBufferSize());

                serialPort.notifyOnOutputEmpty(true);
                serialPort.notifyOnDataAvailable(true);

                // flow i/o
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                serialInputStream  = serialPort.getInputStream();
                serialOutputStream = serialPort.getOutputStream();

                // bug workaround
                serialOutputStream.write(0);

                // settings
                serialPort.disableReceiveFraming();
                serialPort.disableReceiveThreshold();
                serialPort.enableReceiveTimeout(1);

                // set baud rate
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialPort.setDTR(true);
                serialPort.setRTS(true);

                logger.debug("Port Opened ({})", portName);

            } catch(Exception ex) {

                // close the port if we have an object
                if (serialPort != null) {
                    serialPort.close();
                }

                serialPort = null;

                throw new IOException(portName + ": failed to open", ex);
            }
        } finally {
            ThreadContext.pop();
        }
    }
    public synchronized String getPortName() {
        return portName;
    }

    public synchronized boolean isPortOpen() {
        return serialPort!=null;
    }

    public synchronized boolean isDTR() {
        return serialPort.isDTR();
    }

    public synchronized void setDTR(boolean newDTR) {
        serialPort.setDTR(newDTR);
    }

    public synchronized boolean isRTS() {
        return serialPort.isRTS();
    }

    public synchronized void setRTS(boolean newRTS) {
        serialPort.setRTS(newRTS);
    }

    /**
     * Send a break on this serial port.
     *
     * @param  duration - break duration in ms.
     */
    public synchronized void sendBreak(int duration){
        serialPort.sendBreak(duration);
    }

    public synchronized int getBaudRate() {
        return serialPort.getBaudRate();
    }

    public synchronized void setBaudRate(int baudRate) throws IOException {

        ThreadContext.push("setBaudRate(" + baudRate + ")");

        try {
            checkOpen();

            try {
                // set baud rate
                serialPort.setSerialPortParams(baudRate,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                logger.debug("Set baudRate={}", baudRate);

            } catch(UnsupportedCommOperationException ex) {
                throw new IOException("Failed to set baud rate: ", ex);
            }
        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Close this serial port.
     */
    public synchronized void closePort() {

        logger.debug("close(): {}", portName);

        serialPort.close();
        serialPort = null;
        serialInputStream = null;
        serialOutputStream = null;
    }

    public synchronized void flush() throws IOException {

        logger.debug("SerialService.flush");

        checkOpen();
        serialOutputStream.flush();

        while(serialInputStream.available() > 0) {
            serialInputStream.read(); // NOSONAR this data is meant to be discarded
        }
    }

    /**
     * Gets exclusive use of the 1-Wire to communicate with an iButton or
     * 1-Wire Device.
     *
     * This method should be used for critical sections of code where a
     * sequence of commands must not be interrupted by communication of
     * threads with other iButtons, and it is permissible to sustain
     * a delay in the special case that another thread has already been
     * granted exclusive access and this access has not yet been
     * relinquished.
     */
    public void beginExclusive() {
        logger.debug("{}: beginExclusive()", portName);
        theLock.lock();
    }

    /**
     * Relinquishes exclusive control of the 1-Wire Network.
     * This command dynamically marks the end of a critical section and
     * should be used when exclusive control is no longer needed.
     */
    public synchronized void endExclusive () {
        logger.debug("{}: endExclusive()", portName);
        theLock.unlock();
    }

    public synchronized int available() throws IOException {
        checkOpen();
        return serialInputStream.available();
    }

    public synchronized int read() throws IOException {
        checkOpen();
        return serialInputStream.read();
    }

    public synchronized int read(byte[] buffer) throws IOException {
        checkOpen();
        return read(buffer, 0, buffer.length);
    }

    public synchronized int read(byte[] buffer, int offset, int length) throws IOException {
        checkOpen();
        return serialInputStream.read(buffer, offset, length);
    }

    public synchronized int readWithTimeout(byte[] buffer, int offset, int length) throws IOException {

        ThreadContext.push("readWithTimeout");

        try {

            checkOpen();

            // set timeout to be very long
            var timeout = Duration.ofMillis(length * 20L + 800);

            logger.debug("SerialService.readWithTimeout(): length={}, timeout={}", length, timeout);


            int count = byteBang
                    ? readWithTimeoutByteBang(buffer, offset, length, timeout)
                    : readWithTimeoutNoByteBang(buffer, offset, length, timeout);

            logger.debug("read {} bytes: {}", () -> count, () -> Convert.toHexString(buffer, offset, count));
            return count;

        } finally {
            ThreadContext.pop();
        }
    }

    private int readWithTimeoutByteBang(byte[] buffer, int offset, int length, Duration timeout) throws IOException {

        ThreadContext.push("readWithTimeoutByteBang");

        var start = clock.instant();
        int count = 0;
        try {

            do {

                var read = serialInputStream.read();

                if (read != -1) {

                    buffer[count+offset] = (byte)read;
                    count++;

                } else {

                    if (clock.instant().isAfter(start.plus(timeout))) {
                        logger.debug("premature return, timeout ({}) exceeded", timeout);
                        return count;
                    }

                    // no bytes available yet so yield
                    Thread.yield();

                    logger.debug("yield ended");
                }

            } while (length > count);

            return count;

        } finally {
            logger.debug("returning {}", count);
            ThreadContext.pop();
        }
    }

    private int readWithTimeoutNoByteBang(byte[] buffer, int offset, int length, Duration timeout) throws IOException {

        var start = clock.instant();
        int count = 0;

        do {

            int available = serialInputStream.available();

            if (available > 0) {

                // check for block bigger then buffer
                if (available + count > length) {
                    available = length - count;
                }

                // read the block
                count += serialInputStream.read(buffer, count + offset, available);

            } else {

                // check for timeout
                if (clock.instant().isAfter(start.plus(timeout))) {
                    length = 0;
                }

                Thread.yield();
            }

        } while (length > count);

        return count;
    }

    public synchronized char[] readWithTimeout(int length) throws IOException {

        byte[] buffer = new byte[length];

        int count = readWithTimeout(buffer, 0, length);

        if (length != count) {
            throw new IOException("readWithTimeout, timeout waiting for return bytes (wanted " + length + ", got " + count + ")");
        }

        char[] returnBuffer = new char[length];

        for(int i = 0; i < length; i++) {
            returnBuffer[i] = (char) (buffer[i] & 0x00FF);
        }

        return returnBuffer;
    }

    public synchronized void write(int data) throws IOException {

        ThreadContext.push("write");

        checkOpen();

        logger.debug("data: {}", () -> Convert.toHexString((byte)data));

        try {

            serialOutputStream.write(data);
            serialOutputStream.flush();

        } catch (IOException e) {

            // drain IOExceptions that are 'Interrrupted' on Linux
            // convert the rest to IOExceptions

            if (!(System.getProperty("os.name").contains("Linux")
                    && e.toString().contains("Interrupted"))) {
                throw new IOException("write(char): " + e);
            }
        }
    }

    public synchronized void write(byte[] data, int offset, int length) throws IOException {

        ThreadContext.push("write");

        try {

            checkOpen();

            logger.debug("length: {} bytes", length);
            logger.debug("data: {}", () -> Convert.toHexString(data, offset, length));

            try {

                serialOutputStream.write(data, offset, length);
                serialOutputStream.flush();

            } catch (IOException e) {

                // drain IOExceptions that are 'Interrrupted' on Linux
                // convert the rest to IOExceptions

                if (!(System.getProperty("os.name").contains("Linux")
                        && e.toString().contains("Interrupted"))) {
                    throw new IOException("write(char): " + e);
                }
            }

        } finally {
            ThreadContext.pop();
        }
    }

    public synchronized void write(byte[] data) throws IOException {
        write(data, 0, data.length);
    }

    public synchronized void write(String data) throws IOException {
        byte[] dataBytes = data.getBytes();
        write(dataBytes, 0, dataBytes.length);
    }

    public synchronized void write(char data) throws IOException {
        write((int)data);
    }

    public synchronized void write(char[] data) throws IOException {
        write(data, 0, data.length);
    }

    public synchronized void write(char[] data, int offset, int length) throws IOException {

        ThreadContext.push("write");

        try {

            if (length > tempArray.length) {

                logger.warn("Extending temp buffer to {} bytes", length);

                tempArray = new byte[length];
            }

            for (int i=0; i<length; i++) {
                tempArray[i] = (byte) data[i];
            }

            write(tempArray, 0, length);

        } finally {
            ThreadContext.pop();
        }
    }

    private void checkOpen() {
        if (!isPortOpen()) {
            throw new IllegalStateException(portName + ": port not open");
        }
    }
}
