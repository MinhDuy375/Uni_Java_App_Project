package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private SearchPanel searchPanel;

    public CustomerPanel() {
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Quản lý tài khoản", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        String[] columns = { "Tên tài khoản", "Tình trạng" }; // Bỏ cột "Mật khẩu", thay "Số giờ còn lại" bằng "Tình
                                                              // trạng"
        tableModel = new DefaultTableModel(columns, 0);
        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(30);
        customerTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        String[] filterOptions = { "Tên tài khoản" };
        searchPanel = new SearchPanel(filterOptions, this::filterTable);
        add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton rechargeButton = new JButton("Cập nhật trạng thái"); // Đổi tên nút vì không có cột hours

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(rechargeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addCustomer());
        editButton.addActionListener(e -> editCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        rechargeButton.addActionListener(e -> updateStatus()); // Thay rechargeHours() bằng updateStatus()

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            ResultSet rs = dbManager.select("TAI_KHOAN", new String[] { "matkhau", "tinhtrang" }, "");
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getString("matkhau"),
                        rs.getInt("tinhtrang")
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(ActionEvent e) {
        String keyword = searchPanel.getSearchField().getText().trim().toLowerCase();
        DefaultTableModel filteredModel = new DefaultTableModel(
                new String[] { "Tên tài khoản", "Tình trạng" }, 0);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String matkhau = tableModel.getValueAt(i, 0).toString().toLowerCase();
            if (matkhau.contains(keyword)) {
                filteredModel.addRow(new Object[] {
                        tableModel.getValueAt(i, 0),
                        tableModel.getValueAt(i, 1)
                });
            }
        }
        customerTable.setModel(filteredModel);
    }

    private void addCustomer() {
        JTextField matkhauField = new JTextField();
        JTextField chucvuField = new JTextField();
        JTextField batdaucaField = new JTextField();
        JTextField kethuccaField = new JTextField();
        JTextField tinhtrangField = new JTextField();

        Object[] message = {
                "Tên tài khoản:", matkhauField,
                "Chức vụ:", chucvuField,
                "Bắt đầu ca:", batdaucaField,
                "Kết thúc ca:", kethuccaField,
                "Tình trạng (0/1):", tinhtrangField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "Thêm tài khoản", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String matkhau = matkhauField.getText();
            String chucvu = chucvuField.getText();
            String batdauca = batdaucaField.getText();
            String kethucca = kethuccaField.getText();
            String tinhtrangStr = tinhtrangField.getText();

            if (!matkhau.isEmpty()) {
                try {
                    int tinhtrang = tinhtrangStr.isEmpty() ? 0 : Integer.parseInt(tinhtrangStr);
                    dbManager.insert("TAI_KHOAN",
                            new Object[] { null, matkhau, chucvu, batdauca, kethucca, tinhtrang });
                    loadData();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Lỗi thêm tài khoản: " + e.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Tình trạng phải là số (0 hoặc 1)!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên tài khoản!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String currentMatkhau = tableModel.getValueAt(selectedRow, 0).toString();

        // Lấy thông tin hiện tại từ database
        String chucvu = "", batdauca = "", kethucca = "";
        int tinhtrang = 0;
        try {
            ResultSet rs = dbManager.select("TAI_KHOAN", new String[] { "chucvu", "batdauca", "kethucca", "tinhtrang" },
                    "matkhau = '" + currentMatkhau + "'");
            if (rs.next()) {
                chucvu = rs.getString("chucvu") != null ? rs.getString("chucvu") : "";
                batdauca = rs.getString("batdauca") != null ? rs.getString("batdauca") : "";
                kethucca = rs.getString("kethucca") != null ? rs.getString("kethucca") : "";
                tinhtrang = rs.getInt("tinhtrang");
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy thông tin tài khoản: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField matkhauField = new JTextField(currentMatkhau);
        JTextField chucvuField = new JTextField(chucvu);
        JTextField batdaucaField = new JTextField(batdauca);
        JTextField kethuccaField = new JTextField(kethucca);
        JTextField tinhtrangField = new JTextField(String.valueOf(tinhtrang));

        Object[] message = {
                "Tên tài khoản:", matkhauField,
                "Chức vụ:", chucvuField,
                "Bắt đầu ca:", batdaucaField,
                "Kết thúc ca:", kethuccaField,
                "Tình trạng (0/1):", tinhtrangField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "Chỉnh sửa tài khoản", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newMatkhau = matkhauField.getText();
            String newChucvu = chucvuField.getText();
            String newBatdauca = batdaucaField.getText();
            String newKethucca = kethuccaField.getText();
            String newTinhtrangStr = tinhtrangField.getText();

            if (!newMatkhau.isEmpty()) {
                try {
                    int newTinhtrang = newTinhtrangStr.isEmpty() ? 0 : Integer.parseInt(newTinhtrangStr);
                    dbManager.update("TAI_KHOAN",
                            new String[] { "matkhau", "chucvu", "batdauca", "kethucca", "tinhtrang" },
                            new Object[] { newMatkhau, newChucvu, newBatdauca, newKethucca, newTinhtrang },
                            "matkhau = '" + currentMatkhau + "'");
                    loadData();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Lỗi sửa tài khoản: " + e.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Tình trạng phải là số (0 hoặc 1)!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên tài khoản!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String matkhau = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa tài khoản này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbManager.delete("TAI_KHOAN", "matkhau = '" + matkhau + "'");
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa tài khoản: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateStatus() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để cập nhật trạng thái!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String matkhau = tableModel.getValueAt(selectedRow, 0).toString();
        String statusString = JOptionPane.showInputDialog(this, "Nhập trạng thái mới (0 hoặc 1):");
        if (statusString != null) {
            try {
                int newStatus = Integer.parseInt(statusString);
                if (newStatus == 0 || newStatus == 1) {
                    dbManager.update("TAI_KHOAN", new String[] { "tinhtrang" }, new Object[] { newStatus },
                            "matkhau = '" + matkhau + "'");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Trạng thái phải là 0 hoặc 1!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập trạng thái hợp lệ (0 hoặc 1)!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật trạng thái: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}