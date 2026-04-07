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

    // Modern Color Palette
    private static final Color BG_COLOR = new Color(240, 242, 245);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color ACCENT_BLUE = new Color(30, 64, 175); // Deeper blue for Server
    private static final Color TEXT_DARK = new Color(31, 41, 55);
    private static final Color TEXT_LIGHT = new Color(107, 114, 128);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);

    public ServerGUI() {
        int port = Integer.parseInt(System.getProperty("port", "5001"));

        frame = new JFrame("Admin Console — Port " + port);
        frame.setSize(460, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(BG_COLOR);
        frame.setLayout(new BorderLayout(0, 0));

        // ── MODERN HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel title = new JLabel("Voice Chat Server");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT_BLUE);

        portLabel = new JLabel("PORT " + port);
        portLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        portLabel.setForeground(TEXT_LIGHT);

        header.add(title, BorderLayout.WEST);
        header.add(portLabel, BorderLayout.EAST);

        // ── STATUS & CLOCK BAR ──
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        statusBar.setOpaque(false);

        statusLabel = new JLabel(" ● Server Online ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLabel.setForeground(SUCCESS_GREEN);

        clockLabel = new JLabel("Logical Clock: T=0");
        clockLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clockLabel.setForeground(TEXT_DARK);

        statusBar.add(statusLabel);
        statusBar.add(new JLabel("|")); // Separator
        statusBar.add(clockLabel);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(header, BorderLayout.NORTH);
        topContainer.add(statusBar, BorderLayout.CENTER);

        // ── LOG AREA (CLEAN WHITE) ──
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12)); // Technical font for logs
        logArea.setBackground(CARD_BG);
        logArea.setForeground(TEXT_DARK);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(10, 15, 10, 15));
        logArea.setText("System: Server initialized. Waiting for incoming data...\n\n");

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new MatteBorder(1, 0, 1, 0, BORDER_COLOR));

        // ── ACTION PANEL ──
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        actionPanel.setBackground(CARD_BG);
        actionPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        JButton playButton = new JButton("Play Last Recording");
        styleServerButton(playButton, ACCENT_BLUE);

        actionPanel.add(playButton);

        // ── ASSEMBLE ──
        frame.add(topContainer, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(actionPanel, BorderLayout.SOUTH);

        // ── LOGIC INTEGRATION ──
        playButton.addActionListener(e -> {
            if (!lastFileName.isEmpty()) {
                AudioFileHandler.playAudio(lastFileName);
            } else {
                logEvent("System: No audio received yet.");
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Start Receiver
        new Thread(new ServerReceiver(this, clock)).start();
    }

    private void styleServerButton(JButton btn, Color theme) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(theme);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 25, 10, 25));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }

    // Methods for ServerReceiver to interact with GUI
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