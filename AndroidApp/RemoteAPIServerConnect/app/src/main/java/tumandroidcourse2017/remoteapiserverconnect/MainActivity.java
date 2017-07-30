package tumandroidcourse2017.remoteapiserverconnect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

import java.io.*;
import java.net.*;
import java.util.regex.Pattern;

import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.getSocket;
import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.setSocket;
import static tumandroidcourse2017.remoteapiserverconnect.SocketHandler.unsetSocket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ToolTipView.OnToolTipViewClickedListener {

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static String IPInfoPort;

    private ToolTipRelativeLayout mToolTipRelativeLayout;
    private ToolTipView mHelpToolTipView;

    SharedPreferences sharedpreferences;
    EditText editTextIP;
    EditText editTextPort;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup actionbar
        setTitle("Connect to Server Adapter");

        // Setup header text font
        TextView textVrep = (TextView) findViewById(R.id.text_vrep);
        TextView textController = (TextView) findViewById(R.id.text_controller);
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/PKMN RBYGSC.ttf");
        textVrep.setTypeface(customFont);
        textController.setTypeface(customFont);

        // Set up tooltip
        mToolTipRelativeLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_main_tooltipRelativeLayout);
        findViewById(R.id.img_help).setOnClickListener(this);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //load saved ip and port
        editTextIP = (EditText) findViewById(R.id.input_ipAddress);
        editTextPort = (EditText) findViewById(R.id.input_port);
        SharedPreferences prefs = getSharedPreferences("IPandPORT", MODE_PRIVATE);
        String restoredText = prefs.getString("IP", null);
        if (restoredText != null) {
            editTextIP.setText(prefs.getString("IP",""));
            editTextPort.setText(prefs.getString("Port",""));
        }

    }

    // add about to actionbar
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

    // =============================================================
    //                      CLICK LISTENERS
    // =============================================================

    @Override
    public void onClick(final View view) {
        int id = view.getId();
        if (id == R.id.img_help) {
            if (mHelpToolTipView == null) {
                addHelpToolTip();
            } else {
                mHelpToolTipView.remove();
                mHelpToolTipView = null;
            }
        }
    }

    public void onClickHeader(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void onClickConnectButton(View view) {
        /*
        // To test the UI of ControlActivity
        Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra(IPInfoPort, "192.168.137.1:6789");
        intent.putExtra(getString(R.string.str_clientID), 0);
        startActivity(intent);
        */

        doConnect();
    }

    @Override
    public void onToolTipViewClicked(final ToolTipView toolTipView) {
        if (mHelpToolTipView == toolTipView) {
            mHelpToolTipView = null;
        }
    }

    //disconnect from server adapter when resuming to main activity
    public void onResume(){
        super.onResume();
        unsetSocket();
    }

    // =============================================================
    //                      HELPER METHODS
    // =============================================================

    private void addHelpToolTip() {
        ToolTip toolTip = new ToolTip()
                .withText(getString(R.string.text_tooltip))
                .withTextColor(getResources().getColor(R.color.white))
                .withColor(getResources().getColor(R.color.darkRed))
                .withAnimationType(ToolTip.AnimationType.NONE);

        mHelpToolTipView = mToolTipRelativeLayout.showToolTipForView(toolTip, findViewById(R.id.img_help));
        mHelpToolTipView.setOnToolTipViewClickedListener(this);
    }

    private void doConnect(){
         editTextIP = (EditText) findViewById(R.id.input_ipAddress);
         editTextPort = (EditText) findViewById(R.id.input_port);
        String ip = editTextIP.getText().toString();
        int port = Integer.parseInt(editTextPort.getText().toString());

        //save ip and port to sharedpreference
        sharedpreferences = getSharedPreferences("IPandPORT", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString("IP", ip);
        editor.putString("Port", ""+port);
        editor.commit();
        if (validate(ip)) {
            try {
                unsetSocket();
                Toast.makeText(MainActivity.this, "Connecting to " + ip + ":" + port, Toast.LENGTH_SHORT).show();
                Socket clientSocket;
                setSocket(new Socket(ip, port));
                clientSocket = getSocket();
                clientSocket.setSoTimeout(2000);

                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes(getString(R.string.msg_remoteApiConnectReq) + '\n');
                String serverResponse = inFromServer.readLine();

                if (serverResponse.equals(getString(R.string.msg_remoteApiConnectAccept))) {
                    String id = inFromServer.readLine();
                    int clientID = Integer.parseInt(id);
                    Toast.makeText(MainActivity.this, "Connection to " + ip + ":" + port + " accepted", Toast.LENGTH_SHORT).show();

                    Intent controlui1 = new Intent(this, ControlActivity.class);
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
