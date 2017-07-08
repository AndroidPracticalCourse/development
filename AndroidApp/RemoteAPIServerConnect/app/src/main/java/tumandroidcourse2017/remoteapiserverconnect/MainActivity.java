package tumandroidcourse2017.remoteapiserverconnect;

import android.content.Context;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.*;
import java.net.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }
    }

    private void initWidgets(){
        Button ConnectButton = (Button) findViewById(R.id.ConnectButton);
        ConnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doConnect();
            }
        });

    }

    private void doConnect(){
        EditText IPedittext = (EditText) findViewById(R.id.IPedittext);
        EditText PorteditText = (EditText) findViewById(R.id.PorteditText);
        String ip = IPedittext.getText().toString();
        int port = Integer.parseInt(PorteditText.getText().toString());
        Toast.makeText(MainActivity.this, "Connecting to "+ip+":"+port, Toast.LENGTH_SHORT).show();
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        try {
            Socket clientSocket = new Socket(ip, port);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Error! Cannot Connect.", Toast.LENGTH_SHORT).show();
            Log.d("connectdebug",e.toString());
        }
    }


}
