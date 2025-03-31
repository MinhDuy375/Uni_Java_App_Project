package views;

<<<<<<< Updated upstream
=======
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

>>>>>>> Stashed changes
import javax.swing.*;
import java.awt.*;
<<<<<<< Updated upstream
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StatisticsPanel extends JPanel {
    private JTable statsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton searchButton;
=======
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StatisticsPanel extends JPanel {
    private DatabaseManager dbManager;
    private JComboBox<String> timeFilterComboBox;
    private JPanel chartPanel;
>>>>>>> Stashed changes

    public StatisticsPanel() {
        setLayout(new BorderLayout());

<<<<<<< Updated upstream
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
=======
        // Bộ lọc thời gian
        JPanel filterPanel = new JPanel();
        timeFilterComboBox = new JComboBox<>(new String[]{"Theo ngày", "Theo tháng"});
        timeFilterComboBox.addActionListener(e -> updateRevenueChart());
        filterPanel.add(new JLabel("Hiển thị doanh thu: "));
        filterPanel.add(timeFilterComboBox);
        add(filterPanel, BorderLayout.NORTH);
>>>>>>> Stashed changes

        // Biểu đồ doanh thu
        chartPanel = new JPanel(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        // Thống kê sử dụng máy
        addMachineUsageStats();

<<<<<<< Updated upstream
        String[] columnNames = {"Mã", "Doanh thu (VNĐ)", "Ngày", "Số lượng khách"};
        filterComboBox = new JComboBox<>(columnNames);
        filterComboBox.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
=======
        // Biểu đồ doanh thu theo nhân viên
        addStaffRevenueChart();
>>>>>>> Stashed changes

        // Danh sách hóa đơn
        addPaidInvoicesTable();

<<<<<<< Updated upstream
        searchButton = new JButton("Tìm kiếm");
        styleButton(searchButton);

        searchPanel.add(filterComboBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(columnNames, 0);
        statsTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        statsTable.setRowSorter(sorter);

        statsTable.setShowGrid(false);
        statsTable.setIntercellSpacing(new Dimension(0, 0));
        statsTable.setRowHeight(30);
        statsTable.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        statsTable.getTableHeader().setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        statsTable.getTableHeader().setBackground(new Color(97, 187, 252));
        statsTable.getTableHeader().setForeground(Color.WHITE);

        statsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 241, 249));
                }
                if (column == 0 || column == 1 || column == 3) {
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(statsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton addButton = new JButton("Thêm");
        JButton editButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");

        styleButton(addButton);
        styleButton(editButton);
        styleButton(deleteButton);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addSampleData();
        setupSearch();
    }

    private void setupSearch() {
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filter();
            }
        });
    }

    private void filter() {
        int columnIndex = filterComboBox.getSelectedIndex();
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(97, 187, 252));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("IBM Plex Mono", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 35));
=======
        // Hiển thị biểu đồ mặc định
        updateRevenueChart();
    }

    private void updateRevenueChart() {
        chartPanel.removeAll();

        String selectedFilter = (String) timeFilterComboBox.getSelectedItem();
        Map<String, Double> revenueData;
        String categoryLabel;

        try {
            if ("Theo ngày".equals(selectedFilter)) {
                revenueData = dbManager.getRevenueByDay("2025-03-01", "2025-03-31");
                categoryLabel = "Ngày";
            } else {
                revenueData = dbManager.getRevenueByMonth("2025-01-01", "2025-12-31");
                categoryLabel = "Tháng";
            }

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, Double> entry : revenueData.entrySet()) {
                dataset.addValue(entry.getValue(), "Doanh thu", entry.getKey());
            }

            JFreeChart barChart = ChartFactory.createBarChart(
                    "Doanh thu " + selectedFilter.toLowerCase(),
                    categoryLabel,
                    "Doanh thu (VND)",
                    dataset
            );

            ChartPanel chartPanelInstance = new ChartPanel(barChart);
            chartPanel.add(chartPanelInstance, BorderLayout.CENTER);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy dữ liệu doanh thu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void addMachineUsageStats() {
        JPanel machineStatsPanel = new JPanel(new GridLayout(2, 1));
        machineStatsPanel.setBorder(BorderFactory.createTitledBorder("Thống kê sử dụng máy"));

        try {
            JLabel todayLabel = new JLabel("Máy sử dụng hôm nay: " + dbManager.getMachinesUsedToday());
            JLabel weekLabel = new JLabel("Máy sử dụng tuần này: " + dbManager.getMachinesUsedThisWeek());

            machineStatsPanel.add(todayLabel);
            machineStatsPanel.add(weekLabel);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy dữ liệu sử dụng máy: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        add(machineStatsPanel, BorderLayout.WEST);
    }

    private void addStaffRevenueChart() {
        JPanel staffChartPanel = new JPanel(new BorderLayout());
        staffChartPanel.setBorder(BorderFactory.createTitledBorder("Doanh thu theo nhân viên"));
>>>>>>> Stashed changes

        try {
            DefaultPieDataset dataset = new DefaultPieDataset();
            Map<String, Double> revenueByStaff = dbManager.getRevenueByStaff();
            for (Map.Entry<String, Double> entry : revenueByStaff.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }

            JFreeChart pieChart = ChartFactory.createPieChart(
                    "Doanh thu theo nhân viên",
                    dataset,
                    true,
                    true,
                    false
            );

            ChartPanel chartPanelInstance = new ChartPanel(pieChart);
            staffChartPanel.add(chartPanelInstance, BorderLayout.CENTER);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy dữ liệu doanh thu nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        add(staffChartPanel, BorderLayout.EAST);
    }

    private void addPaidInvoicesTable() {
        JPanel invoicesPanel = new JPanel(new BorderLayout());
        invoicesPanel.setBorder(BorderFactory.createTitledBorder("Danh sách hóa đơn đã thanh toán"));

        String[] columnNames = {"Mã HD", "Mã KH", "Mã NV", "Mã Máy", "Ngày", "Số Tiền", "Hình Thức Thanh Toán"};
        try {
            List<String[]> invoices = dbManager.getPaidInvoices();
            String[][] data = invoices.toArray(new String[0][0]);

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);
            invoicesPanel.add(scrollPane, BorderLayout.CENTER);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy danh sách hóa đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        add(invoicesPanel, BorderLayout.SOUTH);
    }

    private void addSampleData() {
        tableModel.addRow(new Object[]{1, 5000000, "01/03/2025", 20});
        tableModel.addRow(new Object[]{2, 3000000, "02/03/2025", 15});
    }
}
