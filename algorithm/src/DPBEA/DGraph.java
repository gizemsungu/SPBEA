package DPBEA;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class DGraph {
    private final Graph graph;
    private final String graphName = "DPBEA Graph";
    private final Random rand;
    private final double nodeProb, edgeProb, edgeDensity;
    private final int initGraphSize;
    public int currentNodeID;
    private boolean added;
   // protected Graph completeGraph;

    public DGraph(int GRAPH_SIZE, double NODE_PROB, double EDGE_PROB, double EDGE_DENSITY) {
        super();
        this.initGraphSize = GRAPH_SIZE;
        this.nodeProb = NODE_PROB;
        this.edgeProb = EDGE_PROB;
        this.edgeDensity = EDGE_DENSITY;
        this.rand = new Random();
        this.added = true;
      //  this.completeGraph = new SingleGraph("ReverseGraph");
        this.graph = initializeGraph();

    }
    private Graph readGraph(){
        Graph graph = new SingleGraph(graphName);
        graph = Util.readFileStatic("GEOM20.col.txt", graph);
        //     initialEdge(graph);
        return graph;
    }
    private Graph initializeGraph(){
        Graph graph = new SingleGraph(graphName);
        for (int i = 0; i < initGraphSize; i++) {
            createNewNode(graph);
        }
   //     initialEdge(graph);
        return graph;
    }

    private Node createNewNode(Graph graph) {
       // currentNodeID = graph.getNodeCount()+1;
        Node node = graph.addNode("" + currentNodeID);

        node.setAttribute("ui.label", node.getId());
        node.setAttribute("color", 0);

    //    double prob = edgeDensity*edgeProb/(1-edgeDensity);
        /*This part for node dynamic graphs*/
        graph.getNodeSet().forEach(n -> {
            if ((rand.nextDouble() < edgeDensity) && !(n.getId().equals("" + currentNodeID))){
                graph.addEdge(currentNodeID + "," + n.getId(), "" + currentNodeID, n.getId());
            }
        });
        /*This part for node dynamic graphs*/

        currentNodeID++;
        return node; /* Eklenen node'u dondurur */
    }


    public ArrayList<Node> removeNodes(){
        ArrayList<Node> removedNodes = new ArrayList<>();
        graph.getNodeSet().forEach(node -> {
            if(rand.nextDouble() < nodeProb){
                removedNodes.add(node);
            }
        });
        graph.getNodeSet().removeAll(removedNodes);
        return removedNodes;
    }

    public ArrayList<Node> addNodes(){
        ArrayList<Node> addedNodes = new ArrayList<>();
        int min = (int)(initGraphSize * nodeProb * (1 - nodeProb));
        int max = (int)(initGraphSize * nodeProb * (1 + nodeProb));
        int nodeSize = rand.nextInt((max - min) + 1) + min;
        for (int i = 0; i < nodeSize ; i++) {
            addedNodes.add(createNewNode(graph));
        }
        return addedNodes;
    }

    public ArrayList<Node> addNodes2(){
        ArrayList<Node> addedNodes = new ArrayList<>();
        for (int i = 0; i < 50 ; i++) {
            addedNodes.add(createNewNode(graph));
        }
        return addedNodes;
    }

    private void initialEdge(Graph graph){
        int numberOfEdges = (int)( initGraphSize * (initGraphSize -1) * edgeDensity / 2 );
        for (int i = 0; i < numberOfEdges ; i++) {
            Edge newEdge = null;
            while (newEdge == null){
                newEdge = createNewEdge(graph);
            }
        }
    }

    public ArrayList<Edge> removeEdges(){
        ArrayList<Edge> removedEdges = new ArrayList<>();
        graph.getEdgeSet().forEach(edge -> {
            if(rand.nextDouble() < edgeProb){
                removedEdges.add(edge);
            }
        });
        graph.getEdgeSet().removeAll(removedEdges);
        return removedEdges;
    }
    public ArrayList<Edge> addEdges(){
        ArrayList<Edge> addedEdges = new ArrayList<>();
        double prob = edgeDensity*edgeProb/(1-edgeDensity);
        for (int i = 0; i < graph.getNodeCount()-1 ; i++) {
            for (int j = i+1; j < graph.getNodeCount(); j++) {
                if(rand.nextDouble() < prob && !graph.getNode(i).hasEdgeBetween(graph.getNode(j).getId())){
                    Edge edge = graph.addEdge(graph.getNode(i).getId() + "," + graph.getNode(j).getId(), "" + graph.getNode(i).getId(), graph.getNode(j).getId());
                    addedEdges.add(edge);
                }
            }
        }

        return addedEdges;
    }

    public ArrayList<Edge> addEdges2(){
        ArrayList<Edge> addedEdges = new ArrayList<>();
        int addedEdgeSize = (int)(edgeDensity * initGraphSize * (initGraphSize - 1) / 2);
        int i = 0;
        while (i <= addedEdgeSize && added) {
            if(graph.getEdgeCount() == initGraphSize * (initGraphSize - 1) / 2){
                added = false;
                break;
            }
            int idx1 = rand.nextInt(initGraphSize);
            int idx2 = rand.nextInt(initGraphSize);
            while (idx1 == idx2){
                idx2 = rand.nextInt(initGraphSize);
            }
            if(!graph.getNode(idx1).hasEdgeBetween(graph.getNode(idx2).getId())){
                Edge edge = graph.addEdge(graph.getNode(idx1).getId() + "," + graph.getNode(idx2).getId(), "" + graph.getNode(idx1).getId(), graph.getNode(idx2).getId());
                addedEdges.add(edge);
                i++;
            }
        }

        return addedEdges;
    }


    public ArrayList<Edge> removeEdges2(){
        ArrayList<Edge> removedEdges = new ArrayList<>();
        if(!added){
            int removedEdgeSize = (int)(edgeDensity * initGraphSize * (initGraphSize - 1) / 2);
            for (int i = 0; i < removedEdgeSize ; i++) {
                int idx = rand.nextInt(graph.getEdgeCount());
                Edge edge = graph.getEdge(idx);
                graph.getEdgeSet().remove(edge);
                removedEdges.add(edge);
            }
        }
        return removedEdges;
    }

    private Edge createNewEdge(Graph graph){
        Edge edge = null;
        int node1Idx = rand.nextInt(graph.getNodeCount());
        int node2Idx = rand.nextInt(graph.getNodeCount());
        while (node1Idx == node2Idx){
            node1Idx = rand.nextInt(graph.getNodeCount());
            node2Idx = rand.nextInt(graph.getNodeCount());
        }
        Node node1 = graph.getNode(node1Idx);
        Node node2 = graph.getNode(node2Idx);
        if(!node1.hasEdgeBetween(node2)){
            edge = graph.addEdge(node1.getId()+"--"+node2.getId(),node1.getId(),node2.getId());
        }
        return edge;
    }

    public Graph getGraph() {
        return graph;
    }
}
