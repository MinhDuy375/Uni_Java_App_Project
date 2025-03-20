package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class StaffPanel extends JPanel {
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private SearchPanel searchPanel;

    public StaffPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Quản lý nhân viên", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Mã NV", "Họ và Tên", "Chức vụ", "Số điện thoại"};
        tableModel = new DefaultTableModel(columns, 0);
        staffTable = new JTable(tableModel);
        staffTable.setRowHeight(30);
        staffTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(staffTable);
        add(scrollPane, BorderLayout.CENTER);

        // Thêm SearchPanel
        String[] filterOptions = {"Mã NV", "Họ và Tên"};
        searchPanel = new SearchPanel(filterOptions, this::filterTable);
        add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addStaff());
        editButton.addActionListener(e -> editStaff());
        deleteButton.addActionListener(e -> deleteStaff());

        addSampleData();
    }

    private void addSampleData() {
        tableModel.addRow(new Object[]{"NV001", "Nguyễn Văn A", "Quản lý", "0123456789"});
        tableModel.addRow(new Object[]{"NV002", "Trần Thị B", "Nhân viên", "0987654321"});
        tableModel.addRow(new Object[]{"NV003", "Lê Văn C", "Nhân viên", "0345678901"});
    }

    private void filterTable(ActionEvent e) {
        String keyword = searchPanel.getSearchField().getText().trim().toLowerCase();
        DefaultTableModel filteredModel = new DefaultTableModel(new String[]{"Mã NV", "Họ và Tên", "Chức vụ", "Số điện thoại"}, 0);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String staffID = tableModel.getValueAt(i, 0).toString().toLowerCase();
            String fullName = tableModel.getValueAt(i, 1).toString().toLowerCase();
            if (staffID.contains(keyword) || fullName.contains(keyword)) {
                filteredModel.addRow(new Object[]{
                        tableModel.getValueAt(i, 0),
                        tableModel.getValueAt(i, 1),
                        tableModel.getValueAt(i, 2),
                        tableModel.getValueAt(i, 3)
                });
            }
        }
        staffTable.setModel(filteredModel);
    }

    private void addStaff() {
        JTextField staffIDField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField roleField = new JTextField();
        JTextField phoneField = new JTextField();

        Object[] message = {
                "Mã NV:", staffIDField,
                "Họ và Tên:", nameField,
                "Chức vụ:", roleField,
                "Số điện thoại:", phoneField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Thêm nhân viên", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String staffID = staffIDField.getText();
            String name = nameField.getText();
            String role = roleField.getText();
            String phone = phoneField.getText();

            if (!staffID.isEmpty() && !name.isEmpty() && !role.isEmpty() && !phone.isEmpty()) {
                tableModel.addRow(new Object[]{staffID, name, role, phone});
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField staffIDField = new JTextField(tableModel.getValueAt(selectedRow, 0).toString());
        JTextField nameField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString());
        JTextField roleField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString());
        JTextField phoneField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString());

        Object[] message = {
                "Mã NV:", staffIDField,
                "Họ và Tên:", nameField,
                "Chức vụ:", roleField,
                "Số điện thoại:", phoneField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Sửa thông tin nhân viên", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            tableModel.setValueAt(staffIDField.getText(), selectedRow, 0);
            tableModel.setValueAt(nameField.getText(), selectedRow, 1);
            tableModel.setValueAt(roleField.getText(), selectedRow, 2);
            tableModel.setValueAt(phoneField.getText(), selectedRow, 3);
        }
    }

    private void deleteStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);
        }
    }
}