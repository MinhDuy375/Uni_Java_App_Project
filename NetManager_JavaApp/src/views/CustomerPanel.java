package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CustomerPanel extends JPanel {
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private SearchPanel searchPanel;

    public CustomerPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Quản lý tài khoản", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Tên tài khoản", "Số giờ còn lại", "Mật khẩu"};
        tableModel = new DefaultTableModel(columns, 0);
        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(30);
        customerTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        // Thêm SearchPanel
        String[] filterOptions = {"Tên tài khoản"};
        searchPanel = new SearchPanel(filterOptions, this::filterTable);
        add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton rechargeButton = new JButton("Nạp giờ");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(rechargeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addCustomer());
        editButton.addActionListener(e -> editCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        rechargeButton.addActionListener(e -> rechargeHours());

        // Dữ liệu mẫu
        addSampleData();
    }

    private void addSampleData() {
        tableModel.addRow(new Object[]{"user1", 5, "password1"});
        tableModel.addRow(new Object[]{"user2", 10, "password2"});
        tableModel.addRow(new Object[]{"admin", 15, "adminpass"});
    }

    private void filterTable(ActionEvent e) {
        String keyword = searchPanel.getSearchField().getText().trim().toLowerCase();
        DefaultTableModel filteredModel = new DefaultTableModel(new String[]{"Tên tài khoản", "Số giờ còn lại", "Mật khẩu"}, 0);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String username = tableModel.getValueAt(i, 0).toString().toLowerCase();
            if (username.contains(keyword)) {
                filteredModel.addRow(new Object[]{
                        tableModel.getValueAt(i, 0),
                        tableModel.getValueAt(i, 1),
                        tableModel.getValueAt(i, 2)
                });
            }
        }
        customerTable.setModel(filteredModel);
    }

    private void addCustomer() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] message = {"Tên tài khoản:", usernameField, "Mật khẩu:", passwordField};

        int option = JOptionPane.showConfirmDialog(this, message, "Thêm tài khoản", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (!username.isEmpty() && !password.isEmpty()) {
                tableModel.addRow(new Object[]{username, 0, password});
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String currentUsername = tableModel.getValueAt(selectedRow, 0).toString();
        String currentPassword = tableModel.getValueAt(selectedRow, 2).toString();

        JTextField usernameField = new JTextField(currentUsername);
        JPasswordField passwordField = new JPasswordField(currentPassword);

        Object[] message = {"Tên tài khoản:", usernameField, "Mật khẩu:", passwordField};

        int option = JOptionPane.showConfirmDialog(this, message, "Chỉnh sửa tài khoản", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newUsername = usernameField.getText();
            String newPassword = new String(passwordField.getPassword());
            if (!newUsername.isEmpty() && !newPassword.isEmpty()) {
                tableModel.setValueAt(newUsername, selectedRow, 0);
                tableModel.setValueAt(newPassword, selectedRow, 2);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa tài khoản này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);
        }
    }

    private void rechargeHours() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để nạp giờ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String amountString = JOptionPane.showInputDialog(this, "Nhập số tiền (VND):");
        if (amountString != null) {
            try {
                int amount = Integer.parseInt(amountString);
                if (amount >= 5000) {
                    int hoursToAdd = amount / 5000;
                    int currentHours = Integer.parseInt(tableModel.getValueAt(selectedRow, 1).toString());
                    tableModel.setValueAt(currentHours + hoursToAdd, selectedRow, 1);
                } else {
                    JOptionPane.showMessageDialog(this, "Số tiền tối thiểu là 5.000VND!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
