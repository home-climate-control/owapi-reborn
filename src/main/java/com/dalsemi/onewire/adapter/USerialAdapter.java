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

package com.dalsemi.onewire.adapter;

import com.dalsemi.onewire.Family;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.utils.Bit;
import com.dalsemi.onewire.utils.CRC8;
import org.apache.logging.log4j.ThreadContext;

import java.io.IOException;

import static com.dalsemi.onewire.adapter.DSPortAdapter.Level.BREAK;
import static com.dalsemi.onewire.adapter.DSPortAdapter.Level.NORMAL;
import static com.dalsemi.onewire.adapter.DSPortAdapter.Level.POWER_DELIVERY;
import static com.dalsemi.onewire.adapter.DSPortAdapter.PowerChangeCondition.AFTER_NEXT_BIT;
import static com.dalsemi.onewire.adapter.DSPortAdapter.PowerChangeCondition.AFTER_NEXT_BYTE;
import static com.dalsemi.onewire.adapter.DSPortAdapter.PowerChangeCondition.NOW;
import static com.dalsemi.onewire.adapter.DSPortAdapter.PowerDeliveryDuration.EPROM;
import static com.dalsemi.onewire.adapter.DSPortAdapter.PowerDeliveryDuration.INFINITE;
import static com.dalsemi.onewire.adapter.DSPortAdapter.Speed.FLEX;
import static com.dalsemi.onewire.adapter.DSPortAdapter.Speed.OVERDRIVE;
import static com.dalsemi.onewire.adapter.DSPortAdapter.Speed.REGULAR;
import static com.dalsemi.onewire.adapter.UAdapterState.BAUD_9600;
import static com.dalsemi.onewire.adapter.UAdapterState.USPEED_FLEX;
import static com.dalsemi.onewire.adapter.UAdapterState.USPEED_OVERDRIVE;
import static com.dalsemi.onewire.adapter.UPacketBuilder.FUNCTION_12VPULSE_NOW;
import static com.dalsemi.onewire.adapter.UPacketBuilder.FUNCTION_5VPULSE_NOW;
import static com.dalsemi.onewire.adapter.UPacketBuilder.FUNCTION_RESET;
import static com.dalsemi.onewire.adapter.UPacketBuilder.FUNCTION_STOP_PULSE;
import static com.dalsemi.onewire.adapter.UPacketBuilder.OPERATION_BYTE;
import static com.dalsemi.onewire.adapter.UPacketBuilder.OPERATION_SEARCH;
import static com.dalsemi.onewire.adapter.UParameterSettings.PARAMETER_12VPULSE;
import static com.dalsemi.onewire.adapter.UParameterSettings.PARAMETER_5VPULSE;
import static com.dalsemi.onewire.adapter.UParameterSettings.PARAMETER_BAUDRATE;
import static com.dalsemi.onewire.adapter.UParameterSettings.PARAMETER_SAMPLEOFFSET;
import static com.dalsemi.onewire.adapter.UParameterSettings.PARAMETER_SLEW;
import static com.dalsemi.onewire.adapter.UParameterSettings.PARAMETER_WRITE1LOW;
import static com.dalsemi.onewire.adapter.UParameterSettings.TIME12V_512us;
import static com.dalsemi.onewire.adapter.UParameterSettings.TIME5V_infinite;

/**
 * The USerialAdapter class implements the DSPortAdapter interface for a DS2480
 * based serial adapter such as the DS9097U-009 or DS9097U-S09.
 * <p>
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
 *
 * @see OneWireContainer
 * @author DS
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 */
public class USerialAdapter extends DSPortAdapter {

    /** Family code for the EPROM iButton DS1982 */
    private static final Family ADAPTER_ID_FAMILY = Family.F09;

    /** Extended read page command for DS1982 */
    private static final int EXTENDED_READ_PAGE = 0xC3;

    /** Normal Search, all devices participate */
    private static final char NORMAL_SEARCH_CMD = 0xF0;

    /** Conditional Search, only 'alarming' devices participate */
    private static final char ALARM_SEARCH_CMD = 0xEC;

    /** Max baud rate supported by DS9097U */
    private static final int maxBaud = 115200;

    /** Reference to the current SerialService */
    private SerialService serial;

    /** String name of the current opened port */
    private boolean adapterPresent;

    /** U Adapter packet builder */
    UPacketBuilder uBuild;

    /** State of the OneWire */
    private OneWireState owState;

    /** U Adapter state */
    private UAdapterState uState;

    /** Input buffer to hold received data */
    private StringBuffer inBuffer;

    /**
     * Constructs a DS9097U serial adapter class.
     */
    public USerialAdapter() {

        serial = null;
        owState = new OneWireState();
        uState = new UAdapterState(owState);
        uBuild = new UPacketBuilder(uState);
        inBuffer = new StringBuffer();
        adapterPresent = false;
    }

    @Override
    public String getAdapterName() {
        return "DS9097U";
    }

    @Override
    public String getPortTypeDescription() {
        return "Serial communication port";
    }

    @Override
    public boolean selectPort(String newPortName) throws OneWireException {

        serial = new SerialService(newPortName);

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // attempt to open the port
            serial.openPort();
            return true;

        } catch (IOException ex) {
            throw new OneWireIOException("Oops", ex);
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    /**
     * Retrieve the name of the selected port as a {@code String}.
     *
     * @return {@code String} representation of selected port name.
     * @throws OneWireException if valid port not yet selected.
     */
    @Override
    public String getPortName() throws OneWireException {

        if (serial == null) {
            throw new OneWireException("USerialAdapter-getPortName, port not selected");
        }

        return serial.getPortName();
    }

    @Override
    public void freePort() throws OneWireException {

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // attempt to close the port
            serial.closePort();
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public boolean adapterDetected() {

        ThreadContext.push("adapterDetected");
        try {

            // acquire exclusive use of the port
            beginLocalExclusive();
            uAdapterPresent();

            return uVerify();

        } catch (OneWireException ex) {
            logger.error("Error trying to detect the adapter", ex);
            return false;
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
            ThreadContext.pop();
        }
    }

    @Override
    public String getAdapterVersion() throws OneWireException {

        String versionString = "DS2480 based adapter";

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // only check if the port is acquired
            if (uAdapterPresent()) {

                // perform a reset to read the version
                if (uState.revision == 0) {
                    reset();
                }

                versionString = versionString.concat(", version " + (uState.revision >> 2));

                return versionString;
            } else {
                throw new OneWireIOException("USerialAdapter-getAdapterVersion, adapter not present");
            }
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public String getAdapterAddress() {

        // get a reference to the current oneWire State
        OneWireState preservedMstate = owState;

        owState = new OneWireState();

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // only check if the port is aquired
            if (uAdapterPresent()) {

                // set the search to find all of the available DS1982's
                this.setSearchAllDevices();
                this.targetAllFamilies();
                this.targetFamily(ADAPTER_ID_FAMILY.code);

                // 8 bytes
                byte[] address = {
                        (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00
                        };

                // loop through each of the DS1982's to find an adapter ID
                for (var device : getAllDeviceContainers()) {
                    System.arraycopy(device.getAddress(), 0, address, 0, 8);

                    // select this device
                    if (select(address)) {

                        // create a buffer to read the first page
                        // 37 bytes
                        byte[] readBuffer = {
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00
                        };
                        int cnt = 0;
                        int i;

                        // extended read memory command
                        readBuffer[cnt++] = (byte) EXTENDED_READ_PAGE;

                        // address of first page
                        readBuffer[cnt++] = 0;
                        readBuffer[cnt++] = 0;

                        // CRC, data of page and CRC from device
                        for (i = 0; i < 34; i++)
                            readBuffer[cnt++] = (byte) 0xFF;

                        // perform CRC8 of the first chunk of known data
                        int crc8 = CRC8.compute(readBuffer, 0, 3, 0);

                        // send/receive data to 1-Wire
                        dataBlock(readBuffer, 0, cnt);

                        // check the first CRC
                        if (CRC8.compute(readBuffer, 3, 1, crc8) == 0) {

                            // compute the next CRC8 with data from device
                            if (CRC8.compute(readBuffer, 4, 33, 0) == 0) {

                                // now loop to see if all data is 0xFF
                                for (i = 4; i < 36; i++)
                                    if (readBuffer[i] != (byte) 0xFF)
                                        continue;

                                // must be the one!
                                if (i == 36)
                                    return device.getAddressAsString();
                            }
                        }
                    }
                }
            } else {
                throw new OneWireIOException("USerialAdapter-getAdapterAddress, adapter not present");
            }
        } catch (OneWireException ex) {

            // Drain.
            logger.fatal("DalSemi ignored this exception", ex);

        } finally {

            // restore the old state
            owState = preservedMstate;

            // release local exclusive use of port
            endLocalExclusive();
        }

        // don't know the ID
        return "<not available>";
    }

    @Override
    public boolean canOverdrive() throws OneWireException {
        return true;
    }

    @Override
    public boolean canHyperdrive() throws OneWireException {
        return false;
    }

    @Override
    public boolean canFlex() throws OneWireException {
        return true;
    }

    @Override
    public boolean canProgram() throws OneWireException {

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // only check if the port is aquired
            if (uAdapterPresent()) {

                // perform a reset to read the program available flag
                if (uState.revision == 0)
                    reset();

                // return the flag
                return uState.programVoltageAvailable;
            } else
                throw new OneWireIOException("USerialAdapter-canProgram, adapter not present");
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public boolean canDeliverPower() throws OneWireException {
        return true;
    }

    @Override
    public boolean canDeliverSmartPower() throws OneWireException {
        // regardless of adapter, the class does not support it
        return false;
    }

    @Override
    public boolean canBreak() throws OneWireException {
        return true;
    }

    @Override
    public boolean findFirstDevice() throws OneWireException {

        // reset the current search
        owState.searchLastDiscrepancy = 0;
        owState.searchFamilyLastDiscrepancy = 0;
        owState.searchLastDevice = false;

        // search for the first device using next
        return findNextDevice();
    }

    @Override
    public boolean findNextDevice() throws OneWireException {

        boolean searchResult;

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // check for previous last device
            if (owState.searchLastDevice) {
                owState.searchLastDiscrepancy = 0;
                owState.searchFamilyLastDiscrepancy = 0;
                owState.searchLastDevice = false;

                return false;
            }

            // check for 'first' and only 1 target
            if (owState.searchLastDiscrepancy == 0
                    && !owState.searchLastDevice
                    && owState.searchIncludeFamilies.length == 1) {

                // set the search to find the 1 target first
                owState.searchLastDiscrepancy = 64;

                // create an id to set
                // 8 bytes
                byte[] new_id = {
                        (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00,
                        };

                // set the family code
                new_id[0] = owState.searchIncludeFamilies[0];

                // set this new ID
                System.arraycopy(new_id, 0, owState.ID, 0, 8);
            }

            // loop until the correct type is found or no more devices
            do {

                // perform a search and keep the result
                searchResult = search(owState);

                if (searchResult) {

                    // check if not in exclude list
                    boolean is_excluded = false;

                    for (int i = 0; i < owState.searchExcludeFamilies.length; i++) {
                        if (owState.ID[0] == owState.searchExcludeFamilies[i]) {
                            is_excluded = true;

                            break;
                        }
                    }

                    // if not in exclude list then check for include list
                    if (!is_excluded) {

                        // loop through the include list
                        boolean is_included = false;

                        for (int i = 0; i < owState.searchIncludeFamilies.length; i++) {
                            if (owState.ID[0] == owState.searchIncludeFamilies[i]) {
                                is_included = true;

                                break;
                            }
                        }

                        // check if include list or there is no include list
                        if (is_included || (owState.searchIncludeFamilies.length == 0))
                            return true;
                    }
                }

                // skip the current type if not last device
                if (!owState.searchLastDevice && (owState.searchFamilyLastDiscrepancy != 0)) {
                    owState.searchLastDiscrepancy = owState.searchFamilyLastDiscrepancy;
                    owState.searchFamilyLastDiscrepancy = 0;
                    owState.searchLastDevice = false;
                }

                // end of search so reset and return
                else {
                    owState.searchLastDiscrepancy = 0;
                    owState.searchFamilyLastDiscrepancy = 0;
                    owState.searchLastDevice = false;
                    searchResult = false;
                }
            } while (searchResult);

            // device not found
            return false;
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public void getAddress(byte[] address) {
        System.arraycopy(owState.ID, 0, address, 0, 8);
    }

    public void setAddress(byte[] address) {
        System.arraycopy(address, 0, owState.ID, 0, 8);
    }

    @Override
    public boolean isPresent(byte[] address) throws OneWireException {

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // make sure adapter is present
            if (uAdapterPresent()) {

                // check for pending power conditions
                if (owState.oneWireLevel != NORMAL)
                    setPowerNormal();

                // if in overdrive, then use the block method in super
                if (owState.oneWireSpeed == OVERDRIVE)
                    return blockIsPresent(address, false);

                // create a private OneWireState
                OneWireState onewireState = new OneWireState();

                // set the ID to the ID of the iButton passes to this method
                System.arraycopy(address, 0, onewireState.ID, 0, 8);

                // set the state to find the specified device
                onewireState.searchLastDiscrepancy = 64;
                onewireState.searchFamilyLastDiscrepancy = 0;
                onewireState.searchLastDevice = false;
                onewireState.searchOnlyAlarmingButtons = false;

                // perform a search
                if (search(onewireState)) {

                    // compare the found device with the desired device
                    for (int i = 0; i < 8; i++)
                        if (address[i] != onewireState.ID[i])
                            return false;

                    // must be the correct device
                    return true;
                }

                // failed to find device
                return false;
            } else
                throw new OneWireIOException("Error communicating with adapter");
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public boolean isAlarming(byte[] address) throws OneWireException {

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // make sure adapter is present
            if (uAdapterPresent()) {

                // check for pending power conditions
                if (owState.oneWireLevel != NORMAL)
                    setPowerNormal();

                // if in overdrive, then use the block method in super
                if (owState.oneWireSpeed == OVERDRIVE)
                    return blockIsPresent(address, true);

                // create a private OneWireState
                OneWireState onewireState = new OneWireState();

                // set the ID to the ID of the iButton passes to this method
                System.arraycopy(address, 0, onewireState.ID, 0, 8);

                // set the state to find the specified device (alarming)
                onewireState.searchLastDiscrepancy = 64;
                onewireState.searchFamilyLastDiscrepancy = 0;
                onewireState.searchLastDevice = false;
                onewireState.searchOnlyAlarmingButtons = true;

                // perform a search
                if (search(onewireState)) {

                    // compare the found device with the desired device
                    for (int i = 0; i < 8; i++)
                        if (address[i] != onewireState.ID[i])
                            return false;

                    // must be the correct device
                    return true;
                }

                // failed to find any alarming device
                return false;
            } else
                throw new OneWireIOException("Error communicating with adapter");
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public void setSearchOnlyAlarmingDevices() {

        owState.searchOnlyAlarmingButtons = true;
    }

    @Override
    public void setNoResetSearch() {

        owState.skipResetOnSearch = true;
    }

    @Override
    public void setSearchAllDevices() {

        owState.searchOnlyAlarmingButtons = false;
        owState.skipResetOnSearch = false;
    }

    @Override
    public synchronized void targetAllFamilies() {

        // clear the include and exclude family search lists
        owState.searchIncludeFamilies = new byte[0];
        owState.searchExcludeFamilies = new byte[0];
    }

    @Override
    public synchronized void targetFamily(int familyID) {

        // replace include family array with 1 element array
        owState.searchIncludeFamilies = new byte[1];
        owState.searchIncludeFamilies[0] = (byte) familyID;
    }

    @Override
    public synchronized void targetFamily(byte familyID[]) {

        // replace include family array with new array
        owState.searchIncludeFamilies = new byte[familyID.length];

        System.arraycopy(familyID, 0, owState.searchIncludeFamilies, 0, familyID.length);
    }

    @Override
    public synchronized void excludeFamily(int familyID) {

        // replace exclude family array with 1 element array
        owState.searchExcludeFamilies = new byte[1];
        owState.searchExcludeFamilies[0] = (byte) familyID;
    }

    @Override
    public synchronized void excludeFamily(byte familyID[]) {

        // replace exclude family array with new array
        owState.searchExcludeFamilies = new byte[familyID.length];

        System.arraycopy(familyID, 0, owState.searchExcludeFamilies, 0, familyID.length);
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
            throw new OneWireException("USerialAdapter: port not selected ");
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

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // make sure adapter is present
            if (uAdapterPresent()) {

                // check for pending power conditions
                if (owState.oneWireLevel != NORMAL)
                    setPowerNormal();

                // flush out the com buffer
                serial.flush();

                // build a message to send bit to the U brick
                uBuild.restart();

                int bit_offset = uBuild.dataBit(bitValue, owState.levelChangeOnNextBit);

                // check if just started power delivery
                if (owState.levelChangeOnNextBit) {

                    // clear the primed condition
                    owState.levelChangeOnNextBit = false;

                    // set new level state
                    owState.oneWireLevel = POWER_DELIVERY;
                }

                // send and receive
                char[] result_array = uTransaction(uBuild);

                // check for echo
                if (bitValue != uBuild.interpretOneWireBit(result_array[bit_offset]))
                    throw new OneWireIOException("1-Wire communication error, echo was incorrect");
            } else
                throw new OneWireIOException("Error communicating with adapter");
        } catch (IOException ex) {
            throw new OneWireIOException("Oops", ex);
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
            if (uAdapterPresent()) {

                // check for pending power conditions
                if (owState.oneWireLevel != NORMAL)
                    setPowerNormal();

                // flush out the com buffer
                serial.flush();

                // build a message to send bit to the U brick
                uBuild.restart();

                int bit_offset = uBuild.dataBit(true, owState.levelChangeOnNextBit);

                // check if just started power delivery
                if (owState.levelChangeOnNextBit) {

                    // clear the primed condition
                    owState.levelChangeOnNextBit = false;

                    // set new level state
                    owState.oneWireLevel = POWER_DELIVERY;
                }

                // send and receive
                char[] result_array = uTransaction(uBuild);

                // check the result
                if (result_array.length == (bit_offset + 1))
                    return uBuild.interpretOneWireBit(result_array[bit_offset]);
                else
                    return false;
            } else
                throw new OneWireIOException("Error communicating with adapter");
        } catch (IOException ex) {
            throw new OneWireIOException("Oops", ex);
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

        // check to make sure echo was what was sent
        if (temp_block[0] != (byte) byteValue)
            throw new OneWireIOException("Error short on 1-Wire during putByte");
    }

    @Override
    public int getByte() throws OneWireException {

        byte[] temp_block = new byte[1];

        temp_block[0] = (byte) 0xFF;

        dataBlock(temp_block, 0, 1);

        if (temp_block.length == 1)
            return (temp_block[0] & 0xFF);
        else
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

        int data_offset;
        char[] ret_data;

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // make sure adapter is present
            if (uAdapterPresent()) {

                // check for pending power conditions
                if (owState.oneWireLevel != NORMAL)
                    setPowerNormal();

                // set the correct baud rate to stream this operation
                setStreamingSpeed(OPERATION_BYTE);

                // flush out the com buffer
                serial.flush();

                // build a message to write/read data bytes to the U brick
                uBuild.restart();

                // check for primed byte
                if ((len == 1) && owState.levelChangeOnNextByte) {
                    data_offset = uBuild.primedDataByte(dataBlock[off]);
                    owState.levelChangeOnNextByte = false;

                    // send and receive
                    ret_data = uTransaction(uBuild);

                    // set new level state
                    owState.oneWireLevel = POWER_DELIVERY;

                    // extract the result byte
                    dataBlock[off] = uBuild.interpretPrimedByte(ret_data, data_offset);
                } else {
                    data_offset = uBuild.dataBytes(dataBlock, off, len);

                    // send and receive
                    ret_data = uTransaction(uBuild);

                    // extract the result byte(s)
                    uBuild.interpretDataBytes(ret_data, data_offset, dataBlock, off, len);
                }
            } else
                throw new OneWireIOException("Error communicating with adapter");
        } catch (IOException ex) {
            throw new OneWireIOException("Oops", ex);
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
            if (uAdapterPresent()) {

                // check for pending power conditions
                if (owState.oneWireLevel != NORMAL)
                    setPowerNormal();

                // flush out the com buffer
                serial.flush();

                // build a message to read the baud rate from the U brick
                uBuild.restart();

                int reset_offset = uBuild.oneWireReset();

                // send and receive
                char[] result_array = uTransaction(uBuild);

                // check the result
                if (result_array.length == (reset_offset + 1))
                    return uBuild.interpretOneWireReset(result_array[reset_offset]);
                else
                    throw new OneWireIOException("USerialAdapter-reset: no return byte form 1-Wire reset");
            } else
                throw new OneWireIOException("Error communicating with adapter");
        } catch (IOException ex) {
            throw new OneWireIOException("Oops", ex);
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public void setPowerDuration(PowerDeliveryDuration timeFactor) throws OneWireException {

        if (timeFactor != INFINITE)
            // VT: FIXME: Replace with UnsupportedOperationException?
            throw new OneWireException("USerialAdapter-setPowerDuration, does not support this duration, infinite only");
        else
            owState.levelTimeFactor = INFINITE;
    }

    @Override
    public boolean startPowerDelivery(PowerChangeCondition changeCondition) throws OneWireException {

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            if (changeCondition == AFTER_NEXT_BIT) {
                owState.levelChangeOnNextBit = true;
                owState.primedLevelValue = POWER_DELIVERY;
            } else if (changeCondition == AFTER_NEXT_BYTE) {
                owState.levelChangeOnNextByte = true;
                owState.primedLevelValue = POWER_DELIVERY;
            } else if (changeCondition == NOW) {

                // make sure adapter is present
                if (uAdapterPresent()) {

                    // check for pending power conditions
                    if (owState.oneWireLevel != NORMAL)
                        setPowerNormal();

                    // flush out the com buffer
                    serial.flush();

                    // build a message to read the baud rate from the U brick
                    uBuild.restart();

                    // set the SPUD time value
                    int set_SPUD_offset = uBuild.setParameter(PARAMETER_5VPULSE,
                            TIME5V_infinite);

                    // add the command to begin the pulse
                    uBuild.sendCommand(FUNCTION_5VPULSE_NOW, false);

                    // send and receive
                    char[] result_array = uTransaction(uBuild);

                    // check the result
                    if (result_array.length == (set_SPUD_offset + 1)) {
                        owState.oneWireLevel = POWER_DELIVERY;

                        return true;
                    }
                } else
                    throw new OneWireIOException("Error communicating with adapter");
            } else
                throw new OneWireException("Invalid power delivery condition");

            return false;
        } catch (IOException ex) {
            throw new OneWireIOException("Oops", ex);
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public void setProgramPulseDuration(PowerDeliveryDuration timeFactor) throws OneWireException {

        if (timeFactor != EPROM) {
            // VT: FIXME: Replace with UnsupportedOperationException?
            throw new OneWireException("Only support EPROM length program pulse duration");
        }
    }

    @Override
    public boolean startProgramPulse(PowerChangeCondition changeCondition) throws OneWireException {

        // check if adapter supports program
        if (!uState.programVoltageAvailable)
            throw new OneWireException("USerialAdapter: startProgramPulse, program voltage not available");

        // check if correct change condition
        if (changeCondition != NOW)
            throw new OneWireException("USerialAdapter: startProgramPulse, CONDITION_NOW only currently supported");

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // build a message to read the baud rate from the U brick
            uBuild.restart();

            // int set_SPUD_offset =
            uBuild.setParameter(PARAMETER_12VPULSE, TIME12V_512us);

            // add the command to begin the pulse
            // int pulse_offset =
            uBuild.sendCommand(FUNCTION_12VPULSE_NOW, true);

            // send the command
            // char[] result_array =
            uTransaction(uBuild);

            // check the result ??
            return true;
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public void startBreak() throws OneWireException {

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // power down the 2480 (dropping the 1-Wire)
            serial.setDTR(false);
            serial.setRTS(false);

            // wait for power to drop
            sleep(200);

            // set the level state
            owState.oneWireLevel = BREAK;
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public void setPowerNormal() throws OneWireException {

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            if (owState.oneWireLevel == POWER_DELIVERY) {

                // make sure adapter is present
                if (uAdapterPresent()) {

                    // flush out the com buffer
                    serial.flush();

                    // build a message to read the baud rate from the U brick
                    uBuild.restart();

                    // \\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                    // shughes - 8-28-2003
                    // Fixed the Set Power Level Normal problem where adapter
                    // is left in a bad state. Removed bad fix: extra getBit()
                    // SEE BELOW!
                    // stop pulse command
                    uBuild.sendCommand(FUNCTION_STOP_PULSE, true);

                    // start pulse with no prime
                    uBuild.sendCommand(FUNCTION_5VPULSE_NOW, false);
                    // \\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

                    // add the command to stop the pulse
                    int pulse_response_offset = uBuild.sendCommand(FUNCTION_STOP_PULSE, true);

                    // send and receive
                    char[] result_array = uTransaction(uBuild);

                    // check the result
                    if (result_array.length == (pulse_response_offset + 1)) {
                        owState.oneWireLevel = NORMAL;

                        // \\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                        // shughes - 8-28-2003
                        // This is a bad "fix", it was needed when we were
                        // causing
                        // a bad condition. Instead of fixing it here, we should
                        // fix it where we were causing it.. Which we did!
                        // SEE ABOVE!
                        // getBit();
                        // \\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                    } else
                        throw new OneWireIOException("Did not get a response back from stop power delivery");
                }
            } else if (owState.oneWireLevel == BREAK) {

                // restore power
                serial.setDTR(true);
                serial.setRTS(true);

                // wait for power to come up
                sleep(300);

                // set the level state
                owState.oneWireLevel = NORMAL;

                // set the DS2480 to the correct mode and verify
                adapterPresent = false;

                if (!uAdapterPresent())
                    throw new OneWireIOException("Did not get a response back from adapter after break");
            }
        } catch (IOException ex) {
            throw new OneWireIOException("Oops", ex);
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public void setSpeed(Speed speed) throws OneWireException {

        try {

            // acquire exclusive use of the port
            beginLocalExclusive();

            // check for valid speed
            if ((speed == REGULAR) || (speed == OVERDRIVE) || (speed == FLEX)) {

                // change 1-Wire speed
                owState.oneWireSpeed = speed;

                // set adapter to communicate at this new speed (regular == flex
                // for now)
                if (speed == OVERDRIVE)
                    uState.uSpeedMode = USPEED_OVERDRIVE;
                else
                    uState.uSpeedMode = USPEED_FLEX;
            } else
                throw new OneWireException("Requested speed is not supported by this adapter");
        } finally {

            // release local exclusive use of port
            endLocalExclusive();
        }
    }

    @Override
    public Speed getSpeed() {
        return owState.oneWireSpeed;
    }

    /**
     * Peform a search using the oneWire state provided
     *
     * @param mState current OneWire state used to do the search
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    private boolean search(OneWireState mState) throws OneWireException {

        int reset_offset = 0;

        // make sure adapter is present
        if (uAdapterPresent()) {

            // check for pending power conditions
            if (owState.oneWireLevel != NORMAL)
                setPowerNormal();

            // set the correct baud rate to stream this operation
            setStreamingSpeed(OPERATION_SEARCH);

            // reset the packet
            uBuild.restart();

            // add a reset/ search command
            if (!mState.skipResetOnSearch)
                reset_offset = uBuild.oneWireReset();

            if (mState.searchOnlyAlarmingButtons)
                uBuild.dataByte(ALARM_SEARCH_CMD);
            else
                uBuild.dataByte(NORMAL_SEARCH_CMD);

            // add search sequence based on mState
            int search_offset = uBuild.search(mState);

            // send/receive the search
            char[] result_array = uTransaction(uBuild);

            // interpret search result and return
            if (!mState.skipResetOnSearch)
                uBuild.interpretOneWireReset(result_array[reset_offset]);

            return uBuild.interpretSearch(mState, result_array, search_offset);
        } else
            throw new OneWireIOException("Error communicating with adapter");
    }

    /**
     * Perform a 'strongAccess' with the provided 1-Wire address. 1-Wire Network
     * has already been reset and the 'search' command sent before this is
     * called.
     *
     * @param address device address to do strongAccess on
     * @param alarmOnly verify device is present and alarming if true
     * @return true if device participated and was present in the strongAccess
     * search
     */
    private boolean blockIsPresent(byte[] address, boolean alarmOnly) throws OneWireException {

        // 24 bytes
        byte[] send_packet = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,(byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,(byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,(byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,(byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,(byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,(byte) 0xFF
                };
        int i;

        // reset the 1-Wire
        reset();

        // send search command
        if (alarmOnly) {
            putByte(ALARM_SEARCH_CMD);
        } else {
            putByte(NORMAL_SEARCH_CMD);
        }

        // now set or clear apropriate bits for search
        for (i = 0; i < 64; i++) {
            Bit.arrayWriteBit(Bit.arrayReadBit(i, 0, address), (i + 1) * 3 - 1, 0, send_packet);
        }

        // send to 1-Wire Net
        dataBlock(send_packet, 0, 24);

        // check the results of last 8 triplets (should be no conflicts)
        int cnt = 56, goodbits = 0, tst, s;

        for (i = 168; i < 192; i += 3) {
            tst = (Bit.arrayReadBit(i, 0, send_packet) << 1) | Bit.arrayReadBit(i + 1, 0, send_packet);
            s = Bit.arrayReadBit(cnt++, 0, address);

            if (tst == 0x03) // no device on line
            {
                goodbits = 0; // number of good bits set to zero

                break; // quit
            }

            if (((s == 0x01) && (tst == 0x02)) || ((s == 0x00) && (tst == 0x01))) // correct bit
                goodbits++; // count as a good bit
        }

        // check too see if there were enough good bits to be successful
        return (goodbits >= 8);
    }

    /**
     * set the correct baud rate to stream this operation
     */
    private void setStreamingSpeed(int operation) throws OneWireIOException {

        ThreadContext.push("setStreamingSpeed(" + operation + ")");

        try {

            // get the desired baud rate for this operation
            int baud = UPacketBuilder.getDesiredBaud(operation, owState.oneWireSpeed, maxBaud);

            // check if already at the correct speed
            if (baud == serial.getBaudRate()) {
                return;
            }

            logger.debug("Changing baud rate from {} to {}", serial.getBaudRate(), baud);

            // convert this baud to 'u' baud
            char ubaud;

            switch (baud) {

            case 115200:
                ubaud = UAdapterState.BAUD_115200;
                break;
            case 57600:
                ubaud = UAdapterState.BAUD_57600;
                break;
            case 19200:
                ubaud = UAdapterState.BAUD_19200;
                break;
            case 9600:
            default:
                ubaud = BAUD_9600;
                break;
            }

            // see if this is a new baud
            if (ubaud == uState.ubaud) {
                return;
            }

            // default, loose communication with adapter
            adapterPresent = false;

            // build a message to read the baud rate from the U brick
            uBuild.restart();

            int baud_offset = uBuild.setParameter(PARAMETER_BAUDRATE, ubaud);

            try {
                // send command, no response at this baud rate
                serial.flush();

                RawSendPacket pkt = uBuild.getPackets().iterator().next();
                char[] temp_buf = new char[pkt.buffer.length()];

                pkt.buffer.getChars(0, pkt.buffer.length(), temp_buf, 0);
                serial.write(temp_buf);

                // delay to let things settle
                sleep(5);
                serial.flush();

                // set the baud rate
                sleep(5); // solaris hack!!!
                serial.setBaudRate(baud);
            } catch (IOException ex) {
                throw new OneWireIOException("Oops", ex);
            }

            uState.ubaud = ubaud;

            // delay to let things settle
            sleep(5);

            // verify adapter is at new baud rate
            uBuild.restart();

            baud_offset = uBuild.getParameter(PARAMETER_BAUDRATE);

            // set the DS2480 communication speed for subsequent blocks
            uBuild.setSpeed();

            try {

                // send and receive
                serial.flush();

                char[] result_array = uTransaction(uBuild);

                // check the result
                if (result_array.length == 1) {
                    if (((result_array[baud_offset] & 0xF1) == 0) && ((result_array[baud_offset] & 0x0E) == uState.ubaud)) {
                        logger.debug("Success, baud changed and DS2480 is there");

                        // adapter still with us
                        adapterPresent = true;

                        // flush any garbage characters
                        sleep(150);
                        serial.flush();

                        return;
                    }
                }
            } catch (IOException|OneWireIOException ex) {
                logger.error("USerialAdapter-setStreamingSpeed", ex);
            }

            logger.error("Failed to change baud of DS2480");

        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Verify that the DS2480 based adapter is present on the open port.
     *
     * @return 'true' if adapter present
     */
    private synchronized boolean uAdapterPresent() {

        boolean rt = true;

        // check if adapter has already be verified to be present
        if (!adapterPresent) {

            // do a master reset
            uMasterReset();

            // attempt to verify
            if (!uVerify()) {

                // do a master reset and try again
                uMasterReset();

                if (!uVerify()) {

                    // do a power reset and try again
                    uPowerReset();

                    if (!uVerify())
                        rt = false;
                }
            }
        }

        adapterPresent = rt;

        logger.debug("AdapterPresent result: {}", rt);

        return rt;
    }

    /**
     * Do a master reset on the DS2480. This reduces the baud rate to 9600 and
     * performs a break. A single timing byte is then sent.
     */
    private synchronized void uMasterReset() {

        ThreadContext.push("uMasterReset");

        try {

            logger.debug("uMasterReset()");

            // try to acquire the port
            try {

                // set the baud rate
                serial.setBaudRate(9600);

                // put back to standard speed
                owState.oneWireSpeed = REGULAR;
                uState.uSpeedMode = USPEED_FLEX;
                uState.ubaud = BAUD_9600;

                // send a break to reset DS2480
                serial.sendBreak(10);
                sleep(5);

                // send the timing byte
                serial.flush();
                serial.write(FUNCTION_RESET);
                serial.flush();
            } catch (IOException ex) {
                logger.error("Reset failed", ex);
            }
        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Do a power reset on the DS2480. This reduces the baud rate to 9600 and
     * powers down the DS2480. A single timing byte is then sent.
     */
    private synchronized void uPowerReset() {

        ThreadContext.push("uPowerReset");

        try {

            logger.debug("uPowerReset()");

            // try to acquire the port
            try {

                // set the baud rate
                serial.setBaudRate(9600);

                // put back to standard speed
                owState.oneWireSpeed = REGULAR;
                uState.uSpeedMode = USPEED_FLEX;
                uState.ubaud = BAUD_9600;

                // power down DS2480
                serial.setDTR(false);
                serial.setRTS(false);
                sleep(300);
                serial.setDTR(true);
                serial.setRTS(true);
                sleep(1);

                // send the timing byte
                serial.flush();
                serial.write(FUNCTION_RESET);
                serial.flush();
            } catch (IOException ex) {
                logger.error("Reset failed", ex);
            }
        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Read and verify the baud rate with the DS2480 chip and perform a single
     * bit MicroLAN operation. This is used as a DS2480 detect.
     *
     * @return 'true' if the correct baud rate and bit operation was read from
     * the DS2480
     */
    private boolean uVerify() {

        try {
            serial.flush();

            // build a message to read the baud rate from the U brick
            uBuild.restart();

            // VT: FIXME: Consider making uParameters a map
            uBuild.setParameter(PARAMETER_SLEW,
                    uState.uParameters[owState.oneWireSpeed.code].pullDownSlewRate);
            uBuild.setParameter(PARAMETER_WRITE1LOW,
                    uState.uParameters[owState.oneWireSpeed.code].write1LowTime);
            uBuild.setParameter(PARAMETER_SAMPLEOFFSET,
                    uState.uParameters[owState.oneWireSpeed.code].sampleOffsetTime);
            uBuild.setParameter(PARAMETER_5VPULSE, TIME5V_infinite);
            var baud_offset = uBuild.getParameter(PARAMETER_BAUDRATE);
            var bit_offset = uBuild.dataBit(true, false);

            // send and receive
            char[] result_array = uTransaction(uBuild);

            // check the result
            if (result_array.length == (bit_offset + 1)) {
                if (((result_array[baud_offset] & 0xF1) == 0) && ((result_array[baud_offset] & 0x0E) == uState.ubaud)
                        && ((result_array[bit_offset] & 0xF0) == 0x90)
                        && ((result_array[bit_offset] & 0x0C) == uState.uSpeedMode))
                    return true;
            }
        } catch (IOException | OneWireIOException ex) {
            logger.error("USerialAdapter-uVerify: ", ex);
        }

        return false;
    }

    /**
     * Write the raw U packet and then read the result.
     *
     * @param tempBuild the U Packet Build where the packet to send resides
     * @return the result array
     * @throws OneWireIOException on a 1-Wire communication error
     */
    private char[] uTransaction(UPacketBuilder tempBuild) throws OneWireIOException {

        try {
            // clear the buffers
            serial.flush();
            inBuffer.setLength(0);

            // loop to send all the packets
            for (RawSendPacket pkt : tempBuild.getPackets()) {

                // get the next packet
                // bogus packet to indicate need to wait for long DS2480 alarm
                // reset
                if ((pkt.buffer.length() == 0) && (pkt.returnLength == 0)) {
                    sleep(6);
                    serial.flush();

                    continue;
                }

                // get the data
                char[] temp_buf = new char[pkt.buffer.length()];

                pkt.buffer.getChars(0, pkt.buffer.length(), temp_buf, 0);

                // send the packet
                serial.write(temp_buf);

                // wait on returnLength bytes in inBound
                inBuffer.append(serial.readWithTimeout(pkt.returnLength));
            }

            // read the return packet
            char[] ret_buffer = new char[inBuffer.length()];

            inBuffer.getChars(0, inBuffer.length(), ret_buffer, 0);

            // clear the inbuffer
            inBuffer.setLength(0);

            return ret_buffer;
        } catch (IOException ex) {

            // need to check on adapter
            adapterPresent = false;

            // pass it on
            throw new OneWireIOException("Oops", ex);
        }
    }

    /**
     * Sleep for the specified number of milliseconds
     */
    private void sleep(long msTime) {

        logger.debug("sleep({})", msTime);

        try {
            Thread.sleep(msTime);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.debug("sleep interrupted", ex);
        }
    }
}
