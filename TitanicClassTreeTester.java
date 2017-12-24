import java.io.*;
import java.util.*;

// Read CSV
class ReadCSV {
    public static int rows, columns;

    public static String[][] readCSV(String file, int lines, int variables,
            Variable[] variable_list) {
        String s;
        String file_name = file;
        rows = lines;
        columns = variables;
        String data[][] = new String[rows][columns];
        String processed_data[][] = new String[rows][variable_list.length];

        int row = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))){
            while((s = br.readLine()) != null) {
                data[row++] = s.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            }
        } catch(IOException exc) {
            System.out.println(exc);
        }

        // Eliminate columns that will not be used to sort observations.
        int count = 0;
        for(Variable x : variable_list) {
            for(int n=0; n<rows; n++)
                processed_data[n][count] = data[n][x.column_num];
            count++;
        }

        return processed_data;

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

// Classify an observation.
class Classify {

    public static String observation(String[][] data, int row_num,
           String file, String start_node, Variable[] var_list) {
        String[][] test_data = data;
        int row = row_num;
        int split_column = 0;
        String model_file = file;
        String node = start_node;
        String new_node;
        String assigned_cat = "?";
        int ch;
        double ob_num, split_num;
        String node_name, ob_val;
        String split_type = "";
        String split_value = "";
        String split_type_cat = "";
        Variable[] variable_list = var_list;
        // Flag that indicates when node has been found.
        int node_name_found = 0;
        Integer terminal_node_found = 0;

        try (BufferedReader br =
                new BufferedReader(new FileReader(model_file)))
        {
            do {
                // read characters until a # is found
                ch = br.read();
                if(ch == '#') {
                    node_name = br.readLine();
                    if(node_name.compareTo(node)==0) {
                        node_name_found = 1;
                        System.out.println(node_name);
                    }
                    if(node_name.compareTo(node + "terminal") == 0) {
                        terminal_node_found = 1;
                        System.out.println("Terminal node found.");
                    }
                }
                if((ch == '^') & (terminal_node_found == 1)) {
                    assigned_cat = br.readLine();
                    System.out.println("Assigned cat: " + assigned_cat);
                    break;
                }
                if((ch == '$') & (node_name_found == 1)) {
                    split_type = br.readLine();
                }
                if((ch == '%') & (node_name_found == 1)) {
                    split_value = br.readLine();
                    node_name_found = 0;
                    break;
                }
            } while(ch != -1);
        } catch(IOException exc) {
            System.out.println(exc);
        }
        if(terminal_node_found == 1) {
            terminal_node_found = 0;
            return assigned_cat;
        }
        // Determine the next node to drop observation to.
        // First determine whether splitting value is numerical or
        // categorical.
        int count = 0;
        for(Variable x : variable_list) {
            if(x.column_name.compareTo(split_type) == 0) {
                split_type_cat = x.column_type;
                split_column = count;
            }
            count++;
         }

         // If value is numerical, split using '<'
         if(split_type_cat.compareTo("numerical") == 0) {
             ob_num = Double.parseDouble(data[row][split_column]);
             split_num = Double.parseDouble(split_value);
             if(ob_num < split_num)
                new_node = node + "1";
             else
                new_node = node + "0";
         }
         // If value is categorical, split using '='
         else {
             ob_val = data[row][split_column];
             if(ob_val.compareTo(split_value) == 0)
                 new_node = node + "1";
             else
                 new_node = node + "0";
         }
         
         // Call observation() again with name of new node.  Will keep calling
         // observation() until reaches a terminal node, at which point the
         // method will return.
         assigned_cat = observation(test_data, row, model_file, new_node,
                 variable_list);
         return assigned_cat;
     }
}


              


// Titanic Classification Tree Tester
class TitanicClassTreeTester {
    public static void main(String args[]) {
        String test_model = args[0];
        String classification;
        String id;
        
        // Create variable objects.
        Variable[] variableList = new Variable[8];
        variableList[0] = new Variable(0, "", "ID");
        variableList[1] = new Variable(1, "categorical", "Pclass");
        variableList[2] = new Variable(3, "categorical", "Sex");
        variableList[3] = new Variable(4, "numerical", "Age");
        variableList[4] = new Variable(5, "numerical", "SibSp");
        variableList[5] = new Variable(6, "numerical", "Parch");
        variableList[6] = new Variable(8, "numerical", "Fare");
        variableList[7] = new Variable(10, "categorical", "Embark");
        
        // Read data from Titanic CSV into 2-dimensional array.
        String[][] data = ReadCSV.readCSV("test_wo_header.csv", 418, 11,
                variableList);
        
        try(FileWriter fw = new FileWriter("submission1.txt", true)) {
            fw.write("PassengerId,Survived\n");
            for(int n=0; n<ReadCSV.rows; n++) {
                classification = Classify.observation(data, n, test_model,
                    "root", variableList);
                id = data[n][0];
                fw.write(id + "," + classification + "\n");
            }
        } catch(IOException exc) {
            System.out.println(exc);
        }
    }
}
        
