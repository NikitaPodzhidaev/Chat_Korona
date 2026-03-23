package ru.shift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Main {
    private static final String DEFAULT_SERVER_PROPERTIES_FILE = "server.properties";
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            Path configPath = Path.of(DEFAULT_SERVER_PROPERTIES_FILE);
            log.info("Загружаем конфигурацию сервера из файла: {}", configPath.toAbsolutePath());

            ServerConfig config = ConfigLoader.load(configPath);
            log.info("Конфигурация успешно загружена. Порт: {}", config.port());

            ChatServer server = new ChatServer(config.port());
            server.start();

        } catch (InvalidPathException e) {
            log.error("Некорректный путь к конфигу: '{}'", DEFAULT_SERVER_PROPERTIES_FILE, e);
        } catch (FileNotFoundException e) {
            log.error("Файл конфигурации не найден: {}", e.getMessage(), e);
        } catch (IOException e) {
            log.error("Ошибка при чтении конфигурации: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при запуске сервера", e);
        }
    }
}
