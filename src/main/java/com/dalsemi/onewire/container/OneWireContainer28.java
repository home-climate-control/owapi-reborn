
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

//----------------------------------------------------------------------------

/**
 * <P> 1-Wire container for temperature iButton which measures temperatures
 * from -55&#176C to +125&#176C, DS18B20.  This container encapsulates the
 * functionality of the iButton family type <B>28</B> (hex)</P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> Measures temperatures from -55&#176C to +125&#176C. Fahrenheit
 *        equivalent is -67&#176F to +257&#176F
 *   <LI> Power supply range is 3.0V to 5.5V
 *   <LI> Zero standby power
 *   <LI> +/- 0.5&#176C accuracy from -10&#176C to +85&#176C
 *   <LI> Thermometer resolution programmable from 9 to 12 bits
 *   <LI> Converts 12-bit temperature to digital word in 750 ms (max.)
 *   <LI> User-definable, nonvolatile temperature alarm settings
 *   <LI> Alarm search command identifies and addresses devices whose temperature is
 *        outside of programmed limits (temperature alarm condition)
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
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS18B20.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS18B20.pdf</A>
 * </DL>
 *
 * @see TemperatureContainer
 *
 * @author     BH
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 */
public class OneWireContainer28 extends OneWireContainer implements TemperatureContainer {

    /**
     * DS18B20 reads power supply command.  This command is used to determine
     * if external power is supplied.
     */
    public static final byte READ_POWER_SUPPLY_COMMAND = ( byte ) 0xB4;

    /** DS18B20 12-bit resolution constant for CONFIG byte  */
    public static final byte RESOLUTION_12_BIT = ( byte ) 0x7F;

    /** DS18B20 11-bit resolution constant for CONFIG byte  */
    public static final byte RESOLUTION_11_BIT = ( byte ) 0x5F;

    /** DS18B20 10-bit resolution constant for CONFIG byte  */
    public static final byte RESOLUTION_10_BIT = ( byte ) 0x3F;

    /** DS18B20 9-bit resolution constant for CONFIG byte   */
    public static final byte RESOLUTION_9_BIT = ( byte ) 0x1F;

    /**
     * Creates an empty <code>OneWireContainer28</code>.  Must call
     * <code>setupContainer()</code> before using this new container.<p>
     *
     * This is one of the methods to construct a <code>OneWireContainer28</code>.
     * The others are through creating a <code>OneWireContainer28</code> with
     * parameters.
     *
     * @see #OneWireContainer28(DSPortAdapter,byte[])
     * @see #OneWireContainer28(DSPortAdapter,long)
     * @see #OneWireContainer28(DSPortAdapter,String)
     */
    public OneWireContainer28() {
    }

    /**
     * Creates a <code>OneWireContainer28</code> with the provided adapter
     * object and the address of this One-Wire device.
     *
     * This is one of the methods to construct a <code>OneWireContainer28</code>.
     * The others are through creating a <code>OneWireContainer28</code> with
     * different parameters types.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     *                           this One-Wire device
     * @param  newAddress        address of this One-Wire device
     *
     * @see com.dalsemi.onewire.utils.Address
     * @see #OneWireContainer28()
     * @see #OneWireContainer28(DSPortAdapter,long)
     * @see #OneWireContainer28(DSPortAdapter,String)
     */
    public OneWireContainer28(DSPortAdapter sourceAdapter, byte[] newAddress) {
        super(sourceAdapter, newAddress);
    }

    /**
     * Creates a <code>OneWireContainer28</code> with the provided adapter
     * object and the address of this One-Wire device.
     *
     * This is one of the methods to construct a <code>OneWireContainer28</code>.
     * The others are through creating a <code>OneWireContainer28</code> with
     * different parameters types.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     *                           this One-Wire device
     * @param  newAddress        address of this One-Wire device
     *
     * @see com.dalsemi.onewire.utils.Address
     * @see #OneWireContainer28()
     * @see #OneWireContainer28(DSPortAdapter,byte[])
     * @see #OneWireContainer28(DSPortAdapter,String)
     */
    public OneWireContainer28(DSPortAdapter sourceAdapter, long newAddress) {
        super(sourceAdapter, newAddress);
    }

    /**
     * Creates a <code>OneWireContainer28</code> with the provided adapter
     * object and the address of this One-Wire device.
     *
     * This is one of the methods to construct a <code>OneWireContainer28</code>.
     * The others are through creating a <code>OneWireContainer28</code> with
     * different parameters types.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     *                           this One-Wire device
     * @param  newAddress        address of this One-Wire device
     *
     * @see com.dalsemi.onewire.utils.Address
     * @see #OneWireContainer28()
     * @see #OneWireContainer28(DSPortAdapter,byte[])
     * @see #OneWireContainer28(DSPortAdapter,long)
     */
    public OneWireContainer28(DSPortAdapter sourceAdapter, String newAddress) {
        super(sourceAdapter, newAddress);
    }

    @Override
    public String getName() {
        return "DS18B20";
    }

    @Override
    public String getAlternateNames() {
        return "DS1820B, DS18B20X";
    }

    @Override
    public String getDescription() {

        return "Digital thermometer measures temperatures from "
        + "-55C to 125C in 0.75 seconds (max).  +/- 0.5C "
        + "accuracy between -10C and 85C. Thermometer "
        + "resolution is programmable at 9, 10, 11, and 12 bits. ";
    }

    @Override
    public boolean hasTemperatureAlarms() {
        return true;
    }

    @Override
    public boolean hasSelectableTemperatureResolution() {
        return true;
    }

    @Override
    public double[] getTemperatureResolutions() {

        double[] resolutions = new double [4];

        resolutions [0] = 0.5;      // 9-bit
        resolutions [1] = 0.25;     // 10-bit
        resolutions [2] = 0.125;    // 11-bit
        resolutions [3] = 0.0625;   // 12-bit

        return resolutions;
    }

    @Override
    public double getTemperatureAlarmResolution() {
        return 1.0;
    }

    @Override
    public double getMaxTemperature() {
        return 125.0;
    }

    @Override
    public double getMinTemperature() {
        return -55.0;
    }

    @Override
    public void doTemperatureConvert(byte[] state) throws OneWireException {

        int msDelay = 750;   // in milliseconds

        // select the device
        if (!adapter.select(address)) {
            // device must not have been present
            throw new OneWireIOException(address, "OneWireContainer28-device not present");
        }

        // Setup Power Delivery
        adapter.setPowerDuration(DSPortAdapter.PowerDeliveryDuration.INFINITE);
        adapter.startPowerDelivery(DSPortAdapter.PowerChangeCondition.AFTER_NEXT_BYTE);
        // send the convert temperature command
        adapter.putByte(Command.CONVERT_TEMPERATURE.code);

        // calculate duration of delay according to resolution desired
        switch (state[4]) {

        case RESOLUTION_9_BIT:
            msDelay = 94;
            break;
        case RESOLUTION_10_BIT:
            msDelay = 188;
            break;
        case RESOLUTION_11_BIT:
            msDelay = 375;
            break;
        case RESOLUTION_12_BIT:
            msDelay = 750;
            break;
        default:
            msDelay = 750;
        } // switch

        // delay for specified amount of time
        try {
            Thread.sleep(msDelay);
        } catch (InterruptedException ex) {
            logger.debug("Interrupted", ex);
        }

        // Turn power back to normal.
        adapter.setPowerNormal();

        // check to see if the temperature conversion is over
        if (adapter.getByte() != 0xFF) {
            throw new OneWireIOException(address, "OneWireContainer28-temperature conversion not complete");
        }
    }

    @Override
    public double getTemperature(byte[] state) throws OneWireIOException {

        // Take these three steps:
        // 1)  Make an 11-bit integer number out of MSB and LSB of the first 2 bytes from scratchpad
        // 2)  Divide final number by 16 to retrieve the floating point number.
        // 3)  Afterwards, test for the following temperatures:
        //     0x07D0 = 125.0C
        //     0x0550 = 85.0C
        //     0x0191 = 25.0625C
        //     0x00A2 = 10.125C
        //     0x0008 = 0.5C
        //     0x0000 = 0.0C
        //     0xFFF8 = -0.5C
        //     0xFF5E = -10.125C
        //     0xFE6F = -25.0625C
        //     0xFC90 = -55.0C
        int    inttemperature = state [1];   // inttemperature is automatically sign extended here.

        inttemperature = (inttemperature << 8) | (state [0] & 0xFF);   // this converts 2 bytes into integer
        return (( double ) inttemperature / ( double ) 16);   // converts integer to a double
    }

    @Override
    public double getTemperatureAlarm(int alarmType, byte[] state) {
        return state[alarmType == ALARM_LOW ? 3 : 2];
    }

    @Override
    public double getTemperatureResolution(byte[] state) {

        double tempres;

        // calculate temperature resolution according to configuration byte
        switch (state[4]) {

        case RESOLUTION_9_BIT:
            tempres = 0.5;
            break;
        case RESOLUTION_10_BIT:
            tempres = 0.25;
            break;
        case RESOLUTION_11_BIT:
            tempres = 0.125;
            break;
        case RESOLUTION_12_BIT:
            tempres = 0.0625;
            break;
        default:
            tempres = 0.0;
        } // switch

        return tempres;
    }

    @Override
    public void setTemperatureAlarm(int alarmType, double alarmValue,
            byte[] state) throws OneWireIOException {

        if ((alarmType != ALARM_LOW) && (alarmType != ALARM_HIGH)) {
            throw new IllegalArgumentException("Invalid alarm type.");
        }

        if (alarmValue > 125.0 || alarmValue < -55.0) {
            throw new IllegalArgumentException(
            "Value for alarm not in accepted range.  Must be -55 C <-> +125 C.");
        }

        state[(alarmType == ALARM_LOW) ? 3 : 2] = (byte) alarmValue;
    }

    /**
     * Sets the current temperature resolution in Celsius in the provided
     * <code>state</code> data.   Use the method <code>writeDevice()</code>
     * with this data to finalize the change to the device.
     *
     * @param  resolution temperature resolution in Celsius. Valid values are
     *                    <code>RESOLUTION_9_BIT</code>,
     *                    <code>RESOLUTION_10_BIT</code>,
     *                    <code>RESOLUTION_11_BIT</code> and
     *                    <code>RESOLUTION_12_BIT</code>.
     * @param  state      byte array with device state information
     *
     * @see    #RESOLUTION_9_BIT
     * @see    #RESOLUTION_10_BIT
     * @see    #RESOLUTION_11_BIT
     * @see    #RESOLUTION_12_BIT
     * @see    #hasSelectableTemperatureResolution
     * @see    #getTemperatureResolution
     * @see    #getTemperatureResolutions
     */
    @Override
    public synchronized void setTemperatureResolution(double resolution, byte[] state) throws OneWireException {

        byte configbyte = RESOLUTION_12_BIT;

        // calculate configbyte from given resolution
        if (resolution == 0.5) {
            configbyte = RESOLUTION_9_BIT;
        }

        if (resolution == 0.25) {
            configbyte = RESOLUTION_10_BIT;
        }

        if (resolution == 0.125) {
            configbyte = RESOLUTION_11_BIT;
        }

        if (resolution == 0.0625) {
            configbyte = RESOLUTION_12_BIT;
        }

        state[4] = configbyte;
    }

    /**
     * Retrieves this <code>OneWireContainer28</code> state information.
     * The state information is returned as a byte array.  Pass this byte
     * array to the '<code>get</code>' and '<code>set</code>' methods.
     * If the device state needs to be changed, then call the
     * <code>writeDevice()</code> to finalize the changes.
     *
     * @return <code>OneWireContainer28</code> state information.
     * Device state looks like this:
     * <pre>
     *   0 : temperature LSB
     *   1 : temperature MSB
     *   2 : trip high
     *   3 : trip low
     *   4 : configuration register (for resolution)
     *   5 : reserved
     *   6 : reserved
     *   7 : reserved
     *   8 : an 8 bit CRC of the previous 8 bytes
     * </pre>
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
     *         This could be caused by a physical interruption in the 1-Wire
     *         Network due to shorts or a newly arriving 1-Wire device issuing a
     *         'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter
     *
     * @see    #writeDevice
     */
    @Override
    public byte[] readDevice() throws OneWireException {

        // VT: NOTE: Yeah, this is real consistent... it's 9 bytes, but why, oh why they buried it that deep???

        return recallE2();
    }

    @Override
    public void readDevice(byte[] buffer) throws OneWireException {

        recallE2(buffer);
    }

    @Override
    public void writeDevice(byte[] state) throws OneWireException {

        byte[] temp = {state[2], state[3], state[4]};

        // Write it to the Scratchpad.
        writeScratchpad(temp);

        // Place in memory.
        copyScratchpad();
    }

    /**
     * Reads the Scratchpad of the DS18B20.
     *
     * @return 9-byte buffer representing the scratchpad
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
     *         This could be caused by a physical interruption in the 1-Wire
     *         Network due to shorts or a newly arriving 1-Wire device issuing a
     *         'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter
     */
    public byte[] readScratchpad() throws OneWireException {

        byte[] resultBlock = new byte [9];

        readScratchpad(resultBlock);

        return resultBlock;
    }

    public void readScratchpad(byte[] resultBlock) throws OneWireException {

        // select the device
        if (!adapter.select(address)) {
            // device must not have been present
            throw new OneWireIOException(address, "OneWireContainer28-Device not found on 1-Wire Network");
        }

        // create a block to send that reads the scratchpad
        // 10 bytes
        byte[] sendBlock = {
                Command.READ_SCRATCHPAD.code, (byte) 0xFF, (byte) 0xFF,(byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,(byte) 0xFF,
                (byte) 0xFF,(byte) 0xFF,
        };

        // send the block
        adapter.dataBlock(sendBlock, 0, sendBlock.length);

        // now, sendBlock contains the 9-byte Scratchpad plus READ_SCRATCHPAD_COMMAND byte
        // convert the block to a 9-byte array representing Scratchpad (get rid of first byte)

        for (int i = 0; i < 9; i++) {
            resultBlock[i] = sendBlock[i + 1];
        }

        // see if CRC8 is correct
        if (CRC8.compute(sendBlock, 1, 9) == 0) {
            return;
        }

        throw new OneWireIOException(address, "OneWireContainer28-Error reading CRC8 from device.");
    }

    /**
     * Writes to the Scratchpad of the DS18B20.
     *
     * @param data data to be written to the scratchpad.  First
     *             byte of data must be the temperature High Trip Point, the
     *             second byte must be the temperature Low Trip Point, and
     *             the third must be the Resolution (configuration register).
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
     *         This could be caused by a physical interruption in the 1-Wire
     *         Network due to shorts or a newly arriving 1-Wire device issuing a
     *         'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter
     * @throws IllegalArgumentException when data is of invalid length
     */
    public void writeScratchpad(byte[] data) throws OneWireException {

        // setup buffer to write to scratchpad
        byte[] writeBuffer = {Command.WRITE_SCRATCHPAD.code, data[0], data[1], data[2]};

        // send command block to device
        if (!adapter.select(address)) {
            // device must not have been present
            throw new OneWireIOException(address, "OneWireContainer28-Device not found on 1-Wire Network");
        }

        adapter.dataBlock(writeBuffer, 0, writeBuffer.length);

        // double check by reading scratchpad
        byte[] readBuffer;

        readBuffer = readScratchpad();

        if ((readBuffer[2] != data[0]) || (readBuffer[3] != data[1])
                || (readBuffer[4] != data[2])) {

            // writing to scratchpad failed
            throw new OneWireIOException(address, "OneWireContainer28-Error writing to scratchpad");
        }
    }

    /**
     * Copies the Scratchpad to the E-squared memory of the DS18B20.
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
     *         This could be caused by a physical interruption in the 1-Wire
     *         Network due to shorts or a newly arriving 1-Wire device issuing a
     *         'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter
     */
    public void copyScratchpad() throws OneWireException {

        // first, let's read the scratchpad to compare later.
        byte[] readfirstbuffer;

        readfirstbuffer = readScratchpad();

        // second, let's copy the scratchpad.
        if (!adapter.select(address)) {
            // device must not have been present
            throw new OneWireIOException(address, "OneWireContainer28-Device not found on 1-Wire Network");
        }

        // apply the power delivery
        adapter.setPowerDuration(DSPortAdapter.PowerDeliveryDuration.INFINITE);
        adapter.startPowerDelivery(DSPortAdapter.PowerChangeCondition.AFTER_NEXT_BYTE);

        // send the convert temperature command
        adapter.putByte(Command.COPY_SCRATCHPAD.code);

        // sleep for 10 milliseconds to allow copy to take place.
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted", ex);
        }

        // Turn power back to normal.
        adapter.setPowerNormal();

        // third, let's read the scratchpad again with the recallE2 command and compare.
        byte[] readlastbuffer;

        readlastbuffer = recallE2();

        if ((readfirstbuffer[2] != readlastbuffer[2])
                || (readfirstbuffer[3] != readlastbuffer[3])
                || (readfirstbuffer[4] != readlastbuffer[4])) {

            // copying to scratchpad failed
            throw new OneWireIOException(address, "OneWireContainer28-Error copying scratchpad to E2 memory.");
        }
    }

    /**
     * Recalls the DS18B20 temperature trigger values (<code>ALARM_HIGH</code>
     * and <code>ALARM_LOW</code>) and the configuration register to the
     * scratchpad and reads the scratchpad.
     *
     * @return byte array representing data in the device's scratchpad.
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
     *         This could be caused by a physical interruption in the 1-Wire
     *         Network due to shorts or a newly arriving 1-Wire device issuing a
     *         'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter
     */
    public byte[] recallE2() throws OneWireException {

        // select the device
        if (adapter.select(address)) {

            // send the Recall E-squared memory command
            adapter.putByte(Command.RECALL_E2MEMORY.code);

            // read scratchpad
            return readScratchpad();
        }

        // device must not have been present
        throw new OneWireIOException(address, "OneWireContainer28-Device not found on 1-Wire Network");
    }

    public void recallE2(byte[] buffer) throws OneWireException {

        // select the device
        if (!adapter.select(address)) {
            // device must not have been present
            throw new OneWireIOException(address, "OneWireContainer28-Device not found on 1-Wire Network");
        }

        // send the Recall E-squared memory command
        adapter.putByte(Command.RECALL_E2MEMORY.code);

        // read scratchpad
        readScratchpad(buffer);
    }

    /**
     * Reads the way power is supplied to the DS18B20.
     *
     * @return <code>true</code> for external power, <BR>
     *         <code>false</code> for parasite power
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
     *         This could be caused by a physical interruption in the 1-Wire
     *         Network due to shorts or a newly arriving 1-Wire device issuing a
     *         'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter
     */
    public boolean isExternalPowerSupplied() throws OneWireException {

        int     intresult = 0;
        boolean result    = false;

        // select the device
        if (adapter.select(address)) {

            // send the "Read Power Supply" memory command
            adapter.putByte(Command.READ_POWER_SUPPLY.code);

            // read results
            intresult = adapter.getByte();
        } else {

            // device must not have been present
            throw new OneWireIOException(address, "OneWireContainer28-Device not found on 1-Wire Network");
        }

        if (intresult != 0x00) {
            result = true;   // reads 0xFF for true and 0x00 for false
        }

        return result;
    }
}
