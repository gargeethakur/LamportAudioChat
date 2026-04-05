package server;

import javax.swing.*;
import lamport.LamportClock;
import utils.AudioFileHandler;

public class ServerGUI {
    JFrame frame;
    JTextArea logArea;
    JLabel clockLabel;
    JLabel portLabel;
    private String lastFileName = "";
    LamportClock clock = new LamportClock();

    public ServerGUI() {
        int port = Integer.parseInt(System.getProperty("port", "5001"));

        frame = new JFrame("Lamport Chat Server - Port " + port);
        clockLabel = new JLabel("Logical Clock: 0");
        portLabel = new JLabel("Running on Port: " + port);
        logArea = new JTextArea();
        JButton playButton = new JButton("Play Last Received");
        JScrollPane scrollPane = new JScrollPane(logArea);

        clockLabel.setBounds(20, 20, 200, 30);
        portLabel.setBounds(220, 20, 180, 30);
        scrollPane.setBounds(20, 60, 350, 220);
        playButton.setBounds(20, 290, 350, 30);

        frame.add(clockLabel);
        frame.add(portLabel);
        frame.add(scrollPane);
        frame.add(playButton);

        frame.setSize(400, 380);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        playButton.addActionListener(e -> {
            if (!lastFileName.isEmpty()) AudioFileHandler.playAudio(lastFileName);
        });

        new Thread(new ServerReceiver(this, clock)).start();
    }

    public void setLastFileName(String fileName) { this.lastFileName = fileName; }
    public void updateClock() { clockLabel.setText("Logical Clock: " + clock.getTime()); }
    public void logEvent(String event) { logArea.append(event + "\n"); }

    public static void main(String[] args) { new ServerGUI(); }
}