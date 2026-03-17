package server;

import javax.swing.*;
import lamport.LamportClock;
import utils.AudioFileHandler;

public class ServerGUI {
    JFrame frame;
    JTextArea logArea;
    JLabel clockLabel;
    private String lastFileName = ""; 

    LamportClock clock = new LamportClock();

    public ServerGUI() {
        frame = new JFrame("Lamport Chat Server");
        clockLabel = new JLabel("Logical Clock: 0");
        logArea = new JTextArea();
        JButton playButton = new JButton("Play Last Received");
        JScrollPane scrollPane = new JScrollPane(logArea);

        clockLabel.setBounds(20,20,200,30);
        scrollPane.setBounds(20,60,350,220);
        playButton.setBounds(20, 290, 350, 30);

        frame.add(clockLabel);
        frame.add(scrollPane);
        frame.add(playButton);

        frame.setSize(400,380);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        playButton.addActionListener(e -> {
            if (lastFileName != null && !lastFileName.isEmpty()) {
                AudioFileHandler.playAudio(lastFileName);
            } else {
                logArea.append("No file to play yet.\n");
            }
        });

        new Thread(new ServerReceiver(this, clock)).start();
    }

    public void setLastFileName(String fileName) {
        this.lastFileName = fileName;
    }

    public void updateClock() {
        clockLabel.setText("Logical Clock: " + clock.getTime());
    }

    public void logEvent(String event) {
        logArea.append(event + "\n");
    }

    public static void main(String[] args) {
        new ServerGUI();
    }
}

