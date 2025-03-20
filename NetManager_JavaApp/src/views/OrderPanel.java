package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrderPanel extends JPanel {
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private SearchPanel searchPanel;

    public OrderPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Quản lý đơn đặt món ăn", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Bảng hiển thị đơn đặt món ăn
        String[] columns = {"Mã đơn", "MÁY(N)", "Món ăn", "Số lượng", "Tổng tiền"};
        tableModel = new DefaultTableModel(columns, 0);
        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(30);
        orderTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);

        // Tạo SearchPanel
        String[] filterOptions = {"MÁY(N)", "Món ăn"};
        searchPanel = new SearchPanel(filterOptions, e -> {
            String[] data = e.getActionCommand().split(";");
            String keyword = data[0];
            int columnIndex = Integer.parseInt(data[1]);
            filterTable(keyword, columnIndex);
        });

        add(searchPanel, BorderLayout.NORTH);

        // Panel chứa các nút thao tác
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Thêm dữ liệu mẫu
        addSampleData();
    }

    private void addSampleData() {
        tableModel.addRow(new Object[]{"OD001", "MÁY 1", "Mì xào", 2, "60.000đ"});
        tableModel.addRow(new Object[]{"OD002", "MÁY 3", "Coca Cola", 1, "15.000đ"});
        tableModel.addRow(new Object[]{"OD003", "MÁY 5", "Bánh mì", 3, "45.000đ"});
        tableModel.addRow(new Object[]{"OD004", "MÁY 2", "Trà sữa", 2, "50.000đ"});
    }

    private void filterTable(String keyword, int columnIndex) {
        DefaultTableModel filteredModel = new DefaultTableModel(new String[]{"Mã đơn", "MÁY(N)", "Món ăn", "Số lượng", "Tổng tiền"}, 0);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String cellValue = tableModel.getValueAt(i, columnIndex + 1).toString().toLowerCase();
            if (cellValue.contains(keyword.toLowerCase())) {
                filteredModel.addRow(new Object[]{
                        tableModel.getValueAt(i, 0),
                        tableModel.getValueAt(i, 1),
                        tableModel.getValueAt(i, 2),
                        tableModel.getValueAt(i, 3),
                        tableModel.getValueAt(i, 4)
                });
            }
        }
        orderTable.setModel(filteredModel);
    }
}
