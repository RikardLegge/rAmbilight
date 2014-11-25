package com.rambilight.core.api.ui;

import javax.swing.JOptionPane;

public class MessageBox {

    /**
     * Display an error box that also halts the program.
     *
     * @param title,   The title of the message box.
     * @param message, The message to be displayed.
     */
    public static void Error(String title, String message) {
        System.out.println(message);
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an input box that also halts the program, waiting for user input.
     *
     * @param title,   The title of the message box.
     * @param message, The message to be displayed.
     * @return The inputted valued or null if canceled.
     */
    public static String Input(String title, String message) {
        System.out.println(message);
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
    }
}
