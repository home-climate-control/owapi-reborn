
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

package com.dalsemi.onewire.utils;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.SwitchContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 1-Wire&#174 Network path.  Large 1-Wire networks can be sub-divided into branches
 * for load, location, or organizational reasons.  Once 1-Wire devices are placed
 * on these branches there needs to be a mechanism to reach these devices.  The
 * OWPath class was designed to provide a convenient method to open and close
 * 1-Wire paths to reach remote devices.
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> <H4> Example</H4>
 * Open the path 'path' to the 1-Wire temperature device 'tc' and read the temperature:
 * <PRE> <CODE>
 *  // open a path to the temp device
 *  path.open();
 *
 *  // read the temp device
 *  byte[] state = tc.readDevice();
 *  tc.doTemperatureConvert(state);
 *  state = tc.readDevice();
 *  System.out.println("Temperature of " +
 *           address + " is " +
 *           tc.getTemperature(state) + " C");
 *
 *  // close the path to the device
 *  path.close();
 * </CODE> </PRE>
 * </DL>
 *
 * @see OWPathElement
 * @see com.dalsemi.onewire.container.SwitchContainer
 * @see com.dalsemi.onewire.container.OneWireContainer05
 * @see com.dalsemi.onewire.container.OneWireContainer12
 * @see com.dalsemi.onewire.container.OneWireContainer1F
 *
 * @author DS
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 */
public class OWPath implements Comparable<OWPath> {

    protected final Logger logger = LogManager.getLogger(getClass());

    private final List<OWPathElement> elements;
    private final DSPortAdapter adapter;

    /**
     * Create a new 1-Wire path with no elements.
     *
     * @param adapter where the path is based.
     */
    public OWPath(DSPortAdapter adapter) {
        this(adapter, null);
    }

    /**
     * Create a new path with a starting path.
     *
     * @param  adapter where the 1-Wire path is based
     * @param  currentOWPath starting value of this 1-Wire path
     */
    public OWPath(DSPortAdapter adapter, OWPath currentOWPath) {

        this.adapter = adapter;
        elements     = new ArrayList<>();

        copy(currentOWPath);
    }

    /**
     * Copy the elements from the provided 1-Wire path into this 1-Wire path.
     *
     * @param  currentOWPath path to copy from.
     */
    public void copy(OWPath currentOWPath) {

        elements.clear();

        if (currentOWPath == null) {
            return;
        }

        elements.addAll(currentOWPath.asList());
    }

    /**
     * Add a 1-Wire path element to this 1-Wire path.
     *
     * @param container 1-Wire device switch
     * @param channel of device that represents this 1-Wire path element
     *
     * @see #copy(OWPath) copy
     */
    public void add(SwitchContainer container, int channel) {
        elements.add(new OWPathElement(container, channel));
    }

    /**
     * Get the list of all the 1-Wire path elements in this 1-Wire path.
     *
     * @return The path as a list of elements.
     */
    public List<OWPathElement> asList() {
        return elements;
    }

    /**
     * Get a string representation of this 1-Wire path.
     *
     * @return string 1-Wire path as string
     */
    @Override
    public String toString () {

        StringBuilder sb = new StringBuilder();

        // append 'drive'

        try {

            sb.append(adapter.getAdapterName()).append("_").append(adapter.getPortName()).append("/");

        } catch (OneWireException ex) {

            // VT: FIXME: WTF? Swallowing exception?

            logger.fatal("DalSemi ignored this exception", ex);

            // VT: FIXME: This also creates a problem with possibly different
            // string representations of this object, depending on the
            // network state. Since both equals() and hashCode() depend on
            // this method, this introduces a problem wherever the string
            // representation is used.
            //
            // Bottomline: I guess it would be a good idea to cache the
            // string representation and change it whenever a path elements
            // gets added and/or removed.

            sb.append(adapter.getAdapterName()).append("/");
        }

        for (var owPathElement : elements) {

            var owc = owPathElement.container;

            // append 'directory' name

            sb.append(((OneWireContainer) owc).getAddressAsString()).append("_").append(owPathElement.channel).append("/");
        }

        return sb.toString();
    }

    /**
     * Open this 1-Wire path so that a remote device can be accessed.
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         no device present or a CRC read from the device is incorrect.  This could be
     *         caused by a physical interruption in the 1-Wire Network due to
     *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter.
     */
    public void open() throws OneWireException {

        for (var element : elements) {

            var sw = element.container;

            // turn on the elements channel
            var swState = sw.readDevice();

            sw.setLatchState(element.channel, true, sw.hasSmartOn(), swState);
            sw.writeDevice(swState);
        }

        // check if not depth in path, do a reset so a resetless search will work
        if (elements.isEmpty()) {
            adapter.reset();
        }
    }

    /**
     * Close each element in this 1-Wire path in reverse order.
     *
     * @throws OneWireIOException on a 1-Wire communication error such as
     *         no device present or a CRC read from the device is incorrect.  This could be
     *         caused by a physical interruption in the 1-Wire Network due to
     *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     * @throws OneWireException on a communication or setup error with the 1-Wire
     *         adapter.
     */
    public void close() throws OneWireException {

        // loop through elements in path in reverse order
        for (int i = elements.size() - 1; i >= 0; i--) {

            var pathElement = elements.get(i);
            var sw = pathElement.container;

            // turn off the elements channel
            var swState = sw.readDevice();
            sw.setLatchState(pathElement.channel, false, false, swState);
            sw.writeDevice(swState);
        }
    }

    @Override
    public int compareTo(OWPath other) {

        if (other == null) {
            throw new IllegalArgumentException("other can't be null");
        }

        return toString().compareTo(other.toString());
    }

    @Override
    public boolean equals(Object other) {

        if (other == null) {
            return false;
        }

        if (!(other instanceof OWPath)) {
            return false;
        }

        return this.toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
