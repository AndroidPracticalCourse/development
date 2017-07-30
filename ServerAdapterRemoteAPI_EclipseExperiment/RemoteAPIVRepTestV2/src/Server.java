import coppelia.*;
import java.io.*;
import java.net.*;

public class Server implements Runnable {

    private static final String MSG_REMOTEAPI_CONNECTACCEPT = "REMOTEAPI_CONNECTACCEPT";
    private static final String MSG_REMOTEAPI_CONNECTREQ = "REMOTEAPI_CONNECTREQ";
    private static final String MSG_SIMULATION = "SIMULATION";
    private static final String MSG_MOVEMENTDATA = "MOVEMENTDATA";
    private static final String MSG_GRIPPERDATA = "GRIPPERDATA";
    private static final String MSG_MOVEMENTDATAVIABUTTON = "MOVEMENTDATAVIABUTTON";
    private static final String MSG_REGCOLORDATA = "REQCOLORDATA";

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
					receiveSimulationData(inFromClient, outToClient);
			  	} else if (receivedBuffer.equals(MSG_MOVEMENTDATA)) { // receive sensor data from Android device and send to the robot
                    receiveMovementData(inFromClient, outToClient);
                } else if (receivedBuffer.equals(MSG_GRIPPERDATA)) { // receive data to control the gripper
                    receiveGripperData(inFromClient, outToClient);
                } else if (receivedBuffer.equals(MSG_MOVEMENTDATAVIABUTTON)) { // receive data to control the gripper
                    receiveMovementDataViaButton(inFromClient, outToClient);
                }  else if (receivedBuffer.equals(MSG_REGCOLORDATA)) { // receive data to control the gripper
                	sendSenderImageData(inFromClient, outToClient);
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

    private void receiveSimulationData(BufferedReader inFromClient, DataOutputStream outToClient) throws IOException {
        int clientCmd = Integer.parseInt(inFromClient.readLine());
        System.out.println("Received: " + clientCmd);
        if (clientCmd == 1) {
            vrep.simxStartSimulation(clientID, clientID);
            System.out.println("V-REP simulation started");
        }
        else if (clientCmd==2) {
            vrep.simxPauseSimulation(clientID, clientID);
            System.out.println("V-REP simulation paused");
        }
        else if (clientCmd==3) {
            vrep.simxStopSimulation(clientID, clientID);
            System.out.println("V-REP simulation stopped");
        } else {
        	System.exit(0);
        }
    }

    private void receiveMovementData(BufferedReader inFromClient, DataOutputStream outToClient) throws IOException {
    	int tiltLeftRight = Integer.parseInt(inFromClient.readLine());
        int tiltUpDown = Integer.parseInt(inFromClient.readLine());
        //System.out.println("tiltLeftRight = " + tiltLeftRight + ", tiltUpDown = " + tiltUpDown);

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

    private void receiveMovementDataViaButton(BufferedReader inFromClient, DataOutputStream outToClient) throws IOException{
    	String command = inFromClient.readLine();
    	System.out.println(command);
    	if(command.equals("L")){
    		vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0.02"), remoteApi.simx_opmode_oneshot);
    	}
    	else if(command.equals("R")){
    		vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("-0.02"), remoteApi.simx_opmode_oneshot);
    	}
    	else if(command.equals("U")){
    		vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("0.02"), remoteApi.simx_opmode_oneshot);
    	}
    	else if(command.equals("D")){
    		vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("-0.02"), remoteApi.simx_opmode_oneshot);
    	}
    	else if(command.equals("STOP")){
    		vrep.simxSetFloatSignal(clientID, "rotate", Float.valueOf("0"), remoteApi.simx_opmode_oneshot);
    		vrep.simxSetFloatSignal(clientID, "moveUpDown", Float.valueOf("0"), remoteApi.simx_opmode_oneshot);
    	}
    }

    private void receiveGripperData(BufferedReader inFromClient, DataOutputStream outToClient) throws IOException {
        int gripperStatus = Integer.parseInt(inFromClient.readLine());
        // 0 for opening, 1 for closing
        vrep.simxSetIntegerSignal(clientID, "closeGripper", gripperStatus, remoteApi.simx_opmode_oneshot);
    }
    
    private void sendSenderImageData(BufferedReader inFromClient, DataOutputStream outToClient) throws IOException{
    	int[] rgbValues = getSensorImageData();
    	System.out.println("abc");
        System.out.println("rgbValues = " + rgbValues[0] + ", " + rgbValues[1] + ", " + rgbValues[2]);
        outToClient.writeBytes(rgbValues[0] + "" + '\n');
        outToClient.writeBytes(rgbValues[1] + "" + '\n');
        outToClient.writeBytes(rgbValues[2] + "" + '\n');
    }

    private int[] getSensorImageData() {
        int res = 1; // resolution of the Sensor is 1x1 pixel
        char[] imarray = new char[3];
        int[] rgbValues = {-1, -1, -1};
        CharWA image = new CharWA(res * res * 3);
        IntWA resolution = new IntWA(2);

        IntW sensorHandle = new IntW(0);
        vrep.simxGetObjectHandle(clientID, "Vision_sensor", sensorHandle, vrep.simx_opmode_oneshot_wait);
        vrep.simxGetVisionSensorImage(clientID, sensorHandle.getValue(), resolution, image, 0, vrep.simx_opmode_streaming);
        
        
            // Get sensor image through API
            vrep.simxGetVisionSensorImage(clientID, sensorHandle.getValue(), resolution, image, 0, vrep.simx_opmode_buffer);
            imarray = image.getArray();
            // Assign RGB integer values via typecast of char
            rgbValues[0] = (int) imarray[0];
            rgbValues[1] = (int) imarray[1];
            rgbValues[2] = (int) imarray[2];
            //TBD Process the colour infos 
            System.out.printf("Red: %d  Green:%d  Blue:%d \n", rgbValues[0], rgbValues[1], rgbValues[2]);
            //Thread.sleep(50);
         

        return rgbValues;
    }

}
