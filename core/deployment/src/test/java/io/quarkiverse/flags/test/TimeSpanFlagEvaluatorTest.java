package io.quarkiverse.flags.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.flags.Flags;
import io.quarkiverse.flags.TimeSpanFlagEvaluator;
import io.quarkus.test.QuarkusUnitTest;

public class TimeSpanFlagEvaluatorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withEmptyApplication()
            .overrideRuntimeConfigKey("quarkus.flags.runtime.alpha.value", "true")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.alpha.meta.evaluator", TimeSpanFlagEvaluator.ID)
            .overrideRuntimeConfigKey("quarkus.flags.runtime.alpha.meta.start-time",
                    "2011-11-01T10:15:30+01:00[Europe/Prague]")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.bravo.value", "true")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.bravo.meta.evaluator", TimeSpanFlagEvaluator.ID)
            .overrideRuntimeConfigKey("quarkus.flags.runtime.bravo.meta.start-time",
                    "2115-11-01T10:15:30+01:00[Europe/Prague]")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.charlie.value", "true")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.charlie.meta.evaluator", TimeSpanFlagEvaluator.ID)
            .overrideRuntimeConfigKey("quarkus.flags.runtime.charlie.meta.start-time",
                    "2001-01-01T10:15:30+01:00[Europe/Prague]")
            .overrideRuntimeConfigKey("quarkus.flags.runtime.charlie.meta.end-time",
                    "2115-11-01T10:15:30+01:00[Europe/Prague]");

    @Inject
    Flags flags;

    @Test
    public void testFlag() {
        assertTrue(flags.isEnabled("alpha"));
        assertEquals("true", flags.find("alpha").orElseThrow().getString());
        assertFalse(flags.isEnabled("bravo"));
        assertEquals(0, flags.find("bravo").orElseThrow().getInt());
        assertTrue(flags.isEnabled("charlie"));
        assertEquals(1, flags.find("charlie").orElseThrow().getInt());
    }

}
