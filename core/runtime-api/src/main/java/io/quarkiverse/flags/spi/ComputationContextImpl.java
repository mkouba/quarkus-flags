package io.quarkiverse.flags.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.Flag.ComputationContext;

/**
 * Immutable {@link Flag.ComputationContext} implementation. Use {@link BuilderImpl} to construct instances.
 */
public class ComputationContextImpl implements Flag.ComputationContext {

    private final Map<String, Object> data;

    private ComputationContextImpl(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public Object get(String key) {
        return data.get(key);
    }

    @Override
    public Map<String, Object> asMap() {
        return data;
    }

    public static class BuilderImpl implements Flag.ComputationContext.Builder {

        private final Map<String, Object> data = new HashMap<>();

        @Override
        public Builder put(String key, Object value) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");
            data.put(key, value);
            return this;
        }

        @Override
        public ComputationContext build() {
            return new ComputationContextImpl(Map.copyOf(data));
        }

    }

}
