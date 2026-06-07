package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.NoSuchElementException;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.flags.Feature;
import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.InMemoryFlagProvider;
import io.quarkus.test.QuarkusUnitTest;

public class InjectedFlagTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withEmptyApplication();

    @Inject
    InMemoryFlagProvider inMemoryFlagProvider;

    @Inject
    @Feature("alpha")
    Flag alpha;

    @Inject
    @Feature
    Flag bravo;

    @Test
    public void testFlags() {
        assertNotNull(alpha);
        assertThrows(NoSuchElementException.class, () -> alpha.isEnabled());
        inMemoryFlagProvider.addFlag(Flag.builder("alpha").setEnabled(true));
        assertTrue(alpha.isEnabled());
        assertNull(alpha.metadata().get("foo"));
        inMemoryFlagProvider.removeFlag("alpha");
        assertThrows(NoSuchElementException.class, () -> alpha.origin());
        inMemoryFlagProvider.addFlag(Flag.builder("alpha").setEnabled(false).setMetadata(Map.of("foo", "bar")));
        assertFalse(alpha.isEnabled());
        assertEquals("bar", alpha.metadata().get("foo"));
    }

    @Test
    public void testDefaultFeatureName() {
        assertNotNull(bravo);
        assertEquals("bravo", bravo.feature());
        assertThrows(NoSuchElementException.class, () -> bravo.isEnabled());
        inMemoryFlagProvider.addFlag(Flag.builder("bravo").setEnabled(true));
        assertTrue(bravo.isEnabled());
    }

}
