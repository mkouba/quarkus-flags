package io.quarkiverse.flags.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class TopologicalSortTest {

    @Test
    public void testNoEdges() {
        List<String> result = FlagsProcessor.topologicalSort(
                Set.of("a", "b", "c"),
                Map.of(),
                Map.of());
        // Alphabetical tiebreaker ensures deterministic order
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    public void testSingleBeforeEdge() {
        // "a" before "b" means a comes first
        List<String> result = FlagsProcessor.topologicalSort(
                Set.of("a", "b"),
                Map.of("b", List.of("a")),
                Map.of());
        assertEquals(List.of("b", "a"), result);
    }

    @Test
    public void testSingleAfterEdge() {
        // "b" after "a" means a comes first
        List<String> result = FlagsProcessor.topologicalSort(
                Set.of("a", "b"),
                Map.of(),
                Map.of("a", List.of("b")));
        assertEquals(List.of("b", "a"), result);
    }

    @Test
    public void testChain() {
        // a before b, b before c
        List<String> result = FlagsProcessor.topologicalSort(
                Set.of("a", "b", "c"),
                Map.of("a", List.of("b"), "b", List.of("c")),
                Map.of());
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    public void testMixedBeforeAndAfter() {
        // "in-memory" before "config", "hibernate" after "in-memory" and before "config"
        List<String> result = FlagsProcessor.topologicalSort(
                Set.of("in-memory", "hibernate", "config"),
                Map.of("in-memory", List.of("config"), "hibernate", List.of("config")),
                Map.of("hibernate", List.of("in-memory")));
        assertEquals(List.of("in-memory", "hibernate", "config"), result);
    }

    @Test
    public void testCycleDetected() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            FlagsProcessor.topologicalSort(
                    Set.of("a", "b"),
                    Map.of("a", List.of("b"), "b", List.of("a")),
                    Map.of());
        });
        assertTrue(e.getMessage().contains("Cycle detected"));
    }

    @Test
    public void testCycleInThreeNodes() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            FlagsProcessor.topologicalSort(
                    Set.of("a", "b", "c"),
                    Map.of("a", List.of("b"), "b", List.of("c"), "c", List.of("a")),
                    Map.of());
        });
        assertTrue(e.getMessage().contains("Cycle detected"));
    }

    @Test
    public void testBeforeNonExistentProvider() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            FlagsProcessor.topologicalSort(
                    Set.of("a"),
                    Map.of("a", List.of("nonexistent")),
                    Map.of());
        });
        assertTrue(e.getMessage().contains("nonexistent"));
    }

    @Test
    public void testAfterNonExistentProvider() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            FlagsProcessor.topologicalSort(
                    Set.of("a"),
                    Map.of(),
                    Map.of("a", List.of("nonexistent")));
        });
        assertTrue(e.getMessage().contains("nonexistent"));
    }

    @Test
    public void testSingleNode() {
        List<String> result = FlagsProcessor.topologicalSort(
                Set.of("a"),
                Map.of(),
                Map.of());
        assertEquals(List.of("a"), result);
    }

    @Test
    public void testEmptySet() {
        List<String> result = FlagsProcessor.topologicalSort(
                Set.of(),
                Map.of(),
                Map.of());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDiamondShape() {
        // a -> b, a -> c, b -> d, c -> d
        List<String> result = FlagsProcessor.topologicalSort(
                Set.of("b", "a", "c", "d"),
                Map.of("a", List.of("b", "c"), "b", List.of("d"), "c", List.of("d")),
                Map.of());
        // b and c are alphabetically tiebroken
        assertEquals(List.of("a", "b", "c", "d"), result);
    }

}
