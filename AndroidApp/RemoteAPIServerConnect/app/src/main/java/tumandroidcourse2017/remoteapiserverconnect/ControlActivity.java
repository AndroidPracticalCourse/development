package tumandroidcourse2017.remoteapiserverconnect;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

import tumandroidcourse2017.remoteapiserverconnect.sensors.Accelerometer;

import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.getSocket;

public class ControlActivity extends Activity implements SensorEventListener {

    // Sensors and data
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean isSensorsStarted;
    private boolean isSensorControlEnabled;
    private boolean sentZeroMovementDataToStopRobotArmMovement = false;
    // Accelerometer
    private int tiltLeftRight;
    private int tiltUpDown;
    private long accelerometerLastUpdateTime = 0;

    // Logging
    private static final String TAG = ControlActivity.class.getSimpleName();

    // Widgets
    private TextView mTextTiltLeftRight;
    private TextView mTextTiltUpDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /*
        initWidgets();
        startSensors();
        */
    }

    /*
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.global_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.aboutMenu:
                Intent aboutui = new Intent(this, AboutActivity.class);
                startActivity(aboutui);
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    */

    private void initWidgets(){
        TextView textViewInfo = (TextView) findViewById(R.id.data_detailsIpPort);
        textViewInfo.setText(getIntent().getStringExtra(MainActivity.IPInfoPort));
        /*
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
         */

        final Button buttonToggleGripper = (Button) findViewById(R.id.btn_toggleGripper);
        buttonToggleGripper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sendGripperData(1);
                    //buttonToggleGripper.setBackgroundColor(getResources().getColor(R.color.blue));
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

        /*
        ToggleButton toggleSensorControl = (ToggleButton) findViewById(R.id.toggleSensorControl);
        toggleSensorControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if (isChecked) {
                   isSensorControlEnabled = true;
               } else {
                   isSensorControlEnabled = false;
               }
           }
       });
 */


        mTextTiltLeftRight = (TextView) findViewById(R.id.data_tiltLeftRight);
        mTextTiltUpDown = (TextView) findViewById(R.id.data_tiltUpDown);
        ImageButton btnControlLeft = (ImageButton) findViewById(R.id.btn_controlLeft);
        ImageButton btnControlUp = (ImageButton) findViewById(R.id.btn_controlUp);
        ImageButton btnControlRight = (ImageButton) findViewById(R.id.btn_controlRight);
        ImageButton btnControlDown = (ImageButton) findViewById(R.id.btn_controlDown);
        //btnControlLeft.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.MULTIPLY);
        //btnControlUp.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.MULTIPLY);
        //btnControlRight.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.MULTIPLY);
        //btnControlDown.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.MULTIPLY);

        btnControlLeft.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                sendMovementDataViaButton("L");
            }
            else if (event.getAction() == MotionEvent.ACTION_UP){
                sendMovementDataViaButton("STOP");
            }

            return false;
             }
        });
        btnControlUp.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                sendMovementDataViaButton("U");
            }
            else if (event.getAction() == MotionEvent.ACTION_UP){
                sendMovementDataViaButton("STOP");
            }

            return false;
            }
        });
        btnControlRight.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                sendMovementDataViaButton("R");
            }
            else if (event.getAction() == MotionEvent.ACTION_UP){
                sendMovementDataViaButton("STOP");
            }

            return false;
            }
        });
        btnControlDown.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                sendMovementDataViaButton("D");
            }
            else if (event.getAction() == MotionEvent.ACTION_UP){
                sendMovementDataViaButton("STOP");
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
                /*
                Accelerometer accelerometer = new Accelerometer(event, accelerometerLastUpdateTime);
                accelerometer.calculateRotation();

                getAccelerometerData(accelerometer);
                processAccelerometerData();
                 */
                break;
            default:
                break;
        }
    }

    // =============================================================
    //                          HELPER METHODS
    // =============================================================

    private void getAccelerometerData(Accelerometer accelerometer) {
        tiltLeftRight = accelerometer.getTiltLeftRight();
        tiltUpDown = accelerometer.getTiltUpDown();
        accelerometerLastUpdateTime = accelerometer.getLastUpdateTime();
    }

    private void processAccelerometerData() {
        if(tiltLeftRight != 0 && tiltUpDown != 0){
            mTextTiltLeftRight.setText("" + tiltLeftRight);
            mTextTiltUpDown.setText("" + tiltUpDown);

            if (isSensorControlEnabled) {
                sendMovementData();
            } else {
                if (!sentZeroMovementDataToStopRobotArmMovement) {
                    try {
                        Socket clientSocket = getSocket();
                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        outToServer.writeBytes(getString(R.string.msg_movementdata) + '\n');

                        outToServer.writeBytes(0 + "" + '\n');
                        outToServer.writeBytes(0 + "" + '\n');
                        sentZeroMovementDataToStopRobotArmMovement = true;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

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
            Toast.makeText(ControlActivity.this, "FROM SERVER: " + inFromServer.readLine(), Toast.LENGTH_SHORT).show();
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
            sentZeroMovementDataToStopRobotArmMovement=false;
        } catch (SocketException e) {
            TextView textConnStatus = (TextView) findViewById(R.id.data_connStatus);
            //TextView textConnDetails = (TextView) findViewById(R.id.data_detailsIpPort);
            textConnStatus.setText(R.string.text_disconnected);
            textConnStatus.setTextColor(getResources().getColor(R.color.red));
            //textConnDetails.setTextColor(getResources().getColor(R.color.red));
        } catch (IOException e) {
            e.printStackTrace();
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
        }
    }
}
