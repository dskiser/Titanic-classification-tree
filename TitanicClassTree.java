import java.io.*;

// Observation object
class Observation {
    private int id, survived, pclass, sibsp, parch;
    private String sex, ticket, cabin, embark;
    private double fare, age;

    Observation(String[] array) {
        String[] variables = array;
        
        if(variables[0].compareTo("") != 0)
            id = Integer.parseInt(variables[0]);
        if(variables[1].compareTo("") != 0)
            survived = Integer.parseInt(variables[1]);
        if(variables[2].compareTo("") != 0)
            pclass = Integer.parseInt(variables[2]);
        sex = variables[5];
        if(variables[6].compareTo("") != 0)
            age = Double.parseDouble(variables[6]);
        if(variables[7].compareTo("") != 0)
            sibsp = Integer.parseInt(variables[7]);
        if(variables[8].compareTo("") != 0)
        parch = Integer.parseInt(variables[8]);
        ticket = variables[9];
        if(variables[10].compareTo("") != 0)
            fare = Double.parseDouble(variables[10]);
        cabin = variables[11];
        embark = variables[12];
    }

    public void getObs() {
        System.out.println(id + " " + survived + " " + pclass + " " + sex + " "
                + age + " " + sibsp + " " + parch + " " + ticket + " " + fare +
                " " + cabin + " " + embark);
    }

    public boolean survived() {
        if(survived == 1) 
            return true;
        else
            return false;
    }
}

// Read CSV
class ReadCSV {
    public static Observation[] readCSV(String file, int observ) {
        String s;
        String file_name = file;
        int num_observ = observ;
        int count = 0;

        Observation observations[] = new Observation[num_observ];

        try (BufferedReader br = new BufferedReader(new FileReader(file_name))){
            while((s = br.readLine()) != null) {
                String[] variables = s.split(",", -1);
                observations[count++] = new Observation(variables);
            }
        } catch(IOException exc) {
            System.out.println(exc);
        }

        return observations;

    }
}

// Node object
class Node {
    private int survived, total;
    private double p_survived, impurity;
    
    Node(Observation[] array) {
        Observation[] observations = array;
        
        survived = 0;
        total = observations.length;

        for(Observation x : observations) {
            if(x.survived())
                survived += 1;
        }

        p_survived = (double) survived / (double) total;
        impurity = 2 * p_survived * (1 - p_survived);
    } 
    
    public double getTotal() {
        return total;
    }

    public double getImpurity() {
        return impurity;
    }

    public double getPropSurv() {
        return p_survived;
    }


//    private ArrayList<Integer> getPossibleSplits(Observation array[], int column) {
//        Observation[] node_obs = array;
//        int var_num = column;
//        ArrayList<Integer> uniq_vals = new ArrayList();
//        
//        for(Observation x : node_obs) {
//            if(x
}
            
            
        


// Main class
class TitanicClassTree {
    public static void main(String args[]) {
        Observation[] observations = ReadCSV.readCSV("train_wo_header.csv", 891);
        
        Node root = new Node(observations);
        System.out.println("Total: " + root.getTotal());
        System.out.println("Proportion Surviving: " + root.getPropSurv());
        System.out.println("Impurity: " + root.getImpurity());
    }
}
