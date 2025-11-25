package io.quarkiverse.flags;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.ImmutableBooleanValue;
import io.smallrye.mutiny.Uni;

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
            ZonedDateTime start = startTime != null ? ZonedDateTime.parse(startTime, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                    : now.minusSeconds(1);
            ZonedDateTime end = endTime != null ? ZonedDateTime.parse(endTime, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                    : now.plusSeconds(1);
            if (now.isAfter(start) && now.isBefore(end)) {
                initialValue = ImmutableBooleanValue.TRUE;
            } else {
                initialValue = ImmutableBooleanValue.FALSE;
            }
        }
        return Uni.createFrom().item(initialValue);
    }

}
