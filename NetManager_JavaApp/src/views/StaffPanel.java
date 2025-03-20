package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffPanel extends JPanel {
    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;
    private JTable staffTable;
    private JTextField searchField;

    public StaffPanel() {
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel title = new JLabel("Quản lý nhân viên", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] columnNames = {"Mã NV", "Họ và tên", "Chức vụ", "Ca làm", "Tình trạng", "Số điện thoại"};
        tableModel = new DefaultTableModel(columnNames, 0);
        staffTable = new JTable(tableModel);
        staffTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 12));
        staffTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 12));
        staffTable.setRowHeight(25);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(staffTable);
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
        searchButton.addActionListener(e -> searchStaff());

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

        addButton.addActionListener(e -> addStaff());
        editButton.addActionListener(e -> editStaff());
        deleteButton.addActionListener(e -> deleteStaff());
        refreshButton.addActionListener(e -> loadData());

        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(controlPanel, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        if (tableModel == null) {
            System.out.println("tableModel chưa được khởi tạo!");
            return;
        }

        tableModel.setRowCount(0);
        try {
            ResultSet rs = dbManager.select("NHAN_VIEN", new String[]{"MaNV", "TenNV", "ChucVu", "CaLam", "TinhTrang", "SDT"}, "");
            int rowCount = 0;
            while (rs.next()) {
                String sdt = rs.getString("SDT");
                if (sdt == null || sdt.isEmpty()) {
                    sdt = "Chưa có";
                }
                String caLam = rs.getString("CaLam");
                if (caLam == null || caLam.isEmpty()) {
                    caLam = "Chưa có";
                }
                tableModel.addRow(new Object[]{
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("ChucVu"),
                        caLam,
                        rs.getInt("TinhTrang") == 1 ? "Hoạt động" : "Không hoạt động",
                        sdt
                });
                rowCount++;
            }
            System.out.println("Số dòng dữ liệu hiển thị trong StaffPanel: " + rowCount);
            tableModel.fireTableDataChanged();
            rs.close();
        } catch (SQLException e) {
            System.out.println("Lỗi tải dữ liệu nhân viên: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchStaff() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        try {
            String whereClause = "TenNV LIKE '%" + keyword + "%'";
            ResultSet rs = dbManager.select("NHAN_VIEN", new String[]{"MaNV", "TenNV", "ChucVu", "CaLam", "TinhTrang", "SDT"}, whereClause);
            while (rs.next()) {
                String sdt = rs.getString("SDT");
                if (sdt == null || sdt.isEmpty()) {
                    sdt = "Chưa có";
                }
                String caLam = rs.getString("CaLam");
                if (caLam == null || caLam.isEmpty()) {
                    caLam = "Chưa có";
                }
                tableModel.addRow(new Object[]{
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("ChucVu"),
                        caLam,
                        rs.getInt("TinhTrang") == 1 ? "Hoạt động" : "Không hoạt động",
                        sdt
                });
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Lỗi tìm kiếm nhân viên: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addStaff() {
        JTextField maNvField = new JTextField(10);
        JTextField tenNvField = new JTextField(10);
        JTextField chucVuField = new JTextField(10);
        JTextField caLamField = new JTextField(10);
        JTextField tinhTrangField = new JTextField(10);
        JTextField sdtField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("Mã nhân viên:"));
        panel.add(maNvField);
        panel.add(new JLabel("Họ và tên:"));
        panel.add(tenNvField);
        panel.add(new JLabel("Chức vụ:"));
        panel.add(chucVuField);
        panel.add(new JLabel("Ca làm:"));
        panel.add(caLamField);
        panel.add(new JLabel("Tình trạng (1: Hoạt động, 0: Không hoạt động):"));
        panel.add(tinhTrangField);
        panel.add(new JLabel("Số điện thoại:"));
        panel.add(sdtField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm nhân viên", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String maNv = maNvField.getText();
                String tenNv = tenNvField.getText();
                String chucVu = chucVuField.getText();
                String caLam = caLamField.getText();
                int tinhTrang = Integer.parseInt(tinhTrangField.getText());
                String sdt = sdtField.getText();
                dbManager.insert("NHAN_VIEN", new Object[]{maNv, tenNv, chucVu, caLam, tinhTrang, sdt});
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
                loadData();
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maNv = (String) tableModel.getValueAt(selectedRow, 0);
        JTextField tenNvField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString(), 10);
        JTextField chucVuField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString(), 10);
        JTextField caLamField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString(), 10);
        JTextField tinhTrangField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString().equals("Hoạt động") ? "1" : "0", 10);
        JTextField sdtField = new JTextField(tableModel.getValueAt(selectedRow, 5).toString(), 10);

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Họ và tên:"));
        panel.add(tenNvField);
        panel.add(new JLabel("Chức vụ:"));
        panel.add(chucVuField);
        panel.add(new JLabel("Ca làm:"));
        panel.add(caLamField);
        panel.add(new JLabel("Tình trạng (1: Hoạt động, 0: Không hoạt động):"));
        panel.add(tinhTrangField);
        panel.add(new JLabel("Số điện thoại:"));
        panel.add(sdtField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa nhân viên", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String tenNv = tenNvField.getText();
                String chucVu = chucVuField.getText();
                String caLam = caLamField.getText();
                int tinhTrang = Integer.parseInt(tinhTrangField.getText());
                String sdt = sdtField.getText();
                dbManager.update("NHAN_VIEN", new String[]{"TenNV", "ChucVu", "CaLam", "TinhTrang", "SDT"},
                        new Object[]{tenNv, chucVu, caLam, tinhTrang, sdt}, "MaNV = '" + maNv + "'");
                JOptionPane.showMessageDialog(this, "Sửa nhân viên thành công!");
                loadData();
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Lỗi sửa nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maNv = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbManager.delete("NHAN_VIEN", "MaNV = '" + maNv + "'");
                JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!");
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}