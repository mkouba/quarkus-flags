package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

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
    }

    @Test
    public void testString() {
        Value foo = new StringValue("foo");
        assertThrows(NoSuchElementException.class, () -> foo.asInt());
        assertEquals("foo", foo.asString());
        assertThrows(NoSuchElementException.class, () -> foo.asBoolean());
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
