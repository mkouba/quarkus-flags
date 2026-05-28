package io.quarkiverse.flags;

import java.util.Map;

import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.mutiny.Uni;

/**
 * Evaluates a flag by selecting a variant based on the {@link ComputationContext}.
 * <p>
 * Variants are defined in {@link Flag#metadata()} with the {@value #VARIANT_PREFIX} key prefix. The variant is selected using a
 * context key specified by the {@value #VARIANT_KEY} metadata. If the context does not contain the key or the value does not
 * match any variant, the {@value #DEFAULT_VARIANT} metadata is used to select the default variant. If no default variant is
 * configured, the initial value is returned.
 * <p>
 * Example configuration:
 *
 * <pre>
 * quarkus.flags.runtime.checkout.value=Buy Now
 * quarkus.flags.runtime.checkout.meta.evaluator=quarkus.variant
 * quarkus.flags.runtime.checkout.meta.variant-control=Buy Now
 * quarkus.flags.runtime.checkout.meta.variant-treatment=Add to Cart
 * quarkus.flags.runtime.checkout.meta.default-variant=control
 * quarkus.flags.runtime.checkout.meta.variant-key=group
 * </pre>
 *
 * With this configuration, {@code ctx.get("group")} is used to look up the variant. If the context value is
 * {@code "treatment"}, the flag evaluates to {@code "Add to Cart"}.
 */
@Identifier(VariantFlagEvaluator.ID)
@Singleton
public class VariantFlagEvaluator implements FlagEvaluator {

    public static final String ID = "quarkus.variant";
    public static final String VARIANT_PREFIX = "variant-";
    public static final String VARIANT_KEY = "variant-key";
    public static final String DEFAULT_VARIANT = "default-variant";

    @Override
    public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
        Map<String, String> metadata = flag.metadata();
        String variantKey = metadata.get(VARIANT_KEY);
        String selectedVariant = null;
        if (variantKey != null && computationContext != null) {
            Object contextValue = computationContext.get(variantKey);
            if (contextValue != null) {
                selectedVariant = contextValue.toString();
            }
        }
        if (selectedVariant == null) {
            selectedVariant = metadata.get(DEFAULT_VARIANT);
        }
        if (selectedVariant != null) {
            String variantValue = metadata.get(VARIANT_PREFIX + selectedVariant);
            if (variantValue != null) {
                return Uni.createFrom().item(new StringValue(variantValue));
            }
        }
        return Uni.createFrom().item(initialValue);
    }

}
