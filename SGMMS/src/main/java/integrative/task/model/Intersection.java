package integrative.task.model;

import integrative.task.structures.Graph;
import integrative.task.structures.NodeGraph;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Intersection {
    private String name;
    private NodeGraph<Intersection> topLeft, topRight, bottomLeft, bottomRight;
    private double x, y, width, height;

    public Intersection(String name) {
        this.name = name;
    }

    public void defineReferences(double x, double y, double width, double height, Graph<Intersection> graph) {
        // Store the intersection's bounds
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Create 4 nodes for each intersection (one for each corner)
        topLeft = new NodeGraph<>(this, x, y);
        topRight = new NodeGraph<>(this, x + width, y);
        bottomLeft = new NodeGraph<>(this, x, y + height);
        bottomRight = new NodeGraph<>(this, x + width, y + height);

        // Add nodes to the graph
        graph.addNode(topLeft);
        graph.addNode(topRight);
        graph.addNode(bottomLeft);
        graph.addNode(bottomRight);

        // Connect the nodes in a counter-clockwise direction: N1 → N4 → N3 → N2 → N1
        // N1 (topLeft) → N4 (bottomLeft)
        graph.connect(topLeft, "down", bottomLeft);

        // N4 (bottomLeft) → N3 (bottomRight)
        graph.connect(bottomLeft, "right", bottomRight);

        // N3 (bottomRight) → N2 (topRight)
        graph.connect(bottomRight, "up", topRight);

        // N2 (topRight) → N1 (topLeft)
        graph.connect(topRight, "left", topLeft);
    }

    public void defineReferences(double x, double y, double width, double height) {
        // Store the intersection's bounds
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Create 4 nodes for each intersection (one for each corner)
        topLeft = new NodeGraph<>(this, x, y);
        topRight = new NodeGraph<>(this, x + width, y);
        bottomLeft = new NodeGraph<>(this, x, y + height);
        bottomRight = new NodeGraph<>(this, x + width, y + height);
    }

    public void draw(GraphicsContext gc, double offsetX, double offsetY) {
        gc.setFill(Color.BLUE);
        double size = 6;

        drawNode(gc, topLeft, offsetX, offsetY, size);
        drawNode(gc, topRight, offsetX, offsetY, size);
        drawNode(gc, bottomLeft, offsetX, offsetY, size);
        drawNode(gc, bottomRight, offsetX, offsetY, size);
    }

    private void drawNode(GraphicsContext gc, NodeGraph<Intersection> nodeGraph, double offsetX, double offsetY, double size) {
        gc.fillOval(nodeGraph.getX() + offsetX - size / 2, nodeGraph.getY() + offsetY - size / 2, size, size);
    }

    public NodeGraph<Intersection> getNode(int index) {
        return switch (index) {
            case 0 -> topLeft;
            case 1 -> topRight;
            case 2 -> bottomLeft;
            case 3 -> bottomRight;
            default -> null;
        };
    }

    public NodeGraph<Intersection>[] getAllNodes() {
        return new NodeGraph[]{topLeft, topRight, bottomLeft, bottomRight};
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeGraph<Intersection> getTopLeft() {
        return topLeft;
    }

    public NodeGraph<Intersection> getTopRight() {
        return topRight;
    }

    public NodeGraph<Intersection> getBottomLeft() {
        return bottomLeft;
    }

    public NodeGraph<Intersection> getBottomRight() {
        return bottomRight;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX <= x + width && 
               pointY >= y && pointY <= y + height;
    }
}
