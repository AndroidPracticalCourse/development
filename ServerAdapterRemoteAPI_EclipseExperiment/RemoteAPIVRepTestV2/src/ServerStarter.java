import java.nio.charset.Charset;
import java.io.*;
import java.util.Scanner;

import coppelia.*;

public class ServerStarter {
	private static String IP;
    private static int Port;
    private static int ServerPort;
    private static String menuchoice;
	public static void main(String[] args){
		String line;
		try {
		    InputStream fis = new FileInputStream("serveradapterconfig.txt");
		    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader br = new BufferedReader(isr);
		
		    	br.readLine();
		    	IP=br.readLine();
		    	try{
		    		Port=Integer.parseInt(br.readLine());
		    	}
		    	catch(NumberFormatException e){
		    		System.out.println("Invalid v-rep port format! Please change port in serveradapterconfig.txt");
		    		Port=19997;
		    	}
		    	try{
		    		ServerPort=Integer.parseInt(br.readLine());
		    	}
		    	catch(NumberFormatException e){
		    		System.out.println("Invalid server adapter port format! Please change port in serveradapterconfig.txt");
		    		ServerPort=6789;
		    	}
		    	

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			BufferedWriter bw;
			FileWriter fw;
			IP="127.0.0.1";
			Port=19997;
			ServerPort=6789;
			try {
				fw = new FileWriter("serveradapterconfig.txt");
				bw = new BufferedWriter(fw);
				bw.write("Edit IP and Port of V-REP here. Second line is RemoteAPI IP. Third line is RemoteAPI Port. Forth line is Server Port.");
				bw.newLine();
				bw.write("127.0.0.1");
				bw.newLine();
				bw.write("19997");
				bw.newLine();
				bw.write("6789");
				bw.flush();
				bw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Error! Cannot create serveradapterconfig.txt");
				e1.printStackTrace();
			}
			
			System.out.println("serveradapterconfig.txt is not found. Created with default value of RemoteAPI=127.0.0.1:19997, ServerPort=6789");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			IP="127.0.0.1";
			Port=19997;
			ServerPort=6789;
			System.out.println("serveradapterconfig.txt is not in right format!");
			System.out.println("please manually delete it and restart this server adapter.");
			
		}
		
		
		
		remoteApi vrep = new remoteApi();
        vrep.simxFinish(-1); // just in case, close all opened connections
        System.out.println("Connecting to V-REP via Remote API on "+IP+":"+Port+"...");
        int clientID = vrep.simxStart(IP,Port,true,true,5000,5);
        System.out.println("ClientID is = "+clientID);
        System.out.println("ConnectionID is = "+vrep.simxGetConnectionId(clientID));
        if(clientID==-1){
        	System.out.println("ERROR, CANNOT CONNECT TO V-REP! SERVER ADAPTER TERMINATED.");
        	System.exit(0);
        }
        System.out.println("Starting ThreadSupervisor thread...");
        ThreadSupervisor ts = new ThreadSupervisor(vrep, clientID, ServerPort);
    	Thread tsthread = new Thread(ts);
    	tsthread.start();
        Scanner sc = new Scanner(System.in);
        System.out.println("You can manually type in command in console to interface with V-REP.");
        System.out.println("For full functionallity, the Android app is required.");
        System.out.println("1 = Start Simulation");
        System.out.println("2 = Pause Simulation");
        System.out.println("3 = Stop Simulation");
        System.out.println("X = Exit Server Adapter");
        System.out.println("L = Left");
        System.out.println("R = Right");
        System.out.println("U = Up");
        System.out.println("D = Down");
        System.out.println("S = Stop");
        while(clientID!=-1){
        	menuchoice = sc.next();
        	
        	if(menuchoice.equals("1")){
                //vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0.01"), remoteApi.simx_opmode_oneshot);
                //start simulation
                vrep.simxStartSimulation(clientID, clientID);
        	}
        	if(menuchoice.equals("2")){
                //vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0.0"), remoteApi.simx_opmode_oneshot);
        		//pause simulation
                vrep.simxPauseSimulation(clientID, clientID);
        	}
        	if(menuchoice.equals("3")){
        		//stop simulation
                vrep.simxStopSimulation(clientID, clientID);
        	}
        	if(menuchoice.equals("L")){
        		vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0.02"), remoteApi.simx_opmode_oneshot);
        	}
        	if(menuchoice.equals("R")){
        		vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("-0.02"), remoteApi.simx_opmode_oneshot);
        	}
        	if(menuchoice.equals("U")){
        		vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("0.02"), remoteApi.simx_opmode_oneshot);
        	}
        	if(menuchoice.equals("D")){
        		vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("-0.02"), remoteApi.simx_opmode_oneshot);
        	}
        	if(menuchoice.equals("S")){
        		vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0"), remoteApi.simx_opmode_oneshot);
        		vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("0"), remoteApi.simx_opmode_oneshot);
        	}

        	if (menuchoice.equals("X")) {
                System.exit(0);
            }
        }
        System.out.println("Program ended");
        System.exit(0);
	}
}

