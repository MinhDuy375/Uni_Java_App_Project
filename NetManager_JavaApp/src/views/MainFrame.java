package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private JPanel menuPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private List<JButton> menuButtons = new ArrayList<>();
    private JButton selectedButton = null;

    // Panels for submenus
    private JPanel subMenuManageInfo;
    private JPanel subMenuServiceMenu;
    private boolean isSubMenuManageInfoVisible = false;
    private boolean isSubMenuServiceMenuVisible = false;

    public MainFrame() {
        setTitle("Net Management");
        setSize(1066, 668);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Color.GRAY);
        setMinimumSize(new Dimension(600, 120));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.GRAY);

        // Header Panel (unchanged)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(1127, 80));
        headerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(173, 173, 173, 200)));

        JPanel netCoBlock = new JPanel(new GridBagLayout());
        netCoBlock.setBackground(new Color(97, 187, 252));
        netCoBlock.setPreferredSize(new Dimension(235, 80));
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/views/NétCỏ.png"));
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setPreferredSize(new Dimension(150, 40));
        netCoBlock.add(imageLabel);

        JButton bellButton = new JButton("\uD83D\uDD14");
        bellButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bellButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        bellButton.setBackground(Color.WHITE);
        bellButton.setForeground(Color.DARK_GRAY);
        bellButton.setBorderPainted(false);
        bellButton.setFocusPainted(false);

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
        notification.add(bellButton, BorderLayout.WEST);
        notification.add(account, BorderLayout.EAST);
        notification.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        headerPanel.add(netCoBlock, BorderLayout.WEST);
        headerPanel.add(notification, BorderLayout.EAST);

        // Sidebar with BoxLayout
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setPreferredSize(new Dimension(235, 0));
        menuPanel.setBackground(new Color(0, 54, 92));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));

        // Content Panel
        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.setBorder(BorderFactory.createMatteBorder(16, 16, 16, 16, new Color(239, 241, 249)));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, Color.WHITE));
        paddingPanel.add(contentPanel, BorderLayout.CENTER);

        // Add content panels
        contentPanel.add(new ManageComputer(), "Quản lý bàn máy");
        contentPanel.add(createContentPanel("Menu dịch vụ"), "Menu dịch vụ");
        contentPanel.add(new InventoryPanel(), "Kho hàng");
        contentPanel.add(new PromotionsPanel(), "Khuyến mãi");
        contentPanel.add(new StaffPanel(), "Quản lý thông tin");
        contentPanel.add(new StatisticsPanel(), "Báo cáo - Thống kê");

        // Main menu items
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

        // Submenu panels with adjusted green left border
        subMenuManageInfo = new JPanel();
        subMenuManageInfo.setLayout(new BoxLayout(subMenuManageInfo, BoxLayout.Y_AXIS));
        subMenuManageInfo.setBackground(new Color(0, 54, 92));
        subMenuManageInfo.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(97, 187, 252))); // Light blue
        // border

        subMenuServiceMenu = new JPanel();
        subMenuServiceMenu.setLayout(new BoxLayout(subMenuServiceMenu, BoxLayout.Y_AXIS));
        subMenuServiceMenu.setBackground(new Color(0, 54, 92));
        subMenuServiceMenu.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(97, 187, 252))); // Light
        // blue
        // border

        // Initially build the menu
        rebuildMenu();

        // Add main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.WEST);
        mainPanel.add(paddingPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void rebuildMenu() {
        menuPanel.removeAll();

        // Add "Quản lý bàn máy"
        menuPanel.add(menuButtons.get(0));

// Add "Menu dịch vụ" and its submenu if visible
        menuPanel.add(menuButtons.get(1));
        if (isSubMenuServiceMenuVisible) {
            subMenuServiceMenu.removeAll();
            String[] subMenuItems = { "ServiceMenu", "Order", "Storage" };
            for (String item : subMenuItems) {
                JButton button = new JButton(item);
                styleSubMenuButton(button);
                button.addActionListener(e -> {
                    if (item.equals("Menu")) {
                        if (contentPanel.getComponentCount() == 0 || contentPanel.getComponent(0).getName() == null ||
                                contentPanel.getComponent(0).getName().equals("ServiceMenuPanel")) {
                            ServiceMenuPanel serviceMenuPanel = new ServiceMenuPanel();
                            serviceMenuPanel.setName("ServiceMenuPanel");
                            contentPanel.add(serviceMenuPanel, "ServiceMenuPanel");
                        }
                        cardLayout.show(contentPanel, "ServiceMenuPanel");
                    } else if (item.equals("Order")) {
                        if (contentPanel.getComponentCount() == 0 || contentPanel.getComponent(0).getName() == null ||
                                contentPanel.getComponent(0).getName().equals("OrderPanel")) {
                            OrderPanel orderPanel = new OrderPanel();
                            orderPanel.setName("OrderPanel");
                            contentPanel.add(orderPanel, "OrderPanel");
                        }
                        cardLayout.show(contentPanel, "OrderPanel");
                    } else if (item.equals("Kho hàng")) {
                        if (contentPanel.getComponentCount() == 0 || contentPanel.getComponent(0).getName() == null ||
                                contentPanel.getComponent(0).getName().equals("InventoryPanel")) {
                            InventoryPanel inventoryPanel = new InventoryPanel();
                            inventoryPanel.setName("InventoryPanel");
                            contentPanel.add(inventoryPanel, "InventoryPanel");
                        }
                        cardLayout.show(contentPanel, "InventoryPanel");
                    }
                    updateSelectedButton(button);
                });
                subMenuServiceMenu.add(button);
            }
            menuPanel.add(subMenuServiceMenu);
        }

        // Add "Khuyến mãi"
        menuPanel.add(menuButtons.get(2));

        // Add "Quản lý thông tin" and its submenu if visible
        menuPanel.add(menuButtons.get(3));
        if (isSubMenuManageInfoVisible) {
            subMenuManageInfo.removeAll();
            String[] subMenuItems = {  "Thông tin nhân viên", "Thông tin khách hàng"};
            for (String item : subMenuItems) {
                JButton button = new JButton(item);
                styleSubMenuButton(button);
                button.addActionListener(e -> {
                    if (item.equals("Thông tin nhân viên")) {
                        cardLayout.show(contentPanel, "Quản lý thông tin");
                        updateSelectedButton(button);
                    } else {
                        if (contentPanel.getComponentCount() == 0 || contentPanel.getComponent(0).getName() == null ||
                                !contentPanel.getComponent(0).getName().equals("CustomerPanel")) {
                            CustomerPanel customerPanel = new CustomerPanel();
                            customerPanel.setName("CustomerPanel");
                            contentPanel.add(customerPanel, "CustomerPanel");
                        }
                        cardLayout.show(contentPanel, "CustomerPanel");
                        updateSelectedButton(button);
                    }
                });
                subMenuManageInfo.add(button);
            }
            menuPanel.add(subMenuManageInfo);
        }


        // Add "Báo cáo - Thống kê" last
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

    private JPanel createContentPanel(String text) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel(text));
        return panel;
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
        button.setBorder(BorderFactory.createEmptyBorder(5, 50, 5, 20)); // Indent submenu items
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
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}