
/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

package com.dalsemi.onewire.container;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.utils.CRC8;



/**
 * <P> 1-Wire container for temperature iButton which measures temperatures
 * from -55&#176C to +100&#176C, DS1920 or DS18S20.  This container encapsulates the
 * functionality of the iButton family type <B>10</B> (hex)</P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> Measures temperatures from -55&#176C to +100&#176C in typically 0.2 seconds
 *   <LI> Zero standby power
 *   <LI> 0.5&#176C resolution, digital temperature reading in two's complement
 *   <LI> Increased resolution through interpolation in internal counters
 *   <LI> 8-bit device-generated CRC for data integrity
 *   <LI> Special command set allows user to skip ROM section and do temperature
 *        measurements simultaneously for all devices on the bus
 *   <LI> 2 bytes of EEPROM to be used either as alarm triggers or user memory
 *   <LI> Alarm search directly indicates which device senses alarming temperatures
 * </UL>
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> See the usage example in
 * {@link TemperatureContainer}
 * for temperature specific operations.
 * </DL>
 *
 * <H3> DataSheet </H3>
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1920.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1920.pdf</A>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS18S20.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS18S20.pdf</A>
 * </DL>
 *
 * @see TemperatureContainer
 *
 * @author     DS,JK
 * @author Converted to use TemperatureContainer interface 9-1-2000 KLA
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 */
public class OneWireContainer10 extends OneWireContainer implements TemperatureContainer {

    private boolean normalResolution = true;

    /**
     * default temperature resolution for this <code>OneWireContainer10</code>
     * device.
     */
    public static final double RESOLUTION_NORMAL = 0.5;

    /**
     * maximum temperature resolution for this <code>OneWireContainer10</code>
     * device. Use <code>RESOLUTION_MAXIMUM</code> in
     * <code>setResolution()</code> if higher resolution is desired.
     */
    public static final double RESOLUTION_MAXIMUM = 0.1;

    /** DS1920 convert temperature command  */
    private static final byte CONVERT_TEMPERATURE_COMMAND = 0x44;

    /** DS1920 read data from scratchpad command      */
    private static final byte READ_SCRATCHPAD_COMMAND = ( byte ) 0xBE;

    /** DS1920 write data to scratchpad command     */
    private static final byte WRITE_SCRATCHPAD_COMMAND = ( byte ) 0x4E;

    /** DS1920 copy data from scratchpad to EEPROM command     */
    private static final byte COPY_SCRATCHPAD_COMMAND = ( byte ) 0x48;

    /** DS1920 recall EEPROM command       */
    //private static final byte RECALL_EEPROM_COMMAND = ( byte ) 0xB8;


    /**
     * Creates an empty <code>OneWireContainer10</code>.  Must call
     * <code>setupContainer()</code> before using this new container.<p>
     *
     * This is one of the methods to construct a <code>OneWireContainer10</code>.
     * The others are through creating a <code>OneWireContainer10</code> with
     * parameters.
     *
     * @see #OneWireContainer10(DSPortAdapter,byte[])
     * @see #OneWireContainer10(DSPortAdapter,long)
     * @see #OneWireContainer10(DSPortAdapter,String)
     */
    public OneWireContainer10 () {
    }

    /**
     * Creates a <code>OneWireContainer10</code> with the provided adapter
     * object and the address of this One-Wire device.
     *
     * This is one of the methods to construct a <code>OneWireContainer10</code>.
     * The others are through creating a <code>OneWireContainer10</code> with
     * different parameters types.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     *                           this One-Wire device
     * @param  newAddress        address of this One-Wire device
     *
     * @see com.dalsemi.onewire.utils.Address
     * @see #OneWireContainer10()
     * @see #OneWireContainer10(DSPortAdapter,long)
     * @see #OneWireContainer10(DSPortAdapter,String)
     */
    public OneWireContainer10 (DSPortAdapter sourceAdapter, byte[] newAddress) {
        super(sourceAdapter, newAddress);
    }

    /**
     * Creates a <code>OneWireContainer10</code> with the provided adapter
     * object and the address of this One-Wire device.
     *
     * This is one of the methods to construct a <code>OneWireContainer10</code>.
     * The others are through creating a <code>OneWireContainer10</code> with
     * different parameters types.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     *                           this One-Wire device
     * @param  newAddress        address of this One-Wire device
     *
     * @see com.dalsemi.onewire.utils.Address
     * @see #OneWireContainer10()
     * @see #OneWireContainer10(DSPortAdapter,byte[])
     * @see #OneWireContainer10(DSPortAdapter,String)
     */
    public OneWireContainer10 (DSPortAdapter sourceAdapter, long newAddress) {
        super(sourceAdapter, newAddress);
    }

    /**
     * Creates a <code>OneWireContainer10</code> with the provided adapter
     * object and the address of this One-Wire device.
     *
     * This is one of the methods to construct a <code>OneWireContainer10</code>.
     * The others are through creating a <code>OneWireContainer10</code> with
     * different parameters types.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     *                           this One-Wire device
     * @param  newAddress        address of this One-Wire device
     *
     * @see com.dalsemi.onewire.utils.Address
     * @see #OneWireContainer10()
     * @see #OneWireContainer10(DSPortAdapter,byte[])
     * @see #OneWireContainer10(DSPortAdapter,long)
     */
    public OneWireContainer10 (DSPortAdapter sourceAdapter, String newAddress) {
        super(sourceAdapter, newAddress);
    }

    //--------
    //-------- Information methods
    //--------

    @Override
    public String getName () {
        return "DS1920";
    }

    @Override
    public String getAlternateNames () {
        return "DS18S20";
    }

    @Override
    public String getDescription () {
        return "Digital thermometer measures temperatures from "
        + "-55C to 100C in typically 0.2 seconds.  +/- 0.5C "
        + "Accuracy between 0C and 70C. 0.5C standard "
        + "resolution, higher resolution through interpolation.  "
        + "Contains high and low temperature set points for "
        + "generation of alarm.";
    }

    @Override
    public boolean hasTemperatureAlarms() {
        return true;
    }

    @Override
    public boolean hasSelectableTemperatureResolution () {
        return true;
    }

    @Override
    public double[] getTemperatureResolutions () {

        return new double[] { RESOLUTION_NORMAL, RESOLUTION_MAXIMUM };
    }

    @Override
    public double getTemperatureAlarmResolution() {
        return 1.0;
    }

    @Override
    public double getMaxTemperature() {
        return 100.0;
    }

    @Override
    public double getMinTemperature () {
        return -55.0;
    }

    @Override
    public void doTemperatureConvert (byte[] state) throws OneWireException {

        doSpeed();

        // select the device
        if (adapter.select(address)) {

            // Setup Power Delivery
            adapter.setPowerDuration(DSPortAdapter.PowerDeliveryDuration.INFINITE);
            adapter.startPowerDelivery(DSPortAdapter.PowerChangeCondition.AFTER_NEXT_BYTE);

            // send the convert temperature command
            adapter.putByte(CONVERT_TEMPERATURE_COMMAND);

            // delay for 750 ms
            try {
                Thread.sleep(750);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                logger.debug("Interrupted", ex);
            }

            // Turn power back to normal.
            adapter.setPowerNormal();

            // check to see if the temperature conversion is over
            if (adapter.getByte() != 0x0FF) {
                throw new OneWireIOException(address, "OneWireContainer10-temperature conversion not complete");
            }

            // read the result
            byte mode = state [4];   //preserve the resolution in the state

            adapter.select(address);
            readScratch(state);

            state [4] = mode;
        } else {

            // device must not have been present
            throw new OneWireIOException(address, "OneWireContainer10-device not present");
        }
    }

    @Override
    public double getTemperature (byte[] state) throws OneWireIOException {

        //on some parts, namely the 18S20, you can get invalid readings.
        //basically, the detection is that all the upper 8 bits should
        //be the same by sign extension.  the error condition (DS18S20
        //returns 185.0+) violated that condition
        if (((state [1] & 0x0ff) != 0x00) && ((state [1] & 0x0ff) != 0x0FF)) {
            throw new OneWireIOException(address, "Invalid temperature data!");
        }

        short temp = ( short ) ((state [0] & 0x0ff) | (state [1] << 8));

        if (state [4] == 1) {
            temp = ( short ) (temp >> 1);   //lop off the last bit

            //also takes care of the / 2.0
            double tmp = ( double ) temp;
            double cr  = (state [6] & 0x0ff);
            double cpc = (state [7] & 0x0ff);

            //just let the thing throw a divide by zero exception
            tmp = tmp - ( double ) 0.25 + (cpc - cr) / cpc;

            return tmp;

        } else {

            //do normal resolution
            return temp / 2.0;
        }
    }

    @Override
    public double getTemperatureAlarm(int alarmType, byte[] state) {

        return ( double ) state [alarmType == ALARM_LOW ? 3 : 2];
    }

    @Override
    public double getTemperatureResolution(byte[] state) {

        if (state [4] == 0) {
            return RESOLUTION_NORMAL;
        }

        return RESOLUTION_MAXIMUM;
    }

    @Override
    public void setTemperatureAlarm (int alarmType, double alarmValue, byte[] state) {
        if ((alarmType != ALARM_LOW) && (alarmType != ALARM_HIGH)) {
            throw new IllegalArgumentException("Invalid alarm type.");
        }

        if (alarmValue > 100.0 || alarmValue < -55.0) {
            throw new IllegalArgumentException(
            "Value for alarm not in accepted range.  Must be -55 C <-> +100 C.");
        }

        state [(alarmType == ALARM_LOW) ? 3 : 2] = ( byte ) alarmValue;
    }

    @Override
    public synchronized void setTemperatureResolution (double resolution, byte[] state) {
        if (resolution == RESOLUTION_NORMAL) {
            normalResolution = true;
        } else {
            normalResolution = false;
        }

        state [4] = ( byte ) (normalResolution ? 0 : 1);
    }

    /**
     * Retrieves this <code>OneWireContainer10</code> state information.
     * The state information is returned as a byte array.  Pass this byte
     * array to the '<code>get</code>' and '<code>set</code>' methods.
     * If the device state needs to be changed, then call the
     * <code>writeDevice()</code> to finalize the changes.
     *
     * @return <code>OneWireContainer10</code> state information.
     * Device state looks like this:
     * <pre>
     *   0 : temperature LSB
     *   1 : temperature MSB
     *   2 : trip high
     *   3 : trip low
     *   4 : reserved (put the resolution here, 0 for normal, 1 for max)
     *   5 : reserved
     *   6 : count remain
     *   7 : count per degree Celsius
     *   8 : an 8 bit CRC over the previous 8 bytes of data
     * </pre>
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         reading an incorrect CRC from this <code>OneWireContainer10</code>.
     *         This could be caused by a physical interruption in the 1-Wire
     *         Network due to shorts or a newly arriving 1-Wire device issuing a
     *         'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter
     *
     * @see    #writeDevice
     *
     * @deprecated Use {@link #readDevice(byte[])} instead, it doesn't allocate memory.
     */
    @Override
    @Deprecated(forRemoval = false)
    public byte[] readDevice() throws OneWireException {

        var data = new byte [8];

        readDevice(data);

        return data;
    }

    @Override
    public void readDevice(byte[] outputBuffer) throws OneWireException {

        doSpeed();

        // select the device
        if (adapter.select(address)) {

            // construct a block to read the scratchpad
            // 10 bytes
            byte[] buffer = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00,
                    (byte) 0x00,(byte) 0x00,
                    };

            // read scratchpad command
            buffer [0] = ( byte ) READ_SCRATCHPAD_COMMAND;

            // now add the read bytes for data bytes and crc8
            for (int i = 1; i < 10; i++) {
                buffer [i] = ( byte ) 0x0FF;
            }

            // send the block
            adapter.dataBlock(buffer, 0, buffer.length);

            // see if crc is correct
            if (CRC8.compute(buffer, 1, 9) == 0) {
                System.arraycopy(buffer, 1, outputBuffer, 0, 8);
            } else {
                throw new OneWireIOException(address, "OneWireContainer10-Error reading CRC8 from device.");
            }

        } else {
            throw new OneWireIOException(address, "OneWireContainer10-Device not found on 1-Wire Network");
        }

        //we are just reading normalResolution here, no need to synchronize
        outputBuffer [4] = ( byte ) (normalResolution ? 0 : 1);
    }

    @Override
    public void writeDevice (byte[] state) throws OneWireException {

        doSpeed();

        var temp = new byte [2];

        temp [0] = state [2];
        temp [1] = state [3];

        // Write it to the Scratchpad.
        writeScratchpad(temp);

        // Place in memory.
        copyScratchpad();
    }

    /**
     * Reads the 8 bytes from the scratchpad and verify CRC8 returned.
     *
     * @param  data  buffer to store the scratchpad data
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         reading an incorrect CRC from this <code>OneWireContainer10</code>.
     *         This could be caused by a physical interruption in the 1-Wire
     *         Network due to shorts or a newly arriving 1-Wire device issuing a
     *         'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter
     */
    private void readScratch (byte[] data) throws OneWireException {

        // select the device
        if (adapter.select(address)) {

            // construct a block to read the scratchpad
            var buffer = new byte [10];

            // read scratchpad command
            buffer [0] = ( byte ) READ_SCRATCHPAD_COMMAND;

            // now add the read bytes for data bytes and crc8
            for (int i = 1; i < 10; i++)
                buffer [i] = ( byte ) 0x0FF;

            // send the block
            adapter.dataBlock(buffer, 0, buffer.length);

            // see if crc is correct
            if (CRC8.compute(buffer, 1, 9) == 0) {
                System.arraycopy(buffer, 1, data, 0, 8);
            } else {
                throw new OneWireIOException(address, "OneWireContainer10-Error reading CRC8 from device.");
            }
        } else {
            throw new OneWireIOException(address, "OneWireContainer10-Device not found on 1-Wire Network");
        }
    }

    /**
     * Writes to the Scratchpad.
     *
     * @param data this is the data to be written to the scratchpad.  Cannot
     *             be more than two bytes in size. First byte of data must be
     *             the temperature High Trip Point and second byte must be
     *             temperature Low Trip Point.
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         reading an incorrect CRC from this <code>OneWireContainer10</code>.
     *         This could be caused by a physical interruption in the 1-Wire
     *         Network due to shorts or a newly arriving 1-Wire device issuing a
     *         'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter
     * @throws IllegalArgumentException when data length is not equal to <code>2</code>
     */
    private void writeScratchpad (byte[] data) throws OneWireException, IllegalArgumentException {

        // Variables.
        var write_block = new byte [3];
        var buffer      = new byte [8];

        // First do some error checking.
        if (data.length != 2)
            throw new IllegalArgumentException(
            "Bad data.  Data must consist of only TWO bytes.");

        // Prepare the write_block to be sent.
        write_block [0] = WRITE_SCRATCHPAD_COMMAND;
        write_block [1] = data [0];
        write_block [2] = data [1];

        // Send the block of data to the DS1920.
        if (adapter.select(address)) {
            adapter.dataBlock(write_block, 0, 3);
        } else {
            throw new OneWireIOException(address, "OneWireContainer10 - Device not found");
        }

        // Check data to ensure correctly recived.
        buffer = new byte [8];

        readScratch(buffer);

        // verify data
        if ((buffer [2] != data [0]) || (buffer [3] != data [1])) {
            throw new OneWireIOException(address, "OneWireContainer10 - data read back incorrect");
        }
    }

    /**
     * Copies the contents of the User bytes of the ScratchPad to the EEPROM.
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         reading an incorrect CRC from this <code>OneWireContainer10</code>.
     *         This could be caused by a physical interruption in the 1-Wire
     *         Network due to shorts or a newly arriving 1-Wire device issuing a
     *         'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter
     */
    private void copyScratchpad () throws OneWireException {

        // select the device
        if (adapter.select(address)) {

            // send the copy command
            adapter.putByte(COPY_SCRATCHPAD_COMMAND);

            // Setup Power Delivery
            adapter.setPowerDuration(DSPortAdapter.PowerDeliveryDuration.INFINITE);
            adapter.startPowerDelivery(DSPortAdapter.PowerChangeCondition.NOW);

            // delay for 10 ms
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex){
                Thread.currentThread().interrupt();
                logger.debug("Interrupted", ex);
            }

            // Turn power back to normal.
            adapter.setPowerNormal();

        } else {
            throw new OneWireIOException(address, "OneWireContainer10 - device not found");
        }
    }
}
