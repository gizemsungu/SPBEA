package OldDPBEA;
import org.graphstream.graph.Node;

import java.util.ArrayList;

/**
 * Created by Gizem on 01.12.2017.
 */
public class DPBEAColor {

    private ArrayList<Node> nodes;

    public DPBEAColor(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public void addNode(Node node){
        nodes.add(node);
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }
}
