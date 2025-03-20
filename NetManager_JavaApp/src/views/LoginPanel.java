package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginPanel extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JToggleButton rdoManager, rdoEmployee;
    private DatabaseManager dbManager;

    public LoginPanel() {
        dbManager = new DatabaseManager();
        setTitle("Đăng nhập");
        setSize(600, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 102, 204), getWidth(), getHeight(),
                        new Color(0, 51, 102));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new GridBagLayout());
        add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 5, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblLogo = new JLabel(new ImageIcon(getClass().getResource("/views/NétCỏ.png")));
        lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(lblLogo, gbc);

        gbc.gridy++;
        JLabel lblUsername = createLabel("Tài khoản");
        panel.add(lblUsername, gbc);

        gbc.gridy++;
        txtUsername = createTextField();
        panel.add(txtUsername, gbc);

        gbc.gridy++;
        JLabel lblPassword = createLabel("Mật khẩu");
        panel.add(lblPassword, gbc);

        gbc.gridy++;
        txtPassword = createPasswordField();
        panel.add(txtPassword, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        rdoManager = createToggleButton("Quản lý");
        rdoEmployee = createToggleButton("Nhân viên");
        rdoEmployee.setPreferredSize(new Dimension(125, 30));
        rdoManager.setPreferredSize(new Dimension(125, 30));

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(rdoManager);
        roleGroup.add(rdoEmployee);

        gbc.gridx = 0;
        panel.add(rdoManager, gbc);
        gbc.gridx = 1;
        panel.add(rdoEmployee, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnLogin = createButton("Đăng nhập");
        panel.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());
            String role = rdoManager.isSelected() ? "Quản lý" : "Nhân viên";

            if (checkLogin(username, password, role)) {
                JOptionPane.showMessageDialog(null, "Đăng nhập thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField(18);
        styleTextField(textField);
        return textField;
    }

    private JPasswordField createPasswordField() {
        JPasswordField passwordField = new JPasswordField(18);
        styleTextField(passwordField);
        return passwordField;
    }

    private void styleTextField(JComponent component) {
        component.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                BorderFactory.createEmptyBorder(5, 10, 8, 10)));
        component.setBackground(new Color(240, 240, 240));
    }

    private JToggleButton createToggleButton(String text) {
        JToggleButton toggleButton = new JToggleButton(text);
        toggleButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toggleButton.setBackground(Color.LIGHT_GRAY);
        toggleButton.setFocusPainted(false);
        toggleButton.setMargin(new Insets(0, 40, 50, 20));
        toggleButton.setBorder(BorderFactory.createEmptyBorder(8, 3, 9, 3));

        toggleButton.addActionListener(e -> {
            toggleButton.setBackground(new Color(50, 150, 250));
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

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(50, 150, 250));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setMargin(new Insets(550, 0, 0, 0));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(30, 130, 230));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 150, 250));
            }
        });

        return button;
    }

    private boolean checkLogin(String username, String password, String role) {
        String query = "SELECT * FROM TAI_KHOAN WHERE username=? AND password=? AND role=?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:D:/ProjectQuanNeet/NetManager_JavaApp/resources/Uni_JavaDeskApp.db");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Lỗi đăng nhập: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPanel login = new LoginPanel();
            login.setVisible(true);
        });
    }
}