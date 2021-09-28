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
import com.dalsemi.onewire.utils.Bit;

/**
 * <P>
 * 1-Wire&#174 container for 1-Wire(MicroLAN) Coupler, DS2409. This container
 * encapsulates the functionality of the 1-Wire family type <B>1F</B> (hex).
 * </P>
 *
 * <H3>Features</H3>
 * <UL>
 * <li>Low impedance coupler to create large common-ground, multi-level MicroLAN
 * networks
 * <li>Keeps inactive branches pulled high to 5V
 * <li>Simplifies network topology analysis by logically decoupling devices on
 * active network segments
 * <li>Conditional search for fast event signaling
 * <li>Auxiliary 1-Wire TM line to connect a memory chip or to be used as
 * digital input
 * <li>Programmable, general purpose open drain control output
 * <li>Operating temperature range from -40&#176C to +85&#176C
 * <li>Compact, low cost 6-pin TSOC surface mount package
 * </UL>
 *
 * <P>
 * Setting the latch on the DS2409 to 'on' (see
 * {@link #setLatchState(int,boolean,boolean,byte[]) seLatchState}) connects the
 * channel [Main(0) or Auxillary(1)] to the 1-Wire data line. Note that this is
 * the opposite of the {@link com.dalsemi.onewire.container.OneWireContainer12
 * DS2406} and {@link com.dalsemi.onewire.container.OneWireContainer05 DS2405}
 * which connect thier I/O lines to ground.
 * <H3>Usage</H3>
 *
 * <DL>
 * <DD>See the usage example in
 * {@link com.dalsemi.onewire.container.SwitchContainer SwitchContainer} for
 * basic switch operations.
 * </DL>
 *
 * <H3>DataSheet</H3>
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS2409.pdf">
 * http://pdfserv.maxim-ic.com/arpdf/DS2409.pdf</A>
 * </DL>
 *
 * @see com.dalsemi.onewire.container.OneWireSensor
 * @see com.dalsemi.onewire.container.SwitchContainer
 * @see com.dalsemi.onewire.container.OneWireContainer05
 * @see com.dalsemi.onewire.container.OneWireContainer12
 *
 * @author DSS
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 */
public class OneWireContainer1F extends OneWireContainer implements SwitchContainer {

    /**
     * Commands.
     *
     * Note that this set is different from the one in {@link Command}, hence inner enum.
     */
    enum OWC1FCommand {

        READ_WRITE_STATUS(0x5A),
        ALL_LINES_OFF(0x66),
        DISCHARGE(0x99),
        DIRECT_ON_MAIN(0xA5),
        SMART_ON_MAIN(0xCC),
        SMART_ON_AUX(0x33);

        public final byte code;

        OWC1FCommand(int code) {
            this.code = (byte) code;
        }
    }

    /** Offset of BITMAP in array returned from read state. */
    protected static final int BITMAP_OFFSET = 3;

    /** Offset of Status in array returned from read state. */
    protected static final int STATUS_OFFSET = 0;

    /** Offset of Main channel flag in array returned from read state. */
    protected static final int MAIN_OFFSET = 1;

    /** Offset of Main channel flag in array returned from read state. */
    protected static final int AUX_OFFSET = 2;

    /** Channel flag to indicate turn off. */
    protected static final int SWITCH_OFF = 0;

    /** Channel flag to indicate turn on. */
    protected static final int SWITCH_ON = 1;

    /** Channel flag to indicate smart on. */
    protected static final int SWITCH_SMART = 2;

    /** Main Channel number. */
    public static final int CHANNEL_MAIN = 0;

    /** Aux Channel number. */
    public static final int CHANNEL_AUX = 1;

    /** Flag to clear the activity on a write operation */
    private boolean clearActivityOnWrite;
    /** Flag to do speed checking */
    private boolean doSpeedEnable = true;
    /** Flag to indicated devices detected on branch during smart-on */
    private boolean devicesOnBranch = false;

    /**
     * Create an empty container that is not complete until after a call to
     * <code>setupContainer</code>.
     * <p>
     *
     * This is one of the methods to construct a container. The others are
     * through creating a OneWireContainer with parameters.
     *
     * @see #setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[])
     *      super.setupContainer()
     */
    public OneWireContainer1F() {

        clearActivityOnWrite = false;
    }

    /**
     * Create a container with the provided adapter instance and the address of
     * the iButton or 1-Wire device.
     * <p>
     *
     * This is one of the methods to construct a container. The other is through
     * creating a OneWireContainer with NO parameters.
     *
     * @param sourceAdapter
     *            adapter instance used to communicate with this iButton
     * @param newAddress
     *            {@link com.dalsemi.onewire.utils.Address Address} of this
     *            1-Wire device
     *
     * @see #OneWireContainer1F() OneWireContainer1F
     * @see com.dalsemi.onewire.utils.Address utils.Address
     */
    public OneWireContainer1F(DSPortAdapter sourceAdapter, byte[] newAddress) {

        super(sourceAdapter, newAddress);

        clearActivityOnWrite = false;
    }

    /**
     * Create a container with the provided adapter instance and the address of
     * the iButton or 1-Wire device.
     * <p>
     *
     * This is one of the methods to construct a container. The other is through
     * creating a OneWireContainer with NO parameters.
     *
     * @param sourceAdapter
     *            adapter instance used to communicate with this 1-Wire device
     * @param newAddress
     *            {@link com.dalsemi.onewire.utils.Address Address} of this
     *            1-Wire device
     *
     * @see #OneWireContainer1F() OneWireContainer1F
     * @see com.dalsemi.onewire.utils.Address utils.Address
     */
    public OneWireContainer1F(DSPortAdapter sourceAdapter, long newAddress) {
        super(sourceAdapter, newAddress);

        clearActivityOnWrite = false;
    }

    /**
     * Create a container with the provided adapter instance and the address of
     * the iButton or 1-Wire device.
     * <p>
     *
     * This is one of the methods to construct a container. The other is through
     * creating a OneWireContainer with NO parameters.
     *
     * @param sourceAdapter
     *            adapter instance used to communicate with this 1-Wire device
     * @param newAddress
     *            {@link com.dalsemi.onewire.utils.Address Address} of this
     *            1-Wire device
     *
     * @see #OneWireContainer1F() OneWireContainer1F
     * @see com.dalsemi.onewire.utils.Address utils.Address
     */
    public OneWireContainer1F(DSPortAdapter sourceAdapter, String newAddress) {
        super(sourceAdapter, newAddress);

        clearActivityOnWrite = false;
    }

    @Override
    public String getName() {
        return "DS2409";
    }

    @Override
    public String getAlternateNames() {
        return "Coupler";
    }

    @Override
    public String getDescription() {
        return "1-Wire Network Coupler with dual addressable "
                + "switches and a general purpose open drain control "
                + "output.  Provides a common ground for all connected"
                + "multi-level MicroLan networks.  Keeps inactive branches"
                + "Pulled to 5V.";
    }

    /**
     * Directs the container to avoid the calls to doSpeed() in methods that
     * communicate with the Thermocron. To ensure that all parts can talk to the
     * 1-Wire bus at their desired speed, each method contains a call to
     * <code>doSpeed()</code>. However, this is an expensive operation. If a
     * user manages the bus speed in an application, call this method with
     * <code>doSpeedCheck</code> as <code>false</code>. The default behavior is
     * to call <code>doSpeed()</code>.
     *
     * @param doSpeedCheck
     *            <code>true</code> for <code>doSpeed()</code> to be called
     *            before every 1-Wire bus access, <code>false</code> to skip
     *            this expensive call
     *
     * @see OneWireContainer#doSpeed()
     */
    public synchronized void setSpeedCheck(boolean doSpeedCheck) {
        doSpeedEnable = doSpeedCheck;
    }

    /**
     * Retrieves the 1-Wire device sensor state. This state is returned as a
     * byte array. Pass this byte array to the 'get' and 'set' methods. If the
     * device state needs to be changed then call the 'writeDevice' to finalize
     * the changes.
     *
     * @return 1-Wire device sensor state
     *
     * @throws OneWireIOException
     *             on a 1-Wire communication error such as reading an incorrect
     *             CRC from a 1-Wire device. This could be caused by a physical
     *             interruption in the 1-Wire Network due to shorts or a newly
     *             arriving 1-Wire device issuing a 'presence pulse'.
     * @throws OneWireException
     *             on a communication or setup error with the 1-Wire adapter
     *
     */
    @Override
    public byte[] readDevice() throws OneWireException {

        var outputBuffer = new byte[4];

        readDevice(outputBuffer);

        return outputBuffer;
    }

    @Override
    public void readDevice(byte[] outputBuffer) throws OneWireException {

        if (doSpeedEnable) {
            doSpeed();
        }

        // read the status byte
        byte[] tmp_buf = deviceOperation(OWC1FCommand.READ_WRITE_STATUS.code,
                (byte) 0x00FF, 2);

        // extract the status byte
        outputBuffer[0] = tmp_buf[2];
    }

    /**
     * Writes the 1-Wire device sensor state that have been changed by 'set'
     * methods. Only the state registers that changed are updated. This is done
     * by referencing a field information appended to the state data.
     *
     * @param state
     *            1-Wire device sensor state
     *
     * @throws OneWireIOException
     *             on a 1-Wire communication error such as reading an incorrect
     *             CRC from a 1-Wire device. This could be caused by a physical
     *             interruption in the 1-Wire Network due to shorts or a newly
     *             arriving 1-Wire device issuing a 'presence pulse'.
     * @throws OneWireException
     *             on a communication or setup error with the 1-Wire adapter
     */
    @Override
    public void writeDevice(byte[] state) throws OneWireException {

        var extra = 0;
        byte command;
        byte first_byte;
        byte[] tmp_buf = null;

        if (doSpeedEnable) {
            doSpeed();
        }

        // check for both switches set to on
        if ((Bit.arrayReadBit(MAIN_OFFSET, BITMAP_OFFSET, state) == 1)
                && (Bit.arrayReadBit(AUX_OFFSET, BITMAP_OFFSET, state) == 1)) {
            if ((state[MAIN_OFFSET] != SWITCH_OFF)
                    && (state[AUX_OFFSET] != SWITCH_OFF)) {
                throw new OneWireException(address,
                        "Attempting to set both channels on, only single channel on at a time");
            }
        }

        // check if need to set control
        if (Bit.arrayReadBit(STATUS_OFFSET, BITMAP_OFFSET, state) == 1) {

            // create a command based on bit 6/7 of status
            first_byte = 0;

            // mode bit
            if (Bit.arrayReadBit(7, STATUS_OFFSET, state) == 1)
                first_byte |= (byte) 0x20;

            // Control output
            if (Bit.arrayReadBit(6, STATUS_OFFSET, state) == 1)
                first_byte |= (byte) 0xC0;

            tmp_buf = deviceOperation(OWC1FCommand.READ_WRITE_STATUS.code, first_byte, 2);
            state[0] = tmp_buf[2];
        }

        // check for AUX state change
        command = 0;

        if (Bit.arrayReadBit(AUX_OFFSET, BITMAP_OFFSET, state) == 1) {
            if ((state[AUX_OFFSET] == SWITCH_ON)
                    || (state[AUX_OFFSET] == SWITCH_SMART)) {
                command = OWC1FCommand.SMART_ON_AUX.code;
                extra = 2;
            } else {
                command = OWC1FCommand.ALL_LINES_OFF.code;
                extra = 0;
            }
        }

        // check for MAIN state change
        if (Bit.arrayReadBit(MAIN_OFFSET, BITMAP_OFFSET, state) == 1) {
            if (state[MAIN_OFFSET] == SWITCH_ON) {
                command = OWC1FCommand.DIRECT_ON_MAIN.code;
                extra = 0;
            } else if (state[MAIN_OFFSET] == SWITCH_SMART) {
                command = OWC1FCommand.SMART_ON_MAIN.code;
                extra = 2;
            } else {
                command = OWC1FCommand.ALL_LINES_OFF.code;
                extra = 0;
            }
        }

        // check if there are events to clear and not about to do clear anyway
        if ((clearActivityOnWrite) && (command != OWC1FCommand.ALL_LINES_OFF.code)) {
            if ((Bit.arrayReadBit(4, STATUS_OFFSET, state) == 1)
                    || (Bit.arrayReadBit(5, STATUS_OFFSET, state) == 1)) {

                // clear the events
                deviceOperation(OWC1FCommand.ALL_LINES_OFF.code, (byte) 0xFF, 0);

                // set the channels back to the correct state
                if (command == 0) {
                    if (Bit.arrayReadBit(0, STATUS_OFFSET, state) == 0) {
                        command = OWC1FCommand.SMART_ON_MAIN.code;
                    } else if (Bit.arrayReadBit(2, STATUS_OFFSET, state) == 0) {
                        command = OWC1FCommand.SMART_ON_AUX.code;
                    }

                    extra = 2;
                }
            }
        }

        // check if there is a command to send
        if (command != 0) {
            tmp_buf = deviceOperation(command, (byte) 0xFF, extra);
        }

        // if doing a SMART_ON, then look at result data for presence
        if ((command == OWC1FCommand.SMART_ON_MAIN.code)
                || (command == OWC1FCommand.SMART_ON_AUX.code)) {
            // devices on branch indicated if 3rd byte is 0
            devicesOnBranch = (tmp_buf[2] == 0);
        } else {
            devicesOnBranch = false;
        }

        // clear clear activity on write
        clearActivityOnWrite = false;

        // clear the bitmap
        state[BITMAP_OFFSET] = 0;
    }

    /**
     * <P>
     * Force a power-on reset for parasitically powered 1-Wire devices connected
     * to the main or auziliary output of the DS2409.
     * </P>
     *
     * <P>
     * IMPORTANT: the duration of the discharge time should be 100ms minimum.
     * </P>
     * <BR>
     *
     * @param time
     *            number of milliseconds the lines are to be discharged for
     *            (minimum 100)
     *
     * @throws OneWireIOException
     *             on a 1-Wire communication error such as reading an incorrect
     *             CRC from a 1-Wire device. This could be caused by a physical
     *             interruption in the 1-Wire Network due to shorts or a newly
     *             arriving 1-Wire device issuing a 'presence pulse'.
     * @throws OneWireException
     *             on a communication or setup error with the 1-Wire adapter
     */
    public void dischargeLines(int time) throws OneWireException {

        // Error checking
        if (time < 100) {
            time = 100;
        }

        if (doSpeedEnable) {
            doSpeed();
        }

        // discharge the lines
        deviceOperation(OWC1FCommand.DISCHARGE.code, (byte) 0xFF, 0);

        // wait for desired time and return.
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            // DRAIN
            logger.debug("Interrupted", ex);
        }

        // clear the discharge
        deviceOperation(OWC1FCommand.READ_WRITE_STATUS.code, (byte) 0x00FF, 2);
    }

    @Override
    public boolean isHighSideSwitch() {
        return true;
    }

    @Override
    public boolean hasActivitySensing() {
        return true;
    }

    @Override
    public boolean hasLevelSensing() {
        return true;
    }

    @Override
    public boolean hasSmartOn() {
        return true;
    }

    @Override
    public boolean onlySingleChannelOn() {
        return true;
    }

    @Override
    public int getNumberChannels(byte[] state) {
        return 2;
    }

    @Override
    public boolean getLevel(int channel, byte[] state) throws OneWireException {
        return (Bit.arrayReadBit(1 + channel * 2, STATUS_OFFSET, state) == 1);
    }

    @Override
    public boolean getLatchState(int channel, byte[] state) {
        return (Bit.arrayReadBit(channel * 2, STATUS_OFFSET, state) == 0);
    }

    @Override
    public boolean getSensedActivity(int channel, byte[] state)
            throws OneWireException {
        return (Bit.arrayReadBit(4 + channel, STATUS_OFFSET, state) == 1);
    }

    /**
     * Checks if the control I/O pin mode is automatic (see DS2409 data sheet).
     *
     * @param state
     *            current state of the device returned from
     *            <code>readDevice()</code>
     *
     * @return <code>true</code> if control mode is automatic
     */
    public boolean isModeAuto(byte[] state) {
        return (Bit.arrayReadBit(7, STATUS_OFFSET, state) == 0);
    }

    /**
     * Checks the channel association of the control pin. This value only makes
     * sense if the control mode is automatic (see <CODE>isModeAuto</CODE>).
     *
     * @param state
     *            current state of the device returned from
     *            <code>readDevice()</code>
     *
     * @return <code>int</code> the channel number that is associated with the
     *         control pin
     */
    public int getControlChannelAssociation(byte[] state) {
        return Bit.arrayReadBit(6, STATUS_OFFSET, state);
    }

    /**
     * Checks the control data value. This value only makes sense if the control
     * mode is manual (see <CODE>isModeAuto</CODE>). 0 = output transistor off,
     * 1 = output transistor on
     *
     * @param state
     *            current state of the device returned from
     *            <code>readDevice()</code>
     *
     * @return <code>int</code> the control output transistor state
     */
    public int getControlData(byte[] state) {
        return Bit.arrayReadBit(6, STATUS_OFFSET, state);
    }

    /**
     * Gets flag that indicates if a device was present when doing the last
     * smart on. Note that this flag is only valid if the DS2409 flag was
     * cleared with an ALL_LINES_OFF command and the last writeDevice performed
     * a 'smart-on' on one of the channels.
     *
     * @return <code>true</code> if device detected on branch
     */
    public boolean getLastSmartOnDeviceDetect() {
        return devicesOnBranch;
    }

    @Override
    public void setLatchState(int channel, boolean latchState, boolean doSmart,
            byte[] state) {

        // set the state flag
        if (latchState) {
            state[channel + 1] = (byte) ((doSmart) ? SWITCH_SMART : SWITCH_ON);
        } else {
            state[channel + 1] = (byte) SWITCH_OFF;
        }

        // indicate in bitmap the the state has changed
        Bit.arrayWriteBit(1, channel + 1, BITMAP_OFFSET, state);
    }

    @Override
    public void clearActivity() throws OneWireException {
        clearActivityOnWrite = true;
    }

    /**
     * Sets the control pin mode. The method <code>writeDevice(byte[])</code>
     * must be called to finalize changes to the device. Note that multiple
     * 'set' methods can be called before one call to
     * <code>writeDevice(byte[])</code>.
     *
     * @param makeAuto
     *            <CODE>true</CODE> to set to auto mode, false for manual mode
     * @param state
     *            current state of the device returned from
     *            <code>readDevice()</code>
     */
    public void setModeAuto(boolean makeAuto, byte[] state) {
        // set the bit
        Bit.arrayWriteBit((makeAuto ? 0 : 1), 7, STATUS_OFFSET, state);

        // indicate in bitmap the the state has changed
        Bit.arrayWriteBit(1, STATUS_OFFSET, BITMAP_OFFSET, state);
    }

    /**
     * Sets the control pin channel association. This only makes sense if the
     * contol pin is in automatic mode. The method
     * <code>writeDevice(byte[])</code> must be called to finalize changes to
     * the device. Note that multiple 'set' methods can be called before one
     * call to <code>writeDevice(byte[])</code>.
     *
     * @param channel
     *            channel to associate with control pin
     * @param state
     *            current state of the device returned from
     *            <code>readDevice()</code>
     *
     * @throws OneWireException
     *             when trying to set channel association in manual mode
     */
    public void setControlChannelAssociation(int channel, byte[] state)
            throws OneWireException {

        // check for invalid mode
        if (!isModeAuto(state)) {
            throw new OneWireException(address,
                    "Trying to set channel association in manual mode");
        }

        // set the bit
        Bit.arrayWriteBit(channel, 6, STATUS_OFFSET, state);

        // indicate in bitmap the the state has changed
        Bit.arrayWriteBit(1, STATUS_OFFSET, BITMAP_OFFSET, state);
    }

    /**
     * Sets the control pin data to a value. Note this method only works if the
     * control pin is in manual mode. The method
     * <code>writeDevice(byte[])</code> must be called to finalize changes to
     * the device. Note that multiple 'set' methods can be called before one
     * call to <code>writeDevice(byte[])</code>.
     *
     * @param data
     *            <CODE>true</CODE> for on and <CODE>false</CODE> for off
     * @param state
     *            current state of the device returned from
     *            <code>readDevice()</code>
     *
     * @throws OneWireException
     *             when trying to set control data in automatic mode
     */
    public void setControlData(boolean data, byte[] state)
            throws OneWireException {

        // check for invalid mode
        if (isModeAuto(state)) {
            throw new OneWireException(address,
                    "Trying to set control data when control is in automatic mode");
        }

        // set the bit
        Bit.arrayWriteBit((data ? 1 : 0), 6, STATUS_OFFSET, state);

        // indicate in bitmap the the state has changed
        Bit.arrayWriteBit(1, STATUS_OFFSET, BITMAP_OFFSET, state);
    }

    /**
     * Do a DS2409 specidific operation.
     *
     * @param command
     *            code to send
     * @param sendByte
     *            data byte to send
     * @param extra
     *            number of extra bytes to send
     *
     * @return block of the complete resulting transaction
     *
     * @throws OneWireIOException
     *             on a 1-Wire communication error such as reading an incorrect
     *             CRC from a 1-Wire device. This could be caused by a physical
     *             interruption in the 1-Wire Network due to shorts or a newly
     *             arriving 1-Wire device issuing a 'presence pulse'.
     * @throws OneWireException
     *             on a communication or setup error with the 1-Wire adapter
     */
    private byte[] deviceOperation(byte command, byte sendByte, int extra)
            throws OneWireException {

        OneWireIOException exc = null;

        for (int attemptCounter = 2; attemptCounter > 0; attemptCounter--) {
            // Variables.
            byte[] raw_buf = new byte[extra + 2];

            // build block.
            raw_buf[0] = command;
            raw_buf[1] = sendByte;

            for (int i = 2; i < raw_buf.length; i++) {
                raw_buf[i] = (byte) 0xFF;
            }

            // Select the device.
            if (adapter.select(address)) {

                // send the block
                adapter.dataBlock(raw_buf, 0, raw_buf.length);

                // verify
                if (command == OWC1FCommand.READ_WRITE_STATUS.code) {

                    if (raw_buf[raw_buf.length - 1] != raw_buf[raw_buf.length - 2]) {

                        if (exc == null) {
                            exc = new OneWireIOException(address,
                                    "OneWireContainer1F verify on command incorrect");
                        }

                        continue;
                    }
                } else {
                    if (raw_buf[raw_buf.length - 1] != command) {
                        if (exc == null) {
                            exc = new OneWireIOException(address, "OneWireContainer1F verify on command incorrect");
                        }
                        continue;
                    }
                }

                return raw_buf;

            } else {

                throw new OneWireIOException(address, "OneWireContainer1F failure - Device not found");
            }
        }
        // get here after a few attempts
        throw exc;
    }
}
