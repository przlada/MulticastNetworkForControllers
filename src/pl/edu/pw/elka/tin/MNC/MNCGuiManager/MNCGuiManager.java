package pl.edu.pw.elka.tin.MNC.MNCGuiManager;

import javax.swing.*;

/**
 * Menadzer do zarzadzania sterownikami
 * @author Paweł
 */
public class MNCGuiManager {
    JFrame frame;
    public MNCGuiManager(){
        JFrame frame = new JFrame("Empty Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
