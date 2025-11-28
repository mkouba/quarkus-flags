package io.quarkiverse.flags;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.inject.Singleton;

import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.spi.BooleanValue;
import io.quarkiverse.flags.spi.FlagEvaluator;
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
@Singleton
public class TimeSpanFlagEvaluator implements FlagEvaluator {

    public static final String ID = "quarkus.time-span";
    public static final String START_TIME = "start-time";
    public static final String END_TIME = "end-time";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
        if (initialValue.asBoolean()) {
            String startTime = flag.metadata().get(START_TIME);
            String endTime = flag.metadata().get(END_TIME);
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime start = startTime != null ? parse(startTime) : now.minusSeconds(1);
            ZonedDateTime end = endTime != null ? parse(endTime) : now.plusSeconds(1);
            if (now.isAfter(start) && now.isBefore(end)) {
                initialValue = BooleanValue.TRUE;
            } else {
                initialValue = BooleanValue.FALSE;
            }
        }
        return Uni.createFrom().item(initialValue);
    }

    private ZonedDateTime parse(String value) {
        return ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

}
