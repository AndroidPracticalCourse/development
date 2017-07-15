import coppelia.*;
import java.io.*;
import java.net.*;

public class Server implements Runnable {

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
			  	if (receivedBuffer.equals("REMOTEAPI_CONNECTREQ")) { // initial connection setup
                    acceptConnectionRequest(outToClient);
			  	} else if (receivedBuffer.equals("SIMULATION")) { // commands to start/pause/stop simulation
					modifySimulation(inFromClient, outToClient);
			  	} else if (receivedBuffer.equals("SENSORDATA")) { // receive sensor data from Android device and send to the robot
                    receiveSensorData(inFromClient, outToClient);
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
        outToClient.writeBytes("REMOTEAPI_CONNECTACCEPT\n");
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
        }
    }

    private void receiveSensorData(BufferedReader inFromClient, DataOutputStream outToClient) throws IOException {
        String nameSelComponent = inFromClient.readLine();
		
        // Obtains the handle of the selected component based on its name
		IntW handleSelComponent = new IntW(1);
        vrep.simxGetObjectHandle(clientID, nameSelComponent, handleSelComponent, remoteApi.simx_opmode_blocking);

        // Obtain acceleration data
        String accelerationX = inFromClient.readLine();
     	String accelerationY = inFromClient.readLine();
        String accelerationZ = inFromClient.readLine();
        System.out.println("Acceleration = [" + accelerationX + ", " + accelerationY + ", " + accelerationZ + "]");
    }

}
