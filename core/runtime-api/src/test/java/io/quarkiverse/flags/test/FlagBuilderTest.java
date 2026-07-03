package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import io.quarkiverse.flags.BooleanValue;
import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.ComputedFlag;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.ImmutableFlag;

public class FlagBuilderTest {

    @Test
    public void testBuilder() {
        assertThrows(IllegalArgumentException.class, () -> Flag.builder(null));
        assertThrows(IllegalArgumentException.class, () -> Flag.builder(""));
        assertThrows(IllegalArgumentException.class, () -> Flag.builder("foo").setMetadata(null));
        assertThrows(IllegalArgumentException.class, () -> Flag.builder("foo").setCompute(null));
        assertThrows(IllegalArgumentException.class, () -> Flag.builder("foo").setComputeAsync(null));
        assertThrows(IllegalStateException.class,
                () -> Flag.builder("foo").setEnabled(true).build(),
                "Origin must be set");

        // Evaluator resolution is lazy - build() succeeds even without the Arc container;
        // the container lookup happens when the flag is actually evaluated
        Flag withEvaluator = Flag.builder("foo").setOrigin("test")
                .setMetadata(Map.of(FlagEvaluator.META_KEY, "bar")).setEnabled(true).build();
        assertTrue(withEvaluator.feature().equals("foo"));

        Flag trueByDefault = Flag.builder("foo").setOrigin("test").build();
        assertTrue(trueByDefault.isEnabled());
    }

    @Test
    public void testSetInt() {
        Flag flag = Flag.builder("intFlag").setOrigin("test").setInt(42).build();
        assertInstanceOf(ImmutableFlag.class, flag);
        assertEquals(42, flag.computeAndAwait().asInt());
        assertEquals("42", flag.getString());
    }

    @Test
    public void testSetDecimal() {
        Flag flag = Flag.builder("decFlag").setOrigin("test").setDecimal(new BigDecimal("3.14")).build();
        assertInstanceOf(ImmutableFlag.class, flag);
        assertEquals(new BigDecimal("3.14"), flag.computeAndAwait().asDecimal());
        assertEquals("3.14", flag.getString());
    }

    @Test
    public void testSetString() {
        Flag flag = Flag.builder("strFlag").setOrigin("test").setString("hello").build();
        assertInstanceOf(ImmutableFlag.class, flag);
        assertEquals("hello", flag.getString());
    }

    @Test
    public void testComputedFlag() {
        Flag flag = Flag.builder("computed").setOrigin("test")
                .setCompute(cc -> BooleanValue.from(true)).build();
        assertInstanceOf(ComputedFlag.class, flag);
        assertTrue(flag.isEnabled());
        assertTrue(flag.toString().contains("computed"));
    }

    @Test
    public void testLastValueSetterWins() {
        Flag flag = Flag.builder("multi").setOrigin("test")
                .setEnabled(true)
                .setInt(42)
                .setString("final")
                .build();
        assertEquals("final", flag.getString());
    }

    @Test
    public void testAccessorDefaults() {
        Flag boolFlag = Flag.builder("bool").setOrigin("test").setEnabled(true).build();
        assertTrue(boolFlag.isEnabled());
        assertTrue(boolFlag.isEnabled(false));
        assertEquals("true", boolFlag.getString());
        assertEquals("true", boolFlag.getString("fallback"));
        assertEquals(1, boolFlag.getInt());
        assertEquals(1, boolFlag.getInt(99));
        assertEquals(BigDecimal.ONE, boolFlag.getDecimal());
        assertEquals(BigDecimal.ONE, boolFlag.getDecimal(BigDecimal.ZERO));

        Flag strFlag = Flag.builder("str").setOrigin("test").setString("hello").build();
        assertEquals("hello", strFlag.getString("fallback"));
        // "hello" cannot be converted to boolean/int/decimal, defaults returned
        assertTrue(strFlag.isEnabled(true));
        assertFalse(strFlag.isEnabled(false));
        assertEquals(42, strFlag.getInt(42));
        assertEquals(BigDecimal.TEN, strFlag.getDecimal(BigDecimal.TEN));
        assertThrows(NoSuchElementException.class, strFlag::isEnabled);
        assertThrows(NoSuchElementException.class, strFlag::getInt);
        assertThrows(NoSuchElementException.class, strFlag::getDecimal);

        Flag intFlag = Flag.builder("int").setOrigin("test").setInt(42).build();
        assertEquals(42, intFlag.getInt(0));
        assertEquals("42", intFlag.getString("fallback"));
        assertEquals(BigDecimal.valueOf(42), intFlag.getDecimal(BigDecimal.ZERO));
        // 42 cannot be converted to boolean, default returned
        assertTrue(intFlag.isEnabled(true));
        assertFalse(intFlag.isEnabled(false));

        Flag decFlag = Flag.builder("dec").setOrigin("test").setDecimal(new BigDecimal("3.14")).build();
        assertEquals(new BigDecimal("3.14"), decFlag.getDecimal(BigDecimal.ZERO));
        assertEquals("3.14", decFlag.getString("fallback"));
        // 3.14 cannot be converted to boolean or int, defaults returned
        assertTrue(decFlag.isEnabled(true));
        assertEquals(99, decFlag.getInt(99));
    }

}
