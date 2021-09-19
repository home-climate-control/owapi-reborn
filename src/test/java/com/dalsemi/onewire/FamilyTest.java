package com.dalsemi.onewire;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyTest {

    @Test
    void match() {

        for (var family : Family.values()) {
            assertThat(family.toString().toLowerCase()).isEqualTo(String.format("f%02x", family.code));
        }
    }
}
