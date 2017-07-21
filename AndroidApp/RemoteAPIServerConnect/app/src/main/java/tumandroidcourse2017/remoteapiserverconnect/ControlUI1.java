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

import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.getSocket;

public class ControlUI1 extends Activity implements SensorEventListener {

    private final String nameArmCamera = "Vision_sensor";
    private String selMode = "Arm"; // "Arm" or "Wrist"

    // Sensors and data
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean isSensorsStarted;
    // Accelerometer
    private int inclinationAngle;
    private long accelerometerLastUpdateTime = 0;

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
            TextView currModeSel = (TextView) findViewById(R.id.text_currModeSel);
            if (selMode.equals(getString(R.string.text_arm))) {
                currModeSel.setText(R.string.text_wrist);
                selMode = getString(R.string.text_wrist);
            } else if (selMode.equals(getString(R.string.text_wrist))) {
                currModeSel.setText(R.string.text_arm);
                selMode = getString(R.string.text_arm);
            }
            System.out.println("selMode = " + selMode);
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
                Accelerometer accelerometer = new Accelerometer(event, accelerometerLastUpdateTime, selMode);
                inclinationAngle = accelerometer.calculateRotation();
                accelerometerLastUpdateTime = accelerometer.getLastUpdateTime();

                sendSensorData();
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

    private void sendSensorData() {
        try {
            Socket clientSocket = getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(getString(R.string.msg_sensordata) + '\n');

            outToServer.writeBytes(selMode + "" + '\n');
            outToServer.writeBytes(inclinationAngle + "" + '\n');

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
