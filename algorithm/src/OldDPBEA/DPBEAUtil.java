package OldDPBEA;

import DPBEA.DPBEAConst;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by DELL on 1.12.2017.
 */
public class DPBEAUtil {
    public static void processPopulation(DPBEAPopulation population, Graph graph){
        Random rand = DPBEAConst.RAND;
        ArrayList<DPBEAParent> twoParents = new ArrayList<>();
        DPBEAParent child = null;
        try{
            twoParents = selectParentsRandom(population);
        }catch (Exception e) {
            e.printStackTrace();
        }

        try {
            child = crossover(graph, twoParents);
        }catch (Exception e) {
            e.printStackTrace();
        }
        if(population.getPopType() == 1){
            try{
                if(child != null && rand.nextDouble() <= .5){
                    child = mutation(child, graph);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else if(population.getPopType() == 2){
            DPBEAParent childMut = null;
            try{
                if(child != null && rand.nextDouble() <= DPBEAConst.MUTRATE){
                    childMut = mutationOld(child, graph);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            if(childMut != null){
                if(childMut.numberofColors() <= child.numberofColors()){
                    child = childMut;
                }
            }
        }

        if(child.numberofColors() < twoParents.get(0).numberofColors() || child.numberofColors() < twoParents.get(1).numberofColors()){
            if(twoParents.get(0).numberofColors() > twoParents.get(1).numberofColors()){
                population.getParents().remove(twoParents.get(0));
                population.getParents().add(child);
            }
            else{
                population.getParents().remove(twoParents.get(1));
                population.getParents().add(child);
            }
        }
     /*   double fitness1 = calculateFitness(graph, twoParents.get(0));
        double fitness2 = calculateFitness(graph, twoParents.get(1));
        double fitChild = calculateFitness(graph, child);
        DPBEAParent worseFit = (fitness1 > fitness2) ? twoParents.get(0) : twoParents.get(1);
        boolean changed = false;
        if(fitChild < population.bestFitness){
            population.bestFitness = fitChild;
            changed = true;
        }

        if (child.numberofColors() < population.bestColor){
            population.bestColor = child.numberofColors();
            changed = true;
        }

        population.getParents().remove(worseFit);
        population.getParents().add(child);*/

    }

    public static void InitialProcessPopulation(Graph graph, DPBEAPopulation population){
        int populationSize = population.getParents().size();
        Random rand = DPBEAConst.RAND;
        ArrayList<DPBEAParent> parents = population.getParents();
        while (populationSize != 0){
            //  System.out.println("populationSize: " + populationSize);
            /* Randomly select first and second parents */
            int firstIdx = rand.nextInt(populationSize);
            int secondIdx = rand.nextInt(populationSize);

            		/* They can not be the same */
            while (firstIdx == secondIdx){
                secondIdx = rand.nextInt(populationSize);
            }
            //  System.out.println("firstIdx: " + firstIdx + " secondIdx: " + secondIdx);
            ArrayList<DPBEAParent> twoParents = new ArrayList<>();
            twoParents.add(parents.get(firstIdx));
            twoParents.add(parents.get(secondIdx));

            DPBEAParent child = null;
            DPBEAParent child2 = null;

            try {
                child = crossover(graph, twoParents);

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                child2 = crossover(graph, twoParents);

            } catch (Exception e) {
                e.printStackTrace();
            }
            double fitChild = calculateFitness(graph, child);
            double fitChild2 = calculateFitness(graph, child2);
            parents.removeAll(twoParents);

            parents.add(child);
            parents.add(child2);
            populationSize -=2;
            boolean changed = false;

            if(fitChild < population.bestFitness){
                population.bestFitness = fitChild;
                changed = true;
            }
            if(fitChild2 < population.bestFitness){
                population.bestFitness = fitChild2;
                changed = true;
            }

            if (child.numberofColors() < population.bestColor){
                population.bestColor = child.numberofColors();
                changed = true;
            }
            if (child2.numberofColors() < population.bestColor){
                population.bestColor = child2.numberofColors();
                changed = true;
            }


        }
        population.fitStep[0] = population.bestFitness;
        population.colorStep[0] = population.bestColor;

    }

    public static double calculateFitness(Graph graph, DPBEAParent parent){

        double fitness = 0;
        int nodeCount = graph.getNodeCount();
        ArrayList<DPBEAColor> colors = parent.getColors();

        if (colors.size() < 3){ return 0; }

		/* Destencing sort colors by included node number */
        ArrayList<DPBEAColor> sortedColors = new ArrayList<>();
        sortedColors.addAll(colors);
        Collections.sort(sortedColors, (o1, o2) -> o1.getNodes().size() - o2.getNodes().size());

        fitness = (int) Math.pow(nodeCount, 3) * colors.size()
                + (int) Math.pow(nodeCount, 2) * sortedColors.get(0).getNodes().size()
                + nodeCount * sortedColors.get(1).getNodes().size()
                + sortedColors.get(2).getNodes().size();

        return fitness;
    }

    public static DPBEAParent selectBestParent(DPBEAPopulation population, Graph graph){
        ArrayList<DPBEAParent> parents = population.getParents();
        double minFit = Double.MAX_VALUE;
        DPBEAParent best = null;

        for (DPBEAParent parent : parents) {
            double fitness = calculateFitness(graph, parent);
            if (fitness < minFit){
                minFit = fitness;
                best = parent;
                best.setFitness(minFit);
            }
        }

        return best;
    }

    public static int selectBestParentColor(DPBEAPopulation pop){
        DPBEAParent best = null;
        ArrayList<DPBEAParent> parents = pop.getParents();
        int minColor = Integer.MAX_VALUE;
        for (DPBEAParent parent: parents) {
            int colorNum = parent.numberofColors();
            if(colorNum < minColor){
                minColor = colorNum;
                best = parent;
            }
        }
        return minColor;
    }
    public static ArrayList<DPBEAParent> selectParentsRandom(DPBEAPopulation population) throws Exception{
        Random rand = DPBEAConst.RAND;
        ArrayList<DPBEAParent> parents = population.getParents();
        int parentSize = parents.size();

        /* Randomly select first and second parents */
        int firstIdx = rand.nextInt(parentSize);
        int secondIdx = rand.nextInt(parentSize);

		/* They can not be the same */
        while (firstIdx == secondIdx){
            secondIdx = rand.nextInt(parentSize);
        }

        DPBEAParent parent1 = parents.get(firstIdx);
        DPBEAParent parent2 = parents.get(secondIdx);
        ArrayList<DPBEAParent> twoParents = new ArrayList<>();
        twoParents.add(parent1);
        twoParents.add(parent2);

        if (parent1 == null || parent2 == null){
            throw new Exception("Parent1 or/and Parent2 is null.");
        }
     //   System.out.println("Parent " + firstIdx + " Parent " + secondIdx + " are selected.");

        return twoParents;
    }

    public static int getUnusedIdx(HashSet<Integer> usedColorIdx, int size){
        int idx = -1;
        Random rand = DPBEAConst.RAND;
        idx = rand.nextInt(size);
        while (usedColorIdx.contains(idx)){
            idx = rand.nextInt(size);
        }
        return idx;
    }

    public static DPBEAParent crossover(Graph graph, ArrayList<DPBEAParent> parents) throws Exception {

        DPBEAParent parent1 = parents.get(0);
        DPBEAParent parent2 = parents.get(1);
        DPBEAParent child = new DPBEAParent();

        /*Crossover Size*/
        int numberofColors1 = parent1.numberofColors();
        int numberofColors2 = parent2.numberofColors();
        int numberofCrossover = (numberofColors1 <= numberofColors2) ? numberofColors1 : numberofColors2;

        /*Initial Values*/
        HashMap<Node, Set<DPBEAColor>> tabu = new HashMap<Node, Set<DPBEAColor>>();
        HashSet<Integer> usedColorIdx1 = new HashSet<>(numberofColors1);
        HashSet<Integer> usedColorIdx2 = new HashSet<>(numberofColors2);
        HashSet<Node> pool = new HashSet<>();
        HashSet<Node> usedNodes = new HashSet<>(graph.getNodeCount());

        if(!parent1.getPpool().isEmpty() ){
            pool.addAll(parent1.getPpool());
            usedNodes.addAll(pool);
        }
        else if(!parent2.getPpool().isEmpty()){
            pool.addAll(parent2.getPpool());
            usedNodes.addAll(pool);
        }

        // LOOP
        for (int j = 0; j < numberofCrossover; j++) {

            // Find two unused colors
            int idx1 = getUnusedIdx(usedColorIdx1, numberofColors1);
            int idx2 = getUnusedIdx(usedColorIdx2, numberofColors2);

            // Mark them as used
            usedColorIdx1.add(idx1);
            usedColorIdx2.add(idx2);

            // Get colors of idx
            DPBEAColor color1 = parent1.getColor(idx1);
            DPBEAColor color2 = parent2.getColor(idx2);

            // If inputs not valid throw an exception
            if (color1 == null || color2 == null){
                throw new Exception("Color1 or/and Color2 is null.");
            }
            if (color1.getNodes().isEmpty() || color2.getNodes().isEmpty()){
                throw new Exception("Color1 or/and Color2 has no node.");
            }

            // Get node lists of colors.
            ArrayList<Node> color1Nodes = color1.getNodes();
            ArrayList<Node> color2Nodes = color2.getNodes();

            // Create color with distinct nodes.
            ArrayList<Node> colorWOPool = new ArrayList<>();
            for (Node node : color1Nodes) {
                if (!usedNodes.contains(node)){
                    colorWOPool.add(node);
                    usedNodes.add(node);
                }
            }
            for (Node node : color2Nodes) {
                if (!usedNodes.contains(node)){
                    colorWOPool.add(node);
                    usedNodes.add(node);
                }
            }

            ArrayList<Node> newColor;

            // If Pool is null, it is first process
            // No need to check pool
            if (!pool.isEmpty()){
                colorWOPool.addAll(pool);
            }

            // Clear pool.
            pool.clear();
            // Set raw data.
            newColor = colorWOPool;

            // Find total number of nodes in new color.
            int totalNodeNum = newColor.size();
            boolean noNeedColor = false;
            if (totalNodeNum < 1){
                // Continue with next.
                // No need to continue.
                continue;
            }
            else if(totalNodeNum == 1){
                Node oneNode = newColor.get(0);
                noNeedColor = false;
                for (DPBEAColor color: child.getColors()) {
                    boolean CF = true;
                    for (Node n: color.getNodes()) {
                        if(oneNode.hasEdgeBetween(n)){
                            CF = false;
                            break;
                        }
                    }
                    if (CF){
                        color.addNode(oneNode);
                        noNeedColor = true;
                        break;
                    }
                }
            }
            else if (totalNodeNum > 1){
                // IF there is more than one node
                // Calculate conflicts.
                for (int q = 0; q < totalNodeNum; q++) {
                    // Continue until
                    // We say OK, new color is conflict free.

                    // Initialize variables
                    // For every conflict calculation loop.
                    int currNumOFNodes = newColor.size();
                    int maxConflictNum = 0;
                    Node maxConflictedNode = null;
                    int[] conflicts = new int[currNumOFNodes];

                    for (int i = 0; i < currNumOFNodes; i++) {

                        // [i] [0] [0] [0] [0]
                        // [0] [k] [0] [0] [0]
                        // [0] [0] [k] [0] [0]
                        // [0] [0] [0] [k] [0]
                        // [0] [0] [0] [0] [k]
                        // ...
                        // [0] [0] [0] [i] [0]
                        // [0] [0] [0] [0] [k]

                        // currNum is filled by
                        // previous loops.
                        int currNum = conflicts[i];
                        Node currNode = newColor.get(i);

                        for (int k = i + 1; k < currNumOFNodes; k++) {
                            Node kNode = newColor.get(k);
                            if (currNode.hasEdgeBetween(kNode)){
                                // No need to increment
                                // conflicts[i] it is done.
                                currNum++;
                                // Fill next elements.
                                // This provides no need to double check.
                                conflicts[k] += 1;
                            }
                        }
                        if (currNum > maxConflictNum){
                            maxConflictNum = currNum;
                            maxConflictedNode = currNode;
                        }
                    }

                    if (maxConflictNum > 0){
                        // Add bit to pool
                        // Delete bit from new color
                        if (maxConflictedNode == null){
                            throw new Exception("Max Conflicted Num > 0, but Node is null !");
                        }
                        pool.add(maxConflictedNode);
                        newColor.remove(maxConflictedNode);
                    } else {
                        break;
                    }
                }
            }

            //Search back
            ArrayList<Node> nodesBack = new ArrayList<Node>();
            if (!pool.isEmpty()&& j > 0){
                for (Node node : pool) {
                    if(!tabu.containsKey(node)){
                        Set<DPBEAColor> tabuColors = new HashSet<DPBEAColor>();
                        tabu.put(node, tabuColors);
                    }
                    for (DPBEAColor dpbeaColor : child.getColors()) {
                        if(!tabu.get(node).contains(dpbeaColor)){
                            boolean conflictFree = true;
                            ArrayList<Node> nodes = dpbeaColor.getNodes();
                            for (Node colNode : nodes) {
                                if (node.hasEdgeBetween(colNode)){
                                    conflictFree = false;
                                    tabu.get(node).add(dpbeaColor);
                                    break;
                                }
                            }
                            if (conflictFree){
                                nodes.add(node);
                                nodesBack.add(node);
                                break;
                            }
                        }

                    }// Each color in child

                }// Each node in pool
            }//pool is not empty
            if(!nodesBack.isEmpty()){
                pool.removeAll(nodesBack);
            }

            if(!noNeedColor){
                // Create a new hard-copy color.
                DPBEAColor dpbeaColor = new DPBEAColor(newColor);
                child.addColor(dpbeaColor);
            }


            // Check is it logical?
            int usedNodeCount = usedNodes.size();
            if (usedNodeCount == graph.getNodeCount()){
                break;
            }

        }

        // Check pool.
        if (pool != null && !pool.isEmpty()){
            // Distribute nodes in pool to colors
            // or create a new color for node.
            for (Node node : pool) {
                boolean isPlaced = false;
                for (DPBEAColor dpbeaColor : child.getColors()) {
                    boolean conflictFree = true;
                    ArrayList<Node> nodes = dpbeaColor.getNodes();
                    for (Node colNode : nodes) {
                        if (node.hasEdgeBetween(colNode)){
                            conflictFree = false;
                            break;
                        }
                    }
                    if (conflictFree){
                        nodes.add(node);
                        isPlaced = true;
                        break;
                    }
                }
                if (!isPlaced){
                    // Create new color
                    ArrayList<Node> nodes = new ArrayList<>();
                    nodes.add(node);
                    DPBEAColor newColor = new DPBEAColor(nodes);
                    child.addColor(newColor);
                }
            }
        }

        return child;
    }

    public static DPBEAParent mutation(DPBEAParent child, Graph graph) throws Exception{
        int minSize = graph.getNodeCount();
        int minConflict = graph.getEdgeCount();
        DPBEAColor minColor = child.getColor(0);
        for (DPBEAColor color: child.getColors()) {
            if(color.getNodes().size() <= minSize){
                int conflict = 0;
                for (Node n: color.getNodes()) {
                    conflict += n.getDegree();
                }
                if(color.getNodes().size() == minSize){
                    if (conflict < minConflict){
                        minSize = color.getNodes().size();
                        minColor = color;
                        minConflict = conflict;
                    }
                    else {
                        continue;
                    }
                }
                else {
                    minSize = color.getNodes().size();
                    minColor = color;
                    minConflict = conflict;
                }
            }
        }
        ArrayList<Node> mutNodes = minColor.getNodes();
        child.getColors().remove(minColor);

        for (Node node : mutNodes) {
            boolean isPlaced = false;
            for (DPBEAColor dpbeaColor : child.getColors()) {
                boolean conflictFree = true;
                ArrayList<Node> nodes = dpbeaColor.getNodes();
                for (Node colNode : nodes) {
                    if (node.hasEdgeBetween(colNode)){
                        conflictFree = false;
                        break;
                    }
                }
                if (conflictFree){
                    nodes.add(node);
                    isPlaced = true;
                    //    System.out.println(node.getId() + " is placed.");
                    break;
                }
            }
            if (!isPlaced){
                // Create new color
                ArrayList<Node> nodes = new ArrayList<>();
                nodes.add(node);
                DPBEAColor newColor = new DPBEAColor(nodes);
                child.addColor(newColor);
            }
        }

        return child;
    }

    public static DPBEAParent mutationOld(DPBEAParent child, Graph graph) throws Exception {
        int maxConflict = 0;
        Node nMax = null;
        HashSet<Node> pool = new HashSet<>();
        for (Node n: graph.getNodeSet()) {
            int edgeSize = n.getDegree();
            if (edgeSize >= maxConflict){
                maxConflict = edgeSize;
                nMax = n;
            }
        }
        DPBEAColor removeColor = null;
        for (DPBEAColor c: child.getColors()) {
            if(c.getNodes().contains(nMax)){
                c.getNodes().remove(nMax);
                if(c.getNodes().isEmpty())
                    removeColor = c;
                break;
            }
        }
        if(removeColor !=null){
            child.getColors().remove(removeColor);
        }
        Random rand = DPBEAConst.RAND;
        int colorIdx = rand.nextInt(child.numberofColors());
        DPBEAColor color = child.getColor(colorIdx);
        ArrayList<Node> nList = new ArrayList<>();
        for (Node n : color.getNodes() ) {
            if(n.hasEdgeBetween(nMax)){
                nList.add(n);
                pool.add(n);
            }
        }

        for (Node n: nList) {
            color.getNodes().remove(n);
        }

        color.addNode(nMax);
        // Check pool.
        if (pool != null && !pool.isEmpty()){
            // Distribute nodes in pool to colors
            // or create a new color for node.
            for (Node node : pool) {
                boolean isPlaced = false;
                for (DPBEAColor dpbeaColor : child.getColors()) {
                    boolean conflictFree = true;
                    ArrayList<Node> nodes = dpbeaColor.getNodes();
                    for (Node colNode : nodes) {
                        if (node.hasEdgeBetween(colNode)){
                            conflictFree = false;
                            break;
                        }
                    }
                    if (conflictFree){
                        nodes.add(node);
                        isPlaced = true;
                        break;
                    }
                }
                if (!isPlaced){
                    // Create new color
                    ArrayList<Node> nodes = new ArrayList<>();
                    nodes.add(node);
                    DPBEAColor newColor = new DPBEAColor(nodes);
                    child.addColor(newColor);
                }
            }
        }


        return child;
    }


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static void writeLine(String fileName, String content){
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {

            //Specify the file name and path here
            File file = new File(fileName);

	 /* This logic will make sure that the file
	  * gets created if it is not present at the
	  * specified location*/
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(content);
            System.out.println("File written Successfully");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally
        {
            try{
                if(bw!=null)
                    bw.close();
            }catch(Exception ex){
                System.out.println("Error in closing the BufferedWriter"+ex);
            }
        }
    }
}
