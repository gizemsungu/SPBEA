package DPBEA;
import java.util.Random;

/**
 * Created by Gizem on 01.03.2017.
 */
public class DPBEAConst {

    public static long GRAPH_VERSION = Long.MIN_VALUE + 1;

    public static final Random RAND = new Random();
    public static final int POPULATION = 100;
    public static final int INDIVID_STEP = 10000;
    public static final int TOUR_SIZE = 3;
    public static final int CROSSOP = 0;
    public static final int MUTOP = 1;
    public static final double MUTRATE = .3;
    public static final int TESTPOPSIZE = 5;
    public static final int eStepSize = INDIVID_STEP / 50;
    public static final double CROSS_RATE = .5;
}
