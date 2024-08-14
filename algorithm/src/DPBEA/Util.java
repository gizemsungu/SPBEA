package DPBEA;

import OldDPBEA.DPBEAParent;
import com.sun.xml.internal.bind.v2.model.core.ID;
import monica.Individual;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.io.*;
import java.util.*;

public class Util {
    public static boolean LocalSearchUsed = false;
    public static boolean SearchBackUsed = false;
    public static boolean SimilarityUsed = false;
    public static boolean OtherUsed = false;

    public static ArrayList<Parent> selectParentsRandom(Population population) throws Exception{
        Random rand = DPBEAConst.RAND;
        ArrayList<Parent> parents = population.getParents();
        int parentSize = parents.size();

        /* Randomly select first and second parents */
        int firstIdx = rand.nextInt(parentSize);
        int secondIdx = rand.nextInt(parentSize);

		/* They can not be the same */
        while (firstIdx == secondIdx){
            secondIdx = rand.nextInt(parentSize);
        }

        Parent parent1 = parents.get(firstIdx);
        Parent parent2 = parents.get(secondIdx);
        ArrayList<Parent> twoParents = new ArrayList<>();
        twoParents.add(parent1);
        twoParents.add(parent2);

        if (parent1 == null || parent2 == null){
            throw new Exception("Parent1 or/and Parent2 is null.");
        }
        //   System.out.println("Parent " + firstIdx + " Parent " + secondIdx + " are selected.");

        return twoParents;
    }

    public static void processPopulation(Population population, Graph graph){
        Random rand = DPBEAConst.RAND;
        ArrayList<Parent> twoParents = new ArrayList<>();
        Parent child = null;
        try{
            twoParents = selectParentsRandom(population);
        }catch (Exception e) {
            e.printStackTrace();
        }
        Parent p1 = twoParents.get(0);
        Parent p2 = twoParents.get(1);

        try {

            child = initialCrossover(p1, p2, graph);


        }catch (Exception e) {
            e.printStackTrace();
        }
        try{
            if(child != null && rand.nextDouble() <= .5){
                child = mutation(child, graph);
            }
        }catch (Exception e){
            e.printStackTrace();
        }



        if(child.getColors().size() < p1.getColors().size() || child.getColors().size() < p2.getColors().size()){
            if(p1.getColors().size() > p2.getColors().size()){
                population.getParents().remove(p1);
            }
            else {
                population.getParents().remove(p2);
            }
            population.getParents().add(child);
        }

    }


    public static void processDPBEA2(Population pop, Graph graph, double EDGE_DENSITY){
        Random random = DPBEAConst.RAND;
        for (int j = 50; j < DPBEAConst.INDIVID_STEP ; j++) {
            Parent p1 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent p2 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent child = null;
            boolean newCross = false;
            if(random.nextDouble() > EDGE_DENSITY ){
                child = Util.crossover2(p1, p2, graph);
                newCross = true;
            }
            else {
                try {
                    child = initialCrossover(p1, p2, graph);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(child != null){
                child = Util.mutation(child, graph);
            }


            int p1ColorSize = p1.getColors().size();
            int p2ColorSize = p2.getColors().size();
            int childColorSize = child.getColors().size();

            if(childColorSize < p1ColorSize || childColorSize < p2ColorSize){
                if(p1ColorSize > p2ColorSize){
                    pop.getParents().remove(p1);
                }
                else {
                    pop.getParents().remove(p2);
                }
                pop.getParents().add(child);
            }
          /*  else if(!newCross){
                if(p1ColorSize > p2ColorSize){
                    pop.getParents().remove(p1);
                }
                else {
                    pop.getParents().remove(p2);
                }
                pop.getParents().add(child);
            }*/
            if((j+1) % 50 == 0){
                int index = ((j+1) / 50) -1;
                pop.colorStep[index] = bestParent(pop);
            }
        }
    }

    public static void processDPBEACrossMerged(Population pop, Graph graph){
        pop.sumInitialCross = 0;
        pop.sumSimilarCross = 0;
        pop.goodInitial = 0;
        pop.goodSimilar = 0;
        Random random = DPBEAConst.RAND;
        for (int j = 50; j < DPBEAConst.INDIVID_STEP; j++) {
            Parent p1 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent p2 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent child = null;

            try {
                child = crossoverMerged(p1, p2, graph);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //}
            /*if (child != null) {
                if(random.nextDouble() < DPBEAConst.MUTRATE)
                    child = Util.mutation(child, graph);
            }*/

            int p1ColorSize = p1.getColors().size();
            int p2ColorSize = p2.getColors().size();
            int childColorSize = child.getColors().size();
            if (childColorSize <= p1ColorSize || childColorSize <= p2ColorSize) {
                if (p1ColorSize > p2ColorSize) {
                    pop.getParents().remove(p1);
                } else {
                    pop.getParents().remove(p2);
                }
                pop.getParents().add(child);
            }

            if ((j + 1) % 50 == 0) {
                int index = ((j + 1) / 50) - 1;
                pop.colorStep[index] = bestParent(pop);
            }
        }
    }

    public static void processDPBEACrossInit(Population pop, Graph graph){
        pop.sumInitialCross = 0;
        pop.sumSimilarCross = 0;
        pop.goodInitial = 0;
        pop.goodSimilar = 0;
        Random random = DPBEAConst.RAND;
        for (int j = 50; j < DPBEAConst.INDIVID_STEP; j++) {
            Parent p1 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent p2 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent child = null;

            try {
                child = initialCrossover(p1, p2, graph);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //}
            /*if (child != null) {
                if(random.nextDouble() < DPBEAConst.MUTRATE)
                    child = Util.mutation(child, graph);
            }*/

            int p1ColorSize = p1.getColors().size();
            int p2ColorSize = p2.getColors().size();
            int childColorSize = child.getColors().size();
            if (childColorSize <= p1ColorSize || childColorSize <= p2ColorSize) {
                if (p1ColorSize > p2ColorSize) {
                    pop.getParents().remove(p1);
                } else {
                    pop.getParents().remove(p2);
                }
                pop.getParents().add(child);
            }

            if ((j + 1) % 50 == 0) {
                int index = ((j + 1) / 50) - 1;
                pop.colorStep[index] = bestParent(pop);
            }
        }
    }

    public static void processDPBEA(Population pop, Graph graph, double EDGE_DENSITY) {
        pop.sumInitialCross = 0;
        pop.sumSimilarCross = 0;
        pop.goodInitial = 0;
        pop.goodSimilar = 0;
        Random random = DPBEAConst.RAND;
        for (int j = 50; j < DPBEAConst.INDIVID_STEP; j++) {
            Parent p1 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent p2 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent child = null;

            try {
                child = crossoverMerged(p1, p2, graph);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //}
            if (child != null) {
                if(random.nextDouble() < DPBEAConst.MUTRATE)
                    child = Util.mutation(child, graph);
            }

            int p1ColorSize = p1.getColors().size();
            int p2ColorSize = p2.getColors().size();
            int childColorSize = child.getColors().size();
            if (childColorSize <= p1ColorSize || childColorSize <= p2ColorSize) {
                if (p1ColorSize > p2ColorSize) {
                    pop.getParents().remove(p1);
                } else {
                    pop.getParents().remove(p2);
                }
                pop.getParents().add(child);
            }

            /*if ((j + 1) % 50 == 0) {
                int index = ((j + 1) / 50) - 1;
                pop.colorStep[index] = bestParent(pop);
            }*/
        }
    }

    public static void processDPBEAWOSimilar(Population pop, Graph graph, double EDGE_DENSITY) {
        pop.sumInitialCross = 0;
        pop.sumSimilarCross = 0;
        pop.goodInitial = 0;
        pop.goodSimilar = 0;
        Random random = DPBEAConst.RAND;
        for (int j = 50; j < DPBEAConst.INDIVID_STEP; j++) {
            Parent p1 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent p2 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent child = null;

            try {
                child = crossoverWOSimilar(p1, p2, graph);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //}
            if (child != null) {
                if(random.nextDouble() < DPBEAConst.MUTRATE)
                    child = Util.mutation(child, graph);
            }

            int p1ColorSize = p1.getColors().size();
            int p2ColorSize = p2.getColors().size();
            int childColorSize = child.getColors().size();
            if (childColorSize <= p1ColorSize || childColorSize <= p2ColorSize) {
                if (p1ColorSize > p2ColorSize) {
                    pop.getParents().remove(p1);
                } else {
                    pop.getParents().remove(p2);
                }
                pop.getParents().add(child);
            }

            /*if ((j + 1) % 50 == 0) {
                int index = ((j + 1) / 50) - 1;
                pop.colorStep[index] = bestParent(pop);
            }*/
        }
    }
    public static void processDPBEAWOSimilarMerged(Population pop, Graph graph, double EDGE_DENSITY) {
        pop.sumInitialCross = 0;
        pop.sumSimilarCross = 0;
        pop.goodInitial = 0;
        pop.goodSimilar = 0;
        Random random = DPBEAConst.RAND;
        for (int j = 50; j < DPBEAConst.INDIVID_STEP; j++) {
            Parent p1 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent p2 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent child = null;
            if (j <= DPBEAConst.INDIVID_STEP/2){
                try {
                    child = crossoverWOSimilar(p1, p2, graph);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    child = crossoverMerged(p1, p2, graph);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //}
            if (child != null) {
                if (random.nextDouble() < DPBEAConst.MUTRATE)
                    child = Util.mutation(child, graph);


                int p1ColorSize = p1.getColors().size();
                int p2ColorSize = p2.getColors().size();
                int childColorSize = child.getColors().size();
                if (childColorSize <= p1ColorSize || childColorSize <= p2ColorSize) {
                    if(j <= DPBEAConst.INDIVID_STEP/2){
                        pop.goodInitial++;
                    }
                    else {
                        pop.goodSimilar++;
                    }
                    if (p1ColorSize > p2ColorSize) {
                        pop.getParents().remove(p1);
                    } else {
                        pop.getParents().remove(p2);
                    }
                    pop.getParents().add(child);
                }
            }

            /*if ((j + 1) % 50 == 0) {
                int index = ((j + 1) / 50) - 1;
                pop.colorStep[index] = bestParent(pop);
            }*/
        }
    }


    public static Parent partRep(Graph graph, Individual individual){
        Parent parent = new Parent();
        ArrayList<Color> colors = parent.getColors();
        for (int i = 0; i < individual.size() ; i++) {
            Node newNode = graph.getNode(String.valueOf(individual.get(i)));
            if(colors.isEmpty()){
                Color newColor = new Color();
                newColor.addNode(newNode);
                colors.add(newColor);
                continue;
            }
            boolean noNeedColor = false;
            for (Color color: colors) {
                boolean isPlaced = true;
                for (Node node: color.getNodes()) {
                    if(node.hasEdgeBetween(newNode)){
                        isPlaced = false;
                        break;
                    }
                }
                if(isPlaced){
                    color.addNode(newNode);
                    noNeedColor = true;
                    break;
                }
            }
            if(!noNeedColor){
                Color newColor = new Color();
                newColor.addNode(newNode);
                colors.add(newColor);
            }
        }

        return parent;
    }


    public static void processBenchmark(Population pop, Graph graph, HashMap<String, Individual> monica) {
        pop.sumInitialCross = 0;
        pop.sumSimilarCross = 0;
        pop.goodInitial = 0;
        pop.goodSimilar = 0;
        Random random = DPBEAConst.RAND;
        for (int j = 0; j < DPBEAConst.INDIVID_STEP; j++) {
            LocalSearchUsed = false;
            SearchBackUsed = false;
            SimilarityUsed = false;
            OtherUsed = false;
            Parent p1 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent p2 = pop.getParents().get(random.nextInt(DPBEAConst.POPULATION));
            Parent child = null;

            try {
                child = crossoverMerged(p1, p2, graph);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //}
            if (child != null) {
                if(random.nextDouble() < DPBEAConst.MUTRATE)
                    child = Util.mutation(child, graph);
            }
           // if(child.numberofColors() <= monica.get("dga").getColors() && child.numberofColors() <= monica.get("dsatur").getColors()){
            if( LocalSearchUsed && SimilarityUsed && OtherUsed && SearchBackUsed && child.numberofColors()==3){
                for (Color color: p1.getColors()) {
                    System.out.print(color.getNodes().toString());
                }
                System.out.println();
                for (Color color: p2.getColors()) {
                    System.out.print(color.getNodes().toString());
                }
                System.out.println();
                for (Color color: child.getColors()) {
                    System.out.print(color.getNodes().toString());
                }

                System.out.println();
                Parent parent_dga = partRep(graph, monica.get("dga"));
                for (Color color: parent_dga.getColors()) {
                    System.out.print(color.getNodes().toString());
                }
                System.out.println();
                System.out.println(child.numberofColors() + " " + monica.get("dsatur").getColors() + " " + monica.get("dga").getColors() );
                break;
            }


            int p1ColorSize = p1.getColors().size();
            int p2ColorSize = p2.getColors().size();
            int childColorSize = child.getColors().size();
            if (childColorSize <= p1ColorSize || childColorSize <= p2ColorSize) {
                if (p1ColorSize > p2ColorSize) {
                    pop.getParents().remove(p1);
                } else {
                    pop.getParents().remove(p2);
                }
                pop.getParents().add(child);
            }

            if ((j + 1) % 50 == 0) {
                int index = ((j + 1) / 50) - 1;
                pop.colorStep[index] = bestParent(pop);
            }
        }
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



    public static Parent crossoverMerged(Parent p1, Parent p2, Graph graph){
        Parent child = new Parent();
        HashSet<Node> unusedNodes = new HashSet<>(graph.getNodeSet());
        ArrayList<Node> usedNodes = new ArrayList<>();
        HashMap<Node, Set<Color>> tabu = new HashMap<Node, Set<Color>>();
        Random random = new Random();
        HashSet<Node> Pool = new HashSet<>();
        ArrayList<Node> alfanodes_print = new ArrayList<>();
        while (!unusedNodes.isEmpty()){

            Node alfaNode = getAlfaNode(unusedNodes, p1, p2);
            //System.out.println(alfaNode);
            alfanodes_print.add(alfaNode);
            unusedNodes.remove(alfaNode);

            HashSet<Node> color1Nodes = new HashSet<>(p1.getHashColors().get(alfaNode).getNodes());
            HashSet<Node> color2Nodes = new HashSet<>(p2.getHashColors().get(alfaNode).getNodes());
            color1Nodes.removeAll(usedNodes);
            color2Nodes.removeAll(usedNodes);
            HashSet<Node> commonNodes = new HashSet<>(color1Nodes);
            commonNodes.retainAll(color2Nodes);//common nodes
            color1Nodes.removeAll(commonNodes);//different nodes
            color2Nodes.removeAll(commonNodes);//different nodes
            usedNodes.addAll(color1Nodes);
            usedNodes.addAll(color2Nodes);
            usedNodes.addAll(commonNodes);
            unusedNodes.removeAll(color1Nodes);
            unusedNodes.removeAll(color2Nodes);
            unusedNodes.removeAll(commonNodes);
            Color newColor = new Color();
            color2Nodes.addAll(color1Nodes);

            if(commonNodes.size() >= (color2Nodes.size()/2) && commonNodes.size() > 1){
                SimilarityUsed = true;
                //System.out.println("Similarity");
                HashSet<Node> cNodes = new HashSet<>();
                while (!color2Nodes.isEmpty()){
                    Node maxNode = getMaxNode(color2Nodes);
                    color2Nodes.remove(maxNode);
                    boolean CF = true;
                    if(!newColor.getNodes().isEmpty()){
                        for (Node node: newColor.getNodes()) {
                            if(node.hasEdgeBetween(maxNode)){
                                CF = false;
                                break;
                            }
                        }
                    }
                    if(CF){
                        newColor.getNodes().add(maxNode);
                    }
                    else {
                        cNodes.add(maxNode);
                    }
                }
                newColor.getNodes().addAll(commonNodes);

                while (!Pool.isEmpty()){
                    Node maxNode = getMaxNode(Pool);
                    Pool.remove(maxNode);
                    boolean CF = true;
                    if(!newColor.getNodes().isEmpty()){
                        for (Node node: newColor.getNodes()) {
                            if(node.hasEdgeBetween(maxNode)){
                                CF = false;
                                break;
                            }
                        }
                    }
                    if(CF){
                        newColor.getNodes().add(maxNode);
                    }
                    else {
                        cNodes.add(maxNode);
                    }
                }

                Pool.addAll(cNodes);
            }
            else {
                //System.out.println("Other");
                OtherUsed = true;

                color2Nodes.addAll(commonNodes);
                if(color2Nodes.size()>=2)
                    OtherUsed = true;
                color2Nodes.addAll(Pool);
                Pool.clear();
                ArrayList<Node> colorArr = new ArrayList<>(color2Nodes);


                // Find total number of nodes in new color.
                int totalNodeNum = colorArr.size();
                boolean noNeedColor = false;
                if (totalNodeNum < 1){
                    // Continue with next.
                    // No need to continue.
                    continue;
                }
                else if(totalNodeNum == 1){
                    Node oneNode = colorArr.get(0);
                    noNeedColor = false;
                    for (Color color: child.getColors()) {
                        boolean CF = true;
                        for (Node n: color.getNodes()) {
                            if(oneNode.hasEdgeBetween(n)){
                                CF = false;
                                break;
                            }
                        }
                        if (CF){
                            color.addNode(oneNode);
                            child.updateHash(1, color, oneNode);
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
                        int currNumOFNodes = colorArr.size();
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
                            Node currNode = colorArr.get(i);

                            for (int k = i + 1; k < currNumOFNodes; k++) {
                                Node kNode = colorArr.get(k);
                                if (currNode.hasEdgeBetween(kNode)){
                                    // No need to increment
                                    // conflicts[i] it is done.
                                    currNum++;
                                    // Fill next elements.
                                    // This provides no need to double check.
                                    conflicts[k] += 1;
                                }
                            }
                            if (currNum >= maxConflictNum){
                                if(currNum > maxConflictNum){
                                    maxConflictNum = currNum;
                                    maxConflictedNode = currNode;
                                }
                                else if( currNum == maxConflictNum && maxConflictedNode!= null){
                                    if(currNode.getDegree() < maxConflictedNode.getDegree()){
                                        maxConflictNum = currNum;
                                        maxConflictedNode = currNode;
                                    }
                                    else if(currNode.getDegree() == maxConflictedNode.getDegree()){
                                        if(random.nextDouble() < 0.5){
                                            maxConflictNum = currNum;
                                            maxConflictedNode = currNode;
                                        }
                                    }
                                }

                            }
                        }

                        if (maxConflictNum > 0){
                            // Add bit to pool
                            // Delete bit from new color
                      /*  if (maxConflictedNode == null){
                            throw new Exception("Max Conflicted Num > 0, but Node is null !");
                        }*/
                            Pool.add(maxConflictedNode);
                            colorArr.remove(maxConflictedNode);
                        } else {
                            break;
                        }
                    }
                }


                if(!noNeedColor){
                    // Create a new hard-copy color.
                    HashSet<Node> nodes = new HashSet<>(colorArr);
                    newColor.getNodes().addAll(nodes);
                }
            }



            //Search back
            HashSet<Node> tempPool = new HashSet<>();
            HashSet<Node> nodesBack = new HashSet<>();
            if (!Pool.isEmpty() && !child.getColors().isEmpty()){
                while (!Pool.isEmpty()){
                    Node poolNode = getMaxNode(Pool);
                    if(!tabu.containsKey(poolNode)){
                        Set<Color> tabuColors = new HashSet<>();
                        tabu.put(poolNode, tabuColors);
                    }
                    for (Color dpbeaColor : child.getColors()) {
                        if(!tabu.get(poolNode).contains(dpbeaColor)){
                            boolean conflictFree = true;
                            HashSet<Node> nodes = dpbeaColor.getNodes();
                            for (Node colNode : nodes) {
                                if (poolNode.hasEdgeBetween(colNode)){
                                    conflictFree = false;
                                    tabu.get(poolNode).add(dpbeaColor);
                                    break;
                                }
                            }
                            if (conflictFree){
                                SearchBackUsed = true;
                                nodes.add(poolNode);
                                child.updateHash(1, dpbeaColor, poolNode);
                                nodesBack.add(poolNode);
                                break;
                            }
                        }
                    }// Each color in child
                    Pool.remove(poolNode);
                    tempPool.add(poolNode);
                }// Each node in pool
            }//pool is not empty
            if(!nodesBack.isEmpty())
                tempPool.removeAll(nodesBack);
            if(!tempPool.isEmpty())
                Pool.addAll(tempPool);


            child.addColor(newColor);
        }

        // Check pool.
        while (Pool != null && !Pool.isEmpty()){
            // Distribute nodes in pool to colors
            // or create a new color for node.
            Node node = getMaxNode(Pool);
            boolean isPlaced = false;
            for (Color dpbeaColor : child.getColors()) {
                boolean conflictFree = true;
                HashSet<Node> nodes = dpbeaColor.getNodes();
                for (Node colNode : nodes) {
                    if (node.hasEdgeBetween(colNode)){
                        conflictFree = false;
                        break;
                    }
                }
                if (conflictFree){
                    nodes.add(node);
                    child.updateHash(1, dpbeaColor, node);
                    isPlaced = true;
                    break;
                }
            }
            if (!isPlaced){
                // Create new color
                Color newColor = new Color();
                newColor.addNode(node);
                child.addColor(newColor);
            }
            Pool.remove(node);
        }
        //System.out.println("\nAlfa nodes:" + alfanodes_print.toString());
        return child;

    }

    public static Parent crossoverWOSimilar(Parent p1, Parent p2, Graph graph){
        Parent child = new Parent();
        HashSet<Node> unusedNodes = new HashSet<>(graph.getNodeSet());
        ArrayList<Node> usedNodes = new ArrayList<>();
        HashMap<Node, Set<Color>> tabu = new HashMap<Node, Set<Color>>();
        Random random = new Random();
        HashSet<Node> Pool = new HashSet<>();
        ArrayList<Node> alfanodes_print = new ArrayList<>();
        while (!unusedNodes.isEmpty()){

            Node alfaNode = getAlfaNode(unusedNodes, p1, p2);
            //System.out.println(alfaNode);
            alfanodes_print.add(alfaNode);
            unusedNodes.remove(alfaNode);

            HashSet<Node> color1Nodes = new HashSet<>(p1.getHashColors().get(alfaNode).getNodes());
            HashSet<Node> color2Nodes = new HashSet<>(p2.getHashColors().get(alfaNode).getNodes());
            color1Nodes.removeAll(usedNodes);
            color2Nodes.removeAll(usedNodes);
            HashSet<Node> commonNodes = new HashSet<>(color1Nodes);
            commonNodes.retainAll(color2Nodes);//common nodes
            color1Nodes.removeAll(commonNodes);//different nodes
            color2Nodes.removeAll(commonNodes);//different nodes
            usedNodes.addAll(color1Nodes);
            usedNodes.addAll(color2Nodes);
            usedNodes.addAll(commonNodes);
            unusedNodes.removeAll(color1Nodes);
            unusedNodes.removeAll(color2Nodes);
            unusedNodes.removeAll(commonNodes);
            Color newColor = new Color();
            color2Nodes.addAll(color1Nodes);


                //System.out.println("Other");
                OtherUsed = true;

                color2Nodes.addAll(commonNodes);
                if(color2Nodes.size()>=2)
                    OtherUsed = true;
                color2Nodes.addAll(Pool);
                Pool.clear();
                ArrayList<Node> colorArr = new ArrayList<>(color2Nodes);


                // Find total number of nodes in new color.
                int totalNodeNum = colorArr.size();
                boolean noNeedColor = false;
                if (totalNodeNum < 1){
                    // Continue with next.
                    // No need to continue.
                    continue;
                }
                else if(totalNodeNum == 1){
                    Node oneNode = colorArr.get(0);
                    noNeedColor = false;
                    for (Color color: child.getColors()) {
                        boolean CF = true;
                        for (Node n: color.getNodes()) {
                            if(oneNode.hasEdgeBetween(n)){
                                CF = false;
                                break;
                            }
                        }
                        if (CF){
                            color.addNode(oneNode);
                            child.updateHash(1, color, oneNode);
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
                        int currNumOFNodes = colorArr.size();
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
                            Node currNode = colorArr.get(i);

                            for (int k = i + 1; k < currNumOFNodes; k++) {
                                Node kNode = colorArr.get(k);
                                if (currNode.hasEdgeBetween(kNode)){
                                    // No need to increment
                                    // conflicts[i] it is done.
                                    currNum++;
                                    // Fill next elements.
                                    // This provides no need to double check.
                                    conflicts[k] += 1;
                                }
                            }
                            if (currNum >= maxConflictNum){
                                if(currNum > maxConflictNum){
                                    maxConflictNum = currNum;
                                    maxConflictedNode = currNode;
                                }
                                else if( currNum == maxConflictNum && maxConflictedNode!= null){
                                    if(currNode.getDegree() < maxConflictedNode.getDegree()){
                                        maxConflictNum = currNum;
                                        maxConflictedNode = currNode;
                                    }
                                    else if(currNode.getDegree() == maxConflictedNode.getDegree()){
                                        if(random.nextDouble() < 0.5){
                                            maxConflictNum = currNum;
                                            maxConflictedNode = currNode;
                                        }
                                    }
                                }

                            }
                        }

                        if (maxConflictNum > 0){
                            // Add bit to pool
                            // Delete bit from new color
                      /*  if (maxConflictedNode == null){
                            throw new Exception("Max Conflicted Num > 0, but Node is null !");
                        }*/
                            Pool.add(maxConflictedNode);
                            colorArr.remove(maxConflictedNode);
                        } else {
                            break;
                        }
                    }
                }


                if(!noNeedColor){
                    // Create a new hard-copy color.
                    HashSet<Node> nodes = new HashSet<>(colorArr);
                    newColor.getNodes().addAll(nodes);
                }




            //Search back
            HashSet<Node> tempPool = new HashSet<>();
            HashSet<Node> nodesBack = new HashSet<>();
            if (!Pool.isEmpty() && !child.getColors().isEmpty()){
                while (!Pool.isEmpty()){
                    Node poolNode = getMaxNode(Pool);
                    if(!tabu.containsKey(poolNode)){
                        Set<Color> tabuColors = new HashSet<>();
                        tabu.put(poolNode, tabuColors);
                    }
                    for (Color dpbeaColor : child.getColors()) {
                        if(!tabu.get(poolNode).contains(dpbeaColor)){
                            boolean conflictFree = true;
                            HashSet<Node> nodes = dpbeaColor.getNodes();
                            for (Node colNode : nodes) {
                                if (poolNode.hasEdgeBetween(colNode)){
                                    conflictFree = false;
                                    tabu.get(poolNode).add(dpbeaColor);
                                    break;
                                }
                            }
                            if (conflictFree){
                                SearchBackUsed = true;
                                nodes.add(poolNode);
                                child.updateHash(1, dpbeaColor, poolNode);
                                nodesBack.add(poolNode);
                                break;
                            }
                        }
                    }// Each color in child
                    Pool.remove(poolNode);
                    tempPool.add(poolNode);
                }// Each node in pool
            }//pool is not empty
            if(!nodesBack.isEmpty())
                tempPool.removeAll(nodesBack);
            if(!tempPool.isEmpty())
                Pool.addAll(tempPool);


            child.addColor(newColor);
        }

        // Check pool.
        while (Pool != null && !Pool.isEmpty()){
            // Distribute nodes in pool to colors
            // or create a new color for node.
            Node node = getMaxNode(Pool);
            boolean isPlaced = false;
            for (Color dpbeaColor : child.getColors()) {
                boolean conflictFree = true;
                HashSet<Node> nodes = dpbeaColor.getNodes();
                for (Node colNode : nodes) {
                    if (node.hasEdgeBetween(colNode)){
                        conflictFree = false;
                        break;
                    }
                }
                if (conflictFree){
                    nodes.add(node);
                    child.updateHash(1, dpbeaColor, node);
                    isPlaced = true;
                    break;
                }
            }
            if (!isPlaced){
                // Create new color
                Color newColor = new Color();
                newColor.addNode(node);
                child.addColor(newColor);
            }
            Pool.remove(node);
        }
        //System.out.println("\nAlfa nodes:" + alfanodes_print.toString());
        return child;

    }
    public static Parent initialCrossover(Parent parent1, Parent parent2, Graph graph){

        Parent child = new Parent();

        /*Crossover Size*/
        int numberofColors1 = parent1.numberofColors();
        int numberofColors2 = parent2.numberofColors();
        int numberofCrossover = (numberofColors1 <= numberofColors2) ? numberofColors1 : numberofColors2;

        /*Initial Values*/
        HashMap<Node, Set<Color>> tabu = new HashMap<Node, Set<Color>>();
        HashSet<Integer> usedColorIdx1 = new HashSet<>(numberofColors1);
        HashSet<Integer> usedColorIdx2 = new HashSet<>(numberofColors2);
        HashSet<Node> pool = new HashSet<>();
        HashSet<Node> usedNodes = new HashSet<>(graph.getNodeCount());

      /*  if(!parent1.getPpool().isEmpty() ){
            pool.addAll(parent1.getPpool());
            usedNodes.addAll(pool);
        }
        if(!parent2.getPpool().isEmpty()){
            parent1.getPpool().retainAll(parent2.getPpool());
            parent2.getPpool().removeAll(parent1.getPpool());
            pool.addAll(parent2.getPpool());
            usedNodes.addAll(pool);
        }*/

        // LOOP
        for (int j = 0; j < numberofCrossover; j++) {

            // Find two unused colors
            int idx1 = getUnusedIdx(usedColorIdx1, numberofColors1);
            int idx2 = getUnusedIdx(usedColorIdx2, numberofColors2);
            // Mark them as used
            usedColorIdx1.add(idx1);
            usedColorIdx2.add(idx2);

            // Get colors of idx
            Color color1 = parent1.getColors().get(idx1);
            Color color2 = parent2.getColors().get(idx2);

            // Get node lists of colors.
            ArrayList<Node> color1Nodes = new ArrayList<>(color1.getNodes());
            ArrayList<Node> color2Nodes = new ArrayList<>(color2.getNodes());

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
                for (Color color: child.getColors()) {
                    boolean CF = true;
                    for (Node n: color.getNodes()) {
                        if(oneNode.hasEdgeBetween(n)){
                            CF = false;
                            break;
                        }
                    }
                    if (CF){
                        color.addNode(oneNode);
                        child.updateHash(1, color, oneNode);
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
                        if (currNum >= maxConflictNum){
                            if(currNum > maxConflictNum){
                                maxConflictNum = currNum;
                                maxConflictedNode = currNode;
                            }
                            else if( currNum == maxConflictNum && maxConflictedNode!= null){
                                if(currNode.getDegree() < maxConflictedNode.getDegree()){
                                    maxConflictNum = currNum;
                                    maxConflictedNode = currNode;
                                }
                                else if(currNode.getDegree() == maxConflictedNode.getDegree()){
                                    Random random = DPBEAConst.RAND;
                                    if(random.nextDouble() < 0.5){
                                        maxConflictNum = currNum;
                                        maxConflictedNode = currNode;
                                    }
                                }
                            }

                        }
                    }

                    if (maxConflictNum > 0){
                        // Add bit to pool
                        // Delete bit from new color
                      /*  if (maxConflictedNode == null){
                            throw new Exception("Max Conflicted Num > 0, but Node is null !");
                        }*/
                        pool.add(maxConflictedNode);
                        newColor.remove(maxConflictedNode);
                    } else {
                        break;
                    }
                }
            }

            //Search back
            HashSet<Node> tempPool = new HashSet<>();
            HashSet<Node> nodesBack = new HashSet<>();
            if (!pool.isEmpty() && !child.getColors().isEmpty()){
                while (!pool.isEmpty()){
                    Node poolNode = getMaxNode(pool);
                    if(!tabu.containsKey(poolNode)){
                        Set<Color> tabuColors = new HashSet<>();
                        tabu.put(poolNode, tabuColors);
                    }
                    for (Color dpbeaColor : child.getColors()) {
                        if(!tabu.get(poolNode).contains(dpbeaColor)){
                            boolean conflictFree = true;
                            HashSet<Node> nodes = dpbeaColor.getNodes();
                            for (Node colNode : nodes) {
                                if (poolNode.hasEdgeBetween(colNode)){
                                    conflictFree = false;
                                    tabu.get(poolNode).add(dpbeaColor);
                                    break;
                                }
                            }
                            if (conflictFree){
                                nodes.add(poolNode);
                                child.updateHash(1, dpbeaColor, poolNode);
                                nodesBack.add(poolNode);
                                break;
                            }
                        }
                    }// Each color in child
                    pool.remove(poolNode);
                    tempPool.add(poolNode);
                }// Each node in pool
            }//pool is not empty
            if(!nodesBack.isEmpty())
                tempPool.removeAll(nodesBack);
            if(!tempPool.isEmpty())
                pool.addAll(tempPool);

            if(!noNeedColor){
                // Create a new hard-copy color.
                HashSet<Node> nodes = new HashSet<>(newColor);
                Color dpbeaColor = new Color();
                dpbeaColor.getNodes().addAll(nodes);
                child.addColor(dpbeaColor);
            }


            // Check is it logical?
            int usedNodeCount = usedNodes.size();
            if (usedNodeCount == graph.getNodeCount()){
                break;
            }

        }

        // Check pool.
        while (pool != null && !pool.isEmpty()){
            // Distribute nodes in pool to colors
            // or create a new color for node.
            Node node = getMaxNode(pool);
            boolean isPlaced = false;
            for (Color dpbeaColor : child.getColors()) {
                boolean conflictFree = true;
                HashSet<Node> nodes = dpbeaColor.getNodes();
                for (Node colNode : nodes) {
                    if (node.hasEdgeBetween(colNode)){
                        conflictFree = false;
                        break;
                    }
                }
                if (conflictFree){
                    nodes.add(node);
                    child.updateHash(1, dpbeaColor, node);
                    isPlaced = true;
                    break;
                }
            }
            if (!isPlaced){
                // Create new color
                Color newColor = new Color();
                newColor.addNode(node);
                child.addColor(newColor);
            }
            pool.remove(node);
        }

        return child;
    }
    public static Parent crossover(Parent p1, Parent p2, Graph graph){
        Parent child = new Parent();
        HashSet<Node> unusedNodes = new HashSet<>(graph.getNodeSet());
        ArrayList<Node> usedNodes = new ArrayList<>();
        HashMap<Node, Set<Color>> tabu = new HashMap<Node, Set<Color>>();
        Random random = new Random();
        HashSet<Node> Pool = new HashSet<>();

        while (!unusedNodes.isEmpty()){
            Node alfaNode = getAlfaNode(unusedNodes, p1, p2);
            unusedNodes.remove(alfaNode);
            if(!p1.getHashColors().containsKey(alfaNode) ){
                System.out.println("alfaNode: " + alfaNode);
            }
            if(!p2.getHashColors().containsKey(alfaNode) ){
                System.out.println("alfaNode: " + alfaNode);
            }

            HashSet<Node> color1Nodes = new HashSet<>(p1.getHashColors().get(alfaNode).getNodes());
            HashSet<Node> color2Nodes = new HashSet<>(p2.getHashColors().get(alfaNode).getNodes());
            color1Nodes.removeAll(usedNodes);
            color2Nodes.removeAll(usedNodes);
            HashSet<Node> commonNodes = new HashSet<>(color1Nodes);
            commonNodes.retainAll(color2Nodes);//common nodes
            System.out.println("alfaNode: " + alfaNode.getId() +" commonNodes: " + commonNodes + " p1 size: " + p1.numberofColors() + " p2 size: " + p2.numberofColors());
            color1Nodes.removeAll(commonNodes);//different nodes
            color2Nodes.removeAll(commonNodes);//different nodes
            usedNodes.addAll(color1Nodes);
            usedNodes.addAll(color2Nodes);
            usedNodes.addAll(commonNodes);
            unusedNodes.removeAll(color1Nodes);
            unusedNodes.removeAll(color2Nodes);
            unusedNodes.removeAll(commonNodes);
            Color newColor = new Color();
            color2Nodes.addAll(color1Nodes);

            HashSet<Node> cNodes = new HashSet<>();

            while (!color2Nodes.isEmpty()){
                Node maxNode = getMaxNode(color2Nodes);
                color2Nodes.remove(maxNode);
                boolean CF = true;
                if(!newColor.getNodes().isEmpty()){
                    for (Node node: newColor.getNodes()) {
                        if(node.hasEdgeBetween(maxNode)){
                            CF = false;
                            break;
                        }
                    }
                }
                if(CF){
                    newColor.getNodes().add(maxNode);
                }
                else {
                    cNodes.add(maxNode);
                }
            }

            newColor.getNodes().addAll(commonNodes);

            while (!Pool.isEmpty()){
                Node maxNode = getMaxNode(Pool);
                Pool.remove(maxNode);
                boolean CF = true;
                if(!newColor.getNodes().isEmpty()){
                    for (Node node: newColor.getNodes()) {
                        if(node.hasEdgeBetween(maxNode)){
                            CF = false;
                            break;
                        }
                    }
                }
                if(CF){
                    newColor.getNodes().add(maxNode);
                }
                else {
                    cNodes.add(maxNode);
                }
            }

            Pool.addAll(cNodes);

            //Search back
            HashSet<Node> tempPool = new HashSet<>();
            HashSet<Node> nodesBack = new HashSet<>();
            if (!Pool.isEmpty() && !child.getColors().isEmpty()){
                while (!Pool.isEmpty()){
                    Node poolNode = getMaxNode(Pool);
                    if(!tabu.containsKey(poolNode)){
                        Set<Color> tabuColors = new HashSet<>();
                        tabu.put(poolNode, tabuColors);
                    }
                    for (Color dpbeaColor : child.getColors()) {
                        if(!tabu.get(poolNode).contains(dpbeaColor)){
                            boolean conflictFree = true;
                            HashSet<Node> nodes = dpbeaColor.getNodes();
                            for (Node colNode : nodes) {
                                if (poolNode.hasEdgeBetween(colNode)){
                                    conflictFree = false;
                                    tabu.get(poolNode).add(dpbeaColor);
                                    break;
                                }
                            }
                            if (conflictFree){
                                nodes.add(poolNode);
                                child.updateHash(1, dpbeaColor, poolNode);
                                nodesBack.add(poolNode);
                                break;
                            }
                        }
                    }// Each color in child
                    Pool.remove(poolNode);
                    tempPool.add(poolNode);
                }// Each node in pool
            }//pool is not empty
            if(!nodesBack.isEmpty())
                tempPool.removeAll(nodesBack);
            if(!tempPool.isEmpty())
                Pool.addAll(tempPool);

            child.addColor(newColor);
        }

        // Check pool.
        while (Pool != null && !Pool.isEmpty()){
            // Distribute nodes in pool to colors
            // or create a new color for node.
            Node node = getMaxNode(Pool);
            boolean isPlaced = false;
            for (Color dpbeaColor : child.getColors()) {
                boolean conflictFree = true;
                HashSet<Node> nodes = dpbeaColor.getNodes();
                for (Node colNode : nodes) {
                    if (node.hasEdgeBetween(colNode)){
                        conflictFree = false;
                        break;
                    }
                }
                if (conflictFree){
                    nodes.add(node);
                    child.updateHash(1, dpbeaColor, node);
                    isPlaced = true;
                    break;
                }
            }
            if (!isPlaced){
                // Create new color
                Color newColor = new Color();
                newColor.addNode(node);
                child.addColor(newColor);
            }
            Pool.remove(node);
        }
        System.out.println(" child size: " + child.numberofColors());
        return child;
    }

    public static Parent crossover2(Parent p1, Parent p2, Graph graph){
        Parent child = new Parent();
        ArrayList<Node> unusedNodes = new ArrayList<>(graph.getNodeSet());
        ArrayList<Node> usedNodes = new ArrayList<>();
        HashMap<Node, Set<Color>> tabu = new HashMap<Node, Set<Color>>();
        Random random = new Random();
        HashSet<Node> Pool = new HashSet<>();
        while (!unusedNodes.isEmpty()){
            int alfaIdx = random.nextInt(unusedNodes.size());
            Node alfaNode = unusedNodes.get(alfaIdx);
            unusedNodes.remove(alfaNode);
            if(!p1.getHashColors().containsKey(alfaNode) ){
                System.out.println("alfaNode: " + alfaNode);
            }
            if(!p2.getHashColors().containsKey(alfaNode) ){
                System.out.println("alfaNode: " + alfaNode);
            }

            HashSet<Node> color1Nodes = new HashSet<>(p1.getHashColors().get(alfaNode).getNodes());
            HashSet<Node> color2Nodes = new HashSet<>(p2.getHashColors().get(alfaNode).getNodes());
            color1Nodes.removeAll(usedNodes);
            color2Nodes.removeAll(usedNodes);
            HashSet<Node> commonNodes = new HashSet<>(color1Nodes);
            commonNodes.retainAll(color2Nodes);//common nodes
            color1Nodes.removeAll(commonNodes);//different nodes
            color2Nodes.removeAll(commonNodes);//different nodes
            usedNodes.addAll(color1Nodes);
            usedNodes.addAll(color2Nodes);
            usedNodes.addAll(commonNodes);
            unusedNodes.removeAll(color1Nodes);
            unusedNodes.removeAll(color2Nodes);
            unusedNodes.removeAll(commonNodes);
            Color newColor = new Color();
            boolean poolAdded = false;
            if(!Pool.isEmpty()){
                newColor.getNodes().addAll(commonNodes);
                poolAdded = true;
            }
            color2Nodes.addAll(Pool);
            color2Nodes.addAll(color1Nodes);
            Pool.clear();


            while (!color2Nodes.isEmpty()){
                Node maxNode = getMaxNode(color2Nodes);
                color2Nodes.remove(maxNode);
                boolean CF = true;
                if(!newColor.getNodes().isEmpty()){
                    for (Node node: newColor.getNodes()) {
                        if(node.hasEdgeBetween(maxNode)){
                            CF = false;
                            break;
                        }
                    }
                }
                if(CF){
                    newColor.getNodes().add(maxNode);
                }
                else {
                    Pool.add(maxNode);
                }
            }

            if(!poolAdded)
                newColor.getNodes().addAll(commonNodes);

            //Search back
            ArrayList<Node> nodesBack = new ArrayList<Node>();
            if (!Pool.isEmpty() && !child.getColors().isEmpty()){
                for (Node node : Pool) {
                    if(!tabu.containsKey(node)){
                        Set<Color> tabuColors = new HashSet<Color>();
                        tabu.put(node, tabuColors);
                    }
                    for (Color dpbeaColor : child.getColors()) {
                        if(!tabu.get(node).contains(dpbeaColor)){
                            boolean conflictFree = true;
                            HashSet<Node> nodes = dpbeaColor.getNodes();
                            for (Node colNode : nodes) {
                                if (node.hasEdgeBetween(colNode)){
                                    conflictFree = false;
                                    tabu.get(node).add(dpbeaColor);
                                    break;
                                }
                            }
                            if (conflictFree){
                                nodes.add(node);
                                child.updateHash(1, dpbeaColor, node);
                                nodesBack.add(node);
                                break;
                            }
                        }

                    }// Each color in child

                }// Each node in pool
            }//pool is not empty

            if(!nodesBack.isEmpty()){
                Pool.removeAll(nodesBack);
            }
            child.addColor(newColor);
        }

        // Check pool.
        if (Pool != null && !Pool.isEmpty()){
            // Distribute nodes in pool to colors
            // or create a new color for node.
            for (Node node : Pool) {
                boolean isPlaced = false;
                for (Color dpbeaColor : child.getColors()) {
                    boolean conflictFree = true;
                    HashSet<Node> nodes = dpbeaColor.getNodes();
                    for (Node colNode : nodes) {
                        if (node.hasEdgeBetween(colNode)){
                            conflictFree = false;
                            break;
                        }
                    }
                    if (conflictFree){
                        nodes.add(node);
                        child.updateHash(1, dpbeaColor, node);
                        isPlaced = true;
                        break;
                    }
                }
                if (!isPlaced){
                    // Create new color
                    Color newColor = new Color();
                    newColor.addNode(node);
                    child.addColor(newColor);
                }
            }
        }
        return child;
    }

    public static Node getMaxNode(HashSet<Node> nodes){
        int maxDegree = -1;
        Node maxNode = null;
        ArrayList<Node> maxNodes = new ArrayList<>();
        for (Node node: nodes) {
            if(node.getDegree() >= maxDegree){
                if(node.getDegree() > maxDegree){
                    maxDegree = node.getDegree();
                    maxNode = node;
                    maxNodes.clear();
                    maxNodes.add(node);
                }
                else{
                    maxNodes.add(node);
                }

            }
        }
        if(maxNodes.size() > 1){
            Random random = DPBEAConst.RAND;
            maxNode = maxNodes.get(random.nextInt(maxNodes.size()));
        }
        return maxNode;
    }

    public static Node getAlfaNode(HashSet<Node> nodes, Parent p1, Parent p2){
   /*     int minDegree = Integer.MAX_VALUE;
        Node minNode = null;
        ArrayList<Node> minNodes = new ArrayList<>();
        for (Node node: nodes) {
            if(node.getDegree() <= minDegree){
                if(node.getDegree() < minDegree){
                    minDegree = node.getDegree();
                    minNode = node;
                    minNodes.clear();
                    minNodes.add(node);
                }
                else{
                    minNodes.add(node);
                }

            }
        }
        if(minNodes.size() > 1){
            Random random = DPBEAConst.RAND;
            minNode = minNodes.get(random.nextInt(minNodes.size()));
        }*/
        Random rand = DPBEAConst.RAND;
        ArrayList<Node> nodeArr = new ArrayList<>(nodes);
        ArrayList<Node> nodeArr2 = new ArrayList<>(nodes);
        boolean found = false;
        while (!found && !nodeArr.isEmpty()){
            int Idx = rand.nextInt(nodeArr.size());
            Node alfa = nodeArr.get(Idx);
            HashSet<Node> color1Nodes = new HashSet<>(p1.getHashColors().get(alfa).getNodes());
            color1Nodes.retainAll(p2.getHashColors().get(alfa).getNodes());
            if(color1Nodes.size() > 2){
                found = true;
                return alfa;
            }
            else {
                nodeArr.remove(alfa);
            }
        }
        int id = rand.nextInt(nodeArr2.size());

        return nodeArr2.get(id);

    }

    public static Parent mutation(Parent child, Graph graph){
        int minSize = graph.getNodeCount();
        int minConflict = graph.getEdgeCount();
        Color minColor = child.getColors().get(0);
        for (Color color: child.getColors()) {
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
        HashSet<Node> mutNodes = minColor.getNodes();
        child.removeColor(minColor);


        while (!mutNodes.isEmpty()){
            Node node = getMaxNode(mutNodes);
            boolean isPlaced = false;
            for (Color dpbeaColor : child.getColors()) {
                boolean conflictFree = true;
                HashSet<Node> nodes = dpbeaColor.getNodes();
                int conflict = 0;
                HashSet<Node> minConflictNodes = new HashSet<>();
                for (Node colNode : nodes) {
                    if (node.hasEdgeBetween(colNode)){
                        conflict += colNode.getDegree();
                        minConflictNodes.add(colNode);
                        if(conflict >= node.getDegree()){
                            conflictFree = false;
                            break;
                        }
                    }
                }
                if (conflictFree){
                    nodes.add(node);
                    child.updateHash(1, dpbeaColor, node);
                    isPlaced = true;
                    LocalSearchUsed = true;
                    if(!minConflictNodes.isEmpty()){
                        child.updateHash(2, dpbeaColor, minConflictNodes);
                        dpbeaColor.getNodes().removeAll(minConflictNodes);
                        mutNodes.addAll(minConflictNodes);
                    }
                    break;
                }
            }
            if (!isPlaced){
                // Create new color
                Color newColor = new Color();
                newColor.addNode(node);
                child.addColor(newColor);
            }
            mutNodes.remove(node);
        }

        return child;
    }

    public static Parent mutationIterative(Parent child, Graph graph){
        int minSize = graph.getNodeCount();
        int minConflict = graph.getEdgeCount();
        Color minColor = child.getColors().get(0);
        ArrayList<Color> minColors = new ArrayList<>();
        for (Color color: child.getColors()) {
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
                        minColors.clear();
                        minColors.add(minColor);
                    }
                    else if(conflict == minConflict){
                        minColors.add(color);
                    }
                    else {
                        continue;
                    }
                }
                else {
                    minSize = color.getNodes().size();
                    minColor = color;
                    minConflict = conflict;
                    minColors.clear();
                    minColors.add(minColor);
                }
            }
        }
        if(minColors.size() > 1){
            Random rand = DPBEAConst.RAND;
            minColor = minColors.get(rand.nextInt(minColors.size()));
        }
        HashSet<Node> mutNodes = minColor.getNodes();
        child.getColors().remove(minColor);
        child.updateHash(2, minColor, mutNodes);

        while (!mutNodes.isEmpty()){
            Node maxNode = getMaxNode(mutNodes);
            boolean isPlaced = false;
            HashSet<Node> minConflictNodes = null;
            minConflict = graph.getEdgeCount();
            minColor = null;
            for (Color color: child.getColors()) {
                boolean CF = true;
                int conflict = 0;
                HashSet<Node> conflictNodes = new HashSet<>();
                HashSet<Node> nodes = color.getNodes();
                for (Node node: nodes) {
                    if (node.hasEdgeBetween(maxNode)){
                        conflict += node.getDegree();
                        conflictNodes.add(node);
                        CF = false;
                    }
                }
                if(CF){
                    color.addNode(maxNode);
                    child.updateHash(1, color, maxNode);
                    isPlaced = true;
                    mutNodes.remove(maxNode);
                    break;
                }
                else if(minConflict > conflict){
                    minConflict = conflict;
                    minConflictNodes = conflictNodes;
                    minColor = color;
                }
            }
            if(!isPlaced){
                if(minConflict <= maxNode.getDegree()){
                    minColor.getNodes().removeAll(minConflictNodes);
                    child.updateHash(2, minColor, minConflictNodes);
                    mutNodes.addAll(minConflictNodes);
                    minColor.addNode(maxNode);
                    child.updateHash(1, minColor, maxNode);
                    mutNodes.remove(maxNode);
                }
                else {
                    // Create new color
                    Color newColor = new Color();
                    newColor.addNode(maxNode);
                    child.addColor(newColor);
                    mutNodes.remove(maxNode);
                }
            }

        }

        return child;
    }

    public static boolean verify(Parent child, Graph graph){
        boolean CF = true;
        int size = 0;
        for (Color color: child.getColors()) {
            for (Node node: color.getNodes()) {
                size++;
                for (Node node2: color.getNodes()) {
                    if(!node.getId().equals(node2.getId())){
                        if(node.hasEdgeBetween(node2)){
                            CF = false;
                            break;
                        }
                    }
                }
                if(!CF){
                    break;
                }
            }
            if(!CF){
                break;
            }
        }
        if(graph.getNodeCount() != size){
            System.out.println("eksik!");

        }
        return CF;
    }

    public static int bestParent(Population pop){
        int bestColor = Integer.MAX_VALUE;
        Parent best = null;
        for (Parent parent: pop.getParents()) {
            if(parent.getColors().size() < bestColor){
                bestColor = parent.getColors().size();
                best = parent;
            }
        }
        return bestColor;
    }

    public static int bestParent(Population pop, Graph graph){
        int bestColor = Integer.MAX_VALUE;
        Parent best = null;
        for (Parent parent: pop.getParents()) {
            if(parent.getColors().size() < bestColor){
                bestColor = parent.getColors().size();
                best = parent;
            }
        }
        verify(best,graph);
        return bestColor;
    }

    public static void InitialProcessPopulation(Graph graph, Population population){
        int populationSize = DPBEAConst.POPULATION;
        ArrayList<Parent> parents = population.getParents();

        while (populationSize != 0){
            ArrayList<Parent> twoParents = new ArrayList<>();
            twoParents.add(parents.get(0));
            twoParents.add(parents.get(1));

            Parent child = null;
            Parent child2 = null;

            try {
                child = initialCrossover(twoParents.get(0), twoParents.get(1), graph);

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                child2 = initialCrossover(twoParents.get(0), twoParents.get(1), graph);

            } catch (Exception e) {
                e.printStackTrace();
            }

            parents.removeAll(twoParents);

            parents.add(child);
            parents.add(child2);
            populationSize -=2;

        }
        population.colorStep[0] = bestParent(population);

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

    public static Graph readFileStatic(String FILENAME, Graph graph) {
        BufferedReader br = null;
        FileReader fr = null;


        try {


            fr = new FileReader(FILENAME);
            br = new BufferedReader(fr);
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if(sCurrentLine.startsWith("e")) {
                    String[] parts = sCurrentLine.split("    |\\     ");
                    for (int i = 0; i < parts.length ; i++) {
                        System.out.println(parts[i]);
                    }
                    String node1 = parts[1];
                    String node2 = parts[2];
                    if(node1.equals(node2)){
                        continue;
                    }
                    if(node1.contains(" ")){
                        node1 = node1.replaceAll(" ", "");
                    }
                    if(node2.contains(" ")){
                        node2 = node2.replaceAll(" ", "");
                    }
                    node1 = String.valueOf(Integer.parseInt(node1)-1);
                    node2 = String.valueOf(Integer.parseInt(node2)-1);
                    System.out.print(node1 + " " + node2 + "\n");
                    Node n1 = null;
                    Node n2 = null;

                    if(graph.getNode(node1) == null){
                        n1 = graph.addNode(node1);
                    }
                    else {
                        n1 = graph.getNode(node1);
                    }

                    if(graph.getNode(node2) == null){
                        n2 = graph.addNode(node2);
                    }
                    else {
                        n2 = graph.getNode(node2);
                    }


                    Edge newEdge = null;

                    if(!n1.hasEdgeBetween(n2)){
                        newEdge = graph.addEdge(n1.getId()+"--"+n2.getId(),n1.getId(),n2.getId());
                    }

                }

            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }
        }
        return graph;
    }

}




