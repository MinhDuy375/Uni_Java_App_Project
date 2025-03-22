package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class ServiceMenuPanel extends JPanel {
    private JTable menuTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton;
    private JLabel imagePreview;
    private JTextField nameField, priceField;

    public ServiceMenuPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Thực đơn quán net", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        String[] columns = { "Ảnh", "Tên món", "Giá (VNĐ)" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return (column == 0) ? ImageIcon.class : String.class;
            }
        };
        menuTable = new JTable(tableModel);
        menuTable.setRowHeight(80);
        menuTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        menuTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        menuTable.getTableHeader().setBackground(new Color(97, 187, 252));
        menuTable.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(menuTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.add(new JLabel("Tên món:", SwingConstants.RIGHT));
        nameField = new JTextField();
        formPanel.add(nameField);
        formPanel.add(new JLabel("Giá (VNĐ):", SwingConstants.RIGHT));
        priceField = new JTextField();
        formPanel.add(priceField);
        formPanel.add(new JLabel("Ảnh món:", SwingConstants.RIGHT));
        imagePreview = new JLabel("Chọn ảnh", SwingConstants.CENTER);
        imagePreview.setOpaque(true);
        imagePreview.setBackground(Color.LIGHT_GRAY);
        imagePreview.setPreferredSize(new Dimension(100, 80));
        imagePreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imagePreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                chooseImage();
            }
        });
        formPanel.add(imagePreview);
        add(formPanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        addButton = new JButton("Thêm");
        editButton = new JButton("Sửa");
        deleteButton = new JButton("Xóa");
        styleButton(addButton);
        styleButton(editButton);
        styleButton(deleteButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setupActions();
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            ImageIcon icon = new ImageIcon(new ImageIcon(fileChooser.getSelectedFile().getPath()).getImage()
                    .getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            imagePreview.setIcon(icon);
            imagePreview.setText("");
        }
    }

    private void setupActions() {
        addButton.addActionListener(e -> addMenuItem());
        editButton.addActionListener(e -> editMenuItem());
        deleteButton.addActionListener(e -> deleteMenuItem());
    }

    private void addMenuItem() {
        String name = nameField.getText().trim();
        String price = priceField.getText().trim();
        Icon image = imagePreview.getIcon();

        if (name.isEmpty() || price.isEmpty() || image == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.addRow(new Object[] { image, name, price });
        clearForm();
    }

    private void editMenuItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một món để sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String price = priceField.getText().trim();
        Icon image = imagePreview.getIcon();

        if (name.isEmpty() || price.isEmpty() || image == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setValueAt(image, selectedRow, 0);
        tableModel.setValueAt(name, selectedRow, 1);
        tableModel.setValueAt(price, selectedRow, 2);
        clearForm();
    }

    private void deleteMenuItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một món để xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.removeRow(selectedRow);
    }

    private void clearForm() {
        nameField.setText("");
        priceField.setText("");
        imagePreview.setIcon(null);
        imagePreview.setText("Chọn ảnh");
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(97, 187, 252));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}