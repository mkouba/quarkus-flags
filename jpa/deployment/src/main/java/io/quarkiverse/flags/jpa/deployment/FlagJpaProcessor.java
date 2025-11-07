package io.quarkiverse.flags.jpa.deployment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.jpa.FlagDefinition;
import io.quarkiverse.flags.jpa.deployment.FlagDefinitionBuildItem.Property;
import io.quarkiverse.flags.spi.FlagProvider;
import io.quarkiverse.flags.spi.ImmutableFlag;
import io.quarkiverse.flags.spi.ImmutableStringValue;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmo2Adaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.This;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.hibernate.orm.deployment.PersistenceUnitDescriptorBuildItem;
import io.quarkus.hibernate.orm.runtime.PersistenceUnitUtil;
import io.quarkus.panache.common.deployment.PanacheEntityClassesBuildItem;

public class FlagJpaProcessor {

    @BuildStep
    void flagDefinition(ApplicationIndexBuildItem index, List<PanacheEntityClassesBuildItem> panacheEntityClasses,
            BuildProducer<FlagDefinitionBuildItem> flagDefinition) {
        List<AnnotationInstance> flagDefinitions = index.getIndex().getAnnotations(DotName.createSimple(FlagDefinition.class));
        if (flagDefinitions.size() > 1) {
            throw new RuntimeException("At most one entity class can be annotated with @FlagDefinition");
        } else if (!flagDefinitions.isEmpty()) {
            Set<String> panacheEntities = new HashSet<>();
            for (PanacheEntityClassesBuildItem entityClasses : panacheEntityClasses) {
                panacheEntities.addAll(entityClasses.getEntityClasses());
            }
            ClassInfo entityClass = flagDefinitions.get(0).target().asClass();
            flagDefinition.produce(
                    new FlagDefinitionBuildItem(entityClass, panacheEntities.contains(entityClass.name().toString())));
        }
    }

    @BuildStep
    void generateFlagProvider(FlagJpaBuildTimeConfig config, List<PersistenceUnitDescriptorBuildItem> descriptors,
            FlagDefinitionBuildItem flagDefinition, BuildProducer<GeneratedBeanBuildItem> generatedBeans) {
        if (descriptors.stream().noneMatch(pud -> pud.getPersistenceUnitName().equals(config.persistenceUnitName()))) {
            throw new IllegalStateException("Invalid persistence unit selected: " + config.persistenceUnitName());
        }

        ClassOutput classOutput = new GeneratedBeanGizmo2Adaptor(generatedBeans);
        Gizmo gizmo = Gizmo.create(classOutput);

        ClassInfo entityClass = flagDefinition.getEntityClass();

        gizmo.class_(entityClass.name() + "_JpaFlagProvider", cc -> {
            cc.implements_(FlagProvider.class);
            cc.defaultConstructor();
            cc.addAnnotation(Singleton.class);

            FieldDesc em = cc.field("em", fc -> {
                fc.setType(EntityManager.class);
                fc.packagePrivate();
                fc.addAnnotation(Inject.class);
                if (!config.persistenceUnitName().equals(PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME)) {
                    // Non-default persistence unit used
                    fc.addAnnotation(new PersistenceUnit.PersistenceUnitLiteral(config.persistenceUnitName()));
                }
            });

            This this_ = cc.this_();

            cc.method("getPriority", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(FlagProvider.DEFAULT_PRIORITY + 5);
                });
            });

            cc.method("getFlags", mc -> {
                mc.returning(Iterable.class);
                mc.addAnnotation(Transactional.class);
                mc.body(bc -> {
                    // List<MyFlag> flags = em.createQuery("from MyFlag").getResultList();
                    Expr query = bc.invokeInterface(
                            MethodDesc.of(EntityManager.class, "createQuery", Query.class, String.class),
                            this_.field(em),
                            Const.of("from " + flagDefinition.getEntityName()));
                    LocalVar flags = bc.localVar("flags",
                            bc.invokeInterface(MethodDesc.of(Query.class, "getResultList", List.class),
                                    query));
                    // List<Flag> ret = new ArrayList(all.size());
                    LocalVar ret = bc.localVar("ret", bc.new_(ArrayList.class, bc.withList(flags).size()));
                    // for (MyFlag myFlag : all) {
                    //    ret.add(new ImmutableFlag(myFlag.feature, myFlag.metadata, new ImmutableStringValue(myFlag.value)));
                    // }
                    bc.forEach(flags, (ibc, item) -> {
                        Expr feature = flagDefinition.getFeature().read(item, ibc);
                        Expr metadata;
                        Property metadataProperty = flagDefinition.getMetadata();
                        if (metadataProperty != null) {
                            metadata = metadataProperty.read(item, ibc);
                        } else {
                            metadata = Const.ofNull(Map.class);
                        }
                        Expr value = flagDefinition.getValue().read(item, ibc);
                        ibc.withList(ret)
                                .add(ibc.new_(
                                        ConstructorDesc.of(ImmutableFlag.class, String.class, Map.class, Flag.Value.class),
                                        feature, metadata,
                                        ibc.new_(ImmutableStringValue.class, value)));
                    });
                    bc.return_(ret);
                });
            });

        });

    }

}
