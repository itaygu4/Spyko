import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    public static void main(String [] args){
        ConcurrentHashMap<String, CopyOnWriteArrayList<DatabaseItem>> database = new ConcurrentHashMap<>();
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(5213);
        }catch (IOException e){
            System.err.println("Could not listen on port 5213");
            System.exit(1);
        }
        Socket socket = null;
        CopyOnWriteArrayList<SocketsInfo> sockets = new CopyOnWriteArrayList<>();
        while (true){
            try {
                socket = serverSocket.accept();
                new HandleWorkThread(socket, database, sockets, System.currentTimeMillis()).start();
            }catch (IOException e){
                System.err.println("Accept failed");
                System.exit(1);
            }
        }
    }
}
