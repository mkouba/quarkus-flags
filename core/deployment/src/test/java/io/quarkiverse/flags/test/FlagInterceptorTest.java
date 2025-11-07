package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.Flags;
import io.quarkiverse.flags.InMemoryFlagProvider;
import io.quarkiverse.flags.spi.FlagInterceptor;
import io.quarkiverse.flags.spi.ImmutableBooleanValue;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Uni;

public class FlagInterceptorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot(root -> root.addClasses(FlagInterceptor1.class, FlagInterceptor2.class));

    @Inject
    InMemoryFlagProvider inMemoryFlagProvider;

    @Inject
    Flags flags;

    @Test
    public void testFlags() {
        inMemoryFlagProvider.newFlag("alpha")
                .setEnabled(true)
                .register();
        assertFalse(flags.find("alpha").orElseThrow().computeAndAwait().asBoolean());
    }

    @Priority(10)
    @Singleton
    public static class FlagInterceptor1 implements FlagInterceptor {

        @Override
        public Uni<Value> afterCompute(Flag flag, Value value, ComputationContext computationContext) {
            if (!value.asBoolean()) {
                throw new IllegalStateException();
            }
            return Uni.createFrom().item(value);
        }

    }

    @Priority(5)
    @Singleton
    public static class FlagInterceptor2 implements FlagInterceptor {

        @Override
        public Uni<Value> afterCompute(Flag flag, Value value, ComputationContext computationContext) {
            // just invert the state
            return Uni.createFrom().item(ImmutableBooleanValue.from(!value.asBoolean()));
        }

    }
}
