package tumandroidcourse2017.remoteapiserverconnect;

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
}
