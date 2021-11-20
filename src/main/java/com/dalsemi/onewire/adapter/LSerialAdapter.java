/*---------------------------------------------------------------------------
 * Copyright (C) 2001 Dallas Semiconductor Corporation, All Rights Reserved.
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

package com.dalsemi.onewire.adapter;

// imports

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.utils.CRC8;

import java.io.IOException;

/**
 * The LSerialAdapter class implememts the DSPortAdapter interface for a legacy
 * 1-Wire serial interface adapters such as the DS9097.
 * <p>
 * Instances of valid LSerialdapter's are retrieved from methods in
 * {@link com.dalsemi.onewire.OneWireAccessProvider OneWireAccessProvider}.
 * <P>
 * The DSPortAdapter methods can be organized into the following categories:
 * </P>
 * <UL>
 * <LI> <B> Information </B>
 * <UL>
 * <LI> {@link #getAdapterName() getAdapterName}
 * <LI> {@link #getPortTypeDescription() getPortTypeDescription}
 * <LI> {@link #adapterDetected() adapterDetected}
 * <LI> {@link #getAdapterVersion() getAdapterVersion}
 * <LI> {@link #getAdapterAddress() getAdapterAddress}
 * </UL>
 * <LI> <B> Port Selection </B>
 * <UL>
 * <LI> {@link #getPortNames() getPortNames}
 * <LI> {@link #selectPort(String) selectPort}
 * <LI> {@link #getPortName() getPortName}
 * <LI> {@link #freePort() freePort}
 * </UL>
 * <LI> <B> Adapter Capabilities </B>
 * <UL>
 * <LI> {@link #canOverdrive() canOverdrive}
 * <LI> {@link #canHyperdrive() canHyperdrive}
 * <LI> {@link #canFlex() canFlex}
 * <LI> {@link #canProgram() canProgram}
 * <LI> {@link #canDeliverPower() canDeliverPower}
 * <LI> {@link #canDeliverSmartPower() canDeliverSmartPower}
 * <LI> {@link #canBreak() canBreak}
 * </UL>
 * <LI> <B> 1-Wire Network Semaphore </B>
 * <UL>
 * <LI> {@link #beginExclusive()}
 * <LI> {@link #endExclusive() endExclusive}
 * </UL>
 * <LI> <B> 1-Wire Device Discovery </B>
 * <UL>
 * <LI> Selective Search Options
 * <UL>
 * <LI> {@link #targetAllFamilies() targetAllFamilies}
 * <LI> {@link #targetFamily(int) targetFamily(int)}
 * <LI> {@link #targetFamily(byte[]) targetFamily(byte[])}
 * <LI> {@link #excludeFamily(int) excludeFamily(int)}
 * <LI> {@link #excludeFamily(byte[]) excludeFamily(byte[])}
 * <LI> {@link #setSearchOnlyAlarmingDevices() setSearchOnlyAlarmingDevices}
 * <LI> {@link #setNoResetSearch() setNoResetSearch}
 * <LI> {@link #setSearchAllDevices() setSearchAllDevices}
 * </UL>
 * <LI> Search With Automatic 1-Wire Container creation
 * <UL>
 * <LI> {@link #getAllDeviceContainers() getAllDeviceContainers}
 * <LI> {@link #getFirstDeviceContainer() getFirstDeviceContainer}
 * <LI> {@link #getNextDeviceContainer() getNextDeviceContainer}
 * </UL>
 * <LI> Search With NO 1-Wire Container creation
 * <UL>
 * <LI> {@link #findFirstDevice() findFirstDevice}
 * <LI> {@link #findNextDevice() findNextDevice}
 * <LI> {@link #getAddress(byte[]) getAddress(byte[])}
 * <LI> {@link #getAddressAsLong() getAddressAsLong}
 * <LI> {@link #getAddressAsString() getAddressAsString}
 * </UL>
 * <LI> Manual 1-Wire Container creation
 * <UL>
 * <LI> {@link #getDeviceContainer(byte[]) getDeviceContainer(byte[])}
 * <LI> {@link #getDeviceContainer(long) getDeviceContainer(long)}
 * <LI> {@link #getDeviceContainer(String) getDeviceContainer(String)}
 * <LI> {@link #getDeviceContainer() getDeviceContainer()}
 * </UL>
 * </UL>
 * <LI> <B> 1-Wire Network low level access (usually not called directly) </B>
 * <UL>
 * <LI> Device Selection and Presence Detect
 * <UL>
 * <LI> {@link #isPresent(byte[]) isPresent(byte[])}
 * <LI> {@link #isPresent(long) isPresent(long)}
 * <LI> {@link #isPresent(String) isPresent(String)}
 * <LI> {@link #isAlarming(byte[]) isAlarming(byte[])}
 * <LI> {@link #isAlarming(long) isAlarming(long)}
 * <LI> {@link #isAlarming(String) isAlarming(String)}
 * <LI> {@link #select(byte[]) select(byte[])}
 * <LI> {@link #select(long) select(long)}
 * <LI> {@link #select(String) select(String)}
 * </UL>
 * <LI> Raw 1-Wire IO
 * <UL>
 * <LI> {@link #reset() reset}
 * <LI> {@link #putBit(boolean) putBit}
 * <LI> {@link #getBit() getBit}
 * <LI> {@link #putByte(int) putByte}
 * <LI> {@link #getByte() getByte}
 * <LI> {@link #getBlock(int) getBlock(int)}
 * <LI> {@link #getBlock(byte[], int) getBlock(byte[], int)}
 * <LI> {@link #getBlock(byte[], int, int) getBlock(byte[], int, int)}
 * <LI> {@link #dataBlock(byte[], int, int) dataBlock(byte[], int, int)}
 * </UL>
 * <LI> 1-Wire Speed and Power Selection
 * <UL>
 * <LI> {@link #setPowerDuration(PowerDeliveryDuration)}
 * <LI> {@link #startPowerDelivery(PowerChangeCondition)}
 * <LI> {@link #setProgramPulseDuration(PowerDeliveryDuration)}
 * <LI> {@link #startProgramPulse(PowerChangeCondition)}
 * <LI> {@link #startBreak() startBreak}
 * <LI> {@link #setPowerNormal() setPowerNormal}
 * <LI> {@link #setSpeed(Speed)}
 * <LI> {@link #getSpeed() getSpeed}
 * </UL>
 * </UL>
 * <LI> <B> Advanced </B>
 * <UL>
 * <LI>
 * {@link #registerOneWireContainerClass(int, Class) registerOneWireContainerClass}
 * </UL>
 * </UL>
 * 8/11/2003 - shughes - modified to support RXTX instead of javax.comm
 *
 * @see com.dalsemi.onewire.OneWireAccessProvider
 * @see com.dalsemi.onewire.container.OneWireContainer
 * @version 0.00, 4 Dec 2001
 * @author DS
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 */
public class LSerialAdapter extends DSPortAdapter {

    /** Normal Search, all devices participate */
    private static final int NORMAL_SEARCH_CMD = 0xF0;

    /** Conditional Search, only 'alarming' devices participate */
    private static final int ALARM_SEARCH_CMD = 0xEC;

    /** Reference to the current SerialService */
    private SerialService serial;

    /** String name of the current opened port */
    private boolean adapterPresent;

    /** flag to indicate the last discrepancy */
    private int lastDiscrepancy;

    /** true if device found is the last device */
    private boolean lastDevice;

    /** current device */
    private final byte[] currentDevice = new byte[8];

    /**
     * Whether we are searching only alarming iButtons. This is currently
     * ignored.
     */
    boolean searchOnlyAlarmingButtons;

    /** Flag to indicate next search will not be preceeded by a 1-Wire reset */
    private boolean skipResetOnSearch = false;

    /** Flag to indicate next search will be a 'first' */
    private boolean resetSearch = true;

    /**
     * Constructs a legacy serial adapter class.
     */
    public LSerialAdapter() {
        serial = null;
        adapterPresent = false;
    }

    @Override
    public String getAdapterName() {
        return "DS9097";
    }

    @Override
    public String getPortTypeDescription() {
        return "serial communication port";
    }

    @Override
    public boolean selectPort(String newPortName) throws OneWireException {

        // find the port reference
        serial = new SerialService(newPortName);

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // attempt to open the port
            serial.openPort();
            serial.setBaudRate(115200);

            return true;
        } catch (IOException ioe) {
            throw new OneWireIOException(ioe.toString());
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public String getPortName() throws OneWireException {

        if (serial != null) {
            return serial.getPortName();
        }

        throw new OneWireException("DS9097EAdapter-getPortName, port not selected");
    }

    @Override
    public void freePort() throws OneWireException {

        try {
            // acquire exclusive use of the port
            beginLocalExclusive();

            // attempt to open the port
            serial.closePort();
        } finally {
            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public boolean adapterDetected() throws OneWireException {

        if (!adapterPresent) {
            try {
                // acquire exclusive use of the port
                beginLocalExclusive();

                adapterPresent();
            } catch (OneWireIOException e) {
                logger.error("DS9097EAdapter: Not detected ", e);
            } finally {
                // release local exclusive use of port
                endLocalExclusive();
            }
        }

        return adapterPresent;
    }

    @Override
    public String getAdapterVersion() throws OneWireException {
        return "DS9097 adapter"; // Does not look for DS9097E yet
    }

    /**
     * Retrieve the address of the adapter if it has one.
     *
     * @return <code>String</code> of the adapter address. It will return "<na>"
     * if the adapter does not have an address. The address is a string
     * representation of an 1-Wire address.
     * @throws OneWireIOException on a 1-Wire communication error such as no
     * device present. This could be caused by a physical interruption in the
     * 1-Wire Network due to shorts or a newly arriving 1-Wire device issuing a
     * 'presence pulse'.
     * @throws OneWireException on a communication or setup error with the
     * 1-Wire adapter
     * @see com.dalsemi.onewire.utils.Address
     */
    @Override
    public String getAdapterAddress() throws OneWireException {
        // there is no ID
        throw new UnsupportedOperationException("This adapter doesn't have an address");
    }

    /**
     * Returns <code>true</code> if the first iButton or 1-Wire device is
     * found on the 1-Wire Network. If no devices are found, then
     * <code>false</code> will be returned.
     *
     * @return <code>true</code> if an iButton or 1-Wire device is found.
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public boolean findFirstDevice() throws OneWireException {

        // reset the internal rom buffer
        resetSearch = true;
        return findNextDevice();
    }

    /**
     * Returns <code>true</code> if the next iButton or 1-Wire device is
     * found. The previous 1-Wire device found is used as a starting point in
     * the search. If no more devices are found then <code>false</code> will
     * be returned.
     *
     * @return <code>true</code> if an iButton or 1-Wire device is found.
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public boolean findNextDevice() throws OneWireException {

        boolean retval;

        try {
            // acquire exclusive use of the port
            beginLocalExclusive();

            while (true) {
                retval = search(resetSearch);

                if (retval) {
                    resetSearch = false;

                    // check if this is an OK family type
                    if (isValidFamily(currentDevice))
                        return true;

                    // Else, loop to the top and do another search.
                } else {
                    resetSearch = true;

                    return false;
                }
            }
        } finally {
            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public void getAddress(byte[] address) {
        System.arraycopy(currentDevice, 0, address, 0, 8);
    }

    /**
     * Copies the provided 1-Wire device address into the 'current' array. This
     * address will then be used in the getDeviceContainer() method. Permits the
     * adapter instance to create containers of devices it did not find in a
     * search.
     *
     * @param address An array to be copied into the current iButton address.
     */
    public void setAddress(byte[] address) {
        System.arraycopy(address, 0, currentDevice, 0, 8);
    }

    @Override
    public void setSearchOnlyAlarmingDevices() {
        searchOnlyAlarmingButtons = true;
    }

    @Override
    public void setNoResetSearch() {
        skipResetOnSearch = true;
    }

    @Override
    public void setSearchAllDevices() {
        searchOnlyAlarmingButtons = false;
        skipResetOnSearch = false;
    }

    @Override
    public void beginExclusive() throws OneWireException {
        serial.beginExclusive();
    }

    @Override
    public void endExclusive() {

        serial.endExclusive();
    }

    /**
     * Gets exclusive use of the 1-Wire to communicate with an iButton or 1-Wire
     * Device if it is not already done. Used to make methods thread safe.
     *
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    private void beginLocalExclusive() throws OneWireException {

        // check if there is no such port
        if (serial == null) {
            throw new OneWireException("DS9097EAdapter: port not selected ");
        }

        serial.beginExclusive();
    }

    /**
     * Relinquishes local exclusive control of the 1-Wire Network. This just
     * checks if we did our own 'beginExclusive' block and frees it.
     */
    private void endLocalExclusive() {

        serial.endExclusive();
    }

    @Override
    public void putBit(boolean bitValue) throws OneWireException {

        char send_byte;

        try {
            // acquire exclusive use of the port
            beginLocalExclusive();

            // make sure adapter is present
            if (adapterDetected()) {
                if (bitValue)
                    send_byte = (char) 0xFF;
                else
                    send_byte = (char) 0x00;

                serial.flush();

                serial.write(send_byte);
                char[] result = serial.readWithTimeout(1);

                if (result[0] != send_byte)
                    throw new OneWireIOException("Error during putBit(), echo was incorrect");
            } else {
                throw new OneWireIOException("Error communicating with adapter");
            }
        } catch (IOException ioe) {
            throw new OneWireIOException(ioe.toString());
        } finally {
            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public boolean getBit() throws OneWireException {

        try {
            // acquire exclusive use of the port
            beginLocalExclusive();

            // make sure adapter is present
            if (adapterDetected()) {
                serial.flush();

                serial.write((char) 0x00FF);
                char[] result = serial.readWithTimeout(1);

                return (result[0] == 0xFF);
            }

            throw new OneWireIOException("Error communicating with adapter");

        } catch (IOException ioe) {
            throw new OneWireIOException(null, "getBit() failed", ioe);
        } finally {
            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public void putByte(int byteValue) throws OneWireException {

        byte[] temp_block = new byte[1];

        temp_block[0] = (byte) byteValue;

        dataBlock(temp_block, 0, 1);
    }

    @Override
    public int getByte() throws OneWireException {

        byte[] temp_block = new byte[1];

        temp_block[0] = (byte) 0xFF;

        dataBlock(temp_block, 0, 1);

        if (temp_block.length == 1) {
            return (temp_block[0] & 0xFF);
        }

        throw new OneWireIOException("Error communicating with adapter");
    }

    @Override
    public byte[] getBlock(int len) throws OneWireException {

        byte[] temp_block = new byte[len];

        // set block to read 0xFF
        for (int i = 0; i < len; i++)
            temp_block[i] = (byte) 0xFF;

        getBlock(temp_block, len);

        return temp_block;
    }

    @Override
    public void getBlock(byte[] arr, int len) throws OneWireException {
        getBlock(arr, 0, len);
    }

    @Override
    public void getBlock(byte[] arr, int off, int len) throws OneWireException {

        // set block to read 0xFF
        for (int i = off; i < len; i++)
            arr[i] = (byte) 0xFF;

        dataBlock(arr, off, len);
    }

    @Override
    public void dataBlock(byte[] dataBlock, int off, int len) throws OneWireException {

        try {
            // acquire exclusive use of the port
            beginLocalExclusive();

            // make sure adapter is present
            if (adapterDetected()) {
                int t_off, t_len;
                t_off = off;
                t_len = len;

                // break up large blocks to not exceed 128 bytes at a time
                do {
                    if (t_len > 128)
                        t_len = 128;

                    char[] send_block = constructSendBlock(dataBlock, t_off, t_len);

                    serial.flush();

                    serial.write(send_block);
                    char[] raw_recv = serial.readWithTimeout(send_block.length);

                    byte[] recv = interpretRecvBlock(raw_recv);

                    System.arraycopy(recv, 0, dataBlock, t_off, t_len);

                    t_off += t_len;
                    t_len = off + len - t_off;
                } while (t_len > 0);
            } else {
                throw new OneWireIOException("Error communicating with adapter");
            }
        } catch (IOException ioe) {
            throw new OneWireIOException(ioe.toString());
        } finally {
            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public ResetResult reset() throws OneWireException {

        try {
            // acquire exclusive use of the port
            beginLocalExclusive();

            // make sure adapter is present
            if (adapterDetected()) {
                serial.flush();

                // send a break to reset 1-Wire
                serial.sendBreak(1);

                // get the result
                serial.readWithTimeout(1);

                // does not work: return ((c.length > 1) ? RESET_PRESENCE :
                // RESET_NOPRESENCE);

                return ResetResult.PRESENCE;
            }

            return ResetResult.NOPRESENCE;

        } catch (IOException ioe) {

            ioe.printStackTrace();
            return ResetResult.NOPRESENCE;

        } catch (OneWireIOException e) {

            System.err.println("DS9097EAdapter: Not detected ");
            e.printStackTrace();

            return ResetResult.NOPRESENCE;

        } finally {
            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    /**
     * Single search. Take into account reset and alarm options.
     *
     * @param resetSearch - true to start search over (like first)
     * @return true if device found of false if end of search
     */
    private boolean search(boolean resetSearch) throws OneWireException {

        int bit_test, bit_number;
        int last_zero, serial_byte_number;
        int serial_byte_mask;
        int lastcrc8;
        boolean next_result, search_direction;

        // initialize for search
        bit_number = 1;
        last_zero = 0;
        serial_byte_number = 0;
        serial_byte_mask = 1;
        next_result = false;
        lastcrc8 = 0;

        // check for a force reset of the search
        if (resetSearch) {
            lastDiscrepancy = 0;
            lastDevice = false;
            //LastFamilyDiscrepancy = 0;
        }

        // if the last call was not the last one
        if (!lastDevice) {
            // check if reset first is requested
            if (!skipResetOnSearch) {
                // reset the 1-wire
                // if there are no parts on 1-wire, return false
                if (reset() != ResetResult.PRESENCE) {
                    // reset the search
                    lastDiscrepancy = 0;
                    //LastFamilyDiscrepancy = 0;
                    return false;
                }
            }

            // If finding alarming devices issue a different command
            if (searchOnlyAlarmingButtons)
                putByte(ALARM_SEARCH_CMD); // issue the alarming search command
            else
                putByte(NORMAL_SEARCH_CMD); // issue the search command

            // loop to do the search
            do {
                // read a bit and its compliment
                bit_test = (getBit() ? 1 : 0) << 1;
                bit_test |= (getBit() ? 1 : 0);

                // check for no devices on 1-wire
                if (bit_test == 3) {
                    break;
                }

                // all devices coupled have 0 or 1
                if (bit_test > 0)
                    search_direction = !((bit_test & 0x01) == 0x01); // bit
                                                                        // write
                                                                        // value
                                                                        // for
                                                                        // search
                else {
                    // if this discrepancy if before the Last Discrepancy
                    // on a previous next then pick the same as last time
                    if (bit_number < lastDiscrepancy)
                        search_direction = ((currentDevice[serial_byte_number] & serial_byte_mask) > 0);
                    else
                        // if equal to last pick 1, if not then pick 0
                        search_direction = (bit_number == lastDiscrepancy);

                    // if 0 was picked then record its position in LastZero
                    if (!search_direction)
                        last_zero = bit_number;

                    // check for Last discrepancy in family
                    //if (last_zero < 9)
                        //LastFamilyDiscrepancy = last_zero;
                }

                // set or clear the bit in the CurrentDevice byte
                // serial_byte_number
                // with mask serial_byte_mask
                if (search_direction)
                    currentDevice[serial_byte_number] |= serial_byte_mask;
                else
                    currentDevice[serial_byte_number] &= ~serial_byte_mask;

                // serial number search direction write bit
                putBit(search_direction);

                // increment the byte counter bit_number
                // and shift the mask serial_byte_mask
                bit_number++;
                serial_byte_mask = (serial_byte_mask <<= 1) & 0x00FF;

                // if the mask is 0 then go to new CurrentDevice byte
                // serial_byte_number
                // and reset mask
                if (serial_byte_mask == 0) {
                    // accumulate the CRC
                    lastcrc8 = CRC8.compute(currentDevice[serial_byte_number], lastcrc8);
                    serial_byte_number++;
                    serial_byte_mask = 1;
                }

            } while (serial_byte_number < 8); // loop until through all
                                                // CurrentDevice bytes 0-7

            // if the search was successful then
            if (!((bit_number < 65) || (lastcrc8 != 0))) {
                // search successful so set
                // LastDiscrepancy,LastDevice,next_result
                lastDiscrepancy = last_zero;
                lastDevice = (lastDiscrepancy == 0);
                next_result = true;
            }
        }

        // if no device found then reset counters so next 'next' will be
        // like a first
        if (!next_result || (currentDevice[0] == 0)) {
            lastDiscrepancy = 0;
            lastDevice = false;
            //LastFamilyDiscrepancy = 0;
            next_result = false;
        }

        return next_result;
    }

    /**
     * Attempt to detect prense of DS9097 style adapter. Mostly just checks to
     * make sure it is NOT a DS2480 or an AT modem.
     *
     * @return true if adapter likely present
     */
    private boolean adapterPresent() {

        if (!adapterPresent) {
            char[] test_buf = { 0xFF, 0x00, 0xFF, 0x00, 0xFF, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0xE3, 0xC1, 'A', 'T', 'E', '0', 0x0D, 'A' };

            try {
                // do reset
                serial.flush();

                // send a break to reset 1-Wire
                serial.sendBreak(1);

                // get the result
                serial.readWithTimeout(1);

                // send the test message
                serial.flush();
                serial.write(test_buf);

                // get echo
                serial.readWithTimeout(test_buf.length);

                serial.flush();

                // if get entire echo then must be OK
                adapterPresent = true;
            } catch (IOException ex) {
                // DRAIN
                logger.fatal("DalSemi ignored this exception", ex);
            }
        }

        return adapterPresent;
    }

    /**
     * Translate a data block to a DS9097 style block where every byte represent
     * one bit timeslot.
     *
     * @param data byte array
     * @param off offset into data array
     * @param len length of data array to send
     * @return character array send block
     */
    private char[] constructSendBlock(byte[] data, int off, int len) {

        int shift_byte, cnt = 0;
        char[] send_block = new char[len * 8];

        for (int i = 0; i < len; i++) {
            shift_byte = data[i + off];

            for (int j = 0; j < 8; j++) {
                if ((shift_byte & 0x01) == 0x01)
                    send_block[cnt++] = 0x00FF;
                else
                    send_block[cnt++] = 0x00;

                shift_byte >>>= 1;
            }
        }

        return send_block;
    }

    /**
     * Inerpret a response communication block from a DS9097 adapter and
     * translate it into a byte array of data.
     *
     * @param rawBlock character array of raw communication
     * @return byte array of data recieved
     */
    private byte[] interpretRecvBlock(char[] rawBlock) {

        int shift_byte = 0, bit_cnt = 0, byte_cnt = 0;
        byte[] recv_block = new byte[rawBlock.length / 8];

        for (char c : rawBlock) {
            shift_byte >>>= 1;

            if (c == 0x00FF)
                shift_byte |= 0x80;

            bit_cnt++;

            if (bit_cnt == 8) {
                bit_cnt = 0;
                recv_block[byte_cnt++] = (byte) shift_byte;
                shift_byte = 0;
            }
        }

        return recv_block;
    }
}
