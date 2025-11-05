package io.quarkiverse.flags.spi;

import io.quarkiverse.flags.Flag;

public interface FlagProvider {

    int DEFAULT_PRIORITY = 1;

    /**
     * @return the flags
     */
    Iterable<Flag> getFlags();

}
