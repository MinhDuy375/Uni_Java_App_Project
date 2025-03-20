package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PromotionsPanel extends JPanel {
    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;
    private JTable promotionsTable;
    private JTextField searchField;

    public PromotionsPanel() {
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel title = new JLabel("Quản lý khuyến mãi", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] columnNames = {"Mã KM", "Tên chương trình", "Mức KM", "Thời gian bắt đầu", "Thời gian kết thúc", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0);
        promotionsTable = new JTable(tableModel);
        promotionsTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 12));
        promotionsTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 12));
        promotionsTable.setRowHeight(25);
        promotionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(promotionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel tìm kiếm và nút
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchField = new JTextField(20);
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setFont(new Font("IBM Plex Mono", Font.PLAIN, 12));
        searchButton.setBackground(new Color(0, 54, 92));
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> searchPromotions());

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        controlPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton refreshButton = new JButton("Làm mới");

        for (JButton button : new JButton[]{addButton, editButton, deleteButton, refreshButton}) {
            button.setFont(new Font("IBM Plex Mono", Font.PLAIN, 12));
            button.setBackground(new Color(97, 187, 252));
            button.setForeground(Color.WHITE);
            buttonPanel.add(button);
        }

        addButton.addActionListener(e -> addPromotion());
        editButton.addActionListener(e -> editPromotion());
        deleteButton.addActionListener(e -> deletePromotion());
        refreshButton.addActionListener(e -> loadData());

        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(controlPanel, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            ResultSet rs = dbManager.select("KHUYEN_MAI", new String[]{"MaKM", "TenChuongTrinh", "MucKM", "TGBatDau", "TGKetThuc", "TrangThai"}, "");
            int rowCount = 0;
            while (rs.next()) {
                String maKM = rs.getString("MaKM");
                String tenChuongTrinh = rs.getString("TenChuongTrinh");
                String mucKM = rs.getString("MucKM");
                String tgBatDau = rs.getString("TGBatDau");
                String tgKetThuc = rs.getString("TGKetThuc");
                String trangThai = rs.getString("TrangThai");
                System.out.println("Dữ liệu KHUYEN_MAI: " + maKM + ", " + tenChuongTrinh + ", " + mucKM + ", " + tgBatDau + ", " + tgKetThuc + ", " + trangThai);
                tableModel.addRow(new Object[]{
                        maKM,
                        tenChuongTrinh,
                        mucKM,
                        tgBatDau,
                        tgKetThuc,
                        trangThai
                });
                rowCount++;
            }
            System.out.println("Số dòng dữ liệu hiển thị trong PromotionsPanel: " + rowCount);
            tableModel.fireTableDataChanged();
            rs.close();
        } catch (SQLException e) {
            System.out.println("Lỗi tải dữ liệu khuyến mãi: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu khuyến mãi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchPromotions() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        try {
            String whereClause = "TenChuongTrinh LIKE '%" + keyword + "%'";
            ResultSet rs = dbManager.select("KHUYEN_MAI", new String[]{"MaKM", "TenChuongTrinh", "MucKM", "TGBatDau", "TGKetThuc", "TrangThai"}, whereClause);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("MaKM"),
                        rs.getString("TenChuongTrinh"),
                        rs.getString("MucKM"),
                        rs.getString("TGBatDau"),
                        rs.getString("TGKetThuc"),
                        rs.getString("TrangThai")
                });
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Lỗi tìm kiếm khuyến mãi: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPromotion() {
        JTextField maKmField = new JTextField(10);
        JTextField tenChuongTrinhField = new JTextField(10);
        JTextField mucKmField = new JTextField(10);
        JTextField tgBatDauField = new JTextField(10);
        JTextField tgKetThucField = new JTextField(10);
        JTextField trangThaiField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("Mã khuyến mãi:"));
        panel.add(maKmField);
        panel.add(new JLabel("Tên chương trình:"));
        panel.add(tenChuongTrinhField);
        panel.add(new JLabel("Mức khuyến mãi:"));
        panel.add(mucKmField);
        panel.add(new JLabel("Thời gian bắt đầu (yyyy-MM-dd HH:mm:ss):"));
        panel.add(tgBatDauField);
        panel.add(new JLabel("Thời gian kết thúc (yyyy-MM-dd HH:mm:ss):"));
        panel.add(tgKetThucField);
        panel.add(new JLabel("Trạng thái:"));
        panel.add(trangThaiField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm khuyến mãi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String maKm = maKmField.getText();
                String tenChuongTrinh = tenChuongTrinhField.getText();
                String mucKm = mucKmField.getText();
                String tgBatDau = tgBatDauField.getText();
                String tgKetThuc = tgKetThucField.getText();
                String trangThai = trangThaiField.getText();
                dbManager.insert("KHUYEN_MAI", new Object[]{maKm, tenChuongTrinh, mucKm, tgBatDau, tgKetThuc, trangThai});
                JOptionPane.showMessageDialog(this, "Thêm khuyến mãi thành công!");
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm khuyến mãi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editPromotion() {
        int selectedRow = promotionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi để sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maKm = (String) tableModel.getValueAt(selectedRow, 0);
        JTextField tenChuongTrinhField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString(), 10);
        JTextField mucKmField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString(), 10);
        JTextField tgBatDauField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString(), 10);
        JTextField tgKetThucField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString(), 10);
        JTextField trangThaiField = new JTextField(tableModel.getValueAt(selectedRow, 5).toString(), 10);

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Tên chương trình:"));
        panel.add(tenChuongTrinhField);
        panel.add(new JLabel("Mức khuyến mãi:"));
        panel.add(mucKmField);
        panel.add(new JLabel("Thời gian bắt đầu (yyyy-MM-dd HH:mm:ss):"));
        panel.add(tgBatDauField);
        panel.add(new JLabel("Thời gian kết thúc (yyyy-MM-dd HH:mm:ss):"));
        panel.add(tgKetThucField);
        panel.add(new JLabel("Trạng thái:"));
        panel.add(trangThaiField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa khuyến mãi", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String tenChuongTrinh = tenChuongTrinhField.getText();
                String mucKm = mucKmField.getText();
                String tgBatDau = tgBatDauField.getText();
                String tgKetThuc = tgKetThucField.getText();
                String trangThai = trangThaiField.getText();
                dbManager.update("KHUYEN_MAI", new String[]{"TenChuongTrinh", "MucKM", "TGBatDau", "TGKetThuc", "TrangThai"},
                        new Object[]{tenChuongTrinh, mucKm, tgBatDau, tgKetThuc, trangThai}, "MaKM = '" + maKm + "'");
                JOptionPane.showMessageDialog(this, "Sửa khuyến mãi thành công!");
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi sửa khuyến mãi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deletePromotion() {
        int selectedRow = promotionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maKm = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khuyến mãi này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbManager.delete("KHUYEN_MAI", "MaKM = '" + maKm + "'");
                JOptionPane.showMessageDialog(this, "Xóa khuyến mãi thành công!");
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa khuyến mãi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}