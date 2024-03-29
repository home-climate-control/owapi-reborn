
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

// imports

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.utils.Address;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;


/**
 * A <code>OneWireContainer</code> encapsulates the <code>DSPortAdapter</code>,
 * the 1-Wire&#174 network address, and methods to manipulate a specific 1-Wire device. A
 * 1-Wire device may be in the form of a stainless steel armored can, called an iButton&#174,
 * or in standard IC plastic packaging.
 *
 * <p>General 1-Wire device container class with basic communication functions.
 * This class should only be used if a device specific class is not available
 * or known.  Most <code>OneWireContainer</code> classes will extend this basic class.
 *
 * <P> 1-Wire devices with memory can be accessed through the objects that
 * are returned from the {@link #getMemoryBanks() getMemoryBanks} method. See the
 * usage example below. </P>
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> <H4> Example 1</H4>
 * Iterate through memory banks retrieved from the OneWireContainer
 * instance 'owd' and cast to the highest interface.  See the
 * interface descriptions
 * {@link MemoryBank}, {@link PagedMemoryBank}, and {@link OTPMemoryBank} for specific examples.
 * <PRE> <CODE>
 *  MemoryBank      mb;
 *  PagedMemoryBank pg_mb;
 *  OTPMemoryBank   otp_mb;
 *
 *  for(var bank_enum = owd.getMemoryBanks().iterator();
 *                      bank_enum.hasMoreElements(); )
 *  {
 *     // get the next memory bank, cast to MemoryBank
 *     mb = (MemoryBank)bank_enum.next();
 *
 *     // check if has paged services
 *     if (mb instanceof PagedMemoryBank)
 *         pg_mb = (PagedMemoryBank)mb;
 *
 *     // check if has One-Time-Programable services
 *     if (mb instanceof OTPMemoryBank)
 *         otp_mb = (OTPMemoryBank)mb;
 *  }
 * </CODE> </PRE>
 * </DL>
 *
 * @see com.dalsemi.onewire.container.MemoryBank
 * @see com.dalsemi.onewire.container.PagedMemoryBank
 * @see com.dalsemi.onewire.container.OTPMemoryBank
 *
 * @author DS
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 */
public class OneWireContainer {

    protected final Logger logger = LogManager.getLogger();

    /**
     * Reference to the adapter that is needed to communicate with this
     * iButton or 1-Wire device.
     */
    protected DSPortAdapter adapter;

    /**
     * 1-Wire Network Address of this iButton or 1-Wire
     * device.
     * Family code is byte at offset 0.
     * @see com.dalsemi.onewire.utils.Address
     */
    protected final byte[] address = new byte[8];

    /**
     * Communication speed requested.
     *
     * @see DSPortAdapter#setSpeed
     */
    protected DSPortAdapter.Speed speed;

    /**
     * Flag to indicate that falling back to a slower speed then requested
     * is OK.
     */
    protected boolean speedFallBackOK;

    /**
     * Create an empty container.  Must call {@code setupContainer} before
     * using this new container.
     *
     * This is one of the methods to construct a container. The others are
     * through creating a OneWireContainer with parameters, they are preferred.
     *
     * @see #OneWireContainer(DSPortAdapter,byte[])
     * @see #OneWireContainer(DSPortAdapter,long)
     * @see #OneWireContainer(DSPortAdapter,String)
     * @see #setupContainer(DSPortAdapter,byte[])
     * @see #setupContainer(DSPortAdapter,long)
     * @see #setupContainer(DSPortAdapter,String)
     */
    public OneWireContainer () {
    }

    /**
     * Create a container with a provided adapter object
     * and the address of the iButton or 1-Wire device.<p>
     *
     * This is one of the methods to construct a container.  The other (discouraged) is
     * through creating a OneWireContainer with NO parameters.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     * this iButton.
     * @param  newAddress        address of this 1-Wire device
     * @see #OneWireContainer()
     * @see com.dalsemi.onewire.utils.Address
     */
    public OneWireContainer (DSPortAdapter sourceAdapter, byte[] newAddress) {
        this.setupContainer(sourceAdapter, newAddress);
    }

    /**
     * Create a container with a provided adapter object
     * and the address of the iButton or 1-Wire device.
     *
     * This is one of the methods to construct a container.  The other (discouraged) is
     * through creating a OneWireContainer with NO parameters.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     * this iButton.
     * @param  newAddress        address of this 1-Wire device
     * @see #OneWireContainer()
     * @see com.dalsemi.onewire.utils.Address
     */
    public OneWireContainer(DSPortAdapter sourceAdapter, long newAddress) {
        this.setupContainer(sourceAdapter, newAddress);
    }

    /**
     * Create a container with a provided adapter object
     * and the address of the iButton or 1-Wire device.
     *
     * This is one of the methods to construct a container.  The other (discouraged) is
     * through creating a OneWireContainer with NO parameters.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     * this iButton.
     * @param  newAddress        address of this 1-Wire device
     * @see #OneWireContainer()
     * @see com.dalsemi.onewire.utils.Address
     */
    public OneWireContainer(DSPortAdapter sourceAdapter, String newAddress) {
        this.setupContainer(sourceAdapter, newAddress);
    }

    /**
     * Provides this container with the adapter object used to access this device and
     * the address of the iButton or 1-Wire device.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     *                           this iButton
     * @param  newAddress        address of this 1-Wire device
     * @see com.dalsemi.onewire.utils.Address
     */
    public synchronized void setupContainer(DSPortAdapter sourceAdapter, byte[] newAddress) {

        // get a reference to the source adapter (will need this to communicate)
        adapter = sourceAdapter;

        // set the Address
        System.arraycopy(newAddress, 0, address, 0, 8);

        // set desired speed to be SPEED_REGULAR by default with no fallback
        speed           = DSPortAdapter.Speed.REGULAR;
        speedFallBackOK = false;
    }

    /**
     * Provides this container with the adapter object used to access this device and
     * the address of the iButton or 1-Wire device.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     *                           this iButton
     * @param  newAddress        address of this 1-Wire device
     * @see com.dalsemi.onewire.utils.Address
     */
    public synchronized void setupContainer(DSPortAdapter sourceAdapter, long newAddress) {

        // get a reference to the source adapter (will need this to communicate)
        adapter = sourceAdapter;

        // set the Address
        Address.toByteArray(newAddress, address);

        // set desired speed to be SPEED_REGULAR by default with no fallback
        speed           = DSPortAdapter.Speed.REGULAR;
        speedFallBackOK = false;
    }

    /**
     * Provides this container with the adapter object used to access this device and
     * the address of the iButton or 1-Wire device.
     *
     * @param  sourceAdapter     adapter object required to communicate with
     *                           this iButton
     * @param  newAddress        address of this 1-Wire device
     * @see com.dalsemi.onewire.utils.Address
     */
    public synchronized void setupContainer(DSPortAdapter sourceAdapter, String newAddress) {

        // get a reference to the source adapter (will need this to communicate)
        adapter = sourceAdapter;

        // set the Address
        Address.toByteArray(newAddress, address);

        // set desired speed to be SPEED_REGULAR by default with no fallback
        speed           = DSPortAdapter.Speed.REGULAR;
        speedFallBackOK = false;
    }

    /**
     * Retrieves the port adapter object used to create this container.
     *
     * @return port adapter instance
     */
    public DSPortAdapter getAdapter() {
        return adapter;
    }

    /**
     * Retrieves the Dallas Semiconductor part number of the 1-Wire device
     * as a <code>String</code>.  For example 'Crypto iButton' or 'DS1992'.
     *
     * @return 1-Wire device name
     */
    public synchronized String getName() {

        return "Device type: "
        + (((address [0] & 0x0FF) < 16)
                ? ("0" + Integer.toHexString(address [0] & 0x0FF))
                        : Integer.toHexString(address [0] & 0x0FF));
    }

    /**
     * Retrieves the alternate Dallas Semiconductor part numbers or names.
     * A 'family' of 1-Wire Network devices may have more than one part number
     * depending on packaging.  There can also be nicknames such as
     * 'Crypto iButton'.
     *
     * @return 1-Wire device alternate names
     */
    public String getAlternateNames() {
        return "";
    }

    /**
     * Retrieves a short description of the function of the 1-Wire device type.
     *
     * @return device functional description
     */
    public String getDescription() {
        return "No description available.";
    }

    /**
     * Sets the maximum speed for this container.  Note this may be slower then the
     * devices maximum speed.  This method can be used by an application
     * to restrict the communication rate due 1-Wire line conditions. <p>
     *
     * @param newSpeed Adapter speed requested.
     * @param fallBack boolean indicating it is OK to fall back to a slower speed if {@code true}.
     *
     */
    public void setSpeed(DSPortAdapter.Speed newSpeed, boolean fallBack) {
        speed           = newSpeed;
        speedFallBackOK = fallBack;
    }

    /**
     * Returns the maximum speed this iButton or 1-Wire device can
     * communicate at.
     * Override this method if derived iButton type can go faster then
     * SPEED_REGULAR(0).
     *
     * @return maximum speed
     * @see DSPortAdapter#setSpeed
     */
    public DSPortAdapter.Speed getMaxSpeed() {
        return DSPortAdapter.Speed.REGULAR;
    }

    /**
     * Gets the 1-Wire Network address of this device as an array of bytes.
     *
     * @return 1-Wire address
     * @see com.dalsemi.onewire.utils.Address
     */
    public byte[] getAddress() {
        return address;
    }

    /**
     * Gets this device's 1-Wire Network address as a String.
     *
     * @return 1-Wire address
     * @see com.dalsemi.onewire.utils.Address
     */
    public String getAddressAsString() {
        return Address.toString(address);
    }

    /**
     * Gets this device's 1-Wire Network address as a long.
     *
     * @return 1-Wire address
     * @see com.dalsemi.onewire.utils.Address
     */
    public long getAddressAsLong() {
        return Address.toLong(address);
    }

    /**
     * Returns a list of memory banks, default is none.
     *
     * @return List of memory banks to read and write memory on this iButton or 1-Wire device.
     */
    public List<MemoryBank> getMemoryBanks() {
        return List.of();
    }

    /**
     * Verifies that the iButton or 1-Wire device is present on
     * the 1-Wire Network.
     *
     * @return  <code>true</code> if device present on the 1-Wire Network
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         a read back verification fails.
     * @throws OneWireException if adapter is not open
     */
    public synchronized boolean isPresent() throws OneWireException {
        return adapter.isPresent(address);
    }

    /**
     * Verifies that the iButton or 1-Wire device is present
     * on the 1-Wire Network and in an alarm state.  This does not
     * apply to all device types.
     *
     * @return  <code>true</code> if device present and in alarm condition
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         a read back verification fails.
     * @throws OneWireException if adapter is not open
     */
    public synchronized boolean isAlarming() throws OneWireException {
        return adapter.isAlarming(address);
    }

    /**
     * Go to the specified speed for this container.  This method uses the
     * containers selected speed (method setSpeed(speed, fallback)) and
     * will optionally fall back to a slower speed if communciation failed.
     * Only call this method once to get the device into the desired speed
     * as long as the device is still responding.
     *
     * @throws OneWireIOException WHEN selected speed fails and fallback
     *                                 is false
     * @throws OneWireException WHEN hypterdrive is selected speed
     * @see #setSpeed(com.dalsemi.onewire.adapter.DSPortAdapter.Speed,boolean)
     */
    public void doSpeed() throws OneWireException {

        boolean is_present = false;

        try {
            // check if already at speed and device present
            if ((speed == adapter.getSpeed()) && adapter.isPresent(address))
                return;
        } catch (OneWireIOException ex) {
            // VOID
            logger.fatal("DalSemi ignored this exception", ex);
        }

        // speed Overdrive
        if (speed == DSPortAdapter.Speed.OVERDRIVE) {

            try {
                // get this device and adapter to overdrive
                adapter.setSpeed(DSPortAdapter.Speed.REGULAR);
                adapter.reset();
                adapter.putByte(( byte ) 0x69);
                adapter.setSpeed(DSPortAdapter.Speed.OVERDRIVE);

            } catch (OneWireIOException ex) {
                // VOID
                logger.fatal("DalSemi ignored this exception", ex);
            }

            // get copy of address
            synchronized (this) {

                // 8 bytes
                byte[] addressCopy = {
                        (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x00,
                        };

                System.arraycopy(address, 0, addressCopy, 0, 8);
                adapter.dataBlock(addressCopy, 0, 8);
            }

            try {
                is_present = adapter.isPresent(address);
            } catch (OneWireIOException ex) {
                // VOID
                logger.fatal("DalSemi ignored this exception", ex);
            }

            // check if new speed is OK
            if (!is_present) {

                // check if allow fallback
                if (speedFallBackOK) {
                    adapter.setSpeed(DSPortAdapter.Speed.REGULAR);
                } else {
                    throw new OneWireIOException(address, "Failed to get device to selected speed (overdrive)");
                }
            }
        }
        // speed regular or flex
        else if ((speed == DSPortAdapter.Speed.REGULAR)
                || (speed == DSPortAdapter.Speed.FLEX)) {
            adapter.setSpeed(speed);
        // speed hyperdrive, don't know how to do this
        } else {
            throw new OneWireException(address, "Speed selected (hyperdrive) is not supported by this method");
        }
    }

    @Override
    public int hashCode() {
        return Long.valueOf(Address.toLong(this.address)).hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if(o==this) {
            return true;
        }

        if(o instanceof OneWireContainer) {

            OneWireContainer owc = (OneWireContainer)o;
            // don't claim that all subclasses of a specific container are
            // equivalent to the parent container
            if(owc.getClass()==this.getClass()) {
                return owc.getAddressAsLong()==this.getAddressAsLong();
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return Address.toString(this.address) + " " + this.getName();
    }
}
