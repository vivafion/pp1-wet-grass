package de.tum.in.pp1;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
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
	private static String trainingSetPath;
	
	
	/**
	 * The main method containing the main code.
	 * Command line parameters:
	 * 1. a path to an output of Program 1 (the SVM model)
	 * 2. a path to the testing set
	 * 3. a path to the output file/folder.
	 * 4. a path to the training set (optional)  why do we need training set?
 *
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
		
		//optional
		if (args.length > 3){
			trainingSetPath = args[3];
		}
		
		Instances testingData;
		List<Double> predictions;
		Attribute idAndPositionAtt;
		
		try { 
			
			//load trained model
			weka.classifiers.functions.LibSVM svmScheme = loadModel(modelPath);

			//Read testing dataset
			//testingData = ProteinUtils.loadDataset(testingSetPath, false);
			testingData = ProteinUtils.loadDataset(testingSetPath, true);
			
			//keep the first attribute - ids and positions of the amino acids
			idAndPositionAtt = testingData.attribute(0);
			
			//we don't need the first string attribute for prediction
			testingData = ProteinUtils.removeFirstAttribute(testingData);

			//Predict
			predictions = predictTestingSet(testingData, svmScheme);
		
			
			Map<String,String> proteinId2Class = ProteinUtils.postProcessPredictions(predictions, idAndPositionAtt);
			
			//save the predictions in file
			SerializationHelper.write(resultOutputPath, proteinId2Class);
			System.out.println("Predictions saved in " + resultOutputPath);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load trained LibSVM model from the specified path
	 * @param theModelPath
	 * @return {@link LibSVM} object
	 */
	private static LibSVM loadModel(String theModelPath) {
		try {
			return (weka.classifiers.functions.LibSVM)SerializationHelper.read(theModelPath);
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
	public static List<Double> predictTestingSet(Instances testingData, weka.classifiers.functions.LibSVM svmScheme) throws Exception {
		System.out.println("\n Testset prediction...");
		
		Evaluation eval = new Evaluation(testingData);
		double[] predictions = eval.evaluateModel(svmScheme, testingData);
		
		List<Double> predictionList = Arrays.asList(ArrayUtils.toObject(predictions));
		System.out.println(eval.toSummaryString("\nResults\n======\n", true));
		System.out.println(eval.toClassDetailsString());
		System.out.println(eval.toMatrixString());
		return  predictionList;
		
	}
	

	
}
