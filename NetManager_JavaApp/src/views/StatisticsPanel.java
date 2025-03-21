package views;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities; // Sử dụng ChartUtilities thay vì ChartUtils
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StatisticsPanel extends JPanel {
    private DatabaseManager dbManager;
    private JLabel totalRevenueLabel, totalOrdersLabel;
    private ChartPanel chartPanel;
    private DefaultCategoryDataset dataset;
    private JFreeChart chart; // Lưu chart để sử dụng khi in PDF

    public StatisticsPanel() {
        dbManager = new DatabaseManager();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Tiêu đề
        JLabel title = new JLabel("Thống kê", SwingConstants.CENTER);
        title.setFont(new Font("IBM Plex Mono", Font.BOLD, 20));
        title.setForeground(new Color(0, 54, 92));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Panel thông tin và biểu đồ
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        // Panel thông tin
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalRevenueLabel = createLabel("Tổng doanh thu: 0 VNĐ");
        totalOrdersLabel = createLabel("Tổng số hóa đơn: 0");

        infoPanel.add(totalRevenueLabel);
        infoPanel.add(totalOrdersLabel);

        centerPanel.add(infoPanel, BorderLayout.NORTH);

        // Khởi tạo biểu đồ
        dataset = new DefaultCategoryDataset();
        chart = createChart(dataset); // Lưu chart để sử dụng sau
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        chartPanel.setBackground(Color.WHITE);
        centerPanel.add(chartPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Panel nút In PDF
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton exportButton = new JButton("In PDF");
        exportButton.setFont(new Font("IBM Plex Mono", Font.PLAIN, 12));
        exportButton.setBackground(new Color(97, 187, 252));
        exportButton.setForeground(Color.WHITE);
        exportButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        exportButton.addActionListener(e -> exportToPDF());
        buttonPanel.add(exportButton);

        add(buttonPanel, BorderLayout.SOUTH);

        loadStatistics();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("IBM Plex Mono", Font.PLAIN, 14));
        label.setForeground(new Color(0, 54, 92));
        return label;
    }

    private JFreeChart createChart(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                "", // Tiêu đề biểu đồ (để trống vì đã có tiêu đề chính)
                "Mã hóa đơn", // Nhãn trục X
                "Doanh thu (triệu VNĐ)", // Nhãn trục Y
                dataset, // Dữ liệu
                PlotOrientation.VERTICAL,
                false, // Không hiển thị legend
                true, // Hiển thị tooltips
                false // Không hiển thị URLs
        );

        // Tùy chỉnh giao diện biểu đồ
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));

        // Tùy chỉnh cột
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 54, 92)); // Màu cột: xanh đậm
        renderer.setMaximumBarWidth(0.05); // Độ rộng cột

        // Tùy chỉnh font
        chart.getTitle().setFont(new Font("IBM Plex Mono", Font.BOLD, 16));
        plot.getDomainAxis().setLabelFont(new Font("IBM Plex Mono", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("IBM Plex Mono", Font.PLAIN, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("IBM Plex Mono", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("IBM Plex Mono", Font.PLAIN, 10));

        // Tùy chỉnh trục X (xoay nhãn 45 độ)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)); // Xoay 45 độ

        // Định dạng trục Y (triệu VNĐ)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        rangeAxis.setLowerBound(0);
        rangeAxis.setUpperBound(0.1); // Tạm thời đặt giới hạn trên là 0.1 triệu (100,000 VNĐ)

        return chart;
    }

    private void loadStatistics() {
        double totalRevenue = 0;
        int totalOrders = 0;
        Map<String, Double> invoiceRevenue = new HashMap<>();

        try {
            ResultSet rs = dbManager.select("HOA_DON", new String[]{"MaHD", "SoTien"}, "");
            int rowCount = 0;
            double maxRevenue = 0; // Để điều chỉnh trục Y của biểu đồ
            while (rs.next()) {
                String maHD = rs.getString("MaHD");
                int soTien = rs.getInt("SoTien");

                totalRevenue += soTien;
                totalOrders++;
                rowCount++;

                // Doanh thu theo hóa đơn (đơn vị: triệu VNĐ)
                double revenueInMillion = soTien / 1_000_000.0; // Chuyển sang triệu VNĐ
                invoiceRevenue.put(maHD, revenueInMillion);
                maxRevenue = Math.max(maxRevenue, revenueInMillion);
            }
            System.out.println("Số dòng dữ liệu hiển thị trong StatisticsPanel: " + rowCount);
            rs.close();

            // Điều chỉnh trục Y của biểu đồ dựa trên doanh thu tối đa
            CategoryPlot plot = chartPanel.getChart().getCategoryPlot();
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setUpperBound(Math.max(0.1, maxRevenue * 1.2)); // Thêm 20% để biểu đồ không bị sát đỉnh

        } catch (SQLException e) {
            System.out.println("Lỗi tải dữ liệu thống kê: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu thống kê: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // Cập nhật nhãn
        totalRevenueLabel.setText("Tổng doanh thu: " + String.format("%,.0f", totalRevenue) + " VNĐ");
        totalOrdersLabel.setText("Tổng số hóa đơn: " + totalOrders);

        // Cập nhật dữ liệu cho biểu đồ
        dataset.clear();
        for (Map.Entry<String, Double> entry : invoiceRevenue.entrySet()) {
            dataset.addValue(entry.getValue(), "Doanh thu", entry.getKey());
        }
    }

    private void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file PDF");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF files", "pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".pdf")) {
                filePath += ".pdf";
            }

            File tempChartFile = null;
            try (PDDocument document = new PDDocument()) {
                // Tạo một trang mới
                PDPage page = new PDPage();
                document.addPage(page);

                // Nhúng font Roboto-Medium.ttf với đường dẫn tuyệt đối
                File fontFile = new File("NetManager_JavaApp/resources/fonts/Roboto-Medium.ttf");
                System.out.println("Đường dẫn font: " + fontFile.getAbsolutePath()); // In đường dẫn để kiểm tra
                if (!fontFile.exists()) {
                    throw new IOException("File font Roboto-Medium.ttf không tồn tại tại: " + fontFile.getAbsolutePath());
                }

                PDType0Font font;
                try {
                    font = PDType0Font.load(document, fontFile);
                } catch (IOException e) {
                    throw new IOException("Không thể nhúng font Roboto-Medium.ttf: " + e.getMessage(), e);
                }

                // Lưu biểu đồ thành file hình ảnh tạm thời
                tempChartFile = new File("temp_chart.png");
                ChartUtilities.saveChartAsPNG(tempChartFile, chart, 500, 300); // Sử dụng ChartUtilities thay vì ChartUtils

                // Tạo nội dung cho trang
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Tiêu đề
                    contentStream.beginText();
                    contentStream.setFont(font, 16);
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText("BÁO CÁO THỐNG KÊ");
                    contentStream.endText();

                    // Tổng doanh thu
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.newLineAtOffset(50, 720);
                    contentStream.showText(totalRevenueLabel.getText());
                    contentStream.endText();

                    // Tổng số hóa đơn
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.newLineAtOffset(50, 700);
                    contentStream.showText(totalOrdersLabel.getText());
                    contentStream.endText();

                    // Chèn biểu đồ vào PDF
                    PDImageXObject chartImage = PDImageXObject.createFromFile(tempChartFile.getAbsolutePath(), document);
                    float chartWidth = 500; // Chiều rộng biểu đồ trong PDF
                    float chartHeight = 300; // Chiều cao biểu đồ trong PDF
                    float scale = 0.8f; // Tỷ lệ thu nhỏ (80%)
                    contentStream.drawImage(chartImage, 50, 350, chartWidth * scale, chartHeight * scale); // Vị trí (50, 350)
                }

                // Lưu file PDF
                document.save(filePath);
                JOptionPane.showMessageDialog(this, "Xuất PDF thành công tại: " + filePath, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất PDF: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Xóa file hình ảnh tạm thời
                if (tempChartFile != null && tempChartFile.exists()) {
                    tempChartFile.delete();
                }
            }
        }
    }
}