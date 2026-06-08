package io.quarkiverse.flags;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a metadata entry to a {@link RegisterFlag}-annotated field or method.
 *
 * @see RegisterFlag
 * @see Flag#metadata()
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(WithMetadata.List.class)
public @interface WithMetadata {

    String key();

    String value();

    @Target({ ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        WithMetadata[] value();
    }
}
