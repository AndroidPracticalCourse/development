package tumandroidcourse2017.remoteapiserverconnect;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by ninekaw9 on 7/9/2017.
 */

public class SocketHandler {
    private static Socket socket;

    public static synchronized Socket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(Socket socket){
        SocketHandler.socket = socket;
    }

    public static synchronized void unsetSocket(){
        SocketHandler.socket = null;
    }

    public static synchronized void disconnect(){
        Socket clientSocket = getSocket();
        try {
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes("REQSHUTDOWN" + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
