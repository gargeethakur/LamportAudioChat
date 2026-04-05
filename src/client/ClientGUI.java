package client;

import java.io.File;
import javax.swing.*;
import lamport.LamportClock;

public class ClientGUI {
    private static LamportClock clock = new LamportClock();
    private static JTextArea log = new JTextArea();
    private static JLabel clockLabel = new JLabel("Logical Clock: 0");

    public static void main(String[] args) {
        JFrame frame = new JFrame("Lamport Chat Client");

        JTextField inputField = new JTextField("Recognize the instrument...");
        JButton selectButton = new JButton("Select Audio");
        JButton sendButton = new JButton("Send to Server");
        JScrollPane scroll = new JScrollPane(log);

        inputField.setBounds(50, 20, 320, 30);
        selectButton.setBounds(50, 60, 150, 30);
        sendButton.setBounds(220, 60, 150, 30);
        clockLabel.setBounds(50, 100, 320, 30);
        scroll.setBounds(50, 140, 320, 200);

        frame.add(inputField);
        frame.add(selectButton);
        frame.add(sendButton);
        frame.add(clockLabel);
        frame.add(scroll);

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
            
            // Show clock before sending
            clock.tick();
            clockLabel.setText("Logical Clock: " + clock.getTime() + " [Sending...]");
            log.append("You [T=" + clock.getTime() + "]: " + userMsg + "\n");

            String response = ClientSender.sendRequest(selectedFile[0], userMsg, clock);
            
            // Show clock after receiving response
            clockLabel.setText("Logical Clock: " + clock.getTime() + " [Received]");
            log.append("Server [T=" + clock.getTime() + "]: " + response + "\n");
            
            log.append("----------------------------\n");
        });
    }
}