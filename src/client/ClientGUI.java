package client;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.*;
import lamport.LamportClock;

public class ClientGUI {
    private static LamportClock clock = new LamportClock();
    private static JTextArea log = new JTextArea();
    private static JLabel clockLabel;
    private static JLabel fileLabel;
    private static JLabel statusLabel;

    public static void main(String[] args) {
        // Ask for client ID
        String clientId = JOptionPane.showInputDialog(null,
            "Enter your Client ID (e.g. Client1, Client2):",
            "Client ID", JOptionPane.QUESTION_MESSAGE);
        if (clientId == null || clientId.isEmpty()) clientId = "Client1";
        final String finalClientId = clientId;

        // Colors
        Color bgColor = new Color(245, 247, 250);
        Color primaryBlue = new Color(37, 99, 235);
        Color lightBlue = new Color(219, 234, 254);
        Color white = Color.WHITE;
        Color textGray = new Color(75, 85, 99);
        Color borderGray = new Color(209, 213, 219);
        Color greenColor = new Color(34, 197, 94);

        JFrame frame = new JFrame("Voice Chat — " + finalClientId);
        frame.setSize(480, 580);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(bgColor);

        // ── TOP HEADER PANEL ──
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(primaryBlue);
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLabel = new JLabel("Voice Chat Client");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(white);

        JPanel clockPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        clockPanel.setOpaque(false);
        clockLabel = new JLabel("Clock: T=0");
        clockLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        clockLabel.setForeground(lightBlue);
        clockPanel.add(clockLabel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(clockPanel, BorderLayout.EAST);

        // ── CLIENT ID BADGE ──
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        badgePanel.setBackground(lightBlue);
        JLabel badgeLabel = new JLabel("  Connected as: " + finalClientId + "  ");
        badgeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        badgeLabel.setForeground(primaryBlue);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(white);
        badgeLabel.setBorder(new CompoundBorder(
            new LineBorder(primaryBlue, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        statusLabel = new JLabel("  Ready  ");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(white);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(greenColor);
        statusLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        badgePanel.add(badgeLabel);
        badgePanel.add(statusLabel);

        // ── CHAT LOG ──
        log.setEditable(false);
        log.setFont(new Font("Arial", Font.PLAIN, 13));
        log.setBackground(white);
        log.setForeground(textGray);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBorder(new EmptyBorder(10, 12, 10, 12));
        log.setText("Welcome, " + finalClientId + "! Select an audio file and send it.\n\n");

        JScrollPane scrollPane = new JScrollPane(log);
        scrollPane.setBorder(new MatteBorder(1, 0, 1, 0, borderGray));
        scrollPane.setBackground(white);

        // ── BOTTOM PANEL ──
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(bgColor);
        bottomPanel.setBorder(new EmptyBorder(12, 16, 16, 16));

        // File label
        fileLabel = new JLabel("No file selected");
        fileLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        fileLabel.setForeground(new Color(156, 163, 175));
        fileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Buttons row
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton selectButton = new JButton("Select Audio File");
        styleButton(selectButton, white, primaryBlue, primaryBlue);

        JButton sendButton = new JButton("Send to Server");
        styleButton(sendButton, white, primaryBlue, primaryBlue);
        sendButton.setBackground(primaryBlue);
        sendButton.setForeground(white);

        buttonRow.add(selectButton);
        buttonRow.add(sendButton);

        bottomPanel.add(fileLabel);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(buttonRow);

        // ── ASSEMBLE ──
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(badgePanel, BorderLayout.AFTER_LAST_LINE);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(badgePanel, BorderLayout.NORTH);
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        centerWrapper.add(bottomPanel, BorderLayout.SOUTH);
        centerWrapper.setBackground(bgColor);

        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(centerWrapper, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        final File[] selectedFile = new File[1];

        selectButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("WAV Audio Files", "wav"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                fileLabel.setText("File: " + selectedFile[0].getName());
                fileLabel.setForeground(new Color(37, 99, 235));
            }
        });

        sendButton.addActionListener(e -> {
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please select an audio file first!", "No File", JOptionPane.WARNING_MESSAGE);
                return;
            }

            statusLabel.setText("  Sending...  ");
            statusLabel.setBackground(new Color(234, 179, 8));
            sendButton.setEnabled(false);

            clock.tick();
            clockLabel.setText("Clock: T=" + clock.getTime());
            log.append("[T=" + clock.getTime() + "] " + finalClientId + ": Sending audio...\n");

            new Thread(() -> {
                String response = ClientSender.sendRequest(selectedFile[0], finalClientId + ": audio message", clock);
                SwingUtilities.invokeLater(() -> {
                    clockLabel.setText("Clock: T=" + clock.getTime());
                    log.append("[T=" + clock.getTime() + "] Server: " + response + "\n");
                    log.append("─────────────────────────────\n");
                    statusLabel.setText("  Ready  ");
                    statusLabel.setBackground(greenColor);
                    sendButton.setEnabled(true);
                    // Auto scroll to bottom
                    log.setCaretPosition(log.getDocument().getLength());
                });
            }).start();
        });
    }

    private static void styleButton(JButton btn, Color fg, Color bg, Color border) {
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setBorder(new CompoundBorder(
            new LineBorder(border, 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
    }
}