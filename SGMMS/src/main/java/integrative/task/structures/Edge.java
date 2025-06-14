package integrative.task.structures;

public class Edge<T> {
    private NodeGraph<T> source;
    private NodeGraph<T> target;
    private String direction; // "up", "down", "left", "right" por ejemplo
    private int weight; // peso para distancias, tiempos, etc.

    public Edge(NodeGraph<T> source, NodeGraph<T> target, String direction, int weight) {
        this.source = source;
        this.target = target;
        this.direction = direction;
        this.weight = weight;
    }

    public NodeGraph<T> getSource() {
        return source;
    }

    public NodeGraph<T> getTarget() {
        return target;
    }

    public String getDirection() {
        return direction;
    }

    public int getWeight() {
        return weight;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean connects(NodeGraph<T> nodeGraph) {
        return source.equals(nodeGraph) || target.equals(nodeGraph);
    }

    public NodeGraph<T> getOtherNode(NodeGraph<T> nodeGraph) {
        if (source.equals(nodeGraph)) return target;
        else if (target.equals(nodeGraph)) return source;
        else return null;
    }
}
