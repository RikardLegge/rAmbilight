package com.rambilight.core.ui;

import javax.swing.*;

public class MessageBox {

    private static final JFrame component = new JFrame();

    /** Display an errorbox that also halts the program */
    public static void Error(String message) {
        JOptionPane.showMessageDialog(component, message, "An error ocurred!", JOptionPane.ERROR_MESSAGE);
    }

    /** Display an inputbox that also halts the program */
    public static String Input(String title, String message) {
        return JOptionPane.showInputDialog(component, message, title, JOptionPane.PLAIN_MESSAGE);
    }
}
