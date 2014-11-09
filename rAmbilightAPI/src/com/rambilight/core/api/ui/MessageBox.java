package com.rambilight.core.api.ui;

import javax.swing.JOptionPane;

public class MessageBox {

    /**
     * Display an error box that also halts the program
     */
    public static void Error(String message) {
        JOptionPane.showMessageDialog(null, message, "An error ocurred!", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an input box that also halts the program
     */
    public static String Input(String title, String message) {
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
    }
}
