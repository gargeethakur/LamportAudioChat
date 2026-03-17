package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import lamport.LamportClock;
import utils.AudioFileHandler;

public class ServerReceiver implements Runnable {
    private ServerGUI gui;
    private LamportClock clock;
    private static final int PORT = 5000;

    public ServerReceiver(ServerGUI gui, LamportClock clock) {
        this.gui = gui;
        this.clock = clock;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            gui.logEvent("Server listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleIncomingFile(socket)).start();
            }
        } catch (IOException e) {
            gui.logEvent("Critical Server Error: " + e.getMessage());
        }
    }

    private void handleIncomingFile(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            String fileName = dis.readUTF();
            int senderTimestamp = dis.readInt();
            
            clock.receiveAction(senderTimestamp);
            gui.updateClock();
            
            gui.logEvent("Receiving: " + fileName);
            AudioFileHandler.receiveAudio(dis, fileName);
            
            gui.setLastFileName(fileName);
            gui.logEvent("File Saved: " + fileName);

            AudioFileHandler.playAudio(fileName);

        } catch (IOException e) {
            gui.logEvent("Error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}

