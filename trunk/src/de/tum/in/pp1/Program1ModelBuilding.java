package de.tum.in.pp1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.GridSearch;
import weka.core.Debug.Random;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Program 1: Model Building and Parameter Optimization
 */
public class Program1ModelBuilding {
	
	private static final int AUTO_ATRIB_SELECTION_THRESHOLD = 180;
	private static String trainingSetPath = "";
	//this is the path where the built classifier model is saved
	private static String outputPath = "";
	private static final double SUBSAMPLE_SIZE = 1.0; // percent
	
	//Sets the bias towards a uniform class. A value of 0 leaves the class distribution as-is, 
	//a value of 1 ensures the class distributions are uniform in the output data.
	private static final double BIAS_TO_UNIFORM_CLASS = 1.0;
	private static final int FOLDS = 10;
	private static final double SPLIT_PERCENTAGE = 66.0;
	
	//The sample size (in percent) to use in the initial grid search.
	private static final int GRID_SEARCH_SAMPLE_SIZE = 10;
	
	
/**
 *  Command line params:
 *  1. a path to the training set (fasta files)
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
		
		
		String arffFilePath = PredictProteinRunner.generateARFF(trainingSetPath, true);
		
		Instances data = null;
	 	try {
	 		
	 		//Load the Protein dataset
	 		data = ProteinUtils.loadDataset(arffFilePath, true);
			
	 		
			
	 		//create subsample of the dataset
	 		//data = resampleDataset(data);
	 		
	 		//randomly shuffle the dataset
	 		data = ProteinUtils.randomizeDataset(data);
			
			
			// automatic feature reduction. this method will select attributes and write the attributes that need to be removed in attribute_removed.txt
			filterImportantAttributes(data);
			
	 		data = ProteinUtils.removeNotImportantAttributes(data);
	 		
			System.out.println(data.numAttributes());
	 		long startTime = System.currentTimeMillis();
	 		
			 //create new instance of SVM
	 		AbstractClassifier svmScheme = buildOptimizedSVMUsingGridSearch(data);
			 
	 		long endTime = System.currentTimeMillis();
	 		
	 		System.out.println("Time requred to train SVM: " + (endTime-startTime)/1000.0 +" sec");
	 		 
			//save the model using the output path
			saveModel(svmScheme);
			
			//evaluateKFold(data);
			
			
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
	public static void filterImportantAttributes(Instances data) throws Exception {
		System.out.println("\n Select important attributes...");
		
		//remove first attribute
		String[] options = new String[2];
		options[0] = "-R"; // "range"
		options[1] = "1"; 
		Remove remove = new Remove(); // new instance of filter
		remove.setOptions(options); // set options
		remove.setInputFormat(data); // inform filter about dataset **AFTER**
		data = Filter.useFilter(data, remove); // apply filter
		
		
		int[] ranking1;
		int[] ranking2;
		int[] ranking3;
		int[] ranking4;
		int[] ranking5;
		
		System.out.println("CfsSubsetEval -> generating ranking...");
		AttributeSelection attSelector = new AttributeSelection();
		ASEvaluation eval = new CfsSubsetEval();
		ASSearch search = new GreedyStepwise();
		((GreedyStepwise)search).setGenerateRanking(true);
		attSelector.setEvaluator(eval);
		attSelector.setSearch(search);
		attSelector.SelectAttributes(data);
		ranking1 = attSelector.selectedAttributes();
		
		System.out.println("ChiSquaredAttributeEval -> generating ranking...");
		attSelector = new AttributeSelection();
		eval = new ChiSquaredAttributeEval();
		search = new Ranker();
		((Ranker)search).setGenerateRanking(true);
		attSelector.setEvaluator(eval);
		attSelector.setSearch(search);
		attSelector.SelectAttributes(data);
		ranking2 = attSelector.selectedAttributes();
		
		System.out.println("GainRatioAttributeEval -> generating ranking...");
		attSelector = new AttributeSelection();
		eval = new GainRatioAttributeEval();
		search = new Ranker();
		((Ranker) search).setGenerateRanking(true);
		attSelector.setEvaluator(eval);
		attSelector.setSearch(search);
		attSelector.SelectAttributes(data);
		ranking3 = attSelector.selectedAttributes();
		
		System.out.println("InfoGainAttributeEval -> generating ranking...");
		attSelector = new AttributeSelection();
		eval = new InfoGainAttributeEval();
		search = new Ranker();
		((Ranker) search).setGenerateRanking(true);
		attSelector.setEvaluator(eval);
		attSelector.setSearch(search);
		attSelector.SelectAttributes(data);
		ranking4 = attSelector.selectedAttributes();
		
		System.out.println("ReliefFAttributeEval -> generating ranking...");
		attSelector = new AttributeSelection();
		eval = new ReliefFAttributeEval();
		((ReliefFAttributeEval)eval).setSampleSize(2000);
		search = new Ranker();
		((Ranker) search).setGenerateRanking(true);
		attSelector.setEvaluator(eval);
		attSelector.setSearch(search);
		attSelector.SelectAttributes(data);
		ranking5 = attSelector.selectedAttributes();
		
		Map<Integer,Integer> attributeToScore = new HashMap<Integer, Integer>();
		for (int i = 0; i < data.numAttributes(); i++) {
			Integer currentScore = attributeToScore.get(ranking1[i]);
			if (currentScore == null) {
				currentScore = 0;
			}
			attributeToScore.put(ranking1[i], i + currentScore);
			
			currentScore = attributeToScore.get(ranking2[i]);
			if (currentScore == null) {
				currentScore = 0;
			}
			attributeToScore.put(ranking2[i], i + currentScore);
			
			currentScore = attributeToScore.get(ranking3[i]);
			if (currentScore == null) {
				currentScore = 0;
			}
			attributeToScore.put(ranking3[i], i + currentScore);
			
			currentScore = attributeToScore.get(ranking4[i]);
			if (currentScore == null) {
				currentScore = 0;
			}
			attributeToScore.put(ranking4[i], i + currentScore);
			
			currentScore = attributeToScore.get(ranking5[i]);
			if (currentScore == null) {
				currentScore = 0;
			}
			attributeToScore.put(ranking5[i], i + currentScore);
		}
		attributeToScore.remove(data.classIndex());
		
		FileWriter fstream;
		fstream = new FileWriter("attributes_remove.txt");
		BufferedWriter out = new BufferedWriter(fstream);
		
		int count = 0;
		
		for (Integer attr : attributeToScore.keySet()) {
			Integer score = attributeToScore.get(attr);
			if (score/5.0 > AUTO_ATRIB_SELECTION_THRESHOLD) {
				out.write(data.attribute(attr).name());
				out.newLine();
				count++;
			}
		}
		out.close();
		fstream.close();
		
		//System.out.println("Automatic attribute selection: Selected " + count + " attributes");
	}
	
	/**
	 * Trains an SVM model using the provided training set.
	 * @param data the training instances
	 * @return trained {@link AbstractClassifier}
	 * @throws Exception
	 */
	public static AbstractClassifier buildSVM(Instances data) throws Exception {
		System.out.println("\n Building SVM...");
		//LibSVM svmScheme = setupSVM();
		AbstractClassifier svmScheme = setupLibSVM();
		svmScheme.buildClassifier(data);
		return svmScheme;
	}
	
	/**
	 * Creates an LibSVM with optimized Cost and Gamma parameter for the given training set. It performs a Grid-Search for parameter optimization.
	 * @param instances the training set
	 * @return libsvm
	 */
	public static AbstractClassifier buildOptimizedSVMUsingGridSearch(Instances instances) {
		System.out.println("GridSearch started...");
		GridSearch gridSearch = new GridSearch();
		try {
			gridSearch.setOptions(weka.core.Utils.splitOptions("weka.classifiers.meta.GridSearch -E ACC -y-property classifier.gamma -y-min -15 -y-max 5.0 -y-step 1.0 -y-base 2.0 -y-expression pow(BASE,I) -filter weka.filters.AllFilter -x-property classifier.cost -x-min -15.0 -x-max 15.0 -x-step 1.0 -x-base 2.0 -x-expression pow(BASE,I) -extend-grid -max-grid-extensions 6 -sample-size 10 -traversal COLUMN-WISE -log-file \"grid_search.txt\" -num-slots 1 -S 1 -W weka.classifiers.functions.LibSVM -- -S 0 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.0010 -P 0.1 "));
			gridSearch.setSampleSizePercent(GRID_SEARCH_SAMPLE_SIZE);
			//gridSearch.setClassifier(setupLibSVM());
			gridSearch.buildClassifier(instances);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Classifier  c = gridSearch.getBestClassifier();
		return gridSearch;
	}
	
	/**
	 * Creates new LibSVM and sets the options.
	 * There are a lot of parameters to be adjusted for the SVM!!!
	 * @return SVM that is still not trained
	 * @throws Exception
	 */
	public static LibSVM setupLibSVM() throws Exception{
		LibSVM svmScheme = new weka.classifiers.functions.LibSVM();
		// set options (-W \"0.95 0.05\")
		svmScheme.setOptions(weka.core.Utils.splitOptions("weka.classifiers.functions.LibSVM -S 0 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 4870.0 -E 0.0009765625 -P 0.1"));
		//svmScheme.setProbabilityEstimates(true);
		return svmScheme;
	}
	
	/**
	 * Creates new SMO and sets the options.
	 * There are a lot of parameters to be adjusted for the SVM!!!
	 * @return SVM that is still not trained
	 * @throws Exception
	 */
	public static SMO setupSMO() throws Exception{
		SMO smoScheme = new SMO();
		// set options
		smoScheme.setOptions(weka.core.Utils.splitOptions("weka.classifiers.functions.SMO -C 4870.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 0.0009765625\""));
		smoScheme.setBuildLogisticModels(true);
		return smoScheme;
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
		AbstractClassifier svmScheme = buildSVM(train);
		
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
		LibSVM svmScheme = setupLibSVM();
		eval.crossValidateModel(svmScheme, data, FOLDS, new Random((int)System.currentTimeMillis()));
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		System.out.println(eval.toClassDetailsString());
		System.out.println(eval.toMatrixString());
	}
	
	private static void saveModel(AbstractClassifier svmScheme) throws Exception {
		SerializationHelper.write(outputPath, svmScheme);
		System.out.println("SVM model saved in " + outputPath);
	}

}
