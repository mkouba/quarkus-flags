package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.FlagEvaluator;

public class FlagBuilderTest {

    @Test
    public void testBuilder() {
        assertThrows(IllegalArgumentException.class, () -> Flag.builder(null));
        assertThrows(IllegalArgumentException.class, () -> Flag.builder(""));
        assertThrows(IllegalArgumentException.class, () -> Flag.builder("foo").setMetadata(null));
        IllegalStateException containerNotFound = assertThrows(IllegalStateException.class,
                () -> Flag.builder("foo").setMetadata(Map.of(FlagEvaluator.META_KEY, "bar")).setEnabled(true).build());
        assertTrue(containerNotFound.getMessage().startsWith("Unable to find the ArC container"));

        Flag trueByDefault = Flag.builder("foo").build();
        assertTrue(trueByDefault.isEnabled());
    }

}
