package com.womensafety;

import com.womensafety.ui.AppFrame;
import com.womensafety.ui.Theme;

import javax.swing.SwingUtilities;

/**
 * Application entry point for Women Safety System.
 */
public class Main {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            Theme.setupLookAndFeel();
            AppFrame frame = new AppFrame();
            frame.launch();
        });
    }
}
