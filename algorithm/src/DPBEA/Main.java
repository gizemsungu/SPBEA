package DPBEA;

import monica.DynamicGeneticAlgorithm3;
import org.graphstream.graph.Graph;

import java.util.Random;

public class Main {
    public static void main(String args[]){
        //  public TestCases(String testType, double NODE_PROB, double EDGE_PROB, double EDGE_DENSITY, int NUM_STEPS, int GRAPH_SIZE) {




        new Thread(new TestCases("NodeDynamicSimilarPerformance2", .01, .05, .05, 20, 80)).start();
        new Thread(new TestCases("NodeDynamicSimilarPerformance2", .04, .05, .05, 20, 80)).start();
        new Thread(new TestCases("NodeDynamicSimilarPerformance2", .07, .05, .05, 20, 80)).start();
        new Thread(new TestCases("NodeDynamicSimilarPerformance2", .1, .05, .05, 20, 80)).start();

       


    }
}
