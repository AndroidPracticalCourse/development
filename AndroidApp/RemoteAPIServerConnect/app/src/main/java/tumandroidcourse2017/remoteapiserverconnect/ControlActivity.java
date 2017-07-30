package tumandroidcourse2017.remoteapiserverconnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


import tumandroidcourse2017.remoteapiserverconnect.sensors.Accelerometer;

import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.getSocket;

public class ControlActivity extends Activity implements SensorEventListener {

    // Widgets and status variables
    private TextView mTextTiltLeftRight;
    private TextView mTextTiltUpDown;
    private View mTextObjectColor;
    private ImageButton mButtonControlLeft;
    private ImageButton mButtonControlDown;
    private ImageButton mButtonControlUp;
    private ImageButton mButtonControlRight;
    private boolean isSensorControlEnabled = true;
    private boolean isConnectionError=false;

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean isSensorsStarted;
    // Accelerometer data
    private int tiltLeftRight;
    private int tiltUpDown;
    private long accelerometerLastUpdateTime = 0;

    // Logging
    private static final String TAG = ControlActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initWidgets();
        startSensors();
        startRequestColorThread();
    }

    //things for repeatedly request color in thread, backgrounded, repeatedly
    private static boolean ActivityIsActive;

    @Override
    public void onStart() {
        super.onStart();
        ActivityIsActive = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        ActivityIsActive = false;
    }

    private void startRequestColorThread(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //prevent crashing if closing this activity
                if(ActivityIsActive){
                    requestSensorImageData();
                    receiveSensorImageData();
                    handler.postDelayed(this,500);
                }
            }
        }, 500);
    }

    private void initWidgets(){
        // Connection details header
        TextView textConnDetails = (TextView) findViewById(R.id.data_connDetails);
        textConnDetails.setText(getIntent().getStringExtra(MainActivity.IPInfoPort));

        // Simulation settings
        final ImageView buttonStartSimulation = (ImageView) findViewById(R.id.btn_startSimulation);
        final ImageView buttonPauseSimulation = (ImageView) findViewById(R.id.btn_pauseSimulation);
        final ImageView buttonStartSimulation2 = (ImageView) findViewById(R.id.btn_startSimulation2);
        final ImageView buttonStopSimulation = (ImageView) findViewById(R.id.btn_stopSimulation);
        buttonStartSimulation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendSimulationData(1);
                buttonStartSimulation.setVisibility(View.GONE);
                buttonPauseSimulation.setVisibility(View.VISIBLE);
                buttonStopSimulation.setVisibility(View.VISIBLE);
            }
        });
        buttonPauseSimulation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendSimulationData(2);
                buttonStartSimulation2.setVisibility(View.VISIBLE);
                buttonPauseSimulation.setVisibility(View.GONE);
            }
        });
        buttonStartSimulation2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendSimulationData(1);
                buttonStartSimulation2.setVisibility(View.GONE);
                buttonPauseSimulation.setVisibility(View.VISIBLE);
            }
        });
        buttonStopSimulation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendSimulationData(3);
                buttonStartSimulation.setVisibility(View.VISIBLE);
                buttonStartSimulation2.setVisibility(View.GONE);
                buttonPauseSimulation.setVisibility(View.GONE);
                buttonStopSimulation.setVisibility(View.GONE);
            }
        });

        // Control mode settings
        final TextView toggleControlMode = (TextView) findViewById(R.id.data_selMode);
        toggleControlMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isSensorControlEnabled) {
                    isSensorControlEnabled = false;
                    toggleControlMode.setText(getString(R.string.text_button));
                    Toast.makeText(ControlActivity.this, getString(R.string.toast_buttonModeEnabled), Toast.LENGTH_SHORT).show();
                    processAccelerometerData(); // send a last (0,0) to stop the movement of the arm
                    stopSensors();
                } else {
                    isSensorControlEnabled = true;
                    toggleControlMode.setText(getString(R.string.text_sensor));
                    Toast.makeText(ControlActivity.this, getString(R.string.toast_sensorModeEnabled), Toast.LENGTH_SHORT).show();
                    startSensors();
                }
                toggleDisplayOfWidgets();
            }
        });

        // Received data
        mTextTiltLeftRight = (TextView) findViewById(R.id.data_tiltLeftRight);
        mTextTiltUpDown = (TextView) findViewById(R.id.data_tiltUpDown);
        mTextObjectColor = (View) findViewById(R.id.data_colorObject);

        // Button control settings
        mButtonControlLeft = (ImageButton) findViewById(R.id.btn_controlLeft);
        mButtonControlUp = (ImageButton) findViewById(R.id.btn_controlUp);
        mButtonControlRight= (ImageButton) findViewById(R.id.btn_controlRight);
        mButtonControlDown = (ImageButton) findViewById(R.id.btn_controlDown);

        mButtonControlLeft.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendMovementDataViaButton("L");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendMovementDataViaButton("STOP");
            }

            return false;
            }
        });
        mButtonControlUp.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendMovementDataViaButton("U");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendMovementDataViaButton("STOP");
            }

            return false;
            }
        });
        mButtonControlRight.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendMovementDataViaButton("R");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendMovementDataViaButton("STOP");
            }

            return false;
            }
        });
        mButtonControlDown.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendMovementDataViaButton("D");
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                sendMovementDataViaButton("STOP");
            }

            return false;
            }
        });

        // Gripper settings
        final Button buttonToggleGripper = (Button) findViewById(R.id.btn_toggleGripper);
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
    //                  CONNECTION PROBLEM DIALOG
    // =============================================================

    private void showErrorDialog(String ErrorMsg){
        isConnectionError=true;
        TextView textConnStatus = (TextView) findViewById(R.id.data_connStatus);
        textConnStatus.setText(R.string.text_disconnected);
        textConnStatus.setTextColor(getResources().getColor(R.color.red));

        AlertDialog alertDialog = new AlertDialog.Builder(ControlActivity.this).create();
        alertDialog.setTitle("Connection Error!");
        alertDialog.setMessage(ErrorMsg);
        // Alert dialog button
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "EXIT",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Alert dialog action goes here
                        // onClick button code here
                        dialog.dismiss();// use dismiss to cancel alert dialog
                        closeActivity();
                    }
                });
        alertDialog.show();

    }

    private void closeActivity(){
        this.finish();
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

                getAccelerometerData(accelerometer);
                processAccelerometerData();
                break;
            default:
                break;
        }
    }

    // =============================================================
    //                          HELPER METHODS
    // =============================================================

    // Called when the control mode is toggled between Button and Sensor
    // The relevant information will be highlighted accordingly depending on the selected mode
    private void toggleDisplayOfWidgets() {
        if (isSensorsStarted) { // Sensor mode
            mTextTiltLeftRight.setTextColor(getResources().getColor(R.color.blue));
            mTextTiltUpDown.setTextColor(getResources().getColor(R.color.blue));
            mButtonControlLeft.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
            mButtonControlUp.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
            mButtonControlRight.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
            mButtonControlDown.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
        } else { // Button mode
            mButtonControlLeft.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            mButtonControlUp.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            mButtonControlRight.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            mButtonControlDown.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            mTextTiltLeftRight.setTextColor(getResources().getColor(R.color.lightGray));
            mTextTiltUpDown.setTextColor(getResources().getColor(R.color.lightGray));
        }
    }

    private void getAccelerometerData(Accelerometer accelerometer) {
        tiltLeftRight = accelerometer.getTiltLeftRight();
        tiltUpDown = accelerometer.getTiltUpDown();
        accelerometerLastUpdateTime = accelerometer.getLastUpdateTime();
    }

    private void processAccelerometerData() {
        if(tiltLeftRight != 0 && tiltUpDown != 0){
            mTextTiltLeftRight.setText("" + (tiltLeftRight * -1));
            mTextTiltUpDown.setText("" + tiltUpDown);

            // when the control mode is switched to 'Button', send zeroes to stop the arm movement
            if (!isSensorControlEnabled) {
                tiltLeftRight = 1;
                tiltUpDown = 1;
            }
            if(!isConnectionError){
                sendMovementData();
            }

        }
    }

    // =============================================================
    //                  SOCKET COMMUNICATION METHODS
    // =============================================================

    private void sendSimulationData(int userInput) {
        try {
            Socket clientSocket = getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(getString(R.string.msg_simulation) + '\n');

            outToServer.writeBytes(userInput + "" + '\n');
            if (userInput == 1) {
                Toast.makeText(this, getString(R.string.toast_simulationStarted), Toast.LENGTH_SHORT).show();
            } else if (userInput == 2) {
                Toast.makeText(this, getString(R.string.toast_simulationPaused), Toast.LENGTH_SHORT).show();
            } else if (userInput == 3) {
                Toast.makeText(this, getString(R.string.toast_simulationStopped), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Failure to transmit: Simulation data");
        }
    }

    private void sendMovementData() {
        try {
            Socket clientSocket = getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(getString(R.string.msg_movementdata) + '\n');

            outToServer.writeBytes(tiltLeftRight + "" + '\n');
            outToServer.writeBytes(tiltUpDown + "" + '\n');
        }  catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Failure to transmit: sensor data");
        }
    }

    private void sendMovementDataViaButton(String direction) {
        if (!isSensorControlEnabled) {
            try {
                Socket clientSocket = getSocket();
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes(getString(R.string.msg_movementdataviabutton) + '\n');
                outToServer.writeBytes(direction + '\n');
            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog("Failure to transmit: movement button data");
            }
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
            showErrorDialog("Failure to transmit: Grip data");
        }
    }

    private void requestSensorImageData(){
        try {
            Socket clientSocket = getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes(getString(R.string.msg_reqcolordata) + '\n');

        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Failure to transmit: request sensor data");
        }
    }

    private void receiveSensorImageData() {
        try {
            Socket clientSocket = getSocket();
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            int r = Integer.parseInt(inFromServer.readLine());
            int g = Integer.parseInt(inFromServer.readLine());
            int b = Integer.parseInt(inFromServer.readLine());

            int color = Color.rgb(r, g, b);
            mTextObjectColor.setBackgroundColor(color);
            //mTextObjectColor.setText(r + ", " + g + ", " + b);
            //mTextObjectColor.setTextColor(color);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Failure to receive: color data");
        }
    }


}