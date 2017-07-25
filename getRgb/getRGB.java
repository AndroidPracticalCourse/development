

import coppelia.CharWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;

/**
 *
 * @author bauer
 */
public class getRGB {
    
    public static void main(String[] args) throws InterruptedException {
        //Standard connection routine
        System.out.println("Program started");
        remoteApi vrep = new remoteApi();
        int clientID = vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        vrep.simxStartSimulation(clientID, vrep.simx_opmode_oneshot);
        if (clientID != -1) {
            System.out.println("Connected to remote API server");
            //Resolution of the Sensor is 1x1 pixel
            int res = 1;
            char[] imarray = new char[3];
            int r, g, b;
            CharWA image = new CharWA(res * res * 3);
            IntWA resolution = new IntWA(2);

            IntW sensorHandle = new IntW(0);
            vrep.simxGetObjectHandle(clientID, "Vision_sensor", sensorHandle, vrep.simx_opmode_oneshot_wait);
            vrep.simxGetVisionSensorImage(clientID, sensorHandle.getValue(), resolution, image, 0, vrep.simx_opmode_streaming);
            //I would suggest to call this Part  one time when the gripper is closed
            while (vrep.simxGetConnectionId(clientID) != -1) {
                //Get sensor image through API
                vrep.simxGetVisionSensorImage(clientID, sensorHandle.getValue(), resolution, image, 0, vrep.simx_opmode_buffer);
                imarray = image.getArray();
                //Assign RGB integer Values via typecast of char
                r = (int)imarray[0];
                g = (int)imarray[1];
                b = (int)imarray[2];
                //TBD Process the colour infos 
                System.out.printf("Red: %d  Green:%d  Blue:%d \n", r,g,b);
                Thread.sleep(50);
            
        }
        vrep.simxFinish(clientID);
    }

    
        else {
            System.out.println("Failed connecting to remote API server");
    }

    System.out.println (

"Program ended");
    }
}
