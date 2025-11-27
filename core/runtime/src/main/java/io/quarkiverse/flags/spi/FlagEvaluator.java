package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

/**
 * An evaluator can be used to compute a value of a feature flag. It receives the initial flag value but it does not have to use
 * it during evaluation.
 * <p>
 * Implementation classes must be CDI beans. Qualifiers are ignored. {@link jakarta.enterprise.context.Dependent} beans are
 * reused.
 */
public interface FlagEvaluator {

    /**
     * This key can be used to obtain the evaluator id from flag metadata.
     *
     * @see Flag#metadata()
     */
    static String META_KEY = "evaluator";

    /**
     * The identifier must be unique. If multiple flag evaluators with the same identifier exist then the application fails to
     * start.
     *
     * @return the identifier
     */
    String id();

    /**
     * The initial flag value does not have to be used during evaluation.
     *
     * @param flag
     * @param initialValue
     * @param computationContext
     * @return the evaluated value
     */
    Uni<Value> evaluate(Flag flag, Value initialValue, ComputationContext computationContext);

}
