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
            .overrideConfigKey("flag.alpha", "true")
            .overrideConfigKey("flag.bravo", "0")
            .overrideRuntimeConfigKey("flag.charlie", "true")
            .overrideRuntimeConfigKey("flag.delta", "false");

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
        assertTrue(manager.getFlag("alpha").orElseThrow().computeAndAwait().asBoolean());
        assertFalse(manager.getFlag("bravo").orElseThrow().computeAndAwait().asBoolean());
        assertEquals(0, manager.getFlag("bravo").orElseThrow().computeAndAwait().asInt());
        assertTrue(manager.getFlag("charlie").orElseThrow().computeAndAwait().asBoolean());
        assertFalse(manager.getFlag("delta").orElseThrow().computeAndAwait().asBoolean());
        assertTrue(alpha.computeAndAwait().asBoolean());
        assertTrue(foo.isEmpty());
        assertFalse(delta.orElseThrow().computeAndAwait().asBoolean());
    }

}
