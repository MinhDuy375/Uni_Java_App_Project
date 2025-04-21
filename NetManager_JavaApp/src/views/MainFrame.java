package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private JPanel menuPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private List<JButton> menuButtons = new ArrayList<>();
    private JButton selectedButton = null;
    private String userRole;

    private JPanel subMenuManageInfo;
    private JPanel subMenuServiceMenu;
    private boolean isSubMenuManageInfoVisible = false;
    private boolean isSubMenuServiceMenuVisible = false;

    private InventoryPanel inventoryPanel;
    private ServiceMenuPanel serviceMenuPanel;
    private ManageComputer manageComputer;
    private PromotionsPanel promotionsPanel;
    private StaffPanel staffPanel;
    private StatisticsPanel statisticsPanel;
    private CustomerPanel customerPanel;
    private OrderPanel orderPanel;
    private ImportInfoPanel importInfoPanel;

    public MainFrame(String userRole) {
        this.userRole = userRole;
        setTitle("Net Management");
        setSize(1266, 698);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Color.GRAY);
        setMinimumSize(new Dimension(600, 120));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.GRAY);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(1127, 80));
        headerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(173, 173, 173, 200)));

        JPanel netCoBlock = new JPanel(new GridBagLayout());
        netCoBlock.setBackground(new Color(97, 187, 252));
        netCoBlock.setPreferredSize(new Dimension(235, 80));
        netCoBlock.setMinimumSize(new Dimension(235, 80));
        netCoBlock.setMaximumSize(new Dimension(235, 80));

        JLabel imageLabel = new JLabel("NétCỏ", SwingConstants.CENTER);
        try {
            File fontFile = new File("NetManager_JavaApp/resources/fonts/Pacifico-Regular.ttf");
            if (!fontFile.exists()) {
                throw new IOException(
                        "File font Pacifico-Regular.ttf không tồn tại tại: " + fontFile.getAbsolutePath());
            }
            Font pacificoFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(40f);
            imageLabel.setFont(pacificoFont);
        } catch (FontFormatException | IOException e) {
            System.out.println("Lỗi khi nhúng font Pacifico-Regular: " + e.getMessage());
            imageLabel.setFont(new Font("Serif", Font.BOLD, 30));
        }
        imageLabel.setForeground(Color.WHITE);
        imageLabel.setPreferredSize(new Dimension(150, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        netCoBlock.add(imageLabel, gbc);

        JLabel account = new JLabel("Tài khoản");
        account.setCursor(new Cursor(Cursor.HAND_CURSOR));
        account.setOpaque(true);
        account.setBackground(Color.WHITE);
        account.setHorizontalAlignment(SwingConstants.CENTER);
        account.setPreferredSize(new Dimension(150, 70));
        account.setFont(new Font("Arial", Font.BOLD, 14));

        JPopupMenu adminMenu = new JPopupMenu();
        adminMenu.setBackground(Color.WHITE);
        adminMenu.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(173, 173, 173, 200)));
        JMenuItem logoutItem = new JMenuItem("Đăng xuất");
        logoutItem.setBackground(Color.white);
        logoutItem.setBorder(BorderFactory.createEmptyBorder(5, 20, 7, 30));
        logoutItem.addActionListener(e -> showLoginPanel());
        adminMenu.add(logoutItem);

        final boolean[] menuVisible = { false };
        account.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!menuVisible[0]) {
                    adminMenu.show(account, 11, 51);
                } else {
                    adminMenu.setVisible(false);
                }
                menuVisible[0] = !menuVisible[0];
            }
        });

        JPanel notification = new JPanel(new BorderLayout());
        notification.setPreferredSize(new Dimension(260, 80));
        notification.setBackground(Color.WHITE);
        notification.add(account, BorderLayout.EAST);
        notification.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        headerPanel.add(netCoBlock, BorderLayout.WEST);
        headerPanel.add(notification, BorderLayout.EAST);

        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setPreferredSize(new Dimension(235, 0));
        menuPanel.setBackground(new Color(0, 54, 92));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));

        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.setBorder(BorderFactory.createMatteBorder(16, 16, 16, 16, new Color(239, 241, 249)));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, Color.WHITE));
        paddingPanel.add(contentPanel, BorderLayout.CENTER);

        manageComputer = new ManageComputer(new DatabaseManager(), userRole);
        inventoryPanel = new InventoryPanel(userRole);
        promotionsPanel = new PromotionsPanel(userRole);
        staffPanel = new StaffPanel(userRole);
        statisticsPanel = new StatisticsPanel();
        customerPanel = new CustomerPanel(userRole);
        orderPanel = new OrderPanel();
        serviceMenuPanel = new ServiceMenuPanel();
        importInfoPanel = new ImportInfoPanel();

        inventoryPanel.addMenuChangeListener(serviceMenuPanel);
        promotionsPanel.addPromotionChangeListener(manageComputer);

        contentPanel.add(manageComputer, "Quản lý bàn máy");
        contentPanel.add(inventoryPanel, "Kho hàng");
        contentPanel.add(promotionsPanel, "Khuyến mãi");
        contentPanel.add(staffPanel, "Quản lý thông tin");
        contentPanel.add(statisticsPanel, "Báo cáo - Thống kê");
        contentPanel.add(customerPanel, "Quản lý khách hàng");
        contentPanel.add(orderPanel, "Đơn đặt món");
        contentPanel.add(serviceMenuPanel, "Thực đơn");
        contentPanel.add(importInfoPanel, "Thông tin nhập hàng");

        String[] menuItems = {
                "Quản lý bàn máy",
                "Menu dịch vụ",
                "Khuyến mãi",
                "Quản lý thông tin",
                "Báo cáo - Thống kê"
        };

        for (String item : menuItems) {
            JButton button = new JButton(item);
            styleMenuButton(button);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.addActionListener(e -> {
                if (item.equals("Quản lý thông tin")) {
                    toggleSubMenuManageInfo();
                } else if (item.equals("Menu dịch vụ")) {
                    toggleSubMenuServiceMenu();
                } else {
                    cardLayout.show(contentPanel, item);
                    updateSelectedButton(button);
                }
            });
            menuButtons.add(button);
        }

        subMenuManageInfo = new JPanel();
        subMenuManageInfo.setLayout(new BoxLayout(subMenuManageInfo, BoxLayout.Y_AXIS));
        subMenuManageInfo.setBackground(new Color(0, 54, 92));
        subMenuManageInfo.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(97, 187, 252)));

        subMenuServiceMenu = new JPanel();
        subMenuServiceMenu.setLayout(new BoxLayout(subMenuServiceMenu, BoxLayout.Y_AXIS));
        subMenuServiceMenu.setBackground(new Color(0, 54, 92));
        subMenuServiceMenu.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(97, 187, 252)));

        rebuildMenu();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.WEST);
        mainPanel.add(paddingPanel, BorderLayout.CENTER);

        add(mainPanel);

        cardLayout.show(contentPanel, "Quản lý bàn máy");
        updateSelectedButton(menuButtons.get(0));
    }

    private void rebuildMenu() {
        menuPanel.removeAll();

        menuPanel.add(menuButtons.get(0));

        menuPanel.add(menuButtons.get(1));
        if (isSubMenuServiceMenuVisible) {
            subMenuServiceMenu.removeAll();
            String[] subMenuItems = { "Thực đơn", "Đơn đặt món", "Kho hàng", "Thông tin nhập hàng" };
            for (String item : subMenuItems) {
                JButton button = new JButton(item);
                styleSubMenuButton(button);
                button.addActionListener(e -> {
                    if (item.equals("Thực đơn")) {
                        cardLayout.show(contentPanel, "Thực đơn");
                    } else if (item.equals("Đơn đặt món")) {
                        cardLayout.show(contentPanel, "Đơn đặt món");
                    } else if (item.equals("Kho hàng")) {
                        cardLayout.show(contentPanel, "Kho hàng");
                    } else if (item.equals("Thông tin nhập hàng")) {
                        cardLayout.show(contentPanel, "Thông tin nhập hàng");
                    }
                    updateSelectedButton(button);
                });
                subMenuServiceMenu.add(button);
            }
            menuPanel.add(subMenuServiceMenu);
        }

        menuPanel.add(menuButtons.get(2));

        menuPanel.add(menuButtons.get(3));
        if (isSubMenuManageInfoVisible) {
            subMenuManageInfo.removeAll();
            String[] subMenuItems = { "Thông tin nhân viên", "Quản lý khách hàng" };
            for (String item : subMenuItems) {
                JButton button = new JButton(item);
                styleSubMenuButton(button);
                button.addActionListener(e -> {
                    if (item.equals("Thông tin nhân viên")) {
                        cardLayout.show(contentPanel, "Quản lý thông tin");
                    } else {
                        cardLayout.show(contentPanel, "Quản lý khách hàng");
                    }
                    updateSelectedButton(button);
                });
                subMenuManageInfo.add(button);
            }
            menuPanel.add(subMenuManageInfo);
        }

        menuPanel.add(menuButtons.get(4));

        revalidate();
        repaint();
    }

    private void toggleSubMenuManageInfo() {
        isSubMenuManageInfoVisible = !isSubMenuManageInfoVisible;
        rebuildMenu();
    }

    private void toggleSubMenuServiceMenu() {
        isSubMenuServiceMenuVisible = !isSubMenuServiceMenuVisible;
        rebuildMenu();
    }

    private void updateSelectedButton(JButton button) {
        if (selectedButton != null && selectedButton != button) {
            selectedButton.setBackground(new Color(0, 54, 92));
        }
        selectedButton = button;
        button.setBackground(new Color(97, 187, 252));
    }

    private void showLoginPanel() {
        JOptionPane.showMessageDialog(this, "Trở về Đăng nhập");
        dispose();
        LoginPanel login = new LoginPanel();
        login.setVisible(true);
    }

    private void styleMenuButton(JButton button) {
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(235, 50));
        button.setMaximumSize(new Dimension(235, 50));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setFont(new Font("IBM Plex Mono", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 30, 15, 20));
        button.setBackground(new Color(0, 54, 92));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(new Color(70, 100, 120));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(new Color(0, 54, 92));
                }
            }
        });
    }

    private void styleSubMenuButton(JButton button) {
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(235, 40));
        button.setMaximumSize(new Dimension(235, 40));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setFont(new Font("IBM Plex Mono", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(5, 50, 5, 20));
        button.setBackground(new Color(0, 54, 92));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(new Color(70, 100, 120));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(new Color(0, 54, 92));
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame("admin");
            frame.setVisible(true);
        });
    }
}