package com.dalsemi.onewire.container;

/**
 * 1-Wire common commands.
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 */
public enum Command {

    WRITE_SCRATCHPAD(0x4E),
    READ_SCRATCHPAD(0xBE),
    COPY_SCRATCHPAD(0x48),
    CONVERT_TEMPERATURE(0x44),
    RECALL_E2MEMORY(0xB8),
    READ_POWER_SUPPLY(0xB4),
    CONVERT_VOLTAGE(0xB4);

    public final byte code;

    Command(int code) {
        this.code = (byte) code;
    }
}
