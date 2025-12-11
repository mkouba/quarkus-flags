package io.quarkiverse.flags.hibernate.orm.deployment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import io.quarkiverse.flags.hibernate.common.FlagDefinition;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.panache.common.deployment.PanacheEntityClassesBuildItem;

public class FlagHibernateProcessor {

    @BuildStep
    void collectFlagDefinitions(ApplicationIndexBuildItem index, List<PanacheEntityClassesBuildItem> panacheEntityClasses,
            BuildProducer<FlagDefinitionBuildItem> flagDefinition) {
        List<AnnotationInstance> flagDefinitions = index.getIndex().getAnnotations(DotName.createSimple(FlagDefinition.class));
        for (AnnotationInstance flagDefinitionAnnotation : flagDefinitions) {
            Set<String> panacheEntities = new HashSet<>();
            for (PanacheEntityClassesBuildItem entityClasses : panacheEntityClasses) {
                panacheEntities.addAll(entityClasses.getEntityClasses());
            }
            ClassInfo entityClass = flagDefinitionAnnotation.target().asClass();
            flagDefinition.produce(
                    new FlagDefinitionBuildItem(entityClass, panacheEntities.contains(entityClass.name().toString())));
        }
    }

}
