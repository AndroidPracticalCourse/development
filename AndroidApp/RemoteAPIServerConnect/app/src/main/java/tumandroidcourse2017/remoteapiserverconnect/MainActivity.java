package tumandroidcourse2017.remoteapiserverconnect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
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

public class MainActivity extends Activity implements View.OnClickListener, ToolTipView.OnToolTipViewClickedListener {

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static String IPInfoPort;

    private ToolTipRelativeLayout mToolTipRelativeLayout;
    private ToolTipView mHelpToolTipView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup header text font
        TextView tx_vrep = (TextView) findViewById(R.id.text_vrep);
        TextView tx_controller = (TextView) findViewById(R.id.text_controller);
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/PKMN RBYGSC.ttf");
        tx_vrep.setTypeface(customFont);
        tx_controller.setTypeface(customFont);

        // Set up tooltip
        mToolTipRelativeLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_main_tooltipRelativeLayout);
        findViewById(R.id.img_help).setOnClickListener(this);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
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
        } else if (id == R.id.button_connect) {
            doConnect();
        }
    }

    public void onClickShowAbout(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    @Override
    public void onToolTipViewClicked(final ToolTipView toolTipView) {
        if (mHelpToolTipView == toolTipView) {
            mHelpToolTipView = null;
        }
    }

    // =============================================================
    //                      HELPER METHODS
    // =============================================================

    private void addHelpToolTip() {
        ToolTip toolTip = new ToolTip()
                .withText(getString(R.string.text_tooltip))
                .withTextColor(getResources().getColor(R.color.white))
                .withColor(getResources().getColor(R.color.blue))
                .withAnimationType(ToolTip.AnimationType.NONE);

        mHelpToolTipView = mToolTipRelativeLayout.showToolTipForView(toolTip, findViewById(R.id.img_help));
        mHelpToolTipView.setOnToolTipViewClickedListener(this);
    }

    private void doConnect(){
        EditText editTextIP = (EditText) findViewById(R.id.input_ipAddress);
        EditText editTextPort = (EditText) findViewById(R.id.input_port);
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
                System.out.println("Writing some bytes");
                outToServer.writeBytes("REMOTEAPI_CONNECTREQ\n");
                System.out.println("Bytes written");
                String serverResponse = inFromServer.readLine();
                System.out.println("Server response read");

                if (serverResponse.equals(getString(R.string.msg_remoteApiConnectAccept))) {
                    System.out.println("Connection accepted");
                    String id = inFromServer.readLine();
                    System.out.println("id = " + id);
                    int clientID = Integer.parseInt(id);
                    System.out.println("clientID = " + clientID);
                    Toast.makeText(MainActivity.this, getString(R.string.toast_connToServerAccept), Toast.LENGTH_SHORT).show();

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
