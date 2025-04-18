package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import java.io.File;
import java.io.IOException;

public class ManageComputer extends JPanel {
    private DatabaseManager dbManager;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private Map<Integer, JPanel> computerBlocks;
    private JLabel selectedComputerLabel;
    private JLabel timeLabel;
    private JLabel feeLabel;
    private JTextArea servicesArea;
    private JLabel totalLabel;
    private Integer selectedComputerId;
    private Timer timer;
    private Map<Integer, Long> computerStartTimes;
    private double hourlyRate;
    private Map<Integer, Boolean> computerUsageStatus;
    private Map<Integer, String> computerInvoiceMap;
    private Map<Integer, Integer> computerServiceFees;
    private int currentEmployeeId = 1;
    private JComboBox<String> customerComboBox;
    private Map<Integer, String> lastTimeInfo;
    private Map<Integer, String> lastFeeInfo;
    private Map<Integer, String> lastServicesInfo;
    private Map<Integer, String> lastTotalInfo;

    public ManageComputer(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.computerBlocks = new HashMap<>();
        this.computerStartTimes = new HashMap<>();
        this.computerUsageStatus = new HashMap<>();
        this.computerInvoiceMap = new HashMap<>();
        this.computerServiceFees = new HashMap<>();
        this.lastTimeInfo = new HashMap<>();
        this.lastFeeInfo = new HashMap<>();
        this.lastServicesInfo = new HashMap<>();
        this.lastTotalInfo = new HashMap<>();

        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.6);
        splitPane.setEnabled(false);

        leftPanel = createLeftPanel();
        splitPane.setLeftComponent(leftPanel);

        rightPanel = createRightPanel();
        splitPane.setRightComponent(new JScrollPane(rightPanel));

        add(splitPane, BorderLayout.CENTER);

        timer = new Timer(1000, e -> updateRealTimeInfo());
        timer.start();
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(97, 187, 252));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 30));

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

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Thêm máy");
        JButton transferButton = new JButton("Chuyển máy");

        styleButton(addButton);
        styleButton(transferButton);

        addButton.addActionListener(e -> addComputer());
        transferButton.addActionListener(e -> transferComputer());

        controlPanel.add(addButton);
        controlPanel.add(transferButton);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editButton = new JButton("Sửa thông tin máy");
        JComboBox<String> statusFilter = new JComboBox<>(
                new String[] { "Tất cả", "Sẵn sàng", "Đang sử dụng", "Bảo trì" });

        styleButton(editButton);

        editButton.addActionListener(e -> editComputerInfo());
        statusFilter.addActionListener(e -> filterComputers((String) statusFilter.getSelectedItem()));

        filterPanel.add(editButton);
        filterPanel.add(new JLabel("Lọc: "));
        filterPanel.add(statusFilter);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(controlPanel);
        topPanel.add(filterPanel);

        JPanel computersGrid = new JPanel();
        loadComputers(computersGrid);
        JScrollPane scrollPane = new JScrollPane(computersGrid);

        panel.add(topPanel, BorderLayout.NORTH);
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
            List<Object[]> computers = new ArrayList<>();
            while (rs.next()) {
                Integer maMay = rs.getInt("MaMay");
                computers.add(new Object[] {
                        maMay,
                        rs.getString("SoMay"),
                        rs.getString("TrangThai")
                });
                if (!computerUsageStatus.containsKey(maMay)) {
                    computerUsageStatus.put(maMay, rs.getString("TrangThai").equals("Đang sử dụng"));
                }
            }
            rs.close();
            grid.setLayout(new GridLayout((computers.size() + cols - 1) / cols, cols, 20, 20));

            for (Object[] computer : computers) {
                Integer maMay = (Integer) computer[0];
                String soMay = (String) computer[1];
                String trangThai = (String) computer[2];
                JPanel block = createComputerBlock(maMay, soMay, trangThai);
                grid.add(block);
                computerBlocks.put(maMay, block);
            }
            grid.revalidate();
            grid.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách máy: " + e.getMessage());
        }
    }

    private JPanel createComputerBlock(Integer id, String soMay, String status) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(100, 100));
        panel.setMaximumSize(new Dimension(100, 100));
        panel.setMinimumSize(new Dimension(100, 100));
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
        JPanel panel = new JPanel(new GridLayout(10, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        selectedComputerLabel = new JLabel("Chưa chọn máy");
        timeLabel = new JLabel("Thời gian: 00:00:00");
        feeLabel = new JLabel("Tiền giờ: 0 VNĐ");
        servicesArea = new JTextArea("Dịch vụ:\n");
        servicesArea.setEditable(false);
        totalLabel = new JLabel("Tổng tiền: 0 VNĐ");

        JLabel customerLabel = new JLabel("Khách hàng:");
        customerComboBox = new JComboBox<>();
        customerComboBox.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        customerComboBox.addItem("");
        loadCustomers();

        JLabel promotionLabel = new JLabel("Mã giảm giá:");
        JComboBox<String> promotionComboBox = new JComboBox<>();
        promotionComboBox.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        promotionComboBox.addItem("Không áp dụng");
        loadPromotions(promotionComboBox);

        JButton startButton = new JButton("Bắt đầu sử dụng");
        JButton payButton = new JButton("Thanh toán");
        JButton printButton = new JButton("In hóa đơn");
        JButton resetButton = new JButton("Làm mới");

        styleButton(startButton);
        styleButton(payButton);
        styleButton(printButton);
        styleButton(resetButton);

        startButton.addActionListener(e -> startUsing());
        payButton.addActionListener(e -> processPayment(promotionComboBox));
        printButton.addActionListener(e -> printInvoice());
        resetButton.addActionListener(e -> {
            resetComputerInfo();
            promotionComboBox.setSelectedIndex(0);
        });

        promotionComboBox.addActionListener(e -> updateTotalWithPromotion(promotionComboBox));

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
        panel.add(customerLabel);
        panel.add(customerComboBox);
        panel.add(promotionLabel);
        panel.add(promotionComboBox);
        panel.add(startButton);
        panel.add(payButton);
        panel.add(printButton);
        panel.add(resetButton);

        return panel;
    }

    private void loadCustomers() {
        try {
            ResultSet customerRs = dbManager.select("KHACH_HANG", new String[] { "MaKH", "TenKH" }, "");
            customerComboBox.removeAllItems();
            customerComboBox.addItem("");
            while (customerRs.next()) {
                String maKH = customerRs.getString("MaKH");
                String tenKH = customerRs.getString("TenKH");
                if (maKH != null && tenKH != null) {
                    customerComboBox.addItem(maKH + " - " + tenKH);
                }
            }
            customerRs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách khách hàng: " + e.getMessage());
        }
    }

    private void loadPromotions(JComboBox<String> promotionComboBox) {
        try {
            ResultSet rs = dbManager.select("KHUYEN_MAI",
                    new String[] { "MaKM", "TenChuongTrinh", "MucKM" },
                    "TrangThai = ?", new Object[] { "Hoạt động" });
            while (rs.next()) {
                String maKM = rs.getString("MaKM");
                String tenChuongTrinh = rs.getString("TenChuongTrinh");
                double mucKM = rs.getDouble("MucKM");
                int discountPercentage = (int) ((1 - mucKM) * 100);
                promotionComboBox.addItem(maKM + " - " + tenChuongTrinh + " (" + discountPercentage + "%)");
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách mã giảm giá: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectComputer(Integer computerId) {
        this.selectedComputerId = computerId;
        updateComputerInfo();
    }

    private void startUsing() {
        if (selectedComputerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một máy trước!");
            return;
        }

        String selectedCustomer = (String) customerComboBox.getSelectedItem();
        if (selectedCustomer == null || selectedCustomer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng!");
            return;
        }
        if (!selectedCustomer.contains(" - ")) {
            JOptionPane.showMessageDialog(this, "Dữ liệu khách hàng không hợp lệ!");
            return;
        }
        String[] customerParts = selectedCustomer.split(" - ");
        if (customerParts.length < 2) {
            JOptionPane.showMessageDialog(this, "Dữ liệu khách hàng không đúng định dạng!");
            return;
        }
        String maKH = customerParts[0];

        String maHD = "HD" + System.currentTimeMillis();
        String ngay = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String hinhThucThanhToan = "Tiền mặt";
        double khuyenMai = 0.0;
        double soTien = 0.0;

        try {
            ResultSet rs = dbManager.select("BAN_MAY",
                    new String[] { "TrangThai" },
                    "MaMay=?", new Object[] { selectedComputerId });
            if (rs.next()) {
                String status = rs.getString("TrangThai");
                if (!status.equals("Sẵn sàng")) {
                    JOptionPane.showMessageDialog(this, "Máy phải ở trạng thái Sẵn sàng để bắt đầu sử dụng!");
                    rs.close();
                    return;
                }
            }
            rs.close();

            boolean maKHExists = false, maNVExists = false, maMayExists = false;
            rs = dbManager.select("KHACH_HANG", new String[] { "MaKH" }, "MaKH=?", new Object[] { maKH });
            if (rs.next()) {
                maKHExists = true;
            }
            rs.close();

            rs = dbManager.select("NHAN_VIEN", new String[] { "MaNV" }, "MaNV=?", new Object[] { currentEmployeeId });
            if (rs.next()) {
                maNVExists = true;
            }
            rs.close();

            rs = dbManager.select("BAN_MAY", new String[] { "MaMay" }, "MaMay=?", new Object[] { selectedComputerId });
            if (rs.next()) {
                maMayExists = true;
            }
            rs.close();

            if (!maKHExists) {
                JOptionPane.showMessageDialog(this, "Mã khách hàng không tồn tại!");
                return;
            }
            if (!maNVExists) {
                JOptionPane.showMessageDialog(this, "Mã nhân viên không tồn tại!");
                return;
            }
            if (!maMayExists) {
                JOptionPane.showMessageDialog(this, "Mã máy không tồn tại!");
                return;
            }

            dbManager.insert("HOA_DON", new Object[] {
                    maHD,
                    maKH,
                    currentEmployeeId,
                    selectedComputerId,
                    ngay,
                    khuyenMai,
                    soTien,
                    hinhThucThanhToan
            });

            dbManager.update("BAN_MAY",
                    new String[] { "TrangThai" },
                    new Object[] { "Đang sử dụng" },
                    "MaMay=?",
                    new Object[] { selectedComputerId });

            computerStartTimes.put(selectedComputerId, System.currentTimeMillis());
            computerUsageStatus.put(selectedComputerId, true);
            computerInvoiceMap.put(selectedComputerId, maHD);
            computerServiceFees.put(selectedComputerId, 0);

            loadComputers((JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView());
            updateComputerInfo();

            ComputerStatusManager.getInstance().notifyComputerStatusChanged(String.valueOf(selectedComputerId),
                    "Đang sử dụng");

            customerComboBox.setSelectedIndex(0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi bắt đầu sử dụng: " + ex.getMessage());
        }
    }

    private void updateComputerInfo() {
        if (selectedComputerId == null) {
            selectedComputerLabel.setText("Chưa chọn máy");
            timeLabel.setText("Thời gian: 00:00:00");
            feeLabel.setText("Tiền giờ: 0 VNĐ");
            servicesArea.setText("Dịch vụ:\n");
            totalLabel.setText("Tổng tiền: 0 VNĐ");
            return;
        }

        try {
            ResultSet rs = dbManager.select("BAN_MAY",
                    new String[] { "SoMay", "TrangThai", "GiaThueBan" },
                    "MaMay=?", new Object[] { selectedComputerId });
            if (rs.next()) {
                selectedComputerLabel.setText(rs.getString("SoMay") + " (" + rs.getString("TrangThai") + ")");
                hourlyRate = rs.getDouble("GiaThueBan");

                if (rs.getString("TrangThai").equals("Sẵn sàng")
                        || !computerUsageStatus.getOrDefault(selectedComputerId, false)) {
                    if (lastTimeInfo.containsKey(selectedComputerId)) {
                        timeLabel.setText(lastTimeInfo.get(selectedComputerId));
                        feeLabel.setText(lastFeeInfo.get(selectedComputerId));
                        servicesArea.setText(lastServicesInfo.get(selectedComputerId));
                        totalLabel.setText(lastTotalInfo.get(selectedComputerId));
                    } else {
                        timeLabel.setText("Thời gian: 00:00:00");
                        feeLabel.setText("Tiền giờ: 0 VNĐ");
                        servicesArea.setText("Dịch vụ:\n");
                        totalLabel.setText("Tổng tiền: 0 VNĐ");
                    }
                } else {
                    updateRealTimeInfo();
                    updateServices();
                }
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật thông tin: " + e.getMessage());
        }
    }

    private void updateRealTimeInfo() {
        if (selectedComputerId != null && computerUsageStatus.getOrDefault(selectedComputerId, false)) {
            Long startTime = computerStartTimes.get(selectedComputerId);
            if (startTime != null) {
                long currentTime = System.currentTimeMillis();
                long secondsUsed = (currentTime - startTime) / 1000;
                long hours = secondsUsed / 3600;
                long minutes = (secondsUsed % 3600) / 60;
                long seconds = secondsUsed % 60;

                double hoursUsed = secondsUsed / 3600.0;
                double fee = hoursUsed * hourlyRate;
                int serviceFee = computerServiceFees.getOrDefault(selectedComputerId, 0);

                double khuyenMai = 0.0;
                String maHD = computerInvoiceMap.get(selectedComputerId);
                if (maHD != null) {
                    try {
                        ResultSet rs = dbManager.select("HOA_DON",
                                new String[] { "KhuyenMai" },
                                "MaHD=?", new Object[] { maHD });
                        if (rs.next()) {
                            khuyenMai = rs.getDouble("KhuyenMai");
                        }
                        rs.close();
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(this, "Lỗi lấy thông tin khuyến mãi: " + e.getMessage());
                    }
                }

                double totalBeforeDiscount = fee + serviceFee;
                double totalAfterDiscount = totalBeforeDiscount * khuyenMai;

                timeLabel.setText(String.format("Thời gian: %02d:%02d:%02d", hours, minutes, seconds));
                feeLabel.setText(String.format("Tiền giờ: %.2f VNĐ", fee));
                totalLabel.setText(String.format("Tổng tiền: %.2f VNĐ", totalAfterDiscount));
            }
        }
    }

    private void updateTotalWithPromotion(JComboBox<String> promotionComboBox) {
        if (selectedComputerId == null || !computerUsageStatus.getOrDefault(selectedComputerId, false)) {
            totalLabel.setText("Tổng tiền: 0 VNĐ");
            return;
        }

        Long startTime = computerStartTimes.get(selectedComputerId);
        if (startTime == null) {
            totalLabel.setText("Tổng tiền: 0 VNĐ");
            return;
        }

        long secondsUsed = (System.currentTimeMillis() - startTime) / 1000;
        double hoursUsed = secondsUsed / 3600.0;
        double fee = hoursUsed * hourlyRate;
        int serviceFee = computerServiceFees.getOrDefault(selectedComputerId, 0);
        double totalBeforeDiscount = fee + serviceFee;

        double khuyenMai = 1.0; // Mặc định không giảm giá
        String selectedPromotion = (String) promotionComboBox.getSelectedItem();
        if (selectedPromotion != null && !selectedPromotion.equals("Không áp dụng")) {
            String maKM = selectedPromotion.split(" - ")[0];
            try {
                ResultSet rs = dbManager.select("KHUYEN_MAI", new String[] { "MucKM" }, "MaKM = ?",
                        new Object[] { maKM });
                if (rs.next()) {
                    khuyenMai = rs.getDouble("MucKM"); // Lấy hệ số khuyến mãi (ví dụ: 0.7)
                }
                rs.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi lấy thông tin mã giảm giá: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        double totalAfterDiscount = totalBeforeDiscount * khuyenMai; // Sửa ở đây
        totalLabel.setText(String.format("Tổng tiền: %.2f VNĐ", totalAfterDiscount));
    }

    private void updateServices() {
        if (selectedComputerId == null || !computerInvoiceMap.containsKey(selectedComputerId)) {
            servicesArea.setText("Dịch vụ:\n");
            return;
        }

        String maHD = computerInvoiceMap.get(selectedComputerId);
        StringBuilder servicesText = new StringBuilder("Dịch vụ:\n");
        int totalServiceFee = 0;

        try {
            ResultSet rs = dbManager.select("CHI_TIET_HOA_DON cthd JOIN MON_AN ma ON cthd.MaSP = ma.MaSP",
                    new String[] { "ma.TenSP", "cthd.SoLuong", "cthd.ThanhTien" },
                    "cthd.MaHD=?", new Object[] { maHD });
            while (rs.next()) {
                String tenSP = rs.getString("TenSP");
                int soLuong = rs.getInt("SoLuong");
                int thanhTien = rs.getInt("ThanhTien");
                servicesText.append(String.format("- %s: %d x %d VNĐ\n", tenSP, soLuong, thanhTien / soLuong));
                totalServiceFee += thanhTien;
            }
            rs.close();

            computerServiceFees.put(selectedComputerId, totalServiceFee);
            servicesArea.setText(servicesText.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách dịch vụ: " + e.getMessage());
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

        styleButton(saveButton);

        dialog.add(new JLabel("Máy số:"));
        dialog.add(maMayField);
        dialog.add(new JLabel("Tên máy(Tên hiển thị):"));
        dialog.add(soMayField);
        dialog.add(new JLabel("Giá thuê (VNĐ/giờ):"));
        dialog.add(giaThueField);
        dialog.add(new JLabel("Trạng thái:"));
        dialog.add(trangThaiCombo);
        dialog.add(new JLabel());
        dialog.add(saveButton);

        saveButton.addActionListener(e -> {
            String maMayStr = maMayField.getText();
            String soMay = soMayField.getText();
            String giaThue = giaThueField.getText();
            String trangThai = (String) trangThaiCombo.getSelectedItem();

            if (maMayStr.isEmpty() || soMay.isEmpty() || giaThue.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!");
                return;
            }

            try {
                Integer maMay = Integer.parseInt(maMayStr);
                double giaThueValue = Double.parseDouble(giaThue);
                if (giaThueValue < 0) {
                    JOptionPane.showMessageDialog(dialog, "Giá thuê phải lớn hơn hoặc bằng 0!");
                    return;
                }

                ResultSet rs = dbManager.select("BAN_MAY", new String[] { "MaMay" }, "MaMay=?", new Object[] { maMay });
                if (rs.next()) {
                    JOptionPane.showMessageDialog(dialog, "Đã tồn tại máy với mã này!");
                    rs.close();
                    return;
                }
                rs.close();

                dbManager.insert("BAN_MAY", new Object[] { maMay, soMay, giaThueValue, trangThai });
                if (trangThai.equals("Đang sử dụng")) {
                    computerStartTimes.put(maMay, System.currentTimeMillis());
                    computerUsageStatus.put(maMay, true);
                } else {
                    computerUsageStatus.put(maMay, false);
                }
                loadComputers((JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView());
                dialog.dispose();

                ComputerStatusManager.getInstance().notifyComputerStatusChanged(String.valueOf(maMay), trangThai);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi thêm máy: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Mã máy và giá thuê phải là số!");
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

        JTextField maMayField = new JTextField(String.valueOf(selectedComputerId));
        maMayField.setEditable(false);
        JTextField soMayField = new JTextField();
        JTextField giaThueField = new JTextField();
        JComboBox<String> trangThaiCombo = new JComboBox<>(new String[] { "Sẵn sàng", "Đang sử dụng", "Bảo trì" });
        JButton saveButton = new JButton("Lưu");

        styleButton(saveButton);

        try {
            ResultSet rs = dbManager.select("BAN_MAY",
                    new String[] { "SoMay", "GiaThueBan", "TrangThai" },
                    "MaMay=?", new Object[] { selectedComputerId });
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
                double giaThueValue = Double.parseDouble(giaThue);
                if (giaThueValue < 0) {
                    JOptionPane.showMessageDialog(dialog, "Giá thuê phải lớn hơn hoặc bằng 0!");
                    return;
                }

                dbManager.update("BAN_MAY",
                        new String[] { "SoMay", "GiaThueBan", "TrangThai" },
                        new Object[] { soMay, giaThueValue, trangThai },
                        "MaMay=?",
                        new Object[] { selectedComputerId });
                if (trangThai.equals("Đang sử dụng") && !computerUsageStatus.getOrDefault(selectedComputerId, false)) {
                    computerStartTimes.put(selectedComputerId, System.currentTimeMillis());
                    computerUsageStatus.put(selectedComputerId, true);
                } else if (!trangThai.equals("Đang sử dụng")) {
                    computerStartTimes.remove(selectedComputerId);
                    computerUsageStatus.put(selectedComputerId, false);
                    computerInvoiceMap.remove(selectedComputerId);
                    computerServiceFees.remove(selectedComputerId);
                }
                loadComputers((JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView());
                updateComputerInfo();
                dialog.dispose();

                ComputerStatusManager.getInstance().notifyComputerStatusChanged(String.valueOf(selectedComputerId),
                        trangThai);
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

        JDialog dialog = new JDialog(owner, "Chuyển dữ liệu máy", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        JComboBox<String> fromCombo = new JComboBox<>();
        JComboBox<String> toCombo = new JComboBox<>();
        JButton transferButton = new JButton("Chuyển");

        styleButton(transferButton);

        try {
            ResultSet rs = dbManager.select("BAN_MAY", new String[] { "MaMay", "SoMay" }, "");
            while (rs.next()) {
                String maMay = String.valueOf(rs.getInt("MaMay"));
                String soMay = rs.getString("SoMay");
                fromCombo.addItem(maMay + " - " + soMay);
                toCombo.addItem(maMay + " - " + soMay);
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

            Integer fromId = Integer.parseInt(fromSelection.split(" - ")[0]);
            Integer toId = Integer.parseInt(toSelection.split(" - ")[0]);

            if (fromId.equals(toId)) {
                JOptionPane.showMessageDialog(dialog, "Máy nguồn và máy đích không được trùng!");
                return;
            }

            String toStatus = null;
            try {
                ResultSet rs = dbManager.select("BAN_MAY", new String[] { "TrangThai" }, "MaMay=?",
                        new Object[] { toId });
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
                            "MaMay=?",
                            new Object[] { toId });

                    dbManager.update("BAN_MAY",
                            new String[] { "TrangThai" },
                            new Object[] { "Sẵn sàng" },
                            "MaMay=?",
                            new Object[] { fromId });

                    String maHD = computerInvoiceMap.remove(fromId);
                    if (maHD != null) {
                        computerInvoiceMap.put(toId, maHD);
                        dbManager.update("HOA_DON",
                                new String[] { "MaMay" },
                                new Object[] { toId },
                                "MaHD=?",
                                new Object[] { maHD });
                    }

                    Long startTime = computerStartTimes.remove(fromId);
                    if (startTime != null) {
                        computerStartTimes.put(toId, startTime);
                        computerUsageStatus.put(toId, true);
                    }
                    computerUsageStatus.put(fromId, false);

                    Integer serviceFee = computerServiceFees.remove(fromId);
                    if (serviceFee != null) {
                        computerServiceFees.put(toId, serviceFee);
                    }

                    success = true;

                    ComputerStatusManager.getInstance().notifyComputerStatusChanged(String.valueOf(toId),
                            "Đang sử dụng");
                    ComputerStatusManager.getInstance().notifyComputerStatusChanged(String.valueOf(fromId), "Sẵn sàng");
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
            }
            loadComputers((JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView());
            updateComputerInfo();
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private void filterComputers(String status) {
        JPanel grid = (JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView();
        grid.removeAll();
        try {
            String whereClause = status.equals("Tất cả") ? "" : "TrangThai=?";
            ResultSet rs;
            if (whereClause.isEmpty()) {
                rs = dbManager.select("BAN_MAY",
                        new String[] { "MaMay", "SoMay", "TrangThai" },
                        "");
            } else {
                rs = dbManager.select("BAN_MAY",
                        new String[] { "MaMay", "SoMay", "TrangThai" },
                        whereClause, new Object[] { status });
            }
            int cols = 4;
            List<Object[]> computers = new ArrayList<>();
            while (rs.next()) {
                computers.add(new Object[] {
                        rs.getInt("MaMay"),
                        rs.getString("SoMay"),
                        rs.getString("TrangThai")
                });
            }
            rs.close();
            grid.setLayout(new GridLayout((computers.size() + cols - 1) / cols, cols, 20, 20));

            for (Object[] computer : computers) {
                Integer maMay = (Integer) computer[0];
                String soMay = (String) computer[1];
                String trangThai = (String) computer[2];
                JPanel block = createComputerBlock(maMay, soMay, trangThai);
                grid.add(block);
                computerBlocks.put(maMay, block);
            }
            grid.revalidate();
            grid.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lọc máy: " + e.getMessage());
        }
    }

    private void processPayment(JComboBox<String> promotionComboBox) {
        if (selectedComputerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một máy để thanh toán!");
            return;
        }

        try {
            Long startTime = computerStartTimes.get(selectedComputerId);
            double fee = 0;
            if (startTime != null) {
                long secondsUsed = (System.currentTimeMillis() - startTime) / 1000;
                double hoursUsed = secondsUsed / 3600.0;
                fee = hoursUsed * hourlyRate;
            }

            int serviceFee = computerServiceFees.getOrDefault(selectedComputerId, 0);
            double totalBeforeDiscount = fee + serviceFee;

            double khuyenMai = 1.0; // Mặc định không giảm giá
            String selectedPromotion = (String) promotionComboBox.getSelectedItem();
            if (selectedPromotion != null && !selectedPromotion.equals("Không áp dụng")) {
                String maKM = selectedPromotion.split(" - ")[0];
                ResultSet rs = dbManager.select("KHUYEN_MAI", new String[] { "MucKM" }, "MaKM = ?",
                        new Object[] { maKM });
                if (rs.next()) {
                    khuyenMai = rs.getDouble("MucKM"); // Hệ số khuyến mãi (ví dụ: 0.7)
                }
                rs.close();
            }

            double totalAmount = totalBeforeDiscount * khuyenMai; // Sửa ở đây

            String maHD = computerInvoiceMap.get(selectedComputerId);
            if (maHD != null) {
                dbManager.update("HOA_DON",
                        new String[] { "KhuyenMai", "SoTien" },
                        new Object[] { khuyenMai, totalAmount },
                        "MaHD=?", new Object[] { maHD });
            }

            dbManager.update("BAN_MAY",
                    new String[] { "TrangThai" },
                    new Object[] { "Sẵn sàng" },
                    "MaMay=?", new Object[] { selectedComputerId });

            lastTimeInfo.put(selectedComputerId, timeLabel.getText());
            lastFeeInfo.put(selectedComputerId, feeLabel.getText());
            lastServicesInfo.put(selectedComputerId, servicesArea.getText());
            lastTotalInfo.put(selectedComputerId, totalLabel.getText());

            JOptionPane.showMessageDialog(this,
                    "Đã thanh toán cho " + selectedComputerId + "\nTổng tiền: " + String.format("%.2f", totalAmount)
                            + " VNĐ");

            computerStartTimes.remove(selectedComputerId);
            computerUsageStatus.put(selectedComputerId, false);

            loadComputers((JPanel) ((JScrollPane) leftPanel.getComponent(1)).getViewport().getView());
            updateComputerInfo();
            promotionComboBox.setSelectedIndex(0);

            ComputerStatusManager.getInstance().notifyComputerStatusChanged(String.valueOf(selectedComputerId),
                    "Sẵn sàng");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi thanh toán: " + e.getMessage());
        }
    }

    private void resetComputerInfo() {
        if (selectedComputerId == null) {
            return;
        }

        lastTimeInfo.remove(selectedComputerId);
        lastFeeInfo.remove(selectedComputerId);
        lastServicesInfo.remove(selectedComputerId);
        lastTotalInfo.remove(selectedComputerId);
        computerInvoiceMap.remove(selectedComputerId);
        computerServiceFees.remove(selectedComputerId);

        timeLabel.setText("Thời gian: 00:00:00");
        feeLabel.setText("Tiền giờ: 0 VNĐ");
        servicesArea.setText("Dịch vụ:\n");
        totalLabel.setText("Tổng tiền: 0 VNĐ");
        selectedComputerId = null;
        selectedComputerLabel.setText("Chưa chọn máy");
    }

    private void printInvoice() {
        if (selectedComputerId == null || !computerInvoiceMap.containsKey(selectedComputerId)) {
            JOptionPane.showMessageDialog(this, "Không có hóa đơn để in!");
            return;
        }

        String maHD = computerInvoiceMap.get(selectedComputerId);
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            File fontFile = new File("NetManager_JavaApp/resources/fonts/Roboto-Medium.ttf");
            if (!fontFile.exists()) {
                throw new IOException("File font Roboto-Medium.ttf không tồn tại tại: " + fontFile.getAbsolutePath());
            }
            PDType0Font font = PDType0Font.load(document, fontFile);

            contentStream.beginText();
            contentStream.setFont(font, 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("HÓA ĐƠN THANH TOÁN");
            contentStream.endText();

            float y = 720;
            double khuyenMai = 1.0; // Mặc định không giảm giá
            ResultSet rs = dbManager.select(
                    "HOA_DON hd JOIN KHACH_HANG kh ON hd.MaKH = kh.MaKH JOIN BAN_MAY bm ON hd.MaMay = bm.MaMay",
                    new String[] { "hd.MaHD", "kh.TenKH", "hd.Ngay", "bm.SoMay", "hd.HinhThucThanhToan",
                            "hd.SoTien", "hd.KhuyenMai" },
                    "hd.MaHD=?", new Object[] { maHD });
            if (rs.next()) {
                khuyenMai = rs.getDouble("KhuyenMai");
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Mã hóa đơn: " + rs.getString("MaHD"));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Khách hàng: " + rs.getString("TenKH"));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Ngày: " + rs.getString("Ngay"));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Máy: " + rs.getString("SoMay"));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Hình thức thanh toán: " + rs.getString("HinhThucThanhToan"));
                contentStream.endText();
                y -= 100;
            }
            rs.close();

            Long startTime = computerStartTimes.get(selectedComputerId);
            double fee = 0;
            if (startTime != null) {
                long secondsUsed = (System.currentTimeMillis() - startTime) / 1000;
                double hoursUsed = secondsUsed / 3600.0;
                fee = hoursUsed * hourlyRate;
            }

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("Tiền giờ: " + String.format("%.2f", fee) + " VNĐ");
            contentStream.endText();
            y -= 20;

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("Danh sách dịch vụ:");
            contentStream.endText();
            y -= 20;

            int totalServiceFee = 0;
            ResultSet serviceRs = dbManager.select("CHI_TIET_HOA_DON cthd JOIN MON_AN ma ON cthd.MaSP = ma.MaSP",
                    new String[] { "ma.TenSP", "cthd.SoLuong", "cthd.ThanhTien" },
                    "cthd.MaHD=?", new Object[] { maHD });
            while (serviceRs.next()) {
                String tenSP = serviceRs.getString("TenSP");
                int soLuong = serviceRs.getInt("SoLuong");
                int thanhTien = serviceRs.getInt("ThanhTien");
                totalServiceFee += thanhTien;

                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText(
                        String.format("- %s: %d x %d VNĐ = %d VNĐ", tenSP, soLuong, thanhTien / soLuong, thanhTien));
                contentStream.endText();
                y -= 20;
            }
            serviceRs.close();

            double totalBeforeDiscount = fee + totalServiceFee;
            int discountPercentage = (int) ((1 - khuyenMai) * 100); // Hiển thị % giảm giá
            double totalAmount = totalBeforeDiscount * khuyenMai; // Sửa ở đây

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("Tổng trước giảm giá: " + String.format("%.2f", totalBeforeDiscount) + " VNĐ");
            contentStream.endText();
            y -= 20;

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("Giảm giá: " + discountPercentage + "%");
            contentStream.endText();
            y -= 20;

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("Tổng tiền: " + String.format("%.2f", totalAmount) + " VNĐ");
            contentStream.endText();

            contentStream.close();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("HoaDon_" + maHD + ".pdf"));
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File outputFile = fileChooser.getSelectedFile();
                document.save(outputFile);
                JOptionPane.showMessageDialog(this, "Đã in hóa đơn thành công!");
            }
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi in hóa đơn: " + e.getMessage());
        }
    }
}