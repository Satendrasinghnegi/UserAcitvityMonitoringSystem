package UserActivityMonitoringSystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class LoginLogoutLogger implements Runnable {

    private String lastLogonId = "";

    @Override
    public void run() {
        while (!Main.shouldShutdown) {
            try {
                processLoginLogoutEvents();
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LoggerUtil.log("❌ Error in LoginLogoutLogger: " + e.getMessage());
                break;
            }
        }
    }

    private void processLoginLogoutEvents() {
        try {
            Process p = Runtime.getRuntime().exec("wevtutil qe Security \"/q:*[System[(EventID=4624 or EventID=4634)]]\" /f:text /c:5");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            StringBuilder fullOutput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fullOutput.append(line).append("\n");
            }
            
            String[] events = fullOutput.toString().split("Event\\[\\d+\\]\n");
            for (String eventText : events) {
                if (!eventText.trim().isEmpty()) {
                    processEvent(eventText);
                }
            }
            
            p.waitFor();
            reader.close();
        } catch (Exception e) {
            LoggerUtil.log("❌ Error getting security events: " + e.getMessage());
        }
    }
    
    private void processEvent(String eventText) {
        if (eventText.contains("Event ID: 4624") && eventText.contains("Logon Type: 2")) {
            int accountNameIndex = eventText.indexOf("New Logon:");
            if (accountNameIndex == -1) {
                return;
            }
            String subString = eventText.substring(accountNameIndex);
            
            int nameIndex = subString.indexOf("Account Name:");
            int domainIndex = subString.indexOf("Account Domain:");

            if (nameIndex != -1 && domainIndex != -1) {
                String username = subString.substring(nameIndex + "Account Name:".length(), domainIndex).trim();
                if (!username.isEmpty() && !username.equals("SYSTEM") && !username.equals("ANONYMOUS LOGON")) {
                    String logonId = getLogonId(eventText);
                    if (!logonId.isEmpty() && !logonId.equals(lastLogonId)) {
                        Main.currentUser = username;
                        LoggerUtil.logLogin(Main.currentUser);
                        
                        // Call the application logger immediately after a successful login
                        ApplicationUsageLogger.monitorApplicationUsage();
                        
                        lastLogonId = logonId;
                    }
                }
            }
        } else if (eventText.contains("Event ID: 4634")) {
            LoggerUtil.logStructured("LoginLogout", "User Logout", "Username: " + Main.currentUser);
            Main.currentUser = "";
        }
    }
    
    private String getLogonId(String eventText) {
        int logonIdIndex = eventText.indexOf("Logon ID:");
        if (logonIdIndex > 0) {
            String subString = eventText.substring(logonIdIndex);
            int endIndex = subString.indexOf("\n");
            if (endIndex > 0) {
                return subString.substring("Logon ID:".length(), endIndex).trim();
            }
        }
        return "";
    }
}
