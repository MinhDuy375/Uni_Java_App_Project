package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SearchPanel extends JPanel {
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    private JButton searchButton;

    public SearchPanel(String[] filterOptions, ActionListener searchListener) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchField = new JTextField(15);
        filterComboBox = new JComboBox<>(filterOptions);
        searchButton = new JButton("Tìm kiếm");

        searchButton.setBackground(new Color(97, 187, 252));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setPreferredSize(new Dimension(120, 35));

        searchButton.addActionListener(searchListener);

        add(searchLabel);
        add(searchField);
        add(filterComboBox);
        add(searchButton);
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JComboBox<String> getFilterComboBox() {
        return filterComboBox;
    }
}