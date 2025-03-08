package views;

import javax.swing.*;
import javax.swing.border.Border;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import views.ManageComputer;

public class MainFrame extends JFrame {
    private JPanel menuPanel;
    private JPanel subMenuPanelManageInfo; // Thanh menu phụ cho "Quản lý thông tin"
    private JPanel subMenuPanelServiceMenu; // Thanh menu phụ mới cho "Menu dịch vụ"
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JButton backButtonManageInfo; // Nút quay lại cho "Quản lý thông tin"
    private JButton backButtonServiceMenu; // Nút quay lại cho "Menu dịch vụ"
    private boolean isSubMenuManageInfoVisible = false;
    private boolean isSubMenuServiceMenuVisible = false;
    private JPanel sidebar; // Panel chứa menu hoặc submenu

    public MainFrame() {
        setTitle("Net Management");
        setSize(1066, 668);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Color.GRAY);
        setMinimumSize(new Dimension(600, 120));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.GRAY);

        // Header Panel (giữ nguyên bố cục thanh ngang gốc)
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

        JButton account = new JButton("Admin");
        account.setCursor(new Cursor(Cursor.HAND_CURSOR));
        account.setPreferredSize(new Dimension(150, 80));
        account.setBorderPainted(false);
        account.setFocusPainted(false);
        account.setBackground(Color.WHITE);

        JPanel notification = new JPanel(new BorderLayout());
        notification.setPreferredSize(new Dimension(260, 80));
        notification.setBackground(Color.WHITE);
        notification.add(bellButton, BorderLayout.WEST);
        notification.add(account, BorderLayout.EAST);
        notification.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        headerPanel.add(netCoBlock, BorderLayout.WEST);
        headerPanel.add(notification, BorderLayout.EAST);

        // Sidebar với CardLayout để chuyển đổi giữa menu và submenu
        sidebar = new JPanel(new CardLayout());
        sidebar.setPreferredSize(new Dimension(235, 0));

        // Menu Panel (thanh menu chính)
        menuPanel = new JPanel(new FlowLayout());
        menuPanel.setPreferredSize(new Dimension(235, 0));
        menuPanel.setBackground(new Color(0, 54, 92));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        // Sub Menu Panel cho "Quản lý thông tin" (giữ nguyên)
        subMenuPanelManageInfo = new JPanel(new FlowLayout());
        subMenuPanelManageInfo.setPreferredSize(new Dimension(235, 0));
        subMenuPanelManageInfo.setBackground(new Color(0, 54, 92));
        subMenuPanelManageInfo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        subMenuPanelManageInfo.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        // Nút Quay lại trên subMenuPanelManageInfo
        backButtonManageInfo = new JButton("<< Quay lại");
        backButtonManageInfo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButtonManageInfo.setFont(new Font("IBM Plex Mono", Font.BOLD, 15));
        backButtonManageInfo.setForeground(Color.WHITE);
        backButtonManageInfo.setBackground(new Color(0, 54, 92));
        backButtonManageInfo.setBorderPainted(false);
        backButtonManageInfo.setFocusPainted(false);
        backButtonManageInfo.setPreferredSize(new Dimension(235, 50));
        backButtonManageInfo.setHorizontalAlignment(SwingConstants.LEFT);
        backButtonManageInfo.setBorder(BorderFactory.createEmptyBorder(10, 30, 15, 20));
        backButtonManageInfo.addActionListener(e -> toggleSubMenu("Quản lý thông tin", false));
        backButtonManageInfo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backButtonManageInfo.setBackground(new Color(70, 100, 120));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButtonManageInfo.setBackground(new Color(0, 54, 92));
            }
        });
        subMenuPanelManageInfo.add(backButtonManageInfo);

        // Sub Menu Panel cho "Menu dịch vụ" (thêm mới)
        subMenuPanelServiceMenu = new JPanel(new FlowLayout());
        subMenuPanelServiceMenu.setPreferredSize(new Dimension(235, 0));
        subMenuPanelServiceMenu.setBackground(new Color(0, 54, 92));
        subMenuPanelServiceMenu.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        subMenuPanelServiceMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        // Nút Quay lại trên subMenuPanelServiceMenu
        backButtonServiceMenu = new JButton("<< Quay lại");
        backButtonServiceMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButtonServiceMenu.setFont(new Font("IBM Plex Mono", Font.BOLD, 15));
        backButtonServiceMenu.setForeground(Color.WHITE);
        backButtonServiceMenu.setBackground(new Color(0, 54, 92));
        backButtonServiceMenu.setBorderPainted(false);
        backButtonServiceMenu.setFocusPainted(false);
        backButtonServiceMenu.setPreferredSize(new Dimension(235, 50));
        backButtonServiceMenu.setHorizontalAlignment(SwingConstants.LEFT);
        backButtonServiceMenu.setBorder(BorderFactory.createEmptyBorder(10, 30, 15, 20));
        backButtonServiceMenu.addActionListener(e -> toggleSubMenu("Menu dịch vụ", false));
        backButtonServiceMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backButtonServiceMenu.setBackground(new Color(70, 100, 120));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButtonServiceMenu.setBackground(new Color(0, 54, 92));
            }
        });
        subMenuPanelServiceMenu.add(backButtonServiceMenu);

        // Content Panel
        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.setBorder(BorderFactory.createMatteBorder(16, 16, 16, 16, new Color(239, 241, 249)));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, Color.WHITE));
        paddingPanel.add(contentPanel, BorderLayout.CENTER);

        // Thêm các panel
        contentPanel.add(new ManageComputer(), "Quản lý bàn máy");
        contentPanel.add(createContentPanel("Menu dịch vụ"), "Menu dịch vụ");
        contentPanel.add(new InventoryPanel(), "Kho hàng");
        contentPanel.add(new PromotionsPanel(), "Khuyến mãi");
        contentPanel.add(new StaffPanel(), "Quản lý thông tin");
        contentPanel.add(new StatisticsPanel(), "Báo cáo - Thống kê");

        // Menu chính
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
            button.addActionListener(e -> {
                cardLayout.show(contentPanel, item);
                if (item.equals("Quản lý thông tin")) {
                    toggleSubMenu("Quản lý thông tin", true);
                } else if (item.equals("Menu dịch vụ")) {
                    toggleSubMenu("Menu dịch vụ", true);
                } else {
                    toggleSubMenu("none", false);
                }
            });
            menuPanel.add(button);
        }

        // Menu phụ cho "Quản lý thông tin" (giữ nguyên)
        String[] subMenuItemsManageInfo = {"Thông tin khách hàng", "Thông tin nhân viên"};
        for (String item : subMenuItemsManageInfo) {
            JButton button = new JButton(item);
            styleMenuButton(button);
            button.addActionListener(e -> {
                if (item.equals("Thông tin nhân viên")) {
                    cardLayout.show(contentPanel, "Quản lý thông tin");
                } else {
                    JOptionPane.showMessageDialog(this, "Chức năng 'Thông tin khách hàng' đang phát triển!");
                }
            });
            subMenuPanelManageInfo.add(button);
        }

        // Menu phụ cho "Menu dịch vụ" (thêm mới)
        String[] subMenuItemsServiceMenu = {"Menu", "Kho hàng"};
        for (String item : subMenuItemsServiceMenu) {
            JButton button = new JButton(item);
            styleMenuButton(button);
            button.addActionListener(e -> {
                if (item.equals("Kho hàng")) {
                    cardLayout.show(contentPanel, "Kho hàng");
                } else {
                    JOptionPane.showMessageDialog(this, "Chức năng 'Menu' đang phát triển!");
                }
            });
            subMenuPanelServiceMenu.add(button);
        }

        // Thêm menuPanel và các submenu vào sidebar
        sidebar.add(menuPanel, "mainMenu");
        sidebar.add(subMenuPanelManageInfo, "subMenuManageInfo");
        sidebar.add(subMenuPanelServiceMenu, "subMenuServiceMenu");

        // Thêm các panel vào mainPanel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(paddingPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void toggleSubMenu(String menuType, boolean showSubMenu) {
        CardLayout cl = (CardLayout) sidebar.getLayout();
        if (showSubMenu) {
            if (menuType.equals("Quản lý thông tin") && !isSubMenuManageInfoVisible) {
                cl.show(sidebar, "subMenuManageInfo");
                isSubMenuManageInfoVisible = true;
                isSubMenuServiceMenuVisible = false;
            } else if (menuType.equals("Menu dịch vụ") && !isSubMenuServiceMenuVisible) {
                cl.show(sidebar, "subMenuServiceMenu");
                isSubMenuServiceMenuVisible = true;
                isSubMenuManageInfoVisible = false;
            }
        } else {
            cl.show(sidebar, "mainMenu");
            isSubMenuManageInfoVisible = false;
            isSubMenuServiceMenuVisible = false;
        }
        revalidate();
        repaint();
    }

    private JPanel createContentPanel(String text) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel(text));
        return panel;
    }

    private JButton selectedButton = null;
    private List<JButton> menuButtons = new ArrayList<>();

    private void styleMenuButton(JButton button) {
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(235, 50));
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

            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectedButton != null && selectedButton != button) {
                    selectedButton.setBackground(new Color(0, 54, 92));
                }
                selectedButton = button;
                button.setBackground(new Color(97, 187, 252));
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