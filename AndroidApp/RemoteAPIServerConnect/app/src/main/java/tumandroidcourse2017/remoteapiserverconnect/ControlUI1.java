package tumandroidcourse2017.remoteapiserverconnect;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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

    // Sensors and data
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean isSensorsStarted;
    // Accelerometer
    private int tiltLeftRight;
    private int tiltUpDown;
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

        Button buttonToggleGripper = (Button) findViewById(R.id.btn_toggleGripper);
        buttonToggleGripper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendGripperData(1);
                        break;
                    case MotionEvent.ACTION_UP:
                        sendGripperData(0);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

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
                Accelerometer accelerometer = new Accelerometer(event, accelerometerLastUpdateTime);
                accelerometer.calculateRotation();

                tiltLeftRight = accelerometer.getTiltLeftRight();
                tiltUpDown = accelerometer.getTiltUpDown();
                accelerometerLastUpdateTime = accelerometer.getLastUpdateTime();

                sendMovementData();
                break;
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

    private void sendMovementData() {
        try {
            Socket clientSocket = getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(getString(R.string.msg_movementdata) + '\n');

            outToServer.writeBytes(tiltLeftRight + "" + '\n');
            outToServer.writeBytes(tiltUpDown + "" + '\n');

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendGripperData(int gripperStatus) {
        try {
            Socket clientSocket = getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(getString(R.string.msg_gripperdata) + '\n');

            outToServer.writeBytes(gripperStatus + "" + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
