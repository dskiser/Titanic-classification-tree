import java.io.*;

// Read CSV
class ReadCSV {
    public static String[][] readCSV(String file, int lines, int variables) {
        String s;
        String file_name = file;
        int rows = lines;
        int columns = variables;
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

}

// Main class
class TitanicClassTree {
    public static void main(String args[]) {
        String[][] data = ReadCSV.readCSV("train_wo_header.csv", 891, 12);
        
        for(int i=0; i<12; i++)
            System.out.println(data[4][i]); 

        Node root = new Node(data);
        System.out.println("Total: " + root.getTotal());
        System.out.println("Impurity: " + root.getImpurity());
    }
}
