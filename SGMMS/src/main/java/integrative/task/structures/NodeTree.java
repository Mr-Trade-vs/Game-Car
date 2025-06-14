package integrative.task.structures;

import integrative.task.model.Incident;

public class NodeTree {
    private Incident incident;
    private NodeTree left;
    private NodeTree right;

    public NodeTree(Incident incident) {
        this.incident = incident;
        this.left = null;
        this.right = null;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public NodeTree getLeft() {
        return left;
    }

    public void setLeft(NodeTree left) {
        this.left = left;
    }

    public NodeTree getRight() {
        return right;
    }

    public void setRight(NodeTree right) {
        this.right = right;
    }
}
