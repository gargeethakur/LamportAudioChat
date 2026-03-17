package server;

import lamport.LamportClock;
import utils.AudioFileHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerReceiver implements Runnable {

    private ServerGUI gui;
    private LamportClock clock;
    private static final int PORT = 5000; // Matches ClientSender.java port

    public ServerReceiver(ServerGUI gui, LamportClock clock) {
        this.gui = gui;
        this.clock = clock;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            gui.logEvent("Server listening on port " + PORT);

            while (true) {
                // Wait for an incoming client connection
                Socket socket = serverSocket.accept();
                
                // Process the file transfer in a separate thread
                new Thread(() -> handleIncomingFile(socket)).start();
            }
        } catch (IOException e) {
            gui.logEvent("Critical Server Error: " + e.getMessage());
        }
    }

    private void handleIncomingFile(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            
            // 1. Read metadata sent by ClientSender
            String fileName = dis.readUTF();
            int senderTimestamp = dis.readInt();

            // 2. Synchronize Lamport Clock
            // Rule: Update local clock based on the maximum of local vs. received
            clock.receiveAction(senderTimestamp);
            
            // 3. Update the UI
            gui.updateClock();
            gui.logEvent("Receiving: " + fileName + " | Remote LC: " + senderTimestamp + " | Sync LC: " + clock.getTime());

            // 4. Download and save the audio data
            AudioFileHandler.receiveAudio(dis, fileName);

            gui.logEvent("File Saved: " + fileName);
            
        } catch (IOException e) {
            gui.logEvent("Error receiving file: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

