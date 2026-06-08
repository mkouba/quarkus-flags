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
        assertThrows(IllegalArgumentException.class, () -> Flag.builder("foo").setCompute(null));
        assertThrows(IllegalArgumentException.class, () -> Flag.builder("foo").setComputeAsync(null));
        assertThrows(IllegalStateException.class,
                () -> Flag.builder("foo").setEnabled(true).build(),
                "Origin must be set");

        // Evaluator resolution is lazy - build() succeeds even without the Arc container;
        // the container lookup happens when the flag is actually evaluated
        Flag withEvaluator = Flag.builder("foo").setOrigin("test")
                .setMetadata(Map.of(FlagEvaluator.META_KEY, "bar")).setEnabled(true).build();
        assertTrue(withEvaluator.feature().equals("foo"));

        Flag trueByDefault = Flag.builder("foo").setOrigin("test").build();
        assertTrue(trueByDefault.isEnabled());
    }

}
