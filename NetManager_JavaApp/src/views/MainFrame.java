package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;

public class MainFrame extends JFrame {
    private JPanel menuPanel;
    private JPanel contentPanel;
    private Font pacificoFont;

    public MainFrame() {
        // Nạp font Pacifico
        try {
            InputStream fontStream = getClass()
                    .getResourceAsStream("resources/fonts/Pacifico-Regular.ttf");
            pacificoFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(24);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontStream));
        } catch (Exception e) {
            e.printStackTrace();
            pacificoFont = new Font("Arial", Font.BOLD, 25); // Fallback nếu không nạp được
        }

        // Cấu hình JFrame
        setTitle("NetCo");
        setSize(800, 600); // Kích thước mặc định
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Căn giữa màn hình

        // Panel chính sử dụng BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE); // Nền trắng cho toàn bộ frame

        // Header panel (thanh ngang màu trắng)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE); // Màu trắng cho thanh ngang
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(173, 173, 173))); // Viền dưới xanh
                                                                                                      // nhạt

        // Khối màu xanh nhạt chứa chữ "NetCo"
        JPanel netCoBlock = new JPanel();
        netCoBlock.setBackground(new Color(97, 187, 252)); // Màu xanh nhạt
        netCoBlock.setPreferredSize(new Dimension(200, 60)); // Chiều ngang bằng menu, dọc bằng thanh

        JLabel titleLabel = new JLabel("NétCỏ");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(pacificoFont); // Sử dụng font Pacifico
        titleLabel.setHorizontalAlignment(JLabel.CENTER); // Căn giữa ngang
        titleLabel.setVerticalAlignment(JLabel.CENTER); // Căn giữa dọc
        netCoBlock.setLayout(new GridBagLayout()); // Sử dụng GridBagLayout để căn giữa dọc
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa cả ngang và dọc
        netCoBlock.add(titleLabel, gbc);

        // Thêm nút thông báo (bell icon)
        JButton bellButton = new JButton(""); // Unicode cho biểu tượng chuông
        bellButton.setFont(new Font("Arial", Font.PLAIN, 16));
        bellButton.setBackground(Color.WHITE);
        bellButton.setForeground(Color.BLACK);
        bellButton.setBorderPainted(false);
        bellButton.setFocusPainted(false);
        headerPanel.add(netCoBlock, BorderLayout.WEST);
        headerPanel.add(bellButton, BorderLayout.EAST);

        // Menu panel (bên trái)
        menuPanel = new JPanel();
        menuPanel.setPreferredSize(new Dimension(200, 0)); // Chiều rộng cố định 200px
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(0, 33, 71)); // Màu xanh đậm của menu

        // Content panel (bên phải) với vạch xanh nhạt
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentPanel = new JPanel();
        contentPanel.setBackground(new Color(245, 245, 245)); // Màu nền nhạt
        JPanel sideBar = new JPanel();
        sideBar.setBackground(new Color(0, 122, 204, 100)); // Vạch xanh nhạt (alpha 100)
        sideBar.setPreferredSize(new Dimension(0, 0)); // Chiều rộng vạch là 5px
        contentWrapper.add(sideBar, BorderLayout.WEST);
        contentWrapper.add(contentPanel, BorderLayout.CENTER);

        // Thêm menu items
        String[] menuItems = {
                "Lịch sử hoạt động", "Menu", "Khuyến mãi", "Kho hàng",
                "Thống kê kho hàng", "Quản lý Nhân viên", "Thống kê",
                "Hướng dẫn sử dụng"
        };
        for (String item : menuItems) {
            JButton button = new JButton(item);
            styleMenuButton(button); // Áp dụng style cho nút
            menuPanel.add(button);
            menuPanel.add(Box.createVerticalStrut(10)); // Khoảng cách giữa các nút
        }

        // Thêm tất cả vào main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.WEST);
        mainPanel.add(contentWrapper, BorderLayout.CENTER);

        // Thêm main panel vào frame
        add(mainPanel);

        // Responsive: Thay đổi kích thước content khi resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                contentPanel.setPreferredSize(new Dimension(
                        getWidth() - menuPanel.getPreferredSize().width - sideBar.getPreferredSize().width,
                        getHeight() - headerPanel.getPreferredSize().height));
                revalidate();
                repaint();
            }
        });
    }

    // Hàm style cho các nút menu
    private void styleMenuButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        button.setBackground(new Color(0, 33, 71));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT); // Căn trái
        button.setMargin(new Insets(5, 10, 5, 10)); // Padding cho nút
        button.setPreferredSize(new Dimension(180, 30)); // Chiều ngang 180px (để fit trong 200px của menu)

        // Hiệu ứng hover chiếm 100% chiều ngang menu
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0, 71, 133)); // Màu khi hover
                button.setContentAreaFilled(true); // Fill toàn bộ nút
                button.setPreferredSize(new Dimension(200, 30)); // Chiều ngang bằng menu (200px)
                menuPanel.revalidate();
                menuPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 33, 71)); // Màu mặc định
                button.setContentAreaFilled(false); // Ẩn fill khi rời chuột
                button.setPreferredSize(new Dimension(180, 30)); // Trở về kích thước ban đầu
                menuPanel.revalidate();
                menuPanel.repaint();
            }
        });

        // Action khi click (tạm thời chỉ in text)
        button.addActionListener(e -> {
            contentPanel.removeAll();
            JLabel label = new JLabel("Nội dung cho: " + button.getText());
            label.setHorizontalAlignment(JLabel.CENTER);
            contentPanel.add(label);
            contentPanel.revalidate();
            contentPanel.repaint();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}