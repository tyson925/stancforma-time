package hu.szte.eval;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculate the Recall, Precision and F-Masure.
 */
public class FMeasure {

    public Map<String, Measures> measured = new HashMap<String, Measures>();
    public HashSet<String> labels = new HashSet<String>();

    /**
     * Inner class to store the precision and recall values with their label.
     */
    class Measures {

        private float truepositive = 0;
        private float numLabeled = 0;
        private float numEtalon = 0;

        public Measures() {
        }

        @Override
        public String toString() {
            return "Measures{" + "truepositive=" + truepositive + ", numLabeled=" + numLabeled + ", numEtalon=" + numEtalon + '}';
        }

        /**
         * @return the truepositive
         */
        public float getTruepositive() {
            return truepositive;
        }

        /**
         * @return the numLabeled
         */
        public float getNumLabeled() {
            return numLabeled;
        }

        /**
         * @return the numEtalon
         */
        public float getNumEtalon() {
            return numEtalon;
        }

        /**
         * @param truepositive the truepositive to set
         */
        public void setTruepositive(float truepositive) {
            this.truepositive = truepositive;
        }

        /**
         * @param numLabeled the numLabeled to set
         */
        public void setNumLabeled(float numLabeled) {
            this.numLabeled = numLabeled;
        }

        /**
         * @param numEtalon the numEtalon to set
         */
        public void setNumEtalon(float numEtalon) {
            this.numEtalon = numEtalon;
        }
    }

    public FMeasure() {
    }

    /**
     * Expand the calculated values with a new sentence.
     * Update the recall and precision values.
     */
    public void addSentence(Vector<String> l, Vector<String> r) {

        for (String s : l) {
            s = s.replace("B-", "");
            s = s.replace("E-", "");
            s = s.replace("I-", "");
            if (!s.equals("O")) {
                labels.add(s);
            }
        }

        for (String s : r) {
            s = s.replace("B-", "");
            s = s.replace("I-", "");
            s = s.replace("E-", "");
            if (!s.equals("O")) {
                labels.add(s);
            }
        }

        for (String label : labels) {
            update(phraseSearcherFromVector(l), phraseSearcherFromVector(r), label);
        }
    }

    /**
     * Update the precision values of the prediction and etalon that belongs to the current label.
     */
    protected void update(TreeMap<String, String> etalon, TreeMap<String, String> prediction, String label) {

        int truepositive = 0;
        int numLabeled = 0;
        int numEtalon = 0;

        for (Map.Entry<String, String> e : etalon.entrySet()) {
            if (e.getValue().equals(label)) {
                numEtalon++;
            }
        }

        for (Map.Entry<String, String> p : prediction.entrySet()) {
            if (p.getValue().equals(label)) {
                numLabeled++;
                if (etalon.containsKey(p.getKey())) {
                    if (etalon.get(p.getKey()).equals(label)) {
                        truepositive++;
                    }
                }
            }
        }

        if (!this.measured.containsKey(label)) {
            this.measured.put(label, new Measures());
        }

        if (this.measured.containsKey(label)) {
            this.measured.get(label).truepositive += truepositive;
            this.measured.get(label).numLabeled += numLabeled;
            this.measured.get(label).numEtalon += numEtalon;
        }
    }

    /**
     * Calculate the precision value that belongs to the current label.
     */
    public float getPrecision(String label) {
        if (this.measured.containsKey(label)) {
            if (this.measured.get(label).numLabeled != 0) {
                return this.measured.get(label).truepositive / this.measured.get(label).numLabeled * 100;
            }
        }
        return 0;
    }

    /**
     * Calculate the recall value that belongs to the current label.
     */
    public float getRecall(String label) {
        if (this.measured.containsKey(label)) {
            if (this.measured.get(label).numEtalon != 0) {
                return this.measured.get(label).truepositive / this.measured.get(label).numEtalon * 100;
            }
        }
        return 0;
    }

    /**
     * Calculate the F-Measure value that belongs to the current label.
     */
    public float getFMeasure(String label) {
        float p = getPrecision(label);
        float r = getRecall(label);
        if (p + r != 0) {
            return 2 * p * r / (p + r);
        }
        return 0;
    }

    /**
     * Print all precision values that belong any label.
     */
    public void getAllPrecision() {
        for (String label : labels) {
            System.out.println("Precision(" + label + ") = " + getPrecision(label));
        }
    }

    /**
     * Print all recall values that belong any label.
     */
    public void getAllRecall() {
        for (String label : labels) {
            System.out.println("Recall(" + label + ") = " + getRecall(label));
        }
    }

    /**
     * Print all F-Measure values that belong any label.
     */
    public void getAllFMeasure() {
        for (String label : labels) {
            System.out.println("F(" + label + ") = " + getFMeasure(label));
        }
    }

    /**
     * Print all statistics.
     */
    public String printStatistics() {
        String ret = new String();
        for (String label : labels) {
            System.out.print("Recall(" + label + ") = " + getRecall(label));
            System.out.print("\tPrecision(" + label + ") = " + getPrecision(label));
            System.out.println("\tF(" + label + ") = " + getFMeasure(label));
            if (label.contains("MWE")) {
                ret += "Recall(" + label + ") = " + getRecall(label) + "\tPrecision(" + label + ") = " + getPrecision(label) + "\tF(" + label + ") = " + getFMeasure(label);
            }
            //System.out.print(getRecall(label));
            //System.out.print(" " + getPrecision(label));
            //System.out.println(" " + getFMeasure(label));
        }

        DecimalFormat nf1 = new DecimalFormat("####.00");
        System.out.print("Recall = " + nf1.format(getGlobalRecall()));
        System.out.print("\tPrecision = " + nf1.format(getGlobalPrecision()));
        System.out.print("\tF = " + nf1.format(getGlobalFmeasure()));
        System.out.print("\tAccuracy = " + nf1.format(getGlobalAccuracy()));
        System.out.println("\tTruePos: " + nf1.format(getGlobalTruePositive()));
        System.out.println(nf1.format(getGlobalRecall()) + "/" + nf1.format(getGlobalPrecision()) + "/" + nf1.format(getGlobalFmeasure()));
        return ret;
    }

    public float getGlobalTruePositive() {
        float tp = 0;
        for (Measures m : measured.values()) {
            tp += m.getTruepositive();
        }
        return tp;
    }

    public float getGlobalAccuracy() {
        float tp = 0, e = 0;
        for (Measures m : measured.values()) {
            tp += m.truepositive;
            e += m.numEtalon;
            //e += m.numLabeled - m.truepositive;
        }
        return tp / e * 100;

    }

    /**
     * Update calculate global recall value.
     */
    public float getGlobalRecall() {
        float tp = 0, e = 0;
        for (Measures m : measured.values()) {
            tp += m.truepositive;
            e += m.numEtalon;
        }
        return tp / e * 100;
    }

    /**
     * Update calculate global precision value.
     */
    public float getGlobalPrecision() {
        float tp = 0, p = 0;
        for (Measures m : measured.values()) {
            tp += m.truepositive;
            p += m.numLabeled;
        }
        return tp / p * 100;
    }

    /**
     * Calculate the global F-Measure value.
     */
    public float getGlobalFmeasure() {
        float p = getGlobalPrecision();
        float r = getGlobalRecall();
        return 2 * p * r / (p + r);
    }

    /**
     * Serach for phrases from a label vector.
     */
    protected TreeMap<String, String> phraseSearcherFromVector(
            Vector<String> labelVector) {
        TreeMap<String, String> phrases = new TreeMap<String, String>();

        int counter = 0;
        String label = "";
        String temp = "";
        String label2 = "";
        String pos = "";
        int temp2 = 0;

        for (int i = 0; i < labelVector.size(); i++) {
            temp = labelVector.get(i);

            boolean flag1 = true;
            boolean flag2 = false;

            for (String l : labels) {
                if ((label.equals("B-" + l)) && (temp.equals("I-" + l) || temp.equals("E-" + l))) {
                    flag1 = false;
                }

                if (temp.equals("B-" + l)) {
                    flag2 = true;
                }
            }

            if ((flag1) && ((flag2) || (!label.equals(temp)))) {
                label2 = label;
                label = temp;
                if ((counter - 1) > -1) {
                    pos = (temp2 + 1) + "," + (counter - 1);

                    for (String l : labels) {
                        if (label2.equals("I-" + l) || label2.equals("B-" + l) || label2.equals("E-" + l)) {
                            //if ((temp2 + 1) + 3  <= (counter - 1)) {
                            phrases.put(pos, l);
                            //}
                        }
                    }
                }
                temp2 = counter - 1;
            }
            counter++;
        }
        pos = (temp2 + 1) + "," + (counter - 1);

        for (String l : labels) {
            if (label.equals("I-" + l) || label.equals("B-" + l) || label2.equals("E-" + l)) {
                phrases.put(pos, l);
            }
        }
        return phrases;
    }

    public void evaulateFromTxt(String etalonFileName, String predicateFileName) {
        try {
            //VectorSpaceModel.*/
            Vector<String> etalonLabel = new Vector<String>();
            Vector<String> predicateLabel = new Vector<String>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(etalonFileName)));
            String text = null;
            while ((text = reader.readLine()) != null) {
                etalonLabel.add(text);
            }
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(predicateFileName)));
            while ((text = reader.readLine()) != null) {
                predicateLabel.add(text);
            }

            addSentence(etalonLabel, predicateLabel);
            printStatistics();
        } catch (Exception ex) {
            Logger.getLogger(FMeasure.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String evaulateFromTxt(String predicateFileName) throws IOException {

        Vector<String> etalonLabel = new Vector<String>();
        Vector<String> predicateLabel = new Vector<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(predicateFileName)));
        String text = null;

        while ((text = reader.readLine()) != null) {
            if (!text.startsWith("DOCSTART")) {
                if (text.split(" ").length > 2) {
                    etalonLabel.add(text.split(" ")[1].replaceAll("MWE_LVC_NOUN", "MWE_LVC"));
                    //etalonLabel.add(text.split(" ")[1].replace("SPLIT", "O").replace("_VERB", ""));
                    predicateLabel.add(text.split(" ")[2]);
                } else {
                    addSentence(etalonLabel, predicateLabel);
                    etalonLabel = new Vector<String>();
                    predicateLabel = new Vector<String>();
                }
                //addSentence(etalonLabel, predicateLabel);
            }
        }

        return printStatistics();

    }

    public void evaulateBioRuns() {
        FMeasure fMeasure = null;
        for (File file : new File("./data/bio/teszt/generif/rocchio").listFiles()) {
            if (!file.isDirectory()) {
                try {
                    fMeasure = new FMeasure();
                    System.out.println(file.getCanonicalFile());
                    fMeasure.evaulateFromTxt(file.getCanonicalPath());
                } catch (IOException ex) {
                    Logger.getLogger(FMeasure.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }


    //private void printStat(Set<String> predicateMWEs, Set<String> etalonMWEs) {
    private void printStat(List<String> predicateMWEs, List<String> etalonMWEs) {
        int truePozitive = 0;
        int falsePozitve = 0;
        for (String predicate : predicateMWEs) {
            if (etalonMWEs.contains(predicate)) {
                truePozitive++;
            } else {
                falsePozitve++;
            }
        }
        //System.out.println(predicateMWEs.size() + " " + etalonMWEs.size());
        //System.out.println("trueP: " + truePozitive);
        //System.out.println("falseP: " + falsePozitve);
        Double recall = (double) truePozitive / etalonMWEs.size();
        Double precision = (double) truePozitive / (truePozitive + falsePozitve);
        Double F = 2 * precision * recall / (precision + recall);
        DecimalFormat nf1 = new DecimalFormat("####.00");
        System.out.println(nf1.format(recall * 100) + "/" + nf1.format(precision * 100) + "/" + nf1.format(F * 100) + "\ttp:" + truePozitive);
    }





    public void accuracy(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        int truePositive = 0;
        int falsePositive = 0;
        int lineNumber = 0;
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] lineArray = line.split(" ");
            if (lineArray.length > 1) {
                if (!lineArray[1].equals("O")) {
                    if (lineArray[1].equals(lineArray[2])) {
                        truePositive++;
                    }
                } else {
                    if (lineArray[1].equals(lineArray[2])) {
                        falsePositive++;
                    }
                }
                lineNumber++;
            }

        }
        double acc = (double) (truePositive + falsePositive) / lineNumber;
        System.out.println("acc: " + acc);
        reader.close();
    }

    public void evaulteDict(String fileName) throws IOException {
        FMeasure f = null;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName + "/results.txt")));
        List<File> files = new LinkedList<File>();
        files.addAll(Arrays.asList(new File(fileName).listFiles()));
        Collections.sort(files);
        for (File file : files) {
            if (!file.isDirectory()) {
                f = new FMeasure();
                writer.write(file.getName() + "\n");
                writer.write(String.valueOf(f.evaulateFromTxt(file.getCanonicalPath())) + "\n\n");
            }
        }
        writer.flush();
        writer.close();
    }

    /*
     * Összerakja az adott könyvtárban szereplő fileokat egybe
     */
    public void concat(String dir, String outFileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
        BufferedReader reader = null;
        for (File file : new File(dir).listFiles()) {
            //System.out.println(file);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;
            while ((line = reader.readLine()) != null) {
                writer.write(String.valueOf(line) + "\n");
            }
            writer.write("\n");
        }
        writer.flush();
        writer.close();
    }


    public static double getRecall(int truePos, int falsePos, int falseNeg) {
        return (double) truePos / (truePos + falsePos);
    }

    public static double getPrecison(int truePos, int falseNeg) {
        return (double) (truePos) / (truePos + falseNeg);
    }

    public static double getFscore(double precision, double recall) {
        return 2 * precision * recall / (precision + recall);
    }

    public static void main(String[] args) {
        try {
            /*List<File> files = new LinkedList<File>();
            String rootFile = "./data/fx/parallel/adapt/all_other/";
            for (File file : new File(rootFile).listFiles()) {
            if (!file.isDirectory()) {
            files.add(file);
            }
            }*/
            //Collections.sort(files);

            /*for (File file : files) {
            try {
            if (!file.isDirectory()) {
            FMeasure fMeasure = new FMeasure();
            System.out.println(file.getCanonicalPath());
            fMeasure.evaulateFromTxt(file.getCanonicalPath());
            fMeasure.alterF(file.getCanonicalPath(), "MWE_LVC");
            }
            } catch (IOException ex) {
            Logger.getLogger(FMeasure.class.getName()).log(Level.SEVERE, null, ex);
            }
            }*/
            FMeasure fMeasure = new FMeasure();

            fMeasure.evaulateFromTxt("./data/collocation_20000.iob");


        } catch (Exception ex) {
            Logger.getLogger(FMeasure.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
