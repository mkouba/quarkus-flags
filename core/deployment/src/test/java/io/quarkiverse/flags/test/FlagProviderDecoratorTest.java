package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Collection;
import java.util.stream.StreamSupport;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flags;
import io.quarkiverse.flags.InMemoryFlagProvider;
import io.quarkiverse.flags.spi.FlagProvider;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Uni;

public class FlagProviderDecoratorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot(root -> root.addClasses(FlagProviderDecorator.class));

    @Inject
    InMemoryFlagProvider inMemoryFlagProvider;

    @Inject
    Flags flags;

    @Test
    public void testFlags() {
        inMemoryFlagProvider.addFlag(Flag.builder("alpha")
                .setEnabled(true)
                .build());
        assertFalse(flags.findAndAwait("alpha").orElseThrow().isEnabled());
    }

    @Priority(5)
    @Decorator
    public static class FlagProviderDecorator implements FlagProvider {

        @Inject
        @Delegate
        FlagProvider delegate;

        @Override
        public Uni<Collection<Flag>> getFlags() {
            return delegate.getFlags().map(f -> {
                return StreamSupport.stream(f.spliterator(), false).<Flag> map(flag -> {
                    return Flag.builder(flag.feature()).setMetadata(flag.metadata()).setEnabled(false).build();
                }).toList();
            });
        }

        @Override
        public int getPriority() {
            return delegate.getPriority();
        }

    }
}
