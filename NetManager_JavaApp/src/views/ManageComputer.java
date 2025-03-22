package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageComputer extends JPanel {
    private DatabaseManager dbManager;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private Map<String, JPanel> computerBlocks;
    private JLabel selectedComputerLabel;
    private JLabel timeLabel;
    private JLabel feeLabel;
    private JTextArea servicesArea;
    private JLabel totalLabel;
    private String selectedComputerId;
    private Timer timer;
    private long startTime;
    private double hourlyRate;
    private boolean isUsing; // Biến để kiểm tra trạng thái sử dụng

    public ManageComputer(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.computerBlocks = new HashMap<>();
        this.isUsing = false;

        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);

        leftPanel = createLeftPanel();
        splitPane.setLeftComponent(leftPanel);

        rightPanel = createRightPanel();
        splitPane.setRightComponent(new JScrollPane(rightPanel));

        add(splitPane, BorderLayout.CENTER);

        timer = new Timer(1000, e -> updateRealTimeInfo());
        timer.start();
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        // Thanh điều khiển (cố định)
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Thêm máy");
        JButton transferButton = new JButton("Chuyển máy");
        JComboBox<String> statusFilter = new JComboBox<>(
                new String[] { "Tất cả", "Sẵn sàng", "Đang sử dụng", "Bảo trì" });

        addButton.addActionListener(e -> addComputer());
        transferButton.addActionListener(e -> transferComputer());
        statusFilter.addActionListener(e -> filterComputers((String) statusFilter.getSelectedItem()));

        controlPanel.add(addButton);
        controlPanel.add(transferButton);
        controlPanel.add(new JLabel("Lọc: "));
        controlPanel.add(statusFilter);

        // Panel chứa danh sách máy (có thể cuộn)
        JPanel computersGrid = new JPanel();
        loadComputers(computersGrid);
        JScrollPane scrollPane = new JScrollPane(computersGrid);

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadComputers(JPanel grid) {
        grid.removeAll();
        try {
            ResultSet rs = dbManager.select("BAN_MAY",
                    new String[] { "MaMay", "SoMay", "TrangThai" },
                    "");
            int cols = 4;
            List<String[]> computers = new ArrayList<>();
            while (rs.next()) {
                computers.add(new String[] {
                        rs.getString("MaMay"),
                        rs.getString("SoMay"),
                        rs.getString("TrangThai")
                });
            }
            rs.close();
            grid.setLayout(new GridLayout((computers.size() + cols - 1) / cols, cols, 20, 20));

            for (String[] computer : computers) {
                JPanel block = createComputerBlock(computer[0], computer[1], computer[2]);
                grid.add(block);
                computerBlocks.put(computer[0], block);
            }
            grid.revalidate();
            grid.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách máy: " + e.getMessage());
        }
    }

    private JPanel createComputerBlock(String id, String soMay, String status) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(100, 100));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));

        switch (status) {
            case "Sẵn sàng":
                panel.setBackground(new Color(204, 255, 204));
                break;
            case "Đang sử dụng":
                panel.setBackground(new Color(255, 204, 204));
                break;
            case "Bảo trì":
                panel.setBackground(new Color(255, 255, 204));
                break;
            default:
                panel.setBackground(new Color(97, 187, 252));
        }

        JLabel label = new JLabel(soMay, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectComputer(id);
            }
        });

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10)); // Thêm 1 hàng cho nút "Sửa thông tin máy"
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        selectedComputerLabel = new JLabel("Chưa chọn máy");
        timeLabel = new JLabel("Thời gian: 00:00:00");
        feeLabel = new JLabel("Tiền giờ: 0 VNĐ");
        servicesArea = new JTextArea("Dịch vụ:\n");
        servicesArea.setEditable(false);
        totalLabel = new JLabel("Tổng tiền: 0 VNĐ");

        JButton startButton = new JButton("Bắt đầu sử dụng");
        JButton payButton = new JButton("Thanh toán");
        JButton printButton = new JButton("In hóa đơn");
        JButton editButton = new JButton("Sửa thông tin máy"); // Nút mới

        startButton.addActionListener(e -> startUsing());
        payButton.addActionListener(e -> processPayment());
        printButton.addActionListener(e -> printInvoice());
        editButton.addActionListener(e -> editComputerInfo());

        panel.add(new JLabel("Máy tính:"));
        panel.add(selectedComputerLabel);
        panel.add(new JLabel("Thời gian:"));
        panel.add(timeLabel);
        panel.add(new JLabel("Tiền giờ:"));
        panel.add(feeLabel);
        panel.add(new JLabel("Dịch vụ:"));
        panel.add(new JScrollPane(servicesArea));
        panel.add(new JLabel("Tổng tiền:"));
        panel.add(totalLabel);
        panel.add(startButton);
        panel.add(payButton);
        panel.add(printButton);
        panel.add(editButton);

        return panel;
    }

    private void selectComputer(String computerId) {
        this.selectedComputerId = computerId;
        updateComputerInfo();
    }

    private void startUsing() {
        if (selectedComputerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một máy trước!");
            return;
        }

        try {
            ResultSet rs = dbManager.select("BAN_MAY",
                    new String[] { "TrangThai" },
                    "MaMay='" + selectedComputerId + "'");
            if (rs.next()) {
                String status = rs.getString("TrangThai");
                if (!status.equals("Sẵn sàng")) {
                    JOptionPane.showMessageDialog(this, "Máy phải ở trạng thái Sẵn sàng để bắt đầu sử dụng!");
                    rs.close();
                    return;
                }
            }
            rs.close();

            dbManager.update("BAN_MAY",
                    new String[] { "TrangThai" },
                    new Object[] { "Đang sử dụng" },
                    "MaMay='" + selectedComputerId + "'");
            startTime = System.currentTimeMillis();
            isUsing = true;
            loadComputers((JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView());
            updateComputerInfo();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi bắt đầu sử dụng: " + e.getMessage());
        }
    }

    private void updateComputerInfo() {
        if (selectedComputerId == null)
            return;

        try {
            ResultSet rs = dbManager.select("BAN_MAY",
                    new String[] { "SoMay", "TrangThai", "GiaThueBan" },
                    "MaMay='" + selectedComputerId + "'");
            if (rs.next()) {
                selectedComputerLabel.setText(rs.getString("SoMay") + " (" + rs.getString("TrangThai") + ")");
                hourlyRate = rs.getDouble("GiaThueBan");
                updateRealTimeInfo();
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật thông tin: " + e.getMessage());
        }
    }

    private void updateRealTimeInfo() {
        if (selectedComputerId != null && isUsing) {
            long currentTime = System.currentTimeMillis();
            long secondsUsed = (currentTime - startTime) / 1000;
            long hours = secondsUsed / 3600;
            long minutes = (secondsUsed % 3600) / 60;
            long seconds = secondsUsed % 60;

            double hoursUsed = secondsUsed / 3600.0;
            double fee = hoursUsed * hourlyRate;
            int serviceFee = 0;

            timeLabel.setText(String.format("Thời gian: %02d:%02d:%02d", hours, minutes, seconds));
            feeLabel.setText(String.format("Tiền giờ: %.0f VNĐ", fee));
            totalLabel.setText(String.format("Tổng tiền: %.0f VNĐ", fee + serviceFee));
        }
    }

    private void addComputer() {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame owner = (window instanceof Frame) ? (Frame) window : null;

        JDialog dialog = new JDialog(owner, "Thêm máy mới", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JTextField maMayField = new JTextField();
        JTextField soMayField = new JTextField();
        JTextField giaThueField = new JTextField();
        JComboBox<String> trangThaiCombo = new JComboBox<>(new String[] { "Sẵn sàng", "Đang sử dụng", "Bảo trì" });
        JButton saveButton = new JButton("Lưu");

        dialog.add(new JLabel("Mã máy:"));
        dialog.add(maMayField);
        dialog.add(new JLabel("Số máy:"));
        dialog.add(soMayField);
        dialog.add(new JLabel("Giá thuê (VNĐ/giờ):"));
        dialog.add(giaThueField);
        dialog.add(new JLabel("Trạng thái:"));
        dialog.add(trangThaiCombo);
        dialog.add(new JLabel());
        dialog.add(saveButton);

        saveButton.addActionListener(e -> {
            String maMay = maMayField.getText();
            String soMay = soMayField.getText();
            String giaThue = giaThueField.getText();
            String trangThai = (String) trangThaiCombo.getSelectedItem();

            if (maMay.isEmpty() || soMay.isEmpty() || giaThue.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!");
                return;
            }

            try {
                dbManager.insert("BAN_MAY", new Object[] { maMay, soMay, Double.parseDouble(giaThue), trangThai });
                loadComputers((JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView());
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi thêm máy: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá thuê phải là số!");
            }
        });

        dialog.setVisible(true);
    }

    private void editComputerInfo() {
        if (selectedComputerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một máy để sửa thông tin!");
            return;
        }

        Window window = SwingUtilities.getWindowAncestor(this);
        Frame owner = (window instanceof Frame) ? (Frame) window : null;

        JDialog dialog = new JDialog(owner, "Sửa thông tin máy", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JTextField maMayField = new JTextField(selectedComputerId);
        maMayField.setEditable(false); // Không cho phép sửa mã máy
        JTextField soMayField = new JTextField();
        JTextField giaThueField = new JTextField();
        JComboBox<String> trangThaiCombo = new JComboBox<>(new String[] { "Sẵn sàng", "Đang sử dụng", "Bảo trì" });
        JButton saveButton = new JButton("Lưu");

        // Lấy thông tin hiện tại của máy
        try {
            ResultSet rs = dbManager.select("BAN_MAY",
                    new String[] { "SoMay", "GiaThueBan", "TrangThai" },
                    "MaMay='" + selectedComputerId + "'");
            if (rs.next()) {
                soMayField.setText(rs.getString("SoMay"));
                giaThueField.setText(String.valueOf(rs.getDouble("GiaThueBan")));
                trangThaiCombo.setSelectedItem(rs.getString("TrangThai"));
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Lỗi lấy thông tin máy: " + e.getMessage());
            return;
        }

        dialog.add(new JLabel("Mã máy:"));
        dialog.add(maMayField);
        dialog.add(new JLabel("Số máy:"));
        dialog.add(soMayField);
        dialog.add(new JLabel("Giá thuê (VNĐ/giờ):"));
        dialog.add(giaThueField);
        dialog.add(new JLabel("Trạng thái:"));
        dialog.add(trangThaiCombo);
        dialog.add(new JLabel());
        dialog.add(saveButton);

        saveButton.addActionListener(e -> {
            String soMay = soMayField.getText();
            String giaThue = giaThueField.getText();
            String trangThai = (String) trangThaiCombo.getSelectedItem();

            if (soMay.isEmpty() || giaThue.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!");
                return;
            }

            try {
                dbManager.update("BAN_MAY",
                        new String[] { "SoMay", "GiaThueBan", "TrangThai" },
                        new Object[] { soMay, Double.parseDouble(giaThue), trangThai },
                        "MaMay='" + selectedComputerId + "'");
                loadComputers((JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView());
                updateComputerInfo();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi sửa thông tin máy: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá thuê phải là số!");
            }
        });

        dialog.setVisible(true);
    }

    private void transferComputer() {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame owner = (window instanceof Frame) ? (Frame) window : null;

        JDialog dialog = new JDialog(owner, "Chuyển máy", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        JComboBox<String> fromCombo = new JComboBox<>();
        JComboBox<String> toCombo = new JComboBox<>();
        JButton transferButton = new JButton("Chuyển");

        try {
            ResultSet rs = dbManager.select("BAN_MAY", new String[] { "MaMay", "SoMay" }, "");
            while (rs.next()) {
                fromCombo.addItem(rs.getString("MaMay") + " - " + rs.getString("SoMay"));
                toCombo.addItem(rs.getString("MaMay") + " - " + rs.getString("SoMay"));
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Lỗi tải danh sách máy: " + e.getMessage());
            return;
        }

        dialog.add(new JLabel("Máy nguồn:"));
        dialog.add(fromCombo);
        dialog.add(new JLabel("Máy đích:"));
        dialog.add(toCombo);
        dialog.add(new JLabel());
        dialog.add(transferButton);

        transferButton.addActionListener(e -> {
            String fromSelection = (String) fromCombo.getSelectedItem();
            String toSelection = (String) toCombo.getSelectedItem();

            if (fromSelection == null || toSelection == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn máy nguồn và máy đích!");
                return;
            }

            String fromId = fromSelection.split(" - ")[0];
            String toId = toSelection.split(" - ")[0];

            if (fromId.equals(toId)) {
                JOptionPane.showMessageDialog(dialog, "Máy nguồn và máy đích không được trùng!");
                return;
            }

            String toStatus = null;
            try {
                ResultSet rs = dbManager.select("BAN_MAY", new String[] { "TrangThai" }, "MaMay='" + toId + "'");
                if (rs.next()) {
                    toStatus = rs.getString("TrangThai");
                }
                rs.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi kiểm tra trạng thái máy đích: " + ex.getMessage());
                return;
            }

            if (!"Sẵn sàng".equals(toStatus)) {
                JOptionPane.showMessageDialog(dialog, "Máy đích phải ở trạng thái Sẵn sàng!");
                return;
            }

            boolean success = false;
            int retries = 3;
            while (retries > 0 && !success) {
                try {
                    dbManager.update("BAN_MAY",
                            new String[] { "TrangThai" },
                            new Object[] { "Đang sử dụng" },
                            "MaMay='" + toId + "'");

                    dbManager.update("BAN_MAY",
                            new String[] { "TrangThai" },
                            new Object[] { "Sẵn sàng" },
                            "MaMay='" + fromId + "'");

                    success = true;
                } catch (SQLException ex) {
                    retries--;
                    if (retries == 0) {
                        JOptionPane.showMessageDialog(dialog, "Lỗi chuyển máy: " + ex.getMessage());
                        return;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (selectedComputerId != null && selectedComputerId.equals(fromId)) {
                selectedComputerId = toId;
                updateComputerInfo();
            }

            loadComputers((JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView());
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private void filterComputers(String status) {
        JPanel grid = (JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView();
        grid.removeAll();
        try {
            String whereClause = status.equals("Tất cả") ? "" : "TrangThai='" + status + "'";
            ResultSet rs = dbManager.select("BAN_MAY",
                    new String[] { "MaMay", "SoMay", "TrangThai" },
                    whereClause);
            int cols = 4;
            List<String[]> computers = new ArrayList<>();
            while (rs.next()) {
                computers.add(new String[] {
                        rs.getString("MaMay"),
                        rs.getString("SoMay"),
                        rs.getString("TrangThai")
                });
            }
            rs.close();
            grid.setLayout(new GridLayout((computers.size() + cols - 1) / cols, cols, 20, 20));

            for (String[] computer : computers) {
                JPanel block = createComputerBlock(computer[0], computer[1], computer[2]);
                grid.add(block);
                computerBlocks.put(computer[0], block);
            }
            grid.revalidate();
            grid.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lọc máy: " + e.getMessage());
        }
    }

    private void processPayment() {
        if (selectedComputerId != null) {
            try {
                dbManager.update("BAN_MAY",
                        new String[] { "TrangThai" },
                        new Object[] { "Sẵn sàng" },
                        "MaMay='" + selectedComputerId + "'");
                JOptionPane.showMessageDialog(this, "Đã thanh toán cho " + selectedComputerId);
                loadComputers((JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView());
                selectedComputerId = null;
                isUsing = false;
                selectedComputerLabel.setText("Chưa chọn máy");
                timeLabel.setText("Thời gian: 00:00:00");
                feeLabel.setText("Tiền giờ: 0 VNĐ");
                totalLabel.setText("Tổng tiền: 0 VNĐ");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi thanh toán: " + e.getMessage());
            }
        }
    }

    private void printInvoice() {
        if (selectedComputerId != null) {
            JOptionPane.showMessageDialog(this, "Đã in hóa đơn cho " + selectedComputerId);
        }
    }
}