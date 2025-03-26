package views;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class StatisticsPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable statsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> filterComboBox;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton searchButton;
    private JLabel totalRevenueLabel, totalOrdersLabel;
    private ChartPanel chartPanel;
    private DefaultCategoryDataset dataset;
    private JFreeChart chart;

    public StatisticsPanel() {
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Panel với tiêu đề và bộ lọc
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Báo cáo - Thống kê", SwingConstants.LEFT);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);

        String[] columnNames = { "Mã", "Doanh thu (VNĐ)", "Số lượng khách" }; // Bỏ cột "Ngày"
        filterComboBox = new JComboBox<>(columnNames);
        filterComboBox.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));

        searchField = new JTextField(15);
        searchField.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));

        searchButton = new JButton("Tìm kiếm");
        styleButton(searchButton);

        searchPanel.add(filterComboBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Bảng thống kê
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(239, 241, 249));
                }
                if (column == 0 || column == 1 || column == 2) { // Điều chỉnh chỉ số cột
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(statsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Center Panel với thông tin tổng quan và biểu đồ
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalRevenueLabel = createLabel("Tổng doanh thu: 0 VNĐ");
        totalOrdersLabel = createLabel("Tổng số hóa đơn: 0");

        infoPanel.add(totalRevenueLabel);
        infoPanel.add(totalOrdersLabel);

        centerPanel.add(infoPanel, BorderLayout.NORTH);

        dataset = new DefaultCategoryDataset();
        chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        chartPanel.setBackground(Color.WHITE);
        centerPanel.add(chartPanel, BorderLayout.CENTER);

        JPanel tableAndChartPanel = new JPanel(new GridLayout(2, 1));
        tableAndChartPanel.add(scrollPane);
        tableAndChartPanel.add(centerPanel);
        add(tableAndChartPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton exportButton = new JButton("In PDF");
        styleButton(exportButton);
        exportButton.addActionListener(e -> exportToPDF());
        buttonPanel.add(exportButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setupSearch();
        loadStatistics();
    }

    private void setupSearch() {
        searchButton.addActionListener(e -> filter());
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

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        label.setForeground(new Color(0, 54, 92));
        return label;
    }

    private JFreeChart createChart(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                "",
                "Mã hóa đơn",
                "Doanh thu (triệu VNĐ)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 54, 92));
        renderer.setMaximumBarWidth(0.05);

        chart.getTitle().setFont(new Font("IBM Plex Mono", Font.BOLD, 16));
        plot.getDomainAxis().setLabelFont(new Font("IBM Plex Mono", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("IBM Plex Mono", Font.PLAIN, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("IBM Plex Mono", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("IBM Plex Mono", Font.PLAIN, 10));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        rangeAxis.setLowerBound(0);

        return chart;
    }

    private void loadStatistics() {
        double totalRevenue = 0;
        int totalOrders = 0;
        Map<String, Double> invoiceRevenue = new HashMap<>();
        tableModel.setRowCount(0);
        dataset.clear();

        try {
            ResultSet rs = dbManager.select("HOA_DON", new String[] { "MaHD", "SoTien" }, ""); // Bỏ cột NgayLap
            while (rs.next()) {
                String maHD = rs.getString("MaHD");
                int soTien = rs.getInt("SoTien");

                totalRevenue += soTien;
                totalOrders++;

                double revenueInMillion = soTien / 1_000_000.0;
                invoiceRevenue.put(maHD, revenueInMillion);

                tableModel.addRow(new Object[] { maHD, soTien, 1 }); // Bỏ cột NgayLap
                dataset.addValue(revenueInMillion, "Doanh thu", maHD);
            }
            rs.close();

            DecimalFormat df = new DecimalFormat("#,### VNĐ");
            totalRevenueLabel.setText("Tổng doanh thu: " + df.format(totalRevenue));
            totalOrdersLabel.setText("Tổng số hóa đơn: " + totalOrders);

            CategoryPlot plot = chartPanel.getChart().getCategoryPlot();
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            double maxRevenue = invoiceRevenue.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.1)
                    * 1.2;
            rangeAxis.setUpperBound(maxRevenue > 0 ? maxRevenue : 0.1);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải thống kê: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToPDF() {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            File fontFile = new File("NetManager_JavaApp/resources/fonts/Roboto-Medium.ttf");
            System.out.println("Đường dẫn font: " + fontFile.getAbsolutePath());
            if (!fontFile.exists()) {
                throw new IOException("File font Roboto-Medium.ttf không tồn tại tại: " + fontFile.getAbsolutePath());
            }
            PDType0Font font = PDType0Font.load(document, fontFile);

            // Tiêu đề
            contentStream.beginText();
            contentStream.setFont(font, 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("BÁO CÁO THỐNG KÊ");
            contentStream.endText();

            // Thông tin tổng quan
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, 720);
            contentStream.showText(totalRevenueLabel.getText());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(totalOrdersLabel.getText());
            contentStream.endText();

            // Biểu đồ
            File chartFile = new File("chart.png");
            ChartUtilities.saveChartAsPNG(chartFile, chart, 500, 300);
            PDImageXObject chartImage = PDImageXObject.createFromFile(chartFile.getAbsolutePath(), document);
            contentStream.drawImage(chartImage, 50, 350, 500, 300);

            // Bảng dữ liệu
            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.newLineAtOffset(50, 320);
            contentStream.showText("Danh sách hóa đơn:");
            contentStream.endText();

            float y = 300;
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(50 + i * 100, y);
                contentStream.showText(tableModel.getColumnName(i));
                contentStream.endText();
            }
            y -= 20;

            for (int row = 0; row < tableModel.getRowCount() && y > 50; row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    String value = tableModel.getValueAt(row, col).toString();
                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(50 + col * 100, y);
                    contentStream.showText(value);
                    contentStream.endText();
                }
                y -= 20;
            }

            contentStream.close();

            // Lưu file PDF
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("ThongKe.pdf"));
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File outputFile = fileChooser.getSelectedFile();
                document.save(outputFile);
                JOptionPane.showMessageDialog(this, "Đã xuất báo cáo thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            chartFile.delete();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất PDF: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 150, 200));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(97, 187, 252));
            }
        });
    }
}