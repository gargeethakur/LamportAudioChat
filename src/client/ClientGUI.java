package client;

import lamport.LamportClock;
import javax.swing.*;
import java.io.File;

public class ClientGUI {

    // Use the actual LamportClock class you created
    private static LamportClock clock = new LamportClock();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Lamport Chat Client");

        JButton selectButton = new JButton("Select Audio");
        JButton sendButton = new JButton("Send");
        JTextArea log = new JTextArea();

        selectButton.setBounds(50, 50, 150, 30);
        sendButton.setBounds(220, 50, 150, 30);
        log.setBounds(50, 120, 320, 200);

        frame.add(selectButton);
        frame.add(sendButton);
        frame.add(log);

        frame.setSize(420, 400);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        final File[] selectedFile = new File[1];

        selectButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                // Tick the clock for the local event
                clock.tick(); 
                log.append("Selected: " + selectedFile[0].getName() + 
                           " | LC = " + clock.getTime() + "\n");
            }
        });

        sendButton.addActionListener(e -> {
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please select a file first!");
                return;
            }

            // Use the ClientSender helper to handle the transmission
            // This internally handles the port 5000 and AudioFileHandler logic
            ClientSender.sendFile(selectedFile[0], clock);
            
            log.append("File sent: " + selectedFile[0].getName() + 
                       " | LC = " + clock.getTime() + "\n");
        });
    }
}
