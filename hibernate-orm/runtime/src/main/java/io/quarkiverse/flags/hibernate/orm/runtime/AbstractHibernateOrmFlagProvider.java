package io.quarkiverse.flags.hibernate.orm.runtime;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.AbstractFlagProvider;
import io.quarkiverse.flags.spi.FlagManager;
import io.quarkus.runtime.BlockingOperationControl;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;

public abstract class AbstractHibernateOrmFlagProvider extends AbstractFlagProvider {

    public AbstractHibernateOrmFlagProvider(FlagManager manager) {
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

    @Override
    public Uni<Flag> getFlag(String feature) {
        if (BlockingOperationControl.isBlockingAllowed()) {
            return Uni.createFrom().item(doGetFlag(feature));
        }
        return VertxContextSupport.executeBlocking(new Callable<Flag>() {
            @Override
            public Flag call() throws Exception {
                return doGetFlag(feature);
            }
        });
    }

    protected abstract Collection<Flag> doGetFlags();

    protected abstract Flag doGetFlag(String feature);

    protected Flag createFlag(String feature, String value, Map<String, String> metadata) {
        return Flag.builder(feature)
                .setMetadata(metadata)
                .setString(value)
                .setFeatureManager(manager)
                .build();
    }

    protected Object ensureSingle(List<Object> flagEntities, String feature) {
        if (flagEntities.isEmpty()) {
            return null;
        }
        if (flagEntities.size() > 1) {
            throw new IllegalStateException("Multiple flags match the feature [" + feature + "]: " + flagEntities);
        }
        return flagEntities.get(0);
    }

}
