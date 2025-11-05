package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;
import io.quarkiverse.flags.Flag.State;
import io.smallrye.mutiny.Uni;

public interface FlagInterceptor {

    /**
     *
     * @param flag
     * @param state
     * @param computationContext
     * @return the computed state
     */
    Uni<State> afterCompute(Flag flag, Flag.State state, ComputationContext computationContext);

}
