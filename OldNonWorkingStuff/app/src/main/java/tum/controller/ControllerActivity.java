package tum.controller;

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

    // V-REP objects
    private final String nameArm = "IRB140_link2#0";
    private final String nameWrist = "IRB140_link3#0";
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
        setContentView(R.layout.activity_controller);

        initData();
        initWidgets();

        startSensors();
    }

    // =============================================================
    //                  INITIALISATION METHODS
    // =============================================================

    private void initData() {
        vrep = new remoteApi();
        clientID = getIntent().getIntExtra(getString(R.string.str_clientID), -1);
    }

    // Initialises the widgets and the respective click handlers
    private void initWidgets() {
        Button buttonToggleMode = (Button) findViewById(R.id.btn_toggleMode);
        buttonToggleMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { // Toggles between arm and wrist mode; arm mode selected by default
                TextView selMode = (TextView) findViewById(R.id.text_currModeSel);
                if (selMode.toString().equals(getString(R.string.str_arm))) {
                    nameCurrSelComponent = nameWrist;
                    selMode.setText(R.string.str_wrist);
                } else if (selMode.toString().equals(getString(R.string.str_wrist))) {
                    nameCurrSelComponent = nameArm;
                    selMode.setText(R.string.str_arm);
                }
            }
        });

        Button buttonClaw = (Button) findViewById(R.id.btn_claw);
        // TODO - click handler for claw button
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
    }

    // =============================================================
    //                      V-REP METHODS
    // =============================================================

    // Sends any movement data from the device sensors to the arm/wrist object in the V-REP simulator
    private void sendDataToComponent() {
        IntW handleSelComponent = new IntW(1);

        vrep.simxGetObjectHandle(clientID, nameCurrSelComponent, handleSelComponent, remoteApi.simx_opmode_blocking);
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

}
