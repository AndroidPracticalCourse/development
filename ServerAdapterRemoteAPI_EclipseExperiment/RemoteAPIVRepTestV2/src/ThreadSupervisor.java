import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import coppelia.remoteApi;

public class ThreadSupervisor implements Runnable{
	private remoteApi vrep;
	private int clientID;
	private Server server;
	private Thread serverthread;
	private int ServerPort;
	public ThreadSupervisor(remoteApi vrep, int clientID, int ServerPort){
		this.vrep=vrep;
		this.clientID=clientID;
		this.ServerPort=ServerPort;
	}
	private ServerSocket welcomeSocket;
	private Socket connectionSocket;
	@Override
	public void run() {
		// TODO Auto-generated method stub
			try {
				welcomeSocket = new ServerSocket(ServerPort);
				connectionSocket = welcomeSocket.accept();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Thread supervisor has started.");
				server = new Server(vrep, clientID,connectionSocket);
		    	serverthread = new Thread(server);
		    	serverthread.start();
		    	System.out.println("Started server thread for the first time.");
		 while(true){	
			 
				 try {
					connectionSocket = welcomeSocket.accept();
					 server = new Server(vrep, clientID,connectionSocket);
				    	serverthread = new Thread(server);
				    	serverthread.start();
				    	System.out.println("Server thread is restarted.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				
				
			 }
			 try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				

		}
	}
}

