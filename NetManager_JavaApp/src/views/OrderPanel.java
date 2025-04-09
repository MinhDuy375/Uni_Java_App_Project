package views;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterComboBox;
    private JTextField searchField;
    private JButton searchButton;

    public OrderPanel() {
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Quản lý đơn đặt món ăn", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] filterOptions = { "MÁY(N)", "Hình thức thanh toán", "Mã hóa đơn" };
        filterComboBox = new JComboBox<>(filterOptions);
        searchField = new JTextField(15);
        searchButton = new JButton("Tìm");

        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            int columnIndex = filterComboBox.getSelectedIndex();
            filterTable(keyword, columnIndex);
        });

        searchPanel.add(filterComboBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = { "Mã hóa đơn", "Mã khách hàng", "Mã nhân viên", "MÁY(N)", "Ngày", "Số tiền", "Khuyến mãi",
                "Hình thức thanh toán" };
        tableModel = new DefaultTableModel(columns, 0);
        orderTable = new JTable(tableModel);

        orderTable.setShowGrid(false);
        orderTable.setIntercellSpacing(new Dimension(0, 0));
        orderTable.setRowHeight(30);
        orderTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        orderTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        orderTable.getTableHeader().setBackground(new Color(97, 187, 252));
        orderTable.getTableHeader().setForeground(Color.WHITE);

        orderTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 241, 249));
                }
                if (column == 0 || column == 5 || column == 6) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(orderTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadOrders();
    }

    private void loadOrders() {
        try {
            ResultSet rs = dbManager.select("HOA_DON hd JOIN BAN_MAY bm ON hd.MaMay = bm.MaMay",
                    new String[] { "hd.MaHD", "hd.MaKH", "hd.MaNV", "bm.SoMay", "hd.Ngay", "hd.SoTien",
                            "hd.KhuyenMai", "hd.HinhThucThanhToan" },
                    "");
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getString("MaHD"),
                        rs.getString("MaKH"),
                        rs.getInt("MaNV"),
                        rs.getString("SoMay"),
                        rs.getString("Ngay"),
                        String.format("%.2f", rs.getDouble("SoTien")),
                        String.format("%.2f", rs.getDouble("KhuyenMai")),
                        rs.getString("HinhThucThanhToan")
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách đơn hàng: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String keyword, int columnIndex) {
        try {
            String whereClause;
            String[] columns = { "hd.MaHD", "hd.MaKH", "hd.MaNV", "bm.SoMay", "hd.Ngay", "hd.SoTien",
                    "hd.KhuyenMai", "hd.HinhThucThanhToan" };
            if (keyword.isEmpty()) {
                whereClause = "";
            } else {
                switch (columnIndex) {
                    case 0: // MÁY(N)
                        whereClause = "bm.SoMay LIKE ?";
                        keyword = "%" + keyword + "%";
                        break;
                    case 1: // Hình thức thanh toán
                        whereClause = "hd.HinhThucThanhToan LIKE ?";
                        keyword = "%" + keyword + "%";
                        break;
                    case 2: // Mã hóa đơn
                        whereClause = "hd.MaHD LIKE ?";
                        keyword = "%" + keyword + "%";
                        break;
                    default:
                        whereClause = "";
                        keyword = "";
                }
            }

            ResultSet rs;
            if (whereClause.isEmpty()) {
                rs = dbManager.select("HOA_DON hd JOIN BAN_MAY bm ON hd.MaMay = bm.MaMay", columns, "");
            } else {
                rs = dbManager.select("HOA_DON hd JOIN BAN_MAY bm ON hd.MaMay = bm.MaMay", columns, whereClause,
                        new Object[] { keyword });
            }

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getString("MaHD"),
                        rs.getString("MaKH"),
                        rs.getInt("MaNV"),
                        rs.getString("SoMay"),
                        rs.getString("Ngay"),
                        String.format("%.2f", rs.getDouble("SoTien")),
                        String.format("%.2f", rs.getDouble("KhuyenMai")),
                        rs.getString("HinhThucThanhToan")
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lọc đơn hàng: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}