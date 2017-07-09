import java.util.Scanner;

import coppelia.*;

public class ServerStarter {
	private static String IP="127.0.0.1";
    private static int Port=19997;
    private static int menuchoice;
	public static void main(String[] args){
		remoteApi vrep = new remoteApi();
        vrep.simxFinish(-1); // just in case, close all opened connections
        System.out.println("Connecting to V-REP via Remote API...");
        int clientID = vrep.simxStart(IP,Port,true,true,5000,5);
        System.out.println("ClientID is = "+clientID);
        System.out.println("ConnectionID is = "+vrep.simxGetConnectionId(clientID));
        if(clientID==-1){
        	System.out.println("ERROR, CANNOT CONNECT TO V-REP! SERVER ADAPTER TERMINATED.");
        	System.exit(0);
        }
        System.out.println("Starting server thread...");
    	Server server = new Server(vrep, clientID);
    	Thread serverthread = new Thread(server);
    	serverthread.start();
        Scanner sc = new Scanner(System.in);
        while(clientID!=-1){
        	
        	
        	System.out.println("You can manually type in command in console to interface with V-REP. Read source code for command.");
        	menuchoice = sc.nextInt();
        	
        	
        	if(menuchoice==1){
        		//start simulation
                vrep.simxStartSimulation(clientID, clientID);
        	}
        	if(menuchoice==2){
        		//pause simulation
                vrep.simxPauseSimulation(clientID, clientID);
        	}
        	if(menuchoice==3){
        		//stop simulation
                vrep.simxStopSimulation(clientID, clientID);
        	}
        	if(menuchoice==4){
        		//vrep.simxReadVisionSensor(clientID, sensorHandle, detectionState, auxValues, operationMode);
        		vrep.simxReadVisionSensor(clientID, 0, new BoolW(true), new FloatWAA(0), 0);
        	}
        	
        	
        }
        System.out.println("Program ended");
        System.exit(0);
	}
}

