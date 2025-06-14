package integrative.task.structures;

public class NodeGraphWithDistance<T> extends NodeGraph<T> {
    private int distance;

    public NodeGraphWithDistance(T data, double x, double y, int distance) {
        super(data, x, y);
        this.distance = distance;
    }

    public NodeGraphWithDistance(NodeGraph<T> nodeGraph, int distance) {
        super(nodeGraph.getData(), nodeGraph.getX(), nodeGraph.getY());
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}