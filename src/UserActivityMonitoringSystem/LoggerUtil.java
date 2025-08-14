package UserActivityMonitoringSystem;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggerUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static String logFilePath;
    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private static final Thread loggerThread;

    static {
        loggerThread = new Thread(() -> {
            try (FileWriter writer = new FileWriter(logFilePath, true)) {
                while (true) {
                    String logMessage = logQueue.take();
                    writer.write(logMessage + System.lineSeparator());
                    writer.flush();
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Logger thread terminated unexpectedly.");
            }
        });
        loggerThread.setDaemon(true);
        loggerThread.start();
    }

    public static void setLogFilePath(String filePath) {
        logFilePath = filePath;
    }

    public static void log(String message) {
        String logMessage = String.format("[%s] - %s", LocalDateTime.now().format(FORMATTER), message);
        try {
            logQueue.put(logMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void logStructured(String component, String event, String details) {
        String logMessage = String.format("[%s] - Component: %s | Event: %s | Details: %s",
                LocalDateTime.now().format(FORMATTER), component, event, details);
        try {
            logQueue.put(logMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void logLogin(String username) {
        String logMessage = String.format("Username: %s Login Time: %s", username, LocalDateTime.now().format(FORMATTER));
        try {
            logQueue.put(logMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
