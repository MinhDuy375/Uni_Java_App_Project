package views;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PromotionsPanel extends JPanel {
    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;
    private JTable promotionsTable;
    private JTextField searchField;
    private List<PromotionChangeListener> promotionChangeListeners;
    private String userRole;

    public PromotionsPanel(String userRole) {
        this.userRole = userRole;
        dbManager = new DatabaseManager();
        promotionChangeListeners = new ArrayList<>();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Quản lý khuyến mãi", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        String[] columnNames = { "Mã KM", "Tên chương trình", "Mức KM", "Thời gian bắt đầu", "Thời gian kết thúc",
                "Trạng thái" };
        tableModel = new DefaultTableModel(columnNames, 0);
        promotionsTable = new JTable(tableModel);
        promotionsTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        promotionsTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        promotionsTable.setRowHeight(25);
        promotionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        promotionsTable.getTableHeader().setBackground(new Color(97, 187, 252));
        promotionsTable.getTableHeader().setForeground(Color.WHITE);

        promotionsTable.setShowGrid(false);
        promotionsTable.setIntercellSpacing(new Dimension(0, 0));
        promotionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 241, 249));
                }
                if (column == 0 || column == 2) { // Căn giữa cột "Mã KM" và "Mức KM"
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(promotionsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchField = new JTextField(20);
        JButton searchButton = new JButton("Tìm kiếm");

        styleButton(searchButton);

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        controlPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        JButton refreshButton = new JButton("Làm mới");
        styleButton(refreshButton);

        if (!"user".equals(userRole)) {
            JButton addButton = new JButton("Thêm");
            JButton editButton = new JButton("Sửa");
            JButton deleteButton = new JButton("Xóa");

            styleButton(addButton);
            styleButton(editButton);
            styleButton(deleteButton);

            addButton.addActionListener(e -> addPromotion());
            editButton.addActionListener(e -> editPromotion());
            deleteButton.addActionListener(e -> deletePromotion());

            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
        }

        buttonPanel.add(refreshButton);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(controlPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> {
            loadData();
            notifyPromotionChanged();
        });
        searchButton.addActionListener(e -> searchPromotions());

        loadData();
    }

    public void addPromotionChangeListener(PromotionChangeListener listener) {
        promotionChangeListeners.add(listener);
    }

    private void notifyPromotionChanged() {
        for (PromotionChangeListener listener : promotionChangeListeners) {
            listener.onPromotionChanged();
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            ResultSet rs = dbManager.select("KHUYEN_MAI",
                    new String[] { "MaKM", "TenChuongTrinh", "MucKM", "TGBatDau", "TGKetThuc", "TrangThai" }, "");
            int rowCount = 0;
            while (rs.next()) {
                String maKM = rs.getString("MaKM");
                String tenChuongTrinh = rs.getString("TenChuongTrinh");
                double mucKM = rs.getDouble("MucKM");
                String tgBatDau = rs.getString("TGBatDau");
                String tgKetThuc = rs.getString("TGKetThuc");
                String trangThai = rs.getString("TrangThai");

                int discountPercentage = (int) ((1 - mucKM) * 100);
                String mucKMDisplay = discountPercentage + "%";

                tableModel.addRow(new Object[] {
                        maKM,
                        tenChuongTrinh,
                        mucKMDisplay,
                        tgBatDau,
                        tgKetThuc,
                        trangThai
                });
                rowCount++;
            }
            tableModel.fireTableDataChanged();
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu khuyến mãi: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchPromotions() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        try {
            String whereClause = "TenChuongTrinh LIKE ?";
            ResultSet rs = dbManager.select("KHUYEN_MAI",
                    new String[] { "MaKM", "TenChuongTrinh", "MucKM", "TGBatDau", "TGKetThuc", "TrangThai" },
                    whereClause, new Object[] { "%" + keyword + "%" });
            while (rs.next()) {
                double mucKM = rs.getDouble("MucKM");
                int discountPercentage = (int) ((1 - mucKM) * 100);
                String mucKMDisplay = discountPercentage + "%";

                tableModel.addRow(new Object[] {
                        rs.getString("MaKM"),
                        rs.getString("TenChuongTrinh"),
                        mucKMDisplay,
                        rs.getString("TGBatDau"),
                        rs.getString("TGKetThuc"),
                        rs.getString("TrangThai")
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPromotion() {
        if ("user".equals(userRole)) {
            JOptionPane.showMessageDialog(this, "Nhân viên không có quyền thêm khuyến mãi!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField maKmField = new JTextField(10);
        JTextField tenChuongTrinhField = new JTextField(10);
        JTextField mucKmField = new JTextField("0.8", 10);
        JTextField tgBatDauField = new JTextField(10);
        JTextField tgKetThucField = new JTextField(10);
        JComboBox<String> trangThaiCombo = new JComboBox<>(new String[] { "Hoạt động", "Kết thúc" });

        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("Mã khuyến mãi:"));
        panel.add(maKmField);
        panel.add(new JLabel("Tên chương trình:"));
        panel.add(tenChuongTrinhField);
        panel.add(new JLabel("Mức khuyến mãi (0.0 - 1.0):"));
        panel.add(mucKmField);
        panel.add(new JLabel("Thời gian bắt đầu (yyyy-MM-dd HH:mm:ss):"));
        panel.add(tgBatDauField);
        panel.add(new JLabel("Thời gian kết thúc (yyyy-MM-dd HH:mm:ss):"));
        panel.add(tgKetThucField);
        panel.add(new JLabel("Trạng thái:"));
        panel.add(trangThaiCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm khuyến mãi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String maKm = maKmField.getText();
                String tenChuongTrinh = tenChuongTrinhField.getText();
                String mucKmStr = mucKmField.getText();
                String tgBatDau = tgBatDauField.getText();
                String tgKetThuc = tgKetThucField.getText();
                String trangThai = (String) trangThaiCombo.getSelectedItem();

                if (maKm.isEmpty() || tenChuongTrinh.isEmpty() || mucKmStr.isEmpty() || tgBatDau.isEmpty()
                        || tgKetThuc.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double mucKm;
                try {
                    mucKm = Double.parseDouble(mucKmStr);
                    if (mucKm < 0.0 || mucKm > 1.0) {
                        JOptionPane.showMessageDialog(this, "Mức khuyến mãi phải nằm trong khoảng từ 0.0 đến 1.0!",
                                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Mức khuyến mãi phải là một số hợp lệ!", "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                dbManager.insert("KHUYEN_MAI",
                        new Object[] { maKm, tenChuongTrinh, mucKm, tgBatDau, tgKetThuc, trangThai });
                JOptionPane.showMessageDialog(this, "Thêm khuyến mãi thành công!");
                loadData();
                notifyPromotionChanged();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm khuyến mãi: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editPromotion() {
        if ("user".equals(userRole)) {
            JOptionPane.showMessageDialog(this, "Nhân viên không có quyền sửa khuyến mãi!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = promotionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi để sửa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maKm = (String) tableModel.getValueAt(selectedRow, 0);
        JTextField tenChuongTrinhField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString(), 10);

        double mucKmValue = 0.0;
        try {
            ResultSet rs = dbManager.select("KHUYEN_MAI", new String[] { "MucKM" }, "MaKM='" + maKm + "'");
            if (rs.next()) {
                mucKmValue = rs.getDouble("MucKM");
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy thông tin khuyến mãi: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField mucKmField = new JTextField(String.valueOf(mucKmValue), 10);
        JTextField tgBatDauField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString(), 10);
        JTextField tgKetThucField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString(), 10);
        JComboBox<String> trangThaiCombo = new JComboBox<>(new String[] { "Hoạt động", "Kết thúc" });
        trangThaiCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 5).toString());

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Tên chương trình:"));
        panel.add(tenChuongTrinhField);
        panel.add(new JLabel("Mức khuyến mãi (0.0 - 1.0):"));
        panel.add(mucKmField);
        panel.add(new JLabel("Thời gian bắt đầu (yyyy-MM-dd HH:mm:ss):"));
        panel.add(tgBatDauField);
        panel.add(new JLabel("Thời gian kết thúc (yyyy-MM-dd HH:mm:ss):"));
        panel.add(tgKetThucField);
        panel.add(new JLabel("Trạng thái:"));
        panel.add(trangThaiCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa khuyến mãi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String tenChuongTrinh = tenChuongTrinhField.getText();
                String mucKmStr = mucKmField.getText();
                String tgBatDau = tgBatDauField.getText();
                String tgKetThuc = tgKetThucField.getText();
                String trangThai = (String) trangThaiCombo.getSelectedItem();

                if (tenChuongTrinh.isEmpty() || mucKmStr.isEmpty() || tgBatDau.isEmpty() || tgKetThuc.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double mucKm;
                try {
                    mucKm = Double.parseDouble(mucKmStr);
                    if (mucKm < 0.0 || mucKm > 1.0) {
                        JOptionPane.showMessageDialog(this, "Mức khuyến mãi phải nằm trong khoảng từ 0.0 đến 1.0!",
                                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Mức khuyến mãi phải là một số hợp lệ!", "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                dbManager.update("KHUYEN_MAI",
                        new String[] { "TenChuongTrinh", "MucKM", "TGBatDau", "TGKetThuc", "TrangThai" },
                        new Object[] { tenChuongTrinh, mucKm, tgBatDau, tgKetThuc, trangThai },
                        "MaKM = ?", new Object[] { maKm });
                JOptionPane.showMessageDialog(this, "Sửa khuyến mãi thành công!");
                loadData();
                notifyPromotionChanged();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi sửa khuyến mãi: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deletePromotion() {
        if ("user".equals(userRole)) {
            JOptionPane.showMessageDialog(this, "Nhân viên không có quyền xóa khuyến mãi!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = promotionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi để xóa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maKm = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khuyến mãi này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ResultSet rs = dbManager.select("HOA_DON", new String[] { "MaHD" }, "KhuyenMai = ?",
                        new Object[] { maKm });
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Không thể xóa khuyến mãi này vì đã có hóa đơn sử dụng!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    rs.close();
                    return;
                }
                rs.close();

                dbManager.delete("KHUYEN_MAI", "MaKM = ?", new Object[] { maKm });
                JOptionPane.showMessageDialog(this, "Xóa khuyến mãi thành công!");
                loadData();
                notifyPromotionChanged();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa khuyến mãi: " + e.getMessage(), "Lỗi",
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