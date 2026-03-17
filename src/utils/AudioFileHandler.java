package utils;

import java.io.*;
import javax.sound.sampled.*;

public class AudioFileHandler {

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
        if(!folder.exists()) {
            folder.mkdir();
        }
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
                if (!file.exists()) {
                    System.err.println("FILE MISSING: " + file.getAbsolutePath());
                    return;
                }

                AudioInputStream in = AudioSystem.getAudioInputStream(file);
                AudioFormat baseFormat = in.getFormat();
                
                // Force convert to a standard format Java likes (PCM_SIGNED) 
                // This fixes the "playing but no sound" issue for many WAV types
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED, 
                    baseFormat.getSampleRate(), 16, 
                    baseFormat.getChannels(), 
                    baseFormat.getChannels() * 2, 
                    baseFormat.getSampleRate(), false);

                AudioInputStream din = AudioSystem.getAudioInputStream(targetFormat, in);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

                if (line != null) {
                    line.open(targetFormat);
                    line.start();
                    byte[] data = new byte[4096];
                    int nBytesRead;
                    while ((nBytesRead = din.read(data, 0, data.length)) != -1) {
                        line.write(data, 0, nBytesRead);
                    }
                    line.drain();
                    line.stop();
                    line.close();
                    din.close();
                }
            } catch (Exception e) {
                System.err.println("HARDWARE ERROR: Could not link to speakers.");
                e.printStackTrace();
            }
        }).start();
    }
}