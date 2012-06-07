package de.tum.in.pp1;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.Randomize;
/**
 * Class containing common utility methods used in several other classes.
 *
 */
public class ProteinUtils {
	
	/**
	 * Load dataset
	 * @param datasetPath
	 * @param containsClassLabel true if there is a class attribute (training set) or false when the class label is not provided (testing set)
	 * @return
	 */
	public static Instances loadDataset(String datasetPath, boolean containsClassLabel) throws IOException {
		System.out.println("\n Loading dataset...");
		Instances data;
		ArffLoader loader = new ArffLoader();
		loader.setFile(new File(datasetPath));
		data = loader.getDataSet();
		
		if (containsClassLabel) {
			if (data.classIndex() == -1) {
				data.setClassIndex(data.numAttributes() - 1);
			}
		}
		return data;
	}
	
	/**
	 * During the training or prediction, String atttributes are not used. 
	 * In our case, the first attribute "ID_pos" is string and it is not relevant for the training/testing.
	 * So we must remove it prior the training/testing, otherwise, we get an error.
	 * @param data dataset
	 * @return the dataset without the "ID_pos" column
	 * @throws Exception
	 */
	public static Instances removeFirstAttribute(Instances data) throws Exception {
		System.out.println("\n Remove first attribute...");
		
		if (data.attribute("ID_pos") == null) {
			// ID_pos attribute already removed.
			return data;
		}
		String[] options = new String[2];
		 options[0] = "-R";                                    // "range"
		 options[1] = "1";                                     // first attribute
		 Remove remove = new Remove();                         // new instance of filter
		 remove.setOptions(options);                           // set options
		 remove.setInputFormat(data);                          // inform filter about dataset **AFTER** setting options
		 data = Filter.useFilter(data, remove);   // apply filter
		 return data;
	}
	
	/**
	 * Randomly shuffles the order of instances passed through it. 
	 * @param data the dataset to be shuffled
	 * @return shuffled dataset
	 * @throws Exception
	 */
	public static Instances randomizeDataset(Instances data) throws Exception {
		System.out.println("\n Shuffle dataset...");
		Randomize sampler = new Randomize();
		sampler.setRandomSeed((int)System.currentTimeMillis());
		sampler.setInputFormat(data);
		data = Resample.useFilter(data, sampler);
		return data;
	}

	/**
	 * This method should evaluate the predictions of the aminoacids and do some post-processing to confirm that the protein is TM
	 * For example, it might check whether there are 17 consecutive TM amino-acids ??? 
	 * @param predictions list of the predicted classes for the testing set: value 1.0 for TM and 0.1 for Non-TM amino acid.
	 * @param idsAndPosition the "ID_pos" attribute, containing the protein id and residue position for each 
	 * prediction given with the first argument respectively 
	 * @return a map containing of key/value pairs where the key is the protein id and the value is the class (TM or non-TM).
	 */
	public static Map<String, String> evaluatePredictions(List<Double> predictions, Attribute idsAndPosition) {
		//TODO: this method is not completely implemented!
		Map<String,String> proteins2Class = new LinkedHashMap<String,String>();
		int pos = 0;
		for (Double prediction : predictions) {
			proteins2Class.put(idsAndPosition.value(pos), prediction.toString());
			pos++;
		}
		return proteins2Class;
	}
	
	/**
	 * Resamples a dataset by applying the Synthetic Minority Oversampling TEchnique (SMOTE).
	 * @param dataset dataset
	 * @param percentage The percentage of SMOTE instances to create.
	 * @param classValue The index of the class value to which SMOTE should be applied. Use a value of 0 to auto-detect the non-empty minority class.
	 * @param nearestNeighbors The number of nearest neighbors to use.
	 * @return resampled dataset
	 * @throws Exception
	 */
	public static Instances smoteDataset(Instances dataset, double percentage, String classValue, int nearestNeighbors) throws Exception {
		System.out.println("\n SMOTE dataset...");
		SMOTE smote = new SMOTE();
		smote.setClassValue("0");
		smote.setNearestNeighbors(5);
		smote.setPercentage(percentage);
		smote.setRandomSeed((int)System.currentTimeMillis());
		dataset = Resample.useFilter(dataset, smote);
		return dataset;
	}
}
