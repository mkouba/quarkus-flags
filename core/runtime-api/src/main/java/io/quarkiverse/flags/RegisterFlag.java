package io.quarkiverse.flags;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a feature flag from a static field or method.
 * <p>
 * For fields, the field value is used as the initial compute result. For methods, the method body is the compute function.
 * Access sites (field reads, method calls) are transformed at build time so that each access computes and returns the current
 * flag value.
 * <p>
 * Supported types: {@code boolean}, {@code Boolean}, {@code int}, {@code Integer}, {@code String},
 * {@code java.math.BigDecimal}, and {@link Flag.Value}. Methods may optionally accept a single
 * {@link Flag.ComputationContext} parameter.
 * <p>
 * Fields must be {@code volatile} or {@code final} to ensure thread-safe visibility.
 *
 * @see WithMetadata
 * @see Flag#get(String)
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterFlag {

    /**
     * Constant indicating that the flag name should be derived from the annotated element name (field or method name).
     */
    String ELEMENT_NAME = "<<element name>>";

    /**
     * Constant indicating that the flag name should be the fully qualified class name of the declaring class followed by "."
     * and the element name, e.g. {@code com.example.MyFlags.alpha}.
     */
    String FQCN_ELEMENT_NAME = "<<fqcn element name>>";

    /**
     * The feature flag name. Defaults to the annotated element name.
     *
     * @see #ELEMENT_NAME
     * @see #FQCN_ELEMENT_NAME
     */
    String name() default ELEMENT_NAME;

    /**
     * The identifier of a {@link io.quarkiverse.flags.spi.FlagEvaluator} to use. If set, the compute result is passed as the
     * initial value to the evaluator.
     *
     * @see io.quarkiverse.flags.spi.FlagEvaluator#META_KEY
     */
    String evaluator() default "";
}
