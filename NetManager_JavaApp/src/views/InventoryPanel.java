package views;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;

    public InventoryPanel() {
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Danh sách kho hàng", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        String[] columns = { "Mã", "Tên sản phẩm", "Số lượng", "Giá (VNĐ)" };
        tableModel = new DefaultTableModel(columns, 0);
        inventoryTable = new JTable(tableModel);

        inventoryTable.setShowGrid(false);
        inventoryTable.setIntercellSpacing(new Dimension(0, 0));
        inventoryTable.setRowHeight(30);
        inventoryTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        inventoryTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        inventoryTable.getTableHeader().setBackground(new Color(97, 187, 252));
        inventoryTable.getTableHeader().setForeground(Color.WHITE);

        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 241, 249));
                }
                if (column == 0 || column == 3) {
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton refreshButton = new JButton("Làm mới");

        styleButton(addButton);
        styleButton(editButton);
        styleButton(deleteButton);
        styleButton(refreshButton);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addInventory());
        editButton.addActionListener(e -> editInventory());
        deleteButton.addActionListener(e -> deleteInventory());
        refreshButton.addActionListener(e -> loadData());

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            ResultSet rs = dbManager.select("KHO", new String[] { "MaSP", "TenSP", "TonDu", "GiaNhap" }, "");
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getInt("TonDu"),
                        rs.getInt("GiaNhap")
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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

    private void addInventory() {
        JTextField maSpField = new JTextField(10);
        JTextField tenSpField = new JTextField(10);
        JTextField tonDuField = new JTextField(5);
        JTextField giaNhapField = new JTextField(10);
        JTextField giaBanField = new JTextField(10);
        JComboBox<String> danhMucCombo = new JComboBox<>(new String[] { "Đồ ăn", "Đồ uống" });
        JComboBox<String> dangBanCombo = new JComboBox<>(new String[] { "Có", "Không" });

        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("Mã sản phẩm:"));
        panel.add(maSpField);
        panel.add(new JLabel("Tên sản phẩm:"));
        panel.add(tenSpField);
        panel.add(new JLabel("Số lượng:"));
        panel.add(tonDuField);
        panel.add(new JLabel("Giá nhập (VNĐ):"));
        panel.add(giaNhapField);
        panel.add(new JLabel("Giá bán (VNĐ):"));
        panel.add(giaBanField);
        panel.add(new JLabel("Danh mục:"));
        panel.add(danhMucCombo);
        panel.add(new JLabel("Đang bán:"));
        panel.add(dangBanCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm sản phẩm", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String maSp = maSpField.getText();
                String tenSp = tenSpField.getText();
                int tonDu = Integer.parseInt(tonDuField.getText());
                int giaNhap = Integer.parseInt(giaNhapField.getText());
                double giaBan = Double.parseDouble(giaBanField.getText());
                String danhMuc = (String) danhMucCombo.getSelectedItem();
                String dangBan = (String) dangBanCombo.getSelectedItem();

                if (maSp.isEmpty() || tenSp.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Kiểm tra xem MaSP đã tồn tại trong KHO chưa
                ResultSet rs = dbManager.select("KHO", new String[] { "MaSP" }, "MaSP = ?", new Object[] { maSp });
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Mã sản phẩm đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    rs.close();
                    return;
                }
                rs.close();

                // Thêm vào bảng KHO
                dbManager.insert("KHO", new Object[] { maSp, tenSp, tonDu, null, giaNhap, null });

                // Thêm vào bảng MON_AN
                dbManager.insert("MON_AN", new Object[] { maSp, tenSp, giaBan, danhMuc, dangBan });

                loadData();
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm sản phẩm: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editInventory() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để sửa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maSp = tableModel.getValueAt(selectedRow, 0).toString();
        JTextField tenSpField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString(), 10);
        JTextField tonDuField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString(), 5);
        JTextField giaNhapField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString(), 10);
        JTextField giaBanField = new JTextField(10);
        JComboBox<String> danhMucCombo = new JComboBox<>(new String[] { "Đồ ăn", "Đồ uống" });
        JComboBox<String> dangBanCombo = new JComboBox<>(new String[] { "Có", "Không" });

        // Lấy thông tin hiện tại từ bảng MON_AN
        try {
            ResultSet rs = dbManager.select("MON_AN", new String[] { "GiaBan", "DanhMuc", "DangBan" }, "MaSP = ?",
                    new Object[] { maSp });
            if (rs.next()) {
                giaBanField.setText(String.valueOf(rs.getDouble("GiaBan")));
                danhMucCombo.setSelectedItem(rs.getString("DanhMuc"));
                dangBanCombo.setSelectedItem(rs.getString("DangBan") != null ? rs.getString("DangBan") : "Không");
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy thông tin món: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Tên sản phẩm:"));
        panel.add(tenSpField);
        panel.add(new JLabel("Số lượng:"));
        panel.add(tonDuField);
        panel.add(new JLabel("Giá nhập (VNĐ):"));
        panel.add(giaNhapField);
        panel.add(new JLabel("Giá bán (VNĐ):"));
        panel.add(giaBanField);
        panel.add(new JLabel("Danh mục:"));
        panel.add(danhMucCombo);
        panel.add(new JLabel("Đang bán:"));
        panel.add(dangBanCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa sản phẩm", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String tenSp = tenSpField.getText();
                int tonDu = Integer.parseInt(tonDuField.getText());
                int giaNhap = Integer.parseInt(giaNhapField.getText());
                double giaBan = Double.parseDouble(giaBanField.getText());
                String danhMuc = (String) danhMucCombo.getSelectedItem();
                String dangBan = (String) dangBanCombo.getSelectedItem();

                // Cập nhật bảng KHO
                dbManager.update("KHO", new String[] { "TenSP", "TonDu", "GiaNhap" },
                        new Object[] { tenSp, tonDu, giaNhap }, "MaSP = ?", new Object[] { maSp });

                // Cập nhật bảng MON_AN
                dbManager.update("MON_AN", new String[] { "TenSP", "GiaBan", "DanhMuc", "DangBan" },
                        new Object[] { tenSp, giaBan, danhMuc, dangBan }, "MaSP = ?", new Object[] { maSp });

                loadData();
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Lỗi sửa sản phẩm: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteInventory() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để xóa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maSp = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa sản phẩm này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Kiểm tra xem sản phẩm có được sử dụng trong CHI_TIET_HOA_DON không
                ResultSet rs = dbManager.select("CHI_TIET_HOA_DON", new String[] { "MaSP" }, "MaSP = ?",
                        new Object[] { maSp });
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Không thể xóa sản phẩm này vì đã có hóa đơn liên quan!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    rs.close();
                    return;
                }
                rs.close();

                // Xóa sản phẩm từ bảng KHO
                dbManager.delete("KHO", "MaSP = ?", new Object[] { maSp });

                // Xóa sản phẩm từ bảng MON_AN
                dbManager.delete("MON_AN", "MaSP = ?", new Object[] { maSp });

                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa sản phẩm: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}