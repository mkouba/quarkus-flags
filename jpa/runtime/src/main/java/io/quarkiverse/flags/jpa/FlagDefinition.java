package io.quarkiverse.flags.jpa;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.quarkiverse.flags.Flag;

/**
 * Indicates that annotated entity class should be used as a feature flag.
 *
 * @see Flag
 * @see FlagFeature
 * @see FlagValue
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface FlagDefinition {

}
