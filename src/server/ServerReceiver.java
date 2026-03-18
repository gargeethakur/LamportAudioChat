package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import lamport.LamportClock;
import utils.AudioFileHandler;

public class ServerReceiver implements Runnable {
    private ServerGUI gui;
    private LamportClock clock;

    public ServerReceiver(ServerGUI gui, LamportClock clock) {
        this.gui = gui;
        this.clock = clock;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            gui.logEvent("AI Server ready...");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleChat(socket)).start();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleChat(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            String fileName = dis.readUTF();
            int senderTime = dis.readInt();
            dis.readUTF(); 
            
            clock.receiveAction(senderTime);
            gui.updateClock();

            AudioFileHandler.receiveAudio(dis, fileName);
            
            String transcript = AudioFileHandler.recognizeSpeech(fileName);
            gui.logEvent("AI Heard: '" + transcript + "'");

            String botResponse = generateResponse(transcript);
            
            clock.tick();
            dos.writeUTF(botResponse);
            dos.writeInt(clock.getTime());
            
            gui.logEvent("AI Replied: " + botResponse);
            AudioFileHandler.playAudio(fileName);

        } catch (Exception e) { e.printStackTrace(); }
    }

    private String generateResponse(String input) {
    if (input == null || input.isEmpty()) return "I couldn't hear you clearly.";
    
    try {
        String apiKey = "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";
        
        // Clean input properly
        String cleanInput = input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", " ")
            .replace("\r", " ")
            .replace("\t", " ");
        
        String requestBody = "{\"model\":\"llama-3.3-70b-versatile\","
            + "\"max_tokens\":100,"
            + "\"messages\":[{\"role\":\"user\",\"content\":\"" 
            + cleanInput + "\"}]}";

        System.out.println("Sending: " + requestBody);

        java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
            new java.net.URL("https://api.groq.com/openai/v1/chat/completions").openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.getOutputStream().write(requestBody.getBytes("UTF-8"));

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        java.io.BufferedReader br;
        if (responseCode == 200) {
            br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream()));
        } else {
            br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) response.append(line);
        br.close();

        System.out.println("Full Response: " + response.toString());

        if (responseCode == 200) {
            String raw = response.toString();
            int start = raw.indexOf("\"content\":\"") + 11;
            int end = raw.indexOf("\"", start);
            return raw.substring(start, end);
        }
        return "Error code: " + responseCode;

    } catch (Exception e) {
        return "Error: " + e.getMessage();
    }
}

}

