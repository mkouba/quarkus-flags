package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

/**
 * An evaluator can be used to compute a value of a feature flag.
 */
public interface FlagEvaluator {

    /**
     * @see Flag#metadata()
     */
    static String METADATA_KEY = "evaluator";

    /**
     * The identifier must be unique.
     *
     * @return the identifier
     */
    String id();

    /**
     * @param flag
     * @param initialValue
     * @param computationContext
     * @return the evaluated value
     */
    Uni<Value> evaluate(Flag flag, Flag.Value initialValue, ComputationContext computationContext);

}
