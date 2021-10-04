package com.dalsemi.onewire.utils;

import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer1F;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OWPathTest {

    @Test
    void extend() {

        var adapter = mock(DSPortAdapter.class);
        var original = new OWPath(adapter);
        var originalString = original.toString();

        var s = new OneWireContainer1F(adapter, "1300000000E6B51F");
        var b1 = original.extend(s, 0);
        var b2 = original.extend(s, 1);

        assertThat(original).hasToString(originalString);
        assertThat(b1).hasToString(originalString + "1300000000E6B51F_0/");
        assertThat(b2).hasToString(originalString + "1300000000E6B51F_1/");
    }
}
