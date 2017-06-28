package tum.controller;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import coppelia.CharWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;
import tum.controller.helper.Duplex;
import tum.controller.sensors.Accelerometer;
import tum.controller.sensors.Gyroscope;
import tum.controller.sensors.RotationVector;

public class MainActivity extends Activity implements SensorEventListener {

    // V-REP variables
    private int clientID;
    private String objectName = "IRB140#0";
    private remoteApi vrep;
    private String nameArmCamera = "Vision_sensor";

    // Widgets
    private Button buttonConnect;

    // Sensor variables
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mRotation;
    private boolean isSensorsStarted;

    // Sensor data variables
    private float[] acceleration; // From accelerometer
    private float rollAngle; // From gyroscope
    private float pitchAngle; // From gyroscope
    private float[] rotationVector; // From rotation vector

    // V-REP arm handles


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();

        startSensors();
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
    //                     INITIALISATION METHODS
    // =============================================================

    private void initWidgets() {
        // Connect button
        buttonConnect = (Button) findViewById(R.id.btn_connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initRemoteApi();
            }
        });
    }

    // Initialises the remote API
    private void initRemoteApi() {
        vrep = new remoteApi();
        vrep.simxFinish(-1); // close all opened connections in case

        clientID = vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        if (clientID != -1) {
            Toast.makeText(this, R.string.toast_connectionToRemoteApiSuccessful, Toast.LENGTH_SHORT).show();

            IntW object = new IntW(1);

            vrep.simxGetObjectHandle(clientID, objectName, object, remoteApi.simx_opmode_oneshot_wait);
            Toast.makeText(this, "Object value = " + object.getValue(), Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, R.string.toast_connectionToRemoteApiFailed, Toast.LENGTH_SHORT).show();
        }
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
        }
    }

    private void stopSensors() {
        isSensorsStarted = false;
        mSensorManager.unregisterListener(this);
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
    }

    // =============================================================
    //                      V-REP METHODS
    // =============================================================

    // Obtains the handle of the camera object in V-REP
    private int getHandleArmCamera() {
        IntW handleArmCamera = new IntW(1);

        vrep.simxGetObjectHandle(clientID,
                                nameArmCamera, // name of the camera object
                                handleArmCamera, // handle of the camera object
                                remoteApi.simx_opmode_blocking);

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

}
