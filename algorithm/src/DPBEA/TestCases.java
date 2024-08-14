package DPBEA;

import OldDPBEA.DPBEAPopulation;
import OldDPBEA.DPBEAUtil;
import monica.DynamicGeneticAlgorithm3;
import monica.Individual;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by DELL on 28.03.2018.
 */
public class TestCases implements Runnable {

    private  double NODE_PROB;
    private  double EDGE_PROB;
    private double EDGE_DENSITY;
    private  int NUM_STEPS;
    private  int GRAPH_SIZE;
    private String testType;
    private String fileName;
    final NumberFormat formatter;

    public TestCases(String testType, double NODE_PROB, double EDGE_PROB, double EDGE_DENSITY, int NUM_STEPS, int GRAPH_SIZE) {
        this.NODE_PROB = NODE_PROB;
        this.EDGE_PROB = EDGE_PROB;
        this.EDGE_DENSITY = EDGE_DENSITY;
        this.NUM_STEPS = NUM_STEPS;
        this.GRAPH_SIZE = GRAPH_SIZE;
        this.testType = testType;
        Calendar calendar = Calendar.getInstance();
        String today = calendar.get(Calendar.DAY_OF_MONTH) + "_" + calendar.get(Calendar.MONTH) + "_" + calendar.get(Calendar.YEAR);
        this.fileName = testType  + "_" + NODE_PROB + "_" + EDGE_DENSITY + "_" + GRAPH_SIZE + ".txt ";
        formatter = new DecimalFormat("000E0");
    }

    public void testEdgeDynamic(){
        System.out.println("EDGE_PROB: " + EDGE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE);

        /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        Population pop = new Population(graph);
        pop.initializePopulation(DPBEAConst.POPULATION);

        /*Initialize Old DPBEA population*/
        DPBEAPopulation popOld = new DPBEAPopulation(dGraph, 1);
        popOld.initializePopulation(DPBEAConst.POPULATION);

        /*Initialize DGA and DSATUR*/
        DynamicGeneticAlgorithm3 monica = new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND);

        for (int i = 0; i < NUM_STEPS; i++) {
            /*run monical and get results*/
            double [] results = monica.run();

            /*run DPBEA*/
            if(i != 0){
                Util.InitialProcessPopulation(graph, pop);
                DPBEAUtil.InitialProcessPopulation(graph, popOld);
            }
            /*if the first step, run the newDPBEA*/
            Util.processDPBEA(pop, graph, this.EDGE_DENSITY);
            DPBEAUtil.processPopulation(popOld, graph);



             /*Get DPBEA results*/
            int best = Util.bestParent(pop);
            int bestOld = DPBEAUtil.selectBestParentColor(popOld);
            for (Parent p: pop.getParents()) {
                boolean res = Util.verify(p, graph);
                if(res == false){
                    System.out.println(res+"1");
                }
            }

            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Edge> removedEdges = dGraph.removeEdges();

            /*add new edges to the graph*/
            ArrayList<Edge> addedEdges = dGraph.addEdges();

                //  pops0.get(j).updatePop2(addedEdges);
                popOld.updatePop2(addedEdges);




            /*update DPBEA*/
         //   pop.updatePop(addedEdges);

            /*Write the Results*/
            System.out.println("STEP " + i + "\tDGA: " + results[0] + "\tDSATUR: " + results[1] + "\tDPBEA: " + best + "\tOldDPBEA: " + bestOld +"\tADDED: " + addedEdges.size() + "\tREMOVED: " + removedEdges.size() + "\tGRAPH SIZE: " + edgeSize + "\tDENSITY: " + density);


        }


    }


    public void testNewDPBEA(){
        String line = "EDGE_PROB: " + EDGE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE;
        Util.writeLine(this.fileName, line);
       // System.out.println(line);
         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        ArrayList<Population> pops = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops.get(i).initializePopulation(DPBEAConst.POPULATION);
        }


        /*Initialize DGA and DSATUR*/
        ArrayList<DynamicGeneticAlgorithm3> monicas = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            monicas.add(new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND));
        }

        double [] avgColorStepMonica = new double[DPBEAConst.eStepSize];
        double [] avgColorStepDPBEA = new double[DPBEAConst.eStepSize];
        DecimalFormat decimalFormat=new DecimalFormat("#.##");

        for (int i = 0; i < this.NUM_STEPS ; i++) {
            double [] avg = new double[8];
            for (DynamicGeneticAlgorithm3 monica: monicas) {
                double [] result = monica.run();
                for (int j = 0; j < 8 ; j++) {
                    avg[j] += result[j];
                }
                for (int j = 0; j < DPBEAConst.eStepSize ; j++) {
                    avgColorStepMonica[j] += monica.colorsStep[j];
                }
            }

            for (int j = 0; j < 8 ; j++) {
                avg[j] /= DPBEAConst.TESTPOPSIZE;
            }

            double avgTime = 0;
            double avgColor = 0;
            double bestInit = 0;

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops.get(j));
                bestInit += Util.bestParent(pops.get(j));
                Util.processDPBEA(pops.get(j), graph, this.EDGE_DENSITY);
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTime += (double)elapsedTime / 1000000000.0;
                /*Get DPBEA results*/
                avgColor += Util.bestParent(pops.get(j));
                for (int k = 0; k < DPBEAConst.eStepSize ; k++) {
                    avgColorStepDPBEA[k] += pops.get(j).colorStep[k];
                }

            }
            avgColor /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime /= DPBEAConst.TESTPOPSIZE;
            bestInit /= DPBEAConst.TESTPOPSIZE;

            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Edge> removedEdges = dGraph.removeEdges();

            /*add new edges to the graph*/
            ArrayList<Edge> addedEdges = dGraph.addEdges();

            line = "\nSTEP " + i + "\t\t"+ decimalFormat.format(avg[1]).replace(".",",") + "\t" + decimalFormat.format(avg[0]).replace(".",",") + "\t" + decimalFormat.format(avgColor).replace(".",",") + "\t" + decimalFormat.format(bestInit).replace(".",",") +"\t\t" + decimalFormat.format(DPBEAUtil.round(avg[5], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avg[4], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avgTime, 2)).replace(".",",") + "\t\t" + density + "\t" + addedEdges.size() + "\t" + removedEdges.size();
            Util.writeLine(this.fileName, line);
           // System.out.println(line);
        }

        for (int i = 0; i < DPBEAConst.eStepSize ; i++) {
            avgColorStepDPBEA[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);
            avgColorStepMonica[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);


            int step = (i+1)*50;
            line = "\n" + step + "\t=>\t" + decimalFormat.format(avgColorStepDPBEA[i]).replace(".",",") + "\t" + decimalFormat.format(avgColorStepMonica[i]).replace(".",",");
            DPBEAUtil.writeLine(this.fileName, line);
            //System.out.println(line);
        }

    }

    public void testNewDPBEAInitTime(){
        String line = "EDGE_PROB: " + EDGE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE;
        Util.writeLine(this.fileName, line);
        // System.out.println(line);
         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        ArrayList<Population> pops = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

        double [] avgColorStepDPBEA = new double[DPBEAConst.eStepSize];
        DecimalFormat decimalFormat=new DecimalFormat("#.##");

        for (int i = 0; i < this.NUM_STEPS ; i++) {

            double avgTime = 0;
            double bestInit = 0;

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops.get(j));
                bestInit += Util.bestParent(pops.get(j));
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTime += (double)elapsedTime / 1000000000.0;
                /*Get DPBEA results*/
            }

            avgTime /= DPBEAConst.TESTPOPSIZE;
            bestInit /= DPBEAConst.TESTPOPSIZE;

            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Edge> removedEdges = dGraph.removeEdges();

            /*add new edges to the graph*/
            ArrayList<Edge> addedEdges = dGraph.addEdges();

            line = "\nSTEP " + i + "\t\t"+ decimalFormat.format(bestInit).replace(".",",") +"\t\t"   + "\t" + "\t" + decimalFormat.format(DPBEAUtil.round(avgTime, 2)).replace(".",",") + "\t\t" + density + "\t" + addedEdges.size() + "\t" + removedEdges.size();
            Util.writeLine(this.fileName, line);
            // System.out.println(line);
        }


    }

    public void performans(){
        String line = "EDGE_PROB: " + EDGE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE;
        Util.writeLine(this.fileName, line);
        // System.out.println(line);
         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        ArrayList<Population> pops = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

        /*Initialize DPBEA population*/
        ArrayList<Population> pops2 = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops2.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops2.get(i).initializePopulation(DPBEAConst.POPULATION);
        }


        double [] avgColorStepDPBEA = new double[DPBEAConst.eStepSize];
        double [] avgColorStepDPBEA2 = new double[DPBEAConst.eStepSize];
        DecimalFormat decimalFormat=new DecimalFormat("#.##");

        for (int i = 0; i < this.NUM_STEPS ; i++) {

            double avgTime = 0;
            double avgColor = 0;
            double avgTime2 = 0;
            double avgColor2 = 0;

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops.get(j));
                Util.processDPBEA(pops.get(j), graph, this.EDGE_DENSITY);
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTime += (double)elapsedTime / 1000000000.0;
                /*Get DPBEA results*/
                avgColor += Util.bestParent(pops.get(j));
                for (int k = 0; k < DPBEAConst.eStepSize ; k++) {
                    avgColorStepDPBEA[k] += pops.get(j).colorStep[k];
                }

                start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops2.get(j));
                Util.processDPBEA2(pops2.get(j), graph, this.EDGE_DENSITY);
                end = System.nanoTime();
                elapsedTime = end - start;
                avgTime2 += (double)elapsedTime / 1000000000.0;
                /*Get DPBEA results*/
                avgColor2 += Util.bestParent(pops2.get(j));
                for (int k = 0; k < DPBEAConst.eStepSize ; k++) {
                    avgColorStepDPBEA2[k] += pops2.get(j).colorStep[k];
                }

            }
            avgColor /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime /= DPBEAConst.TESTPOPSIZE;
            avgColor2 /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime2 /= DPBEAConst.TESTPOPSIZE;


            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Edge> removedEdges = dGraph.removeEdges();

            /*add new edges to the graph*/
            ArrayList<Edge> addedEdges = dGraph.addEdges();

            line = "\nSTEP " + i + "\t\t"+ decimalFormat.format(avgColor).replace(".",",") + "\t" + decimalFormat.format(avgColor2).replace(".",",") + "\t\t" +  "\t" + decimalFormat.format(DPBEAUtil.round(avgTime, 2)).replace(".",",") + "\t" + decimalFormat.format(DPBEAUtil.round(avgTime2, 2)).replace(".",",")+ "\t\t" + density + "\t" + addedEdges.size() + "\t" + removedEdges.size();
            Util.writeLine(this.fileName, line);
            // System.out.println(line);
        }

        for (int i = 0; i < DPBEAConst.eStepSize ; i++) {
            avgColorStepDPBEA[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);
            avgColorStepDPBEA2[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);


            int step = (i+1)*50;
            line = "\n" + step + "\t=>\t" + decimalFormat.format(avgColorStepDPBEA[i]).replace(".",",") + "\t" + decimalFormat.format(avgColorStepDPBEA2[i]).replace(".",",");
            DPBEAUtil.writeLine(this.fileName, line);
            //System.out.println(line);
        }

    }



    public void staticBenchmark(){

        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();
        Path path = Paths.get("C:/Users/DELL/Desktop/newDPBEA/src/DSJC500.txt");
        String FILENAME = "C:/Users/DELL/Desktop/newDPBEA/src/DSJC500.txt";
        //FILENAME = FILENAME.replace("\\\\", "/");
        Util.readFileStatic("C:/Users/DELL/Desktop/newDPBEA/src/DSJC500.txt", graph);
        /*Initialize DPBEA population*/
        ArrayList<Population> pops = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops.get(i).initializePopulation(DPBEAConst.POPULATION);
        }


        /*Initialize DGA and DSATUR*/
        ArrayList<DynamicGeneticAlgorithm3> monicas = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            monicas.add(new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND));
        }

        double [] avgColorStepMonica = new double[DPBEAConst.eStepSize];
        double [] avgColorStepDPBEA = new double[DPBEAConst.eStepSize];
        DecimalFormat decimalFormat=new DecimalFormat("#.##");

    //    for (int i = 0; i < this.NUM_STEPS ; i++) {
            double [] avg = new double[8];
            for (DynamicGeneticAlgorithm3 monica: monicas) {
                double [] result = monica.run();
                for (int j = 0; j < 8 ; j++) {
                    avg[j] += result[j];
                }
                for (int j = 0; j < DPBEAConst.eStepSize ; j++) {
                    avgColorStepMonica[j] += monica.colorsStep[j];
                }
            }

            for (int j = 0; j < 8 ; j++) {
                avg[j] /= DPBEAConst.TESTPOPSIZE;
            }

            double avgTime = 0;
            double avgColor = 0;
            double bestInit = 0;

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops.get(j));
                bestInit += Util.bestParent(pops.get(j));
                Util.processDPBEA(pops.get(j), graph, this.EDGE_DENSITY);
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTime += (double)elapsedTime / 1000000000.0;
                /*Get DPBEA results*/
                avgColor += Util.bestParent(pops.get(j));
                for (int k = 0; k < DPBEAConst.eStepSize ; k++) {
                    avgColorStepDPBEA[k] += pops.get(j).colorStep[k];
                }

            }
            avgColor /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime /= DPBEAConst.TESTPOPSIZE;
            bestInit /= DPBEAConst.TESTPOPSIZE;

            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Edge> removedEdges = dGraph.removeEdges();

            /*add new edges to the graph*/
            ArrayList<Edge> addedEdges = dGraph.addEdges();

            String line = decimalFormat.format(avg[1]).replace(".",",") + "\t" + decimalFormat.format(avg[0]).replace(".",",") + "\t" + decimalFormat.format(avgColor).replace(".",",") + " - " + decimalFormat.format(bestInit).replace(".",",") +"\t\t" + decimalFormat.format(DPBEAUtil.round(avg[5], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avg[4], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avgTime, 2)).replace(".",",") + "\t\t" + density + "\t" + addedEdges.size() + "\t" + removedEdges.size();
            Util.writeLine(this.fileName, line);
            // System.out.println(line);
       // }

    }

    public void NodeDynamic(){
        String line = "NODE_PROB: " + NODE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE;
        Util.writeLine(this.fileName, line);
        // System.out.println(line);
         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        ArrayList<Population> pops = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

        /*Initialize Old DPBEA population*/
        ArrayList<DPBEAPopulation> popsOld = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            popsOld.add(new DPBEAPopulation(dGraph, 2));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            popsOld.get(i).initializePopulation2(DPBEAConst.POPULATION);
        }

        /*Initialize DGA and DSATUR*/
        ArrayList<DynamicGeneticAlgorithm3> monicas = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            monicas.add(new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND));
        }

        double [] avgColorStepMonica = new double[DPBEAConst.eStepSize];
        double [] avgColorStepDPBEA = new double[DPBEAConst.eStepSize];
        DecimalFormat decimalFormat=new DecimalFormat("#.##");

        for (int i = 0; i < this.NUM_STEPS ; i++) {
            double [] avg = new double[8];
            int minDSATUR = Integer.MAX_VALUE;
            int maxDSATUR = Integer.MIN_VALUE;
            for (DynamicGeneticAlgorithm3 monica: monicas) {
                double [] result = monica.run();
                for (int j = 0; j < 8 ; j++) {
                    avg[j] += result[j];
                }
                for (int j = 0; j < DPBEAConst.eStepSize ; j++) {
                    avgColorStepMonica[j] += monica.colorsStep[j];
                }
            }

            for (int j = 0; j < 8 ; j++) {
                avg[j] /= DPBEAConst.TESTPOPSIZE;
            }

            double avgTime = 0;
            double avgTimeOld = 0;
            double avgColor = 0;
            double avgColorOld = 0;
            double bestInit = 0;

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops.get(j));
                int bestIn = Util.bestParent(pops.get(j));
                bestInit += bestIn;
                Util.processDPBEA(pops.get(j), graph, this.EDGE_DENSITY);
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTime += (double)elapsedTime / 1000000000.0;

                /*Get DPBEA results*/
                int bestColor = Util.bestParent(pops.get(j));
                avgColor += bestColor;

                for (int k = 0; k < DPBEAConst.eStepSize ; k++) {
                    avgColorStepDPBEA[k] += pops.get(j).colorStep[k];
                }

                start = System.nanoTime();
                DPBEAUtil.InitialProcessPopulation(graph, popsOld.get(j));
                for (int k = 51; k < DPBEAConst.INDIVID_STEP; k++) {
                    DPBEAUtil.processPopulation(popsOld.get(j), graph);
                }
                end = System.nanoTime();
                elapsedTime = end - start;
                int bestColorOld =  DPBEAUtil.selectBestParentColor(popsOld.get(j));
                avgColorOld += bestColorOld;
                avgTimeOld += (double)elapsedTime / 1000000000.0;

            }
            avgColor /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime /= DPBEAConst.TESTPOPSIZE;
            bestInit /= DPBEAConst.TESTPOPSIZE;
            avgColorOld /= DPBEAConst.TESTPOPSIZE;
            avgTimeOld /= DPBEAConst.TESTPOPSIZE;

            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Node> removedNodes = dGraph.removeNodes();

            /*add new edges to the graph*/
            ArrayList<Node> addedNodes = dGraph.addNodes();

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE; j++) {
                monicas.get(j).removeVertices(removedNodes);
                monicas.get(j).addVertices(addedNodes);
                pops.get(j).removeNodes(removedNodes);
                pops.get(j).addNodes(addedNodes);
                popsOld.get(j).updatePopForNodeDynamic(addedNodes, removedNodes);
            }

            line = "\nSTEP " + i + "\t\t"+ decimalFormat.format(avg[1]).replace(".",",") + "\t" + decimalFormat.format(avg[0]).replace(".",",") + "\t" + decimalFormat.format(avgColor).replace(".",",") + "\t" + decimalFormat.format(bestInit).replace(".",",")+ "\t" + decimalFormat.format(avgColorOld).replace(".",",") +"\t\t" + decimalFormat.format(DPBEAUtil.round(avg[5], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avg[4], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avgTime, 2)).replace(".",",") + "\t" + decimalFormat.format(avgTimeOld).replace(".",",")+"\t\t" + density + "\t" + addedNodes.size() + "\t" + removedNodes.size() + "\t" + graphSize;
            Util.writeLine(this.fileName, line);
            // System.out.println(line);
        }

        for (int i = 0; i < DPBEAConst.eStepSize ; i++) {
            avgColorStepDPBEA[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);
            avgColorStepMonica[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);


            int step = (i+1)*50;
            line = "\n" + step + "\t=>\t" + decimalFormat.format(avgColorStepDPBEA[i]).replace(".",",") + "\t" + decimalFormat.format(avgColorStepMonica[i]).replace(".",",");
            DPBEAUtil.writeLine(this.fileName, line);
            //System.out.println(line);
        }

    }

    public void nodeDynamicSimilarPerformance(){
        String line = "NODE_PROB: " + NODE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE;
        Util.writeLine(this.fileName, line);
        // System.out.println(line);
         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        ArrayList<Population> popsSimilar = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            popsSimilar.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            popsSimilar.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

        /*Initialize DPBEA population*/
        ArrayList<Population> popsNonSimilar = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            popsNonSimilar.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            popsNonSimilar.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

        /*Initialize DPBEA population*/
        ArrayList<Population> pops = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

         /*Initialize DGA and DSATUR*/
        ArrayList<DynamicGeneticAlgorithm3> monicas = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            monicas.add(new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND));
        }



        DecimalFormat decimalFormat=new DecimalFormat("#.##");
        int worseDPBEA = 0;
        int betterDPBEA = 0;
        int equalDPBEA = 0;

        int worseDPBEA2 = 0;
        int betterDPBEA2 = 0;
        int equalDPBEA2 = 0;
        for (int i = 0; i < this.NUM_STEPS ; i++) {
            double [] avg = new double[8];
            double [] resultsDGA = new double[DPBEAConst.TESTPOPSIZE];
            double [] resultsDSATUR = new double[DPBEAConst.TESTPOPSIZE];
            for (DynamicGeneticAlgorithm3 monica: monicas) {
                double [] result = monica.run();
                for (int j = 0; j < 8 ; j++) {
                    avg[j] += result[j];
                }
            }

            for (int j = 0; j < 8 ; j++) {
                avg[j] /= DPBEAConst.TESTPOPSIZE;
            }

            int minSimilarDPBEA = Integer.MAX_VALUE;
            int maxSimilarDPBEA = Integer.MIN_VALUE;
            int minNonsimilarDPBEA = Integer.MAX_VALUE;
            int maxNonsimilarDPBEA = Integer.MIN_VALUE;


            double avgTimeSimilar = 0;
            double avgTimeNonsimilar = 0;
            double avgTime = 0;
            double avgColorSimilar = 0;
            double avgColorNonsimilar = 0;
            double avgColor = 0;

            int [] resultsSimilarDPBEA = new int[DPBEAConst.TESTPOPSIZE];
            int [] resultsNonsimilarDPBEA = new int[DPBEAConst.TESTPOPSIZE];
            int [] results = new int[DPBEAConst.TESTPOPSIZE];
            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, popsSimilar.get(j));

                Util.processDPBEA(popsSimilar.get(j), graph, this.EDGE_DENSITY);
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTimeSimilar += (double)elapsedTime / 1000000000.0;

                /*Get DPBEA results*/
                int bestColor = Util.bestParent(popsSimilar.get(j));
                avgColorSimilar += bestColor;
                if(minSimilarDPBEA > bestColor){
                    minSimilarDPBEA = bestColor;
                }
                if(maxSimilarDPBEA < bestColor){
                    maxSimilarDPBEA = bestColor;
                }
                resultsSimilarDPBEA[j] = bestColor;

                start = System.nanoTime();
                Util.InitialProcessPopulation(graph, popsNonSimilar.get(j));

                Util.processDPBEAWOSimilar(popsNonSimilar.get(j), graph, this.EDGE_DENSITY);
                end = System.nanoTime();
                elapsedTime = end - start;
                avgTimeNonsimilar += (double)elapsedTime / 1000000000.0;

                /*Get DPBEA results*/
                bestColor = Util.bestParent(popsNonSimilar.get(j));
                avgColorNonsimilar += bestColor;
                if(minNonsimilarDPBEA > bestColor){
                    minNonsimilarDPBEA = bestColor;
                }
                if(maxNonsimilarDPBEA < bestColor){
                    maxNonsimilarDPBEA = bestColor;
                }
                resultsNonsimilarDPBEA[j] = bestColor;


                start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops.get(j));

                Util.processDPBEAWOSimilarMerged(pops.get(j), graph, this.EDGE_DENSITY);
                end = System.nanoTime();
                elapsedTime = end - start;
                avgTime += (double)elapsedTime / 1000000000.0;

                /*Get DPBEA results*/
                bestColor = Util.bestParent(pops.get(j));
                avgColor += bestColor;
                results[j] = bestColor;

            }
            avgColorSimilar /= (double)DPBEAConst.TESTPOPSIZE;
            avgTimeSimilar /= DPBEAConst.TESTPOPSIZE;
            avgColorNonsimilar /= (double)DPBEAConst.TESTPOPSIZE;
            avgTimeNonsimilar /= DPBEAConst.TESTPOPSIZE;
            avgColor /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime /= DPBEAConst.TESTPOPSIZE;

            /*Comparison*/
            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {

                /*OLDDPBEA vs DPBEA*/
                if(resultsNonsimilarDPBEA[j] < resultsSimilarDPBEA[j]){
                    betterDPBEA++;
                }
                if(resultsNonsimilarDPBEA[j] > resultsSimilarDPBEA[j]){
                    worseDPBEA++;
                }
                if(resultsNonsimilarDPBEA[j] == resultsSimilarDPBEA[j]){
                    equalDPBEA++;
                }

                if(results[j] < resultsSimilarDPBEA[j]){
                    betterDPBEA2++;
                }
                if(results[j] > resultsSimilarDPBEA[j]){
                    worseDPBEA2++;
                }
                if(results[j] == resultsSimilarDPBEA[j]){
                    equalDPBEA2++;
                }
            }


            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Node> removedNodes = dGraph.removeNodes();

            /*add new edges to the graph*/
            ArrayList<Node> addedNodes = dGraph.addNodes();

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE; j++) {
                popsSimilar.get(j).removeNodes(removedNodes);
                popsSimilar.get(j).addNodes(addedNodes);

                popsNonSimilar.get(j).removeNodes(removedNodes);
                popsNonSimilar.get(j).addNodes(addedNodes);

                pops.get(j).removeNodes(removedNodes);
                pops.get(j).addNodes(addedNodes);
                monicas.get(j).removeVertices(removedNodes);
                monicas.get(j).addVertices(addedNodes);

            }

            line = "\nSTEP " + i + "\t\t"+ "\t" + decimalFormat.format(avg[0]).replace(".",",") + "\t" + decimalFormat.format(avg[1]).replace(".",",")+ "\t" + decimalFormat.format(avgColorSimilar).replace(".",",") + "\t" + decimalFormat.format(avgColorNonsimilar).replace(".",",") + "\t" + decimalFormat.format(avgColor).replace(".",",") + "\t" + decimalFormat.format(avgTimeSimilar).replace(".",",") + "\t" + decimalFormat.format(avgTimeNonsimilar).replace(".",",")+ "\t" + decimalFormat.format(avgTime).replace(".",",") +"\t\t" +
                    decimalFormat.format(minSimilarDPBEA).replace(".",",") + "\t" +  decimalFormat.format(maxSimilarDPBEA).replace(".",",")+ "\t" +  decimalFormat.format(minNonsimilarDPBEA).replace(".",",")+ "\t" +  decimalFormat.format(maxNonsimilarDPBEA).replace(".",",") + "\t" + decimalFormat.format(betterDPBEA2).replace(".",",")+ "\t" +  decimalFormat.format(equalDPBEA2).replace(".",",")+ "\t" +  decimalFormat.format(worseDPBEA2).replace(".",",")+ "\t\t"+
                    decimalFormat.format(betterDPBEA).replace(".",",")+ "\t" +  decimalFormat.format(equalDPBEA).replace(".",",")+ "\t" +  decimalFormat.format(worseDPBEA).replace(".",",")+ "\t\t" +
                    density + "\t" + addedNodes.size() + "\t" + removedNodes.size() + "\t" + graphSize;
            Util.writeLine(this.fileName, line);
            // System.out.println(line);
        }

    }

    public void nodeDynamicMinMax(){
        String line = "NODE_PROB: " + NODE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE;
        Util.writeLine(this.fileName, line);
        // System.out.println(line);
         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        ArrayList<Population> pops = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

        /*Initialize Old DPBEA population*/
        ArrayList<DPBEAPopulation> popsOld = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            popsOld.add(new DPBEAPopulation(dGraph, 2));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            popsOld.get(i).initializePopulation2(DPBEAConst.POPULATION);
        }

        /*Initialize DGA and DSATUR*/
        ArrayList<DynamicGeneticAlgorithm3> monicas = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            monicas.add(new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND));
        }

        DecimalFormat decimalFormat=new DecimalFormat("#.##");
        int worseDGA = 0;
        int worseDSATUR = 0;
        int betterDGA = 0;
        int betterDSATUR = 0;
        int equalDGA = 0;
        int equalDSATUR = 0;
        int worseDPBEA = 0;
        int betterDPBEA = 0;
        int equalDPBEA = 0;
        for (int i = 0; i < this.NUM_STEPS ; i++) {
            double [] avg = new double[8];
            int minDSATUR = Integer.MAX_VALUE;
            int maxDSATUR = Integer.MIN_VALUE;
            int minDGA = Integer.MAX_VALUE;
            int maxDGA = Integer.MIN_VALUE;
            int minoldDPBEA = Integer.MAX_VALUE;
            int maxoldDPBEA = Integer.MIN_VALUE;
            int minDPBEA = Integer.MAX_VALUE;
            int maxDPBEA = Integer.MIN_VALUE;
            int mininitDPBEA = Integer.MAX_VALUE;
            int maxinitDPBEA = Integer.MIN_VALUE;
            double [] resultsDGA = new double[DPBEAConst.TESTPOPSIZE];
            double [] resultsDSATUR = new double[DPBEAConst.TESTPOPSIZE];
            int monica_count = 0;
            for (DynamicGeneticAlgorithm3 monica: monicas) {
                double [] result = monica.run();
                for (int j = 0; j < 8 ; j++) {
                    avg[j] += result[j];
                }
                if(minDSATUR > result[1]){
                    minDSATUR = (int)result[1];
                }
                if(maxDSATUR < result[1]){
                    maxDSATUR = (int)result[1];
                }
                if(minDGA > result[0]){
                    minDGA = (int)result[0];
                }
                if(maxDGA < result[0]){
                    maxDGA = (int)result[0];
                }
                resultsDGA[monica_count] = result[0];
                resultsDSATUR[monica_count] = result[1];
                monica_count++;
            }

            for (int j = 0; j < 8 ; j++) {
                avg[j] /= DPBEAConst.TESTPOPSIZE;
            }

            double avgTime = 0;
            double avgTimeOld = 0;
            double avgColor = 0;
            double avgColorOld = 0;
            double bestInit = 0;
            int [] resultsinitDPBEA = new int[DPBEAConst.TESTPOPSIZE];
            int [] resultsDPBEA = new int[DPBEAConst.TESTPOPSIZE];
            int [] resultsoldDPBEA = new int[DPBEAConst.TESTPOPSIZE];
            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops.get(j));
                int bestIn = Util.bestParent(pops.get(j));
                bestInit += bestIn;
                resultsinitDPBEA[j] = bestIn;
                if(mininitDPBEA > bestIn){
                    mininitDPBEA = bestIn;
                }
                if(maxinitDPBEA < bestIn){
                    maxinitDPBEA = bestIn;
                }
                Util.processDPBEA(pops.get(j), graph, this.EDGE_DENSITY);
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTime += (double)elapsedTime / 1000000000.0;

                /*Get DPBEA results*/
                int bestColor = Util.bestParent(pops.get(j));
                avgColor += bestColor;
                if(minDPBEA > bestColor){
                    minDPBEA = bestColor;
                }
                if(maxDPBEA < bestColor){
                    maxDPBEA = bestColor;
                }
                resultsDPBEA[j] = bestColor;
                start = System.nanoTime();
                DPBEAUtil.InitialProcessPopulation(graph, popsOld.get(j));
                for (int k = 51; k < DPBEAConst.INDIVID_STEP; k++) {
                    DPBEAUtil.processPopulation(popsOld.get(j), graph);
                }
                end = System.nanoTime();
                elapsedTime = end - start;
                int bestColorOld =  DPBEAUtil.selectBestParentColor(popsOld.get(j));
                avgColorOld += bestColorOld;
                avgTimeOld += (double)elapsedTime / 1000000000.0;
                if(minoldDPBEA > bestColorOld){
                    minoldDPBEA = bestColorOld;
                }
                if(maxoldDPBEA < bestColorOld){
                    maxoldDPBEA = bestColorOld;
                }
                resultsoldDPBEA[j] = bestColorOld;

            }
            avgColor /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime /= DPBEAConst.TESTPOPSIZE;
            bestInit /= DPBEAConst.TESTPOPSIZE;
            avgColorOld /= DPBEAConst.TESTPOPSIZE;
            avgTimeOld /= DPBEAConst.TESTPOPSIZE;
            /*Comparison*/
            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                /*DGA vs DPBEA*/
                if((int)resultsDGA[j] < resultsDPBEA[j]){
                    betterDGA++;
                }
                if((int)resultsDGA[j] > resultsDPBEA[j]){
                    worseDGA++;
                }
                if((int)resultsDGA[j] == resultsDPBEA[j]){
                    equalDGA++;
                }
                /*DSATUR vs DPBEA*/
                if((int)resultsDSATUR[j] < resultsDPBEA[j]){
                    betterDSATUR++;
                }
                if((int)resultsDSATUR[j] > resultsDPBEA[j]){
                    worseDSATUR++;
                }
                if((int)resultsDSATUR[j] == resultsDPBEA[j]){
                    equalDSATUR++;
                }
                /*OLDDPBEA vs DPBEA*/
                if(resultsoldDPBEA[j] < resultsDPBEA[j]){
                    betterDPBEA++;
                }
                if(resultsoldDPBEA[j] > resultsDPBEA[j]){
                    worseDPBEA++;
                }
                if(resultsoldDPBEA[j] == resultsDPBEA[j]){
                    equalDPBEA++;
                }
            }


            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Node> removedNodes = dGraph.removeNodes();

            /*add new edges to the graph*/
            ArrayList<Node> addedNodes = dGraph.addNodes();

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE; j++) {
                monicas.get(j).removeVertices(removedNodes);
                monicas.get(j).addVertices(addedNodes);
                pops.get(j).removeNodes(removedNodes);
                pops.get(j).addNodes(addedNodes);
                popsOld.get(j).updatePopForNodeDynamic(addedNodes, removedNodes);
            }

            line = "\nSTEP " + i + "\t\t"+ decimalFormat.format(avg[1]).replace(".",",") + "\t" + decimalFormat.format(avg[0]).replace(".",",") + "\t" + decimalFormat.format(avgColor).replace(".",",") + "\t" + decimalFormat.format(bestInit).replace(".",",")+ "\t" + decimalFormat.format(avgColorOld).replace(".",",") +"\t\t" +
                    decimalFormat.format(minDSATUR).replace(".",",") + "\t" +  decimalFormat.format(maxDSATUR).replace(".",",")+ "\t" +  decimalFormat.format(minDGA).replace(".",",")+ "\t" +  decimalFormat.format(maxDGA).replace(".",",")+"\t" +  decimalFormat.format(minDPBEA).replace(".",",")+"\t" +  decimalFormat.format(maxDPBEA).replace(".",",")+"\t" +  decimalFormat.format(mininitDPBEA).replace(".",",")+"\t" +  decimalFormat.format(maxinitDPBEA).replace(".",",")+"\t" +  decimalFormat.format(minoldDPBEA).replace(".",",")+ "\t" +  decimalFormat.format(maxoldDPBEA).replace(".",",")+ "\t\t"+
                    decimalFormat.format(DPBEAUtil.round(avg[5], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avg[4], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avgTime, 2)).replace(".",",") + "\t" + decimalFormat.format(avgTimeOld).replace(".",",")+"\t\t" +
                    decimalFormat.format(betterDSATUR).replace(".",",")+ "\t" +  decimalFormat.format(equalDSATUR).replace(".",",")+ "\t" +  decimalFormat.format(worseDSATUR).replace(".",",")+"\t" +  decimalFormat.format(betterDGA).replace(".",",")+ "\t" +  decimalFormat.format(equalDGA).replace(".",",")+"\t" +  decimalFormat.format(worseDGA).replace(".",",")+ "\t" +  decimalFormat.format(betterDPBEA).replace(".",",")+ "\t" +  decimalFormat.format(equalDPBEA).replace(".",",")+ "\t" +  decimalFormat.format(worseDPBEA).replace(".",",")+ "\t" +
                    density + "\t" + addedNodes.size() + "\t" + removedNodes.size() + "\t" + graphSize;
            Util.writeLine(this.fileName, line);
            // System.out.println(line);
        }

    }

    public void edgeDynamicMinMax(){
        String line = "NODE_PROB: " + NODE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE;
        Util.writeLine(this.fileName, line);
         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        ArrayList<Population> pops = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

        /*Initialize DGA and DSATUR*/
        ArrayList<DynamicGeneticAlgorithm3> monicas = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            monicas.add(new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND));
        }

        DecimalFormat decimalFormat=new DecimalFormat("#.##");
        int worseDGA = 0;
        int worseDSATUR = 0;
        int betterDGA = 0;
        int betterDSATUR = 0;
        int equalDGA = 0;
        int equalDSATUR = 0;
        for (int i = 0; i < this.NUM_STEPS ; i++) {
            double [] avg = new double[8];
            int minDSATUR = Integer.MAX_VALUE;
            int maxDSATUR = Integer.MIN_VALUE;
            int minDGA = Integer.MAX_VALUE;
            int maxDGA = Integer.MIN_VALUE;
            int minDPBEA = Integer.MAX_VALUE;
            int maxDPBEA = Integer.MIN_VALUE;
            int mininitDPBEA = Integer.MAX_VALUE;
            int maxinitDPBEA = Integer.MIN_VALUE;
            double [] resultsDGA = new double[DPBEAConst.TESTPOPSIZE];
            double [] resultsDSATUR = new double[DPBEAConst.TESTPOPSIZE];
            int monica_count = 0;
            for (DynamicGeneticAlgorithm3 monica: monicas) {
                double [] result = monica.run();
                for (int j = 0; j < 8 ; j++) {
                    avg[j] += result[j];
                }
                if(minDSATUR > result[1]){
                    minDSATUR = (int)result[1];
                }
                if(maxDSATUR < result[1]){
                    maxDSATUR = (int)result[1];
                }
                if(minDGA > result[0]){
                    minDGA = (int)result[0];
                }
                if(maxDGA < result[0]){
                    maxDGA = (int)result[0];
                }
                resultsDGA[monica_count] = result[0];
                resultsDSATUR[monica_count] = result[1];
                monica_count++;
            }

            for (int j = 0; j < 8 ; j++) {
                avg[j] /= DPBEAConst.TESTPOPSIZE;
            }

            double avgTime = 0;
            double avgColor = 0;
            double bestInit = 0;
            int [] resultsinitDPBEA = new int[DPBEAConst.TESTPOPSIZE];
            int [] resultsDPBEA = new int[DPBEAConst.TESTPOPSIZE];
            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops.get(j));
                int bestIn = Util.bestParent(pops.get(j));
                bestInit += bestIn;
                resultsinitDPBEA[j] = bestIn;
                if(mininitDPBEA > bestIn){
                    mininitDPBEA = bestIn;
                }
                if(maxinitDPBEA < bestIn){
                    maxinitDPBEA = bestIn;
                }
                Util.processDPBEA(pops.get(j), graph, this.EDGE_DENSITY);
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTime += (double)elapsedTime / 1000000000.0;

                /*Get DPBEA results*/
                int bestColor = Util.bestParent(pops.get(j), graph);
                avgColor += bestColor;
                if(minDPBEA > bestColor){
                    minDPBEA = bestColor;
                }
                if(maxDPBEA < bestColor){
                    maxDPBEA = bestColor;
                }
                resultsDPBEA[j] = bestColor;


            }
            avgColor /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime /= DPBEAConst.TESTPOPSIZE;
            bestInit /= DPBEAConst.TESTPOPSIZE;

            /*Comparison*/
            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                /*DGA vs DPBEA*/
                if((int)resultsDGA[j] < resultsDPBEA[j]){
                    betterDGA++;
                }
                if((int)resultsDGA[j] > resultsDPBEA[j]){
                    worseDGA++;
                }
                if((int)resultsDGA[j] == resultsDPBEA[j]){
                    equalDGA++;
                }
                /*DSATUR vs DPBEA*/
                if((int)resultsDSATUR[j] < resultsDPBEA[j]){
                    betterDSATUR++;
                }
                if((int)resultsDSATUR[j] > resultsDPBEA[j]){
                    worseDSATUR++;
                }
                if((int)resultsDSATUR[j] == resultsDPBEA[j]){
                    equalDSATUR++;
                }
            }


            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Edge> removedEdges = dGraph.removeEdges();

            /*add new edges to the graph*/
            ArrayList<Edge> addedEdges = dGraph.addEdges();

            line = "\nSTEP " + i + "\t\t"+ decimalFormat.format(avg[1]).replace(".",",") + "\t" + decimalFormat.format(avg[0]).replace(".",",") + "\t" + decimalFormat.format(avgColor).replace(".",",") + "\t" + decimalFormat.format(bestInit).replace(".",",")+ "\t\t" +
                    decimalFormat.format(minDSATUR).replace(".",",") + "\t" +  decimalFormat.format(maxDSATUR).replace(".",",")+ "\t" +  decimalFormat.format(minDGA).replace(".",",")+ "\t" +  decimalFormat.format(maxDGA).replace(".",",")+"\t" +  decimalFormat.format(minDPBEA).replace(".",",")+"\t" +  decimalFormat.format(maxDPBEA).replace(".",",")+"\t" +  decimalFormat.format(mininitDPBEA).replace(".",",")+"\t" +  decimalFormat.format(maxinitDPBEA).replace(".",",")+ "\t\t"+
                    decimalFormat.format(DPBEAUtil.round(avg[5], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avg[4], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avgTime, 2)).replace(".",",") + "\t\t" +
                    decimalFormat.format(betterDSATUR).replace(".",",")+ "\t" +  decimalFormat.format(equalDSATUR).replace(".",",")+ "\t" +  decimalFormat.format(worseDSATUR).replace(".",",")+"\t" +  decimalFormat.format(betterDGA).replace(".",",")+ "\t" +  decimalFormat.format(equalDGA).replace(".",",")+"\t" +  decimalFormat.format(worseDGA).replace(".",",")+ "\t" +
                    density + "\t" + addedEdges.size() + "\t" + removedEdges.size() + "\t" + graphSize;
            Util.writeLine(this.fileName, line);
            // System.out.println(line);
        }

    }

    public void crossoverTest(){
        String line = "NODE_PROB: " + NODE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE;
        Util.writeLine(this.fileName, line);
         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        ArrayList<Population> popsMerged = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            popsMerged.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            popsMerged.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

        /*Initialize DPBEA population*/
        ArrayList<Population> popsInit = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            popsInit.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            popsInit.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

         /*Initialize DGA and DSATUR*/
        ArrayList<DynamicGeneticAlgorithm3> monicas = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            monicas.add(new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND));
        }

        double [] avgColorStepMonica = new double[DPBEAConst.eStepSize];
        double [] avgColorStepDPBEA1 = new double[DPBEAConst.eStepSize];
        double [] avgColorStepDPBEA2 = new double[DPBEAConst.eStepSize];
        DecimalFormat decimalFormat=new DecimalFormat("#.##");

        for (int i = 0; i < this.NUM_STEPS ; i++) {

            double avgTime1 = 0;
            double avgColor1 = 0;
            double bestInit1 = 0;

            double avgTime2 = 0;
            double avgColor2 = 0;
            double bestInit2 = 0;
            double [] avg = new double[8];
            for (DynamicGeneticAlgorithm3 monica: monicas) {

                double [] result = monica.run();
                for (int j = 0; j < 8 ; j++) {
                    avg[j] += result[j];
                }
                for (int j = 0; j < DPBEAConst.eStepSize ; j++) {
                    avgColorStepMonica[j] += monica.colorsStep[j];
                }
            }

            for (int j = 0; j < 8 ; j++) {
                avg[j] /= DPBEAConst.TESTPOPSIZE;
            }

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {

                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, popsMerged.get(j));
                int bestIn = Util.bestParent(popsMerged.get(j));
                bestInit1 += bestIn;
                Util.processDPBEACrossMerged(popsMerged.get(j), graph);
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTime1 += (double)elapsedTime / 1000000000.0;

                /*Get DPBEA results*/
                int bestColor = Util.bestParent(popsMerged.get(j));
                avgColor1 += bestColor;

                for (int k = 0; k < DPBEAConst.eStepSize ; k++) {
                    avgColorStepDPBEA1[k] += popsMerged.get(j).colorStep[k];
                }

                start = System.nanoTime();
                Util.InitialProcessPopulation(graph, popsInit.get(j));
                bestIn = Util.bestParent(popsInit.get(j));
                bestInit2 += bestIn;
                Util.processDPBEACrossInit(popsInit.get(j), graph);
                end = System.nanoTime();
                elapsedTime = end - start;
                avgTime2 += (double)elapsedTime / 1000000000.0;

                /*Get DPBEA results*/
                bestColor = Util.bestParent(popsInit.get(j));
                avgColor2 += bestColor;

                for (int k = 0; k < DPBEAConst.eStepSize ; k++) {
                    avgColorStepDPBEA2[k] += popsInit.get(j).colorStep[k];
                }


            }
            avgColor1 /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime1 /= DPBEAConst.TESTPOPSIZE;
            bestInit1 /= DPBEAConst.TESTPOPSIZE;

            avgColor2 /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime2 /= DPBEAConst.TESTPOPSIZE;
            bestInit2 /= DPBEAConst.TESTPOPSIZE;

            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Node> removedNodes = dGraph.removeNodes();

            /*add new edges to the graph*/
            ArrayList<Node> addedNodes = dGraph.addNodes();

            line = "\n" + avgColor1 + "\t" + avgColor2 + "\t" + avgTime1 + "\t" + avgTime2 + "\t" + bestInit1 + "\t" + bestInit2 + "\n";
            Util.writeLine(this.fileName, line);
            // System.out.println(line);
        }

        for (int i = 0; i < DPBEAConst.eStepSize ; i++) {
            avgColorStepDPBEA1[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);
            avgColorStepDPBEA2[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);
            int step = (i+1)*50;
            line = "\n" + step + "\t=>\t" + decimalFormat.format(avgColorStepDPBEA1[i]).replace(".",",") + "\t" + decimalFormat.format(avgColorStepDPBEA2[i]).replace(".",",") + "\t" + decimalFormat.format(avgColorStepMonica[i]).replace(".",",");
            DPBEAUtil.writeLine(this.fileName, line);
            //System.out.println(line);
        }


    }


    public void specialTestEdgeDynamic(){
        String line = "EDGE_PROB: " + EDGE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE;
        Util.writeLine(this.fileName, line);
        // System.out.println(line);
         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        ArrayList<Population> pops = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops.get(i).initializePopulation(DPBEAConst.POPULATION);
        }


        /*Initialize DGA and DSATUR*/
        ArrayList<DynamicGeneticAlgorithm3> monicas = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            monicas.add(new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND));
        }

        double [] avgColorStepMonica = new double[DPBEAConst.eStepSize];
        double [] avgColorStepDPBEA = new double[DPBEAConst.eStepSize];
        DecimalFormat decimalFormat=new DecimalFormat("#.##");

        for (int i = 0; i < this.NUM_STEPS ; i++) {
            double [] avg = new double[8];
            for (DynamicGeneticAlgorithm3 monica: monicas) {
                double [] result = monica.run();
                for (int j = 0; j < 8 ; j++) {
                    avg[j] += result[j];
                }
                for (int j = 0; j < DPBEAConst.eStepSize ; j++) {
                    avgColorStepMonica[j] += monica.colorsStep[j];
                }
            }

            for (int j = 0; j < 8 ; j++) {
                avg[j] /= DPBEAConst.TESTPOPSIZE;
            }

            double avgTime = 0;
            double avgColor = 0;
            double bestInit = 0;

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops.get(j));
                bestInit += Util.bestParent(pops.get(j));
                Util.processDPBEA(pops.get(j), graph, this.EDGE_DENSITY);
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTime += (double)elapsedTime / 1000000000.0;
                /*Get DPBEA results*/
                avgColor += Util.bestParent(pops.get(j));
                for (int k = 0; k < DPBEAConst.eStepSize ; k++) {
                    avgColorStepDPBEA[k] += pops.get(j).colorStep[k];
                }

            }
            avgColor /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime /= DPBEAConst.TESTPOPSIZE;
            bestInit /= DPBEAConst.TESTPOPSIZE;

            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            ArrayList<Edge> removedEdges = dGraph.removeEdges2();

            /*add new edges to the graph*/
            ArrayList<Edge> addedEdges = dGraph.addEdges2();

            line = "\nSTEP " + i + "\t\t"+ decimalFormat.format(avg[1]).replace(".",",") + "\t" + decimalFormat.format(avg[0]).replace(".",",") + "\t" + decimalFormat.format(avgColor).replace(".",",") + "\t" + decimalFormat.format(bestInit).replace(".",",") +"\t\t" + decimalFormat.format(DPBEAUtil.round(avg[5], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avg[4], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avgTime, 2)).replace(".",",") + "\t\t" + density + "\t" + addedEdges.size() + "\t" + removedEdges.size();
            Util.writeLine(this.fileName, line);
            // System.out.println(line);
        }

        for (int i = 0; i < DPBEAConst.eStepSize ; i++) {
            avgColorStepDPBEA[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);
            avgColorStepMonica[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);


            int step = (i+1)*50;
            line = "\n" + step + "\t=>\t" + decimalFormat.format(avgColorStepDPBEA[i]).replace(".",",") + "\t" + decimalFormat.format(avgColorStepMonica[i]).replace(".",",");
            DPBEAUtil.writeLine(this.fileName, line);
            //System.out.println(line);
        }

    }

    public void SpecialTestNodeDynamic(){
        String line = "NODE_PROB: " + NODE_PROB + "\tEDGE_DENSITY: " + EDGE_DENSITY + "\tGRAPH SIZE: " + GRAPH_SIZE;
        Util.writeLine(this.fileName, line);

         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();

        /*Initialize DPBEA population*/
        ArrayList<Population> pops = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            pops.add(new Population(graph));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            pops.get(i).initializePopulation(DPBEAConst.POPULATION);
        }

        /*Initialize Old DPBEA population*/
        ArrayList<DPBEAPopulation> popsOld = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            popsOld.add(new DPBEAPopulation(dGraph, 2));
        }

        for (int i = 0; i < DPBEAConst.TESTPOPSIZE; i++) {
            popsOld.get(i).initializePopulation2(DPBEAConst.POPULATION);
        }

        /*Initialize DGA and DSATUR*/
        ArrayList<DynamicGeneticAlgorithm3> monicas = new ArrayList<>();
        for (int i = 0; i < DPBEAConst.TESTPOPSIZE ; i++) {
            monicas.add(new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND));
        }

        double [] avgColorStepMonica = new double[DPBEAConst.eStepSize];
        double [] avgColorStepDPBEA = new double[DPBEAConst.eStepSize];
        DecimalFormat decimalFormat=new DecimalFormat("#.##");

        for (int i = 0; i < this.NUM_STEPS ; i++) {
            double [] avg = new double[8];
            for (DynamicGeneticAlgorithm3 monica: monicas) {
                double [] result = monica.run();
                for (int j = 0; j < 8 ; j++) {
                    avg[j] += result[j];
                }
                for (int j = 0; j < DPBEAConst.eStepSize ; j++) {
                    avgColorStepMonica[j] += monica.colorsStep[j];
                }
            }

            for (int j = 0; j < 8 ; j++) {
                avg[j] /= DPBEAConst.TESTPOPSIZE;
            }

            double avgTime = 0;
            double avgTimeOld = 0;
            double avgColor = 0;
            double avgColorOld = 0;
            double bestInit = 0;

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE ; j++) {
                long start = System.nanoTime();
                Util.InitialProcessPopulation(graph, pops.get(j));
                bestInit += Util.bestParent(pops.get(j));
                Util.processDPBEA(pops.get(j), graph, this.EDGE_DENSITY);
                long end = System.nanoTime();
                long elapsedTime = end - start;
                avgTime += (double)elapsedTime / 1000000000.0;
                /*Get DPBEA results*/
                avgColor += Util.bestParent(pops.get(j));
                for (int k = 0; k < DPBEAConst.eStepSize ; k++) {
                    avgColorStepDPBEA[k] += pops.get(j).colorStep[k];
                }

                start = System.nanoTime();
                for (int k = 0; k < DPBEAConst.INDIVID_STEP; k++) {
                    DPBEAUtil.processPopulation(popsOld.get(j), graph);
                }
                end = System.nanoTime();
                elapsedTime = end - start;
                avgColorOld += DPBEAUtil.selectBestParentColor(popsOld.get(j));
                avgTimeOld += (double)elapsedTime / 1000000000.0;
            }
            avgColor /= (double)DPBEAConst.TESTPOPSIZE;
            avgTime /= DPBEAConst.TESTPOPSIZE;
            bestInit /= DPBEAConst.TESTPOPSIZE;
            avgColorOld /= DPBEAConst.TESTPOPSIZE;
            avgTimeOld /= DPBEAConst.TESTPOPSIZE;

            /*Get graph size*/
            int graphSize = graph.getNodeCount();
            int edgeSize = graph.getEdgeCount();
            double density = (double)edgeSize / ((double)graphSize * (double)( graphSize-1) / 2.0);

            /*remove edges from graph*/
            // ArrayList<Node> removedNodes = dGraph.removeNodes();

            /*add new edges to the graph*/
            ArrayList<Node> addedNodes = dGraph.addNodes2();

            for (int j = 0; j < DPBEAConst.TESTPOPSIZE; j++) {
                //   monicas.get(j).removeVertices(removedNodes);
                monicas.get(j).addVertices(addedNodes);
                //   pops.get(j).removeNodes(removedNodes);
                pops.get(j).addNodes(addedNodes);
                popsOld.get(j).updatePopForNodeDynamic(addedNodes, new ArrayList<>());
            }

            line = "\nSTEP " + i + "\t\t"+ decimalFormat.format(avg[1]).replace(".",",") + "\t" + decimalFormat.format(avg[0]).replace(".",",") + "\t" + decimalFormat.format(avgColor).replace(".",",") + "\t" + decimalFormat.format(bestInit).replace(".",",")+ "\t" + decimalFormat.format(avgColorOld).replace(".",",") +"\t\t" + decimalFormat.format(DPBEAUtil.round(avg[5], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avg[4], 2)).replace(".",",")  + "\t" + decimalFormat.format(DPBEAUtil.round(avgTime, 2)).replace(".",",") + "\t" + decimalFormat.format(avgTimeOld).replace(".",",")+"\t\t" + density + "\t" + addedNodes.size()  + "\t" + graphSize;
            Util.writeLine(this.fileName, line);
            // System.out.println(line);
        }

        for (int i = 0; i < DPBEAConst.eStepSize ; i++) {
            avgColorStepDPBEA[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);
            avgColorStepMonica[i] /= (this.NUM_STEPS * DPBEAConst.TESTPOPSIZE);


            int step = (i+1)*50;
            line = "\n" + step + "\t=>\t" + decimalFormat.format(avgColorStepDPBEA[i]).replace(".",",") + "\t" + decimalFormat.format(avgColorStepMonica[i]).replace(".",",");
            DPBEAUtil.writeLine(this.fileName, line);
            //System.out.println(line);
        }

    }

    public void benchmark(){

         /*Initialize Dynamic Graph*/
        DGraph dGraph = new DGraph(GRAPH_SIZE, NODE_PROB, EDGE_PROB, EDGE_DENSITY);
        Graph graph = dGraph.getGraph();
        for (Edge edge: graph.getEdgeSet()) {
            System.out.println(edge.toString());
        }
        Population pop = new Population(graph);
        pop.initializePopulation(DPBEAConst.POPULATION);


        DynamicGeneticAlgorithm3 monica = new DynamicGeneticAlgorithm3(graph, DPBEAConst.RAND);


        DecimalFormat decimalFormat=new DecimalFormat("#.##");
        for (int i = 0; i < this.NUM_STEPS ; i++) {

            HashMap<String, Individual> monicaResult = monica.run2();
            //Util.InitialProcessPopulation(graph, pop);
            Util.processBenchmark(pop, graph, monicaResult);

        }

    }

    public void test(){
        if (this.testType.equals("edgeTest")){
            testEdgeDynamic();
        }
        if(this.testType.equals("testNewDPBEA2")){
            testNewDPBEA();
        }
        if(this.testType.equals("performans")){
            benchmark();
        }

        if(this.testType.equals("NodeDynamicCross")){
            NodeDynamic();
        }

        if(this.testType.equals("staticBenchmark")){
            staticBenchmark();
        }

        if(this.testType.equals("InitTime")){
            testNewDPBEAInitTime();
        }
        if(this.testType.equals("SpecialTestEdgeDynamic")){
            specialTestEdgeDynamic();
        }
        if(this.testType.equals("SpecialTestNodeDynamic")){
            SpecialTestNodeDynamic();
        }
        if(this.testType.equals("CrossoverTest")){
            crossoverTest();
        }

        if(this.testType.equals("NodeDynamicMinMax")){
            nodeDynamicMinMax();
        }
        if(this.testType.equals("EdgeDynamicMinMax")){
            edgeDynamicMinMax();
        }
        if(this.testType.equals("NodeDynamicSimilarPerformance2")){
            nodeDynamicSimilarPerformance();
        }



    }



    @Override
    public void run() {
        test();
    }
}
