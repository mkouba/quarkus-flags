package io.quarkiverse.flags;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.NoSuchElementException;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

/**
 * Qualifies an injection point of a feature {@link Flag}.
 * <p>
 * Note that the injected {@link Flag} is never {@code null} but subsequent invocations can throw {@link NoSuchElementException}
 * if no such feature flag exists.
 *
 * @see Flag
 */
@Qualifier
@Retention(RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE })
public @interface Feature {

    /**
     * @see Flag#feature()
     */
    @Nonbinding
    String value();

}
