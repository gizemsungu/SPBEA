package monica;

import java.util.*;

public class Individual extends ArrayList<Integer> implements Comparable<Individual>{
    static final Random rand = new Random();
    int fitness = Integer.MAX_VALUE;
    double normFitness;
    double accumFitness;
    int colors;

    public int getColors() {
        return colors;
    }

    public Individual(int size){
        super();
        for (int i=0;i<size;i++)
            this.add(i);
        Collections.shuffle(this);
        fitness = Integer.MAX_VALUE;
    }

    public Individual (Individual toCopy){
        super();
        this.addAll(toCopy);
        fitness = Integer.MAX_VALUE;
    }

    public Individual() {
        super();
        fitness = Integer.MAX_VALUE;
    }

    //returns true if numbers are a valid permuation of 0 - (size-1) inclusive
    public boolean isValid(){
        for (int i=0;i<size();i++)
            if (Collections.frequency(this,i)!=1)
                return false;
        return true;
    }

    //seems to work
    public static void ox(Individual parentOne, Individual parentTwo,Individual childOne, Individual childTwo){
        //System.out.println(parentOne);
        //System.out.println(parentTwo);
        int size = parentOne.size();
        int index1 = rand.nextInt(size);
        //int index1 = 4;
        int index2;
        do{
            index2= rand.nextInt(size);
            //index2 =7;
        }while(index1==index2);

        childOne.ensureCapacity(size);
        childTwo.ensureCapacity(size);
        childOne.clear();
        childTwo.clear();

        while(childOne.size()<size)
            childOne.add(-1);
        while(childTwo.size()<size)
            childTwo.add(-1);

        //copies area between the crossover points
        int index = index1;
        while (index != index2){
            childOne.set(index,parentOne.get(index));
            childTwo.set(index,parentTwo.get(index));

            index++;
            if (index==size)
                index = 0;
        }

        //System.out.println(childOne);
        //System.out.println(childTwo);

        int indexForChild1 = index2;
        int indexForChild2 = index2;

        while (index!=index1){
            //System.out.println(childOne);
            //System.out.println(childTwo);
            while (childOne.contains(parentTwo.get(indexForChild1))){
                indexForChild1++;
                if (indexForChild1==size)
                    indexForChild1 = 0;
            }
            childOne.set(index, parentTwo.get(indexForChild1));

            while (childTwo.contains(parentOne.get(indexForChild2))){
                indexForChild2++;
                if (indexForChild2==size)
                    indexForChild2=0;
            }
            childTwo.set(index,parentOne.get(indexForChild2));

            index++;
            if (index==size)
                index = 0;
        }
    }

    public static void ox2(Individual parentOne, Individual parentTwo,Individual childOne, Individual childTwo){
        int numPositions = (int).4*parentOne.size();
        TreeSet<Integer> positions = new TreeSet<Integer>();
        while (positions.size()<numPositions)
            positions.add(rand.nextInt(parentOne.size()));

        ArrayList<Integer> elementsInPositionsFromP1 = new ArrayList<Integer>();
        TreeSet<Integer> positionsOfP1ElementsInP2 = new TreeSet<Integer>();

        ArrayList<Integer> elementsInPositionsFromP2 = new ArrayList<Integer>();
        TreeSet<Integer> positionsOfP2ElementsInP1 = new TreeSet<Integer>();

        for (Integer position: positions){
            elementsInPositionsFromP1.add(parentOne.get(position));
            elementsInPositionsFromP2.add(parentTwo.get(position));
        }

        for (Integer element: elementsInPositionsFromP1)
            positionsOfP1ElementsInP2.add(parentTwo.indexOf(element));

        for (Integer element: elementsInPositionsFromP2)
            positionsOfP2ElementsInP1.add(parentOne.indexOf(element));

        childOne.clear();
        childOne.addAll(parentOne);
        childTwo.clear();
        childTwo.addAll(parentTwo);

        int count = 0;
        for (Integer position: positionsOfP2ElementsInP1){
            childOne.set(position, elementsInPositionsFromP2.get(count));
            count++;
        }

        count = 0;
        for (Integer position: positionsOfP1ElementsInP2){
            childTwo.set(position, elementsInPositionsFromP1.get(count));
            count++;
        }
    }

    public static void cycleCross(Individual parentOne, Individual parentTwo,Individual childOne, Individual childTwo){
        //System.out.println(parentOne);
        //System.out.println(parentTwo);
        int size = parentOne.size();
        int index = rand.nextInt(size);
//		index = 3;

        childOne.ensureCapacity(size);
        childTwo.ensureCapacity(size);
        childOne.clear();
        childTwo.clear();

        childOne.addAll(parentTwo);
        childTwo.addAll(parentOne);

        int index2 = index;
        do {
            childOne.set(index2, parentOne.get(index2));
            index2 = parentTwo.indexOf(parentOne.get(index2));
        }while (index2!=index);

        index2 = index;
        do {
            childTwo.set(index2,parentTwo.get(index2));
            index2 = parentOne.indexOf(parentTwo.get(index2));
        }while (index2!=index);

        //System.out.println(childOne);
        //System.out.println(childTwo);
    }

    public static void pmx(Individual parentOne, Individual parentTwo,Individual childOne, Individual childTwo){
        //System.out.println(parentOne);
        //System.out.println(parentTwo);
        int size = parentOne.size();
        int index1 = rand.nextInt(size);
        index1 = 2;
        int index2;
        do{
            index2= rand.nextInt(size);
            index2 =6;
        }while(index1==index2);

//		System.out.println("\n"+index1+" "+index2);

        childOne.ensureCapacity(size);
        childTwo.ensureCapacity(size);
        childOne.clear();
        childTwo.clear();

        //	System.out.println("Parent One: "+parentOne);
        //	System.out.println("Parent Two: "+parentTwo);

        childOne.addAll(parentTwo);
        childTwo.addAll(parentOne);

        //copies area between the crossover points
        int index = index1;
        System.out.println(childOne);
        while (index != index2){
            childOne.set(index,parentOne.get(index));
            childTwo.set(index,parentTwo.get(index));
            System.out.println(index);
            System.out.println(childOne);
            //	System.out.println(parentOne.get(index)+" "+parentTwo.indexOf(parentOne.get(index)));
            //	System.out.println(parentTwo.get(index)+" "+parentOne.indexOf(parentTwo.get(index)));

            childOne.set(parentTwo.indexOf(parentOne.get(index)),parentTwo.get(index)); //index,element
            childTwo.set(parentOne.indexOf(parentTwo.get(index)),parentOne.get(index));

            System.out.println(childOne);

            //		System.out.println("\n"+index);
            //		System.out.println(parentOne+" "+parentTwo);
            //	System.out.println(childOne+" "+childTwo);
            index++;
            if (index==size)
                index = 0;
        }

        for (int i=0;i<size;i++){
            if (!childOne.contains(i)){
                for (int j=0;j<size;j++){
                    if (Collections.frequency(childOne,j)>1){
                        int firstI = childOne.indexOf(j);
                        int secondI = childOne.lastIndexOf(j);

                        if (firstI >= Math.min(index2, index1)&& firstI <= Math.max(index1, index2))
                            childOne.set(secondI,i);
                        else
                            childOne.set(firstI, i);
                        break;
                    }
                }
                System.out.println(childOne);
            }

            if (!childTwo.contains(i)){
                for (int j=0;j<size;j++){
                    if (Collections.frequency(childTwo,j)>1){
                        int firstI = childTwo.indexOf(j);
                        int secondI = childTwo.lastIndexOf(j);

                        if (firstI >= Math.min(index2, index1)&& firstI <= Math.max(index1, index2))
                            childTwo.set(secondI,i);
                        else
                            childTwo.set(firstI, i);
                        break;
                    }
                }
            }

        }

        for (int i=0;i<size;i++){
            if (!childOne.contains(i)){
                System.out.println("Child 1 is missing: "+i);
            }
            if (!childTwo.contains(i)){
                System.out.println("child 2 is missing: "+i);
            }

        }

        //System.out.println(childOne);
        //System.out.println(childTwo);
    }

    public static void positionCross(Individual parentOne, Individual parentTwo,Individual childOne, Individual childTwo){
        int size = parentOne.size();
        int numPositions = (int).4*size;
        TreeSet<Integer> positions = new TreeSet<Integer>();
        while (positions.size()<numPositions)
            positions.add(rand.nextInt(parentOne.size()));


        childOne.ensureCapacity(size);
        childTwo.ensureCapacity(size);
        childOne.clear();
        childTwo.clear();

        while(childOne.size()<size)
            childOne.add(-1);
        while(childTwo.size()<size)
            childTwo.add(-1);

        int indexForParentOne = 0;
        int indexForParentTwo = 0;

        for (Integer i: positions){
            childOne.set(i,parentOne.get(i));
            childTwo.set(i,parentTwo.get(i));
        }
        for (int i=0;i<size;i++){
            if (positions.contains(i)){
                continue;
            }

            else{
                while (childOne.contains(parentTwo.get(indexForParentTwo)))
                    indexForParentTwo++;
                childOne.set(i,parentTwo.get(indexForParentTwo));
                indexForParentTwo++;

                while (childTwo.contains(parentOne.get(indexForParentOne)))
                    indexForParentOne++;
                childTwo.set(i, parentOne.get(indexForParentOne));
                indexForParentOne++;
            }
            //	System.out.println(childOne+" "+childTwo);
        }


    }


    public void rar (){
        int index1 = rand.nextInt(size());
        int index2 = rand.nextInt(size());
        int toMove = get(index1);
        remove(index1);
        add(index2,toMove);
    }

    public void swap(){
        int index1 = rand.nextInt(size());
        int index2 = rand.nextInt(size());
        int temp = get(index2);
        set(index2,get(index1));
        set(index1,temp);
    }

    public void inversion(){
        int index1 = rand.nextInt(size());
        int index2 = rand.nextInt(size());

        while (index1!=index2 && index1!=index2+1){
            int temp = get(index2);
            set(index2,get(index1));
            set(index1,temp);
            index1++;
            index2--;
            if (index1 == size())
                index1 = 0;
            if (index2 == -1)
                index2 = size()-1;

        }
    }



    public void setColors(int c){
        colors = c;
        //	setFitness(1000000-Math.pow(c,2));
    }

    public void setFitness(int fit) {
        fitness = fit;
    }

    public void setNormFitness(double total){
        normFitness = fitness/total;
    }

    public double setAccumFitness(double in){
        accumFitness = in+normFitness;
        return accumFitness;
    }
    public int compareTo(Individual other) {
        // TODO Auto-generated method stub
        double difference = this.fitness - other.fitness;
        if (difference == 0)
            return 0;
        if (difference < 0)
            return -1;
        else
            return 1;
    }
}
