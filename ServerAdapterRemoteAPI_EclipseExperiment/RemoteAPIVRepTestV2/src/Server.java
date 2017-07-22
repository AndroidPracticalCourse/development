import coppelia.*;
import java.io.*;
import java.net.*;

public class Server implements Runnable {

    private static final String MSG_REMOTEAPI_CONNECTACCEPT = "REMOTEAPI_CONNECTACCEPT";
    private static final String MSG_REMOTEAPI_CONNECTREQ = "REMOTEAPI_CONNECTREQ";
    private static final String MSG_SIMULATION = "SIMULATION";
    private static final String MSG_MOVEMENTDATA = "MOVEMENTDATA";
    private static final String MSG_GRIPPERDATA = "GRIPPERDATA";

	private remoteApi vrep;
	private int clientID;

	public Server(remoteApi vrep, int clientID) {
		this.vrep = vrep;
		this.clientID = clientID;
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
			  	String receivedBuffer = inFromClient.readLine();
			  	if (receivedBuffer.equals(MSG_REMOTEAPI_CONNECTREQ)) { // initial connection setup
                    acceptConnectionRequest(outToClient);
			  	} else if (receivedBuffer.equals(MSG_SIMULATION)) { // commands to start/pause/stop simulation
					modifySimulation(inFromClient, outToClient);
			  	} else if (receivedBuffer.equals(MSG_MOVEMENTDATA)) { // receive sensor data from Android device and send to the robot
                    receiveMovementData(inFromClient, outToClient);
                } else if (receivedBuffer.equals(MSG_GRIPPERDATA)) { // receive data to control the gripper
                    receiveGripperData(inFromClient, outToClient);
                }            
            }
		} catch (IOException e ) {
			//e.printStackTrace();
			System.out.println(e.toString());
			System.exit(0);
		}
	}

	private void acceptConnectionRequest(DataOutputStream outToClient) throws IOException {
        System.out.println("Incoming connection accepted");
        outToClient.writeBytes(MSG_REMOTEAPI_CONNECTACCEPT + '\n');
        outToClient.writeBytes(clientID + "" + '\n');
    }

    private void modifySimulation(BufferedReader inFromClient, DataOutputStream outToClient) throws IOException {
		
        int clientCmd = Integer.parseInt(inFromClient.readLine());
        System.out.println("Received: " + clientCmd);
        if (clientCmd == 1) {
            vrep.simxStartSimulation(clientID, clientID);
            System.out.println("V-REP simulation started as requested");
            outToClient.writeBytes("V-REP simulation started as requested\n");
        }
        else if (clientCmd==2) {
            vrep.simxPauseSimulation(clientID, clientID);
            System.out.println("V-REP simulation paused as requested");
            outToClient.writeBytes("V-REP simulation paused as requested\n");
        }
        else if (clientCmd==3) {
            vrep.simxStopSimulation(clientID, clientID);
            System.out.println("V-REP simulation stopped as requested");
            outToClient.writeBytes("V-REP simulation stopped as requested\n");
        } else {
        	System.exit(0);
        }
    }

    private void receiveMovementData(BufferedReader inFromClient, DataOutputStream outToClient) throws IOException {
    	int tiltLeftRight = Integer.parseInt(inFromClient.readLine());
        int tiltUpDown = Integer.parseInt(inFromClient.readLine());
        System.out.println("tiltLeftRight = " + tiltLeftRight + ", tiltUpDown = " + tiltUpDown);

        // Configurable threshold values to adjust the velocity scale
        int threshold1 = 15;
        int threshold2 = 35;
        int threshold3 = 55;

        // Control left/right
        if (tiltLeftRight > (threshold1 * -1) && tiltLeftRight < threshold1) {
            vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0.0"), remoteApi.simx_opmode_oneshot);
        } else if (tiltLeftRight > (threshold2 * -1) && tiltLeftRight <= (threshold1 * -1)) {
            vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("-0.01"), remoteApi.simx_opmode_oneshot);
        } else if (tiltLeftRight >= threshold1 && tiltLeftRight < threshold2) {
            vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0.01"), remoteApi.simx_opmode_oneshot);
        } else if (tiltLeftRight > (threshold3 * -1) && tiltLeftRight <= (threshold2 * -1)) {
            vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("-0.02"), remoteApi.simx_opmode_oneshot);
        } else if (tiltLeftRight >= threshold2 && tiltLeftRight < threshold3) {
            vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0.02"), remoteApi.simx_opmode_oneshot);
        } else if (tiltLeftRight <= (threshold3 * -1)) { 
            vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("-0.04"), remoteApi.simx_opmode_oneshot);
        } else if (tiltLeftRight >= threshold3) {
            vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0.04"), remoteApi.simx_opmode_oneshot);
        }

        // Control up/down
        // Threshold for downward movement is divided by 2 to cater for the typical range of device movement of a user holding the device
        // Upward movement: 0 to 90 degrees, downward movement: 0 to -45 degrees
        if (tiltUpDown > (threshold1 * -1) && tiltUpDown < threshold1) {
            vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("0.0"), remoteApi.simx_opmode_oneshot);
        } else if (tiltUpDown > ((threshold2 * -1) / 2) && tiltUpDown <= ((threshold1 * -1) / 2)) {
            vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("-0.01"), remoteApi.simx_opmode_oneshot);
        } else if (tiltUpDown >= threshold1 && tiltUpDown < threshold2) {
            vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("0.01"), remoteApi.simx_opmode_oneshot);
        } else if (tiltUpDown > ((threshold3 * -1) / 2) && tiltUpDown <= ((threshold2 * -1) / 2)) {
            vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("-0.02"), remoteApi.simx_opmode_oneshot);
        } else if (tiltUpDown >= threshold2 && tiltUpDown < threshold3) {
            vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("0.02"), remoteApi.simx_opmode_oneshot);
        } else if (tiltUpDown <= ((threshold3 * -1) / 2)) { 
            vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("-0.03"), remoteApi.simx_opmode_oneshot);
        } else if (tiltUpDown >= threshold3) {
            vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("0.03"), remoteApi.simx_opmode_oneshot);
        }
    }

    private void receiveGripperData(BufferedReader inFromClient, DataOutputStream outToClient) throws IOException {
        int gripperStatus = Integer.parseInt(inFromClient.readLine());
        // 0 for opening, 1 for closing
        vrep.simxSetIntegerSignal(clientID, "closeGripper", gripperStatus, remoteApi.simx_opmode_oneshot);
    }

}
