package server;

import lamport.LamportClock;

import javax.swing.*;

public class ServerGUI {

    JFrame frame;
    JTextArea logArea;
    JLabel clockLabel;

    LamportClock clock = new LamportClock();

    public ServerGUI() {

        frame = new JFrame("Lamport Chat Server");

        clockLabel = new JLabel("Logical Clock: 0");
        logArea = new JTextArea();

        JScrollPane scrollPane = new JScrollPane(logArea);

        clockLabel.setBounds(20,20,200,30);
        scrollPane.setBounds(20,60,350,250);

        frame.add(clockLabel);
        frame.add(scrollPane);

        frame.setSize(400,380);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        new Thread(new ServerReceiver(this, clock)).start();
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

