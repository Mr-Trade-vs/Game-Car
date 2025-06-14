package integrative.task.structures;

import integrative.task.model.Incident;
import integrative.task.model.TypeIncident;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class BinaryTreeIncident {
    private NodeTree root;
    private Comparator<Incident> comparator;

    public BinaryTreeIncident(Comparator<Incident> comparator) {
        this.root = null;
        this.comparator = comparator;
    }

    public NodeTree getRoot() {
        return root;
    }

    public void setRoot(NodeTree root) {
        this.root = root;
    }

    public void insert(Incident incident) {
        root = insertRecursive(root, incident);
    }

    private NodeTree insertRecursive(NodeTree nodeTree, Incident incident) {
        if (nodeTree == null) return new NodeTree(incident);
        else if (comparator.compare(incident, nodeTree.getIncident()) < 0)
            nodeTree.setLeft(insertRecursive(nodeTree.getLeft(), incident));
         else if (comparator.compare(incident, nodeTree.getIncident()) > 0)
            nodeTree.setRight(insertRecursive(nodeTree.getRight(), incident));
        return nodeTree;
    }

    public NodeTree find(Incident incident) {
        return findRecursive(root, incident);
    }

    private NodeTree findRecursive(NodeTree nodeTree, Incident incident) {
        if (nodeTree == null) return null;
        else if (comparator.compare(incident, nodeTree.getIncident()) < 0)
            return findRecursive(nodeTree.getLeft(), incident);
        else if (comparator.compare(incident, nodeTree.getIncident()) > 0)
            return findRecursive(nodeTree.getRight(), incident);
        else return nodeTree;
    }

    public void remove(Incident incident) {
        root = removeRecursive(root, incident);
    }

    private NodeTree removeRecursive(NodeTree nodeTree, Incident incident) {
        if (nodeTree == null) return null;
        else if (comparator.compare(incident, nodeTree.getIncident()) < 0)
            nodeTree.setLeft(removeRecursive(nodeTree.getLeft(), incident));
         else if (comparator.compare(incident, nodeTree.getIncident()) > 0)
            nodeTree.setRight(removeRecursive(nodeTree.getRight(), incident));
         else {
             if (nodeTree.getLeft() == null) return nodeTree.getRight();
             if (nodeTree.getRight() == null) return nodeTree.getLeft();
             NodeTree min = findMin(nodeTree.getRight());
             nodeTree.setIncident(min.getIncident());
             nodeTree.setRight(removeRecursive(nodeTree.getRight(), min.getIncident()));
        }
         return nodeTree;
    }

    private NodeTree findMin(NodeTree nodeTree) {
        while (nodeTree.getLeft() != null) nodeTree = nodeTree.getLeft();
        return nodeTree;
    }

    public int countIncidents() {
        return countIncidentsRecursive(root);
    }

    private int countIncidentsRecursive(NodeTree node) {
        if (node == null) {
            return 0;
        }
        return 1 + countIncidentsRecursive(node.getLeft()) + countIncidentsRecursive(node.getRight());
    }

    public Map<TypeIncident, Integer> countIncidentsByType() {
        Map<TypeIncident, Integer> counts = new HashMap<>();
        // Initialize counts for all incident types
        for (TypeIncident type : TypeIncident.values()) {
            counts.put(type, 0);
        }
        countIncidentsByTypeRecursive(root, counts);
        return counts;
    }

    private void countIncidentsByTypeRecursive(NodeTree node, Map<TypeIncident, Integer> counts) {
        if (node == null) {
            return;
        }

        // Count this node's incident type
        TypeIncident type = node.getIncident().getTypeIncident();
        counts.put(type, counts.get(type) + 1);

        // Recursively count left and right subtrees
        countIncidentsByTypeRecursive(node.getLeft(), counts);
        countIncidentsByTypeRecursive(node.getRight(), counts);
    }
}
