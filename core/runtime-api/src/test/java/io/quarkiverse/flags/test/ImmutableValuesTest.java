package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        assertEquals(BigDecimal.ONE, yes.asDecimal());
        Value no = BooleanValue.from(false);
        assertEquals(0, no.asInt());
        assertEquals("false", no.asString());
        assertFalse(no.asBoolean());
        assertEquals(BigDecimal.ZERO, no.asDecimal());
    }

    @Test
    public void testInt() {
        Value zero = new IntValue(0);
        assertEquals(0, zero.asInt());
        assertEquals("0", zero.asString());
        assertFalse(zero.asBoolean());
        assertEquals(BigDecimal.valueOf(0), zero.asDecimal());
        Value one = new IntValue(1);
        assertEquals(1, one.asInt());
        assertEquals("1", one.asString());
        assertTrue(one.asBoolean());
        assertEquals(BigDecimal.valueOf(1), one.asDecimal());
        Value fortyTwo = new IntValue(42);
        assertEquals(BigDecimal.valueOf(42), fortyTwo.asDecimal());
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
        assertEquals(new BigDecimal("42"), number.asDecimal());
        assertThrows(NoSuchElementException.class, () -> number.asBoolean());

        Value decimalString = new StringValue("3.14");
        assertEquals(new BigDecimal("3.14"), decimalString.asDecimal());
    }

    @Test
    public void testBigDecimal() {
        Value ten = new BigDecimalValue(BigDecimal.TEN);
        assertEquals(10, ten.asInt());
        assertEquals("10", ten.asString());
        assertEquals(BigDecimal.TEN, ten.asDecimal());
        NoSuchElementException e = assertThrows(NoSuchElementException.class, () -> ten.asBoolean());
        assertTrue(e.getMessage().contains("10"));
        Value one = new BigDecimalValue(BigDecimal.ONE);
        assertTrue(one.asBoolean());
        Value zero = new BigDecimalValue(BigDecimal.ZERO);
        assertFalse(zero.asBoolean());
        assertFalse(new BigDecimalValue(new BigDecimal("0.0")).asBoolean());
        assertTrue(new BigDecimalValue(new BigDecimal("1.0")).asBoolean());

        // asInt() rejects fractional and out-of-range values
        NoSuchElementException fractionalEx = assertThrows(NoSuchElementException.class,
                () -> new BigDecimalValue(new BigDecimal("1.5")).asInt());
        assertTrue(fractionalEx.getMessage().contains("1.5"));
        assertInstanceOf(ArithmeticException.class, fractionalEx.getCause());
        NoSuchElementException overflowEx = assertThrows(NoSuchElementException.class,
                () -> new BigDecimalValue(new BigDecimal("99999999999")).asInt());
        assertTrue(overflowEx.getMessage().contains("99999999999"));
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

        // BigDecimalValue - compareTo-based equality and consistent hashCode
        assertEquals(new BigDecimalValue(new BigDecimal("1.0")), new BigDecimalValue(new BigDecimal("1.00")));
        assertEquals(new BigDecimalValue(new BigDecimal("1.0")).hashCode(),
                new BigDecimalValue(new BigDecimal("1.00")).hashCode());
        assertNotEquals(new BigDecimalValue(BigDecimal.ONE), new BigDecimalValue(BigDecimal.TEN));

        // BooleanValue singletons
        assertEquals(BooleanValue.from(true), BooleanValue.from(true));
        assertEquals(BooleanValue.from(false), BooleanValue.from(false));
        assertNotEquals(BooleanValue.TRUE, BooleanValue.FALSE);
        assertNotEquals(BooleanValue.FALSE, BooleanValue.TRUE);
        assertNotEquals(BooleanValue.TRUE, null);
        assertNotEquals(BooleanValue.TRUE, "true");
        assertNotEquals(BooleanValue.FALSE, new IntValue(0));

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
    public void testCreateUni() {
        assertSame(BooleanValue.TRUE, BooleanValue.createUni(true).await().indefinitely());
        assertSame(BooleanValue.FALSE, BooleanValue.createUni(false).await().indefinitely());
        assertEquals(new StringValue("hello"), StringValue.createUni("hello").await().indefinitely());
        assertEquals(new IntValue(42), IntValue.createUni(42).await().indefinitely());
        assertEquals(new BigDecimalValue(BigDecimal.TEN), BigDecimalValue.createUni(BigDecimal.TEN).await().indefinitely());
    }

    @Test
    public void testNullConstructorArgs() {
        assertThrows(NullPointerException.class, () -> new BigDecimalValue(null));
        assertThrows(NullPointerException.class, () -> new StringValue(null));
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
        assertEquals("1", one.asString("fallback"));
        assertEquals(BigDecimal.valueOf(1), one.asDecimal(BigDecimal.TEN));

        Value fortyTwo = new IntValue(42);
        // conversion fails, default returned
        assertTrue(fortyTwo.asBoolean(true));
        assertFalse(fortyTwo.asBoolean(false));

        Value ten = new BigDecimalValue(BigDecimal.TEN);
        // conversion fails, default returned
        assertFalse(ten.asBoolean(false));
        // conversion succeeds, actual value returned
        assertEquals(10, ten.asInt(99));
        assertEquals("10", ten.asString("fallback"));
        assertEquals(BigDecimal.TEN, ten.asDecimal(BigDecimal.ZERO));

        Value fractional = new BigDecimalValue(new BigDecimal("1.5"));
        // asInt conversion fails for fractional values, default returned
        assertEquals(99, fractional.asInt(99));

        Value boolTrue = BooleanValue.from(true);
        assertTrue(boolTrue.asBoolean(false));
        assertEquals(1, boolTrue.asInt(99));
        assertEquals("true", boolTrue.asString("fallback"));
        assertEquals(BigDecimal.ONE, boolTrue.asDecimal(BigDecimal.ZERO));
    }

}
