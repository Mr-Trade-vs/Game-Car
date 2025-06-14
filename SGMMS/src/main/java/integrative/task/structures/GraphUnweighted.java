package integrative.task.structures;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class GraphUnweighted<T> {
    private Map<NodeGraph<T>, List<EdgeUnweighted<T>>> adjacencyList;

    public GraphUnweighted() {
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
        List<EdgeUnweighted<T>> nodeEdges = adjacencyList.getOrDefault(nodeGraph, new ArrayList<>());

        for (EdgeUnweighted<T> edge : nodeEdges) {
            if (edge.getSource().equals(nodeGraph)) {
                // Convert EdgeUnweighted to Edge for interface compatibility
                edges.add(new Edge<>(edge.getSource(), edge.getTarget(), edge.getDirection(), 1));
            }
        }

        return edges;
    }

    public Map<String, NodeGraph<T>> getNeighbors(NodeGraph<T> nodeGraph) {
        Map<String, NodeGraph<T>> result = new HashMap<>();
        List<EdgeUnweighted<T>> edges = adjacencyList.getOrDefault(nodeGraph, new ArrayList<>());

        for (EdgeUnweighted<T> edge : edges) {
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
        List<EdgeUnweighted<T>> edges = adjacencyList.get(nodeGraph);

        if (edges != null) {
            for (EdgeUnweighted<T> edge : edges) {
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

        List<EdgeUnweighted<T>> edges = adjacencyList.get(nodeGraph);
        if (edges != null) {
            for (EdgeUnweighted<T> edge : edges) {
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
        List<EdgeUnweighted<T>> edges = adjacencyList.get(from);
        if (edges != null) {
            // Check if an edge with the same direction already exists
            for (int i = 0; i < edges.size(); i++) {
                EdgeUnweighted<T> edge = edges.get(i);
                if (edge.getSource().equals(from) && edge.getDirection().equals(direction)) {
                    // Replace the existing edge
                    edges.set(i, new EdgeUnweighted<>(from, to, direction));
                    return;
                }
            }
            // Add a new edge if no existing edge with the same direction was found
            edges.add(new EdgeUnweighted<>(from, to, direction));
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

        // Get all neighbors from the current node
        Map<String, NodeGraph<T>> neighbors = getNeighbors(currentNodeGraph);

        // Check each neighbor
        for (NodeGraph<T> neighbor : neighbors.values()) {
            if (!visited.contains(neighbor)) {
                return neighbor;
            }
        }

        return null; // No unvisited neighbors
    }

    public List<NodeGraph<T>> traverseBFS(T startNodeData) {
        List<NodeGraph<T>> visited = new ArrayList<>();

        NodeGraph<T> startNodeGraph = getNode(startNodeData);
        if (startNodeGraph == null) {
            return visited;
        }

        integrative.task.structures.Queue<NodeGraph<T>> queue = new integrative.task.structures.Queue<>();
        Set<T> visitedSet = new HashSet<>();

        queue.add(startNodeGraph);
        visitedSet.add(startNodeGraph.getData());

        while (!queue.isEmpty()) {
            NodeGraph<T> currentNodeGraph = queue.poll();
            visited.add(currentNodeGraph);

            // Get all neighbors from the current node
            Map<String, NodeGraph<T>> neighbors = getNeighbors(currentNodeGraph);

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
}
