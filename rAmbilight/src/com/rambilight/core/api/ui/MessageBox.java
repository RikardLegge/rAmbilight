package com.rambilight.core.api.ui;

import javax.swing.*;

public class MessageBox {

    private static final JFrame component = new JFrame();

    /**
     * Display an error box that also halts the program
     */
    public static void Error(String message) {
        JOptionPane.showMessageDialog(component, message, "An error ocurred!", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an input box that also halts the program
     */
    public static String Input(String title, String message) {
        return JOptionPane.showInputDialog(component, message, title, JOptionPane.PLAIN_MESSAGE);
    }
}
