package ru.shift;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class ChatWindow extends JFrame implements MessageListener {

    private final ChatClient chatClient;

    private final JTextArea messagesArea = new JTextArea();

    private final DefaultListModel<String> usersListModel = new DefaultListModel<>();
    private final JList<String> usersList = new JList<>(usersListModel);

    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Отправить");

    public ChatWindow(String host, int port, String nickname) {
        super("Чат – " + nickname);

        this.chatClient = new ChatClient(host, port, nickname, this);

        initUi();
        initActions();

        try {
            chatClient.connect();
            appendMessage("[SYSTEM] Подключено к " + host + ":" + port);
        } catch (Exception e) {
            appendMessage("[ERROR] Не удалось подключиться: " + e.getMessage());
        }
    }

    private void initUi() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);

        messagesArea.setEditable(false);
        messagesArea.setLineWrap(true);
        messagesArea.setWrapStyleWord(true);
        JScrollPane messagesScroll = new JScrollPane(messagesArea);

        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane usersScroll = new JScrollPane(usersList);
        usersScroll.setPreferredSize(new Dimension(150, 0));

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(messagesScroll, BorderLayout.CENTER);
        getContentPane().add(usersScroll, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                chatClient.disconnect();
                dispose();
                System.exit(0);
            }
        });
    }

    private void initActions() {
        sendButton.addActionListener(e -> sendCurrentText());
        inputField.addActionListener(e -> sendCurrentText());
    }

    private void sendCurrentText() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        chatClient.sendText(text);
        inputField.setText("");
    }

    private void appendMessage(String line) {
        SwingUtilities.invokeLater(() -> {
            messagesArea.append(line + "\n");
            messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
        });
    }

    private void updateUserList(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            usersListModel.clear();
            if (users != null) {
                for (String u : users) {
                    usersListModel.addElement(u);
                }
            }
        });
    }

    @Override
    public void onMessage(ChatMessage message) {
        if (message == null || message.getType() == null) {
            return;
        }

        MessageType type = message.getType();
        switch (type) {
            case CHAT -> {
                String ts = message.getTimestamp() != null ? message.getTimestamp() : "";
                String from = message.getFrom() != null ? message.getFrom() : "unknown";
                String text = message.getText() != null ? message.getText() : "";
                String line = String.format("[%s] %s: %s", ts, from, text);
                appendMessage(line);
            }
            case SYSTEM -> {
                String ts   = message.getTimestamp() != null ? message.getTimestamp() : "";
                String text = message.getText()      != null ? message.getText()      : "";
                String line = String.format("[SYSTEM][%s] %s", ts, text);
                appendMessage(line);

                if (message.getSystemCode() == SystemCode.NICKNAME_TAKEN) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                this,
                                text,
                                "Ошибка входа",
                                JOptionPane.WARNING_MESSAGE
                        );
                        chatClient.disconnect();
                        dispose();
                    });
                }
            }

            case USERS -> {
                updateUserList(message.getUsers());
            }
        }
    }

    @Override
    public void onError(String errorText) {
        appendMessage("[ERROR] " + errorText);
    }
}
