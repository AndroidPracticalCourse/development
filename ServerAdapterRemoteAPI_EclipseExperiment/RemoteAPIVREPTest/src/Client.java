import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Client {
	public static void main(String argv[]) throws Exception {
		  String userinput;
		  String serverresponse;
		  BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		  Socket clientSocket = new Socket("localhost", 6789);
		  
		 
			  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			  userinput="-1";
			  
		while(Integer.parseInt(userinput)!=9){
			  userinput = inFromUser.readLine();
			  System.out.println(userinput);
			  outToServer.writeBytes(userinput+'\n');
			  serverresponse = inFromServer.readLine();
			  System.out.println("FROM SERVER: " + serverresponse);
		  }
		  
		  clientSocket.close();
		 }
}
