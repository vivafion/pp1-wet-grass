/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fasta2trainingdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author delur
 */
public class Fasta2TrainingData {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        HashMap<String, Integer> aa2Vector = new HashMap<String, Integer>();

        aa2Vector.put("A", 1);
        aa2Vector.put("R", 2);
        aa2Vector.put("N", 3);
        aa2Vector.put("D", 4);
        aa2Vector.put("C", 5);
        aa2Vector.put("E", 6);
        aa2Vector.put("Q", 7);
        aa2Vector.put("G", 8);
        aa2Vector.put("H", 9);
        aa2Vector.put("I", 10);
        aa2Vector.put("L", 11);
        aa2Vector.put("K", 12);
        aa2Vector.put("M", 13);
        aa2Vector.put("F", 14);
        aa2Vector.put("P", 15);
        aa2Vector.put("S", 16);
        aa2Vector.put("T", 17);
        aa2Vector.put("W", 18);
        aa2Vector.put("Y", 19);
        aa2Vector.put("V", 20);
        aa2Vector.put("X", 21);


        File directory = new File(args[0]);
        FileWriter fstream = new FileWriter(args[1]);
        BufferedWriter out = new BufferedWriter(fstream);
        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".fasta")) {
                //System.out.println(file.getName());
                FileReader input = new FileReader(file);
                BufferedReader bufRead = new BufferedReader(input);

                String index = bufRead.readLine();
                String sequence = bufRead.readLine();
                //System.out.println(sequence);
                String sequence2 = bufRead.readLine();
                String structure = bufRead.readLine();
                //System.out.println(structure);
                bufRead.close();
                input.close();

                for (int i = 0; i < sequence.length() - 7; i++) {
                    String subString = sequence.substring(i, i + 7);
                    String line = "";
                    if (structure.charAt(i + 4) == 'H' || structure.charAt(i + 4) == 'U') {
                        line += "1 ";
                    } else {
                        line += "-1 ";
                    }

                    for (int j = 0; j < subString.length(); j++) {
                        int number = 0;
                        try{
                            number = aa2Vector.get(String.valueOf(subString.charAt(j)));
                        }
                        catch (NullPointerException error) {
                            System.out.println("Error!! " + subString.charAt(j));
                        }
                        number = number + (j * 21);
                        
                        line += ""+number + ":1 ";
                    }
                    out.write(line+"\n");
                    //System.out.println(line);
                }
            }


        }



    }
}
