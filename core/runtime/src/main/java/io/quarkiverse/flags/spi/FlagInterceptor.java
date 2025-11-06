package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.Value;
import io.smallrye.mutiny.Uni;

public interface FlagInterceptor extends Comparable<FlagInterceptor> {

    int DEFAULT_PRIORITY = 1;

    /**
     * @return the priority
     */
    default int getPriority() {
        return DEFAULT_PRIORITY;
    }

    /**
     *
     * @param flag
     * @param value
     * @param computationContext
     * @return the computed value
     */
    Uni<Value> afterCompute(Flag flag, Flag.Value value, ComputationContext computationContext);

    @Override
    default int compareTo(FlagInterceptor other) {
        return Integer.compare(other.getPriority(), getPriority());
    }

}
