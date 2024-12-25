package com.stellantis.team.utility.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomLogger {
	private static String logFolder = "C:\\Apps\\CopyIterationUtility\\Logs";

	static {
        File directory = new File(logFolder);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Log directory created: " + logFolder);
            } else {
                System.err.println("Failed to create log directory: " + logFolder);
            }
        }
    }
	
	public static void logException(Exception e) {
        String exceptionLogFilePath = logFolder + "\\exception.log";
        logToFile(exceptionLogFilePath, getCurrentDateTime() + " - Exception: " + e.getMessage());
        logExceptionStackTrace(exceptionLogFilePath, e);
    }

    public static void logMessage(String message) {
        String messageLogFilePath = logFolder + "\\message.log";
        logToFile(messageLogFilePath, getCurrentDateTime() + " - " + message);
    }

    private static void logToFile(String filePath, String logEntry) {
        try (FileWriter fw = new FileWriter(filePath, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    private static void logExceptionStackTrace(String filePath, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        logToFile(filePath, sw.toString());
    }

    private static String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return dateFormat.format(now);
    }

}
