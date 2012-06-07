package de.tum.in.pp1;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Debug.Random;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;

/**
 * Program 1: Model Building and Parameter Optimization
 */
public class Program1ModelBuilding {
	
	private static String trainingSetPath = "";
	//this is the path where the built classifier model is saved
	private static String outputPath = "";
	private static final double SUBSAMPLE_SIZE = 10.0; // percent
	
	//Sets the bias towards a uniform class. A value of 0 leaves the class distribution as-is, 
	//a value of 1 ensures the class distributions are uniform in the output data.
	private static final double BIAS_TO_UNIFORM_CLASS = 1.0;
	private static final int FOLDS = 2;
	private static final double SPLIT_PERCENTAGE = 66.0;
	
	
/**
 *  Command line params:
 *  1. a path to the training set
 *  2. a path to the output file/folder
 * @param args
 */
	public static void main(String[] args) {
		
		//check the command line parameters
		if (args.length < 2) {
			System.out.println("Missing arguments. Proper Usage is: java -jar programname.jar [trainingSetPath] [svmOutputPath] ");
	        System.exit(0);
		}
		trainingSetPath = args[0];
		outputPath = args[1];
		
		
		Instances data = null;
	 	try {
	 		
	 		//Load the Protein dataset
	 		data = ProteinUtils.loadDataset(trainingSetPath);
			
			//filtering
	 		//we don't need the first string attribute
	 		data = ProteinUtils.removeFirstAttribute(data);
			
	 		//create subsample of the dataset
	 		//data = resampleDataset(data);
	 		
	 		//randomly shuffle the dataset
			//data = Utils.randomizeDataset(data);
			
			
			// feature reduction
			//data = filterImportantAttributes(data);
			
			
			 //create new instance of SVM
			LibSVM svmScheme = buildSVM(data);
			 
			//save the model using the output path
			saveModel(svmScheme);
			
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Produces a random subsample of a dataset using either sampling with replacement or without replacement.
	 * The filter can be made to maintain the class distribution in the subsample, or to bias the class distribution toward a uniform distribution
	 * @param data the dataset to be resampled
	 * @return subsample instances
	 * @throws Exception
	 */
	public static Instances resampleDataset(Instances data) throws Exception {
		System.out.println("\n Resample dataset...");
		Resample sampler = new Resample();
		sampler.setRandomSeed((int)System.currentTimeMillis());
		sampler.setSampleSizePercent(SUBSAMPLE_SIZE);
		sampler.setBiasToUniformClass(BIAS_TO_UNIFORM_CLASS);
		sampler.setNoReplacement(true);
		sampler.setInputFormat(data);
		data = Resample.useFilter(data, sampler);
		return data;
	}
	
	/**
	 * A supervised attribute filter that can be used to select attributes. 
	 * TODO: Optimize the parameters!
	 * @param data the dataset
	 * @return instances without the less important attributes.
	 * @throws Exception
	 */
	public static Instances filterImportantAttributes(Instances data) throws Exception {
		System.out.println("\n Select important attributes...");
		AttributeSelection filter = new AttributeSelection();
		CfsSubsetEval eval = new CfsSubsetEval();
		GreedyStepwise search = new GreedyStepwise();
		search.setSearchBackwards(true);//Added by Aparna on 04/06/2012  
		filter.setEvaluator(eval);
		filter.setSearch(search);
		filter.setInputFormat(data);

		// generate new data
		Instances newData = Filter.useFilter(data, filter);
		System.out.println("\n Number of selected attributes: " + newData.numAttributes()); 
		return newData;
	}
	
	/**
	 * Trains an SVM model using the provided training set.
	 * @param data the training instances
	 * @return trained {@link LibSVM}
	 * @throws Exception
	 */
	public static LibSVM buildSVM(Instances data) throws Exception {
		System.out.println("\n Building SVM...");
		LibSVM svmScheme = setupSVM();
		svmScheme.buildClassifier(data);
		return svmScheme;
	}
	
	/**
	 * Creates new LibSVM and sets the options.
	 * There are a lot of parameters to be adjusted for the SVM!!!
	 * @return SVM that is still not trained
	 * @throws Exception
	 */
	public static LibSVM setupSVM() throws Exception{
		LibSVM svmScheme = new weka.classifiers.functions.LibSVM();
		// set options
		//svmScheme.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
		svmScheme.setOptions(weka.core.Utils.splitOptions("weka.classifiers.functions.LibSVM -S 0 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1"));
		return svmScheme;
	}
	
	
	/**
	 * Evaluates the SVM classifier on a given set of instances. Note that the data must have exactly the same format 
	 * (e.g. order of attributes) as the data used to train the classifier!
	 * The percentage split method is used to split the set of instances into training and testing set.
	 * @param trainingData
	 * @return list of prediction
	 * @throws Exception
	 */
	public static List<Double> evaluatePercentageSplit(Instances trainingData) throws Exception {
		//percentage split
		System.out.println("\n Percentage Split Evaluation...");
		int trainSize = (int) Math.round(trainingData.numInstances() * SPLIT_PERCENTAGE / 100); 
		int testSize = trainingData.numInstances() - trainSize; 
		Instances train = new Instances(trainingData, 0, trainSize); 
		Instances test = new Instances(trainingData, trainSize, testSize); 
		
		//build the SVM classifier using the training set
		weka.classifiers.functions.LibSVM svmScheme = buildSVM(train);
		
		Evaluation eval = new Evaluation(train);
		
		//evaluate the performance using the testing set
		double[] predictions = eval.evaluateModel(svmScheme, test);
		List<Double> predictionList = Arrays.asList(ArrayUtils.toObject(predictions));
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		System.out.println(eval.toClassDetailsString());
		System.out.println(eval.toMatrixString());

		return  predictionList;
		
	}

	/**
	 * If you only have a training set and no test you might want to evaluate the classifier by using K times K-fold cross-validation
	 * This method performs a  cross-validation for a classifier on a set of instances using folds
	 * @param data the instances
	 * @throws Exception
	 */
	public static void evaluateKFold(Instances data) throws Exception {
		//K-fold
		System.out.println("\n K-fold evaluation...");
		Evaluation eval = new Evaluation(data);
		LibSVM svmScheme = setupSVM();
		eval.crossValidateModel(svmScheme, data, FOLDS, new Random((int)System.currentTimeMillis()));
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		System.out.println(eval.toClassDetailsString());
		System.out.println(eval.toMatrixString());
	}
	
	private static void saveModel(LibSVM svmScheme) throws Exception {
		SerializationHelper.write(outputPath, svmScheme);
		System.out.println("SVM model saved in " + outputPath);
	}

}
