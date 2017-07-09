package tumandroidcourse2017.remoteapiserverconnect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.getSocket;
public class ControlUI1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_ui1);
        Intent intent = getIntent();
        initWidgets(intent);



    }

    private void initWidgets(Intent intent){
        TextView infoTextView = (TextView) findViewById(R.id.infoTextView);
        infoTextView.setText("Connected to "+intent.getStringExtra(MainActivity.IPInfoPort));
        Button Startbutton = (Button) findViewById(R.id.Startbutton);
        Startbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(1);
            }
        });
        Button Pausebutton = (Button) findViewById(R.id.Pausebutton);
        Pausebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(2);
            }
        });
        Button Stopbutton = (Button) findViewById(R.id.Stopbutton);
        Stopbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(3);
            }
        });
    }

    private void sendCommand(int userinput){
        try {
            Socket clientSocket=getSocket();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes(userinput+""+'\n');
            Toast.makeText(ControlUI1.this, "FROM SERVER: " + inFromServer.readLine(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ControlUI1.this, e.toString(), Toast.LENGTH_SHORT).show();
        }


    }
}
