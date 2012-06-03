package de.tum.in.pp1;

import java.io.File;
import java.io.IOException;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.core.Debug.Random;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.Randomize;

public class TMClassificationUtils {
	
	private static final String arffFilePath = "C:\\Users\\Atanasko\\Dropbox\\pp1_wet_grass\\dataset\\allFiles.arff";
	private static final int FOLDS = 2;
	private static final double SPLIT_PERCENTAGE = 66.0;
	private static final double SUBSAMPLE_SIZE = 10.0; // percent
	
	public static void main(String[] args) {
		Instances data = null;
	 	try {
	 		
	 		//Load the Protein dataset
	 		data = loadDataset();
			
			//filtering
	 		//we don't need the first string attribute
	 		data = removeFirstAttribute(data);
			
	 		//create subsample of the dataset
	 		//data = resampleDataset(data);
	 		
	 		//randomly shuffle the dataset
			data = randomizeDataset(data);
			
			
			// feature reduction
			//data = filterImportantAttributes(data);
			
			
			 //create new instance of SVM
			weka.classifiers.functions.SMO svmScheme = buildSVM(data);
			 
		 	//Evaluate
			evaluatePercentageSplit(data, svmScheme);
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.exit(0);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Instances loadDataset() throws IOException {
		System.out.println("\n Loading dataset...");
		Instances data;
		ArffLoader loader = new ArffLoader();
		//don't forget to set your path to the arff file here!
		loader.setFile(new File(arffFilePath));
		data = loader.getDataSet();
		if (data.classIndex() == -1) {
			data.setClassIndex(data.numAttributes() - 1);
		}
		return data;
	}

	public static Instances resampleDataset(Instances data) throws Exception {
		System.out.println("\n Resample dataset...");
		Resample sampler = new Resample();
		sampler.setRandomSeed((int)System.currentTimeMillis());
		sampler.setSampleSizePercent(SUBSAMPLE_SIZE);
		sampler.setBiasToUniformClass(0.0);
		sampler.setNoReplacement(true);
		sampler.setInputFormat(data);
		data = Resample.useFilter(data, sampler);
		return data;
	}
	
	public static Instances randomizeDataset(Instances data) throws Exception {
		System.out.println("\n Shuffle dataset...");
		Randomize sampler = new Randomize();
		sampler.setRandomSeed((int)System.currentTimeMillis());
		sampler.setInputFormat(data);
		data = Resample.useFilter(data, sampler);
		return data;
	}
	
	public static Instances filterImportantAttributes(Instances data) throws Exception {
		System.out.println("\n Select important attributes...");
		AttributeSelection filter = new AttributeSelection();
		  CfsSubsetEval eval = new CfsSubsetEval();
		  GreedyStepwise search = new GreedyStepwise();

		filter.setEvaluator(eval);
		filter.setSearch(search);
		filter.setInputFormat(data);

		// generate new data
		Instances newData = Filter.useFilter(data, filter);
		System.out.println("\n Number of selected attributes: " + newData.numAttributes()); 
		return newData;
	}
	
	public static Instances removeFirstAttribute(Instances data) throws Exception {
		System.out.println("\n Remove first attribute...");
		String[] options = new String[2];
		 options[0] = "-R";                                    // "range"
		 options[1] = "1";                                     // first attribute
		 Remove remove = new Remove();                         // new instance of filter
		 remove.setOptions(options);                           // set options
		 remove.setInputFormat(data);                          // inform filter about dataset **AFTER** setting options
		 data = Filter.useFilter(data, remove);   // apply filter
		 return data;
	}

	public static weka.classifiers.functions.SMO buildSVM(Instances data) throws Exception {
		System.out.println("\n Building SVM...");
		weka.classifiers.functions.SMO svmScheme = new weka.classifiers.functions.SMO();
		 // set options
		svmScheme.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));

		svmScheme.buildClassifier(data);
		return svmScheme;
	}

	public static void evaluateKFold(Instances data, weka.classifiers.functions.SMO svmScheme) throws Exception {
		//K-fold
		System.out.println("\n K-fold evaluation...");
		Evaluation eval = new Evaluation(data);
		eval.evaluateModel(svmScheme, data);
		eval.crossValidateModel(svmScheme, data, FOLDS, new Random((int)System.currentTimeMillis()));
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		 
	}
	
	public static void evaluatePercentageSplit(Instances data, weka.classifiers.functions.SMO svmScheme) throws Exception {
		//percentage split
		 System.out.println("\n Percentage Split Evaluation...");
		 double percent = SPLIT_PERCENTAGE; 
		 int trainSize = (int) Math.round(data.numInstances() * percent / 100); 
		 int testSize = data.numInstances() - trainSize; 
		 Instances train = new Instances(data, 0, trainSize); 
		 Instances test = new Instances(data, trainSize, testSize); 
		 
		 Evaluation eval = new Evaluation(train);
		 eval.evaluateModel(svmScheme, test);
		 System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		
	}
}
