package de.tum.in.pp1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import weka.classifiers.Evaluation;
import weka.core.Instances;

public class WekaTest {

	public static void main(String[] args) {
		Instances data = null;
	 	try {
	 		BufferedReader reader = new BufferedReader(new FileReader("diabetes.arff"));
			data = new Instances(reader);
			reader.close();
			if (data.classIndex() == -1) {
				data.setClassIndex(data.numAttributes() - 1);
			}
			
			 // create new instance of scheme
			weka.classifiers.functions.SMO scheme = new weka.classifiers.functions.SMO();
			 // set options
			 scheme.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
			 	scheme.buildClassifier(data);
			 
			 Evaluation eval = new Evaluation(data);
			 eval.evaluateModel(scheme, data);
			 //eval.crossValidateModel(scheme, data, 10, new Random(1));
			 
			 System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.exit(0);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
