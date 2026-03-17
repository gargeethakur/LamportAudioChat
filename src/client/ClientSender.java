package client;

import lamport.LamportClock;
import utils.AudioFileHandler;

import java.io.*;
import java.net.Socket;

public class ClientSender {

    public static void sendFile(File file, LamportClock clock) {

        try {

            Socket socket = new Socket("localhost", 5000);

            DataOutputStream dos =
                    new DataOutputStream(socket.getOutputStream());

            clock.tick();
            int timestamp = clock.getTime();

            dos.writeUTF(file.getName());
            dos.writeInt(timestamp);

            AudioFileHandler.sendAudio(file, dos);

            System.out.println("File sent with timestamp: " + timestamp);

            dos.close();
            socket.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}

