package tumandroidcourse2017.remoteapiserverconnect;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
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
        Toast toast = Toast.makeText(MainActivity.this, "Connecting to "+ip+":"+port, Toast.LENGTH_LONG);
        toast.show();
    }


}
