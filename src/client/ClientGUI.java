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

    // Modern Color Palette
    private static final Color BG_COLOR = new Color(240, 242, 245);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color TEXT_DARK = new Color(31, 41, 55);
    private static final Color TEXT_LIGHT = new Color(107, 114, 128);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    private static final Color WARNING_AMBER = new Color(245, 158, 11);

    public static void main(String[] args) {
        String clientId = JOptionPane.showInputDialog(null,
            "Enter your Client ID:", "Sign In", JOptionPane.QUESTION_MESSAGE);
        if (clientId == null || clientId.isEmpty()) clientId = "User";
        final String finalClientId = clientId;

        JFrame frame = new JFrame("Chat - " + finalClientId);
        frame.setSize(420, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(BG_COLOR);
        frame.setLayout(new BorderLayout(0, 0));

        // ── CUSTOM HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel title = new JLabel("Voice Assistant");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_DARK);

        clockLabel = new JLabel("T=" + clock.getTime());
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        clockLabel.setForeground(ACCENT_BLUE);
        
        header.add(title, BorderLayout.WEST);
        header.add(clockLabel, BorderLayout.EAST);

        // ── STATUS INDICATOR ──
        statusLabel = new JLabel(" ● Online ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLabel.setForeground(SUCCESS_GREEN);
        statusLabel.setBorder(new EmptyBorder(0, 20, 5, 0));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(header, BorderLayout.NORTH);
        topContainer.add(statusLabel, BorderLayout.SOUTH);

        // ── CHAT LOG (SCROLLABLE) ──
        log.setEditable(false);
        log.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        log.setForeground(TEXT_DARK);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(log);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        // ── INPUT AREA (CARD) ──
        JPanel inputCard = new JPanel();
        inputCard.setLayout(new GridBagLayout());
        inputCard.setBackground(CARD_BG);
        inputCard.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
            new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        fileLabel = new JLabel("No audio file selected...");
        fileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        fileLabel.setForeground(TEXT_LIGHT);

        JButton selectButton = new JButton("Choose File");
        styleModernButton(selectButton, Color.WHITE, ACCENT_BLUE);

        JButton sendButton = new JButton("Send Message");
        styleModernButton(sendButton, Color.WHITE, ACCENT_BLUE);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        inputCard.add(fileLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 5);
        inputCard.add(selectButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 5, 0, 0);
        inputCard.add(sendButton, gbc);

        // ── ASSEMBLE ──
        frame.add(topContainer, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputCard, BorderLayout.SOUTH);

        // ── LOGIC (IDENTICAL TO YOUR ORIGINAL) ──
        final File[] selectedFile = new File[1];

        selectButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                fileLabel.setText("Selected: " + selectedFile[0].getName());
                fileLabel.setForeground(ACCENT_BLUE);
            }
        });

        sendButton.addActionListener(e -> {
            if (selectedFile[0] == null) {
                JOptionPane.showMessageDialog(frame, "Select a file first.");
                return;
            }

            statusLabel.setText(" ● Sending... ");
            statusLabel.setForeground(WARNING_AMBER);
            sendButton.setEnabled(false);

            clock.tick();
            clockLabel.setText("T=" + clock.getTime());
            log.append("You: Sending audio... [T=" + clock.getTime() + "]\n");

            new Thread(() -> {
                String response = ClientSender.sendRequest(selectedFile[0], finalClientId + ": audio", clock);
                SwingUtilities.invokeLater(() -> {
                    clockLabel.setText("T=" + clock.getTime());
                    log.append("Server: " + response + " [T=" + clock.getTime() + "]\n\n");
                    statusLabel.setText(" ● Online ");
                    statusLabel.setForeground(SUCCESS_GREEN);
                    sendButton.setEnabled(true);
                    log.setCaretPosition(log.getDocument().getLength());
                });
            }).start();
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void styleModernButton(JButton btn, Color fg, Color bg) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        // Flat look
        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }
}