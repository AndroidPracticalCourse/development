import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
	public static void main(String argv[]) throws Exception {
		String userinput;
		String serverresponse;
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		try {
			Socket clientSocket = new Socket("localhost", 6789);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			userinput = "-1";
			outToServer.writeBytes("REMOTEAPI_CONNECTREQ\n");
			serverresponse = inFromServer.readLine();
			System.out.println("FROM SERVER: " + serverresponse);
			if(serverresponse.equals("REMOTEAPI_CONNECTACCEPT")){
				System.out.println("Connection to server accepted.");
				while (Integer.parseInt(userinput) != 9) {
					userinput = inFromUser.readLine();
					System.out.println(userinput);
					outToServer.writeBytes(userinput + '\n');
					serverresponse = inFromServer.readLine();
					System.out.println("FROM SERVER: " + serverresponse);
				}
				clientSocket.close();
			}
			else{
				System.out.println("Handshake error.");
				System.exit(0);
			}
			

		} catch (ConnectException e) {
			System.out.println(e.toString());
		}

		
	}
}
