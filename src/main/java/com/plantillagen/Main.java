package com.plantillagen;

import com.formdev.flatlaf.FlatLightLaf;
import com.plantillagen.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                System.err.println("FlatLaf no disponible, usando Look & Feel del sistema.");
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
            SwingUtilities.invokeLater(frame::initData);
        });
    }
}
