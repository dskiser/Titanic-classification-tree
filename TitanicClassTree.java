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

// Main class
class TitanicClassTree {
    public static void main(String args[]) {
        Observation[] observations = ReadCSV.readCSV("train1.csv", 891);
        int survived = 0;
        int died = 0;
        for(Observation x : observations) {
            if(x.survived())
                survived += 1;
            else
                died += 1;
        }

        System.out.println("Survived: " + survived);
        System.out.println("Died: " + died);

    }
}
