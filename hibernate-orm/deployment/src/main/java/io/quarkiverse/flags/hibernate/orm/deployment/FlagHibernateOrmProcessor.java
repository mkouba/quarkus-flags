package io.quarkiverse.flags.hibernate.orm.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import org.jboss.jandex.ClassInfo;
import org.jboss.logging.Logger;

import io.quarkiverse.flags.Flag;
import io.quarkiverse.flags.hibernate.orm.deployment.FlagDefinitionBuildItem.Property;
import io.quarkiverse.flags.hibernate.orm.runtime.AbstractHibernateOrmFlagProvider;
import io.quarkiverse.flags.spi.FlagManager;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmo2Adaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.This;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.hibernate.orm.deployment.PersistenceUnitDescriptorBuildItem;
import io.quarkus.hibernate.orm.runtime.PersistenceUnitUtil;

public class FlagHibernateOrmProcessor {

    private static final Logger LOG = Logger.getLogger(FlagHibernateOrmProcessor.class);

    @BuildStep
    void generateFlagProvider(FlagHibernateOrmBuildTimeConfig config, List<PersistenceUnitDescriptorBuildItem> descriptors,
            List<FlagDefinitionBuildItem> flagDefinitions, BuildProducer<GeneratedBeanBuildItem> generatedBeans) {
        if (flagDefinitions.isEmpty()) {
            LOG.debugf("No @FlagDefinition found - no JPA FlagProvider will be generated");
            return;
        }
        if (descriptors.stream().noneMatch(pud -> pud.getPersistenceUnitName().equals(config.persistenceUnitName()))) {
            throw new IllegalStateException("Invalid persistence unit selected: " + config.persistenceUnitName());
        }
        ClassOutput classOutput = new GeneratedBeanGizmo2Adaptor(generatedBeans);
        Gizmo gizmo = Gizmo.create(classOutput);

        for (FlagDefinitionBuildItem flagDefinition : flagDefinitions) {
            ClassInfo entityClass = flagDefinition.getEntityClass();

            gizmo.class_(entityClass.name() + "_HibernateOrmFlagProvider", cc -> {
                This this_ = cc.this_();
                cc.addAnnotation(Singleton.class);
                cc.extends_(AbstractHibernateOrmFlagProvider.class);

                // private final EntityManager em;
                FieldDesc emField = cc.field("em", fc -> {
                    fc.setType(EntityManager.class);
                    fc.private_();
                    fc.final_();
                });

                cc.constructor(constructor -> {
                    // MyFlag_HibernateOrmFlagProvider(EntityManager em, FlagManager fm) {
                    //    super(fm);
                    //    this.em = em;
                    // }
                    constructor.public_();
                    ParamVar em = constructor.parameter("em", pc -> {
                        pc.setType(EntityManager.class);
                        if (!config.persistenceUnitName().equals(PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME)) {
                            // Non-default persistence unit used
                            pc.addAnnotation(new PersistenceUnit.PersistenceUnitLiteral(config.persistenceUnitName()));
                        }
                    });
                    ParamVar manager = constructor.parameter("fm", FlagManager.class);
                    constructor.body(bc -> {
                        bc.invokeSpecial(ConstructorDesc.of(AbstractHibernateOrmFlagProvider.class, FlagManager.class), this_,
                                manager);
                        bc.set(this_.field(emField), em);
                        bc.return_();
                    });
                });

                cc.method("getPriority", mc -> {
                    mc.returning(int.class);
                    mc.body(bc -> {
                        // Config provider has 200, in-memory provider has 600
                        bc.return_(400);
                    });
                });

                cc.method("doGetFlags", mc -> {
                    mc.returning(Collection.class);
                    mc.addAnnotation(Transactional.class);

                    mc.body(bc -> {
                        // List<MyFlag> flags = em.createQuery("from MyFlag").getResultList();
                        Expr query = bc.invokeInterface(
                                MethodDesc.of(EntityManager.class, "createQuery", Query.class, String.class),
                                this_.field(emField),
                                Const.of("from " + flagDefinition.getEntityName()));
                        LocalVar flags = bc.localVar("flags",
                                bc.invokeInterface(MethodDesc.of(Query.class, "getResultList", List.class),
                                        query));
                        // List<Flag> ret = new ArrayList(all.size());
                        LocalVar ret = bc.localVar("ret", bc.new_(ArrayList.class, bc.withList(flags).size()));
                        // for (MyFlag myFlag : all) {
                        //    ret.add(this.createFlag(myFlag.feature, myFlag.metadata, myFlag.value));
                        // }
                        bc.forEach(flags, (ibc, item) -> {
                            Expr feature = flagDefinition.getFeature().read(item, ibc);
                            Expr value = flagDefinition.getValue().read(item, ibc);
                            Expr metadata;
                            Property metadataProperty = flagDefinition.getMetadata();
                            if (metadataProperty != null) {
                                metadata = metadataProperty.read(item, ibc);
                            } else {
                                metadata = Const.ofNull(Map.class);
                            }
                            ibc.withList(ret)
                                    .add(ibc.invokeVirtual(
                                            MethodDesc.of(AbstractHibernateOrmFlagProvider.class, "createFlag", Flag.class,
                                                    String.class,
                                                    String.class,
                                                    Map.class),
                                            this_, feature, value, metadata));
                        });
                        bc.return_(ret);
                    });
                });
            });
        }
    }

}
