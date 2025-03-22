package views;

import java.io.File;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_NAME = "Uni_JavaDeskApp.db";
    private static final String DB_URL;
    private static Connection connection;

    static {
        DB_URL = initializeDatabaseUrl();
        initializeDatabase();
    }

    private static String initializeDatabaseUrl() {
        String dbPath = "NetManager_JavaApp/resources/" + DB_NAME;
        File dbFile = new File(dbPath);
        System.out.println("Đường dẫn file cơ sở dữ liệu: " + dbFile.getAbsolutePath());

        if (dbFile.exists()) {
            System.out.println("File cơ sở dữ liệu đã tồn tại, sử dụng file hiện tại.");
            return "jdbc:sqlite:" + dbFile.getAbsolutePath();
        }

        System.out.println("Tạo file cơ sở dữ liệu mới tại: " + dbFile.getAbsolutePath());
        return "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    private static void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true); // Đảm bảo auto-commit được bật
            try (Statement stmt = connection.createStatement()) {
                // Tạo bảng KHO
                stmt.execute("CREATE TABLE IF NOT EXISTS KHO (" +
                        "MaSP TEXT PRIMARY KEY, " +
                        "TenSP TEXT NOT NULL, " +
                        "TonDu INTEGER, " +
                        "DonVi TEXT, " +
                        "GiaNhap INTEGER, " +
                        "DanhMuc TEXT)");
                // Tạo bảng KHUYEN_MAI
                stmt.execute("CREATE TABLE IF NOT EXISTS KHUYEN_MAI (" +
                        "MaKM TEXT PRIMARY KEY, " +
                        "TenChuongTrinh TEXT NOT NULL, " +
                        "MucKM TEXT, " +
                        "TGBatDau TEXT, " +
                        "TGKetThuc TEXT, " +
                        "TrangThai TEXT)");
                // Tạo bảng NHAN_VIEN
                stmt.execute("CREATE TABLE IF NOT EXISTS NHAN_VIEN (" +
                        "MaNV TEXT PRIMARY KEY, " +
                        "TenNV TEXT NOT NULL, " +
                        "ChucVu TEXT, " +
                        "CaLam TEXT, " +
                        "TinhTrang INTEGER, " +
                        "SDT TEXT)");
                // Tạo bảng TAI_KHOAN
                stmt.execute("CREATE TABLE IF NOT EXISTS TAI_KHOAN (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "matkhau TEXT NOT NULL, " +
                        "chucvu TEXT, " +
                        "batdauca TEXT, " +
                        "kethucca TEXT, " +
                        "tinhtrang INTEGER)");
                // Tạo bảng HOA_DON (bỏ cột NgayLap)
                stmt.execute("CREATE TABLE IF NOT EXISTS HOA_DON (" +
                        "MaHD TEXT PRIMARY KEY, " +
                        "MaKH TEXT, " +
                        "MaNV TEXT, " +
                        "MaKM TEXT, " +
                        "SoTien INTEGER NOT NULL, " +
                        "HinhThucThanhToan TEXT)");
                // Tạo bảng BAN_MAY
                stmt.execute("CREATE TABLE IF NOT EXISTS BAN_MAY (" +
                        "MaMay TEXT PRIMARY KEY, " +
                        "SoMay TEXT NOT NULL, " +
                        "GiaThueBan DECIMAL NOT NULL, " +
                        "TrangThai TEXT NOT NULL)");

                // Kiểm tra số dòng dữ liệu trong mỗi bảng
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TAI_KHOAN");
                if (rs.next()) {
                    System.out.println("Số dòng trong bảng TAI_KHOAN: " + rs.getInt(1));
                }
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM KHO");
                if (rs.next()) {
                    System.out.println("Số dòng trong bảng KHO: " + rs.getInt(1));
                }
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM KHUYEN_MAI");
                if (rs.next()) {
                    System.out.println("Số dòng trong bảng KHUYEN_MAI: " + rs.getInt(1));
                }
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM NHAN_VIEN");
                if (rs.next()) {
                    System.out.println("Số dòng trong bảng NHAN_VIEN: " + rs.getInt(1));
                }
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM HOA_DON");
                if (rs.next()) {
                    System.out.println("Số dòng trong bảng HOA_DON: " + rs.getInt(1));
                }
                rs.close();

                rs = stmt.executeQuery("SELECT COUNT(*) FROM BAN_MAY");
                if (rs.next()) {
                    System.out.println("Số dòng trong bảng BAN_MAY: " + rs.getInt(1));
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khởi tạo bảng: " + e.getMessage());
        }
    }

    public DatabaseManager() {
    }

    public synchronized void insert(String table, Object[] values) throws SQLException {
        String placeholders = String.join(",", new String[values.length]).replace("\0", "?");
        String sql = "INSERT INTO " + table + " VALUES (" + placeholders + ")";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer)
                    pstmt.setInt(i + 1, (Integer) values[i]);
                else if (values[i] instanceof Double)
                    pstmt.setDouble(i + 1, (Double) values[i]);
                else if (values[i] instanceof String)
                    pstmt.setString(i + 1, (String) values[i]);
            }
            pstmt.executeUpdate();
        }
    }

    public synchronized void update(String table, String[] columns, Object[] values, String whereClause)
            throws SQLException {
        String setClause = String.join("=?, ", columns) + "=?";
        String sql = "UPDATE " + table + " SET " + setClause + " WHERE " + whereClause;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer)
                    pstmt.setInt(i + 1, (Integer) values[i]);
                else if (values[i] instanceof Double)
                    pstmt.setDouble(i + 1, (Double) values[i]);
                else if (values[i] instanceof String)
                    pstmt.setString(i + 1, (String) values[i]);
            }
            pstmt.executeUpdate();
        }
    }

    public synchronized void delete(String table, String whereClause) throws SQLException {
        String sql = "DELETE FROM " + table + " WHERE " + whereClause;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }

    public synchronized ResultSet select(String table, String[] columns, String whereClause) throws SQLException {
        String columnList = String.join(",", columns);
        String sql = "SELECT " + columnList + " FROM " + table + (whereClause.isEmpty() ? "" : " WHERE " + whereClause);
        System.out.println("Thực thi truy vấn: " + sql);
        PreparedStatement pstmt = connection.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        // Không đóng pstmt ở đây, để người gọi có thể sử dụng ResultSet
        return rs;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}