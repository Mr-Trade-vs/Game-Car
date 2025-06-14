package integrative.task.algorithms;

import integrative.task.structures.Queue;
import integrative.task.structures.Graph;
import integrative.task.structures.NodeGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BFS<T> {

    public static <T> List<NodeGraph<T>> traverse(Graph<T> graph, T startNodeData) {
        List<NodeGraph<T>> visited = new ArrayList<>();

        NodeGraph<T> startNodeGraph = graph.getNode(startNodeData);
        if (startNodeGraph == null) {
            return visited;
        }

        Queue<NodeGraph<T>> queue = new Queue<>();
        Set<T> visitedSet = new HashSet<>();

        queue.add(startNodeGraph);
        visitedSet.add(startNodeGraph.getData());

        while (!queue.isEmpty()) {
            NodeGraph<T> currentNodeGraph = queue.poll();
            visited.add(currentNodeGraph);

            // Get all neighbors from the current node
            Map<String, NodeGraph<T>> neighbors = graph.getNeighbors(currentNodeGraph);

            // Add all unvisited neighbors to the queue
            for (NodeGraph<T> neighbor : neighbors.values()) {
                if (!visitedSet.contains(neighbor.getData())) {
                    queue.add(neighbor);
                    visitedSet.add(neighbor.getData());
                }
            }
        }

        return visited;
    }

    public static <T> NodeGraph<T> getNextNode(Graph<T> graph, NodeGraph<T> currentNodeGraph, Set<NodeGraph<T>> visited) {
        if (currentNodeGraph == null) {
            return null;
        }

        // Get all neighbors from the current node
        Map<String, NodeGraph<T>> neighbors = graph.getNeighbors(currentNodeGraph);

        // Check each neighbor
        for (NodeGraph<T> neighbor : neighbors.values()) {
            if (!visited.contains(neighbor)) {
                return neighbor;
            }
        }

        return null; // No unvisited neighbors
    }
}
