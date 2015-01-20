package com.legge.preferences;

import com.rambilight.core.api.Global;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

/**
 * Yoy can use this class to access an abstraction of the Preference interface for easy loading and saving
 * of preferences.
 * <p>
 * When a module is loaded, they automatically get a preference instance in their name and connected to
 * their module.
 * NOTE: It's not recommended to create a new instance of this in a module if you don't know what your are doing.
 */
public class Preferences {

    // The module which this preference handler communicates with
    String module;

    /**
     * Create a new instance of the class that is associated with a specific part of the application.
     *
     * @param moduleName, Name of the module / preference which should be saved.
     */
    public Preferences(String moduleName) {
        module = moduleName;
        if (!PreferencesCore.rawPrefs.containsKey(module))
            PreferencesCore.rawPrefs.put(module, new Hashtable<>());
    }

    /**
     * Loads a string from cache
     *
     * @param name     Name of the variable to load
     * @param fallback If the variable isn't found, use the fallback
     * @return The value in cache
     */
    public String load(String name, String fallback) {
        return PreferencesCore.load(module, name, fallback);
    }

    /**
     * Loads an int from cache
     *
     * @param name     Name of the variable to load
     * @param fallback If the variable isn't found, use the fallback
     * @return The value in cache
     */
    public int load(String name, int fallback) {
        String loaded = PreferencesCore.load(module, name, "null");
        return !loaded.equals("null") ? Integer.valueOf(loaded) : fallback;
    }

    /**
     * Loads a boolean from cache
     *
     * @param name     Name of the variable to load
     * @param fallback If the variable isn't found, use the fallback
     * @return The value in cache
     */
    public boolean load(String name, boolean fallback) {
        String loaded = PreferencesCore.load(module, name, "null");
        return !loaded.equals("null") ? Boolean.valueOf(loaded) : fallback;
    }

    /**
     * Loads an int[] from cache
     *
     * @param name     Name of the variable to load
     * @param fallback If the variable isn't found, use the fallback
     * @param length   Length of the returned array. Use -1 to use the contents length instead of one specified
     * @return The value in cache
     */
    public int[] load(String name, int[] fallback, int length) {
        String[] loaded = PreferencesCore.load(module, name, "null").split(",");

        if (loaded[0].equals("null"))
            return fallback;

        try {
            Integer.parseInt(loaded[0]);
        } catch (Exception e) {
            return fallback;
        }

        int[] arr;
        if (length == -1)
            arr = new int[loaded.length];
        else
            arr = new int[length];

        for (int i = 0; i < arr.length; i++)
            arr[i] = Integer.valueOf(loaded[i]);
        return arr;
    }

    /**
     * Loads a String[] from cache
     *
     * @param name     Name of the variable to load
     * @param fallback If the variable isn't found, use the fallback
     * @param length   Length of the returned array. Use -1 to use the contents length instead of one specified
     * @return The value in cache
     */
    public String[] load(String name, String[] fallback, int length) {
        String[] loaded = PreferencesCore.load(module, name, "null").split(",");

        if (loaded[0].equals("null"))
            return fallback;

        String[] arr;
        if (length == -1)
            arr = new String[loaded.length];
        else
            arr = new String[length];
        System.arraycopy(loaded, 0, arr, 0, arr.length);

        return arr;
    }

    /**
     * Writes a string to cache
     *
     * @param name  Key
     * @param value Value
     */
    public void save(String name, String value) {
        PreferencesCore.save(module, name, value);
    }

    /**
     * Writes an int to cache
     *
     * @param name  Key
     * @param value Value
     */
    public void save(String name, int value) {
        PreferencesCore.save(module, name, String.valueOf(value));
    }

    /**
     * Writes a boolean to cache
     *
     * @param name  Key
     * @param value Value
     */
    public void save(String name, boolean value) {
        PreferencesCore.save(module, name, String.valueOf(value));
    }

    /**
     * Writes an int[] to cache
     *
     * @param name   Key
     * @param values Values
     */
    public void save(String name, int[] values) {
        String serialized = "";
        if (values.length > 0)
            for (int value : values)
                serialized += value + ",";
        else
            serialized = " ";
        // Use substring to remove the last "," from the string.
        PreferencesCore.save(module, name, serialized.substring(0, serialized.length() - 1));
    }

    /**
     * Writes a String[] to cache
     *
     * @param name   Key
     * @param values Values
     */
    public void save(String name, String[] values) {
        String serialized = "";
        if (values.length > 0)
            for (String value : values)
                serialized += value + ",";
        else
            serialized = " ";
        // Use substring to remove the last "," from the string.
        PreferencesCore.save(module, name, serialized.substring(0, serialized.length() - 1));
    }


    public static void flush() {
        PreferencesCore.flushFile();
    }

    public static void read() {
        PreferencesCore.readFile();
    }


    public static void setPathToDefault() {
        PreferencesCore.setPathToDefault();
    }

    public static void setPathAbsolute(String filePath) {
        PreferencesCore.setPathAbsolute(filePath);
    }

    public static String getPathToFile() {
        return PreferencesCore.PATH;
    }
}

/**
 * Static class for global preferences
 */
class PreferencesCore {

    protected static String PATH;
    protected static Hashtable<String, Hashtable<String, String>> rawPrefs = new Hashtable<>();

    protected static void setPathToDefault() {
        PATH = Global.applicationSupportPath + "/" + Global.APPLICATIONNAME + ".conf";
    }

    protected static void setPathAbsolute(String filePath) {
        PATH = filePath;
    }

    /**
     * Loads a value from cache
     */
    protected static String load(String module, String name, String fallback) {
        if (rawPrefs.get(module) != null && rawPrefs.get(module).containsKey(name))
            return rawPrefs.get(module).get(name);
        return fallback;
    }

    /**
     * Save a value to cache
     */
    protected static void save(String module, String name, String value) {
        if (value != null)
            rawPrefs.get(module).put(name, value);
    }

    /**
     * Read and parse the content of the config file
     */
    protected static void readFile() {
        rawPrefs.clear();
        try {
            String currentModule = "";
            List<String> lines = Files.readAllLines(Paths.get(PATH));
            for (String line : lines) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#')
                    continue;
                else if (line.subSequence(0, 1).equals("[")) {
                    currentModule = line.substring(1, line.length() - 1).trim();
                    rawPrefs.put(currentModule, new Hashtable<>());
                }
                else {
                    String[] keyVal = line.split("=");
                    if (keyVal.length == 2)
                        save(currentModule, keyVal[0].trim(), keyVal[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("The config file was not found. Using defaults.");
        } catch (Exception e) {
            System.out.println("The config file is corrupt. Using defaults.");
        }
    }

    /**
     * Write the cache to the config file
     */
    private static String[] getSortedTable(Hashtable<String, ?> table) {
        String[] fields = new String[table.size()];
        int i = 0;

        for (Entry<String, ?> module : table.entrySet())
            fields[i++] = module.getKey();

        Arrays.sort(fields);
        return fields;
    }

    protected static void flushFile() {

        String serialized = "";

        serialized += "################################## IMPORTANT ##################################\n";
        serialized += "# Please don't edit this configuration file while the application is running. #\n";
        serialized += "# This is because the preferences are only read on startup and then rewritten #\n";
        serialized += "# on application exit. So, any changes made whilst the application is running #\n";
        serialized += "# will be discarded                                                           #\n";
        serialized += "################################## IMPORTANT ##################################\n\n";

        for (String moduleName : getSortedTable(rawPrefs)) {
            serialized += "[" + moduleName + "]\n";

            Hashtable<String, String> module = rawPrefs.get(moduleName);
            for (String fieldName : getSortedTable(module))
                if (!module.get(fieldName).equals(""))
                    serialized += fieldName + "=" + module.get(fieldName) + "\n";

            serialized += "\n";
        }

        try {
            Files.write(Paths.get(PATH), serialized.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Wrote preferences to " + PATH);
        } catch (Exception _e) {
            try {
                Files.write(Paths.get(PATH), serialized.getBytes(), StandardOpenOption.CREATE);
                System.out.println("Created new preference file at " + PATH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
