package com.rambilight.core.api;

public class Platform {
    public static String  osName    = System.getProperty("os.name").toLowerCase();
    public static boolean isWindows = osName.contains("win");
    public static boolean isOSX     = osName.contains("mac");
    public static boolean isOther   = !isWindows && !isOSX;

    public static String getApplicationSupportPath(String name) {
        String partialPath;
        if (isWindows)
            partialPath = "/AppData/Local/";
        else if (isOSX)
            partialPath = "/Library/Application Support/";
        else
            partialPath = "/.";
        return System.getProperty("user.home") + partialPath + name;
    }

    public static String getFilePathFormat(String uri) {
        String prefix;
        if (isWindows)
            prefix = "";
        else if (isOSX)
            prefix = "file:";
        else
            prefix = "";
        return prefix + uri;
    }
}
