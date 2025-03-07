package views;

import javax.swing.*;
import java.awt.*;

public class ManageComputer extends JPanel {
    private static final int ROWS = 3; // Số hàng
    private static final int COLS = 4; // Số cột
    private static final int TOTAL_COMPUTERS = ROWS * COLS;

    public ManageComputer() {
        setLayout(new GridLayout(ROWS, COLS, 20, 20)); // Lưới với khoảng cách giữa các ô
        setBackground(Color.WHITE);

        for (int i = 1; i <= TOTAL_COMPUTERS; i++) {
            JPanel computerPanel = createComputerBlock(i);
            add(computerPanel);
        }
    }

    private JPanel createComputerBlock(int id) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(100, 100)); // Kích thước khối vuông
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true)); // Viền đen
        panel.setBackground(new Color(97, 187, 252)); // Màu xanh nhạt
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("Máy " + id, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }
}
