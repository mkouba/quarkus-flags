package io.quarkiverse.flags;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the {@link io.quarkiverse.flags.spi.FlagEvaluator} to use for a {@link RegisterFlag}-annotated field or method.
 * <p>
 * This is a convenience annotation equivalent to
 * {@code @WithMetadata(key = FlagEvaluator.META_KEY, value = "...")} but with a simpler syntax.
 *
 * @see RegisterFlag
 * @see io.quarkiverse.flags.spi.FlagEvaluator
 * @see WithMetadata
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface WithEvaluator {

    /**
     * The identifier of a {@link io.quarkiverse.flags.spi.FlagEvaluator}.
     *
     * @return the evaluator identifier
     * @see io.quarkiverse.flags.spi.FlagEvaluator#META_KEY
     */
    String value();
}
