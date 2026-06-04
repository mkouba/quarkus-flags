package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import io.quarkiverse.flags.BigDecimalValue;
import io.quarkiverse.flags.BooleanValue;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.IntValue;
import io.quarkiverse.flags.StringValue;

public class ImmutableValuesTest {

    @Test
    public void testBoolean() {
        Value yes = BooleanValue.from(true);
        assertEquals(1, yes.asInt());
        assertEquals("true", yes.asString());
        assertTrue(yes.asBoolean());
        Value no = BooleanValue.from(false);
        assertEquals(0, no.asInt());
        assertEquals("false", no.asString());
        assertFalse(no.asBoolean());
    }

    @Test
    public void testInt() {
        Value zero = new IntValue(0);
        assertEquals(0, zero.asInt());
        assertEquals("0", zero.asString());
        assertFalse(zero.asBoolean());
        Value one = new IntValue(1);
        assertEquals(1, one.asInt());
        assertEquals("1", one.asString());
        assertTrue(one.asBoolean());
        Value fortyTwo = new IntValue(42);
        NoSuchElementException e = assertThrows(NoSuchElementException.class, () -> fortyTwo.asBoolean());
        assertTrue(e.getMessage().contains("42"));
    }

    @Test
    public void testString() {
        Value foo = new StringValue("foo");
        assertEquals("foo", foo.asString());

        NoSuchElementException intEx = assertThrows(NoSuchElementException.class, () -> foo.asInt());
        assertTrue(intEx.getMessage().contains("foo"));
        assertInstanceOf(NumberFormatException.class, intEx.getCause());

        NoSuchElementException boolEx = assertThrows(NoSuchElementException.class, () -> foo.asBoolean());
        assertTrue(boolEx.getMessage().contains("foo"));

        NoSuchElementException decEx = assertThrows(NoSuchElementException.class, () -> foo.asDecimal());
        assertTrue(decEx.getMessage().contains("foo"));
        assertInstanceOf(NumberFormatException.class, decEx.getCause());

        Value yes = new StringValue("true");
        assertThrows(NoSuchElementException.class, () -> yes.asInt());
        assertEquals("true", yes.asString());
        assertTrue(yes.asBoolean());
        assertTrue(new StringValue("TRUE").asBoolean());
        assertTrue(new StringValue("True").asBoolean());
        assertTrue(new StringValue("1").asBoolean());
        Value no = new StringValue("false");
        assertFalse(no.asBoolean());
        assertFalse(new StringValue("FALSE").asBoolean());
        assertFalse(new StringValue("0").asBoolean());
        Value number = new StringValue("42");
        assertEquals(42, number.asInt());
        assertEquals("42", number.asString());
        assertThrows(NoSuchElementException.class, () -> number.asBoolean());
    }

    @Test
    public void testBigDecimal() {
        Value ten = new BigDecimalValue(BigDecimal.TEN);
        assertEquals(10, ten.asInt());
        assertEquals("10", ten.asString());
        NoSuchElementException e = assertThrows(NoSuchElementException.class, () -> ten.asBoolean());
        assertTrue(e.getMessage().contains("10"));
        Value one = new BigDecimalValue(BigDecimal.ONE);
        assertTrue(one.asBoolean());
        Value zero = new BigDecimalValue(BigDecimal.ZERO);
        assertFalse(zero.asBoolean());
        assertTrue(new BigDecimalValue(new BigDecimal("1.0")).asBoolean());
    }

    @Test
    public void testEqualsAndHashCode() {
        // StringValue
        assertEquals(new StringValue("foo"), new StringValue("foo"));
        assertEquals(new StringValue("foo").hashCode(), new StringValue("foo").hashCode());
        assertNotEquals(new StringValue("foo"), new StringValue("bar"));

        // IntValue
        assertEquals(new IntValue(42), new IntValue(42));
        assertEquals(new IntValue(42).hashCode(), new IntValue(42).hashCode());
        assertNotEquals(new IntValue(1), new IntValue(2));

        // BigDecimalValue - compareTo-based equality
        assertEquals(new BigDecimalValue(new BigDecimal("1.0")), new BigDecimalValue(new BigDecimal("1.00")));
        assertNotEquals(new BigDecimalValue(BigDecimal.ONE), new BigDecimalValue(BigDecimal.TEN));

        // BooleanValue singletons
        assertEquals(BooleanValue.from(true), BooleanValue.from(true));
        assertEquals(BooleanValue.from(false), BooleanValue.from(false));

        // Cross-type
        assertNotEquals(new IntValue(1), new StringValue("1"));
    }

    @Test
    public void testToString() {
        assertEquals("true", BooleanValue.from(true).toString());
        assertEquals("false", BooleanValue.from(false).toString());
        assertEquals("foo", new StringValue("foo").toString());
        assertEquals("42", new IntValue(42).toString());
        assertEquals("3.14", new BigDecimalValue(new BigDecimal("3.14")).toString());
    }

    @Test
    public void testDefaultValues() {
        Value foo = new StringValue("foo");
        // conversion fails, default returned
        assertFalse(foo.asBoolean(false));
        assertTrue(foo.asBoolean(true));
        assertEquals(99, foo.asInt(99));
        assertEquals(BigDecimal.TEN, foo.asDecimal(BigDecimal.TEN));
        // conversion succeeds, actual value returned
        assertEquals("foo", foo.asString("bar"));

        Value one = new IntValue(1);
        assertTrue(one.asBoolean(false));
        assertEquals(1, one.asInt(99));
    }

}
