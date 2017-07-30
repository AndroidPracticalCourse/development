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
        System.out.println("Starting ThreadSupervisor thread...");
        ThreadSupervisor ts = new ThreadSupervisor(vrep, clientID);
    	Thread tsthread = new Thread(ts);
    	tsthread.start();
        Scanner sc = new Scanner(System.in);
        while(clientID!=-1){
        	
        	
        	System.out.println("You can manually type in command in console to interface with V-REP. Read source code for command.");
        	menuchoice = sc.nextInt();
        	
        	if(menuchoice==1){
                //vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0.01"), remoteApi.simx_opmode_oneshot);
                //start simulation
                vrep.simxStartSimulation(clientID, clientID);
        	}
        	if(menuchoice==2){
                //vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0.0"), remoteApi.simx_opmode_oneshot);
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

        	if (menuchoice == 0) {
                System.exit(0);
            }
        }
        System.out.println("Program ended");
        System.exit(0);
	}
}

