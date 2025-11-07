package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.flags.Feature;
import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.FlagManager;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.ImmutableBooleanValue;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Uni;

public class ConfigFlagsTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot(root -> {
                root.addClass(DeltaEvaluator.class)
                        .addAsResource(new StringAsset("""
                                quarkus.flags.build.alpha.value=true
                                quarkus.flags.build.bravo.value=0
                                """), "application.properties");
            })
            .overrideRuntimeConfigKey("quarkus.flags.runtime.charlie.value", "true")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.delta.value", "true")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.delta.meta.evaluator", "deltaEval")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.delta.meta.usernames", "foo,bar,baz");

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
        Set<Flag> flags = manager.getFlags();
        assertEquals(4, flags.size(), flags.toString());
        assertTrue(manager.getFlag("alpha").orElseThrow().computeAndAwait().asBoolean());
        assertFalse(manager.getFlag("bravo").orElseThrow().computeAndAwait().asBoolean());
        assertEquals(0, manager.getFlag("bravo").orElseThrow().computeAndAwait().asInt());
        assertTrue(manager.getFlag("charlie").orElseThrow().computeAndAwait().asBoolean());
        assertFalse(manager.getFlag("delta").orElseThrow().computeAndAwait().asBoolean());
        assertTrue(alpha.computeAndAwait().asBoolean());
        assertTrue(foo.isEmpty());

        Flag deltaFlag = delta.orElseThrow();
        assertEquals("deltaEval", deltaFlag.metadata().get(FlagEvaluator.METADATA_KEY));
        assertTrue(deltaFlag.computeAndAwait(Flag.ComputationContext.of("username", "foo")).asBoolean());
        assertFalse(deltaFlag.computeAndAwait(Flag.ComputationContext.of("username", "qux")).asBoolean());
    }

    @Singleton
    public static class DeltaEvaluator implements FlagEvaluator {

        @Override
        public String id() {
            return "deltaEval";
        }

        @Override
        public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
            if (!initialValue.asBoolean()) {
                throw new IllegalStateException();
            }
            String username = computationContext.get("username");
            if (username == null) {
                return Uni.createFrom().item(ImmutableBooleanValue.from(!initialValue.asBoolean()));
            }
            String[] usernames = flag.metadata().get("usernames").split(",");
            String match = Arrays.stream(usernames).filter(u -> username.equals(u)).findFirst().orElse(null);
            return Uni.createFrom().item(ImmutableBooleanValue.from(match != null));
        }

    }

}
