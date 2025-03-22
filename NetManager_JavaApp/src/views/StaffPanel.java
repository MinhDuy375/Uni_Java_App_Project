package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private SearchPanel searchPanel;

    public StaffPanel() {
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Quản lý nhân viên", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        String[] columns = { "Mã NV", "Họ và Tên", "Chức vụ", "Số điện thoại" };
        tableModel = new DefaultTableModel(columns, 0);
        staffTable = new JTable(tableModel);
        staffTable.setRowHeight(30);
        staffTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(staffTable);
        add(scrollPane, BorderLayout.CENTER);

        String[] filterOptions = { "Mã NV", "Họ và Tên" };
        searchPanel = new SearchPanel(filterOptions, e -> {
            String[] data = e.getActionCommand().split(";");
            String keyword = data[0];
            int columnIndex = Integer.parseInt(data[1]);
            filterTable(keyword, columnIndex);
        });
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

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            ResultSet rs = dbManager.select("NHAN_VIEN", new String[] { "MaNV", "TenNV", "ChucVu", "SDT" }, "");
            while (rs.next()) {
                String sdt = rs.getString("SDT");
                if (sdt == null || sdt.isEmpty())
                    sdt = "Chưa có";
                tableModel.addRow(new Object[] {
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("ChucVu"),
                        sdt
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String keyword, int columnIndex) {
        DefaultTableModel filteredModel = new DefaultTableModel(
                new String[] { "Mã NV", "Họ và Tên", "Chức vụ", "Số điện thoại" }, 0);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String cellValue = tableModel.getValueAt(i, columnIndex).toString().toLowerCase();
            if (cellValue.contains(keyword.toLowerCase())) {
                filteredModel.addRow(new Object[] {
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
        JTextField maNvField = new JTextField(10);
        JTextField tenNvField = new JTextField(10);
        JTextField chucVuField = new JTextField(10);
        JTextField sdtField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Mã nhân viên:"));
        panel.add(maNvField);
        panel.add(new JLabel("Họ và Tên:"));
        panel.add(tenNvField);
        panel.add(new JLabel("Chức vụ:"));
        panel.add(chucVuField);
        panel.add(new JLabel("Số điện thoại:"));
        panel.add(sdtField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm nhân viên", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String maNv = maNvField.getText();
                String tenNv = tenNvField.getText();
                String chucVu = chucVuField.getText();
                String sdt = sdtField.getText();
                dbManager.insert("NHAN_VIEN", new Object[] { maNv, tenNv, chucVu, null, 1, sdt });
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm nhân viên: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maNv = tableModel.getValueAt(selectedRow, 0).toString();
        JTextField tenNvField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString(), 10);
        JTextField chucVuField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString(), 10);
        JTextField sdtField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString(), 10);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Họ và Tên:"));
        panel.add(tenNvField);
        panel.add(new JLabel("Chức vụ:"));
        panel.add(chucVuField);
        panel.add(new JLabel("Số điện thoại:"));
        panel.add(sdtField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa nhân viên", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String tenNv = tenNvField.getText();
                String chucVu = chucVuField.getText();
                String sdt = sdtField.getText();
                dbManager.update("NHAN_VIEN", new String[] { "TenNV", "ChucVu", "SDT" },
                        new Object[] { tenNv, chucVu, sdt }, "MaNV = '" + maNv + "'");
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi sửa nhân viên: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maNv = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dbManager.delete("NHAN_VIEN", "MaNV = '" + maNv + "'");
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa nhân viên: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}