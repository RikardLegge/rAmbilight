package com.rambilight.core.preferences;

import java.util.Hashtable;

/**
 * Beelining of an implementation of a version of i18n, Localization.
 * Currently only for cleaning the code from strings.
 */
public class i18n {

    private static Hashtable<String, String> translations = new Hashtable<>();

    public static String get(String identifier) {
        if (translations.containsKey(identifier))
            return translations.get(identifier);
        else {
            System.out.println(String.format("No value registered for identifier: %s", identifier));
            return identifier;
        }
    }

    public static void load() {
        translations.put("noSerialPortSpecified", "No serial port specified. Finding most appropriate port...");
        translations.put("onePortFound", "One port found: %s");
        translations.put("portFound", "Port found!");
        translations.put("usePort?", "Are you sure you want to use this port as an rAmbilight device? (Type 'Yes' or 'No')'\n%s\n");
        translations.put("key", "value");
    }

}
