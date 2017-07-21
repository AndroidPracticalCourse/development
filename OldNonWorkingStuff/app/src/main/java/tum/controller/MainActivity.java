package tum.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import coppelia.IntW;
import coppelia.StringW;
import coppelia.remoteApi;

public class MainActivity extends Activity {

    private String nameArm = "IRB140#0";
    private EditText edittext_IP, edittext_Port;
    // Logging
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();
    }

    // =============================================================
    //                     INITIALISATION METHODS
    // =============================================================

    private void initWidgets() {
        // Connect button
        Button buttonConnect = (Button) findViewById(R.id.btn_connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initRemoteApi();
            }
        });
        EditText edittext_IP = (EditText) findViewById(R.id.editText_IP);
        EditText edittext_Port = (EditText) findViewById(R.id.editText_Port);
    }

    // Initialises the remote API
    private void initRemoteApi() {
        //String ip = edittext_IP.getText().toString();
        //Log.d("Test",ip);
        //int port = Integer.parseInt(edittext_Port.getText().toString());
        //Log.d("Test",""+port);
        remoteApi vrep = new remoteApi();
        vrep.simxFinish(-1); // close all opened connections in case

        int clientID = vrep.simxStart("192.168.0.156", 19997, true, true, 5000, 5);
        if (clientID != -1) {
            Toast.makeText(this, R.string.toast_connectionToRemoteApiSuccessful, Toast.LENGTH_SHORT).show();

            IntW object = new IntW(1);
            vrep.simxGetObjectHandle(clientID, nameArm, object, remoteApi.simx_opmode_oneshot_wait);
            Toast.makeText(this, "Object value = " + object.getValue(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, ControllerActivity.class);
            intent.putExtra(getString(R.string.str_clientID), clientID);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.toast_connectionToRemoteApiFailed, Toast.LENGTH_SHORT).show();
        }
    }
}
