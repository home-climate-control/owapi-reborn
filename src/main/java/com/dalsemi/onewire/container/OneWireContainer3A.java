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


/**
 * <P> 1-Wire&#174 container for a Single Addressable Switch, DS2413.  This container
 * encapsulates the functionality of the 1-Wire family type <B>3A</B> (hex)</P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> Eight channels of programmable I/O with open-drain outputs
 *   <LI> Logic level sensing of the PIO pin can be sensed
 *   <LI> Multiple DS2413's can be identified on a common 1-Wire bus and operated
 *        independently.
 *   <LI> Supports 1-Wire Conditional Search command with response controlled by
 *        programmable PIO conditions
 *   <LI> Supports Overdrive mode which boosts communication speed up to 142k bits
 *        per second.
 * </UL>
 *
 * <H3> Usage </H3>
 *
 *
 * @see OneWireSensor
 * @see SwitchContainer
 * @see OneWireContainer
 *
 * @author     JPE
 * @author Stability enhancements &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2001-2021
 */
public class OneWireContainer3A extends OneWireContainer implements SwitchContainer {

   /**
    * Status memory bank of the DS2413 for memory map registers
    */
   private MemoryBankEEPROMstatus map;

   /**
    * Status memory bank of the DS2413 for the conditional search
    */
   private MemoryBankEEPROMstatus search;

   /**
    * PIO Access read command
    */
   public static final byte PIO_ACCESS_READ = ( byte ) 0xF5;

   /**
    * PIO Access read command
    */
   public static final byte PIO_ACCESS_WRITE = ( byte ) 0x5A;

   /**
    * Used for 0xFF array
    */
   private byte[] FF = new byte [8];


   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2413.
    * Note that the method <code>setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[])</code>
    * must be called to set the correct <code>DSPortAdapter</code> device address.
    *
    * @see com.dalsemi.onewire.container.OneWireContainer#setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) setupContainer(DSPortAdapter,byte[])
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer3A(DSPortAdapter,byte[])
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer3A(DSPortAdapter,long)
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer3A(DSPortAdapter,String)
    */
   public OneWireContainer3A ()
   {
      super();

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2413.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2413
    *
    * @see #OneWireContainer3A()
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer3A(DSPortAdapter,long)
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer3A(DSPortAdapter,String)
    */
   public OneWireContainer3A (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2413.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2413
    *
    * @see #OneWireContainer3A()
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer3A(DSPortAdapter,byte[])
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer3A(DSPortAdapter,String)
    */
   public OneWireContainer3A (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2413.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2413
    *
    * @see #OneWireContainer3A()
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer3A(DSPortAdapter,byte[])
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer3A(DSPortAdapter,long)
    */
   public OneWireContainer3A (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
   }

   @Override
   public String getName ()
   {
      return "DS2413";
   }


   @Override
   public String getAlternateNames ()
   {
      return "Dual Channel Switch";
   }

   @Override
   public String getDescription ()
   {
      return "Dual Channel Addressable Switch";
   }

   @Override
   public DSPortAdapter.Speed getMaxSpeed () {
      return DSPortAdapter.Speed.OVERDRIVE;
   }

   @Override
   public int getNumberChannels (byte[] state)
   {
      return 2;
   }

   @Override
   public boolean isHighSideSwitch ()
   {
      return false;
   }

   @Override
   public boolean hasActivitySensing ()
   {
      return false;
   }

   @Override
   public boolean hasLevelSensing ()
   {
      return true;
   }

   @Override
   public boolean hasSmartOn ()
   {
      return false;
   }

   @Override
   public boolean onlySingleChannelOn ()
   {
      return false;
   }

   @Override
   public boolean getLevel (int channel, byte[] state) {
      byte  level = (byte) (0x01 << (channel*2));
      return ((state[1] & level) == level);
   }

   @Override
   public boolean getLatchState (int channel, byte[] state) {
      byte latch = (byte) (0x01 << ((channel*2)+1));
      return ((state [1] & latch) == latch);
   }

   @Override
   public boolean getSensedActivity (int channel, byte[] state) {
      return false;
   }

   @Override
   public void clearActivity () {
      // VT: NOTE: Do nothing???
   }

   @Override
   public void setLatchState (int channel, boolean latchState,
                              boolean doSmart, byte[] state) {
      byte latch = (byte) (0x01 << channel);
      byte temp;

      state[0] = (byte) 0x00FC;

      if(getLatchState(0,state))
      {
         temp = (byte) 0x01;
         state[0] = (byte) ((state[0]) | temp);
      }

      if(getLatchState(1,state))
      {
         temp = (byte) 0x02;
         state[0] = (byte) ((state[0]) | temp);
      }

      if (latchState)
         state[0] = (byte) (state[0] | latch);
      else
         state[0] = (byte) (state[0] & ~latch);
   }

   /**
    * Sets the latch state for all of the channels.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.
    *
    * @param set the state to set all of the channels, in the range [0 to (<code>getNumberChannels(byte[])</code> - 1)]
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see #getLatchState(int,byte[])
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    */
   public void setLatchState (byte set, byte[] state)
   {
      state[0] = set;
   }

   /**
    * Retrieves the 1-Wire device sensor state.  This state is
    * returned as a byte array.  Pass this byte array to the 'get'
    * and 'set' methods.  If the device state needs to be changed then call
    * the 'writeDevice' to finalize the changes.
    *
    * @return 1-Wire device sensor state
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   @Override
   public byte[] readDevice () throws  OneWireException
   {
      byte[] buff = new byte [2];

      buff[0] = (byte) 0xF5;  // PIO Access Read Command
      buff[1] = (byte) 0xFF;  // Used to read the PIO Status Bit Assignment

      // select the device
      if (adapter.select(address))
      {
         adapter.dataBlock(buff, 0, 2);
      }
      else
         throw new OneWireIOException("Device select failed");

      return buff;
   }

   /**
    * Retrieves the 1-Wire device register mask.  This register is
    * returned as a byte array.  Pass this byte array to the 'get'
    * and 'set' methods.  If the device register mask needs to be changed then call
    * the 'writeRegister' to finalize the changes.
    *
    * @return 1-Wire device register mask
    *
    */
   public byte[] readRegister () {
      return new byte[3];
   }

   /**
    * Writes the 1-Wire device sensor state that
    * have been changed by 'set' methods.  Only the state registers that
    * changed are updated.  This is done by referencing a field information
    * appended to the state data.
    *
    * @param  state 1-Wire device PIO access write (x x x x x x PIOB PIOA)
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   @Override
   public void writeDevice (byte[] state) throws  OneWireException {
      var buff = new byte [5];

      buff[0] = (byte) 0x5A;      // PIO Access Write Command
      buff[1] = state[0];  // Channel write information
      buff[2] = (byte) ~state[0]; // Inverted write byte
      buff[3] = (byte) 0xFF;      // Confirmation Byte
      buff[4] = (byte) 0xFF;      // PIO Pin Status

      // select the device
      if (adapter.select(address))
      {
         adapter.dataBlock(buff, 0, 5);
      }
      else
         throw new OneWireIOException("Device select failed");

      if(buff[3] != (byte) 0x00AA)
      {
         throw new OneWireIOException("Failure to change latch state.");
      }
   }

   /**
    * Writes the 1-Wire device register mask that
    * have been changed by 'set' methods.
    *
    * @param  register 1-Wire device sensor state
    *
    */
   public void writeRegister (byte[] register) {
      // VT: NOTE: Do nothing???
   }

  /** Need to add the following to overide to an an abstract something not being overidden in ./OneWireSensor.java
  *   Note to MW and VT: I've got no idea what the implications of this dirty hack is
  */
	 @Override
	public void readDevice(byte[] outputBuffer) throws OneWireException {
	// read the status byte

         // VT: Huh???

  // These following 3 lines had to be removed from the snip borrowed from line 325-333 of ./OneWireContainer1F.java as they break something
  // and are specific to the 1F chip

  /*     byte[] outputBuffer = new byte[4];
	readDevice(outputBuffer);
  	return outputBuffer;
  */
  }
}
