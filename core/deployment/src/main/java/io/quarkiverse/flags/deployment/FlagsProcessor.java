package io.quarkiverse.flags.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import jakarta.inject.Singleton;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

import io.quarkiverse.flags.CompositeFlagEvaluator;
import io.quarkiverse.flags.Feature;
import io.quarkiverse.flags.TimeSpanFlagEvaluator;
import io.quarkiverse.flags.VariantFlagEvaluator;
import io.quarkiverse.flags.runtime.impl.ConfigFlagProvider;
import io.quarkiverse.flags.runtime.impl.FlagContext;
import io.quarkiverse.flags.runtime.impl.FlagManagerImpl;
import io.quarkiverse.flags.runtime.impl.FlagsRecorder;
import io.quarkiverse.flags.runtime.impl.InMemoryFlagProviderImpl;
import io.quarkiverse.flags.spi.ComponentOrder;
import io.quarkiverse.flags.spi.FlagEvaluator;
import io.quarkiverse.flags.spi.FlagProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanDiscoveryFinishedBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.arc.processor.InjectionPointInfo;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.runtime.RuntimeValue;

public class FlagsProcessor {

    static final DotName FEATURE = DotName.createSimple(Feature.class);
    static final DotName IDENTIFIER = DotName.createSimple("io.smallrye.common.annotation.Identifier");
    static final DotName FLAG_PROVIDER_ORDER = DotName.createSimple(ComponentOrder.class);

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem("feature-flags");
    }

    @BuildStep
    void beans(BuildProducer<AdditionalBeanBuildItem> beans) {
        beans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClasses(FlagManagerImpl.class, ConfigFlagProvider.class,
                        InMemoryFlagProviderImpl.class, TimeSpanFlagEvaluator.class, CompositeFlagEvaluator.class,
                        VariantFlagEvaluator.class)
                .build());
    }

    @BuildStep
    FlagProviderInfoBuildItem validateAndOrderProviders(BeanDiscoveryFinishedBuildItem beanDiscovery) {
        // Validate @Feature injection points
        for (InjectionPointInfo injectionPoint : beanDiscovery.getInjectionPoints()) {
            AnnotationInstance feature = injectionPoint.getRequiredQualifier(FEATURE);
            if (feature != null) {
                AnnotationValue value = feature.value();
                if (value != null && value.asString().isEmpty()) {
                    throw new IllegalStateException(
                            "@Feature with an empty value is not allowed: " + injectionPoint.getTargetInfo());
                }
            }
        }

        // Discover and validate FlagProvider beans
        Map<String, List<String>> beforeEdges = new HashMap<>(); // id -> list of ids it must come before
        Map<String, List<String>> afterEdges = new HashMap<>(); // id -> list of ids it must come after
        Set<String> allProviderIds = new HashSet<>();

        for (BeanInfo bean : beanDiscovery.beanStream().withBeanType(FlagProvider.class)) {
            String id = readIdentifier(bean, "FlagProvider");
            if (!allProviderIds.add(id)) {
                throw new IllegalStateException(
                        "Multiple flag providers with the same @Identifier value detected: " + id);
            }

            // Read @ComponentOrder from the bean target
            // For class-based beans the target is a ClassInfo, for producer beans it's a MethodInfo or FieldInfo
            if (bean.getTarget().isPresent()) {
                AnnotationInstance orderAnnotation = bean.getTarget().get().declaredAnnotation(FLAG_PROVIDER_ORDER);
                if (orderAnnotation != null) {
                    AnnotationValue beforeValue = orderAnnotation.value("before");
                    if (beforeValue != null) {
                        beforeEdges.put(id, List.of(beforeValue.asStringArray()));
                    }
                    AnnotationValue afterValue = orderAnnotation.value("after");
                    if (afterValue != null) {
                        afterEdges.put(id, List.of(afterValue.asStringArray()));
                    }
                }
            }
        }

        // Build the DAG and topological sort
        List<String> orderedIds = topologicalSort(allProviderIds, beforeEdges, afterEdges);

        // Discover and validate FlagEvaluator beans
        Set<String> allEvaluatorIds = new HashSet<>();

        for (BeanInfo bean : beanDiscovery.beanStream().withBeanType(FlagEvaluator.class)) {
            String id = readIdentifier(bean, "FlagEvaluator");
            if (!allEvaluatorIds.add(id)) {
                throw new IllegalStateException(
                        "Multiple flag evaluators with the same @Identifier value detected: " + id);
            }
        }

        return new FlagProviderInfoBuildItem(orderedIds, beforeEdges, afterEdges);
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    SyntheticBeanBuildItem registerFlagContext(FlagsRecorder recorder, FlagProviderInfoBuildItem providerInfo) {
        RuntimeValue<FlagContext> configValue = recorder.createContext(
                providerInfo.getOrderedProviderIds());
        return SyntheticBeanBuildItem.configure(FlagContext.class)
                .scope(Singleton.class)
                .runtimeValue(configValue)
                .done();
    }

    private String readIdentifier(BeanInfo bean, String spiName) {
        Optional<AnnotationInstance> identifier = bean.getQualifier(IDENTIFIER);
        if (identifier.isEmpty()) {
            throw new IllegalStateException(
                    spiName + " bean " + bean.getBeanClass()
                            + " must be annotated with @io.smallrye.common.annotation.Identifier");
        }
        AnnotationValue value = identifier.get().value();
        if (value == null || value.asString().isBlank()) {
            throw new IllegalStateException(
                    spiName + " bean " + bean.getBeanClass()
                            + " has an @Identifier annotation with an empty value");
        }
        return value.asString();
    }

    /**
     * Performs a topological sort of flag provider IDs using Kahn's algorithm.
     * <p>
     * An edge from A to B means A has higher priority and is processed before B.
     *
     * @param allIds the set of all provider IDs (values of {@code @Identifier})
     * @param beforeEdges a map where the key is a provider ID and the value is a list of provider IDs that must come
     *        after it (i.e. the key has higher priority); derived from {@code @ComponentOrder(before = ...)}
     * @param afterEdges a map where the key is a provider ID and the value is a list of provider IDs that must come
     *        before it (i.e. the key has lower priority); derived from {@code @ComponentOrder(after = ...)}
     * @return the sorted list of provider IDs, highest priority first
     * @throws IllegalStateException if a cycle is detected or if a referenced provider ID does not exist
     */
    static List<String> topologicalSort(Set<String> allIds, Map<String, List<String>> beforeEdges,
            Map<String, List<String>> afterEdges) {
        // Build adjacency list: edge from A to B means A comes before B
        Map<String, Set<String>> graph = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        for (String id : allIds) {
            graph.put(id, new HashSet<>());
            inDegree.put(id, 0);
        }

        // "A before B" means edge A -> B
        for (Map.Entry<String, List<String>> entry : beforeEdges.entrySet()) {
            String from = entry.getKey();
            for (String to : entry.getValue()) {
                if (!allIds.contains(to)) {
                    continue;
                }
                if (graph.get(from).add(to)) {
                    inDegree.merge(to, 1, Integer::sum);
                }
            }
        }

        // "A after B" means edge B -> A
        for (Map.Entry<String, List<String>> entry : afterEdges.entrySet()) {
            String to = entry.getKey();
            for (String from : entry.getValue()) {
                if (!allIds.contains(from)) {
                    continue;
                }
                if (graph.get(from).add(to)) {
                    inDegree.merge(to, 1, Integer::sum);
                }
            }
        }

        // Kahn's algorithm with alphabetical tiebreaker for deterministic results
        PriorityQueue<String> queue = new PriorityQueue<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String node = queue.poll();
            sorted.add(node);
            for (String neighbor : graph.get(node)) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);
                if (newDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (sorted.size() != allIds.size()) {
            Set<String> remaining = new HashSet<>(allIds);
            remaining.removeAll(sorted);
            throw new IllegalStateException(
                    "Cycle detected in @ComponentOrder declarations involving providers: " + remaining);
        }

        return sorted;
    }

}
