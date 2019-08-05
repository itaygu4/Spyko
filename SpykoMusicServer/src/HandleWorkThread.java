import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLOutput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;


public class HandleWorkThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    //private CopyOnWriteArrayList<CopyOnWriteArrayList<DatabaseItem>> database;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<DatabaseItem>> database;
    private CopyOnWriteArrayList<SocketsInfo> sockets;
    private long timeSent;
    private long timeReceived;

    public HandleWorkThread(Socket socket, ConcurrentHashMap<String, CopyOnWriteArrayList<DatabaseItem>> database, CopyOnWriteArrayList<SocketsInfo> sockets, long timeReceived) {
        this.database = database;
        this.socket = socket;
        this.sockets = sockets;
        this.timeReceived = timeReceived;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Couldn't open I/O on connection");
        }

    }

    @Override
    public void run() {
        String inputLine = "";
        try {
            if ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
        } catch (IOException e) {
            System.err.println("Couldn't read from connection");
        }
        String uid;

        if (inputLine.startsWith("New")) {
            String[] words = inputLine.split(" ");
            String myUid = words[1];
            for (DatabaseItem item : database.get(myUid)) {
                if (item.uid.equals(myUid))
                    continue;
                try {
                    PrintWriter outForNew = new PrintWriter(item.socket.getOutputStream(), true);
                    outForNew.println("New " + words[2]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (inputLine.startsWith("Pause")) {
            String[] words = inputLine.split(" ");
            String myUid = words[1];

            try{
                for (DatabaseItem item : database.get(myUid)) {
                    if (item.uid.equals(myUid)) {
                        item.pause();
                        continue;
                    }
                    try {
                        PrintWriter outForPause = new PrintWriter(item.socket.getOutputStream(), true);
                        outForPause.println("Pause");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                for (SocketsInfo data : sockets) {
                    if (data.getUid().equals(myUid))
                        data.setSocket(socket);
                }
                poker();
            }catch (NullPointerException e){e.printStackTrace();}
        }

        if (inputLine.startsWith("Continue")) {
            String[] words = inputLine.split(" ");
            String myUid = words[1];
            for (DatabaseItem item : database.get(myUid)) {
                if (item.uid.equals(myUid)) {
                    item.unPause();
                    continue;
                }
                try {
                    PrintWriter outForPause = new PrintWriter(item.socket.getOutputStream(), true);
                    outForPause.println("Continue");
                    System.out.println("Sent continue");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (SocketsInfo data : sockets) {
                if (data.getUid().equals(myUid))
                    data.setSocket(socket);
            }
            poker();
        }

        if (inputLine.startsWith("Seek")) {
            String[] words = inputLine.split(" ");
            String myUid = words[1];
            String time = words[2];
            for (DatabaseItem item : database.get(myUid)) {
                if (item.uid.equals(myUid)) {
                    continue;
                }
                try {
                    PrintWriter outForPause = new PrintWriter(item.socket.getOutputStream(), true);
                    outForPause.println("Seek " + time);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (SocketsInfo data : sockets) {
                if (data.getUid().equals(myUid))
                    data.setSocket(socket);
            }
            poker();
        }

        if (inputLine.startsWith("Disconnect")) {
            prettyPrinter();
            String[] words = inputLine.split(" ");
            String listeningToUid = words[1];
            String myUid = words[2];
            CopyOnWriteArrayList<DatabaseItem> value = database.get(listeningToUid);

            for (Iterator<DatabaseItem> listenersIterator = value.iterator(); listenersIterator.hasNext(); ) {
                DatabaseItem listener = listenersIterator.next();
                if (listener.uid.equals(myUid)) {
                    listenersIterator.remove();
                }
            }
            prettyPrinter();
        }

        if (inputLine.startsWith("Connect")) {
            String[] words = inputLine.split(" ");
            uid = words[1];
            String myUid = words[2];


            BufferedReader inAfterConnect = null;
            PrintWriter outAfterConnect = null;
            Socket newSocket;
            try {
                for (SocketsInfo data : sockets) {
                    if (data.getUid() != null && data.getUid().equals(uid)) {
                        newSocket = data.getSocket();
                        outAfterConnect = new PrintWriter(newSocket.getOutputStream(), true);
                        inAfterConnect = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
                        System.out.println(data);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (inAfterConnect == null)
                return;

            boolean pause = false;
            CopyOnWriteArrayList<DatabaseItem> arr = database.get(uid);
            DatabaseItem item = new DatabaseItem(myUid, socket);
            arr.add(item);
            if (!arr.get(0).playing)
                pause = true;

            try {

                outAfterConnect.println("VideoID");
                String videoID = inAfterConnect.readLine();             //get video id
                System.out.println(videoID);
                if (pause)
                    out.println("VideoID " + videoID + " pause");                      //send it to listener
                else
                    out.println("VideoID " + videoID + " continue");                      //send it to listener
                System.out.println("videoID sent");

                String input;
                input = in.readLine();                                          //get ready from listener
                /*timeReceived = System.currentTimeMillis();
                String [] differentWords = input.split(" ");
                timeSent = Long.valueOf(differentWords[1]);*/


                outAfterConnect.println("TimeMillis");                  //ask time millis
                String timeMillis = inAfterConnect.readLine();          //get time millis
                System.out.println("The time millis is: " + timeMillis);
                //String newTimeMillis = String.valueOf(Long.valueOf(timeMillis) + timeReceived - timeSent);
                String newTimeMillis = timeMillis;
                out.println("TimeMillis " + newTimeMillis);                //send time millis to listener
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Couldn't open I/O on connection");
            }

            poker();
        }

        if (inputLine.startsWith("Start")) {
            String[] words = inputLine.split(" ");
            String myUid = words[1];
            /*for (SocketsInfo data : sockets) {
                if (data.getUid().equals(myUid)) {
                    System.out.println("I returned");
                    return;
                }
            }*/
            CopyOnWriteArrayList<DatabaseItem> newArray = new CopyOnWriteArrayList<>();
            DatabaseItem item = new DatabaseItem(myUid, socket);
            newArray.add(item);
            item.playing = true;

            SocketsInfo dataStream = new SocketsInfo();
            dataStream.setSocket(socket);
            dataStream.setUid(myUid);
            sockets.add(dataStream);
            System.out.println(socket);
            database.put(myUid, newArray);
            System.out.println("started with " + myUid);

            poker();
        }

        if (inputLine.startsWith("End")) {
            String[] words = inputLine.split(" ");
            String myUid = words[1];
            ArrayList<SocketsInfo> toDelete = new ArrayList<>();
            Iterator<SocketsInfo> socketsIterator = sockets.iterator();
            while (socketsIterator.hasNext()){
                SocketsInfo data = socketsIterator.next();
                if (data.getUid().equals(myUid)) {
                    toDelete.add(data);
                    try {
                        data.getSocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            sockets.removeAll(toDelete);
            database.remove(myUid);

            System.out.println("Ending with " + myUid);
        }

        if (inputLine.startsWith("ChatStart")) {
            boolean addedChatSocket = false;

            String[] words = inputLine.split(" ");
            uid = words[1];
            String myUid = words[2];
            while (!addedChatSocket) {
                CopyOnWriteArrayList<DatabaseItem> arr = database.get(uid);
                if(arr == null || arr.size() == 0) {
                    try {
                        sleep(500);
                    }catch (InterruptedException e){}
                    continue;
                }
                for (Iterator<DatabaseItem> itemIterator = arr.iterator(); itemIterator.hasNext(); ) {
                    DatabaseItem connection = itemIterator.next();
                    if (connection.uid.equals(myUid)) {
                        connection.addChatSocket(socket);
                        System.out.println("Chat started!");
                        addedChatSocket = true;
                    }
                }
            }
        }

        if (inputLine.startsWith("ChatSend")) {
            String[] words = inputLine.split(" ");
            uid = words[1];
            String senderUid = words[2];
            String messageLength = words[3];
            int length = Integer.parseInt(messageLength);
            String message = "";
            while (length != 0) {
                try {
                    message += (char) in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                length--;

            }
            System.out.println(message);
            //String message = inputLine.substring(uid.length() + senderUid.length() + 3 + "chatSend".length());
            CopyOnWriteArrayList<DatabaseItem> listeners = database.get(uid);
            for (DatabaseItem item : listeners) {
                if (item.uid.equals(senderUid))
                    continue;
                try {
                    PrintWriter outForPause = new PrintWriter(item.chatSocket.getOutputStream(), true);
                    outForPause.println(senderUid + " " + message.length() + " " + '\n' + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /*try {
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }*/


    }

    private void poker() {
        while (true) {
            try {
                sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            out.println("");
        }
    }

    private void prettyPrinter() {
        System.out.println("-----------------------------------------------");
        System.out.println("database: ");
        System.out.println();
        for(String key : database.keySet()){
            System.out.println(key + "||||" + database.get(key));
        }

    }


    


}
