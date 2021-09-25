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

// OneWireAccessProvider.java
package com.dalsemi.onewire;

import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.adapter.USerialAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * The OneWireAccessProvider class manages the Dallas Semiconductor adapter
 * class derivatives of <code>DSPortAdapter</code>. A list of all
 * available adapters can be accessed through the member function
 * {@link #getAdapters()}. This enables an application to be
 * adapter independent. There are also facilities to get a system appropriate
 * default adapter/port combination.
 * <p>
 * <H3> Usage </H3>
 * <DL>
 * <DD>
 * <H4> Example 1</H4>
 * Get an instance of the default 1-Wire adapter. The adapter will be ready to
 * use if no exceptions are thrown.
 *
 * <PRE> <CODE> try { DSPortAdapter adapter =
 * OneWireAccessProvider.getDefaultAdapter(); System.out.println("Adapter: " +
 * adapter.getAdapterName() + " Port: " + adapter.getPortName()); // use the
 * adapter ... } catch(Exception e) { System.out.println("Default adapter not
 * present: " + e); } </CODE> </PRE>
 *
 * </DL>
 * <DL>
 * <DD>
 * <H4> Example 2</H4>
 * Iterate through the available adapters and ports.
 *
 * <PRE> <CODE> DSPortAdapter adapter; String port; // get the adapters for
 * (Iterator adapter_enum = OneWireAccessProvider.getAllAdapters().iterator();
 * adapter_enum.hasNext(); ) { // cast the enum as a DSPortAdapter
 * adapter = ( DSPortAdapter ) adapter_enum.next();
 * System.out.print("Adapter: " + adapter.getAdapterName() + " with ports: "); //
 * get the ports for (Iterator port_enum = adapter.getPortNames().iterator();
 * port_enum.hasNext(); ) { // cast the enum as a String port = ( String )
 * port_enum.next(); System.out.print(port + " "); }
 * System.out.println(); } </CODE> </PRE>
 *
 * </DL>
 * <DL>
 * <DD>
 * <H4> Example 3</H4>
 * Display the default adapter name and port without getting an instance of the
 * adapter.
 *
 * <PRE> <CODE> System.out.println("Default Adapter: " +
 * OneWireAccessProvider.getProperty("onewire.adapter.default"));
 * System.out.println("Default Port: " +
 * OneWireAccessProvider.getProperty("onewire.port.default")); </CODE> </PRE>
 *
 * </DL>
 *
 * @see DSPortAdapter
 * @author DS
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 *
 * @deprecated Too cumbersome. Create an instance of {@link USerialAdapter#USerialAdapter()} directly, and then
 * call {@link DSPortAdapter#selectPort(String)} on it.
 */
@Deprecated(since = "2021-09-25", forRemoval = true)
public class OneWireAccessProvider {

    protected static final Logger logger = LogManager.getLogger(OneWireAccessProvider.class);

    /**
     * Don't allow anyone to instantiate.
     */
    private OneWireAccessProvider() {

    }

    /**
     * Gets a list of all 1-Wire adapter types supported.
     * Using this list with the port list for each adapter, a
     * search can be done to find all available hardware adapters.
     *
     * @return list of {@link DSPortAdapter} instances in the system.
     */
    public static synchronized List<DSPortAdapter> getAdapters() {

        var adaptersFound = new ArrayList<DSPortAdapter>();

        // get the pure java adapter
        try {
            Class<?> adapterClass = Class.forName(USerialAdapter.class.getName());
            DSPortAdapter adapterInstance = (DSPortAdapter) adapterClass.newInstance();

            // check if has any ports (common javax.comm problem)
            if (DSPortAdapter.getPortNames().isEmpty()) {
                logger.warn("Warning: serial communications API not setup properly, no ports detected ");
                logger.warn("Pure-Java DS9097U adapter will not work, not added to adapter list");
            } else {
                adaptersFound.add(adapterInstance);
            }

        } catch (Exception ex) {
            // DRAIN
            logger.fatal("DalSemi ignored this exception", ex);
        }

        // get adapters from property file with keys
        // 'onewire.register.adapter0-15'

        // VT: FIXME: Need to see if this is a bug: the nesting of the loop and try/catc seems funky,
        // the loop will be broken on the first unsuccessful attempt

        String className = null;

        try {
            // loop through the possible registered adapters
            for (int reg_num = 0; reg_num <= 15; reg_num++) {
                className = getProperty("onewire.register.adapter" + reg_num);

                // done if no property by that name
                if (className == null)
                    break;

                // add it to the enum
                Class<?> adapterClass = Class.forName(className);
                DSPortAdapter adapterInstance = (DSPortAdapter) adapterClass.newInstance();
                adaptersFound.add(adapterInstance);
            }
        } catch (Exception ex) {
            // DRAIN
            logger.fatal("DalSemi ignored this exception", ex);
        }

        // check for no adapters
        if (adaptersFound.isEmpty())
            logger.error("No 1-Wire adapter classes found");

        return (adaptersFound);
    }

    /**
     * Finds, opens, and verifies the specified adapter on the indicated port.
     *
     * @param adapterName string name of the adapter (match to result of call to
     * getAdapterName() method in DSPortAdapter).
     * @param portName string name of the port used in the method selectPort()
     * in DSPortAdapter.
     * @return <code>DSPortAdapter</code> if adapter present.
     * @throws OneWireIOException when communication with the adapter fails.
     * @throws OneWireException when the port or adapter not present.
     */
    public static synchronized DSPortAdapter getAdapter(String adapterName, String portName) throws OneWireException {

        DSPortAdapter found = null;


        // iterate through available adapters to find the correct one
        for (DSPortAdapter adapter : getAdapters()) {
            // cast the enum as a DSPortAdapter
            // see if this is the type of adapter we want
            if ((found != null) || (!adapter.getAdapterName().equals(adapterName))) {
                // not this adapter, then just cleanup
                try {
                    adapter.freePort();
                } catch (Exception ex) {
                    // DRAIN
                    logger.fatal("DalSemi ignored this exception", ex);
                }
                continue;
            }

            // attempt to open and verify the adapter
            if (adapter.selectPort(portName)) {
                adapter.beginExclusive();

                try {
                    // check for the adapter
                    if (adapter.adapterDetected())
                        found = adapter;
                    else {

                        // close the port just opened
                        adapter.freePort();

                        throw new OneWireException("Port found \"" + portName + "\" but Adapter \"" + adapterName
                                + "\" not detected");
                    }
                } finally {
                    adapter.endExclusive();
                }
            } else
                throw new OneWireException("Specified port \"" + portName + "\" could not be selected for adapter \""
                        + adapterName + "\"");
        }

        // if adapter found then return it
        if (found != null)
            return found;

        // adapter by that name not found
        throw new OneWireException("Specified adapter name \"" + adapterName + "\" is not known");
    }

    /**
     * Gets the specified onewire property. Looks for the property in the
     * following locations:
     * <p>
     * <ul>
     * <li> In System.properties
     * <li> In onewire.properties file in current directory or < java.home
     * >/lib/ (Desktop) or /etc/ (TINI)
     * <li> 'smart' default if property is 'onewire.adapter.default' or
     * 'onewire.port.default'
     * </ul>
     *
     * @param propName string name of the property to read
     * @return <code>String</code> representing the property value or
     * <code>null</code> if it could not be found (<code>onewire.adapter.default</code>
     * and <code>onewire.port.default</code> may return a 'smart' default even
     * if property not present)
     */
    public static synchronized String getProperty(String propName) {

        Properties onewire_properties = new Properties();
        FileInputStream prop_file = null;
        String ret_str = null;
        DSPortAdapter adapter_instance;
        Class<?> adapter_class;

        // try system properties
        try {
            ret_str = System.getProperty(propName, null);
        } catch (Exception e) {
            ret_str = null;
        }

        // if defaults not found then try onewire.properties file
        if (ret_str == null) {

            // loop to attempt to open the onewire.properties file in two
            // locations
            // .\onewire.properties or <java.home>\lib\onewire.properties
            String path = new String("");

            for (int i = 0; i <= 1; i++) {

                // attempt to open the onewire.properties file
                try {
                    prop_file = new FileInputStream(path + "onewire.properties");
                } catch (Exception e) {
                    prop_file = null;
                }

                // if open, then try to read value
                if (prop_file != null) {

                    // attempt to read the onewire.properties
                    try {
                        onewire_properties.load(prop_file);

                        ret_str = onewire_properties.getProperty(propName, null);
                    } catch (Exception e) {
                        ret_str = null;
                    }
                }

                // check to see if we now have the value
                if (ret_str != null)
                    break;

                // try the second path
                path = System.getProperty("java.home") + File.separator + "lib" + File.separator;
            }
        }

        // if STILL not found then just pick DS9097U on 'smartDefaultPort'
        if (ret_str == null) {
            if (propName.equals("onewire.adapter.default"))
                ret_str = "DS9097U";
            else if (propName.equals("onewire.port.default")) {
                try {

                    adapter_class = Class.forName(USerialAdapter.class.getName());
                    adapter_instance = (DSPortAdapter) adapter_class.newInstance();

                    // check if has any ports (common javax.comm problem)
                    if (!DSPortAdapter.getPortNames().isEmpty()) {
                        ret_str = DSPortAdapter.getPortNames().iterator().next();
                    }

                } catch (Throwable t) {
                    // DRAIN
                    logger.fatal("DalSemi ignored this exception", t);
                }
            }
        }

        return ret_str;
    }
}
