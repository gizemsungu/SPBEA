package DPBEA;

import org.graphstream.graph.Node;

import java.util.HashSet;

public class Color {
    private HashSet<Node> nodes;

    public Color() {
        this.nodes = new HashSet<>();
    }

    public HashSet<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node node){
        this.nodes.add(node);
    }
}
