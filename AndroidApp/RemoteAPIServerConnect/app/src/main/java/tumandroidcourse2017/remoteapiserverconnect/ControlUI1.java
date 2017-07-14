package tumandroidcourse2017.remoteapiserverconnect;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import coppelia.CharWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;

import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.getSocket;
import tumandroidcourse2017.remoteapiserverconnect.helper.Duplex;
import tumandroidcourse2017.remoteapiserverconnect.sensors.Accelerometer;
import tumandroidcourse2017.remoteapiserverconnect.sensors.Gyroscope;
import tumandroidcourse2017.remoteapiserverconnect.sensors.RotationVector;

public class ControlUI1 extends Activity implements SensorEventListener {

    // V-REP variables
    private remoteApi vrep;
    private int clientID;

    // V-REP objects
    private final String nameArm = "IRB140_joint3#0";
    private final String nameWrist = "IRB140_joint5#0";
    private final String nameArmCamera = "Vision_sensor";
    private String nameCurrSelComponent = nameArm; // stores the name of the arm or wrist component, depending on the selected mode

    // Sensors and data
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mRotation;
    private boolean isSensorsStarted;
    private float[] acceleration; // From accelerometer
    private float rollAngle; // From gyroscope
    private float pitchAngle; // From gyroscope
    private float[] rotationVector; // From rotation vector

    // Logging
    private static final String TAG = "ControllerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_ui1);

        initData();
        initWidgets();

        startSensors();
    }

    private void initData() {
        vrep = new remoteApi();
        clientID = getIntent().getIntExtra(getString(R.string.str_clientID), -1);
    }

    private void initWidgets(){
        TextView textViewInfo = (TextView) findViewById(R.id.textView_info);
        textViewInfo.setText("Connected to " + getIntent().getStringExtra(MainActivity.IPInfoPort)); // TODO - change to append()
        Button buttonStart = (Button) findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(1);
            }
        });
        Button buttonPause = (Button) findViewById(R.id.button_pause);
        buttonPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(2);
            }
        });
        Button buttonStop = (Button) findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(3);
            }
        });

        Button buttonToggleMode = (Button) findViewById(R.id.btn_toggleMode);
        buttonToggleMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { // Toggles between arm and wrist mode; arm mode selected by default
                TextView selMode = (TextView) findViewById(R.id.text_currModeSel);
                if (selMode.toString().equals(getString(R.string.text_arm))) {
                    nameCurrSelComponent = nameWrist;
                    selMode.setText(R.string.text_wrist);
                } else if (selMode.toString().equals(getString(R.string.text_wrist))) {
                    nameCurrSelComponent = nameArm;
                    selMode.setText(R.string.text_arm);
                }
            }
        });

        // Button buttonClaw = (Button) findViewById(R.id.btn_claw);
    }

    // =============================================================
    //                  ACTIVITY CLASS METHODS
    // =============================================================

    @Override
    protected void onResume() {
        super.onResume();
        startSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensors(); // To conserve phone battery
    }

    // =============================================================
    //                      SENSOR METHODS
    // =============================================================

    private void startSensors() {
        if (!isSensorsStarted) {
            isSensorsStarted = true;
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

            registerSensorListeners();
            Log.i(TAG, "Sensors started");
        }
    }

    private void stopSensors() {
        isSensorsStarted = false;
        mSensorManager.unregisterListener(this);
        Log.i(TAG, "Sensors stopped");
    }

    private void registerSensorListeners() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // =============================================================
    //          SENSOREVENTLISTENER IMPLEMENTATION METHODS
    // =============================================================

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER :
                Accelerometer accelerometer = new Accelerometer(event);
                acceleration = accelerometer.update();
                break;
            case Sensor.TYPE_GYROSCOPE :
                Gyroscope gyroscope = new Gyroscope(event);
                float[] gyroscopeValues = gyroscope.update();
                rollAngle = gyroscopeValues[0];
                pitchAngle = gyroscopeValues[1];
                break;
            case Sensor.TYPE_ROTATION_VECTOR :
                RotationVector rotation = new RotationVector(event);
                rotationVector = rotation.update();
                break;
            default :
                break;
        }

        sendDataToComponent();
    }

    // =============================================================
    //                      V-REP METHODS
    // =============================================================

    // Sends any movement data from the device sensors to the arm/wrist object in the V-REP simulator
    private void sendDataToComponent() {
        IntW handleSelComponent = new IntW(1);

        vrep.simxGetObjectHandle(clientID, nameCurrSelComponent, handleSelComponent, remoteApi.simx_opmode_blocking);

        // TODO - edit this portion
        vrep.simxSetJointPosition(clientID, handleSelComponent.getValue(), rollAngle, remoteApi.simx_opmode_oneshot);
        vrep.simxSetJointPosition(clientID, handleSelComponent.getValue(), pitchAngle, remoteApi.simx_opmode_oneshot);
    }

    // Obtains the handle of the camera object
    private int getHandleArmCamera() {
        IntW handleArmCamera = new IntW(1);
        vrep.simxGetObjectHandle(clientID, nameArmCamera, handleArmCamera, remoteApi.simx_opmode_blocking);

        return handleArmCamera.getValue();
    }

    // Returns the image details of the object currently picked up by the arm
    private Duplex<IntWA, CharWA> getImageDataFromArmCamera() {
        IntWA imageResolution = new IntWA(2); // int array to store the resolution of the image
        CharWA imageData = new CharWA(65536); // char array to store the image data

        int handleArmCamera = getHandleArmCamera(); // to obtain the handle of the camera object

        vrep.simxGetVisionSensorImage(clientID,
                handleArmCamera,
                imageResolution,
                imageData,
                0, // image options; set to 0 for RGB
                remoteApi.simx_opmode_streaming);

        return new Duplex<>(imageResolution, imageData);
    }

    // =============================================================
    //                      HELPER METHODS
    // =============================================================

    private void sendCommand(int userInput){
        try {
            Socket clientSocket = getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(userInput + "" + '\n');
            Toast.makeText(ControlUI1.this, "FROM SERVER: " + inFromServer.readLine(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ControlUI1.this, e.toString(), Toast.LENGTH_SHORT).show();
        }


    }
}
