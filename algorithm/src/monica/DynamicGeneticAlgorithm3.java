package monica;

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.*;


import DPBEA.Parent;
import DPBEA.Util;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import DPBEA.DPBEAConst;



public class DynamicGeneticAlgorithm3 {

    private Graph graph;
    private Random rand;
    final int POPULATION_SIZE;

    public ArrayList<Individual> getdPopulation() {
        return dPopulation;
    }

    ArrayList<Individual> dPopulation;
    public int[] colorsStep = new int[DPBEAConst.eStepSize];
    public double [] fitStep = new double[DPBEAConst.eStepSize];

    public DynamicGeneticAlgorithm3(Graph graph, Random rand) {

        this.graph = graph;
        this.rand = rand;
        this.POPULATION_SIZE = DPBEAConst.POPULATION;

        dPopulation = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++){
            dPopulation.add(new Individual(graph.getNodeCount()));
        }
    }


    //returns the number of colors
    public Individual dsatur(Graph graph){
        clearColoring(graph);
        Individual answer = new Individual();
        ArrayList<Node> nodesToColor = new ArrayList<>();
        nodesToColor.addAll(graph.getNodeSet());
        TreeSet<Integer> colorsUsed = new TreeSet<>();
        while(nodesToColor.size()!=0){
            Collections.shuffle(nodesToColor); //this way we can pick the first node with the desired property and it is a random one with that property
            Node toColor = nodesToColor.get(0);
            for (Node n: nodesToColor){
                //is the degree saturation of n higher than toColor, yes -- toColor = n, equal -- check degree, no -- continue
                int nSatDegree = neighborColors(n, graph).size();
                int toColorSatDegree = neighborColors(toColor, graph).size();
                if (nSatDegree>=toColorSatDegree){
                    if (nSatDegree>toColorSatDegree)
                        toColor = n;
                    else if (n.getDegree()>toColor.getDegree())
                        toColor = n;
                }
            }

            TreeSet<Integer> nColors = neighborColors(toColor,graph);
            int nodeColor = 0;
            while(nColors.contains(nodeColor))
                nodeColor++;
            toColor.setAttribute("color", nodeColor);
            colorsUsed.add(nodeColor);

            nodesToColor.remove(toColor);
            answer.add(Integer.parseInt(toColor.getId()));
        }
        return answer;
    }

    public Individual selectBest (ArrayList<Individual> tournament){
        Collections.sort(tournament);
        return tournament.get(0);
    }

    public void clearColoring(Graph graph){
        for (Node n: graph){
            n.setAttribute("color",-1);
            //	n.setAttribute("ui.color", 0);
        }
    }

    public void removeVertices (ArrayList<Node> removedNodes){

        //remove nodes from graph and from population members
        ArrayList<Integer> toRemoveNums = new ArrayList<>();
        for (Node n: removedNodes){
            toRemoveNums.add(Integer.parseInt(n.getId()));
        }
        for (Individual i: dPopulation){
            i.removeAll(toRemoveNums);
        }
    }

    public void addVertices(ArrayList<Node> addedNodes) {

        for (Node n : addedNodes) {
            //add vertex to each member of the population
            for (Individual individ : dPopulation)
                if (individ.size() == 0)
                    individ.add(Integer.parseInt(n.getId()));
                else
                    individ.add(rand.nextInt(individ.size()), Integer.parseInt(n.getId()));
        }
    }


    public TreeSet<Integer> neighborColors (Node node, Graph graph){
        TreeSet<Integer> colors = new TreeSet<>();
        Iterator<Node> neighbors = node.getNeighborNodeIterator();
        while (neighbors.hasNext())
            colors.add(neighbors.next().getAttribute("color"));
        return colors;
    }

    public double averageColors(ArrayList<Individual>population){
        double total = 0;
        for (Individual individ: population)
            total+=individ.colors;
        return total/population.size();
    }

    public void evaluateFitness (Graph graph, Individual permutation){
        clearColoring(graph);
        int colorsUsed;
        int N = graph.getNodeCount();
        TreeMap<Integer,Integer> colorCount = new TreeMap<>();
        for (int vertex: permutation){
            Node node = graph.getNode(""+vertex);
            if (node==null)
                continue;
            TreeSet<Integer> nColors = neighborColors(node,graph);
            int nodeColor = 0;
            while(nColors.contains(nodeColor))
                nodeColor++;
            node.setAttribute("color", nodeColor);
            if (colorCount.containsKey(nodeColor))
                colorCount.put(nodeColor,colorCount.get(nodeColor)+1);
            else
                colorCount.put(nodeColor, 1);
        }
        ArrayList<Integer> colorHistogram = new ArrayList<>();
        for (int color: colorCount.keySet())
            colorHistogram.add(colorCount.get(color));
        colorsUsed = colorHistogram.size();
        Collections.sort(colorHistogram);
        int fitness;
        if (colorsUsed>2)
            fitness = (int)Math.pow(N,3)*colorsUsed + (int)Math.pow(N,2)*colorHistogram.get(0) + (int)Math.pow(N,1)*colorHistogram.get(1) + colorHistogram.get(2) ;
        else
            fitness = 0;

        permutation.setFitness(fitness);
        permutation.setColors(colorsUsed);
    }



    public double[] evolvePopulation(long start, ArrayList<Individual>population){

        final int POPULATION_SIZE = DPBEAConst.POPULATION;
        final int INDIVID_STEP = DPBEAConst.INDIVID_STEP;
        final int TOUR_SIZE = DPBEAConst.TOUR_SIZE;
        final int CROSSOVER = DPBEAConst.CROSSOP;
        final int MUTATION = DPBEAConst.MUTOP;
        final double MUT_RATE = DPBEAConst.MUTRATE;

        for (Individual individ: population){
            clearColoring(graph);
            evaluateFitness(graph, individ);
        }
        double[] results = new double[2];//0:time, 1:firstIte
        int firstBestColor = Integer.MAX_VALUE;
        int firstBestFit = Integer.MAX_VALUE;
        int firstIte = 0;
        double dgaFirstTime = 0;
        int counter = 0;

        while (counter < INDIVID_STEP){

            Collections.sort(population);
            int currentBestColor = population.get(0).colors;
            int currentBestFit = population.get(0).fitness;
            if(counter % 50 == 0 && counter!=0){
                int index = (counter / 50)-1;
                this.colorsStep[index] = currentBestColor;
                this.fitStep[index] = currentBestFit;
            }
            boolean changed = false;
            if(currentBestColor < firstBestColor){
                firstBestColor = currentBestColor;
                changed = true;
            }

            if(currentBestFit < firstBestFit){
                firstBestFit = currentBestFit;
                changed = true;
            }

            if(changed){
                long endFirst = System.nanoTime();
                long elapsedTime = endFirst - start;
                dgaFirstTime = (double)elapsedTime / 1000000000.0;
                firstIte = counter;
            }

            //select the first parent
            ArrayList<Individual> tournament = new ArrayList<>();
            for (int i=0; i < TOUR_SIZE; i++)
                tournament.add(population.get(rand.nextInt(POPULATION_SIZE)));
            Individual parent1 = selectBest(tournament);

            //select the second parent
            tournament = new ArrayList<>();
            for (int i=0;i<TOUR_SIZE;i++)
                tournament.add(population.get(rand.nextInt(POPULATION_SIZE)));
            Individual parent2 = selectBest(tournament);

            Individual child1 = new Individual();
            Individual child2 = new Individual();

            int cross = CROSSOVER;
            if (rand.nextDouble() < .3)
                cross = 2;

            switch (cross){
                case 0:Individual.ox(parent1, parent2, child1, child2);
                    break;
                case 1: Individual.ox2(parent1, parent2, child1, child2);
                    break;
                case 2: child1.addAll(parent1);
                    child2.addAll(parent2);
                    break;
                case 3: Individual.cycleCross(parent1, parent2, child1, child2);
                    break;
                case 4: Individual.pmx(parent1, parent2, child1, child2);
                    break;
                case 5: Individual.positionCross(parent1, parent2, child1, child2);
                    break;
            }

            population.remove(POPULATION_SIZE - 1); //this is for removing the bottom of the population
            population.remove(POPULATION_SIZE - 2);

            if (rand.nextDouble()<MUT_RATE){
                switch(MUTATION){
                    case 0: child1.rar();
                        break;
                    case 1: child1.swap();
                        break;
                    case 2: child1.inversion();
                        break;
                    case 3:
                        break;
                }
            }
            population.add(child1);
            evaluateFitness(graph, child1);

            if (rand.nextDouble()<MUT_RATE){
                switch(MUTATION){
                    case 0: child2.rar();
                        break;
                    case 1: child2.swap();
                        break;
                    case 2: child2.inversion();
                        break;
                    case 3:
                        break;
                }
            }

            population.add(child2);
            evaluateFitness(graph, child2);

            counter += 2;
        }
        Collections.sort(population);
        int index = (INDIVID_STEP/50)-1;


        results[0] = dgaFirstTime;
        results[1] = firstIte;
        return results;
    }

    public void partRep(Individual permutation, Graph graph){
        HashMap<Integer,Integer> map = new HashMap<>();
        int colorNum = 1;
        for (int p: permutation) {
            if(map.isEmpty()){
                map.put(p,colorNum);
            }
            else {
                Node n1 = graph.getNode(""+p);
                boolean isPlaced = false;
                for (int i = 1; i <= colorNum ; i++) {
                    boolean cf = true;
                    for (Integer k: map.keySet()) {
                        if(map.get(k).equals(i)){
                            if(graph.getNode(""+k).hasEdgeBetween(n1)){
                                cf = false;
                                break;
                            }
                        }
                    }
                    if(cf){
                        map.put(p,i);
                        isPlaced = true;
                        break;
                    }
                }
                if(!isPlaced){
                    colorNum++;
                    map.put(p,colorNum);
                }
            }
        }
        System.out.println();
        for (int i = 1; i <=colorNum ; i++) {
            for (int k: map.keySet()) {
                if(map.get(k).equals(i)){
                    System.out.print(k+", ");
                }
            }
            System.out.print("|");
        }
        System.out.println();
    }

    public double[] run (){

        double[] result = new double[10]; // dstaur-> fitness, color, time 	dga->fitness, color, time, firstColor, firstFit, firstTime, firstIte

        long start = System.nanoTime();
        Individual dsaturAnswer = dsatur(graph); //run dsatur
        evaluateFitness(graph, dsaturAnswer);
        int dsatur = dsaturAnswer.colors;
        int dsaturFit = dsaturAnswer.fitness;

        long end = System.nanoTime();
        long elapsedTime = end - start;
        double dsaturTime = (double)elapsedTime / 1000000000.0;

        ArrayList<Integer> vertices = new ArrayList<>();
        for (Node n: graph.getNodeSet())
            vertices.add(Integer.parseInt(n.getId()));


        //create static population
    /*    ArrayList<Individual> sPopulation = new ArrayList<>();
        sPopulation.add(dsaturAnswer); //add the dsatur answer
        while (sPopulation.size()<POPULATION_SIZE){
            Individual individ = new Individual();
            individ.addAll(vertices);
            Collections.shuffle(individ);
            sPopulation.add(individ);
        }*/

        start = System.nanoTime();
        double[] firsts = evolvePopulation(start, dPopulation); //evolve dynamic population
        end = System.nanoTime();
        elapsedTime = end - start;
        double timeDGA = (double)elapsedTime / 1000000000.0;
     //   evolvePopulation(start, sPopulation); //evolve static population

        //print data
        Collections.sort(dPopulation);

        int dgaBest = dPopulation.get(0).colors;
        int dgaFit = dPopulation.get(0).fitness;
       /* Collections.sort(sPopulation);
        int sgaBest = sPopulation.get(0).colors;*/


        result[0] = dgaBest;
        result[1] = dsatur;
        result[2] = dgaFit;
        result[3] = dsaturFit;
        result[4] = timeDGA;
        result[5] = dsaturTime;
        result[6] = firsts[0];//time
        result[7] = firsts[1];//iteration

        return result;
    }

    public HashMap<String, Individual> run2 (){
        HashMap<String, Individual> resultInd = new HashMap<>();
        double[] result = new double[10]; // dstaur-> fitness, color, time 	dga->fitness, color, time, firstColor, firstFit, firstTime, firstIte

        long start = System.nanoTime();
        Individual dsaturAnswer = dsatur(graph); //run dsatur
        evaluateFitness(graph, dsaturAnswer);
        int dsatur = dsaturAnswer.colors;
        int dsaturFit = dsaturAnswer.fitness;
        resultInd.put("dsatur", dsaturAnswer);
        long end = System.nanoTime();
        long elapsedTime = end - start;
        double dsaturTime = (double)elapsedTime / 1000000000.0;


        start = System.nanoTime();
        double[] firsts = evolvePopulation(start, dPopulation); //evolve dynamic population
        end = System.nanoTime();
        elapsedTime = end - start;
        double timeDGA = (double)elapsedTime / 1000000000.0;
        //   evolvePopulation(start, sPopulation); //evolve static population

        //print data
        Collections.sort(dPopulation);

        int dgaBest = dPopulation.get(0).colors;
        int dgaFit = dPopulation.get(0).fitness;
       /* Collections.sort(sPopulation);
        int sgaBest = sPopulation.get(0).colors;*/
        resultInd.put("dga", dPopulation.get(0));

        result[0] = dgaBest;
        result[1] = dsatur;
        result[2] = dgaFit;
        result[3] = dsaturFit;
        result[4] = timeDGA;
        result[5] = dsaturTime;
        result[6] = firsts[0];//time
        result[7] = firsts[1];//iteration

        return resultInd;
    }
}
