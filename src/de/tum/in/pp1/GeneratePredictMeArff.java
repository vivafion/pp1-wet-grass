package de.tum.in.pp1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GeneratePredictMeArff {
	public static void main(String[] args) {

	}
	
	public void executeBachScript() {
		try {
			String cmd = "ls -l"; // this is the command to execute in the Unix shell
			// create a process for the shell
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
			pb.redirectErrorStream(true); // use this to capture messages sent to stderr
			Process shell;
				shell = pb.start();
			InputStream shellIn = shell.getInputStream(); // this captures the output from the command
			int shellExitStatus = shell.waitFor(); // wait for the shell to finish and get the return code
			// at this point you can process the output issued by the command
			// for instance, this reads the output and writes it to System.out:
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
	
	public void executePythonScript() {
		try
        {
            Runtime r = Runtime.getRuntime();
            Process p = r.exec("python foo.py");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            p.waitFor();
            String line = "";
            while (br.ready())
                System.out.println(br.readLine());

        }
        catch (Exception e)
        {
		String cause = e.getMessage();
		if (cause.equals("python: not found"))
			System.out.println("No python interpreter found.");
        }
	}
}
