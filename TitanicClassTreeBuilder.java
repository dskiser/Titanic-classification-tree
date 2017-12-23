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
    private String node_name;
    private String[][] node_data;
    
    Node(String[][] data, String name) {
        node_data = data;
        node_name = name;

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
    
    double[] split_optionsNum;
    String[] split_optionsCat;
    double max_gini = 0.0;
    // Variable that is 1 if max_gini is from split on numerical variable and
    // 0 if max_gini is from split on categorical variable.
    int numORcat;
    Split new_split;
    Split chosenSplit;
    String chosenVar;
    // Recursive method to split nodes down to a specified purity level.
    public void chooseSplit(Variable[] variables, int num_observations) {

        for(Variable x : variables) {
            if(x.column_type.compareTo("numerical") == 0) {
                split_optionsNum = Split.getNumSplitOptions(node_data, x.column_num);
                // Do not try to split on number if it is the only number present in
                // node.
                if(split_optionsNum.length > 1) {
                    for(double y : split_optionsNum) {
                        new_split = new Split(y, x.column_num, node_data, impurity,
                                node_name);
                        if((new_split.gini >= max_gini) & (new_split != null)) {
                            chosenSplit = new_split;
                            max_gini = chosenSplit.gini;
                            numORcat = 1;
                            chosenVar = x.column_name;
                        }
                    }
                }
            }
            else {
                split_optionsCat = Split.getCatSplitOptions(node_data, x.column_num);
                // Do not try to split on category if only one category present in node
                if(split_optionsCat.length > 1) {
                    for(String y : split_optionsCat) {
                        new_split = new Split(y, x.column_num, node_data, impurity,
                                node_name);
                        if(new_split.gini >= max_gini) {
                            chosenSplit = new_split;
                            max_gini = chosenSplit.gini;
                            numORcat = 0;
                            chosenVar = x.column_name;
                        }
                    }
                }
           }
        }

        // Output
        System.out.println("Node name: " + node_name + " Impurity: " + impurity);
        if(numORcat == 1) 
            System.out.println("Chosen Split Value: " + chosenVar + " < "
                    + chosenSplit.split_valueNum);
        else
            System.out.println("Chosen Split Value: " + chosenVar + " = "
                    + chosenSplit.split_valueCat);
        System.out.println("Left Split Impurity: " + chosenSplit.left.getImpurity());
        System.out.println("Right Split Impurity: " + chosenSplit.right.getImpurity());
        System.out.println();

        // Continue splitting node if conditions are met.
        if((chosenSplit.left.node_data.length >= num_observations) &
                (chosenSplit.left.getImpurity() > 0.0))
            chosenSplit.left.chooseSplit(variables, num_observations);
        else {
            // If splitting conditions not met, output terminal node information.
            chosenSplit.left.node_name = chosenSplit.left.node_name + "terminal";
            System.out.println("Node name: " + chosenSplit.left.node_name 
                    + " Impurity: " + chosenSplit.left.impurity +
                    " Observations: " + chosenSplit.left.node_data.length);
            System.out.println();
        }
        if((chosenSplit.right.node_data.length >= num_observations) &
                (chosenSplit.right.getImpurity() > 0.0))
            chosenSplit.right.chooseSplit(variables, num_observations);
        else {
            // If splitting conditions not met, output terminal node information.
            chosenSplit.right.node_name = chosenSplit.right.node_name + "terminal";
            System.out.println("Node name: " + chosenSplit.right.node_name
                    + " Impurity: " + chosenSplit.right.impurity +
                    " Observations: " + chosenSplit.right.node_data.length);
            System.out.println();
        }
    }
}

// Determines possible splits and best splits, and creates new nodes
class Split {
    double split_valueNum, gini, impurity_after, pleft, pright, parent_impurity;
    int variable, num_left, num_right, countL, countR, leftORright;
    String[][] parent_data, nodeLdata, nodeRdata;
    Node left, right;
    String split_valueCat, parent_name;

    // Constructor for Split object if value is numerical.
    Split(double value, int column_num, String[][] data, double impurity, String name) {
        split_valueNum = value;
        variable = column_num;
        parent_data = data;
        parent_impurity = impurity;
        parent_name = name;

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
                    <= split_valueNum)
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
                    <= split_valueNum) {
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
        left = new Node(nodeLdata, parent_name + "1");
        right = new Node(nodeRdata, parent_name + "0");

        // Calculate gini diversity index for split.
        pleft = ((double) num_left / parent_data.length);
        pright = ((double) num_right / parent_data.length);
        impurity_after = (pleft * left.getImpurity())
           + (pright * right.getImpurity()); 
        gini = parent_impurity - impurity_after;
    }

    // Constructor for Split object if value is categorical.
    Split(String value, int column_num, String[][] data, double impurity,
            String name) {
        split_valueCat = value;
        variable = column_num;
        parent_data = data;
        parent_impurity = impurity;
        parent_name = name;

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
            // If value is same as split value, observation goes to the left node.
            else if(parent_data[i][variable].compareTo(split_valueCat) == 0)
                num_left += 1;
            // If value different from split value, observation goes
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
            // If value is same as split value, send observation to left node.
            else if(parent_data[n][variable].compareTo(split_valueCat) == 0) {
              countL++;  
              for(int p=0; p<ReadCSV.columns; p++) {
                  nodeLdata[countL][p] = parent_data[n][p];
              }
            }
            // If value is different from split value, send observation
            // to right node.
            else {
              countR++;
              for(int p=0; p<ReadCSV.columns; p++) {
                  nodeRdata[countR][p] = parent_data[n][p];
              }
            }
        }

        // Create new nodes.
        left = new Node(nodeLdata, parent_name + "1");
        right = new Node(nodeRdata, parent_name + "0");

        // Calculate gini diversity index for split.
        pleft = ((double) num_left / parent_data.length);
        pright = ((double) num_right / parent_data.length);
        impurity_after = (pleft * left.getImpurity())
           + (pright * right.getImpurity()); 
        gini = parent_impurity - impurity_after;
    }

    // Grab specified column from the data set
    private static String[] getColumn(String[][] data, int column_num) {
        String[] column = new String[data.length];
        for(int i=0; i<data.length; i++)
            column[i] = data[i][column_num];
        return column;
    }

    // Find the unique values in a column    
    private static String[] getUniqueVals(String[] column) {
        String[] uniqValsArray;

        Set<String> uniqVals = new LinkedHashSet<String>();
        for(String x : column) {
            // Ensure that blank values are not included as split option
            if(x.compareTo("") != 0)
                uniqVals.add(x);
        }

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

// Create variable objects that are specific to the particular dataset I am working
// with
class Variable {
    int column_num;
    String column_type;
    String column_name;

    Variable(int num, String type, String name) {
        column_num = num;
        column_type = type;
        column_name = name;
    }
}


// Main class
class TitanicClassTreeBuilder {
    public static void main(String args[]) {
        Variable[] variableList = new Variable[7];

        // Read data from Titanic CSV into 2-dimensional array.
        String[][] data = ReadCSV.readCSV("train_wo_header.csv", 891, 12);

        // Create Variable objects.
        variableList[0] = new Variable(2, "categorical", "Pclass");
        variableList[1] = new Variable(4, "categorical", "Sex");
        variableList[2] = new Variable(5, "numerical", "Age");
        variableList[3] = new Variable(6, "numerical", "SibSp");
        variableList[4] = new Variable(7, "numerical", "Parch");
        variableList[5] = new Variable(9, "numerical", "Fare");
        variableList[6] = new Variable(11, "categorical", "Embark");

        // Create root node.
        Node root = new Node(data, "root");

        // Begin splitting.
        root.chooseSplit(variableList, 10);
    }
}
