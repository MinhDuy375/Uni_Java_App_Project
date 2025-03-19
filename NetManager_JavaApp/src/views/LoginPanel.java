package views; // Khai báo package chứa class này

import javax.swing.*; // Import thư viện Swing để tạo giao diện
import java.awt.*; // Import thư viện AWT để hỗ trợ đồ họa
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*; // Import thư viện JDBC để làm việc với database

// Class LoginPanel kế thừa JFrame để tạo cửa sổ đăng nhập
public class LoginPanel extends JFrame {
    private JTextField txtUsername; // Ô nhập tên đăng nhập
    private JPasswordField txtPassword; // Ô nhập mật khẩu
    private JToggleButton rdoManager, rdoEmployee; // Nút chọn vai trò

    // Constructor
    public LoginPanel() {
        setTitle("Đăng nhập"); // Đặt tiêu đề cửa sổ
        setSize(600, 550); // Kích thước cửa sổ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Đóng chương trình khi tắt cửa sổ
        setResizable(false); // Không cho phép thay đổi kích thước
        setLocationRelativeTo(null); // Căn giữa cửa sổ trên màn hình

        // Tạo panel với nền gradient
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 102, 204), getWidth(), getHeight(),
                        new Color(0, 51, 102)); // Gradient từ xanh nhạt sang xanh đậm
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight()); // Tô màu nền
            }
        };
        panel.setLayout(new GridBagLayout()); // Dùng GridBagLayout để bố cục linh hoạt
        add(panel); // Thêm panel vào JFrame

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 5, 15); // Tạo khoảng cách giữa các thành phần
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa các thành phần

        // Thêm logo vào giao diện
        JLabel lblLogo = new JLabel(new ImageIcon(getClass().getResource("/views/NétCỏ.png")));
        lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(lblLogo, gbc);

        // Nhãn tài khoản
        gbc.gridy++;
        JLabel lblUsername = createLabel("Tài khoản");
        panel.add(lblUsername, gbc);

        // Ô nhập tài khoản
        gbc.gridy++;
        txtUsername = createTextField();
        panel.add(txtUsername, gbc);

        // Nhãn mật khẩu
        gbc.gridy++;
        JLabel lblPassword = createLabel("Mật khẩu");
        panel.add(lblPassword, gbc);

        // Ô nhập mật khẩu
        gbc.gridy++;
        txtPassword = createPasswordField();
        panel.add(txtPassword, gbc);

        // Nút chọn vai trò (Quản lý hoặc Nhân viên)
        gbc.gridy++;
        gbc.gridwidth = 1;
        rdoManager = createToggleButton("Quản lý");
        rdoEmployee = createToggleButton("Nhân viên");
        rdoEmployee.setPreferredSize(new Dimension(125, 30));
        rdoManager.setPreferredSize(new Dimension(125, 30));

        ButtonGroup roleGroup = new ButtonGroup(); // Nhóm để chọn 1 trong 2 nút

        roleGroup.add(rdoManager);
        roleGroup.add(rdoEmployee);

        gbc.gridx = 0;
        panel.add(rdoManager, gbc);
        gbc.gridx = 1;
        panel.add(rdoEmployee, gbc);

        // Nút đăng nhập
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnLogin = createButton("Đăng nhập");
        panel.add(btnLogin, gbc);

        // Xử lý sự kiện khi nhấn nút đăng nhập
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText(); // Lấy giá trị từ ô tài khoản
            String password = new String(txtPassword.getPassword()); // Lấy giá trị từ ô mật khẩu
            String role = rdoManager.isSelected() ? "Quản lý" : "Nhân viên"; // Lấy vai trò được chọn

            if (checkLogin(username, password, role)) { // Kiểm tra đăng nhập
                JOptionPane.showMessageDialog(null, "Đăng nhập thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // Phương thức tạo nhãn
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Font chữ đậm
        label.setForeground(Color.WHITE); // Màu chữ trắng
        return label;
    }

    // Phương thức tạo ô nhập văn bản
    private JTextField createTextField() {
        JTextField textField = new JTextField(18);
        styleTextField(textField);
        return textField;
    }

    // Phương thức tạo ô nhập mật khẩu
    private JPasswordField createPasswordField() {
        JPasswordField passwordField = new JPasswordField(18);
        styleTextField(passwordField);
        return passwordField;
    }

    // Định dạng chung cho ô nhập liệu
    private void styleTextField(JComponent component) {
        component.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Font chữ bình thường
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2), // Viền ô nhập
                BorderFactory.createEmptyBorder(5, 10, 8, 10))); // Khoảng cách bên trong
        component.setBackground(new Color(240, 240, 240)); // Màu nền nhạt
    }

    // Phương thức tạo nút chọn vai trò
    private JToggleButton createToggleButton(String text) {
        JToggleButton toggleButton = new JToggleButton(text);
        toggleButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toggleButton.setBackground(Color.LIGHT_GRAY);
        toggleButton.setFocusPainted(false);
        toggleButton.setMargin(new Insets(0, 40, 50, 20));
        toggleButton.setBorder(BorderFactory.createEmptyBorder(8, 3, 9, 3));

        // Xử lý sự kiện khi chọn nút vai trò
        toggleButton.addActionListener(e -> {
            toggleButton.setBackground(new Color(50, 150, 250)); // Đổi màu khi được chọn
            toggleButton.setForeground(Color.WHITE);
            if (toggleButton == rdoManager) {
                rdoEmployee.setBackground(Color.LIGHT_GRAY);
                rdoEmployee.setForeground(Color.BLACK);
            } else {
                rdoManager.setBackground(Color.LIGHT_GRAY);
                rdoManager.setForeground(Color.BLACK);
            }
        });

        return toggleButton;
    }

    // Phương thức tạo nút đăng nhập
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(50, 150, 250)); // Màu xanh sáng
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setMargin(new Insets(550, 0, 0, 0));
        // Hiệu ứng hover khi di chuột vào nút
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(30, 130, 230)); // Đổi màu khi hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 150, 250)); // Trả lại màu ban đầu
            }
        });

        return button;
    }

    // Kiểm tra đăng nhập bằng cách truy vấn database
    private boolean checkLogin(String username, String password, String role) {
        String url = "jdbc:sqlite:your_database.db"; // Kết nối đến SQLite
        String query = "SELECT * FROM users WHERE username=? AND password=? AND role=?";

        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Nếu có dữ liệu trả về, đăng nhập thành công
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

// Chạy chương trình
