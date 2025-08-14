package UserActivityMonitoringSystem;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import java.util.concurrent.TimeUnit;

public class ActivityTracker {

    private static String lastWindowTitle = "";

    public static void logActiveWindow() {
        final User32 user32 = User32.INSTANCE;

        while (!Main.shouldShutdown) {
            try {
                HWND hwnd = user32.GetForegroundWindow();
                if (hwnd != null) {
                    char[] windowText = new char[512];
                    user32.GetWindowText(hwnd, windowText, 512);
                    String currentWindowTitle = Native.toString(windowText).trim();

                    if (!currentWindowTitle.isEmpty() && !currentWindowTitle.equals(lastWindowTitle) && currentWindowTitle.length() > 1) {
                        LoggerUtil.logStructured("ActivityTracker", "Window Change", "Window Title: " + currentWindowTitle);
                        lastWindowTitle = currentWindowTitle;
                    }
                }
                
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LoggerUtil.log("❌ Error in ActivityTracker: " + e.getMessage());
                break;
            }
        }
    }
}
