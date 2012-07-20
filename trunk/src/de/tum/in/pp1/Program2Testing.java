package de.tum.in.pp1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * Program 2: Testing of Program 1:

 */
public class Program2Testing {
	//the build classifier SVM model path
	private static String modelPath = "trainedModel.save";
	private static String testingSetPath = "";
	//the path where we write the output of the predictions
	private static String resultOutputPath = "";
	
	
	/**
	 * The main method containing the main code.
	 * Command line parameters:
	 * 1. a path to an output of Program 1 (the SVM model)
	 * 2. a path to the testing set (fasta files)
	 * 3. a path to the output file/folder.
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length < 3) {
			System.out.println("Missing arguments. Proper Usage is: java -jar programname.jar [modelPath] [testingSetPath] [resultOutputPath]");
	        System.exit(0);
		}
		modelPath = args[0];
		testingSetPath = args[1];
		resultOutputPath = args[2];
		
		Instances testingData;
		List<Double> predictions;
		Attribute idAndPositionAtt;
		double[] classification;
		
		try { 
			
			//load trained model
			AbstractClassifier svmScheme = loadModel(modelPath);

			String arffFilePath = PredictProteinRunner.generateARFF(testingSetPath, false);
			
			//Read testing dataset
			//testingData = ProteinUtils.loadDataset(testingSetPath, false);
			testingData = ProteinUtils.loadDataset(arffFilePath, true);

			//Map<String,String> proteinToTrueClass = ProteinUtils.getTestsetAsMap(testingData); //TO BE REMOVED FOR NOT LABELED TESTSET
			
			//keep the first attribute - ids and positions of the amino acids
			idAndPositionAtt = testingData.attribute(0);
			//classification = testingData.attributeToDoubleArray(testingData.numAttributes()-1); //TO BE REMOVED FOR NOT LABELED TESTSET
			
			//we don't need the first string attribute for prediction
			testingData = ProteinUtils.removeNotImportantAttributes(testingData);

			//Predict
			predictions = predictTestingSet(testingData, svmScheme);
			
			Map<String,String> proteinId2Class = ProteinUtils.postProcessPredictions(predictions, idAndPositionAtt, svmScheme);
		
			//int[] histogram = ProteinUtils.getPercentageResidueAccuracyPerProtein(predictions, idAndPositionAtt, classification,proteinId2Class); //TO BE REMOVED FOR NOT LABELED TESTSET
			
			//float qok = ProteinUtils.calculateQok(proteinId2Class, proteinToTrueClass); //TO BE REMOVED FOR NOT LABELED TESTSET
			//System.out.println("Qok=" + qok); ////TO BE REMOVED FOR NOT LABELED TESTSET

			//debugPredictions(predictions, classification, proteinId2Class);
			//ProteinUtils.reEvaluate(proteinId2Class, classification); //TO BE REMOVED FOR NOT LABELED TESTSET!
			//save the predictions in file
			writeOutput(resultOutputPath, proteinId2Class, testingSetPath);
			//SerializationHelper.write(resultOutputPath, proteinId2Class);
			System.out.println("Predictions saved in " + resultOutputPath);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the predictions in new fasta files. The header and the sequence are taken from the original fasta files.
	 * @param resultOutputPath2 the resulting directory where the new files will be written in
	 * @param proteinId2Class the prediction map
	 * @param testingSetPath the path of the original testing fasta files
	 */
	private static void writeOutput(String resultOutputPath2,
			Map<String, String> proteinId2Class, String testingSetPath) {
		FileWriter fstream;
		for (String protein : proteinId2Class.keySet()){
			try {
				//System.out.println(resultOutputPath2);
				//System.out.println(protein);
				fstream = new FileWriter(resultOutputPath2 + '/' + protein +".fasta");
				BufferedWriter out = new BufferedWriter(fstream);
				
				//read the header and the sequence from fasta file
				  FileInputStream originalFastaFstream = new FileInputStream(testingSetPath + '/' + protein + ".fasta");
				  // Get the object of DataInputStream
				  InputStream in = new DataInputStream(originalFastaFstream);
				  BufferedReader br = new BufferedReader(new InputStreamReader(in));
				  String sequence;
				  //Read fasta file header+sequence
				  sequence = br.readLine() + "\n";
				  sequence += br.readLine() + "\n";
				  //Close the input stream
				  in.close();
				
				  
				out.write(sequence);
				out.write(proteinId2Class.get(protein));
				out.newLine();
				out.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	private static void debugPredictions(List<Double> predictions, double[] classification, Map<String, String> proteinId2Class) {
		FileWriter fstream;
		try {
			String postproces = "";
			fstream = new FileWriter("out.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			
			for (Iterator<String> iterator = proteinId2Class.keySet().iterator(); iterator.hasNext();) {
				String prot = (String) iterator.next();
				postproces = postproces.concat(proteinId2Class.get(prot));
			}
			out.write("pred post clas");
			out.newLine();
			for (int i = 1; i < predictions.size(); i++) {
				char pred = predictions.get(i) == 0.0 ? '+' : '-';
				char post = postproces.charAt(i);
				char clas = classification[i] == 0.0 ? '+' : '-';
				//System.out.println(pred + " " + post + " " + clas); //(float)svmScheme.distributionForInstance(testingData.instance(i))[0]
				out.write(pred + " " + post + " " + clas);
				out.newLine();
			}
			// Close the output stream
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Load trained AbstractClassifier model from the specified path
	 * @param theModelPath
	 * @return {@link AbstractClassifier} object
	 */
	private static AbstractClassifier loadModel(String theModelPath) {
		try {
			return (AbstractClassifier)SerializationHelper.read(theModelPath);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Classifies each instance from the testing set using SVM.
	 * @param testingData the instances to be predicted
	 * @param svmScheme the SVM
	 * @return list of predictions for each instance from testingData respectively.
	 * @throws Exception
	 */
	public static List<Double> predictTestingSet(Instances testingData, AbstractClassifier svmScheme) throws Exception {
		System.out.println("\n Testset prediction...");
		
		Evaluation eval = new Evaluation(testingData);
		double[] predictions = eval.evaluateModel(svmScheme, testingData);
		
		List<Double> predictionList = Arrays.asList(ArrayUtils.toObject(predictions));
//		System.out.println(eval.toSummaryString("\nResults\n======\n", true));
//		System.out.println(eval.toClassDetailsString());
//		System.out.println(eval.toMatrixString());
		return  predictionList;
		
	}
	

	
}
