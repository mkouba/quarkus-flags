package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.FlagAdded;
import io.quarkiverse.flags.FlagManager;
import io.quarkiverse.flags.FlagRemoved;
import io.quarkiverse.flags.InMemoryFlagProvider;
import io.quarkiverse.flags.spi.ImmutableBooleanValue;
import io.quarkiverse.flags.spi.ImmutableStringValue;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Uni;

public class InMemoryFlagsTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot(root -> root.addClasses(FlagObservers.class));

    @Inject
    InMemoryFlagProvider inMemoryFlagProvider;

    @Inject
    FlagManager manager;

    @Inject
    FlagObservers flagObservers;

    @Test
    public void testFlags() {
        assertEquals(0, manager.getFlags().size());
        assertEquals(0, flagObservers.added.size());
        assertEquals(0, flagObservers.removed.size());

        inMemoryFlagProvider.newFlag("alpha")
                .setEnabled(true)
                .add();
        inMemoryFlagProvider.newFlag("bravo")
                .setEnabled(false)
                .add();
        inMemoryFlagProvider.newFlag("charlie")
                .setCompute(cc -> ImmutableBooleanValue.TRUE)
                .add();
        inMemoryFlagProvider.newFlag("delta")
                .setComputeAsync(cc -> Uni.createFrom().item(new ImmutableStringValue("no")))
                .add();
        assertEquals(4, flagObservers.added.size());
        assertEquals(0, flagObservers.removed.size());

        Flag.Value alphaValue = manager.getFlag("alpha").orElseThrow().computeAndAwait();
        assertTrue(alphaValue.asBoolean());
        assertEquals("true", alphaValue.asString());
        assertEquals(1, alphaValue.asInt());

        assertFalse(manager.getFlag("bravo").orElseThrow().computeAndAwait().asBoolean());
        assertTrue(manager.getFlag("charlie").orElseThrow().computeAndAwait().asBoolean());

        Flag.Value deltaValue = manager.getFlag("delta").orElseThrow().computeAndAwait();
        assertFalse(deltaValue.asBoolean());
        assertEquals("no", deltaValue.asString());
        assertThrows(NoSuchElementException.class, () -> deltaValue.asInt());

        manager.getFlags().forEach(f -> inMemoryFlagProvider.removeFlag(f.feature()));
        assertEquals(0, manager.getFlags().size());
        assertEquals(4, flagObservers.added.size());
        assertEquals(4, flagObservers.removed.size());
    }

    @Singleton
    public static class FlagObservers {

        final List<Flag> added = new CopyOnWriteArrayList<>();
        final List<Flag> removed = new CopyOnWriteArrayList<>();

        void flagAdded(@Observes FlagAdded flagAdded) {
            added.add(flagAdded.flag());
        }

        void flagRemoved(@Observes FlagRemoved flagRemoved) {
            removed.add(flagRemoved.flag());
        }

    }

}
