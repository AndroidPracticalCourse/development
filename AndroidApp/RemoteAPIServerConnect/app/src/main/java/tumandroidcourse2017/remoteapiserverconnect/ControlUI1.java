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

import tumandroidcourse2017.remoteapiserverconnect.sensors.Accelerometer;
import tumandroidcourse2017.remoteapiserverconnect.sensors.Gyroscope;
import tumandroidcourse2017.remoteapiserverconnect.sensors.RotationVector;

import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.getSocket;

public class ControlUI1 extends Activity implements SensorEventListener {

    private final String nameArm = "IRB140_joint2#0";
    private final String nameWrist = "IRB140_joint5#0";
    private final String nameArmCamera = "Vision_sensor";
    private String nameSelComponent = nameArm; // stores the name of the arm or wrist component, depending on the selected mode

    // Sensors and data
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mRotation;
    private boolean isSensorsStarted;
    // Acccelerometer
    private float velocity;
    private long accelerometerLastUpdateTime = 0;
    private double[] lastAccelerometerValues = {0.0, 0.0, 0.0};
    private double startingVelocity = 0.0;
    private float rollAngle; // From gyroscope
    private float pitchAngle; // From gyroscope
    private float[] rotationVector; // From rotation vector

    // Logging
    private static final String TAG = ControlUI1.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_ui1);

        initWidgets();
        startSensors();
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
            if (nameSelComponent.equals(nameArm)) {
                nameSelComponent = nameWrist;
                selMode.setText(R.string.text_wrist);
            } else if (nameSelComponent.equals(nameWrist)) {
                nameSelComponent = nameArm;
                selMode.setText(R.string.text_arm);
            }
            System.out.println("nameSelComponent = " + nameSelComponent);
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

            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
        //mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);
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
            case Sensor.TYPE_ACCELEROMETER:
                Accelerometer accelerometer = new Accelerometer(event, accelerometerLastUpdateTime, lastAccelerometerValues, startingVelocity);
                velocity = accelerometer.calculateVelocity();
                accelerometerLastUpdateTime = accelerometer.getLastUpdateTime();
                lastAccelerometerValues = accelerometer.getLastAccelerometerValues();
                startingVelocity = accelerometer.getStartingVelocity();

                sendSensorData(Sensor.TYPE_ACCELEROMETER);
                break;
            /*
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
                */
            default:
                break;
        }

    }

    // =============================================================
    //                  SOCKET COMMUNICATION METHODS
    // =============================================================

    private void sendCommand(int userInput) {
        try {
            Socket clientSocket = getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            outToServer.writeBytes(getString(R.string.msg_simulation) + '\n');
            outToServer.writeBytes(userInput + "" + '\n');
            Toast.makeText(ControlUI1.this, "FROM SERVER: " + inFromServer.readLine(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendSensorData(int sensor) {
        try {
            Socket clientSocket = getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

            outToServer.writeBytes(getString(R.string.msg_sensordata) + '\n');
            outToServer.writeBytes(nameSelComponent + '\n');

            switch (sensor) {
                case Sensor.TYPE_ACCELEROMETER:
                    outToServer.writeBytes("accelerometer" + '\n');
                    outToServer.writeBytes(velocity + "" + '\n');
                    break;
                default :
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
