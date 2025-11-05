package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.flags.Feature;
import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.FlagManager;
import io.quarkus.test.QuarkusUnitTest;

public class ConfigFlagsTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withEmptyApplication()
            .overrideConfigKey("quarkus.flags.buildtime.alpha.enabled", "true")
            .overrideConfigKey("quarkus.flags.buildtime.bravo.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.charlie.enabled", "true")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.delta.enabled", "false");

    @Inject
    FlagManager manager;

    @Feature("alpha")
    Flag alpha;

    @Feature("foo")
    Optional<Flag> foo;

    @Feature("delta")
    Optional<Flag> delta;

    @Test
    public void testFlags() {
        assertEquals(4, manager.getFlags().size());
        assertTrue(manager.getFlag("alpha").orElseThrow().isOn());
        assertFalse(manager.getFlag("bravo").orElseThrow().isOn());
        assertTrue(manager.getFlag("charlie").orElseThrow().isOn());
        assertFalse(manager.getFlag("delta").orElseThrow().isOn());
        assertTrue(alpha.isOn());
        assertTrue(foo.isEmpty());
        assertFalse(delta.orElseThrow().isOn());
    }

}
