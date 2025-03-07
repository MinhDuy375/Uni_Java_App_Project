package views; // Định nghĩa package chứa class này

import javax.swing.*; // Import các thành phần giao diện của Swing
import javax.swing.border.Border;
import java.util.List;
import java.awt.*; // Import các lớp liên quan đến giao diện AWT
import java.awt.event.*; // Import các lớp xử lý sự kiện
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import views.ManageComputer;

public class MainFrame extends JFrame { // Kế thừa từ JFrame để tạo cửa sổ giao diện
    private JPanel menuPanel; // Panel chứa menu bên trái
    private JPanel contentPanel; // Panel hiển thị nội dung bên phải
    private CardLayout cardLayout;

    public MainFrame() {

        // Cấu hình JFrame
        setTitle("Net Management"); // Đặt tiêu đề cho cửa sổ
        setSize(1066, 668); // Thiết lập kích thước cửa sổ (1366x768)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Đóng chương trình khi nhấn nút đóng
        setLocationRelativeTo(null);// Căn giữa cửa sổ khi chạy
        setBackground(Color.GRAY); // Đặt màu nền xám cho JFrame
        setMinimumSize(new Dimension(600, 120)); // Đặt kích thước tối thiểu là 800x600

        // Tạo JPanel chính với BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout()); // Dùng BorderLayout để chia khu vực
        mainPanel.setBackground(Color.GRAY); // Đặt màu nền xám

        // Tạo header panel (thanh ngang trên cùng)
        JPanel headerPanel = new JPanel(new BorderLayout()); // Panel chứa header, dùng BorderLayout
        headerPanel.setBackground(Color.WHITE); // Màu nền trắng
        headerPanel.setPreferredSize(new Dimension(1127, 80)); // Định kích thước chiều cao 80px
        headerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(173, 173, 173, 200))); // Viền
                                                                                                           // trên

        JPanel netCoBlock = new JPanel(new GridBagLayout()); // Dùng BorderLayout để dễ căn chỉnh
        netCoBlock.setBackground(new Color(97, 187, 252)); // Màu xanh nhạt
        netCoBlock.setPreferredSize(new Dimension(235, 80)); // Kích thước ngang bằng menu, cao 80px

        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/views/NétCỏ.png"));
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setPreferredSize(new Dimension(150, 40));

        JLabel logo = new JLabel("WWW");
        logo.setFont(new Font("WWW", Font.BOLD, 35));
        logo.setForeground(Color.WHITE);

        netCoBlock.add(imageLabel);
        // Tạo nút thông báo (biểu tượng chuông)
        JButton bellButton = new JButton("\uD83D\uDD14"); // Unicode của 🔔
        bellButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bellButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20)); // Đặt font hỗ trợ Unicode
        bellButton.setBackground(Color.white); // Nền trắng
        bellButton.setForeground(Color.DARK_GRAY); // Chữ màu đen
        bellButton.setBorderPainted(false); // Ẩn viền
        bellButton.setFocusPainted(false); // Ẩn viền khi focus

        JButton account = new JButton("Admin");
        account.setCursor(new Cursor(Cursor.HAND_CURSOR));

        account.setPreferredSize(new Dimension(150, 80));
        account.setBorderPainted(false); // Ẩn viền
        account.setFocusPainted(false); // Ẩn viền khi focus
        account.setBackground(Color.white);

        JPanel notification = new JPanel(new BorderLayout());
        notification.setPreferredSize(new Dimension(260, 80));
        notification.setBackground(Color.WHITE);
        notification.add(bellButton, BorderLayout.WEST);
        notification.add(account, BorderLayout.EAST);
        notification.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));
        // Thêm thành phần vào headerPanel

        headerPanel.add(netCoBlock, BorderLayout.WEST); // Thêm block logo vào bên trái
        headerPanel.add(notification, BorderLayout.EAST); // Thêm nút thông báo vào bên phải

        // Tạo menu panel bên trái
        menuPanel = new JPanel(new FlowLayout()); // Khởi tạo panel menu
        menuPanel.setPreferredSize(new Dimension(235, 0)); // Định chiều rộng 240px
        menuPanel.setBackground(new Color(0, 54, 92)); // Màu nền xanh đậm
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Không có khoảng cách ngang và dọc

        // Tạo content panel (nơi hiển thị nội dung chính)
        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.setBorder(BorderFactory.createMatteBorder(16, 16, 16, 16, new Color(239, 241, 249)));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.setBackground(Color.white);
        contentPanel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, Color.white));
        paddingPanel.add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(new ManageComputer(), "Quản lý bàn máy");
        contentPanel.add(createContentPanel("Menu dịch vụ"), "Menu dịch vụ");
        contentPanel.add(createContentPanel("Khuyến mãi"), "Khuyến mãi");
        contentPanel.add(createContentPanel("Quản lý thông tin"), "Quản lý thông tin");
        contentPanel.add(createContentPanel("Báo cáo - Thống kê"), "Báo cáo - Thống kê");

        String[] menuItems = {
                "Quản lý bàn máy",
                "Menu dịch vụ",
                "Khuyến mãi",
                "Quản lý thông tin",
                "Báo cáo - Thống kê"
        };
        for (String item : menuItems) { // Lặp qua từng mục menu
            JButton button = new JButton(item); // Tạo nút cho mỗi mục
            styleMenuButton(button);
            // Gọi hàm thiết lập style cho nút
            button.addActionListener(e -> cardLayout.show(contentPanel, item));
            menuPanel.add(button); // Thêm nút vào panel menu
        }

        // Thêm các thành phần vào mainPanel
        mainPanel.add(headerPanel, BorderLayout.NORTH); // Thêm header vào trên cùng
        mainPanel.add(menuPanel, BorderLayout.WEST); // Thêm menu vào bên trái
        mainPanel.add(paddingPanel, BorderLayout.CENTER); // Thêm content vào trung tâm

        // Thêm mainPanel vào JFrame
        add(mainPanel);

    }

    private JPanel createContentPanel(String text) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        panel.add(new JLabel(text));
        return panel;
    }

    private JButton selectedButton = null;
    private List<JButton> menuButtons = new ArrayList<>();

    // Hàm thiết lập style cho các nút menu
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
        button.setBackground(new Color(0, 54, 92)); // Màu nền mặc định

        // Mouse Listener để xử lý hover và click
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(new Color(70, 100, 120)); // Hover màu xám nhạt
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(new Color(0, 54, 92)); // Quay về màu nền mặc định
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Nếu đã có nút được chọn trước đó, đặt lại màu nền của nó
                if (selectedButton != null && selectedButton != button) {
                    selectedButton.setBackground(new Color(0, 54, 92)); // Quay về màu nền mặc định ngay lập tức
                }

                // Đặt nút này thành được chọn
                selectedButton = button;
                button.setBackground(new Color(97, 187, 252)); // Màu xanh dương nhạt
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { // Chạy giao diện trong luồng sự kiện Swing
            MainFrame frame = new MainFrame(); // Tạo đối tượng MainFrame
            frame.setVisible(true); // Hiển thị cửa sổ
        });
    }
}
