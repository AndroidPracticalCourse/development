package tumandroidcourse2017.remoteapiserverconnect;

import android.content.Context;
import android.content.Intent;
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
import java.util.regex.Pattern;

import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.getSocket;
import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.setSocket;

public class MainActivity extends AppCompatActivity {
    public static String IPInfoPort;
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
        if(validate(ip)){
            try {
                Socket clientSocket;
                setSocket(new Socket(ip, port));
                clientSocket=getSocket();
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes("REMOTEAPI_CONNECTREQ\n");
                String serverresponse = inFromServer.readLine();
                if(serverresponse.equals("REMOTEAPI_CONNECTACCEPT")){
                    Toast.makeText(MainActivity.this, "Connection to server accepted.", Toast.LENGTH_SHORT).show();
                    Intent controlui1 = new Intent(this, ControlUI1.class);
                    controlui1.putExtra(IPInfoPort,ip+":"+port);
                    startActivity(controlui1);

                }
                else {
                    Toast.makeText(MainActivity.this, "Handshake error.", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Error! Cannot Connect.", Toast.LENGTH_SHORT).show();
                Log.d("connectdebug",e.toString());
            }
        }else{
            Toast.makeText(MainActivity.this, "Invalid IP format", Toast.LENGTH_SHORT).show();
        }

    }

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }


}
