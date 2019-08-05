import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public static void main(String [] args) throws IOException{
        Socket socket = null;
        PrintWriter out = null;
        String host = "localhost";
        try{
            socket = new Socket(host, 5213);
            out = new PrintWriter(socket.getOutputStream(), true);
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
        out.println("Hello");
        out.close();
        socket.close();
    }
}
