package io.quarkiverse.flags.runtime.impl;

import java.util.List;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class FlagsRecorder {

    public RuntimeValue<FlagContext> createContext(List<String> orderedProviderIds) {
        return new RuntimeValue<>(new FlagContext(orderedProviderIds));
    }

}
