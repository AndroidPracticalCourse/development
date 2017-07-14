package tumandroidcourse2017.remoteapiserverconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
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

public class MainActivity extends Activity {

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static String IPInfoPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }
    }

    private void initWidgets(){
        Button buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doConnect();
            }
        });
    }

    private void doConnect(){
        EditText editTextIP = (EditText) findViewById(R.id.editText_ip);
        EditText editTextPort = (EditText) findViewById(R.id.editText_port);
        String ip = editTextIP.getText().toString();
        int port = Integer.parseInt(editTextPort.getText().toString());
        Toast.makeText(MainActivity.this, "Connecting to " + ip + ":" + port, Toast.LENGTH_SHORT).show();

        if (validate(ip)) {
            try {
                Socket clientSocket;
                setSocket(new Socket(ip, port));
                clientSocket = getSocket();

                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes(getString(R.string.msg_remoteApiConnectReq) + '\n');
                String serverResponse = inFromServer.readLine();

                if (serverResponse.equals(getString(R.string.msg_remoteApiConnectAccept))) {
                    int clientID = Integer.parseInt(inFromServer.readLine());
                    System.out.println("clientID = " + clientID);
                    Toast.makeText(MainActivity.this, getString(R.string.toast_connToServerAccept), Toast.LENGTH_SHORT).show();

                    Intent controlui1 = new Intent(this, ControlUI1.class);
                    controlui1.putExtra(IPInfoPort, ip + ":" + port);
                    controlui1.putExtra(getString(R.string.str_clientID), clientID);
                    startActivity(controlui1);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.toast_handshakeError), Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, getString(R.string.toast_cantConnectError), Toast.LENGTH_SHORT).show();
                Log.d("connectdebug", e.toString());
            }
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.toast_invalidIPFormat), Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }


}
