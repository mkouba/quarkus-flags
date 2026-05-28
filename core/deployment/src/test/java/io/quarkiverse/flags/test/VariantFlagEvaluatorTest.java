package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.flags.CompositeFlagEvaluator;
import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.Flags;
import io.quarkiverse.flags.StringValue;
import io.quarkiverse.flags.VariantFlagEvaluator;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.mutiny.Uni;

public class VariantFlagEvaluatorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot(root -> root.addClasses(UpperCaseEvaluator.class))
            .overrideRuntimeConfigKey("quarkus.flags.runtime.checkout.value", "Buy Now")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.checkout.meta.evaluator", VariantFlagEvaluator.ID)
            .overrideRuntimeConfigKey("quarkus.flags.runtime.checkout.meta.variant-key", "group")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.checkout.meta.default-variant", "control")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.checkout.meta.variant-control", "Buy Now")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.checkout.meta.variant-treatment", "Add to Cart")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.checkout.meta.variant-holiday", "Gift This!")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.composite.value", "true")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.composite.meta.evaluator", CompositeFlagEvaluator.ID)
            .overrideRuntimeConfigKey("quarkus.flags.runtime.composite.meta.sub-evaluators",
                    VariantFlagEvaluator.ID + ", upper-case")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.composite.meta.variant-key", "tier")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.composite.meta.default-variant", "free")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.composite.meta.variant-free", "Basic")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.composite.meta.variant-pro", "Premium");

    @Inject
    Flags flags;

    @Test
    public void testDefaultVariant() {
        assertEquals("Buy Now", flags.getString("checkout"));
    }

    @Test
    public void testContextVariantSelection() {
        Flag flag = flags.findAndAwait("checkout").orElseThrow();
        assertEquals("Add to Cart",
                flag.computeAndAwait(ComputationContext.of("group", "treatment")).asString());
    }

    @Test
    public void testContextVariantSelectionHoliday() {
        Flag flag = flags.findAndAwait("checkout").orElseThrow();
        assertEquals("Gift This!",
                flag.computeAndAwait(ComputationContext.of("group", "holiday")).asString());
    }

    @Test
    public void testUnknownVariantFallsBackToDefault() {
        Flag flag = flags.findAndAwait("checkout").orElseThrow();
        assertEquals("Buy Now",
                flag.computeAndAwait(ComputationContext.of("group", "unknown")).asString());
    }

    @Test
    public void testNoContextFallsBackToDefault() {
        Flag flag = flags.findAndAwait("checkout").orElseThrow();
        assertEquals("Buy Now", flag.computeAndAwait().asString());
    }

    @Test
    public void testCompositeVariantThenUpperCase() {
        Flag flag = flags.findAndAwait("composite").orElseThrow();
        // VariantFlagEvaluator selects "Basic", then UpperCaseEvaluator transforms it
        assertEquals("BASIC", flag.computeAndAwait().asString());
        assertEquals("PREMIUM",
                flag.computeAndAwait(ComputationContext.of("tier", "pro")).asString());
    }

    @Identifier("upper-case")
    @Singleton
    public static class UpperCaseEvaluator implements FlagEvaluator {

        @Override
        public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
            return Uni.createFrom().item(new StringValue(initialValue.asString().toUpperCase()));
        }

    }

}
