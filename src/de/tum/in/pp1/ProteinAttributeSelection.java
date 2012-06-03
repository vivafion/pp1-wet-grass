package de.tum.in.pp1;

import java.io.IOException;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;

public class ProteinAttributeSelection {
	
	public static void main(String[] args) {
		
		Instances data;
		try {
			data = TMClassificationUtils.loadDataset();
			data = TMClassificationUtils.resampleDataset(data);
			data = TMClassificationUtils.removeFirstAttribute(data);
			
			
			AttributeSelection wekaattrsel = new AttributeSelection();
			ASEvaluation  eval = new InfoGainAttributeEval();
			ASSearch  search = new Ranker();
			wekaattrsel.setEvaluator(eval);
			wekaattrsel.setSearch(search);
			wekaattrsel.setRanking(true);
			
			System.out.println("\n Ranking protein attributes...");
			wekaattrsel.SelectAttributes(data);
			
			// obtain the attribute indices that were selected
			int[] indices = wekaattrsel.selectedAttributes();
			System.out.println(wekaattrsel.toResultsString());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
