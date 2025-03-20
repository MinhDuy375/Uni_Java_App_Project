package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class SearchPanel extends JPanel {
    private JComboBox<String> filterComboBox;
    private JTextField searchField;
    private JButton searchButton;
    public JTextField getSearchField() {
        return searchField;
    }

    public SearchPanel(String[] filterOptions, ActionListener searchAction) {
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        setBackground(Color.WHITE);

        filterComboBox = new JComboBox<>(filterOptions);
        searchField = new JTextField(15);
        searchButton = new JButton("Tìm");

        // Gọi sự kiện tìm kiếm đúng với filterTable trong OrderPanel
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            int columnIndex = filterComboBox.getSelectedIndex(); // Lấy index của cột cần tìm
            searchAction.actionPerformed(new java.awt.event.ActionEvent(this, ActionEvent.ACTION_PERFORMED, keyword + ";" + columnIndex));
        });

        add(filterComboBox);
        add(searchField);
        add(searchButton);
    }
}
