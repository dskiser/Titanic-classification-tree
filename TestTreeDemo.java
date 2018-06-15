import java.util.ArrayList;
import java.io.*;

// Create TestObservation object for test data.
class TestObservation {
    String[] row; // stores all the values of the line
    String[] split_variables; // stores only the values that will be split on

    TestObservation(String[] line) {
        row = line;

        // Create array of the variables that will be split on.
        split_variables = 
            new String[TestTreeDemo.metadata.split_vars.length];
        int count = 0;
        for(int x : TestTreeDemo.metadata.split_vars)
            split_variables[count++] = row[x];
    }
}

// Converts CSV into list of TestObservation objects.
class ReadTestCSV {

    public static ArrayList<TestObservation> readTestCSV(String csvfile) {
        int row = 0;
        String[] line;
        ArrayList<TestObservation> observations = 
            new ArrayList<TestObservation>();
        
        String s;
        try (BufferedReader br = new BufferedReader(
                    new FileReader(csvfile))){
            while((s = br.readLine()) != null) {
                line = s.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                TestObservation observation = new TestObservation(line);
                observations.add(observation);
            }
        } catch(IOException exc) {
            System.out.println(exc);
        }

        return observations;
    }
}

// Classify observation.
class Classify {
    public static String node_name;
    public static String type_column, split_value, assigned_class;

    public static String obs(String model, String start_node,
            TestObservation observation) {
        TestObservation ob = observation;
        String node_name = start_node;
        String new_node_name;
        String assigned_class = "error";
        int ch;
        String name_found;
        boolean node_found = false;
        boolean terminal_node_found = false;
        String defaultSplit = "left";

        System.out.print("Split Variables: ");
        for(int p = 0; p<observation.split_variables.length; p++)
            System.out.print(observation.split_variables[p] + " ");
        System.out.println();

        try(BufferedReader br = 
                new BufferedReader(new FileReader(model)))
        {
            do {
                // read chacters unti a '#' is found
                ch = br.read();
                if(ch == '#') {
                    // read node name
                    name_found = br.readLine();
                    if(name_found.compareTo(node_name) == 0) {
                        System.out.println(name_found);
                        node_found = true;
                        do {
                            ch = br.read();
                            // find column of variable type
                            if(ch == '$') {
                                type_column = br.readLine();
                                System.out.println("Variable column number: "
                                        + type_column);
                            }
                            // find split value
                            if(ch == '%') {
                                split_value = br.readLine();
                                System.out.println("Split value: " +
                                        split_value);
                            }
                            // find default split if missing value
                            if(ch == '*') {
                                defaultSplit = br.readLine();
                            }
                        } while(ch != '#');
                    }
                    // If name_found has 'terminal' added to end:
                    if(name_found.compareTo(node_name + "terminal") == 0) {
                        terminal_node_found = true;
                        System.out.println(name_found);
                        do {
                            ch = br.read();
                            if(ch == '^') {
                                assigned_class = br.readLine();
                                System.out.println("Assigned Class: " + 
                                        assigned_class);
                            }
                        } while((ch != '#') & (ch != -1));
                    }
                }
            } while((ch != -1) & !node_found & !terminal_node_found);
        } catch(IOException exc) {
            System.out.println(exc);
        }
        if(terminal_node_found==true)
            return assigned_class;
        else {
            // Convert String type_column to int.
            int type = Integer.parseInt(type_column);
            // Check if observation is missing split value.
            if(ob.split_variables[type].compareTo("") == 0) {
                if(defaultSplit.compareTo("left") == 0)
                    new_node_name = node_name + "1";
                else
                    new_node_name = node_name + "0";
                        // send observation in default node direction
                System.out.println("Warning: missing split value.");
            }
            else {
                // If split is on numerical variable:
                if(TestTreeDemo.metadata.split_vars_types[type] == "num") {
                    // Convert String split_value to double.
                    double split_num = Double.parseDouble(split_value);
                    if(Double.parseDouble(ob.split_variables[type])
                            < split_num)
                        new_node_name = node_name + "1";
                    else
                        new_node_name = node_name + "0";
                }
                // If split is on categorical variable:
                else {
                    if(ob.split_variables[type].compareTo(split_value) == 0)
                        new_node_name = node_name + "1";
                    else
                        new_node_name = node_name + "0";
                }
            }
        }
        assigned_class = obs(model, new_node_name, ob);
        return assigned_class;
    }
}

                          


// Uses model created by TestDemo to classify new observations.
class TestTreeDemo {
    public static MetaData metadata;

    public static void main(String args[]) {
        String model_file = args[0];
        String submission_file = args[1];
        ArrayList<TestObservation> observations;
        String classification;
        String id;

        // Create MetaData object that will be available to all methods.
        int[] split_column_nums = { 1, 3, 4, 5, 6, 8, 10 };
        String[] split_column_types = { "cat", "cat", "num", "num", "num",
             "num", "cat" };
        metadata = new MetaData(0, split_column_nums, split_column_types);

        // Convert CSV file into a list of TestObservation objects.
        observations = ReadTestCSV.readTestCSV("data/test_wo_header.csv");

        // Run observations through model and write submission file.
        try(FileWriter fw = new FileWriter(submission_file, true)) {
            fw.write("PassengerId,Survived\n");
            for(TestObservation observation : observations) {
                classification = Classify.obs(model_file, "root",
                        observation);
                id = observation.row[metadata.key];
                fw.write(id + "," + classification + "\n");
            }
        } catch(IOException exc) {
            System.out.println(exc);
        }
    }
}

