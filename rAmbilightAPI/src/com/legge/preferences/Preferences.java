package com.legge.preferences;

public class Preferences {

    /**
     * Create a new instance of the class that is associated with a specific part of the application
     *
     * @param moduleName The name of the module. Must be unique.
     */
    public Preferences(String moduleName) {
    }

    /**
     * Loads a string from cache
     *
     * @param name     Name of the variable to load
     * @param fallback If the variable isn't found, use the fallback
     * @return The value in cache
     */
    public String load(String name, String fallback) {
        return "";
    }

    /**
     * Loads an int from cache
     *
     * @param name     Name of the variable to load
     * @param fallback If the variable isn't found, use the fallback
     * @return The value in cache
     */
    public int load(String name, int fallback) {
        return 0;
    }

    /**
     * Loads a boolean from cache
     *
     * @param name     Name of the variable to load
     * @param fallback If the variable isn't found, use the fallback
     * @return The value in cache
     */
    public boolean load(String name, boolean fallback) {
        return false;
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
        return new int[0];
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
        return new String[0];
    }

    /**
     * Writes a string to cache
     *
     * @param name  Key
     * @param value Value
     */
    public void save(String name, String value) {
    }

    /**
     * Writes an int to cache
     *
     * @param name  Key
     * @param value Value
     */
    public void save(String name, int value) {
    }

    /**
     * Writes a boolean to cache
     *
     * @param name  Key
     * @param value Value
     */
    public void save(String name, boolean value) {
    }

    /**
     * Writes an int[] to cache
     *
     * @param name   Key
     * @param values Values
     */
    public void save(String name, int[] values) {
    }

    /**
     * Writes a String[] to cache
     *
     * @param name   Key
     * @param values Values
     */
    public void save(String name, String[] values) {
    }
}