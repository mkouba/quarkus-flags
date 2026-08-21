package io.quarkiverse.flags.runtime.dev.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class FormatDurationTest {

    @Test
    public void testFormatDuration() {
        assertEquals("0s", FlagsJsonRPCService.formatDuration(Duration.ZERO));
        assertEquals("45s", FlagsJsonRPCService.formatDuration(Duration.ofSeconds(45)));
        assertEquals("10m", FlagsJsonRPCService.formatDuration(Duration.ofMinutes(10)));
        assertEquals("1m 30s", FlagsJsonRPCService.formatDuration(Duration.ofSeconds(90)));
        assertEquals("1h", FlagsJsonRPCService.formatDuration(Duration.ofHours(1)));
        assertEquals("1h 30m", FlagsJsonRPCService.formatDuration(Duration.ofMinutes(90)));
        assertEquals("2h 15m 5s",
                FlagsJsonRPCService.formatDuration(Duration.ofHours(2).plusMinutes(15).plusSeconds(5)));
        assertEquals("1d", FlagsJsonRPCService.formatDuration(Duration.ofDays(1)));
        assertEquals("1d 1h", FlagsJsonRPCService.formatDuration(Duration.ofHours(25)));
        assertEquals("500ms", FlagsJsonRPCService.formatDuration(Duration.ofMillis(500)));
        assertEquals("1s 500ms", FlagsJsonRPCService.formatDuration(Duration.ofMillis(1500)));
    }

}
