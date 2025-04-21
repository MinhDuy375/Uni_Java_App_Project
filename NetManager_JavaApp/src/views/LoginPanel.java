package views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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

        JLabel lblLogo = new JLabel("NétCỏ", SwingConstants.CENTER);
        try {
            File fontFile = new File("NetManager_JavaApp/resources/fonts/Pacifico-Regular.ttf");
            if (!fontFile.exists()) {
                throw new IOException(
                        "File font Pacifico-Regular.ttf không tồn tại tại: " + fontFile.getAbsolutePath());
            }
            Font pacificoFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(60f);
            lblLogo.setFont(pacificoFont);
        } catch (FontFormatException | IOException e) {
            System.out.println("Lỗi khi nhúng font Pacifico-Regular: " + e.getMessage());
            lblLogo.setFont(new Font("Serif", Font.BOLD, 60));
        }
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(lblLogo, gbc);

        gbc.gridy++;
        JLabel lblUsername = createLabel("ID Tài khoản");
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
            String id = txtUsername.getText();
            String matkhau = new String(txtPassword.getPassword());
            boolean isManager = rdoManager.isSelected();

            if (id.isEmpty() || matkhau.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Vui lòng nhập đầy đủ ID tài khoản và mật khẩu!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!rdoManager.isSelected() && !rdoEmployee.isSelected()) {
                JOptionPane.showMessageDialog(null, "Vui lòng chọn vai trò (Quản lý hoặc Nhân viên)!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String role = isManager ? "admin" : "user";
            if (checkLogin(id, matkhau, role)) {
                JOptionPane.showMessageDialog(null,
                        "Đăng nhập thành công với vai trò " + (isManager ? "Quản lý" : "Nhân viên") + "!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
                MainFrame mainFrame = new MainFrame(role);
                mainFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Sai ID, mật khẩu, vai trò hoặc tài khoản không hoạt động!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
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

    private boolean checkLogin(String id, String matkhau, String expectedChucvu) {
        try {
            // Use the parameterized select method to avoid SQL injection and ensure proper
            // query execution
            ResultSet rs = dbManager.select("TAI_KHOAN",
                    new String[] { "id", "matkhau", "chucvu", "tinhtrang" },
                    "id = ? AND matkhau = ? AND chucvu = ? AND tinhtrang = ?",
                    new Object[] { Integer.parseInt(id), matkhau, expectedChucvu, 1 });

            boolean loginSuccess = rs.next();
            rs.close();
            return loginSuccess;
        } catch (SQLException e) {
            System.out.println("Lỗi đăng nhập: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            System.out.println("ID phải là số nguyên: " + e.getMessage());
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