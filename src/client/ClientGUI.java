package client;

import java.io.File;
import javax.swing.*;
import lamport.LamportClock;

public class ClientGUI {
    private static LamportClock clock = new LamportClock();
    private static JTextArea log = new JTextArea();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Lamport Chat Client");

        JTextField inputField = new JTextField(" ");
        JButton selectButton = new JButton("Select Audio");
        JButton sendButton = new JButton("Send to Server");
        JScrollPane scroll = new JScrollPane(log);

        inputField.setBounds(50, 20, 320, 30);
        selectButton.setBounds(50, 60, 150, 30);
        sendButton.setBounds(220, 60, 150, 30);
        scroll.setBounds(50, 110, 320, 230);

        frame.add(inputField); frame.add(selectButton);
        frame.add(sendButton); frame.add(scroll);

        frame.setSize(440, 420);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        final File[] selectedFile = new File[1];

        selectButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                log.append("System: File ready - " + selectedFile[0].getName() + "\n");
            }
        });

        sendButton.addActionListener(e -> {
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please select an audio file first!");
                return;
            }
            String userMsg = inputField.getText();
            log.append("You: " + userMsg + "\n");
            
            // This now waits for a response from the server
            String response = ClientSender.sendRequest(selectedFile[0], userMsg, clock);
            log.append("Server: " + response + "\n");
            
            log.append("----------------------------\n");
        });
    }
}
