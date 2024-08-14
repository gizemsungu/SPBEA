import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

public class Util {
    public static void getEdgeDynamicResults_ColorsVersusEdgeProbs(){
        int [] nodeSize = {100, 200, 300, 400, 500};
        for (int i = 0; i < nodeSize.length ; i++) {
            writeLine("EdgeDynamic_ColorsVersusEdgeProb.txt", "NODE SIZE: " + nodeSize[i]  + "\n");
            double [][] results = readColorsVersusEdgeProb(nodeSize[i]);
            for (int j = 0; j < 10 ; j++) {
                double[] colorsPercent = convertPercentage(results[j]);
                String line="";
                for (int k = 0; k < 4 ; k++) {
                    line = line +  colorsPercent[k] + "\t";
                }
                line += "\n";
                writeLine("EdgeDynamic_ColorsVersusEdgeProb.txt", line);
            }
            writeLine("EdgeDynamic_ColorsVersusEdgeProb.txt", "\n\n");
        }

    }

    public static void getEdgeDynamicResults_ColorsVersusEdgeDensity(){
        int [] nodeSize = {100,200,300, 400, 500};
        for (int i = 0; i < nodeSize.length ; i++) {
            writeLine("EdgeDynamic_ColorsVersusEdgeDensity.txt", "NODE SIZE: " + nodeSize[i]  + "\n");
            double [][] results = readColorsVersusEdgeDensity(nodeSize[i]);
            for (int j = 0; j < 9 ; j++) {
                double[] colorsPercent = convertPercentage(results[j]);
                String line="";
                for (int k = 0; k < 4 ; k++) {
                    line = line +  colorsPercent[k] + "\t";
                }
                line += "\n";
                writeLine("EdgeDynamic_ColorsVersusEdgeDensity.txt", line);
            }
            writeLine("EdgeDynamic_ColorsVersusEdgeDensity.txt", "\n\n");
        }

    }

    public static double[][] readColorsVersusEdgeDensity(int nodeSize){
        double[][] results = new double[9][4];
        double [] density = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        for (int i = 0; i < density.length ; i++) {
            double [] colors = readEdgeProbs(nodeSize, density[i]);
            for (int j = 0; j < colors.length; j++) {
                results[i][j] = colors[j];
            }
        }
        return results;
    }

    public static double [] readLineOnTime(String FILENAME){
        double [] timesAvg = new double[4];
        BufferedReader br = null;
        FileReader fr = null;
        int lineNumber = 0;
        try {
            fr = new FileReader(FILENAME);
            br = new BufferedReader(fr);
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if(sCurrentLine.startsWith("STEP")) {
                    lineNumber++;
                    sCurrentLine = sCurrentLine.replace(',', '.');
                    String[] parts = sCurrentLine.split("\t");
                    //System.out.println(parts[8]);
                    int idx = 8;
                    for (int i = 0; i < timesAvg.length ; i++) {
                        timesAvg[i] += Double.parseDouble(parts[idx]);
                        idx++;
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
        System.out.println(lineNumber);
        for (int i = 0; i < timesAvg.length ; i++) {
            timesAvg[i] /= lineNumber;
        }
        return timesAvg;
    }

    public static double [] readEdgeProbs(int nodeSize, double density){
        double [] colorsAvg = new double[4];
        double [] nodeProbs = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
        for (int i = 0; i < 10; i++) {
            String FILENAME = "/media/suav-ahmet/Elements/DPEA_EXPERIMENTALSTUDY/dpbea_gtueskipc_generatedTests/edgeDynamic_"+ nodeProbs[i] + "_" + density + "_" + nodeSize + ".txt";
            double [] colors = readFile(FILENAME);
            for (int j = 0; j < colorsAvg.length ; j++) {
                colorsAvg[j] += colors[j];
            }
        }
        for (int i = 0; i < colorsAvg.length ; i++) {
            colorsAvg[i] /= 10.0;
        }
        return colorsAvg;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double [] convertPercentage(double [] colors){
        double [] colorsPercent = new double[4];
        colorsPercent[0] = 100;
        for (int i = 1; i < colors.length ; i++) {
            colorsPercent[i] = colors[i]*100/colors[0];
            colorsPercent[i] = round(colorsPercent[i], 2);
        }
        return colorsPercent;
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

    public static double [][] readColorsVersusEdgeProb(int nodeSize){
        double [][] results = new double[10][4];
        double [] nodeProbs = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
        for (int i = 0; i < nodeProbs.length ; i++) {
            double [] colors = readDensity(nodeSize, nodeProbs[i]);
            for (int j = 0; j < colors.length; j++) {
                results[i][j] = colors[j];
            }
        }
        return results;
    }

    public static double [] readDensity(int nodeSize, double edgeProb){
        double [] colorsAvg = new double[4];
        double [] density = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        for (int i = 0; i < 9; i++) {
            String FILENAME = "/media/suav-ahmet/Elements/DPEA_EXPERIMENTALSTUDY/dpbea_gtueskipc_generatedTests/edgeDynamic_"+ edgeProb + "_" + density[i] + "_" + nodeSize + ".txt";
            double [] colors = readFile(FILENAME);
            for (int j = 0; j < colorsAvg.length ; j++) {
                colorsAvg[j] += colors[j];
            }
        }
        for (int i = 0; i < colorsAvg.length ; i++) {
            colorsAvg[i] /= 9.0;
        }
        return colorsAvg;
    }

    public static double [] readFile(String FILENAME){
        BufferedReader br = null;
        FileReader fr = null;
        double [] colorsAvg = new double[4];
        int lineNumber = 0;
        try {


            fr = new FileReader(FILENAME);
            br = new BufferedReader(fr);
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                //   System.out.println(sCurrentLine);
                if(sCurrentLine.startsWith("STEP")) {
                    lineNumber++;
                    sCurrentLine = sCurrentLine.replace(',', '.');
                    String[] parts = sCurrentLine.split("\t|\\ - ");
                    int idx = 2;
                    for (int i = 0; i < colorsAvg.length ; i++) {
                        colorsAvg[i] += Double.parseDouble(parts[idx]);
                        idx++;
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
        if(lineNumber == 0){
            System.out.println(FILENAME);
        }
        for (int i = 0; i < colorsAvg.length ; i++) {
            colorsAvg[i] /= lineNumber;
        }
        return colorsAvg;
    }

    public static void analyzeMinMax(){
        int [] nodeSize = {100, 200, 300, 400, 500};
        for (int i = 0; i < nodeSize.length; i++) {
            double [] density = {0.1, 0.5, 0.9};
            for (int j = 0; j < density.length ; j++) {
                double [] edgeProb = {0.01, 0.04, 0.07, 0.1};
                for (int k = 0; k < edgeProb.length ; k++) {
                    String FILENAME = "C:\\Users\\DELL\\Desktop\\DPBEA\\DPBEA\\EdgeDynamic\\SpecialTests\\" + nodeSize[i] + "\\EdgeDynamicMinMax_" + edgeProb[k] + "_" + density[j] + "_" + nodeSize[i] + ".txt";
                    HashMap<String, double[]>  results = readFileMinMax(FILENAME);
                    writeLine("EdgeDynamicMinMax.txt", nodeSize[i] + "\t" + density[j] + "\t" + edgeProb[k] + "\t" );

                    for (int a = 0; a < 4 ; a++) {
                        writeLine("EdgeDynamicMinMax.txt",Util.round(results.get("Avg")[a],2) + " ( " + Util.round(results.get("Min")[a],2) + " - " + Util.round(results.get("Max")[a],2) + " ) " + "\t");
                    }

                    for (int l = 0; l < 6 ; l++) {
                        writeLine("EdgeDynamicMinMax.txt",results.get("comp")[l] + "\t");
                    }

                    for (int a = 0; a < 3; a++) {
                        writeLine("EdgeDynamicMinMax.txt",Util.round(results.get("diff")[a],2) + "\t");
                    }

                    for (int a = 0; a < 3 ; a++) {
                        writeLine("EdgeDynamicMinMax.txt",Util.round(results.get("time")[a],2) + "\t");
                    }
                    for (int a = 0; a < 4 ; a++) {
                        writeLine("EdgeDynamicMinMax.txt",Util.round(results.get("graph")[a],2) + "\t");
                    }

                    writeLine("EdgeDynamicMinMax.txt", "\n");

                }
            }
        }
    }

    public static HashMap<String, double[]> readFileMinMax(String FILENAME){
        BufferedReader br = null;
        FileReader fr = null;
        double [] colorsAvg = new double[4];
        double [] colorMin = new double[4];
        double [] colorMax = new double[4];
        double [] comp = new double[6];

        double [] time = new double[3];
        double [] graph = new double[4];
        int lineNumber = 0;
        try {


            fr = new FileReader(FILENAME);
            br = new BufferedReader(fr);
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
                if(sCurrentLine.startsWith("STEP")) {
                    lineNumber++;
                    sCurrentLine = sCurrentLine.replace(',', '.');
                    String[] parts = sCurrentLine.split("\t|\\ -");
                    for (int i = 0; i < parts.length ; i++) {
                        System.out.println(i + "\t" + parts[i]);
                    }

                    int idx = 2;
                    for (int i = 0; i < colorsAvg.length ; i++) {
                        colorsAvg[i] += Double.parseDouble(parts[idx]);
                        idx++;
                    }

                    idx = 7;
                    for (int i = 0; i < colorMin.length; i++) {
                        colorMin[i] += Double.parseDouble(parts[idx]);
                        idx+=2;
                    }

                    idx = 8;
                    for (int i = 0; i < colorMin.length; i++) {
                        colorMax[i] += Double.parseDouble(parts[idx]);
                        idx+=2;
                    }


                    idx = 16;
                    for (int i = 0; i < time.length ; i++) {
                        time[i] += Double.parseDouble(parts[idx]);
                        idx++;
                    }

                    idx = 20;
                    for (int i = 0; i < comp.length ; i++) {
                        comp[i] = Double.parseDouble(parts[idx]);
                        idx++;
                    }

                    idx = 26;
                    for (int i = 0; i < graph.length ; i++) {
                        graph[i] += Double.parseDouble(parts[idx]);
                        idx++;
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
        System.out.println(lineNumber);
        if(lineNumber == 0){
            System.out.println(FILENAME);
        }
        for (int i = 0; i < colorsAvg.length ; i++) {
            colorsAvg[i] /= (double)lineNumber;
        }

        for (int i = 0; i < colorMin.length ; i++) {
            colorMin[i] /= (double)lineNumber;
        }

        for (int i = 0; i < colorMax.length ; i++) {
            colorMax[i] /= (double)lineNumber;
        }

        for (int i = 0; i < time.length ; i++) {
            time[i] /= (double)lineNumber;
        }

        for (int i = 0; i < graph.length ; i++) {
            graph[i] /= (double)lineNumber;
        }

        double [] diffcolor = new double[4];

        for (int i = 0; i < diffcolor.length ; i++) {
                diffcolor[i] = colorsAvg[i] - colorsAvg[2];
        }

        HashMap<String, double[]> results = new HashMap<>();
        results.put("Avg", colorsAvg);
        results.put("Min", colorMin);
        results.put("Max", colorMax);
        results.put("diff", diffcolor);
        results.put("time", time);
        results.put("graph", graph);
        results.put("comp", comp);
        return results;
    }


    public static void analyzeGraph(){


        double [] density = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        for (int k = 0; k < density.length ; k++) {
            writeLine("GraphChanges.txt", "DENSITY: " + density[k]  + "\t");
            double [] edgeProb = {0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
            for (int j = 0; j < edgeProb.length ; j++) {
                int[] nodeSize = {100, 200, 300, 400, 500};
                double densityAvg = 0;
                for (int i = 0; i < nodeSize.length; i++) {
                    String FILENAME = "/media/suav-ahmet/Elements/DPEA_EXPERIMENTALSTUDY/dpbea_gtueskipc_generatedTests/edgeDynamic_" + edgeProb[j] + "_" + density[k] + "_" + nodeSize[i] + ".txt";
                    densityAvg += readFileGraphChange(FILENAME);
                }
                densityAvg /= nodeSize.length;
                densityAvg = round(densityAvg, 2);
                writeLine("GraphChanges.txt", densityAvg + "\t");
            }
            System.out.println();
        }




    }

    public static double readFileGraphChange(String FILENAME){
        BufferedReader br = null;
        FileReader fr = null;
        double density = 0;
        int lineNumber = 0;
        try {


            fr = new FileReader(FILENAME);
            br = new BufferedReader(fr);
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                //   System.out.println(sCurrentLine);
                if(sCurrentLine.startsWith("STEP")) {
                    lineNumber++;
                    sCurrentLine = sCurrentLine.replace(',', '.');
                    String[] parts = sCurrentLine.split("\t|\\ - ");
                    int idx = 11;
                    density += Double.parseDouble(parts[idx]);
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
        System.out.println(lineNumber);
        if(lineNumber == 0){
            System.out.println(FILENAME);
        }
        density /= lineNumber;
        return density;
    }
}
