package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceMenuPanel extends JPanel implements ComputerStatusListener {
    private DatabaseManager dbManager;
    private JPanel menuItemsPanel;
    private List<MenuItem> menuItems;
    private List<MenuItem> originalMenuItems;
    private JTextField searchField;
    private JButton searchButton;
    private JComboBox<String> sortComboBox;
    private JButton foodButton;
    private JButton drinkButton;
    private JLabel dateTimeLabel;
    private JComboBox<String> computerComboBox;
    private DefaultListModel<String> orderedItemsModel;
    private JList<String> orderedItemsList;
    private List<OrderedItem> orderedItems;
    private JPanel controlPanel;
    private List<String> computerList;
    private Map<String, String> computerStatusMap;

    public ServiceMenuPanel() {
        dbManager = new DatabaseManager();
        menuItems = new ArrayList<>();
        originalMenuItems = new ArrayList<>();
        orderedItems = new ArrayList<>();
        computerList = new ArrayList<>();
        computerStatusMap = new HashMap<>();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);

        JPanel leftPanel = createLeftPanel();
        splitPane.setLeftComponent(leftPanel);

        JPanel rightPanel = createRightPanel();
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        loadMenuItems();
        updateDateTime();

        ComputerStatusManager.getInstance().addListener(this);

        Timer timer = new Timer(5000, e -> loadComputers());
        timer.start();
    }

    @Override
    public void onComputerStatusChanged(String maMay, String newStatus) {
        computerStatusMap.put(maMay, newStatus);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(0, 100));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topRow.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchField = new JTextField(15);
        searchButton = new JButton("Tìm kiếm");
        styleButton(searchButton);
        searchButton.addActionListener(e -> filterMenuItems());

        sortComboBox = new JComboBox<>(new String[] { "Mặc định", "A-Z", "Z-A" });
        sortComboBox.addActionListener(e -> sortMenuItems());

        topRow.add(searchLabel);
        topRow.add(searchField);
        topRow.add(searchButton);
        topRow.add(new JLabel("Sắp xếp:"));
        topRow.add(sortComboBox);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomRow.setBackground(Color.WHITE);

        foodButton = new JButton("Đồ ăn");
        styleButton(foodButton);
        foodButton.addActionListener(e -> filterByCategory("Đồ ăn"));

        drinkButton = new JButton("Đồ uống");
        styleButton(drinkButton);
        drinkButton.addActionListener(e -> filterByCategory("Đồ uống"));

        JButton allButton = new JButton("Tất cả");
        styleButton(allButton);
        allButton.addActionListener(e -> filterByCategory("Tất cả"));

        bottomRow.add(allButton);
        bottomRow.add(foodButton);
        bottomRow.add(drinkButton);

        controlPanel.add(topRow, BorderLayout.NORTH);
        controlPanel.add(bottomRow, BorderLayout.CENTER);

        panel.add(controlPanel, BorderLayout.NORTH);

        menuItemsPanel = new JPanel();
        menuItemsPanel.setLayout(new GridLayout(0, 3, 10, 10));
        menuItemsPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(menuItemsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Đặt món", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        panel.add(title, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);

        dateTimeLabel = new JLabel();
        infoPanel.add(new JLabel("Ngày giờ:"));
        infoPanel.add(dateTimeLabel);

        infoPanel.add(new JLabel("Máy đặt:"));
        computerComboBox = new JComboBox<>();
        computerComboBox.addItem("");
        loadComputers();
        infoPanel.add(computerComboBox);

        panel.add(infoPanel, BorderLayout.NORTH);

        orderedItemsModel = new DefaultListModel<>();
        orderedItemsList = new JList<>(orderedItemsModel);
        orderedItemsList.setCellRenderer(new OrderedItemRenderer());
        JScrollPane itemsScrollPane = new JScrollPane(orderedItemsList);
        itemsScrollPane.setBorder(BorderFactory.createTitledBorder("Món đã chọn"));
        panel.add(itemsScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton placeOrderButton = new JButton("Đặt món");
        styleButton(placeOrderButton);
        placeOrderButton.addActionListener(e -> placeOrder());

        JButton clearAllButton = new JButton("Xóa tất cả");
        styleButton(clearAllButton);
        clearAllButton.addActionListener(e -> {
            clearAllItems();
            menuItems.clear();
            displayMenuItems();
        });

        buttonPanel.add(placeOrderButton);
        buttonPanel.add(clearAllButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(97, 187, 252));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));

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

    private void loadMenuItems() {
        menuItems.clear();
        originalMenuItems.clear();
        try {
            ResultSet rs = dbManager.select("MON_AN", new String[] { "MaSP", "TenSP", "GiaBan", "DanhMuc", "DangBan" },
                    "DangBan = 'Có'");
            while (rs.next()) {
                MenuItem item = new MenuItem(
                        rs.getString("MaSP"),
                        rs.getString("TenSP"),
                        rs.getDouble("GiaBan"),
                        rs.getString("DanhMuc"));
                menuItems.add(item);
                originalMenuItems.add(item);
            }
            rs.close();
            if (menuItems.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có món ăn nào đang bán!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách món: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
        displayMenuItems();
    }

    private void loadComputers() {
        String selected = (String) computerComboBox.getSelectedItem();
        computerList.clear();
        computerStatusMap.clear();
        computerComboBox.removeAllItems();
        computerComboBox.addItem("");
        try {
            ResultSet rs = dbManager.select("BAN_MAY", new String[] { "MaMay", "SoMay", "TrangThai" }, "");
            while (rs.next()) {
                String maMay = String.valueOf(rs.getInt("MaMay"));
                String soMay = rs.getString("SoMay");
                String trangThai = rs.getString("TrangThai");
                String computerEntry = maMay + " - " + soMay;
                computerList.add(computerEntry);
                computerComboBox.addItem(computerEntry);
                computerStatusMap.put(maMay, trangThai);
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách máy: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
        computerComboBox.setSelectedItem(selected);
    }

    private void displayMenuItems() {
        menuItemsPanel.removeAll();
        if (menuItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có món ăn nào để hiển thị.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("IBM Plex Mono", Font.PLAIN, 16));
            emptyLabel.setForeground(Color.GRAY);
            menuItemsPanel.add(emptyLabel);
        } else {
            for (MenuItem item : menuItems) {
                JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
                itemPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                itemPanel.setBackground(Color.WHITE);
                itemPanel.setPreferredSize(new Dimension(200, 120));
                itemPanel.setMinimumSize(new Dimension(150, 120));
                itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

                JLabel nameLabel = new JLabel(item.name, SwingConstants.CENTER);
                nameLabel.setFont(new Font("IBM Plex Mono", Font.BOLD, 16));
                nameLabel.setForeground(new Color(0, 54, 92));

                JLabel priceLabel = new JLabel(String.format("%.2f", item.price) + " VNĐ", SwingConstants.CENTER);
                priceLabel.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));

                JPanel infoPanel = new JPanel(new GridLayout(2, 1));
                infoPanel.setBackground(Color.WHITE);
                infoPanel.add(nameLabel);
                infoPanel.add(priceLabel);
                itemPanel.add(infoPanel, BorderLayout.CENTER);

                JButton orderButton = new JButton("Đặt");
                styleButton(orderButton);
                orderButton.addActionListener(e -> addToOrder(item));
                itemPanel.add(orderButton, BorderLayout.SOUTH);

                menuItemsPanel.add(itemPanel);
            }
        }
        menuItemsPanel.revalidate();
        menuItemsPanel.repaint();
    }

    private void filterMenuItems() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedCategory = (foodButton.isSelected() ? "Đồ ăn" : drinkButton.isSelected() ? "Đồ uống" : "Tất cả");

        List<MenuItem> filteredItems = new ArrayList<>();
        for (MenuItem item : originalMenuItems) {
            boolean matchesKeyword = item.name.toLowerCase().contains(keyword);
            boolean matchesCategory = selectedCategory.equals("Tất cả") || item.category.equals(selectedCategory);
            if (matchesKeyword && matchesCategory) {
                filteredItems.add(item);
            }
        }

        menuItems.clear();
        menuItems.addAll(filteredItems);
        displayMenuItems();
    }

    private void filterByCategory(String category) {
        foodButton.setSelected(false);
        drinkButton.setSelected(false);

        if (category.equals("Đồ ăn")) {
            foodButton.setSelected(true);
        } else if (category.equals("Đồ uống")) {
            drinkButton.setSelected(true);
        }

        filterMenuItems();
    }

    private void sortMenuItems() {
        String selectedSort = (String) sortComboBox.getSelectedItem();
        if (selectedSort.equals("Mặc định")) {
            menuItems.clear();
            menuItems.addAll(originalMenuItems);
        } else {
            Collections.sort(menuItems, new Comparator<MenuItem>() {
                @Override
                public int compare(MenuItem o1, MenuItem o2) {
                    return selectedSort.equals("A-Z") ? o1.name.compareTo(o2.name) : o2.name.compareTo(o1.name);
                }
            });
        }
        displayMenuItems();
    }

    private void addToOrder(MenuItem item) {
        String computerSelection = (String) computerComboBox.getSelectedItem();
        if (computerSelection == null || computerSelection.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn máy trước khi đặt món!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maMay = computerSelection.split(" - ")[0];
        String trangThai = computerStatusMap.get(maMay);
        if (!"Đang sử dụng".equals(trangThai)) {
            JOptionPane.showMessageDialog(this, "Máy này hiện không được sử dụng!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        OrderedItem existingItem = null;
        for (OrderedItem orderedItem : orderedItems) {
            if (orderedItem.item.id.equals(item.id)) {
                existingItem = orderedItem;
                break;
            }
        }

        if (existingItem != null) {
            existingItem.quantity++;
        } else {
            orderedItems.add(new OrderedItem(item, 1));
        }

        updateOrderedItemsList();
    }

    private void updateOrderedItemsList() {
        orderedItemsModel.clear();
        for (OrderedItem orderedItem : orderedItems) {
            orderedItemsModel.addElement(
                    orderedItem.item.name + " - " + orderedItem.quantity + " x "
                            + String.format("%.2f", orderedItem.item.price) + " VNĐ");
        }
    }

    private void removeItem(int index) {
        if (index >= 0 && index < orderedItems.size()) {
            orderedItems.remove(index);
            updateOrderedItemsList();
        }
    }

    private void clearAllItems() {
        orderedItems.clear();
        updateOrderedItemsList();
    }

    private void placeOrder() {
        String computerSelection = (String) computerComboBox.getSelectedItem();
        if (computerSelection == null || computerSelection.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn máy đặt!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maMay = computerSelection.split(" - ")[0];
        String trangThai = computerStatusMap.get(maMay);
        if (!"Đang sử dụng".equals(trangThai)) {
            JOptionPane.showMessageDialog(this, "Máy này hiện không được sử dụng!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (orderedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một món!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maHD = null;
        try {
            ResultSet rs = dbManager.select("HOA_DON",
                    new String[] { "MaHD" },
                    "MaMay=? AND SoTien=0", new Object[] { Integer.parseInt(maMay) });
            if (rs.next()) {
                maHD = rs.getString("MaHD");
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy mã hóa đơn: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (maHD == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn cho máy này!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double totalServiceFee = 0.0;
            for (OrderedItem orderedItem : orderedItems) {
                String maSP = orderedItem.item.id;
                int quantity = orderedItem.quantity;
                ResultSet rs = dbManager.select("MON_AN", new String[] { "GiaBan" }, "MaSP=?", new Object[] { maSP });
                if (rs.next()) {
                    double gia = rs.getDouble("GiaBan");
                    double thanhTien = gia * quantity;
                    totalServiceFee += thanhTien;

                    dbManager.insert("CHI_TIET_HOA_DON", new Object[] { maHD, maSP, quantity, thanhTien });
                }
                rs.close();
            }

            ResultSet rs = dbManager.select("HOA_DON", new String[] { "SoTien" }, "MaHD=?", new Object[] { maHD });
            if (rs.next()) {
                double currentSoTien = rs.getDouble("SoTien");
                dbManager.update("HOA_DON",
                        new String[] { "SoTien" },
                        new Object[] { currentSoTien + totalServiceFee },
                        "MaHD=?",
                        new Object[] { maHD });
            }
            rs.close();

            JOptionPane.showMessageDialog(this, "Đặt món thành công! Mã hóa đơn: " + maHD, "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            clearAllItems();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi đặt món: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateTimeLabel.setText(sdf.format(new Date()));
        Timer timer = new Timer(1000, e -> dateTimeLabel.setText(sdf.format(new Date())));
        timer.start();
    }

    private static class MenuItem {
        String id;
        String name;
        double price;
        String category;

        MenuItem(String id, String name, double price, String category) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.category = category;
        }
    }

    private static class OrderedItem {
        MenuItem item;
        int quantity;

        OrderedItem(MenuItem item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }
    }

    private class OrderedItemRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel label;
        private JButton removeButton;

        public OrderedItemRenderer() {
            setLayout(new BorderLayout(5, 0));
            label = new JLabel();
            removeButton = new JButton("Xóa");
            removeButton.setBackground(new Color(255, 99, 71));
            removeButton.setForeground(Color.WHITE);
            removeButton.setFont(new Font("IBM Plex Mono", Font.BOLD, 12));
            removeButton.setBorderPainted(false);
            removeButton.setFocusPainted(false);
            removeButton.setPreferredSize(new Dimension(60, 30));
            add(label, BorderLayout.CENTER);
            add(removeButton, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {
            label.setText(value);
            for (ActionListener al : removeButton.getActionListeners()) {
                removeButton.removeActionListener(al);
            }
            removeButton.addActionListener(e -> removeItem(index));
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }
}