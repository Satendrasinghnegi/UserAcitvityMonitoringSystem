package UserActivityMonitoringSystem;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.Tlhelp32.PROCESSENTRY32;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Native;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class ApplicationUsageLogger implements Runnable {

    @Override
    public void run() {
        while (!Main.shouldShutdown) {
            monitorApplicationUsage();
            try {
                TimeUnit.MINUTES.sleep(60);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LoggerUtil.log("❌ Error in ApplicationUsageLogger: " + e.getMessage());
                break;
            }
        }
    }

    public static void monitorApplicationUsage() {
        HANDLE hProcessSnap = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new DWORD(0));
        if (hProcessSnap == WinNT.INVALID_HANDLE_VALUE) {
            LoggerUtil.log("❌ Failed to create process snapshot.");
            return;
        }

        PROCESSENTRY32.ByReference pe32 = new PROCESSENTRY32.ByReference();
        pe32.dwSize = new DWORD(pe32.size());

        if (Kernel32.INSTANCE.Process32First(hProcessSnap, pe32)) {
            do {
                HANDLE hProcess = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ, false, pe32.th32ProcessID.intValue());
                if (hProcess != null) {
                    HMODULE[] hMods = new HMODULE[1024];
                    IntByReference cbNeeded = new IntByReference();

                    if (Psapi.INSTANCE.EnumProcessModules(hProcess, hMods, Native.getNativeSize(HMODULE.class) * hMods.length, cbNeeded)) {
                        char[] fileNameBuffer = new char[WinNT.MAX_PATH];
                        // Using GetModuleFileNameExW as a more reliable alternative to GetModuleBaseName
                        if (Psapi.INSTANCE.GetModuleFileNameExW(hProcess, hMods[0], fileNameBuffer, WinNT.MAX_PATH) > 0) {
                            String processPath = new String(fileNameBuffer).trim();
                            String processName = new File(processPath).getName();
                            LoggerUtil.logStructured("ApplicationUsage", "Running Processes Snapshot", "Process Details: " + processName);
                        }
                    }
                    Kernel32.INSTANCE.CloseHandle(hProcess);
                }
            } while (Kernel32.INSTANCE.Process32Next(hProcessSnap, pe32));
        }

        Kernel32.INSTANCE.CloseHandle(hProcessSnap);
    }
}
