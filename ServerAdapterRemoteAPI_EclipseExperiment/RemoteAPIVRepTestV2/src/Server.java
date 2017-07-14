import coppelia.*;
import java.io.*;
import java.net.*;

public class Server implements Runnable{
	private remoteApi vrep;
	private int clientID;
	public Server(remoteApi vrep, int clientID){
		this.vrep=vrep;
		this.clientID=clientID;
	}
	
	@Override
	public void run() {
		System.out.println("Server thread is running...");  
		String clientFeedback;
		try {
			ServerSocket welcomeSocket = new ServerSocket(6789);
			 Socket connectionSocket = welcomeSocket.accept();
			   BufferedReader inFromClient =
			    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			   DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			while (true) {
				int clientCmd=0;
				  String receivebuffer = inFromClient.readLine();
				  if(receivebuffer.equals("REMOTEAPI_CONNECTREQ")){
					  System.out.println("Incoming connection accepted");
					  outToClient.writeBytes("REMOTEAPI_CONNECTACCEPT\n");
					  outToClient.writeInt(clientID);
				  }else{
					  clientCmd = Integer.parseInt(receivebuffer);
					  System.out.println("Received: " + clientCmd);
					   if(clientCmd==1){
						   vrep.simxStartSimulation(clientID, clientID);
						   System.out.println("V-REP simulation started as requested");
						   outToClient.writeBytes("V-REP simulation started as requested\n");
					   }
					   else if(clientCmd==2){
						   vrep.simxPauseSimulation(clientID, clientID);
						   System.out.println("V-REP simulation paused as requested");
						   outToClient.writeBytes("V-REP simulation paused as requested\n");
					   }
					   else if(clientCmd==3){
						   vrep.simxStopSimulation(clientID, clientID);
						   System.out.println("V-REP simulation stopped as requested");
						   outToClient.writeBytes("V-REP simulation stopped as requested\n");
					   }
				  }
				 }
		} catch (IOException e ) {
			//e.printStackTrace();
			System.out.println(e.toString());
			System.exit(0);
		}
		
		
		
		
		
	}
	

}
