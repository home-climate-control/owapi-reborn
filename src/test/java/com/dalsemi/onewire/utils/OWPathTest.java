package com.dalsemi.onewire.utils;

import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer1F;
import com.dalsemi.onewire.container.SwitchContainer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
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

    @Test
    void parentOf() {

        var adapter = mock(DSPortAdapter.class);
        var switchA = mock(SwitchContainer.class);
        var switchB = mock(SwitchContainer.class);
        var switchC = mock(SwitchContainer.class);

        var parent = new OWPath(adapter);

        var pathA0 = parent.extend(switchA, 0);
        var pathA0B1 = pathA0.extend(switchB, 1);
        var pathA1 = parent.extend(switchA, 1);
        var pathA1C0 = pathA1.extend(switchC, 0);

        assertThat(parent.isParentOf(parent)).isFalse();

        assertThat(parent.isParentOf(pathA0)).isTrue();
        assertThat(parent.isParentOf(pathA0B1)).isTrue();
        assertThat(parent.isParentOf(pathA1)).isTrue();
        assertThat(parent.isParentOf(pathA1C0)).isTrue();

        assertThat(pathA0.isParentOf(pathA1)).isFalse();
        assertThat(pathA1.isParentOf(pathA0)).isFalse();

        assertThat(pathA0.isParentOf(pathA1C0)).isFalse();
        assertThat(pathA1.isParentOf(pathA0B1)).isFalse();
    }

    @Test
    void commonParentNull() {

        var p = new OWPath(mock(DSPortAdapter.class));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> p.getCommonParent(null))
                .withMessage("other path is null");
    }

    @Test
    void commonParentDifferentAdapter() {

        var p0 = new OWPath(mock(DSPortAdapter.class));
        var p1 = new OWPath(mock(DSPortAdapter.class));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> p0.getCommonParent(p1))
                .withMessage("null_null/is on a different adapter");
    }

    @Test
    void commonParent() {

        var adapter = mock(DSPortAdapter.class);
        var switchA = new OneWireContainer1F(adapter, "4C000000086BE81F");
        var switchB = new OneWireContainer1F(adapter, "05000000086B731F");
        var switchC = new OneWireContainer1F(adapter, "6A000000086BF41F");

        var parent = new OWPath(adapter);

        var pathA0 = parent.extend(switchA, 0);
        var pathA0B0 = pathA0.extend(switchB, 0);
        var pathA0B1 = pathA0.extend(switchB, 1);
        var pathA1 = parent.extend(switchA, 1);
        var pathA1C0 = pathA1.extend(switchC, 0);

        assertThat(pathA0B1.getCommonParent(pathA1C0)).isEqualTo(parent);
        assertThat(pathA0B0.getCommonParent(pathA0B1)).isEqualTo(pathA0);
        assertThat(pathA0B0.getCommonParent(pathA0)).isEqualTo(pathA0);
        assertThat(pathA0.getCommonParent(pathA0B0)).isEqualTo(pathA0);
    }
}
