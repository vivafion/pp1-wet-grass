/**
 *@purpose : GeneratePredictMeArff.java is used to execute the scripts from command mode. 
 * */
package de.tum.in.pp1;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This file calls the scripts to generate the arff files.
 * */
public class GeneratePredictMeArff {
	
	public static void main(String[] args) {
		
		String propertyFilePath = "";
		String path1 = "";
		String path2 = "";
		String path3 = "";
		String path4 = "";
		String path5 = "";
		String path6 = "";
		String datasetPath = "";
		String datasetFastaPath = "";
		String predictProteinOutput = "";
		String impOrSolFlag = "";
		String trainingOrTestsetFlag = "";
		String outputArffPath = ""; 
    String PP2FeatureOutputFileName = "";
		String PP2FeatureOutputFileNameTestset = "";  
		String testsetFastaPath = "";
		String predictProteinTestsetOutput = "";
		String testsetPath = "";
		String testsetOutputArffPath = "";
		String callPPFlag = "";
		Properties prop = new Properties();		
		//path = "/mnt/opt/data/pp1_12_exercise/groups/wet_grass/executeScript.sh";
	    
		//check the command line parameters
		if (args.length < 1) {
			System.out.println("Missing arguments. Proper Usage is: java -jar WetGrassScriptCall.jar [propertyFilePath]");
	        System.exit(0);
		}
		
	 
		try {
			/** propertyFilePath = path of the paths.property file provided as argument from command line*/
			propertyFilePath = args[0];
		
      //load a properties file
			prop.load(new FileInputStream(propertyFilePath));
			
			//get the property value
			path1 = prop.getProperty("script1Path");//path of callPredictProtein.sh
			path2 = prop.getProperty("script2Path");//path of parseTMnonTM.pl
			path3 = prop.getProperty("script3Path");//path of pp2features.py
			datasetPath = prop.getProperty("datasetPath");//path of dataset
			datasetFastaPath = prop.getProperty("datasetFastaPath");//path of dataset till fasta folder. arg[0] for callPredictProtein.sh
			predictProteinOutput = prop.getProperty("predictProteinOutput");//path of the output files from PredictProtein, arg[1] for callPredictProtein.sh
			impOrSolFlag = prop.getProperty("impOrSolFlag");//value of arg[2] for callPredictProtein.sh
			trainingOrTestsetFlag = prop.getProperty("trainingOrTestsetFlag");//1 to generate arff for trainingset and 0 to generate arff for testset
			outputArffPath = prop.getProperty("outputArffPath");//path of where the arff file will be generated ; arg[2] while calling parseTMnonTM.pl and
			callPPFlag = prop.getProperty("callPPFlag");// true if PredictProtein script is called else false
			
			//for testing
			testsetFastaPath = prop.getProperty("testsetFastaPath");//path of fasta files for testing, arg[0] for callPredictProtein.sh
			predictProteinTestsetOutput = prop.getProperty("predictProteinTestsetOutput");//output path for predictProtein ;arg[1] for callPredictProtein.sh
			testsetPath = prop.getProperty("testsetPath");//path of test dataset 
			testsetOutputArffPath = prop.getProperty("testsetOutputArffPath");//path of where the arff file will be generated for testset; arg[2] while calling parseTMnonTM.pl 
			
			
			//run scripts to get the training set arff file
			if(trainingOrTestsetFlag.equalsIgnoreCase("1")){
				if(callPPFlag.equalsIgnoreCase("true")){
					//call script1: callPredictProtein.sh to run PredictProtein
					path1 = path1 +" "+ datasetFastaPath+" "+ predictProteinOutput+ " "+impOrSolFlag;
					//Runtime.getRuntime().exec(path1);
					executeScript(path1);
					System.out.println("script1 executed...");
				}
				//call script2: parseTMnonTM.pl to generate Prot files
				path2 = "perl "+path2+" " +trainingOrTestsetFlag+ " " +datasetPath +" " +outputArffPath;
				//Runtime.getRuntime().exec(path2);
				executeScript(path2);
				System.out.println("script2 executed...");
				
				//call script3: pp2features.py to run PP2Features and generate output arff file
        datasetPath = "-p " +datasetPath;
				outputArffPath = "--arff-file "+outputArffPath+PP2FeatureOutputFileName ;
				path3 = "python " +path3 +" "+datasetPath+" "+outputArffPath;
				//Runtime.getRuntime().exec(path3);
				executeScript(path3);
				System.out.println("script3 executed...");
				System.out.println("....trainingset output arff is generated...");
		     }else{	
				//run scripts to get the testing set arff file
		    	if(callPPFlag.equalsIgnoreCase("true")){
			    	 //call script1: callPredictProtein.sh to run PredictProtein
					path4 = path1 +" "+ testsetFastaPath+" "+ predictProteinTestsetOutput+ " "+impOrSolFlag;
					//Runtime.getRuntime().exec(path4);
					executeScript(path4);
					System.out.println("script1 executed for testing set...");
		    	}	
					
				//call script2: parseTMnonTM.pl to generate Prot files
				path5 = "perl "+path2+" " +trainingOrTestsetFlag+ " " +testsetPath +" " +testsetOutputArffPath;
				//Runtime.getRuntime().exec(path5);
				executeScript(path5);
				System.out.println("script2 executed for testing set...");
				
				//call script3: pp2features.py to run PP2Features and generate output arff file
        testsetPath = "-p " +testsetPath;
				testsetOutputArffPath = "--arff-file "+testsetOutputArffPath+PP2FeatureOutputFileNameTestset ;     
				path6 = "python " +path3 +" "+testsetPath+" "+testsetOutputArffPath;
				//Runtime.getRuntime().exec(path6);
				executeScript(path6);
				System.out.println("script3 executed...");
				System.out.println("....testingset output arff is generated...");
		     }
			
 	     } catch (IOException ex) {
 	    	 ex.printStackTrace();
 	     }
	

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

}
