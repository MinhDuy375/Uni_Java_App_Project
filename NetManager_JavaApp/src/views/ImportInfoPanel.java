package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ImportInfoPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable importTable;
    private JTable detailTable;
    private DefaultTableModel importTableModel;
    private DefaultTableModel detailTableModel;

    public ImportInfoPanel() {
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Split pane to divide the panel into two sections
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);

        // Top section: Import Table
        JPanel importPanel = new JPanel(new BorderLayout());
        importPanel.setBorder(BorderFactory.createTitledBorder("Danh sách nhập hàng"));

        String[] importColumns = { "Mã Nhập", "Ngày Nhập", "Nguồn Nhập", "Tổng Tiền" };
        importTableModel = new DefaultTableModel(importColumns, 0);
        importTable = new JTable(importTableModel);
        importTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        importTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = importTable.getSelectedRow();
                if (selectedRow != -1) {
                    String maNhap = (String) importTable.getValueAt(selectedRow, 0);
                    loadDetailTable(maNhap);
                }
            }
        });

        JScrollPane importScrollPane = new JScrollPane(importTable);
        importPanel.add(importScrollPane, BorderLayout.CENTER);

        // Bottom section: Detail Table
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết nhập hàng"));

        String[] detailColumns = { "Mã Nhập", "Mã Sản Phẩm", "Tên Sản Phẩm", "Đơn Vị", "Danh Mục", "Số Lượng",
                "Giá Nhập" };
        detailTableModel = new DefaultTableModel(detailColumns, 0);
        detailTable = new JTable(detailTableModel);
        JScrollPane detailScrollPane = new JScrollPane(detailTable);
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);

        splitPane.setTopComponent(importPanel);
        splitPane.setBottomComponent(detailPanel);

        add(splitPane, BorderLayout.CENTER);

        // Load initial data
        loadImportTable();
    }

    private void loadImportTable() {
        try {
            importTableModel.setRowCount(0); // Clear existing rows
            ResultSet rs = dbManager.select("NHAP_HANG", new String[] { "MaNhap", "NgayNhap", "NguonNhap", "TongTien" },
                    "");
            while (rs.next()) {
                importTableModel.addRow(new Object[] {
                        rs.getString("MaNhap"),
                        rs.getString("NgayNhap"),
                        rs.getString("NguonNhap"),
                        rs.getInt("TongTien")
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách nhập hàng: " + e.getMessage());
        }
    }

    private void loadDetailTable(String maNhap) {
        try {
            detailTableModel.setRowCount(0); // Clear existing rows
            // Perform a JOIN query to get details from CHI_TIET_NHAP_HANG and KHO
            String query = "SELECT c.MaNhap, c.MaSP, k.TenSP, k.DonVi, k.DanhMuc, c.SoLuong, c.GiaNhap " +
                    "FROM CHI_TIET_NHAP_HANG c " +
                    "LEFT JOIN KHO k ON c.MaSP = k.MaSP " +
                    "WHERE c.MaNhap = ?";
            ResultSet rs = dbManager.select("CHI_TIET_NHAP_HANG c LEFT JOIN KHO k ON c.MaSP = k.MaSP",
                    new String[] { "c.MaNhap", "c.MaSP", "k.TenSP", "k.DonVi", "k.DanhMuc", "c.SoLuong", "c.GiaNhap" },
                    "c.MaNhap = ?",
                    new Object[] { maNhap });
            while (rs.next()) {
                detailTableModel.addRow(new Object[] {
                        rs.getString("MaNhap"),
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getString("DonVi"),
                        rs.getString("DanhMuc"),
                        rs.getInt("SoLuong"),
                        rs.getInt("GiaNhap")
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải chi tiết nhập hàng: " + e.getMessage());
        }
    }
}