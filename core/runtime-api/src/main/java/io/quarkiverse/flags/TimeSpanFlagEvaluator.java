package io.quarkiverse.flags;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.mutiny.Uni;

/**
 * Evaluates a flag based on the current date-time obtained from the system clock in the default time-zone.
 * <p>
 * If the initial value is {@code true} and the current date-time is after the {@code start-time} (exclusive) and before the
 * {@code end-time} (exclusive) then the flag evaluates to {@code true}. Otherwise, it evaluates to {@code false}.
 * <p>
 * The evaluator is configured through the {@link Flag#metadata()}. The {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} is used to
 * parse values of the {@value #START_TIME} and {@value #END_TIME} metadata. Both values are optional - an absent value implies
 * no bound.
 */
@Identifier(TimeSpanFlagEvaluator.ID)
@Singleton
public class TimeSpanFlagEvaluator implements FlagEvaluator {

    public static final String ID = "quarkus.time-span";
    public static final String START_TIME = "start-time";
    public static final String END_TIME = "end-time";

    @Override
    public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
        if (initialValue.asBoolean()) {
            String startTime = flag.metadata().get(START_TIME);
            String endTime = flag.metadata().get(END_TIME);
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime start = startTime != null ? parse(START_TIME, startTime) : null;
            ZonedDateTime end = endTime != null ? parse(END_TIME, endTime) : null;
            if (start != null && end != null && !start.isBefore(end)) {
                throw new IllegalStateException(
                        START_TIME + " [" + startTime + "] must be before " + END_TIME + " [" + endTime + "]");
            }
            boolean afterStart = start == null || now.isAfter(start);
            boolean beforeEnd = end == null || now.isBefore(end);
            initialValue = BooleanValue.from(afterStart && beforeEnd);
        }
        return Uni.createFrom().item(initialValue);
    }

    private ZonedDateTime parse(String key, String value) {
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException(
                    "Invalid " + key + " value [" + value + "]; expected ISO_ZONED_DATE_TIME format", e);
        }
    }

}
