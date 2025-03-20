package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryPanel extends JPanel {
    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;
    private JTable inventoryTable;
    private JTextField searchField;

    public InventoryPanel() {
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel title = new JLabel("Quản lý kho hàng", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] columnNames = {"Mã SP", "Tên sản phẩm", "Tồn dư", "Đơn vị", "Giá nhập (VNĐ)", "Danh mục"};
        tableModel = new DefaultTableModel(columnNames, 0);
        inventoryTable = new JTable(tableModel);
        inventoryTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 12));
        inventoryTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 12));
        inventoryTable.setRowHeight(25);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
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
        searchButton.addActionListener(e -> searchInventory());

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

        addButton.addActionListener(e -> addInventory());
        editButton.addActionListener(e -> editInventory());
        deleteButton.addActionListener(e -> deleteInventory());
        refreshButton.addActionListener(e -> loadData());

        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(controlPanel, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            ResultSet rs = dbManager.select("KHO", new String[]{"MaSP", "TenSP", "TonDu", "DonVi", "GiaNhap", "DanhMuc"}, "");
            int rowCount = 0;
            while (rs.next()) {
                String maSP = rs.getString("MaSP");
                String tenSP = rs.getString("TenSP");
                int tonDu = rs.getInt("TonDu");
                String donVi = rs.getString("DonVi");
                int giaNhap = rs.getInt("GiaNhap");
                String danhMuc = rs.getString("DanhMuc");
                System.out.println("Dữ liệu KHO: " + maSP + ", " + tenSP + ", " + tonDu + ", " + donVi + ", " + giaNhap + ", " + danhMuc);
                tableModel.addRow(new Object[]{
                        maSP,
                        tenSP,
                        tonDu,
                        donVi,
                        giaNhap,
                        danhMuc
                });
                rowCount++;
            }
            System.out.println("Số dòng dữ liệu hiển thị trong InventoryPanel: " + rowCount);
            tableModel.fireTableDataChanged();
            rs.close();
        } catch (SQLException e) {
            System.out.println("Lỗi tải dữ liệu kho hàng: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu kho hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchInventory() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        try {
            String whereClause = "TenSP LIKE '%" + keyword + "%'";
            ResultSet rs = dbManager.select("KHO", new String[]{"MaSP", "TenSP", "TonDu", "DonVi", "GiaNhap", "DanhMuc"}, whereClause);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getInt("TonDu"),
                        rs.getString("DonVi"),
                        rs.getInt("GiaNhap"),
                        rs.getString("DanhMuc")
                });
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Lỗi tìm kiếm kho hàng: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addInventory() {
        JTextField maSpField = new JTextField(10);
        JTextField tenSpField = new JTextField(10);
        JTextField tonDuField = new JTextField(5);
        JTextField donViField = new JTextField(10);
        JTextField giaNhapField = new JTextField(10);
        JTextField danhMucField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("Mã sản phẩm:"));
        panel.add(maSpField);
        panel.add(new JLabel("Tên sản phẩm:"));
        panel.add(tenSpField);
        panel.add(new JLabel("Tồn dư:"));
        panel.add(tonDuField);
        panel.add(new JLabel("Đơn vị:"));
        panel.add(donViField);
        panel.add(new JLabel("Giá nhập (VNĐ):"));
        panel.add(giaNhapField);
        panel.add(new JLabel("Danh mục:"));
        panel.add(danhMucField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm sản phẩm", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String maSp = maSpField.getText();
                String tenSp = tenSpField.getText();
                int tonDu = Integer.parseInt(tonDuField.getText());
                String donVi = donViField.getText();
                int giaNhap = Integer.parseInt(giaNhapField.getText());
                String danhMuc = danhMucField.getText();
                dbManager.insert("KHO", new Object[]{maSp, tenSp, tonDu, donVi, giaNhap, danhMuc});
                JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
                loadData();
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editInventory() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm để sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maSp = (String) tableModel.getValueAt(selectedRow, 0);
        JTextField tenSpField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString(), 10);
        JTextField tonDuField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString(), 5);
        JTextField donViField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString(), 10);
        JTextField giaNhapField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString(), 10);
        JTextField danhMucField = new JTextField(tableModel.getValueAt(selectedRow, 5).toString(), 10);

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Tên sản phẩm:"));
        panel.add(tenSpField);
        panel.add(new JLabel("Tồn dư:"));
        panel.add(tonDuField);
        panel.add(new JLabel("Đơn vị:"));
        panel.add(donViField);
        panel.add(new JLabel("Giá nhập (VNĐ):"));
        panel.add(giaNhapField);
        panel.add(new JLabel("Danh mục:"));
        panel.add(danhMucField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa sản phẩm", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String tenSp = tenSpField.getText();
                int tonDu = Integer.parseInt(tonDuField.getText());
                String donVi = donViField.getText();
                int giaNhap = Integer.parseInt(giaNhapField.getText());
                String danhMuc = danhMucField.getText();
                dbManager.update("KHO", new String[]{"TenSP", "TonDu", "DonVi", "GiaNhap", "DanhMuc"},
                        new Object[]{tenSp, tonDu, donVi, giaNhap, danhMuc}, "MaSP = '" + maSp + "'");
                JOptionPane.showMessageDialog(this, "Sửa sản phẩm thành công!");
                loadData();
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Lỗi sửa sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteInventory() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maSp = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa sản phẩm này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbManager.delete("KHO", "MaSP = '" + maSp + "'");
                JOptionPane.showMessageDialog(this, "Xóa sản phẩm thành công!");
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}