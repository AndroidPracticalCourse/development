package tum.controller;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import coppelia.CharWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;
import tum.controller.helper.Duplex;
import tum.controller.sensors.Accelerometer;
import tum.controller.sensors.Gyroscope;
import tum.controller.sensors.RotationVector;

public class ControllerActivity extends Activity implements SensorEventListener {

    // V-REP variables
    private remoteApi vrep;
    private int clientID;
    private String nameArmCamera = "Vision_sensor";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        init();
        startSensors();
    }

    // =============================================================
    //                  INITIALISATION METHODS
    // =============================================================

    private void init() {
        vrep = new remoteApi();
        clientID = getIntent().getIntExtra(getString(R.string.str_clientID), -1);
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
