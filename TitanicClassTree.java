import java.io.*;
import java.util.*;

// Read CSV
class ReadCSV {
    public static int rows, columns;

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
    private Split[] allSplits;
    
    Node(String[][] data) {
        String[][] node_data = data;
        
        survived = 0;
        obsAtNode = node_data.length;

        try {
            for(int i=0; i<obsAtNode; i++)
            survived += Integer.parseInt(node_data[i][1]);
        }
        catch (NumberFormatException exc) {
            System.out.println(exc);
            survived = 0;
        }

        p_survived = (double) survived / (double) obsAtNode;
        impurity = 2 * p_survived * (1 - p_survived);
    } 
    
    // Methods to get some basic information about node.
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
class Split {
    double split_value, gini, impurity_after, pleft, pright;
    int variable, num_left, num_right, countL, countR, leftORright;
    String[][] parent_data, nodeLdata, nodeRdata;
    Node parent, left, right;

    // Create a split object if value is number.
    Split(double value, int column_num, String[][] data, Node parent_node) {
        split_value = value;
        variable = column_num;
        parent_data = data;
        parent = parent_node;

        // Calculate number of observations in left node and right node.
        num_left = 0;
        num_right = 0;
        // Variable equals 0 or 1: 0 shunts blank values to left node, 1 shunts
        // blank values to right node.
        leftORright = 0;
        for(int i=0; i<parent_data.length; i++) {
            // If no value is available, alternate which node observation is sent to.
            if(parent_data[i][variable].compareTo("") == 0) {
                if(leftORright == 0) {
                    num_left += 1;
                    leftORright = 1;
                }
                else {
                    num_right += 1;
                    leftORright = 0;
                }
            }
            // If value is less than split value, observation goes to the left node.
            else if(Double.parseDouble(parent_data[i][variable])
                    <= split_value)
                num_left += 1;
            // If value is greater than or equal to split value, observation goes
            // to the right node.
            else
                num_right += 1;
        }

        // Create datasets for left and right nodes.
        nodeLdata = new String[num_left][ReadCSV.columns];
        nodeRdata = new String[num_right][ReadCSV.columns];
        countL = -1;
        countR = -1;
        leftORright = 0;
        for(int n=0; n<parent_data.length; n++) {
            // If no value is available, alternate which node observation is sent to.
            if(parent_data[n][variable].compareTo("") == 0) {
                if(leftORright == 0) {
                    countL++;
                    for(int p=0; p<ReadCSV.columns; p++) {
                        nodeLdata[countL][p] = parent_data[n][p];
                        leftORright = 1;
                    }
                }
                else {
                    countR++;
                    for(int p=0; p<ReadCSV.columns; p++) { 
                        nodeRdata[countR][p] = parent_data[n][p];
                         leftORright = 0;
                    }
                }
            }
            // If value is less than split value, send observation to left node.
            else if(Double.parseDouble(parent_data[n][variable])
                    <= split_value) {
              countL++;  
              for(int p=0; p<ReadCSV.columns; p++) {
                  nodeLdata[countL][p] = parent_data[n][p];
              }
            }
            // If value is greater than or equal to split value, send observation
            // to right node.
            else {
              countR++;
              for(int p=0; p<ReadCSV.columns; p++) {
                  nodeRdata[countR][p] = parent_data[n][p];
              }
            }
        }

        // Create new nodes.
        left = new Node(nodeLdata);
        right = new Node(nodeRdata);

        // Calculate gini diversity index for split.
        pleft = ((double) num_left / parent_data.length);
        pright = ((double) num_right / parent_data.length);
        impurity_after = (pleft * left.getImpurity())
           + (pright * right.getImpurity()); 
        gini = parent.getImpurity() - impurity_after;

        
    }

    // Create a split object if value is category.
    //Split(String value, int column_num) {
    //    String split_value = value;
    //    int variable = column_num;
    //    double gini;
    //}

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

        Split testSplit = new Split(23.0, 5, data, root);
        System.out.println("Parent node impurity: " + 
                testSplit.parent.getImpurity());
        System.out.println("Left node impurity: " + 
                testSplit.left.getImpurity());
        System.out.println("Right node impurity: " + 
                testSplit.right.getImpurity());
        System.out.println("Impurity after split: " + 
                testSplit.impurity_after);
        System.out.println("Gini: " + testSplit.gini);
        
    }
}
