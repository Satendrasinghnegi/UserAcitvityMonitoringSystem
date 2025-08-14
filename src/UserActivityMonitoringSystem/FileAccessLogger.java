package UserActivityMonitoringSystem;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import static java.nio.file.StandardWatchEventKinds.*;

public class FileAccessLogger {

    private final List<String> foldersToWatch;

    public FileAccessLogger(List<String> foldersToWatch) {
        this.foldersToWatch = foldersToWatch;
    }

    public void watch() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();

            for (String folder : foldersToWatch) {
                Path path = Paths.get(folder);
                if (Files.exists(path) && Files.isDirectory(path)) {
                    path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    LoggerUtil.logStructured("FileAccess", "Watching Folder", "Path: " + path.toString());
                } else {
                    LoggerUtil.log("❌ FileAccess: Directory not found or is not a directory: " + folder);
                }
            }

            WatchKey key;
            while (!Main.shouldShutdown && (key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    Path file = ((WatchEvent<Path>) event).context();
                    
                    // Translate the raw event kind into a more descriptive string
                    String eventDescription;
                    switch (kind.name()) {
                        case "ENTRY_CREATE":
                            eventDescription = "File Created";
                            break;
                        case "ENTRY_DELETE":
                            eventDescription = "File Deleted";
                            break;
                        case "ENTRY_MODIFY":
                            eventDescription = "File Modified";
                            break;
                        default:
                            eventDescription = "Unknown File Event";
                            break;
                    }

                    // Log the translated event
                    LoggerUtil.logStructured("FileAccess", eventDescription,
                        "Path: " + key.watchable().toString() + " | File: " + file.toString());
                }

                boolean valid = key.reset();
                if (!valid) {
                    LoggerUtil.log("FileAccess: Watch key is no longer valid, exiting.");
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            if (!Main.shouldShutdown) {
                LoggerUtil.log("❌ FileAccess Error: " + e.getMessage());
            }
        }
    }
}
