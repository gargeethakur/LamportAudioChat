package server;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import lamport.LamportClock;
import utils.AudioFileHandler;

public class ServerGUI {
    JFrame frame;
    JTextArea logArea;
    JLabel clockLabel;
    JLabel statusLabel;
    JLabel portLabel;
    private String lastFileName = "";
    LamportClock clock = new LamportClock();

    // Colors
    Color bgColor = new Color(245, 247, 250);
    Color primaryBlue = new Color(37, 99, 235);
    Color lightBlue = new Color(219, 234, 254);
    Color white = Color.WHITE;
    Color textGray = new Color(75, 85, 99);
    Color borderGray = new Color(209, 213, 219);
    Color greenColor = new Color(34, 197, 94);

    public ServerGUI() {
        int port = Integer.parseInt(System.getProperty("port", "5001"));

        frame = new JFrame("Voice Chat Server — Port " + port);
        frame.setSize(460, 520);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(bgColor);

        // ── HEADER ──
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 64, 175));
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLabel = new JLabel("Voice Chat Server");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(white);

        portLabel = new JLabel("Port: " + port);
        portLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        portLabel.setForeground(lightBlue);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(portLabel, BorderLayout.EAST);

        // ── STATUS BAR ──
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        statusBar.setBackground(lightBlue);

        statusLabel = new JLabel("  Online  ");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(white);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(greenColor);
        statusLabel.setBorder(new EmptyBorder(4, 10, 4, 10));

        clockLabel = new JLabel("Logical Clock: T=0");
        clockLabel.setFont(new Font("Arial", Font.BOLD, 13));
        clockLabel.setForeground(primaryBlue);

        statusBar.add(statusLabel);
        statusBar.add(clockLabel);

        // ── LOG AREA ──
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Arial", Font.PLAIN, 13));
        logArea.setBackground(white);
        logArea.setForeground(textGray);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        logArea.setText("Server started on port " + port + ". Waiting for clients...\n\n");

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new MatteBorder(1, 0, 1, 0, borderGray));

        // ── BOTTOM ──
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        bottomPanel.setBackground(bgColor);

        JButton playButton = new JButton("Play Last Received Audio");
        playButton.setFont(new Font("Arial", Font.BOLD, 13));
        playButton.setForeground(primaryBlue);
        playButton.setBackground(white);
        playButton.setOpaque(true);
        playButton.setBorder(new CompoundBorder(
            new LineBorder(primaryBlue, 1, true),
            new EmptyBorder(10, 24, 10, 24)
        ));
        playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playButton.setFocusPainted(false);

        bottomPanel.add(playButton);

        // ── ASSEMBLE ──
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(statusBar, BorderLayout.NORTH);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        centerWrapper.setBackground(bgColor);

        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(centerWrapper, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        playButton.addActionListener(e -> {
            if (!lastFileName.isEmpty()) AudioFileHandler.playAudio(lastFileName);
        });

        new Thread(new ServerReceiver(this, clock)).start();
    }

    public void setLastFileName(String fileName) { this.lastFileName = fileName; }

    public void updateClock() {
        SwingUtilities.invokeLater(() -> {
            clockLabel.setText("Logical Clock: T=" + clock.getTime());
        });
    }

    public void logEvent(String event) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(event + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) { new ServerGUI(); }
}