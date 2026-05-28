package io.quarkiverse.flags.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkiverse.flags.Flag.ComputationContext;

public class ComputationContextTest {

    @Test
    public void testEmptyAsMap() {
        assertTrue(ComputationContext.EMPTY.asMap().isEmpty());
    }

    @Test
    public void testAsMap() {
        ComputationContext ctx = ComputationContext.builder()
                .put("key1", "value1")
                .put("key2", 42)
                .build();
        Map<String, Object> map = ctx.asMap();
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals(42, map.get("key2"));
    }

    @Test
    public void testAsMapIsUnmodifiable() {
        ComputationContext ctx = ComputationContext.of("key", "value");
        Map<String, Object> map = ctx.asMap();
        assertThrows(UnsupportedOperationException.class, () -> map.put("new", "entry"));
    }

}
