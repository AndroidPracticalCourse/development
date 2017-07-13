package tumandroidcourse2017.remoteapiserverconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.getSocket;

public class ControlUI1 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_ui1);
        Intent intent = getIntent();
        initWidgets(intent);
    }

    private void initWidgets(Intent intent){
        TextView textViewInfo = (TextView) findViewById(R.id.textView_info);
        textViewInfo.setText("Connected to " + intent.getStringExtra(MainActivity.IPInfoPort)); // TODO - change to append()
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
    }

    private void sendCommand(int userInput){
        try {
            Socket clientSocket = getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(userInput + "" + '\n');
            Toast.makeText(ControlUI1.this, "FROM SERVER: " + inFromServer.readLine(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ControlUI1.this, e.toString(), Toast.LENGTH_SHORT).show();
        }


    }
}
