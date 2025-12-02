package io.quarkiverse.flags.jpa.runtime;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.AbstractFlagProvider;
import io.quarkiverse.flags.spi.FlagManager;
import io.quarkus.runtime.BlockingOperationControl;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;

public abstract class AbstractJpaFlagProvider extends AbstractFlagProvider {

    public AbstractJpaFlagProvider(FlagManager manager) {
        super(manager);
    }

    @Override
    public Uni<Collection<Flag>> getFlags() {
        if (BlockingOperationControl.isBlockingAllowed()) {
            return Uni.createFrom().item(doGetFlags());
        }
        return VertxContextSupport.executeBlocking(new Callable<Collection<Flag>>() {
            @Override
            public Collection<Flag> call() throws Exception {
                return doGetFlags();
            }
        });
    }

    protected abstract Collection<Flag> doGetFlags();

    protected Flag createFlag(String feature, String value, Map<String, String> metadata) {
        return Flag.builder(feature).setMetadata(metadata).setString(value).build();
    }

}
