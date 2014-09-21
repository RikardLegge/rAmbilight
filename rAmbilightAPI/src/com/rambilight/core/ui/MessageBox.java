package com.rambilight.core.ui;

import javax.swing.JOptionPane;

public class MessageBox {

    /** Display an errorbox that also halts the program */
    public static void Error(String message) {
        JOptionPane.showMessageDialog(null, message, "An error ocurred!", JOptionPane.ERROR_MESSAGE);
    }

    /** Display an inputbox that also halts the program */
    public static String Input(String title, String message) {
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
    }
}
