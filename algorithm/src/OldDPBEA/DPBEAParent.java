package OldDPBEA;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Çağatay on 01.03.2017.
 */
public class DPBEAParent {

    private ArrayList<DPBEAColor> colors;
    private double fitness;
    private ArrayList<Node> Ppool;

    public DPBEAParent() {

        this.colors = new ArrayList<>();
        this.Ppool = new ArrayList<>();
    }

    public ArrayList<DPBEAColor> getColors() {
        return colors;
    }

    public void addColor(DPBEAColor color){
        this.colors.add(color);
    }

    public int numberofColors(){
        return colors.size();
    }

    public DPBEAColor getColor(int idx){
        return colors.get(idx);
    }


    public void removeEmptyColors() {

        Iterator<DPBEAColor> iterator = colors.iterator();
        while (iterator.hasNext()){
            DPBEAColor color = iterator.next();
            if (color.getNodes().isEmpty()){
                iterator.remove();
            }
        }
    }

    public ArrayList<Node> getPpool(){
        return this.Ppool;
    }

    public void checkNodes(ArrayList<Edge> addedEdges){
        for (Edge e: addedEdges) {
            DPBEAColor conflictColor = getColor(0);
            Node conflictNode = null;
            boolean found = false;
            for (DPBEAColor color: getColors()) {
                ArrayList<Node> nodes = color.getNodes();
                if(nodes.contains(e.getNode0()) && nodes.contains(e.getNode1())){
                    found = true;
                    conflictColor = color;
                    conflictNode = e.getNode0();
                    break;
                }
            }
            if(found){
                // Create new color
                conflictColor.getNodes().remove(conflictNode);
                ArrayList<Node> nodes = new ArrayList<>();
                nodes.add(conflictNode);
                DPBEAColor newColor = new DPBEAColor(nodes);
                addColor(newColor);
            }
        }
    }

    public ArrayList<Node> checkNodes2(ArrayList<Edge> addedEdges){
        //Conflicting nodes are added to Ppool for edge dynamic graphs
        ArrayList<Node> conflictingNodes = new ArrayList<>();
        for (Edge e: addedEdges) {
            DPBEAColor conflictColor = getColor(0);
            Node conflictNode = null;
            boolean found = false;
            for (DPBEAColor color: getColors()) {
                ArrayList<Node> nodes = color.getNodes();
                if(nodes.contains(e.getNode0()) && nodes.contains(e.getNode1())){
                    found = true;
                    conflictColor = color;
                    conflictNode = e.getNode0();
                    break;
                }
            }
            if(found){
                // Add to conflictingnodes
                conflictColor.getNodes().remove(conflictNode);
                conflictingNodes.add(conflictNode);
            }
        }
        return conflictingNodes;
    }

    public void addingPpool(ArrayList<Node> addedNodes){
        this.Ppool.addAll(addedNodes);
    }

    public void removePpool(Node node){
        this.Ppool.remove(node);
    }

    public int verifyNodeSize(){
        int sum = 0;
        for (DPBEAColor color: this.colors) {
            sum += color.getNodes().size();
        }
        return sum;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public void removingNodes(ArrayList<Node> removedNodes){
        if(!removedNodes.isEmpty()){
            for (Node n: removedNodes) {
                for (DPBEAColor color: getColors()) {
                    if(color.getNodes().contains(n)){
                        color.getNodes().remove(n);
                        break;
                    }
                }
            }

            removeEmptyColors();
        }
    }

}
