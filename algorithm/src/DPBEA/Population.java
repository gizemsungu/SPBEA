package DPBEA;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class Population {
    private ArrayList<Parent> parents;
    private Graph graph;
    public int [] colorStep = new int[DPBEAConst.eStepSize];
    public int sumInitialCross = 0;
    public int sumSimilarCross = 0;
    public int goodInitial = 0;
    public int goodSimilar = 0;

    public Population(Graph graph) {
        this.parents = new ArrayList<>();
        this.graph = graph;
    }

    public void initializePopulation(int popSize){
        for (int i = 0; i < popSize; i++) {
            Parent newParent = createParent();
            parents.add(newParent);
        }
    }



    public Parent createParent(){
        Parent parent = new Parent();
        ArrayList<Node> nodes = new ArrayList<>(this.graph.getNodeSet());
        Collections.shuffle(nodes);
        Random random = new Random();
        while (!nodes.isEmpty()){
            Node node = nodes.get(random.nextInt(nodes.size()));
            nodes.remove(node);
            if (parent.getColors().isEmpty()){
                Color newColor = new Color();
                newColor.getNodes().add(node);
                parent.addColor(newColor);
                parent.updateHash(1, newColor, newColor.getNodes());
            }
            else {
                boolean isPlaced = false;
                for (Color color: parent.getColors()) {
                    boolean CF = true;
                    HashSet<Node> nodes1 = color.getNodes();
                    for (Node n: nodes1) {
                        if(n.hasEdgeBetween(node)){
                            CF = false;
                            break;
                        }
                    }
                    if(CF){
                        nodes1.add(node);
                        parent.updateHash(1, color, node);
                        isPlaced = true;
                        break;
                    }
                }
                if(!isPlaced){
                    Color newColor = new Color();
                    newColor.getNodes().add(node);
                    parent.addColor(newColor);
                    parent.updateHash(1, newColor, newColor.getNodes());
                }
            }
        }
        return parent;
    }

    public ArrayList<Parent> getParents() {
        return parents;
    }

    public void updatePop(ArrayList<Edge> addedEdges){
        //Add the conflicting nodes to the Ppool after edge dynamic graph has been changed
        for (int i = 0; i < DPBEAConst.POPULATION ; i++) {
            parents.get(i).addingPpool(parents.get(i).checkNodes(addedEdges));
        }
    }

    public void removeNodes(ArrayList<Node> removedNodes){
        for (int i = 0; i < parents.size(); i++) {
            parents.get(i).removeNodes(removedNodes);
        }
    }

    public void addNodes(ArrayList<Node> addedNodes){
        for (int i = 0; i < parents.size(); i++) {
            parents.get(i).addNodes(addedNodes);
        }
    }
}
