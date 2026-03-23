package ru.shift;

import javax.swing.*;

public class ClientMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientMain::start);
    }

    private static void start() {
        String host = promptHost();
        if (host == null) {
            return;
        }

        Integer port = promptPort();
        if (port == null) {
            return;
        }

        String nickname = promptNickname();
        if (nickname == null) {
            return;
        }

        ChatWindow window = new ChatWindow(host, port, nickname);
        window.setVisible(true);
    }

    private static String promptHost() {
        while (true) {
            String host = JOptionPane.showInputDialog(
                    null,
                    "Адрес сервера (host):",
                    "localhost"
            );

            if (host == null) {
                return null;
            }

            host = host.trim();
            if (host.isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Адрес сервера не может быть пустым",
                        "Ошибка ввода",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }

            return host;
        }
    }

    private static Integer promptPort() {
        while (true) {
            String portStr = JOptionPane.showInputDialog(
                    null,
                    "Порт (port):",
                    "5555"
            );

            if (portStr == null) {
                return null;
            }

            portStr = portStr.trim();
            if (portStr.isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Порт не может быть пустым",
                        "Ошибка ввода",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }

            int port;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Порт должен быть целым числом",
                        "Ошибка ввода",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }

            if (port < 1 || port > 65535) {
                JOptionPane.showMessageDialog(
                        null,
                        "Порт должен быть в диапазоне 1–65535",
                        "Ошибка ввода",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }

            return port;
        }
    }

    private static String promptNickname() {
        while (true) {
            String nickname = JOptionPane.showInputDialog(
                    null,
                    "Ваш ник (nickname):",
                    "User"
            );

            if (nickname == null) {
                return null;
            }

            nickname = nickname.trim();
            if (nickname.isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Ник не может быть пустым",
                        "Ошибка ввода",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }

            return nickname;
        }
    }
}
