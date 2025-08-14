package UserActivityMonitoringSystem;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static volatile boolean shouldShutdown = false;
    public static String currentUser = "";

    public static void main(String[] args) {
        System.out.println("🚀 Starting User Activity Monitoring System...");

        setupLogFile();
        LoggerUtil.log("🚀 Starting User Activity Monitoring System...");

        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Submitting threads using a consistent lambda approach to prevent compilation errors
        executor.submit(() -> ActivityTracker.logActiveWindow());
        
        List<String> foldersToWatch = new ArrayList<>();
        foldersToWatch.add("C:\\Users\\SATENDRA SINGH NEGI\\Documents");
        foldersToWatch.add("E:\\Desktop");
        executor.submit(() -> new FileAccessLogger(foldersToWatch).watch());
        
        executor.submit(() -> new LoginLogoutLogger().run());
        
        executor.submit(() -> new ApplicationUsageLogger().run());
        
        addShutdownHook(executor);

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            LoggerUtil.log("Main thread interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private static void setupLogFile() {
        String logFilePath = System.getProperty("user.home") + File.separator + "userActivityLog.txt";
        LoggerUtil.setLogFilePath(logFilePath);
    }
    
    private static void addShutdownHook(ExecutorService executor) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook initiated.");
            shouldShutdown = true;
            executor.shutdown();

            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
            
            LoggerUtil.log("✅ Termination Event: " + (currentUser.isEmpty() ? "System" : currentUser) + " logged out/system terminated.");
        }));
    }
}


