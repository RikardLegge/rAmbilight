package com.rambilight.core.api.ui;

import com.rambilight.core.api.Global;

import javax.swing.*;

public class MessageBox {

    private static final JFrame component = new JFrame();

    /**
     * Display an error box that also halts the program.
     *
     * @param title,   The title of the message box.
     * @param message, The message to be displayed.
     */
    @Deprecated
    public static void Error(String title, String message, int i) {
        JOptionPane.showMessageDialog(component, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an error box that also halts the program
     */
    public static void Error(String title, String message) {
        Global.ERRORLOG.add(title);
        if (!Global.disableErrorPopups)
            JOptionPane.showMessageDialog(component, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an input box that also halts the program, waiting for user input.
     *
     * @param title,   The title of the message box.
     * @param message, The message to be displayed.
     * @return The inputted valued or null if canceled.
     */
    public static String Input(String title, String message) {
        return JOptionPane.showInputDialog(component, message, title, JOptionPane.PLAIN_MESSAGE);
    }
}
