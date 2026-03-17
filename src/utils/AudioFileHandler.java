package utils;

import java.io.*;

public class AudioFileHandler {

    public static void sendAudio(File file, DataOutputStream dos)
            throws IOException {

        FileInputStream fis = new FileInputStream(file);

        // This is crucial: we tell the receiver how big the file is
        dos.writeLong(file.length());

        byte[] buffer = new byte[4096];
        int bytesRead;

        while((bytesRead = fis.read(buffer)) != -1) {
            dos.write(buffer, 0, bytesRead);
        }

        fis.close();
    }

    public static void receiveAudio(DataInputStream dis, String fileName)
            throws IOException {

        File folder = new File("received_audio");

        if(!folder.exists()) {
            folder.mkdir();
        }

        FileOutputStream fos = new FileOutputStream("received_audio/" + fileName);

        // Read the length we sent in sendAudio
        long fileSize = dis.readLong();

        byte[] buffer = new byte[4096];
        int read;
        long remaining = fileSize;

        // Only read exactly the amount of bytes belonging to this file
        while(remaining > 0 && (read = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) > 0) {
            fos.write(buffer, 0, read);
            remaining -= read;
        }

        fos.close();
    }
}
