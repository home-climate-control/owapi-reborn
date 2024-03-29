
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

import com.dalsemi.onewire.container.SwitchContainer;


/**
 * 1-Wire&#174 Network path element.  Instances of this class are
 * used to represent a single branch of a complex 1-Wire network.
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> <H4> Example</H4>
 * Iterate through the 1-wire path elements in the 1-Wire path 'path' and print information:
 * <PRE> <CODE>
 *   OWPathElement path_element;
 *
 *   // Iterate through the path elements
 *   for (Iterator path_enum = path.getAllOWPathElements().iterator();
 *           path_enum.hasNext(); )
 *   {
 *
 *      // cast the enum as a OWPathElement
 *      path_element = (OWPathElement)path_enum.next();
 *
 *      // print info
 *      System.out.println("Address: " + path_element.getContainer().getAddressAsString());
 *      System.out.println("Channel number: " + path_element.getChannel());
 *   }
 * </CODE> </PRE>
 * </DL>
 *
 * @see OWPath
 * @see com.dalsemi.onewire.container.OneWireContainer
 *
 * @author DS
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 */
public class OWPathElement {

    /**
     * Path element switch.
     */
    public final SwitchContainer container;

    /**
     * Switch channel the path is on.
     */
    public final int channel;

    /**
     * Create a new 1-Wire path element.
     *
     * @param container Switch that is the path element.
     * @param channel Channel number of the 1-Wire path.
     */
    public OWPathElement(SwitchContainer container, int channel) {
        this.container = container;
        this.channel = channel;
    }
}
