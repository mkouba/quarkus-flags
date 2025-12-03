package io.quarkiverse.flags.cron;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;

import jakarta.inject.Singleton;

import org.jboss.logging.Logger;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.quarkiverse.flags.spi.BooleanValue;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.smallrye.mutiny.Uni;

/**
 * Evaluates a flag based on the current date-time obtained from the system clock in the default time-zone.
 * <p>
 * If the initial value is {@code true} and the current date-time matches the configured CRON expression then the flag evaluates
 * to {@code true}. Otherwise, it evaluates to {@code false}.
 * <p>
 * The evaluator is configured through the {@link Flag#metadata()}. The {@value #CRON_EXPR} is used to specify the CRON
 * expression and {@value #CRON_TYPE} defines the CRON syntax used to parse the expression.
 * no bound.
 */
@Singleton
public class CronFlagEvaluator implements FlagEvaluator {

    public static final String ID = "quarkus.cron";

    public static final String CRON_EXPR = "cron-expr";

    /**
     * The syntax used for CRON expression.
     * <p>
     * Must be one of CRON4J, QUARTZ, UNIX, SPRING, SPRING53.
     */
    public static final String CRON_TYPE = "cron-type";

    private static final Logger LOG = Logger.getLogger(CronFlagEvaluator.class);

    private final EnumMap<CronType, CronParser> parsers;

    CronFlagEvaluator() {
        parsers = new EnumMap<>(CronType.class);
        for (CronType cronType : CronType.values()) {
            parsers.put(cronType, new CronParser(CronDefinitionBuilder.instanceDefinitionFor(cronType)));
        }
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext) {
        if (initialValue.asBoolean()) {
            String cronExpr = flag.metadata().get(CRON_EXPR);
            if (cronExpr == null || cronExpr.isBlank()) {
                throw new IllegalStateException("Cron expression not set");
            }
            CronType cronType = getCronType(flag);
            Cron cron = parsers.get(cronType).parse(cronExpr);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            ZonedDateTime now = ZonedDateTime.now();
            boolean val = executionTime.isMatch(now);
            LOG.debugf("%s %s the cron expression %s [cronType: %s]", now.truncatedTo(ChronoUnit.SECONDS),
                    val ? "matches" : "does not match",
                    cronExpr, cronType);
            return BooleanValue.createUni(val);
        }
        return Uni.createFrom().item(initialValue);
    }

    private CronType getCronType(Flag flag) {
        String cronType = flag.metadata().get(CRON_TYPE);
        if (cronType == null) {
            return CronType.UNIX;
        }
        return CronType.valueOf(cronType.toUpperCase());
    }

}
