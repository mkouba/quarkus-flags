package io.quarkiverse.flags.spi;

import java.util.HashMap;
import java.util.Map;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;

public class ComputationContextImpl implements Flag.ComputationContext {

    private final Map<String, Object> data;

    private ComputationContextImpl(Map<String, Object> data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public static class BuilderImpl implements Flag.ComputationContext.Builder {

        private final Map<String, Object> data = new HashMap<>();

        @Override
        public Builder put(String key, Object value) {
            data.put(key, value);
            return this;
        }

        @Override
        public ComputationContext build() {
            return new ComputationContextImpl(Map.copyOf(data));
        }

    }

}
