import java.io.*;
import java.util.*;

// Read CSV
class ReadCSV {
    static int rows, columns;

    public static String[][] readCSV(String file, int lines, int variables) {
        String s;
        String file_name = file;
        rows = lines;
        columns = variables;
        String data[][] = new String[rows][columns];

        int row = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))){
            while((s = br.readLine()) != null) {
                data[row++] = s.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            }
        } catch(IOException exc) {
            System.out.println(exc);
        }

        return data;

    }
}

// Node object
class Node {
    private int survived, obsAtNode;
    private double p_survived, impurity;
    
    Node(String[][] data) {
        String[][] node_data = data;
        
        survived = 0;
        obsAtNode = data.length;

        for(int i=0; i<obsAtNode; i++)
            survived += Integer.parseInt(data[i][1]);

        p_survived = (double) survived / (double) obsAtNode;
        impurity = 2 * p_survived * (1 - p_survived);
    } 
    
    public double getTotal() {
        return obsAtNode;
    }

    public double getImpurity() {
        return impurity;
    }

    public double getPropSurv() {
        return p_survived;
    }

    public int getSurvived() {
        return survived;
    }

}

// Determines possible splits and best splits, and creates new nodes
class Splits {
    
    // Grab specified column from the data set
    private static String[] getColumn(String[][] data, int column_num) {
        String[] column = new String[ReadCSV.rows];
        for(int i=0; i<ReadCSV.rows; i++)
            column[i] = data[i][column_num];
        return column;
    }

    // Find the unique values in a column    
    private static String[] getUniqueVals(String[] column) {
        String[] uniqValsArray;

        Set<String> uniqVals = new LinkedHashSet<String>();
        for(String x : column)
            // Ensure that blank values are not included as split option
            if(x.compareTo("") != 0)
                uniqVals.add(x);

        // Convert Set object back into a String[] object
        uniqValsArray = Arrays.copyOf(uniqVals.toArray(), uniqVals.toArray().length,
                String[].class);
        return uniqValsArray;
    }

    // Get split options if column is categorical
    public static String[] getCatSplitOptions(String[][] data, int column_num) {
        String[] splitOptions;
        String[] uniqValues;
        String[] column;

        column = getColumn(data, column_num);
        uniqValues = getUniqueVals(column);

        // Note: May want to return later and make this method work for categorical
        // variables that contain more than three categories
        splitOptions = uniqValues;

        return splitOptions;
    }

    
    // Get split options if column is numerical
    public static double[] getNumSplitOptions(String[][] data, int column_num) {
        double[] splitOptions;
        String[] uniqValues;
        String[] column;
        
        column = getColumn(data, column_num);
        uniqValues = getUniqueVals(column);

        // Convert string array to double array.
        splitOptions = new double[uniqValues.length];
        for(int i=0; i<uniqValues.length; i++)
            splitOptions[i] = Double.parseDouble(uniqValues[i]);

        return splitOptions;
    }
}

// Main class
class TitanicClassTree {
    public static void main(String args[]) {
        String[][] data = ReadCSV.readCSV("train_wo_header.csv", 891, 12);

        Node root = new Node(data);

        double[] age = Splits.getNumSplitOptions(data, 5);
        for(int i = 0; i<age.length; i++)
            System.out.print(age[i] + " ");
        System.out.println();

        String[] pclass = Splits.getCatSplitOptions(data, 2);
        for(int i = 0; i<pclass.length; i++)
            System.out.println(pclass[i] + " ");
        System.out.println();

    }
}
