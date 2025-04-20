package views;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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

        JLabel title = new JLabel("Quản lý khách hàng", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        String[] columns = { "Mã khách hàng", "Tên khách hàng", "Số điện thoại", "Email", "Điểm tích lũy" };
        tableModel = new DefaultTableModel(columns, 0);
        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(30);
        customerTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        customerTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        customerTable.getTableHeader().setBackground(new Color(97, 187, 252));
        customerTable.getTableHeader().setForeground(Color.WHITE);

        customerTable.setShowGrid(false);
        customerTable.setIntercellSpacing(new Dimension(0, 0));
        customerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 241, 249));
                }
                if (column == 0 || column == 4) { // Căn giữa cột "Mã khách hàng" và "Điểm tích lũy"
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        String[] filterOptions = { "Mã khách hàng", "Tên khách hàng", "Số điện thoại", "Email" };
        searchPanel = new SearchPanel(filterOptions, this::filterTable);
        add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton rechargeButton = new JButton("Cập nhật điểm tích lũy");

        styleButton(addButton);
        styleButton(editButton);
        styleButton(deleteButton);
        styleButton(rechargeButton);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(rechargeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addCustomer());
        editButton.addActionListener(e -> editCustomer());
        deleteButton.addActionListener(e -> deleteCustomer());
        rechargeButton.addActionListener(e -> updatePoints());

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            ResultSet rs = dbManager.select("KHACH_HANG",
                    new String[] { "MaKH", "TenKH", "SDT", "Email", "DiemTichLuy" }, "");
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getString("MaKH"),
                        rs.getString("TenKH"),
                        rs.getString("SDT"),
                        rs.getString("Email"),
                        rs.getInt("DiemTichLuy")
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(ActionEvent e) {
        String keyword = searchPanel.getSearchField().getText().trim().toLowerCase();
        String selectedFilter = searchPanel.getFilterComboBox().getSelectedItem().toString();
        DefaultTableModel filteredModel = new DefaultTableModel(
                new String[] { "Mã khách hàng", "Tên khách hàng", "Số điện thoại", "Email", "Điểm tích lũy" }, 0);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String value;
            switch (selectedFilter) {
                case "Mã khách hàng":
                    value = tableModel.getValueAt(i, 0).toString().toLowerCase();
                    break;
                case "Tên khách hàng":
                    value = tableModel.getValueAt(i, 1).toString().toLowerCase();
                    break;
                case "Số điện thoại":
                    value = tableModel.getValueAt(i, 2).toString().toLowerCase();
                    break;
                case "Email":
                    value = tableModel.getValueAt(i, 3).toString().toLowerCase();
                    break;
                default:
                    value = "";
            }
            if (value.contains(keyword)) {
                filteredModel.addRow(new Object[] {
                        tableModel.getValueAt(i, 0),
                        tableModel.getValueAt(i, 1),
                        tableModel.getValueAt(i, 2),
                        tableModel.getValueAt(i, 3),
                        tableModel.getValueAt(i, 4)
                });
            }
        }
        customerTable.setModel(filteredModel);
    }

    private void addCustomer() {
        JTextField maKHField = new JTextField();
        JTextField tenKHField = new JTextField();
        JTextField sdtField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField diemTichLuyField = new JTextField("0");

        Object[] message = {
                "Mã khách hàng:", maKHField,
                "Tên khách hàng:", tenKHField,
                "Số điện thoại:", sdtField,
                "Email:", emailField,
                "Điểm tích lũy:", diemTichLuyField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "Thêm khách hàng", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String maKH = maKHField.getText().trim();
            String tenKH = tenKHField.getText().trim();
            String sdt = sdtField.getText().trim();
            String email = emailField.getText().trim();
            String diemTichLuyStr = diemTichLuyField.getText().trim();

            if (maKH.isEmpty() || tenKH.isEmpty() || sdt.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int diemTichLuy = diemTichLuyStr.isEmpty() ? 0 : Integer.parseInt(diemTichLuyStr);
                if (diemTichLuy < 0) {
                    JOptionPane.showMessageDialog(this, "Điểm tích lũy phải là số không âm!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dbManager.insert("KHACH_HANG", new Object[] { maKH, tenKH, sdt, email, diemTichLuy });
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm khách hàng: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Điểm tích lũy phải là số nguyên!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String currentMaKH = tableModel.getValueAt(selectedRow, 0).toString();

        String tenKH = tableModel.getValueAt(selectedRow, 1).toString();
        String sdt = tableModel.getValueAt(selectedRow, 2).toString();
        String email = tableModel.getValueAt(selectedRow, 3).toString();
        int diemTichLuy = Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString());

        JTextField maKHField = new JTextField(currentMaKH);
        JTextField tenKHField = new JTextField(tenKH);
        JTextField sdtField = new JTextField(sdt);
        JTextField emailField = new JTextField(email);
        JTextField diemTichLuyField = new JTextField(String.valueOf(diemTichLuy));

        Object[] message = {
                "Mã khách hàng:", maKHField,
                "Tên khách hàng:", tenKHField,
                "Số điện thoại:", sdtField,
                "Email:", emailField,
                "Điểm tích lũy:", diemTichLuyField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "Chỉnh sửa khách hàng", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newMaKH = maKHField.getText().trim();
            String newTenKH = tenKHField.getText().trim();
            String newSdt = sdtField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newDiemTichLuyStr = diemTichLuyField.getText().trim();

            if (newMaKH.isEmpty() || newTenKH.isEmpty() || newSdt.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int newDiemTichLuy = newDiemTichLuyStr.isEmpty() ? 0 : Integer.parseInt(newDiemTichLuyStr);
                if (newDiemTichLuy < 0) {
                    JOptionPane.showMessageDialog(this, "Điểm tích lũy phải là số không âm!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dbManager.update("KHACH_HANG",
                        new String[] { "MaKH", "TenKH", "SDT", "Email", "DiemTichLuy" },
                        new Object[] { newMaKH, newTenKH, newSdt, newEmail, newDiemTichLuy },
                        "MaKH = ?",
                        new Object[] { currentMaKH });
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi sửa khách hàng: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Điểm tích lũy phải là số nguyên!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maKH = tableModel.getValueAt(selectedRow, 0).toString();
        try {
            ResultSet rs = dbManager.select("HOA_DON", new String[] { "MaHD" }, "MaKH = ?", new Object[] { maKH });
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Không thể xóa khách hàng này vì đã có hóa đơn liên quan!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                rs.close();
                return;
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kiểm tra hóa đơn: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khách hàng này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbManager.delete("KHACH_HANG", "MaKH = ?", new Object[] { maKH });
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa khách hàng: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updatePoints() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để cập nhật điểm tích lũy!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maKH = tableModel.getValueAt(selectedRow, 0).toString();
        String pointsString = JOptionPane.showInputDialog(this, "Nhập điểm tích lũy mới:");
        if (pointsString != null) {
            try {
                int newPoints = Integer.parseInt(pointsString);
                if (newPoints < 0) {
                    JOptionPane.showMessageDialog(this, "Điểm tích lũy phải là số không âm!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dbManager.update("KHACH_HANG", new String[] { "DiemTichLuy" }, new Object[] { newPoints },
                        "MaKH = ?", new Object[] { maKH });
                loadData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập điểm tích lũy hợp lệ!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật điểm tích lũy: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(97, 187, 252));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 35));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 150, 200));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(97, 187, 252));
            }
        });
    }
}