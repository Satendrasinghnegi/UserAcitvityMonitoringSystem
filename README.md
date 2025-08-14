User Activity Monitoring System
Project Description
This is a lightweight, non-intrusive console-based application designed to monitor and log user activity on a local Windows machine. The system provides a detailed audit trail of user sessions, application usage, and file system interactions, consolidating all events into a single, structured log file.

Features
File System Monitoring: Watches user-specified directories for real-time events, logging file creation, modification, and deletion.

Application Usage Tracking: Continuously monitors and logs changes to the active window title, providing a record of the user's application interactions.

Session Logging: Accurately detects and logs user login and logout events based on the Windows Event Log.

Process Snapshot: Captures a one-time snapshot of all running processes at the start of a user session.

Structured Logging: All events are recorded with a timestamp, component, event type, and relevant details, making the output easy to parse and analyze.

Getting Started
Prerequisites
To run this project, you will need:

Java Development Kit (JDK) 8 or higher installed.

The Java Native Access (JNA) library.

Installation and Setup
Clone the Repository: Clone this project from GitHub to your local machine.

https://github.com/Satendrasinghnegi/UserAcitvityMonitoringSystem.git

cd UserActivityMonitoringSystem

Add the JNA Library: Download the jna.jar file from Maven Central and place it in a new folder named lib at the root of your project.

Configure Watched Folders: In the Main.java file, modify the foldersToWatch list to specify the directories you want to monitor.

Run the Project: Compile and run the Main.java class from your IDE or the command line.

How to Run and Stop the Program
The application is designed to run in the background. To start it, simply run the main class.

To stop the program gracefully, open the terminal or command prompt where the application is running and press Ctrl+C. This sends an interrupt signal that safely terminates the application.

Project Structure
This project follows a standard Java repository structure to ensure clarity and maintainability.


A successful run of the monitoring system will produce a log file (userActivityLog.txt) with entries similar to this:

[2025-08-14 00:23:55] - Component: ApplicationUsage | Event: Running Processes Snapshot | Details: Process Details: chrome.exe\n
[2025-08-14 00:34:04] - Component: ActivityTracker | Event: Window Change | Details: Window Title: Google Gemini - Google Chrome\n
[2025-08-14 00:45:10] - Component: FileAccess | Event: File Created | Details: Path: C:\Users\YourUser\Documents | File: new_document.txt\n
[2025-08-14 00:45:15] - Component: FileAccess | Event: File Modified | Details: Path: C:\Users\YourUser\Documents | File: new_document.txt\n
[2025-08-14 01:10:20] - Component: LoginLogout | Event: User Logout | Details: Username: YourUser\n
