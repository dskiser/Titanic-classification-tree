import java.util.ArrayList;
import java.util.Collections;
import java.io.*;

// Stores metadata for classification data set.
class MetaData {
    int key; // column that contains identification of each row
    int class_column; // column that contains classification of observations 
    int[] split_vars; // list of columns that will be split on
    String[] split_vars_types; // types of the variables that will be split on

    // Metadata for training set.
    MetaData(int key_column, int response_column, int[] splitting_variables,
           String[] splitting_var_types) {
        key = key_column;
        class_column = response_column;
        split_vars = splitting_variables;
        split_vars_types = splitting_var_types;
    }

    // Metadata for test set.
    MetaData(int key_column, int[] splitting_variables,
           String[] splitting_var_types) {
        key = key_column;
        split_vars = splitting_variables;
        split_vars_types = splitting_var_types;
    }

}

// Create Observation object for training data.
class Observation {
    String[] row; // stores all the values of the line
    String[] split_variables; // stores only the values that will be split on
    double classification; // stores the classification of an observation.

    Observation(String[] line) {
        row = line;

        // Create array of the variables that will be split on.
        split_variables = new String[TreeDemo.metadata.split_vars.length];
        int count = 0;
        for(int x : TreeDemo.metadata.split_vars)
            split_variables[count++] = row[x];
        
        // Find classification of an observation, converting string to double
        // for calculation purposes.
        classification = Double.parseDouble(
                row[TreeDemo.metadata.class_column]);
    }
}

// Converts CSV into list of Observation objects.
class ReadCSV {

    public static ArrayList<Observation> readCSV(String csvfile) {
        int row = 0;
        String[] line;
        int null_value = 0;
        ArrayList<Observation> observations = new ArrayList<Observation>();
        
        String s;
        try (BufferedReader br = new BufferedReader(
                    new FileReader(csvfile))){
            while((s = br.readLine()) != null) {
                line = s.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                // Check for null values in line.
                for(int x : TreeDemo.metadata.split_vars)
                    if(line[x].compareTo("") == 0) null_value = x;
                // If no null values, create Observation object and add it
                // to observations list.
                if(null_value == 0) {
                    Observation observation = new Observation(line);
                    observations.add(observation);
                }
                else {
                    System.out.println("Missing value from column: " + null_value);
                    null_value = 0; // reset null_value indicator
                }
            }
        } catch(IOException exc) {
            System.out.println(exc);
        }

        return observations;
    }
}

// Creates two dimensional ArrayList object and provides methods for adding
// and getting elements at specified indices.
class TwoDArrayList {
    ArrayList<ArrayList<String>> matrix;

    TwoDArrayList(int num_variables) {
        matrix = new ArrayList<ArrayList<String>>();
        for(int p=0; p<num_variables; p++)
            matrix.add(new ArrayList<String>());
    }

    public boolean checkvalues(int variable_num, String value) {
        return this.matrix.get(variable_num).contains(value);
    }

    public void addvalue(int variable_num, String value) {
        this.matrix.get(variable_num).add(value);
    }

    public String getvalue(int variable_num, int value_num) {
        return this.matrix.get(variable_num).get(value_num);
    }

    public int getsize(int variable_num) {
        return this.matrix.get(variable_num).size();
    }

    public void removeMin(int variable_num) {
        Collections.sort(this.matrix.get(variable_num));
        this.matrix.get(variable_num).remove(0);
        this.matrix.get(variable_num).trimToSize();
    }
}



// Builds nodes and determines whether a node is terminal.  If node is not
// terminal, determines best split.
class Node {
    ArrayList<Observation> observations; // observations contained in node
    String name; // name used by TreeTester to send observ. to correct tree
    double impurity; // impurity of the tree
    double classONE, classZERO, classALL; // counts of observation classes
    TwoDArrayList splitVals;
    String default_split; // stores the node (left or right) that observations 
                          // should be sent to if it is missing the split value

    Node(ArrayList<Observation> node_observations, String node_name) {
        observations = node_observations;
        name = node_name;
        classONE = countClass(1.0, observations);
        classZERO = countClass(0.0, observations);
        classALL = observations.toArray().length;
        impurity = this.calcImpurity(observations, classONE, classALL);
        splitVals = this.findSplitVals(observations);
        default_split = "left";
    }

    private double countClass(double classNUM, ArrayList<Observation> obs) {
        int count = 0;
        for(Observation observation : obs)
            if(observation.classification == classNUM) count++;
        return count;
    }

    private double calcImpurity(ArrayList<Observation> observations, 
            double num_in_classONE, double num_total) {
        double prop_ONE = num_in_classONE / num_total;
        double impurity = 2 * prop_ONE * (1 - prop_ONE); 
        return impurity;
    }

    private TwoDArrayList findSplitVals(ArrayList<Observation> observations) {
        TwoDArrayList splitVals = 
            new TwoDArrayList(TreeDemo.metadata.split_vars.length);

        for(Observation observation : observations) {
            int p = 0;
            for(String split_value : observation.split_variables) {
                if(!splitVals.checkvalues(p, split_value))
                    splitVals.addvalue(p, split_value);
                p++;
            }
        }

        // If node contains observations and variable is numerical, remove 
        // minimum value.
        if(observations.size() > 1) {
            for(int p=0; p<TreeDemo.metadata.split_vars.length; p++) 
                if(TreeDemo.metadata.split_vars_types[p].compareTo("num") == 0)
                    splitVals.removeMin(p);
        }
        return splitVals;
    }

    public void splitNode(FileWriter fw) throws IOException {
        // Declare left and right nodes.
        ArrayList<Observation> empty = new ArrayList<Observation>();
                        // empty observation object
        Node left = new Node(empty, "");
        Node right = new Node(empty, "");

        // If node is less than or equal to stop size or impurity equals zero,
        // do not split.
        if((observations.size() <= TreeDemo.stop_size) | (impurity == 0.0)) {
            name = name + "terminal";

            // Output terminal node information.
            fw.write("#" + name + "\n");
            fw.write("Total: " + classALL + " Survived: " + classONE +
                    " Died: " + classZERO + "\n");
            fw.write("Impurity: " + impurity + "\n");
            if(classONE >= classZERO) fw.write("^1\n");
            else fw.write("^0\n"); // If number in classification 1 greater than
                                 // classification 0, assign to 1, and vice versa
            fw.write("\n");
        }
        // Otherwise, go ahead with split.
        else {
            // Set initial Gini Diversity Index to zero.  Want to find largest
            // Gini possible.
            double gini = 0;
            String split_value_used = "";
            int index_of_variable_used = -1;

            // For each variable:
            for(int p=0; p<TreeDemo.metadata.split_vars.length; p++) {
                // If numerical:
                if(TreeDemo.metadata.split_vars_types[p].compareTo("num") == 0) {
                    double split_value;
                    // For each split value:
                    for(int n=0; n<splitVals.getsize(p); n++) {
                        // Declare observation lists for left and right node.
                        ArrayList<Observation> left_obs = new ArrayList<>();
                        ArrayList<Observation> right_obs = new ArrayList<>();
                        
                        split_value = Double.parseDouble(
                                splitVals.getvalue(p, n));
                        // Compare split value to value of each observation in
                        // node.
                        for(Observation observation : observations) {
                           // if value less than split value, send to left node
                           if(Double.parseDouble(observation.split_variables[p])
                                  < split_value)
                               left_obs.add(observation);
                           // if value more than split value, send to right node
                           else
                               right_obs.add(observation);
                        }
                        // Create proposed left and right nodes.
                        Node proposed_left = new Node(left_obs, name + "1");
                        Node proposed_right = new Node(right_obs, name + "0");

                        // Calculate Gini Diversity Index of split.
                        double left_size = proposed_left.observations.size();
                        double right_size = proposed_right.observations.size();
                        double total_size = classALL;

                        double prop_left = left_size / total_size;
                        double prop_right = right_size / total_size;

                        double new_gini = impurity 
                            - (prop_left * proposed_left.impurity)
                            - (prop_right * proposed_right.impurity);
                        // If gini is the largest of the splits attempted thus
                        // far, keep proposed left and right nodes.
                        if(new_gini > gini) {
                            gini = new_gini;
                            left = proposed_left;
                            right = proposed_right;
                            // If right node has more observations than left node,
                            // make right node the default split if split value is 
                            // missing.  Otherwise, default is left.
                            if(right.observations.size() > left.observations.size())
                                default_split = "right";
                            else
                                default_split = "left";
                            split_value_used = Double.toString(split_value);
                            index_of_variable_used = p;
                        }
                    }
                }
                // If categorical:
                else {
                    // Do not split on categorical variable if only one category 
                    // is present in node.
                    if(!(splitVals.getsize(p) <= 1)) {
                        String split_value;
                        // For each split value:
                        for(int n=0; n<splitVals.getsize(p); n++) {
                            // Declare observation lists for left and right node.
                            ArrayList<Observation> left_obs = new ArrayList<>();
                            ArrayList<Observation> right_obs = new ArrayList<>();

                            split_value = splitVals.getvalue(p, n);
                            // Compare split value to value of each observation in
                            // node.
                            for(Observation observation : observations) {
                                // if value equal to split value, send to left 
                                // node
                                if(observation.split_variables[p].compareTo(
                                            split_value) == 0)
                                    left_obs.add(observation);
                                // If value not equal to split value, send to 
                                // right node.
                                else
                                    right_obs.add(observation);
                            }
                            // Create proposed left and right nodes.
                            Node proposed_left = new Node(left_obs, name + "1");
                            Node proposed_right = new Node(right_obs, name + "0");

                            // Calculate Gini Diversity Index of split.
                            double left_size = proposed_left.observations.size();
                            double right_size = proposed_right.observations.
                                size();
                            double total_size = classALL;

                            double prop_left = left_size / total_size;
                            double prop_right = right_size / total_size;

                            double new_gini = impurity 
                                - (prop_left * proposed_left.impurity)
                                - (prop_right * proposed_right.impurity);
                            // If gini is the largest of the splits attempted thus
                            // far, keep proposed left and right nodes.
                            if(new_gini > gini) {
                                gini = new_gini;
                                left = proposed_left;
                                right = proposed_right;
                                // If right node has more observations than left 
                                // node, make right node the default split if split                                 // value is missing.  Otherwise, default is left.
                                if(right.observations.size() > 
                                        left.observations.size())
                                    default_split = "right";

                                split_value_used = split_value;
                                index_of_variable_used = p;
                            }
                        }
                    }
                }
            }
            // Output information of node that has just been split.
            fw.write("#" + name + "\n");
            fw.write("Total: " + classALL + " Survived: " + classONE
                    + " Died: " + classZERO + "\n");
            fw.write("Impurity: " + impurity + "\n");
            fw.write("$" + index_of_variable_used + "\n");
            fw.write("%" + split_value_used + "\n");
            fw.write("*" + default_split + "\n");
            fw.write("\n");
            // At this point, nodes declared left and right are kept.
            // Split nodes again.
            left.splitNode(fw);
            right.splitNode(fw);
        } // closes split option
    } // closes splitNode() method
}

// Main class that calls the classes that build the Classification Tree Model.
class TreeDemo {

    public static MetaData metadata;

    // Define criterion to make node a terminal node.
    public static int stop_size = 200;

    public static void main(String args[]) {
        ArrayList<Observation> observations;

        // Create MetaData object that will be available to all methods.
        int[] split_column_nums = { 2, 4, 5, 6, 7, 9, 11 };
        String[] split_column_types = { "cat", "cat", "num", "num", "num",
           "num", "cat" }; 
        metadata = new MetaData(0, 1, split_column_nums, split_column_types); 

        // Convert CSV file into a list of Observation objects.
        observations = ReadCSV.readCSV("data/estimated_ages2.csv");
        
        
        // Create root node.
        Node root = new Node(observations, "root");

        // Write model file.
        try(FileWriter fw = new FileWriter("model32.txt", true)) {

            // Showing root node information.
            fw.write("Total observations without missing values: " + 
                root.classALL + "\n"); // count observations to see how many
                                // observations were kept from original csv
            fw.write("Survived: " + root.classONE + "\n");
            fw.write("Died: " + root.classZERO + "\n");
            fw.write("Root Class Impurity: " + root.impurity + "\n");
            fw.write("\n");
        
            // Build tree.
            root.splitNode(fw); // pass fw so that splitNode can write file

            // Show misclassification probability.

        } catch(IOException exc) {
            System.out.println(exc);
        }
    }
}



