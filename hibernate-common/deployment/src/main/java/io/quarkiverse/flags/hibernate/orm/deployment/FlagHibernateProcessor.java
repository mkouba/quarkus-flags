package io.quarkiverse.flags.hibernate.orm.deployment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import io.quarkiverse.flags.hibernate.FlagSource;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.panache.common.deployment.PanacheEntityClassesBuildItem;

public class FlagHibernateProcessor {

    @BuildStep
    void collectFlagSources(ApplicationIndexBuildItem index, List<PanacheEntityClassesBuildItem> panacheEntityClasses,
            BuildProducer<FlagSourceBuildItem> flagSource) {
        List<AnnotationInstance> flagSources = index.getIndex().getAnnotations(DotName.createSimple(FlagSource.class));
        Set<String> panacheEntities = new HashSet<>();
        for (PanacheEntityClassesBuildItem entityClasses : panacheEntityClasses) {
            panacheEntities.addAll(entityClasses.getEntityClasses());
        }
        for (AnnotationInstance flagSourceAnnotation : flagSources) {
            ClassInfo entityClass = flagSourceAnnotation.target().asClass();
            flagSource.produce(
                    new FlagSourceBuildItem(entityClass, panacheEntities.contains(entityClass.name().toString())));
        }
    }

}
