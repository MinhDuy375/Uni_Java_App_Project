package views;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PromotionsPanel extends JPanel {
    private JTable promotionsTable;
    private DefaultTableModel tableModel;

    public PromotionsPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel title = new JLabel("Hoạt động khuyến mãi", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Bảng khuyến mãi
        String[] columns = {"ID", "Tên khuyến mãi", "Giảm giá (%)", "Ngày bắt đầu", "Ngày kết thúc"};
        tableModel = new DefaultTableModel(columns, 0);
        promotionsTable = new JTable(tableModel);

        // Tùy chỉnh giao diện bảng
        promotionsTable.setShowGrid(false); // Tắt lưới để trông phẳng hơn
        promotionsTable.setIntercellSpacing(new Dimension(0, 0)); // Loại bỏ khoảng cách giữa các ô
        promotionsTable.setRowHeight(30); // Tăng chiều cao hàng
        promotionsTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        promotionsTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        promotionsTable.getTableHeader().setBackground(new Color(97, 187, 252)); // Màu xanh nhạt cho tiêu đề bảng
        promotionsTable.getTableHeader().setForeground(Color.WHITE);

        // Tùy chỉnh màu nền xen kẽ cho các hàng
        promotionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 241, 249)); // Xen kẽ trắng và xám nhạt
                }
                // Căn giữa nội dung cho cột "ID" (cột 0) và "Giảm giá (%)" (cột 2)
                if (column == 0 || column == 2) {
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(promotionsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Panel nút điều khiển
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");

        // Tùy chỉnh giao diện nút
        styleButton(addButton);
        styleButton(editButton);
        styleButton(deleteButton);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Thêm dữ liệu giả
        addSampleData();
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(97, 187, 252)); // Màu xanh nhạt
        button.setForeground(Color.WHITE);
        button.setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 35));

        // Hiệu ứng hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 150, 200)); // Màu xanh đậm hơn khi hover
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(97, 187, 252)); // Quay lại màu gốc
            }
        });
    }

    private void addSampleData() {
        tableModel.addRow(new Object[]{1, "Khuyến mãi Tết", 20, "01/01/2025", "10/01/2025"});
        tableModel.addRow(new Object[]{2, "Giờ vàng", 50, "07/03/2025", "07/03/2025"});
    }
}