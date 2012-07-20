/**
 *@purpose : GeneratePredictMeArff.java is used to execute the scripts from command mode. 
 * */
//package in.tum.de;
package de.tum.in.pp1;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;


/**
 * This file calls the scripts to generate the arff files.
 * */
public class PredictProteinRunner {
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Missing arguments. Proper Usage is: java -jar WetGrassScriptCall.jar [propertyFilePath]");
	        System.exit(0);
		}
		String pathOfDataset=args[0];
		System.out.println("Creating Training arff");
		
		generateARFF(pathOfDataset, true);
		System.out.println("Now creating testing arff (overwrite)");
		
		
		generateARFF(pathOfDataset, false);
		System.out.println("Finished");

		
	}
		
	
	
		public static void mainBU(String[] args){
		
		
		
		String propertyFilePath = "";
//		String path1 = "";
//		String path2 = "";
//		String path3 = "";
//		String path4 = "";
//		String path5 = "";
//		String path6 = "";
		String datasetPath = "";
//		String datasetFastaPath = "";
		String predictProteinOutput = "";
//		String impOrSolFlag = "";
//		String trainingOrTestsetFlag = "";
		String outputArffPath = "";
		String PP2FeatureOutputFileName = "";
//		String PP2FeatureOutputFileNameTestset = "";
//		String testsetFastaPath = "";
//		String predictProteinTestsetOutput = "";
//		String testsetPath = "";
//		String testsetOutputArffPath = "";
		String callPPFlag = "";
		String isTraining = "";
		String strucFolder = "";
		String pp2features="";
		Properties prop = new Properties();		
		//path = "/mnt/opt/data/pp1_12_exercise/groups/wet_grass/executeScript.sh";
	    
		//check the command line parameters
		if (args.length < 1) {
			System.out.println("Missing arguments. Proper Usage is: java -jar WetGrassScriptCall.jar [propertyFilePath]");
	        System.exit(0);
		}
		if (args.length < 2) {
			System.out.println("Missing arguments. Proper Usage is: java -jar WetGrassScriptCall.jar [TestsetPath] [Modeloutput]");
	        System.exit(0);
		}
	 
		try {
			/** propertyFilePath = path of the paths.property file provided as argument from command line*/
			propertyFilePath = args[0];
			propertyFilePath = "paths.properties";
			datasetPath = args[0];
			String outputName= args[1];
			
			
			
			
            //load a properties file
			prop.load(new FileInputStream(propertyFilePath));
			
			//get the property value
//			path1 = prop.getProperty("script1Path");//path of callPredictProtein.sh
//			path2 = prop.getProperty("script2Path");//path of parseTMnonTM.pl
			pp2features = prop.getProperty("pp2features");//path of pp2features.py
//			datasetPath = prop.getProperty("datasetPath");//path of dataset
//			datasetFastaPath = prop.getProperty("datasetFastaPath");//path of dataset till fasta folder. arg[0] for callPredictProtein.sh
			predictProteinOutput = prop.getProperty("predictProteinOutput");//path of the output files from PredictProtein, arg[1] for callPredictProtein.sh
//			impOrSolFlag = prop.getProperty("impOrSolFlag");//value of arg[2] for callPredictProtein.sh
//			trainingOrTestsetFlag = prop.getProperty("trainingOrTestsetFlag");//1 to generate arff for trainingset and 0 to generate arff for testset
			outputArffPath = prop.getProperty("outputArffPath");//path of where the arff file will be generated ; arg[2] while calling parseTMnonTM.pl and
			callPPFlag = prop.getProperty("callPPFlag");// true if PredictProtein script is called else false
			PP2FeatureOutputFileName = prop.getProperty("PP2FeatureOutputFileName");//file name of training arff file

			//for testing
//			testsetFastaPath = prop.getProperty("testsetFastaPath");//path of fasta files for testing, arg[0] for callPredictProtein.sh
//			predictProteinTestsetOutput = prop.getProperty("predictProteinTestsetOutput");//output path for predictProtein ;arg[1] for callPredictProtein.sh
//			testsetPath = prop.getProperty("testsetPath");//path of test dataset 
//			testsetOutputArffPath = prop.getProperty("testsetOutputArffPath");//path of where the arff file will be generated for testset; arg[2] while calling parseTMnonTM.pl 
			isTraining = prop.getProperty("isTraining", "true");
			
			
			
			boolean boolTrainSet=isTraining.equalsIgnoreCase("true");
			boolean boolCallPP=callPPFlag.equalsIgnoreCase("true");
//			boolean boolUseImpSolFiles=impOrSolFlag.equalsIgnoreCase("1");
			boolean boolUseImpSolFiles=false;		
			
			
			
			//run scripts to get the training set arff file
		//	if(boolTrainSet){
				if(boolCallPP){
					System.out.println("PredicProtein is called...");
						
					//call script1: callPredictProtein.sh to run PredictProtein
//					path1 = path1 +" "+ datasetFastaPath+" "+ predictProteinOutput+ " "+impOrSolFlag;
					//Runtime.getRuntime().exec(path1);
//					executeScript(path1);
					callPP(datasetPath, boolTrainSet, predictProteinOutput);
					
					System.out.println("PredicProtein calculations done.");
				}
				
				//call script2: parseTMnonTM.pl to generate Prot files
//				path2 = "perl "+path2+" " +trainingOrTestsetFlag+ " " +predictProteinOutput+" "+datasetPath;
				//Runtime.getRuntime().exec(path2);
//				executeScript(path2);
				System.out.println("Creating TMState .arff file(s)...");
				parseTMnonTM(predictProteinOutput, datasetPath, boolTrainSet);
				System.out.println("TMState .arff file(s) created.");
				
				//call script3: pp2features.py to run PP2Features and generate output arff file
				//path3 = "python " +path3 +" -a prot.arff --arff-file "+PP2FeatureOutputFileName+" -p "+outputArffPath+" -f sampleConfig.cfg -e error.txt";
				System.out.println("Beginning to call pp2features to merge all data...");
				String pp2featuresCall = "python " +pp2features +" -a prot.arff --arff-file "+outputArffPath+PP2FeatureOutputFileName+" -p "+predictProteinOutput+" -f sampleConfig.cfg -e error.txt";
				executeScript(pp2featuresCall);
				
				System.out.println(".arff file created.");
				
				System.out.println("Please call \"Program1 "+outputArffPath+PP2FeatureOutputFileName+" "+outputName);
		    /* } else{	
				//run scripts to get the testing set arff file
		    	if(boolCallPP){
			    	 //call script1: callPredictProtein.sh to run PredictProtein
//					path4 = path1 +" "+ testsetFastaPath+" "+ predictProteinTestsetOutput+ " "+impOrSolFlag;
					callPP(testsetFastaPath, boolUseImpSolFiles, predictProteinTestsetOutput);
//					executeScript(path4);
					
					System.out.println("script1 executed for testing set...");
		    	}
		    	
		    	
					
				//call script2: parseTMnonTM.pl to generate Prot files
//				path5 = "perl "+path2+" " +trainingOrTestsetFlag+ " " +predictProteinTestsetOutput +" " +testsetPath;
//				executeScript(path5);
		    	
		    	parseTMnonTM(predictProteinTestsetOutput, testsetPath, boolTrainSet);
				System.out.println("script2 executed for testing set...");
				
				//call script3: pp2features.py to run PP2Features and generate output arff file
				path6 = "python " +path3 +" -a prot.arff --arff-file "+testsetOutputArffPath+PP2FeatureOutputFileName+" -p "+predictProteinTestsetOutput+" -f sampleConfig.cfg -e error.txt";
				executeScript(path6);
				System.out.println("script3 executed for testset...");
				System.out.println("....testingset output arff is generated...");
		     }
			*/
 	     } catch (IOException ex) {
 	    	 ex.printStackTrace();
 	     }
	

	}

	
	public static String generateARFF(String pathOfDataset, boolean isTrain){
		String propertyFilePath = "";
//		String path1 = "";
//		String path2 = "";
//		String path3 = "";
//		String path4 = "";
//		String path5 = "";
//		String path6 = "";
		String datasetPath = "";
//		String datasetFastaPath = "";
		String predictProteinOutput = "";
//		String impOrSolFlag = "";
//		String trainingOrTestsetFlag = "";
		String outputArffPath = "";
		String PP2FeatureOutputFileName = "";
//		String PP2FeatureOutputFileNameTestset = "";
//		String testsetFastaPath = "";
//		String predictProteinTestsetOutput = "";
//		String testsetPath = "";
//		String testsetOutputArffPath = "";
		String callPPFlag = "";
		String isTraining = "";
		String strucFolder = "";
		String pp2features="";
		Properties prop = new Properties();		
		//path = "/mnt/opt/data/pp1_12_exercise/groups/wet_grass/executeScript.sh";
	    
		//check the command line parameters
		
		
	 
		try {
			/** propertyFilePath = path of the paths.property file provided as argument from command line*/
			
			propertyFilePath = "paths.properties";
			datasetPath = pathOfDataset;
			String outputName= "MISSING";
			
			
			
			
            //load a properties file
			prop.load(new FileInputStream(propertyFilePath));
			
			//get the property value
//			path1 = prop.getProperty("script1Path");//path of callPredictProtein.sh
//			path2 = prop.getProperty("script2Path");//path of parseTMnonTM.pl
			pp2features = prop.getProperty("pp2features");//path of pp2features.py
//			datasetPath = prop.getProperty("datasetPath");//path of dataset
//			datasetFastaPath = prop.getProperty("datasetFastaPath");//path of dataset till fasta folder. arg[0] for callPredictProtein.sh
			predictProteinOutput = prop.getProperty("predictProteinOutput");//path of the output files from PredictProtein, arg[1] for callPredictProtein.sh
//			impOrSolFlag = prop.getProperty("impOrSolFlag");//value of arg[2] for callPredictProtein.sh
//			trainingOrTestsetFlag = prop.getProperty("trainingOrTestsetFlag");//1 to generate arff for trainingset and 0 to generate arff for testset
			outputArffPath = prop.getProperty("outputArffPath");//path of where the arff file will be generated ; arg[2] while calling parseTMnonTM.pl and
			callPPFlag = prop.getProperty("callPPFlag");// true if PredictProtein script is called else false
			PP2FeatureOutputFileName = prop.getProperty("PP2FeatureOutputFileName");//file name of training arff file

			//for testing
//			testsetFastaPath = prop.getProperty("testsetFastaPath");//path of fasta files for testing, arg[0] for callPredictProtein.sh
//			predictProteinTestsetOutput = prop.getProperty("predictProteinTestsetOutput");//output path for predictProtein ;arg[1] for callPredictProtein.sh
//			testsetPath = prop.getProperty("testsetPath");//path of test dataset 
//			testsetOutputArffPath = prop.getProperty("testsetOutputArffPath");//path of where the arff file will be generated for testset; arg[2] while calling parseTMnonTM.pl 
			isTraining = prop.getProperty("isTraining", "true");
			
			
			
			boolean boolTrainSet=isTrain;
			boolean boolCallPP=callPPFlag.equalsIgnoreCase("true");
//			boolean boolUseImpSolFiles=impOrSolFlag.equalsIgnoreCase("1");
			boolean boolUseImpSolFiles=false;		
			
			
			
			//run scripts to get the training set arff file
		//	if(boolTrainSet){
				if(boolCallPP){
					System.out.println("PredicProtein is called...");
						
					//call script1: callPredictProtein.sh to run PredictProtein
//					path1 = path1 +" "+ datasetFastaPath+" "+ predictProteinOutput+ " "+impOrSolFlag;
					//Runtime.getRuntime().exec(path1);
//					executeScript(path1);
					callPP(datasetPath, boolTrainSet, predictProteinOutput);
					
					System.out.println("PredicProtein calculations done.");
				}
				
				//call script2: parseTMnonTM.pl to generate Prot files
//				path2 = "perl "+path2+" " +trainingOrTestsetFlag+ " " +predictProteinOutput+" "+datasetPath;
				//Runtime.getRuntime().exec(path2);
//				executeScript(path2);
				System.out.println("Creating TMState .arff file(s)...");
				parseTMnonTM(predictProteinOutput, datasetPath, boolTrainSet);
				System.out.println("TMState .arff file(s) created.");
				
				//call script3: pp2features.py to run PP2Features and generate output arff file
				//path3 = "python " +path3 +" -a prot.arff --arff-file "+PP2FeatureOutputFileName+" -p "+outputArffPath+" -f sampleConfig.cfg -e error.txt";
				System.out.println("Beginning to call pp2features to merge all data...");
				String pp2featuresCall = "python " +pp2features +" -a prot.arff --arff-file "+outputArffPath+PP2FeatureOutputFileName+" -p "+predictProteinOutput+" -f sampleConfig.cfg -e error.txt";
				executeScript(pp2featuresCall);
				
				System.out.println(".arff file created.");
				
				System.out.println("Please call \"Program1 "+outputArffPath+PP2FeatureOutputFileName+" "+outputName);
				
				
				
				
		    /* } else{	
				//run scripts to get the testing set arff file
		    	if(boolCallPP){
			    	 //call script1: callPredictProtein.sh to run PredictProtein
//					path4 = path1 +" "+ testsetFastaPath+" "+ predictProteinTestsetOutput+ " "+impOrSolFlag;
					callPP(testsetFastaPath, boolUseImpSolFiles, predictProteinTestsetOutput);
//					executeScript(path4);
					
					System.out.println("script1 executed for testing set...");
		    	}
		    	
		    	
					
				//call script2: parseTMnonTM.pl to generate Prot files
//				path5 = "perl "+path2+" " +trainingOrTestsetFlag+ " " +predictProteinTestsetOutput +" " +testsetPath;
//				executeScript(path5);
		    	
		    	parseTMnonTM(predictProteinTestsetOutput, testsetPath, boolTrainSet);
				System.out.println("script2 executed for testing set...");
				
				//call script3: pp2features.py to run PP2Features and generate output arff file
				path6 = "python " +path3 +" -a prot.arff --arff-file "+testsetOutputArffPath+PP2FeatureOutputFileName+" -p "+predictProteinTestsetOutput+" -f sampleConfig.cfg -e error.txt";
				executeScript(path6);
				System.out.println("script3 executed for testset...");
				System.out.println("....testingset output arff is generated...");
		     }
			*/
 	     } catch (IOException ex) {
 	    	 ex.printStackTrace();
 	     }
		String returnValue=outputArffPath+PP2FeatureOutputFileName;
		return returnValue;
	}
	
	
	
	
	
	
	
	
	/**Method to execute Bash Script*/
	private static void executeScript(String path) {
        try {
            String cmd = "";
            cmd = path;
            // create a process for the shell
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
            pb.redirectErrorStream(true); // use this to capture messages sent to stderr
            Process shell;
            shell = pb.start();
            InputStream shellIn = shell.getInputStream(); // this captures the output from the command
            int shellExitStatus = shell.waitFor(); // wait for the shell to finish and get the return code
            int c;
            while ((c = shellIn.read()) != -1) {System.out.write(c);}
            // close the stream
            shellIn.close();         
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}



	
	private static void callPP(String pathWithAllFiles, boolean useImpSol, String outputPath){
		
		if (useImpSol){
			String impPath=pathWithAllFiles+"impFasta/";
			String solPath=pathWithAllFiles+"solFasta/";
			callPPInner(impPath, outputPath);
			callPPInner(solPath, outputPath);
			
		} else {
			callPPInner(pathWithAllFiles, outputPath);
		}
		
		
		
		
//		File[] files = new File(pathWithAllFiles).listFiles();
//
//		for (File file : files) {
//	        if (file.isFile()) {
//	            System.out.println("File found: " + file.getName());
//	            
//	            
//	            File folder = new File(outputPath+file.getName());
//	            try{
//		            if(folder.mkdir()){
//		            System.out.println("Directory Created");
//		            }else{
//		            System.out.println("Directory creation failed");}
//	            }catch(Exception e){
//	            	e.printStackTrace();
//	            } 
//	        } else {
//	            System.out.println("File: " + file.getName());
//	        }
//	    }
		
	}
	
	private static void callPPInner(String path, String outputPath){
		File[] files = new File(path).listFiles();
//		System.out.println(path);
		
		for (File file : files) {
	        if (file.isFile()) {
	        	String fileName=file.getName();
	            
	            if (file.getName().endsWith(".fasta")&&file.isFile()){
	            	System.out.println("Fasta file found: "+path+fileName+", calling PP");
		            
	            	String fileNameWithoutEnding=fileName.substring(0, fileName.length()-6);
		            File folder = new File(outputPath+fileNameWithoutEnding);
		            
		            try{
			            if(folder.mkdir()){
			            System.out.println("Directory Created");
			            }else{
			            System.out.println("Directory creation failed");}
		            }catch(Exception e){
		            	e.printStackTrace();
		            } 

		            executeScript("/usr/bin/predictprotein --seqfile "+path+fileName+" --target=all --target=optional --output-dir "+outputPath+fileNameWithoutEnding+"/"+" --nouse-cache");
//	        --bigblastdb=/var/tmp/rost_db/data/big/big --big80blastdb=/var/tmp/rost_db/data/big/big_80 --pfam2db=/var/tmp/rost_db/data/pfam_legacy/Pfam_ls --pfam3db=/var/tmp/rost_db/data/pfam/Pfam-A.hmm --prositeconvdat=/var/tmp/rost_db/data/prosite/prosite_convert.dat --prositedat=/var/tmp/rost_db/data/prosite/prosite.dat --swissblastdb=/var/tmp/rost_db/data/swissprot/uniprot_sprot"
	            }
	        }
	    }
	}
	
	private static void parseTMnonTM(String folderWithSubfolders,String dataSetFolder, boolean trainSet){
	
		File[] files = new File(folderWithSubfolders).listFiles();

		String strucFolder=dataSetFolder+"impStructure/";
		
		for (File folder : files) {
	        if (folder.isDirectory()) {
	        	String folderName=folder.getName();
	            System.out.println("Subfolder found: " + folderName);
	            
	            
	            
	            
	            try{
	            	  // Open the file that is the first 
	            	  // command line parameter
	            	
	            	
	            	  FileInputStream fastaFile = new FileInputStream(folderWithSubfolders+folderName+"/query.fasta");
	            	  // Get the object of DataInputStream
	            	  DataInputStream in = new DataInputStream(fastaFile);
	            	  BufferedReader br = new BufferedReader(new InputStreamReader(in));
	            	  String strLine;
	            	  //Read File Line By Line
	            	  String realTMState="";
	            	  String currentLine="";
	            	  boolean foundStruc=false;
	            	  if (trainSet){
	            		  
//	            		  System.out.println("Testing for structure: "+strucFolder+folder.getName()+".fasta");
	            		  File tester=new File(strucFolder+folder.getName()+".fasta");
	            		  if (tester.exists()){
	            			  System.out.println("Structure exists, reading structure from file"+strucFolder+folder.getName()+".fasta");
	            			  FileInputStream realTMStateFile = new FileInputStream(strucFolder+folder.getName()+".fasta");
			            	  // Get the object of DataInputStream
			            	  DataInputStream inRealTM = new DataInputStream(realTMStateFile);
			            	  BufferedReader brReal = new BufferedReader(new InputStreamReader(inRealTM));
			            	  currentLine= brReal.readLine();
//			            	  System.out.println("Line 1: "+currentLine);
			            	  currentLine= brReal.readLine();
//			            	  System.out.println("Line 2: "+currentLine);
			            	  currentLine= brReal.readLine(); 
//			            	  System.out.println("Line 3: "+currentLine);
			            	  currentLine= brReal.readLine(); 
//			            	  System.out.println("Line 4: "+currentLine);
			            	  foundStruc=true;
	            		  }
	            		  
//		            	  currentLine.replace('H', '+');
//		            	  currentLine.
	            		  
	            	  }
	            	  
	            	  FileWriter writeProtARFF = new FileWriter(folderWithSubfolders+folderName+"/prot.arff");
	            	  BufferedWriter out = new BufferedWriter(writeProtARFF);
	            	  
	            	  String sequence="";
	            	  
	            	  while ((strLine = br.readLine()) != null)   {
	            	  // Print the content on the console
	            		  if (strLine.startsWith(">")){
	            			  out.write( "%Created by group wet_grass\n");
	            			  out.write("@RELATION\t'prot.arff'\n\n");
	            			  out.write("@ATTRIBUTE\tpos\tNUMERIC\n");
	            			  out.write( "@ATTRIBUTE\tclass\t{+,-}\n\n");
	            			  out.write( "@DATA\n");
	            		  } else {
	            			  strLine=strLine.replaceAll("\\s+", "");
	            			  sequence=sequence+strLine;
	            			  
	            		  }
	            		  
//	            	  System.out.println (strLine);
	            	  }
	            	  in.close();
//	            	  System.out.println("Complete Sequence was: "+sequence);
	            	  for (int i=0; i<sequence.length();i++){
	            		  if(!trainSet){
	            			  out.write(i+",?\n");
	            		  } else {
	            			  if (foundStruc){
		            			  if (currentLine.charAt(i)=='H'||currentLine.charAt(i)=='L'){
		            				  out.write(i+",+\n");
		            			  } else {
		            				  out.write(i+",-\n");
		            			  }
	            			  } else {
	            				  out.write(i+",-\n");
	            			  }
	            		  }
	            	  }
	            	  //Close the input stream
	            	  
	            	  out.close();
	            	    }catch (Exception e){//Catch exception if any
	            	  System.err.println("Error: " + e.getMessage());
	            	  }
	            
	        }
	            
	           
	    }
		
	
	}
}
