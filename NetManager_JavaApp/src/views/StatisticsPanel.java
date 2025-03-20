package views;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.io.font.PdfEncodings;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
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
        JFreeChart chart = createChart(dataset);
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

            try {
                PdfWriter writer = new PdfWriter(filePath);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Tạo font đậm hỗ trợ tiếng Việt
                PdfFont boldFont = PdfFontFactory.createFont("fonts/arialbd.ttf", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                PdfFont regularFont = PdfFontFactory.createFont("fonts/arial.ttf", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

                // Tạo tiêu đề với font đậm
                Text titleText = new Text("BÁO CÁO THỐNG KÊ").setFont(boldFont).setFontSize(16);
                Paragraph titleParagraph = new Paragraph(titleText);
                document.add(titleParagraph);

                // Thêm các đoạn văn bản khác với font thường
                document.add(new Paragraph(totalRevenueLabel.getText()).setFont(regularFont));
                document.add(new Paragraph(totalOrdersLabel.getText()).setFont(regularFont));

                document.close();
                JOptionPane.showMessageDialog(this, "Xuất PDF thành công tại: " + filePath, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi tạo font hoặc xuất PDF: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất PDF: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}