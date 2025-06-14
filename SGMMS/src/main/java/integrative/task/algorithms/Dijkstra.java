package integrative.task.algorithms;

import integrative.task.structures.Edge;
import integrative.task.structures.Graph;
import integrative.task.structures.MinHeap;
import integrative.task.structures.NodeGraph;
import integrative.task.structures.NodeGraphWithDistance;

import java.util.*;

public class Dijkstra<T> {

    public static <T> List<NodeGraph<T>> findShortestPath(Graph<T> graph, NodeGraph<T> sourceNodeGraph, NodeGraph<T> destinationNodeGraph) {
        if (sourceNodeGraph == null || destinationNodeGraph == null) {
            return new ArrayList<>();
        }

        // Map to store the shortest distance to each node
        Map<NodeGraph<T>, Integer> distances = new HashMap<>();

        // Map to store the previous node in the optimal path
        Map<NodeGraph<T>, NodeGraph<T>> previousNodes = new HashMap<>();

        // Set of nodes already processed
        Set<NodeGraph<T>> processed = new HashSet<>();

        // Priority queue to get the node with the smallest distance
        MinHeap<NodeGraphWithDistance<T>> priorityQueue = new MinHeap<>(
            Comparator.comparingInt(NodeGraphWithDistance::getDistance)
        );

        // Initialize distances
        for (NodeGraph<T> nodeGraph : graph.getAllNodes()) {
            distances.put(nodeGraph, nodeGraph.equals(sourceNodeGraph) ? 0 : Integer.MAX_VALUE);
        }

        // Add source node to the priority queue
        priorityQueue.add(new NodeGraphWithDistance<>(sourceNodeGraph, 0));

        // Process nodes
        while (!priorityQueue.isEmpty()) {
            NodeGraphWithDistance<T> current = priorityQueue.poll();
            NodeGraph<T> currentNodeGraph = current;

            // If we've reached the destination, we can stop
            if (currentNodeGraph.equals(destinationNodeGraph)) {
                break;
            }

            // Skip if we've already processed this node
            if (processed.contains(currentNodeGraph)) {
                continue;
            }

            // Mark as processed
            processed.add(currentNodeGraph);

            // Check all neighbors
            for (Edge<T> edge : graph.getEdges(currentNodeGraph)) {
                NodeGraph<T> neighbor = edge.getTarget();

                // Skip if already processed
                if (processed.contains(neighbor)) {
                    continue;
                }

                // Calculate new distance
                int newDistance = distances.get(currentNodeGraph) + edge.getWeight();

                // If we found a better path
                if (newDistance < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distances.put(neighbor, newDistance);
                    previousNodes.put(neighbor, currentNodeGraph);
                    priorityQueue.add(new NodeGraphWithDistance<>(neighbor, newDistance));
                }
            }
        }

        // Reconstruct the path
        List<NodeGraph<T>> path = new ArrayList<>();
        NodeGraph<T> current = destinationNodeGraph;

        // Check if a path exists
        if (!previousNodes.containsKey(destinationNodeGraph) && !sourceNodeGraph.equals(destinationNodeGraph)) {
            return path; // No path exists
        }

        // Add destination to path
        path.add(0, destinationNodeGraph);

        // Follow the chain of previous nodes
        while (previousNodes.containsKey(current)) {
            current = previousNodes.get(current);
            path.add(0, current);
        }

        return path;
    }
}
