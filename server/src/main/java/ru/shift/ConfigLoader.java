package ru.shift;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigLoader {

    private ConfigLoader() {
    }

    public static ServerConfig load(Path path) throws IOException {
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            properties.load(fis);
        }

        String portStr = properties.getProperty("port");
        if (portStr == null || portStr.isBlank()) {
            throw new IllegalStateException("Не указан параметр 'port' в " + path);
        }
        int port;

        try {
            port = Integer.parseInt(portStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Некорректное значение порта '" + portStr + "' в " + path, e
            );
        }

        return new ServerConfig(port);
    }
}
