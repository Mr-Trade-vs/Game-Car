package integrative.task.structures;

public class NodeGraph<T> {
    private T data;
    private double x, y;

    public NodeGraph(T data, double x, double y) {
        this.data = data;
        this.x = x;
        this.y = y;
    }

    public T getData() {
        return data;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        NodeGraph<?> nodeGraph = (NodeGraph<?>) obj;

        if (Double.compare(nodeGraph.x, x) != 0) return false;
        if (Double.compare(nodeGraph.y, y) != 0) return false;
        return data != null ? data.equals(nodeGraph.data) : nodeGraph.data == null;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        return result;
    }
}
