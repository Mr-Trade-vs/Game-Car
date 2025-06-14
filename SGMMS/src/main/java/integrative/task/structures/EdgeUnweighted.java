package integrative.task.structures;

public class EdgeUnweighted<T> {
    private NodeGraph<T> source;
    private NodeGraph<T> target;
    private String direction; // "up", "down", "left", "right" por ejemplo

    public EdgeUnweighted(NodeGraph<T> source, NodeGraph<T> target, String direction) {
        this.source = source;
        this.target = target;
        this.direction = direction;
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

    public void setDirection(String direction) {
        this.direction = direction;
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
