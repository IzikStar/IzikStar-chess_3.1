package main;

import javax.swing.*;
import java.awt.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setMinimumSize(new Dimension(1000, 1000));
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(Color.black);
        frame.setLocationRelativeTo(null);


        Board board = new Board();
        frame.add(board);

        frame.setVisible(true);
    }
}