package de.tum.in.pp1;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
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
	 * Removes attributes that are listed in 'attributes_remove.txt' file.
	 * It also removes the first attribute of the dataset, because, during the training or prediction, String atttributes are not used. 
	 * In our case, the first attribute "ID_pos" is string and it is not relevant for the training/testing.
	 * So we must remove it prior the training/testing, otherwise, we get an error.
	 * @param data dataset
	 * @return the dataset without the "ID_pos" column and all columns defined in the 'attributes_remove.txt' file.
	 * @throws Exception
	 */
	public static Instances removeNotImportantAttributes(Instances data) throws Exception {
		int count = 0;
		String[] options = new String[2];
		options[0] = "-R"; // "range"
		options[1] = "1"; 
		FileInputStream fstream = new FileInputStream("attributes_remove.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		String strLine;
		while ((strLine = br.readLine()) != null) {
			Attribute attribute = data.attribute(strLine);
			if (attribute != null) {
				int index = attribute.index();
				options[1] = options[1].concat("," + index);
				count++;
			}
		}
		Remove remove = new Remove(); // new instance of filter
		remove.setOptions(options); // set options
		remove.setInputFormat(data); // inform filter about dataset **AFTER**
										// setting options
		data = Filter.useFilter(data, remove); // apply filter
		System.out.println("Removed:" + count + " attributes");
		return data;
	}
	
	/**
	 * Keeps all attributes that contain any of the keywords listed in 'attributes_selected.txt' file. Other attributes are removed.
	 * It also removes the first attribute of the dataset, because, during the training or prediction, String atttributes are not used. 
	 * In our case, the first attribute "ID_pos" is string and it is not relevant for the training/testing.
	 * So we must remove it prior the training/testing, otherwise, we get an error.
	 * @param data dataset
	 * @return the dataset
	 * @throws Exception
	 */
	public static Instances removeNotImportantAttributesImproved(Instances data) throws Exception {
		
		List<String> selectedAttributeNames = new LinkedList<String>();
		int count = 0;
		String[] options = new String[2];
		options[0] = "-R"; // "range"
		options[1] = "1"; 
		FileInputStream fstream = new FileInputStream("attributes_selected.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		while ((strLine = br.readLine()) != null) {
			selectedAttributeNames.add(strLine);
		}
		
		for (int i = 1; i < data.numAttributes(); i++) {
			Attribute att = data.attribute(i);
			if (!isIncludedInSelectedAttrbiutes(att.name(), selectedAttributeNames)) {
				int index = att.index();
				options[1] = options[1].concat("," + index);
				count++;
			}
		}
		int attNumber = data.numAttributes();
		Remove remove = new Remove(); // new instance of filter
		remove.setOptions(options); // set options
		remove.setInputFormat(data); // inform filter about dataset **AFTER**
		// setting options
		data = Filter.useFilter(data, remove); // apply filter
		System.out.println("Removed:" + count + "of " + attNumber + " attributes");
		
		return data;
	}
	
	private static boolean isIncludedInSelectedAttrbiutes(String testAttName, List<String> selectedAttributeNames) {
		for (String attName : selectedAttributeNames) {
			if (testAttName.contains(attName)) {
				return true;
			}
		}
		return false;
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
	 * @param predictions list of the predicted classes for the testing set: value 0.0 for TM and 1.0 for Non-TM amino acid.
	 * @param idsAndPosition the "ID_pos" attribute, containing the protein id and residue position for each 
	 * @param svmScheme the classifier that might be used for getting the probability estimates for the instances ( svmScheme.distributionForInstance(someInstance) )
	 * prediction given with the first argument respectively 
	 * @return a map containing of key/value pairs where the key is the protein id and the value is a string representing whether or not each AA is TM.
	 */
	public static Map<String, String> postProcessPredictions(List<Double> predictions, Attribute idsAndPosition, double[] classification, AbstractClassifier svmScheme) {
		
		//TODO: this method is not completely implemented!
		Map<String,String> proteins2Class = new LinkedHashMap<String,String>();

		String currentSequence = idsAndPosition.value(0).substring(0, idsAndPosition.value(0).lastIndexOf("_"));
		
		// build prediction string from single position predictions
		for (int i = 0; i < predictions.size(); i++) {
			// is this still the same protein?
			if (!currentSequence.equals(idsAndPosition.value(i).substring(0, idsAndPosition.value(i).lastIndexOf("_")))) {
				//no so change the key, to which the predictions are added
				currentSequence = idsAndPosition.value(i).substring(0, idsAndPosition.value(i).lastIndexOf("_"));
			}
			//line up single predictions into a string
			while (currentSequence.equals(idsAndPosition.value(i).substring(0, idsAndPosition.value(i).lastIndexOf("_")))){
				String tmp = proteins2Class.get(currentSequence);
				// 0.0 is equal to transmembrane, represented as +es in the string
				if (predictions.get(i) == 0.0){
					if (tmp == null){
						proteins2Class.put(currentSequence, "+");
					}
					else{
						proteins2Class.put(currentSequence, tmp + "+");
					}
				}
				// 1.0 is equal to transmembrane, represented as -es in the string
				else if (predictions.get(i) == 1.0){
					if (tmp == null){
						proteins2Class.put(currentSequence, "-");
					}
						else{
						proteins2Class.put(currentSequence, tmp + "-");
					}
				}
				//if not at the end keep moving, else stop, horrible coding style :P
				if (i+1 < idsAndPosition.numValues())
					i++;
				else
					i++;
					break;
			}
			i--;
		}
		
		
//		for (Iterator<String> iterator = proteins2Class.keySet().iterator(); iterator.hasNext();) {
//			String prot = (String) iterator.next();
//			String sequence = proteins2Class.get(prot);
//			//pattern for small gaps in helices (---++++--++++++---) -> connect such small gaps
//			Pattern p = Pattern.compile("(?<=\\+)(\\-{1,4})(\\+)");
//		    Matcher m = p.matcher(sequence);
//		    StringBuffer s = new StringBuffer();
//		    while (m.find()) {
//		    	int gapSize = m.group(1).length();
//		    	String pluses = StringUtils.repeat('+', gapSize + 1);
//		    	m.appendReplacement(s, pluses);
//		    }
//		    m.appendTail(s);
//		    
//		    //pattern for matching predictions with less then 5 consecutive TM residues -> delete such cases
//		    Pattern p1 = Pattern.compile("\\-(\\+{1,4})\\-");
//		    Matcher m1 = p1.matcher(s.toString());
//		    StringBuffer s1 = new StringBuffer();
//		    while (m1.find()) {
//		    	int gapSize = m1.group(1).length();
//		    	String pluses = StringUtils.repeat('-', gapSize + 2);
//		    	m1.appendReplacement(s1, pluses);
//		    }
//		    m1.appendTail(s1);
//		    proteins2Class.put(prot, s1.toString());
//		    //System.out.println(s1.toString());
//		    
//		}
		
		// finds starts and ends for each predicted TM helix
		Map<String,Vector<int[]>> TMpositions = new LinkedHashMap<String,Vector<int[]>>();
		for (String protein : proteins2Class.keySet()){
			Boolean inTM = false;
			TMpositions.put(protein, new Vector<int[]>());
			int[] startEndPair = new int[2];
			//detects starts end ends of consecutive TM predictions
			for (int i = 0; i < proteins2Class.get(protein).length(); i++){
				if (inTM == false){
					if (proteins2Class.get(protein).charAt(i)=='-'){
					}
					else if ( proteins2Class.get(protein).charAt(i)=='+'){
						startEndPair[0] = i;
						inTM = true;
					}
				}
				else{
					if (proteins2Class.get(protein).charAt(i)=='+'){
					}
					else if (proteins2Class.get(protein).charAt(i)=='-'){
						startEndPair[1] = i-1;
						inTM = false;
						//System.out.println ("TM start: "+ startEndPair[0] + " TM end: " + startEndPair[1]);
						TMpositions.get(protein).add( startEndPair.clone() );
					}
				}
					
			}			
		}
		
		// For each predicted TM helix see if you can connect it to a prediction close by
		// then check its size, if its below 10 remove it
		for (String protein : TMpositions.keySet()){
			ListIterator<int[]> itr = TMpositions.get(protein).listIterator();
			while ( itr.hasNext()) {
				int[] startStopPos = itr.next();
				int curStart = startStopPos[0];
				int curEnd = startStopPos[1];
				//look to previous tm part
				if (itr.hasPrevious()){
					itr.previous();
					if (itr.hasPrevious()){
						int[] prev = itr.previous();
						if (curStart - prev[1] <= 2){
							curStart = prev[0];
						}
						itr.next();
					}
					itr.next();
				}
				//look to next tm part
				if (itr.hasNext()){
					int[] next = itr.next();
					if (next[0] - curEnd <= 2){
						curEnd = next[1];
					}
					itr.previous();
				}
				
				// if below 16 residues, throw it out
				if (curEnd - curStart < 4){
					String tmp = proteins2Class.get(protein);
					String start = tmp.substring(0,curStart);
					String mid = "";
					//build up new string with deleted prediction
					for (int j = 0; j <= curEnd - curStart ; j++)
						mid += "-";
					String end = tmp.substring(curEnd+1, tmp.length());
					//System.out.println("Old length: " + tmp.length());
					//System.out.println(tmp);
					//System.out.println(start+mid+end);
					//System.out.println("New lenth:" + (start+mid+end).length());
					proteins2Class.put(protein, start+mid+end);
				}
				//else keep it
				else {
					String tmp = proteins2Class.get(protein);
					String start = tmp.substring(0,curStart);
					String mid = "";
					//build up neu string with combined section
					for (int j = 0; j <= curEnd - curStart ; j++)
						mid += "+";
					String end = tmp.substring(curEnd+1, tmp.length());
					//System.out.println("Old length: " + tmp.length());
					//System.out.println(tmp);
					//System.out.println(start+mid+end);
					//System.out.println("New lenth:" + (start+mid+end).length());
					proteins2Class.put(protein, start+mid+end);
				}
			}		
		}
		
		// do simple evaluation
		int i = 0;
		int true_positives = 0;
		int false_positives = 0;
		int true_negatives = 0;
		int false_negatives = 0;
		
		while (i < classification.length){
			for (String protein : proteins2Class.keySet()){
				char[] predictionArray = proteins2Class.get(protein).toCharArray();
				for ( char c: predictionArray){
					if (classification[i] == 0.0 && c == '+' ){
						true_positives += 1;
					}
					else if (classification[i]== 0.0 && c == '-'){
						false_negatives += 1;
					}
					else if (classification[i]== 1.0 && c == '-'){
						true_negatives += 1;
					}
					else if (classification[i]== 1.0 && c == '+'){
						false_positives += 1;
					}
					i++;
				}
			}
		}
		System.out.println("True TM = " + true_positives);
		System.out.println("False TM = " + false_positives);
		System.out.println("True nonTM = " + true_negatives);
		System.out.println("False nonTM = " + false_negatives);

		System.out.println("Q2=TM Precision = " +(float)true_positives/(true_positives + false_positives));		
		System.out.println("non TM Precision = " +(float)true_negatives/(true_negatives + false_negatives));
		
		System.out.println("Sensitivity=Recall = " +(float)true_positives/(true_positives + false_negatives));
		System.out.println("Specificity = " +(float)true_negatives/(true_negatives + false_positives));
		
		System.out.println("Correctly Classified Instances = " +(float)(true_positives + true_negatives)/(true_negatives + false_positives + true_positives + false_negatives));

		return proteins2Class;
	}
	
	public static float calculateQok(Map<String, String> predicted, Map<String, String> trueproteins) {
		float correctNumberOfHelicesInProtein = 0;
		float tmProteins = 0;
		for (Iterator<String> iterator = predicted.keySet().iterator(); iterator.hasNext();) {
			String protein = (String) iterator.next();
			String sequencePred = predicted.get(protein);
			String sequenceTrue = trueproteins.get(protein);
			int helicesPred =  ("-" + sequencePred + '-').split("\\-+").length;
			int helicesTrue =  ("-" + sequenceTrue + '-').split("\\-+").length;
			if (helicesTrue > 0) {
				//TM protein
				tmProteins++;
				if (helicesPred == helicesTrue) {
					correctNumberOfHelicesInProtein++;
				}
			}
		}
		
		return correctNumberOfHelicesInProtein/tmProteins;
		
	}
	
	public static Map<String, String> getTestsetAsMap(Instances testset) {
		Map<String, String> protein2Map = new HashMap<String, String>();
		Attribute idsAndPosition = testset.attribute(0);
		String currentSequence = idsAndPosition.value(0).substring(0, idsAndPosition.value(0).lastIndexOf("_"));
		protein2Map.put(currentSequence, "");
		for (int i = 0; i < testset.numInstances(); i++) {
			if (!currentSequence.equals(idsAndPosition.value(i).substring(0, idsAndPosition.value(i).lastIndexOf("_")))) {
				currentSequence = idsAndPosition.value(i).substring(0, idsAndPosition.value(i).lastIndexOf("_"));
				protein2Map.put(currentSequence, "");
			}
			String tmp = protein2Map.get(currentSequence);
			protein2Map.put(currentSequence, tmp + testset.instance(i).stringValue(testset.numAttributes() - 1));
		}
		
		return protein2Map;
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
	
	
	public static int[] getPercentageResidueAccuracyPerProtein(List<Double> predictions, 
			Attribute idsAndPosition, 
			double[] classification, 
			Map<String,String> proteins2Class) {
		
		int[] histogram = new int[21];
		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = 0;
		}
		
		for (Iterator<String> iter = proteins2Class.keySet().iterator(); iter.hasNext();) {
			float numberOfHits = 0;
			String protein = (String) iter.next();
			char[] sequence = proteins2Class.get(protein).toCharArray();
			for (int i = 0; i < sequence.length; i++) {
				int indexOfInstance = idsAndPosition.indexOfValue(protein + "_" + i);
				if (classification[indexOfInstance] == predictions.get(indexOfInstance)) {
					numberOfHits++;
				}
			}
			float predictionAccuracyOfProtein = numberOfHits / sequence.length;
			int indexInHistogram = (int) (predictionAccuracyOfProtein * 100 / 5);
			histogram[indexInHistogram]++;
		}
		
		System.out.println("Histogram (Y:Number of proteins, X:Percentage correctly predicted residues per protein) : ");
		for (int i = 0; i < histogram.length; i++) {
			System.out.println(i*5+"%-" + (i*5+5) + "% : " + histogram[i]);
		}
		return histogram;
	}
}
