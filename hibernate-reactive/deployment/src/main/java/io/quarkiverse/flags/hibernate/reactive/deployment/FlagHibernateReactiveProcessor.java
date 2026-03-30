package io.quarkiverse.flags.hibernate.reactive.deployment;

import java.util.List;
import java.util.Map;

import jakarta.inject.Singleton;

import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.gizmo2.Jandex2Gizmo;
import org.jboss.logging.Logger;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.hibernate.orm.deployment.FlagDefinitionBuildItem;
import io.quarkiverse.flags.hibernate.orm.deployment.FlagDefinitionBuildItem.Property;
import io.quarkiverse.flags.hibernate.reactive.runtime.AbstractHibernateReactiveFlagProvider;
import io.quarkiverse.flags.runtime.ConfigFlagProvider;
import io.quarkiverse.flags.runtime.InMemoryFlagProviderImpl;
import io.quarkiverse.flags.spi.ComponentOrder;
import io.quarkiverse.flags.spi.FlagManager;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmo2Adaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.This;
import io.quarkus.gizmo2.TypeArgument;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.hibernate.orm.deployment.PersistenceUnitDescriptorBuildItem;
import io.quarkus.hibernate.orm.runtime.PersistenceUnitUtil;
import io.smallrye.common.annotation.Identifier;
import io.vertx.core.Vertx;

public class FlagHibernateReactiveProcessor {

    private static final Logger LOG = Logger.getLogger(FlagHibernateReactiveProcessor.class);

    @BuildStep
    void generateFlagProvider(FlagHibernateReactiveBuildTimeConfig config,
            List<PersistenceUnitDescriptorBuildItem> descriptors,
            List<FlagDefinitionBuildItem> flagDefinitions, BuildProducer<GeneratedBeanBuildItem> generatedBeans) {
        if (flagDefinitions.isEmpty()) {
            LOG.debugf("No @FlagDefinition found - no Hibernate Reactive FlagProvider will be generated");
            return;
        }
        if (descriptors.stream().noneMatch(pud -> pud.getPersistenceUnitName().equals(config.persistenceUnitName()))) {
            throw new IllegalStateException("Invalid persistence unit selected: " + config.persistenceUnitName());
        }
        ClassOutput classOutput = new GeneratedBeanGizmo2Adaptor(generatedBeans);
        Gizmo gizmo = Gizmo.create(classOutput);

        for (FlagDefinitionBuildItem flagDefinition : flagDefinitions) {
            ClassInfo entityClass = flagDefinition.getEntityClass();
            String className = entityClass.name() + "_HibernateReactiveFlagProvider";
            String entityName = flagDefinition.getEntityName();

            gizmo.class_(className, cc -> {
                This this_ = cc.this_();
                cc.addAnnotation(Singleton.class);
                cc.addAnnotation(Identifier.Literal.of(entityName));
                cc.addAnnotation(ComponentOrder.class, ac -> {
                    ac.addArray("before", new String[] { ConfigFlagProvider.ID });
                    ac.addArray("after", new String[] { InMemoryFlagProviderImpl.ID });
                });
                cc.extends_(GenericType.ofClass(AbstractHibernateReactiveFlagProvider.class,
                        TypeArgument.of(Jandex2Gizmo.classDescOf(entityClass))));

                cc.constructor(constructor -> {
                    // MyFlag_HibernateReactiveFlagProvider(Mutiny.SessionFactory sf, FlagManager fm, Vertx vertx) {
                    //    super(fm, "com.acme.MyFlag_HibernateReactiveFlagProvider", vertx, sf, featureParam, entityName, entityClass);
                    //    this.em = em;
                    // }
                    constructor.public_();
                    ParamVar sf = constructor.parameter("sf", pc -> {
                        pc.setType(Mutiny.SessionFactory.class);
                        if (!config.persistenceUnitName().equals(PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME)) {
                            pc.addAnnotation(new PersistenceUnit.PersistenceUnitLiteral(config.persistenceUnitName()));
                        }
                    });
                    ParamVar manager = constructor.parameter("fm", FlagManager.class);
                    ParamVar vertx = constructor.parameter("vertx", Vertx.class);
                    constructor.body(bc -> {
                        Property metadataProperty = flagDefinition.getMetadata();
                        Expr metadataParam = metadataProperty != null
                                ? Const.of(metadataProperty.name())
                                : Const.ofNull(String.class);
                        bc.invokeSpecial(
                                ConstructorDesc.of(AbstractHibernateReactiveFlagProvider.class,
                                        FlagManager.class, String.class, Vertx.class, Mutiny.SessionFactory.class,
                                        String.class, String.class, Class.class, String.class),
                                this_,
                                manager, Const.of(className), vertx, sf,
                                Const.of(flagDefinition.getFeature().name()), Const.of(flagDefinition.getEntityName()),
                                Const.of(Jandex2Gizmo.classDescOf(entityClass)), metadataParam);
                        bc.return_();
                    });
                });

                cc.method("toFlag", mc -> {
                    mc.returning(Flag.class);
                    mc.protected_();
                    ParamVar entityParam = mc.parameter("entity", Object.class);
                    mc.body(bc -> {
                        Expr feature = flagDefinition.getFeature().read(entityParam, bc);
                        Expr value = flagDefinition.getValue().read(entityParam, bc);
                        Expr metadata;
                        Property metadataProperty = flagDefinition.getMetadata();
                        if (metadataProperty != null) {
                            metadata = metadataProperty.read(entityParam, bc);
                        } else {
                            metadata = bc.mapOf();
                        }
                        bc.return_(bc.invokeVirtual(
                                MethodDesc.of(AbstractHibernateReactiveFlagProvider.class, "createFlag", Flag.class,
                                        String.class,
                                        String.class,
                                        Map.class),
                                this_, feature, value, metadata));
                    });
                });
            });
        }
    }

}
