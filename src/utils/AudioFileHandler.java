package utils;

import org.vosk.Model;
import org.vosk.Recognizer;
import java.io.*;
import javax.sound.sampled.*;

public class AudioFileHandler {
    private static Model model;

    static {
        try {
            // Ensure the folder 'model' is in your project root
            model = new Model("model"); 
        } catch (Exception e) {
            System.err.println("Vosk Model not found! Speech recognition will fail.");
        }
    }

    public static String recognizeSpeech(String fileName) {
        if (model == null) return "Error: Speech Model not loaded.";

        try (AudioInputStream ais = AudioSystem.getAudioInputStream(new File("received_audio/" + fileName))) {
            // Vosk works best with 16kHz, Mono, PCM_SIGNED
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            AudioInputStream resampled = AudioSystem.getAudioInputStream(format, ais);
            
            Recognizer recognizer = new Recognizer(model, 16000);
            byte[] buffer = new byte[4096];
            int nbytes;

            while ((nbytes = resampled.read(buffer)) >= 0) {
                recognizer.acceptWaveForm(buffer, nbytes); 
            }
            
            String result = recognizer.getFinalResult();
            if (result.contains("text\" : \"")) {
                return result.substring(result.indexOf("text\" : \"") + 9, result.lastIndexOf("\""));
            }
            return "";
        } catch (Exception e) {
            return "Speech Error: " + e.getMessage();
        }
    }

    public static void sendAudio(File file, DataOutputStream dos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        dos.writeLong(file.length());
        byte[] buffer = new byte[4096];
        int bytesRead;
        while((bytesRead = fis.read(buffer)) != -1) {
            dos.write(buffer, 0, bytesRead);
        }
        fis.close();
    }

    public static void receiveAudio(DataInputStream dis, String fileName) throws IOException {
        File folder = new File("received_audio");
        if(!folder.exists()) folder.mkdir();
        FileOutputStream fos = new FileOutputStream("received_audio/" + fileName);
        long fileSize = dis.readLong();
        byte[] buffer = new byte[4096];
        int read;
        long remaining = fileSize;
        while(remaining > 0 && (read = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) > 0) {
            fos.write(buffer, 0, read);
            remaining -= read;
        }
        fos.close();
    }

    public static void playAudio(String fileName) {
        new Thread(() -> {
            try {
                File file = new File("received_audio/" + fileName);
                AudioInputStream in = AudioSystem.getAudioInputStream(file);
                SourceDataLine line = AudioSystem.getSourceDataLine(in.getFormat());
                line.open(); line.start();
                byte[] data = new byte[4096];
                int nBytesRead;
                while ((nBytesRead = in.read(data, 0, data.length)) != -1) line.write(data, 0, nBytesRead);
                line.drain(); line.stop(); line.close();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}