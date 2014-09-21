package com.rambilight.core.preferences;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

public class Preferences {

    // The module which this preferencehandler comunicates with
    String module;

    /** Create a new instance of the class that is associated with a specific part of the application
     * 
     * @param moduleName
     *        The name of the module. Must be unique.
     * @throws Exception */
    public Preferences(String moduleName) {
        module = moduleName;
        if (!PreferencesCore.rawPrefs.containsKey(module))
            PreferencesCore.rawPrefs.put(module, new Hashtable<String, String>());
    }

    /** Loads a int[] from cache
     * 
     * @param name
     *        Name of the variable to load
     * @param fallback
     *        If the variable isn't found, use the fallback
     * @return The value in cache */
    public String load(String name, String fallback) {
        return PreferencesCore.load(module, name, fallback);
    }

    /** Loads a int[] from cache
     * 
     * @param name
     *        Name of the variable to load
     * @param fallback
     *        If the variable isn't found, use the fallback
     * @return The value in cache */
    public int load(String name, int fallback) {
        String loaded = PreferencesCore.load(module, name, "null");
        return !loaded.equals("null") ? Integer.valueOf(loaded) : fallback;
    }

    /** Loads a int[] from cache
     * 
     * @param name
     *        Name of the variable to load
     * @param fallback
     *        If the variable isn't found, use the fallback
     * @param length
     *        Length of the returned array. Use -1 to use the contents length instead of one specified
     * @return The value in cache */
    public boolean load(String name, boolean fallback) {
        String loaded = PreferencesCore.load(module, name, "null");
        return !loaded.equals("null") ? Boolean.valueOf(loaded) : fallback;
    }

    /** Loads an int[] from cache
     * 
     * @param name
     *        Name of the variable to load
     * @param fallback
     *        If the variable isn't found, use the fallback
     * @param length
     *        Length of the returned array. Use -1 to use the contents length instead of one specified
     * @return The value in cache */
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

    /** Loads a String[] from cache
     * 
     * @param name
     *        Name of the variable to load
     * @param fallback
     *        If the variable isn't found, use the fallback
     * @param length
     *        Length of the returned array. Use -1 to use the contents length instead of one specified
     * @return The value in cache */
    public String[] load(String name, String[] fallback, int length) {
        String[] loaded = PreferencesCore.load(module, name, "null").split(",");

        if (loaded[0].equals("null"))
            return fallback;

        String[] arr;
        if (length == -1)
            arr = new String[loaded.length];
        else
            arr = new String[length];

        for (int i = 0; i < arr.length; i++)
            arr[i] = loaded[i];
        return arr;
    }

    /** Writes a string to cache
     * 
     * @param name
     *        Key
     * @param value
     *        Value */
    public void save(String name, String value) {
        PreferencesCore.save(module, name, value);
    }

    /** Writes an int to cache
     * 
     * @param name
     *        Key
     * @param value
     *        Value */
    public void save(String name, int value) {
        PreferencesCore.save(module, name, String.valueOf(value));
    }

    /** Writes a boolean to cache
     * 
     * @param name
     *        Key
     * @param value
     *        Value */
    public void save(String name, boolean value) {
        PreferencesCore.save(module, name, String.valueOf(value));
    }

    /** Writes a int[] to cache
     * 
     * @param name
     *        Key
     * @param value
     *        Value */
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

    /** Writes a String[] to cache
     * 
     * @param name
     *        Key
     * @param value
     *        Value */
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

    /** Flush the cache and write it to a file */
    public static void flush() {
        PreferencesCore.flushFile();
    }

    /** Reads a file into cache */
    public static void read() {
        PreferencesCore.readFile();
    }
}

/** Static class for global preferences */
class PreferencesCore {

    private static String                                         PATH     = System.getProperty("user.dir") + "/rambilight.conf";
    protected static Hashtable<String, Hashtable<String, String>> rawPrefs = new Hashtable<String, Hashtable<String, String>>();

    /** Loads a value from cache */
    protected static String load(String module, String name, String fallback) {
        if (rawPrefs.get(module).containsKey(name))
            return rawPrefs.get(module).get(name);
        return fallback;
    }

    /** Save a value to cache */
    protected static void save(String module, String name, String value) {
        if (value != null)
            rawPrefs.get(module).put(name, value);
    }

    /** Read and parse the content of the config file */
    public static void readFile() {
        rawPrefs.clear();
        try {
            String currentModule = "";
            List<String> lines = Files.readAllLines(Paths.get(PATH));
            for (String line : lines) {
                if (line.length() == 0)
                    continue;
                else if (line.subSequence(0, 1).equals("[")) {
                    currentModule = line.trim().substring(1, line.length() - 1).trim();
                    rawPrefs.put(currentModule, new Hashtable<String, String>());
                } else {
                    String[] keyVal = line.split("=");
                    if (keyVal.length == 2)
                        save(currentModule, keyVal[0].trim(), keyVal[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("The config file was not found");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("The config file is corrupt. Using defaults");
        }
    }

    /** Write the cache to the config file */
    public static void flushFile() {
        String serialized = "";

        for (Entry<String, Hashtable<String, String>> module : rawPrefs.entrySet()) {
            serialized += "[" + module.getKey() + "]\n";
            for (Entry<String, String> entry : module.getValue().entrySet())
                if (!entry.getKey().equals(""))
                    serialized += entry.getKey() + "=" + entry.getValue() + "\n";
            serialized += "\n";
        }
        try {
            Files.write(Paths.get(PATH), serialized.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception _e) {
            try {
                Files.write(Paths.get(PATH), serialized.getBytes(), StandardOpenOption.CREATE);
                System.out.println("Wrote preferences to " + PATH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
