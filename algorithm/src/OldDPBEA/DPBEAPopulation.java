package OldDPBEA;
import DPBEA.DGraph;
import DPBEA.DPBEAConst;
import monica.DynamicGeneticAlgorithm3;
import monica.Individual;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Gizem on 03.12.2017.
 */
public class DPBEAPopulation {

    private ArrayList<DPBEAParent> parents;
    private final DGraph dpbeaGraph;
    private final Graph graph;
    private int popType; //0:without Mutation 1:New Mutation 2:Old Mutation
    public int bestColor;
    public double bestFitness;
    public int firstIte;
    public double firstTime;
    public double [] fitStep = new double[DPBEAConst.eStepSize];
    public int [] colorStep = new int[DPBEAConst.eStepSize];

    /* Constructor */
    public DPBEAPopulation(DGraph dpbeaGraph, int popType) {
        this.parents = new ArrayList<>();
        this.dpbeaGraph = dpbeaGraph;
        graph = dpbeaGraph.getGraph();
        this.bestColor = Integer.MAX_VALUE;
        this.bestFitness = Double.MAX_VALUE;
        this.popType = popType;
    }

    public void initializePopulation(int numberofParents) {
        for (int i = 0; i < numberofParents; i++) {
            DPBEAParent newParent = createParent2();
            parents.add(newParent);
        }
    }

    public void initializePopulation2(int numberofParents) {
        for (int i = 0; i < numberofParents; i++) {
            DPBEAParent newParent = new DPBEAParent();
            graph.getNodeSet().forEach(node -> {
                ArrayList<Node> nodes = new ArrayList<>();
                nodes.add(node);
                DPBEAColor color = new DPBEAColor(nodes);
                newParent.addColor(color);
            });
            Collections.shuffle(newParent.getColors());
            parents.add(newParent);
        }
    }

    public DPBEAParent createParent2(){
        Random random = DPBEAConst.RAND;
        DynamicGeneticAlgorithm3 dsatur = new DynamicGeneticAlgorithm3(graph, random);
        Individual ind = dsatur.dsatur(graph);
        //System.out.println("ind: " + ind.toString());
        DPBEAParent newParent = new DPBEAParent();
        for (int vertex: ind) {
            Node node = graph.getNode(""+vertex);
            if(newParent.numberofColors() == 0){
                ArrayList<Node> nodes = new ArrayList<Node>();
                nodes.add(node);
                DPBEAColor color = new DPBEAColor(nodes);
                newParent.addColor(color);
            }
            else {
                boolean isPlace = true;
                for (DPBEAColor color: newParent.getColors()) {
                    isPlace = true;
                    for(Node node2 : color.getNodes()){
                        if(node.hasEdgeBetween(node2)){
                            isPlace = false;
                            break;
                        }
                    }
                    if (isPlace){
                        color.addNode(node);
                        break;
                    }
                }
                if(!isPlace){
                    ArrayList<Node> nodes = new ArrayList<Node>();
                    nodes.add(node);
                    DPBEAColor color = new DPBEAColor(nodes);
                    newParent.addColor(color);
                }

            }
        }
        Collections.shuffle(newParent.getColors());
        return newParent;
    }

    public DPBEAParent createParent(){
        DPBEAParent newParent = new DPBEAParent();
        graph.getNodeSet().forEach(node -> {
            if(newParent.numberofColors() == 0){
                ArrayList<Node> nodes = new ArrayList<Node>();
                nodes.add(node);
                DPBEAColor color = new DPBEAColor(nodes);
                newParent.addColor(color);
            }
            else {
                Collections.shuffle(newParent.getColors());
                boolean isPlace = true;
                for (DPBEAColor color: newParent.getColors()) {
                    isPlace = true;
                    for(Node node2 : color.getNodes()){
                        if(node.hasEdgeBetween(node2)){
                            isPlace = false;
                            break;
                        }
                    }
                    if (isPlace){
                        color.addNode(node);
                        break;
                    }
                }
                if(!isPlace){
                    ArrayList<Node> nodes = new ArrayList<Node>();
                    nodes.add(node);
                    DPBEAColor color = new DPBEAColor(nodes);
                    newParent.addColor(color);
                }
            }
        });
        return newParent;
    }

    public ArrayList<DPBEAParent> getParents(){
        return parents;
    }

    public void updatePop(ArrayList<Edge> addedEdges){
        //Create new color for conflicting node after egde dynamic graph has been changed
        this.bestColor = Integer.MAX_VALUE;
        this.bestFitness = Double.MAX_VALUE;
        for (int i = 0; i < DPBEAConst.POPULATION ; i++) {
            parents.get(i).checkNodes(addedEdges);
            Collections.shuffle(parents.get(i).getColors());
        }
    }

    public void updatePop2(ArrayList<Edge> addedEdges){
        //Add the conflicting nodes to the Ppool after edge dynamic graph has been changed
        this.bestColor = Integer.MAX_VALUE;
        this.bestFitness = Double.MAX_VALUE;
        for (int i = 0; i < DPBEAConst.POPULATION ; i++) {
            parents.get(i).addingPpool(parents.get(i).checkNodes2(addedEdges));
        }
    }

    public void updatePopForNodeDynamic(ArrayList<Node> addedNodes, ArrayList<Node> removedNodes){
        for (int i = 0; i < DPBEAConst.POPULATION ; i++) {
            parents.get(i).removingNodes(removedNodes);
            parents.get(i).addingPpool(addedNodes);
        }
        this.bestColor = Integer.MAX_VALUE;
        this.bestFitness = Double.MAX_VALUE;
    }

    public int getPopType() {
        return popType;
    }
}
