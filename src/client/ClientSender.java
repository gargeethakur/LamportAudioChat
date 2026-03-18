package client;

import java.io.*;
import java.net.Socket;
import lamport.LamportClock;
import utils.AudioFileHandler;

public class ClientSender {

    public static String sendRequest(File file, String message, LamportClock clock) {
        String serverResponse = "No response";
        try (Socket socket = new Socket("localhost", 5000);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            clock.tick();
            int timestamp = clock.getTime();

            // 1. Send Metadata
            dos.writeUTF(file.getName());
            dos.writeInt(timestamp);
            dos.writeUTF(message); // Send the "Recognize this..." message

            // 2. Send Audio Bytes
            AudioFileHandler.sendAudio(file, dos);

            // 3. Wait for Server Response
            serverResponse = dis.readUTF();
            
            // Sync clock with the server's reply
            int responseTime = dis.readInt();
            clock.receiveAction(responseTime);

        } catch(Exception e) {
            e.printStackTrace();
            serverResponse = "Error: Server unreachable.";
        }
        return serverResponse;
    }
}

