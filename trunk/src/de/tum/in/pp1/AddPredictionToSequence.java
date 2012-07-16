package de.tum.in.pp1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class AddPredictionToSequence {

	/**
	 * The main method containing the main code.
	 * Command line parameters:
	 * 1. a path to the output dir of Program 2
	 * 2. a path to the fasta files for which predictions are being made
	 * @param args
	 */
	public static void main(String[] args) {
		File predictionDir = new File(args[0]);
		File fastaDir = new File(args[1]);
		File outputDir = new File(args[2]);

		for (String file : predictionDir.list()){
			try{
				  // Open the file that is the first 
				  // command line parameter
				  FileInputStream fstream = new FileInputStream(predictionDir + predictionDir.separator + file);
				  // Get the object of DataInputStream
				  DataInputStream in = new DataInputStream(fstream);
				  BufferedReader br = new BufferedReader(new InputStreamReader(in));
				  String Prediction;
				  //Read File second file line
				  Prediction = br.readLine();
				  Prediction = br.readLine();
				  //Close the input stream
				  in.close();
				  
				  fstream = new FileInputStream(fastaDir + predictionDir.separator + file);
				  // Get the object of DataInputStream
				  in = new DataInputStream(fstream);
				  br = new BufferedReader(new InputStreamReader(in));
				  String Sequence;
				  //Read File fasta file header+sequence
				  Sequence = br.readLine() + "\n";
				  Sequence += br.readLine() + "\n";
				  //Close the input stream
				  in.close();
				  
				  FileWriter fwstream;
				  fwstream = new FileWriter(outputDir + predictionDir.separator + file);
					BufferedWriter out = new BufferedWriter(fwstream);
					out.write(Sequence);
					out.write(Prediction + "\n");
					out.close();
				    }catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
				  }

		}

	}

}
