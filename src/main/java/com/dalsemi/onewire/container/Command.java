package com.dalsemi.onewire.container;

/**
 * 1-Wire common commands.
 *
 * Some commands are aliases, used in a different way in different contexts. Note:
 *
 * {@link #READ_POWER_SUPPLY} vs. {@link #CONVERT_VOLTAGE}
 * {@link #SELECT_ALL} vs. {@link #SKIP_ROM}
 *
 * @author Copyright &copy; <a href="mailto:vt@homeclimatecontrol.com">Vadim Tkachenko</a> 2021
 */
public enum Command {

    COPY_SCRATCHPAD(0x48),
    WRITE_SCRATCHPAD(0x4E),
    CONVERT_TEMPERATURE(0x44),
    MATCH_ROM(0x55),
    READ_POWER_SUPPLY(0xB4),
    CONVERT_VOLTAGE(0xB4),
    RECALL_E2MEMORY(0xB8),
    READ_SCRATCHPAD(0xBE),
    SELECT_ALL(0xCC),
    SKIP_ROM(0xCC);

    public final byte code;

    Command(int code) {
        this.code = (byte) code;
    }
}
