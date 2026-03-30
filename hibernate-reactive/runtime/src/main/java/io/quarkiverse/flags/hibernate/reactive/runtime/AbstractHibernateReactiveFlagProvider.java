package io.quarkiverse.flags.hibernate.reactive.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.spi.AbstractFlagProvider;
import io.quarkiverse.flags.spi.FlagManager;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public abstract class AbstractHibernateReactiveFlagProvider<E> extends AbstractFlagProvider {

    protected final Vertx vertx;
    protected final Mutiny.SessionFactory sf;
    protected final String origin;
    protected final Class<E> entityClass;
    protected final String entityName;
    protected final String featureParam;
    protected final String allQuery;
    protected final String byFeatureQuery;

    public AbstractHibernateReactiveFlagProvider(FlagManager manager, String origin, Vertx vertx, Mutiny.SessionFactory sf,
            String featureParam, String entityName, Class<E> entityClass, String metadataParam) {
        super(manager);
        this.sf = sf;
        this.vertx = vertx;
        this.origin = origin;
        this.entityName = entityName;
        this.entityClass = entityClass;
        this.featureParam = featureParam;
        String joinFetch = metadataParam != null ? " LEFT JOIN FETCH e." + metadataParam : "";
        this.allQuery = "from " + entityName + " e" + joinFetch;
        this.byFeatureQuery = "from " + entityName + " e" + joinFetch + " where e." + featureParam + " = :" + featureParam;
    }

    @Override
    public Uni<Collection<Flag>> getFlags() {
        Context context = VertxContext.createNewDuplicatedContext(vertx.getOrCreateContext());
        VertxContextSafetyToggle.setContextSafe(context, true);
        return Uni.createFrom().emitter(e -> {
            context.runOnContext(new Handler<Void>() {

                @Override
                public void handle(Void event) {
                    ManagedContext requestContext = Arc.container().requestContext();
                    Runnable terminate = null;
                    if (!requestContext.isActive()) {
                        requestContext.activate();
                        terminate = requestContext::terminate;
                    }
                    try {
                        Uni<Collection<Flag>> uni = sf
                                .withSession(s -> s.createSelectionQuery(allQuery, entityClass)
                                        .getResultList()
                                        .map(r -> toFlags(r)));
                        if (terminate != null) {
                            uni = uni.onTermination().invoke(terminate);
                        }
                        uni.subscribe().with(e::complete, e::fail);
                    } catch (Throwable t) {
                        e.fail(t);
                    }
                }
            });
        });
    }

    @Override
    public Uni<Flag> getFlag(String feature) {
        Context context = VertxContext.createNewDuplicatedContext(vertx.getOrCreateContext());
        VertxContextSafetyToggle.setContextSafe(context, true);
        return Uni.createFrom().emitter(e -> {
            context.runOnContext(new Handler<Void>() {

                @Override
                public void handle(Void event) {
                    ManagedContext requestContext = Arc.container().requestContext();
                    Runnable terminate = null;
                    if (!requestContext.isActive()) {
                        requestContext.activate();
                        terminate = requestContext::terminate;
                    }
                    try {
                        Uni<Flag> uni = sf.withSession(
                                s -> s.createSelectionQuery(byFeatureQuery, entityClass)
                                        .setParameter(featureParam, feature)
                                        .getResultList()
                                        .map(list -> toFlag(ensureSingle(list, feature))));
                        if (terminate != null) {
                            uni = uni.onTermination().invoke(terminate);
                        }
                        uni.subscribe().with(e::complete, e::fail);
                    } catch (Throwable t) {
                        e.fail(t);
                    }
                }
            });
        });
    }

    protected abstract Flag toFlag(E entity);

    protected Flag createFlag(String feature, String value, Map<String, String> metadata) {
        return Flag.builder(feature)
                .setOrigin(origin)
                .setMetadata(metadata)
                .setString(value)
                .setFeatureManager(manager)
                .build();
    }

    protected E ensureSingle(List<E> flagEntities, String feature) {
        if (flagEntities.isEmpty()) {
            return null;
        }
        if (flagEntities.size() > 1) {
            throw new IllegalStateException("Multiple flags match the feature [" + feature + "]: " + flagEntities);
        }
        return flagEntities.get(0);
    }

    private Collection<Flag> toFlags(List<E> entities) {
        List<Flag> ret = new ArrayList<>(entities.size());
        for (E e : entities) {
            ret.add(toFlag(e));
        }
        return ret;
    }

}
