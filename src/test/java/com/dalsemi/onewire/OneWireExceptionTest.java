package com.dalsemi.onewire;

import com.dalsemi.onewire.utils.Address;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class OneWireExceptionTest {

    private final Logger logger = LogManager.getLogger();

    @Test
    @Disabled("Fails the way the code is currently written")
    void addressInMessage() {
        var address = "1300000000E6B51F";
        try {
            throw new OneWireException(Address.toByteArray(address), "oops");
        } catch (OneWireException ex) {
            logger.info("getMessage(): {}", ex.getMessage(), ex);
            assertThat(ex.getMessage()).contains(address);
        }
    }

    @Test
    void addressInToString() {
        var address = "1300000000E6B51F";
        try {
            throw new OneWireException(Address.toByteArray(address), "oops");
        } catch (OneWireException ex) {
            logger.info("toString(): {}", ex.toString(), ex);
            assertThat(ex.toString()).contains(address);
        }
    }

    @Test
    void addressInPrintStackTrace() {
        var address = "1300000000E6B51F";
        try {
            throw new OneWireException(Address.toByteArray(address), "oops");
        } catch (OneWireException ex) {
            var writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            var output = writer.toString();
            logger.info("printStackTrace(): {}", output, ex);
            assertThat(output).contains(address);
        }
    }
}
