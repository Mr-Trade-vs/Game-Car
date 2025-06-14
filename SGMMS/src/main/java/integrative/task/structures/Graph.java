package integrative.task.structures;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Comparator;

public class Graph<T> { //Este debe implemntar Dikstra
    private Map<NodeGraph<T>, List<Edge<T>>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    public void addNode(NodeGraph<T> nodeGraph) {
        adjacencyList.put(nodeGraph, new ArrayList<>());
    }

    public NodeGraph<T> getNode(T data) {
        for (NodeGraph<T> nodeGraph : adjacencyList.keySet()) {
            if (nodeGraph.getData().equals(data)) {
                return nodeGraph;
            }
        }
        return null;
    }

    public List<NodeGraph<T>> getAllNodes() {
        return new ArrayList<>(adjacencyList.keySet());
    }

    public List<Edge<T>> getEdges(NodeGraph<T> nodeGraph) {
        List<Edge<T>> edges = new ArrayList<>();
        List<Edge<T>> nodeEdges = adjacencyList.getOrDefault(nodeGraph, new ArrayList<>());

        for (Edge<T> edge : nodeEdges) {
            if (edge.getSource().equals(nodeGraph)) {
                edges.add(edge);
            }
        }

        return edges;
    }

    public Map<String, NodeGraph<T>> getNeighbors(NodeGraph<T> nodeGraph) {
        Map<String, NodeGraph<T>> result = new HashMap<>();
        List<Edge<T>> edges = adjacencyList.getOrDefault(nodeGraph, new ArrayList<>());

        for (Edge<T> edge : edges) {
            if (edge.getSource().equals(nodeGraph)) {
                result.put(edge.getDirection(), edge.getTarget());
            }
        }

        return result;
    }

    public Set<String> getDirections(NodeGraph<T> nodeGraph) {
        if (nodeGraph == null) {
            return Set.of(); // Return empty set if node is null
        }

        Set<String> directions = new HashSet<>();
        List<Edge<T>> edges = adjacencyList.get(nodeGraph);

        if (edges != null) {
            for (Edge<T> edge : edges) {
                if (edge.getSource().equals(nodeGraph)) {
                    directions.add(edge.getDirection());
                }
            }
        }

        return directions;
    }

    public NodeGraph<T> getNeighbor(NodeGraph<T> nodeGraph, String direction) {
        if (nodeGraph == null || direction == null) {
            return null;
        }

        List<Edge<T>> edges = adjacencyList.get(nodeGraph);
        if (edges != null) {
            for (Edge<T> edge : edges) {
                if (edge.getSource().equals(nodeGraph) && edge.getDirection().equals(direction)) {
                    return edge.getTarget();
                }
            }
        }

        return null;
    }

    public void connectNodes(T fromData, String direction, T toData) {
        NodeGraph<T> from = getNode(fromData);
        NodeGraph<T> to = getNode(toData);
        if (from != null && to != null) {
            connect(from, direction, to);
        }
    }

    public void connect(NodeGraph<T> from, String direction, NodeGraph<T> to) {
        // Determine if this is an internal or external connection
        int weight = 1; // Default weight for internal connections

        // If the nodes belong to different data objects, it's an external connection
        if (from.getData() != to.getData()) {
            weight = 4; // Weight for external connections
        }

        // Call the overloaded connect method with the determined weight
        connect(from, direction, to, weight);
    }

    public void connect(NodeGraph<T> from, String direction, NodeGraph<T> to, int weight) {
        List<Edge<T>> edges = adjacencyList.get(from);
        if (edges != null) {
            // Check if an edge with the same direction already exists
            for (int i = 0; i < edges.size(); i++) {
                Edge<T> edge = edges.get(i);
                if (edge.getSource().equals(from) && edge.getDirection().equals(direction)) {
                    // Replace the existing edge
                    edges.set(i, new Edge<>(from, to, direction, weight));
                    return;
                }
            }
            // Add a new edge if no existing edge with the same direction was found
            edges.add(new Edge<>(from, to, direction, weight));
        }
    }

    public void connectNodesBidirectional(T fromData, String direction, T toData) {
        // Connect in the specified direction
        connectNodes(fromData, direction, toData);

        // Connect in the opposite direction
        String oppositeDirection = getOppositeDirection(direction);
        if (oppositeDirection != null) {
            connectNodes(toData, oppositeDirection, fromData);
        }
    }

    private String getOppositeDirection(String direction) {
        if (direction == null) {
            return null;
        }

        return switch (direction) {
            case "up" -> "down";
            case "down" -> "up";
            case "left" -> "right";
            case "right" -> "left";
            case "topLeft" -> "bottomRight";
            case "topRight" -> "bottomLeft";
            case "bottomLeft" -> "topRight";
            case "bottomRight" -> "topLeft";
            default -> null;
        };
    }

    public NodeGraph<T> getNextNode(NodeGraph<T> currentNodeGraph, Set<NodeGraph<T>> visited) {
        if (currentNodeGraph == null) {
            return null;
        }

        // Get all edges from the current node
        List<Edge<T>> edges = getEdges(currentNodeGraph);

        // Use a priority queue to get the neighbor with the lowest weight
        PriorityQueue<Edge<T>> priorityQueue = new PriorityQueue<>(
            Comparator.comparingInt(Edge::getWeight)
        );

        // Add all unvisited neighbors to the priority queue
        for (Edge<T> edge : edges) {
            NodeGraph<T> neighbor = edge.getTarget();
            if (!visited.contains(neighbor)) {
                priorityQueue.add(edge);
            }
        }

        // Return the neighbor with the lowest weight, or null if there are no unvisited neighbors
        return priorityQueue.isEmpty() ? null : priorityQueue.poll().getTarget();
    }
}
