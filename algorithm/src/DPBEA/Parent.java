package DPBEA;


import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.*;

public class Parent {
    private ArrayList<Color> colors;
    private HashMap<Node, Color> hashColors;
    public HashSet<Node> Ppool;

    public Parent() {
        this.colors = new ArrayList<>();
        this.hashColors = new HashMap<>();
        this.Ppool = new HashSet<>();
    }

    public ArrayList<Color> getColors() {
        return colors;
    }

    public void addColor(Color color){
        updateHash(1, color, color.getNodes());
        this.colors.add(color);
    }

    public void removeColor(Color color){
        if(!color.getNodes().isEmpty()){
            updateHash(2, color, color.getNodes());
        }
        this.colors.remove(color);
    }

    public void updateHash(int opType, Color color, Node node){
        if(opType == 1){ // add
            hashColors.put(node, color);
        }
        else if(opType == 2){//remove
            hashColors.remove(node);
        }
    }

    public void updateHash(int opType, Color color, HashSet<Node> nodes){
        if(opType == 1){ // add
            for ( Node node: nodes) {
                hashColors.put(node, color);
            }
        }
        else if(opType == 2){//remove
            hashColors.remove(nodes);
        }
    }

    public HashMap<Node, Color> getHashColors() {
        return hashColors;
    }

    public int numberofColors(){
        return colors.size();
    }

    public HashSet<Node> getPpool() {
        return Ppool;
    }
    public void addingPpool(HashSet<Node> addedNodes){
        this.Ppool.addAll(addedNodes);
    }

    public void removingNodes(ArrayList<Node> removedNodes){
        if(!removedNodes.isEmpty()){
            for (Node n: removedNodes) {
                for (Color color: getColors()) {
                    if(color.getNodes().contains(n)){
                        color.getNodes().remove(n);
                        updateHash(2, color, n);
                        break;
                    }
                }
            }

            removeEmptyColors();
        }
    }
    public void removeEmptyColors() {

        Iterator<Color> iterator = colors.iterator();
        while (iterator.hasNext()){
            Color color = iterator.next();
            if (color.getNodes().isEmpty()){
                iterator.remove();
            }
        }
    }

    public HashSet<Node> checkNodes(ArrayList<Edge> addedEdges){
        //Conflicting nodes are added to Ppool for edge dynamic graphs
        HashSet<Node> conflictingNodes = new HashSet<>();
        for (Edge e: addedEdges) {
            Color conflictColor = getColors().get(0);
            Node conflictNode = null;
            boolean found = false;
            for (Color color: getColors()) {
                HashSet<Node> nodes = color.getNodes();
                if(nodes.contains(e.getNode0()) && nodes.contains(e.getNode1())){
                  //  System.out.println("found");
                    found = true;
                    conflictColor = color;
                    conflictNode = e.getNode0();
                    break;
                }
            }
            if(found){
                // Add to conflictingnodes
                conflictColor.getNodes().remove(conflictNode);
                updateHash(2, conflictColor, conflictNode);
                conflictingNodes.add(conflictNode);
            }
        }
        return conflictingNodes;
    }

    public void addNodes(ArrayList<Node> addedNodes){
        Collections.shuffle(addedNodes);
        Random rand = DPBEAConst.RAND;
        for (Node node: addedNodes) {
            int colorIdx = rand.nextInt(getColors().size());
            Color color = getColors().get(colorIdx);
            color.addNode(node);
            updateHash(1, color, node);
        }
    }

    public void removeNodes(ArrayList<Node> removedNodes){
        for (Node node: removedNodes) {
            Color color = hashColors.get(node);
            color.getNodes().remove(node);
            updateHash(2, color, node);
        }
        removeEmptyColors();
    }
}
