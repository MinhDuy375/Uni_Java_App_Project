package views;

import javax.swing.*;

public class test {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Dropdown Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        String[] items = { "Item 1", "Item 2", "Item 3", "Item 4" };
        JComboBox<String> comboBox = new JComboBox<>(items);

        frame.add(comboBox);
        frame.setLayout(new java.awt.FlowLayout());
        frame.setVisible(true);
    }
}
