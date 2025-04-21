package views;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private SearchPanel searchPanel;
    private TableRowSorter<DefaultTableModel> sorter;
    private String userRole;

    public StaffPanel(String userRole) {
        this.userRole = userRole;
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Quản lý nhân viên", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        String[] columns = { "Mã NV", "TênNV", "Chức vụ", "Ca làm", "Số điện thoại" };
        tableModel = new DefaultTableModel(columns, 0);
        staffTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        staffTable.setRowSorter(sorter);

        staffTable.setRowHeight(30);
        staffTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        staffTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        staffTable.getTableHeader().setBackground(new Color(97, 187, 252));
        staffTable.getTableHeader().setForeground(Color.WHITE);

        staffTable.setShowGrid(false);
        staffTable.setIntercellSpacing(new Dimension(0, 0));
        staffTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 241, 249));
                }
                if (column == 0) {
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        String[] filterOptions = { "Mã NV", "TênNV", "Chức vụ", "Ca làm", "Số điện thoại" };
        searchPanel = new SearchPanel(filterOptions, e -> {
            try {
                String[] data = e.getActionCommand().split(";");
                if (data.length != 2) {
                    JOptionPane.showMessageDialog(this, "Dữ liệu tìm kiếm không hợp lệ!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String keyword = data[0].trim();
                int columnIndex = Integer.parseInt(data[1]);
                if (keyword.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                filterTable(keyword, columnIndex);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi định dạng cột tìm kiếm! Vui lòng thử lại.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
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

            addButton.addActionListener(e -> addStaff());
            editButton.addActionListener(e -> editStaff());
            deleteButton.addActionListener(e -> deleteStaff());

            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
        }

        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> {
            sorter.setRowFilter(null);
            loadData();
        });

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            ResultSet rs = dbManager.select("NHAN_VIEN", new String[] { "MaNV", "TenNV", "SDT", "ChucVu", "CaLam" },
                    "");
            while (rs.next()) {
                String sdt = rs.getString("SDT");
                String caLam = rs.getString("CaLam");
                if (sdt == null || sdt.isEmpty())
                    sdt = "Chưa có";
                if (caLam == null || caLam.isEmpty())
                    caLam = "Không có ca";
                tableModel.addRow(new Object[] {
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("ChucVu"),
                        caLam,
                        sdt
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable(String keyword, int columnIndex) {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, columnIndex));
    }

    private void addStaff() {
        if ("user".equals(userRole)) {
            JOptionPane.showMessageDialog(this, "Nhân viên không có quyền thêm nhân viên!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField maNvField = new JTextField(10);
        JTextField tenNvField = new JTextField(10);
        JComboBox<String> chucVuComboBox = new JComboBox<>(new String[] { "user", "admin" });
        JComboBox<String> caLamComboBox = new JComboBox<>(new String[] { "Không có ca", "Ca1", "Ca2", "Ca3" });
        JTextField sdtField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Mã nhân viên:"));
        panel.add(maNvField);
        panel.add(new JLabel("TênNV:"));
        panel.add(tenNvField);
        panel.add(new JLabel("Chức vụ:"));
        panel.add(chucVuComboBox);
        panel.add(new JLabel("Ca làm:"));
        panel.add(caLamComboBox);
        panel.add(new JLabel("Số điện thoại:"));
        panel.add(sdtField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm nhân viên", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String maNv = maNvField.getText().trim();
            String tenNv = tenNvField.getText().trim();
            String chucVu = chucVuComboBox.getSelectedItem().toString();
            String caLam = caLamComboBox.getSelectedItem().toString();
            String sdt = sdtField.getText().trim();

            if (maNv.isEmpty() || tenNv.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin (Mã NV, TênNV)!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Integer maNvValue;
            try {
                maNvValue = Integer.parseInt(maNv);
                if (maNvValue < Integer.MIN_VALUE || maNvValue > Integer.MAX_VALUE) {
                    JOptionPane.showMessageDialog(this, "Mã nhân viên phải nằm trong khoảng từ " + Integer.MIN_VALUE
                            + " đến " + Integer.MAX_VALUE + "!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Mã nhân viên phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tenNv.length() > 50) {
                JOptionPane.showMessageDialog(this, "Tên nhân viên không được dài quá 50 ký tự!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!sdt.isEmpty()) {
                if (!sdt.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại chỉ được chứa ký tự số!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (sdt.length() > 10) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại không được dài quá 10 ký tự!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Object caLamValue = caLam.equals("Không có ca") ? null : caLam;

            try {
                dbManager.insert("NHAN_VIEN",
                        new Object[] { maNvValue, tenNv, sdt.isEmpty() ? null : sdt, chucVu, caLamValue, true });
                loadData();
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                String errorMsg = e.getMessage();
                if (errorMsg.contains("unique constraint") || errorMsg.contains("duplicate key")) {
                    errorMsg = "Mã nhân viên đã tồn tại!";
                } else {
                    errorMsg = "Lỗi khi thêm nhân viên: " + e.getMessage();
                }
                JOptionPane.showMessageDialog(this, errorMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editStaff() {
        if ("user".equals(userRole)) {
            JOptionPane.showMessageDialog(this, "Nhân viên không có quyền sửa nhân viên!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maNv = tableModel.getValueAt(selectedRow, 0).toString();
        JTextField tenNvField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString(), 10);
        JComboBox<String> chucVuComboBox = new JComboBox<>(new String[] { "user", "admin" });
        chucVuComboBox.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
        JComboBox<String> caLamComboBox = new JComboBox<>(new String[] { "Không có ca", "Ca1", "Ca2", "Ca3" });
        String currentCaLam = tableModel.getValueAt(selectedRow, 3).toString();
        caLamComboBox.setSelectedItem(currentCaLam.equals("Không có ca") ? "Không có ca" : currentCaLam);
        JTextField sdtField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString(), 10);

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("TênNV:"));
        panel.add(tenNvField);
        panel.add(new JLabel("Chức vụ:"));
        panel.add(chucVuComboBox);
        panel.add(new JLabel("Ca làm:"));
        panel.add(caLamComboBox);
        panel.add(new JLabel("Số điện thoại:"));
        panel.add(sdtField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa nhân viên", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String tenNv = tenNvField.getText().trim();
            String chucVu = chucVuComboBox.getSelectedItem().toString();
            String caLam = caLamComboBox.getSelectedItem().toString();
            String sdt = sdtField.getText().trim();

            if (tenNv.isEmpty()) {
                JOptionPane.showMessageDialog(this, "TênNV không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tenNv.length() > 50) {
                JOptionPane.showMessageDialog(this, "Tên nhân viên không được dài quá 50 ký tự!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!sdt.isEmpty()) {
                if (!sdt.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại chỉ được chứa ký tự số!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (sdt.length() > 10) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại không được dài quá 10 ký tự!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Object caLamValue = caLam.equals("Không có ca") ? null : caLam;

            try {
                dbManager.update("NHAN_VIEN", new String[] { "TenNV", "SDT", "ChucVu", "CaLam" },
                        new Object[] { tenNv, sdt.isEmpty() ? null : sdt, chucVu, caLamValue }, "MaNV = ?",
                        new Object[] { maNv });
                loadData();
                JOptionPane.showMessageDialog(this, "Sửa nhân viên thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi sửa nhân viên: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteStaff() {
        if ("user".equals(userRole)) {
            JOptionPane.showMessageDialog(this, "Nhân viên không có quyền xóa nhân viên!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

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
                ResultSet rs = dbManager.select("HOA_DON", new String[] { "MaHD" }, "MaNV = ?", new Object[] { maNv });
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Không thể xóa nhân viên này vì đã có hóa đơn liên quan!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    rs.close();
                    return;
                }
                rs.close();

                dbManager.delete("NHAN_VIEN", "MaNV = ?", new Object[] { maNv });
                loadData();
                JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa nhân viên: " + e.getMessage(), "Lỗi",
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