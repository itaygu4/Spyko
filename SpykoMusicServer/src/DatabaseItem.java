import java.net.Socket;
import java.util.ArrayList;

public class DatabaseItem {
    public String uid;
    public Socket socket, chatSocket;
    public boolean playing;

    public DatabaseItem(String uid, Socket socket) {
        this.uid = uid;
        this.socket = socket;
    }

    public void addChatSocket(Socket chatSocket){
        this.chatSocket = chatSocket;
    }

    public void pause(){
        playing = false;
    }

    public void unPause(){
        playing = true;
    }
}
