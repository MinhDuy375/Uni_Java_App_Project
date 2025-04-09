package views;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;

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
            connection.setAutoCommit(true);
            try (Statement stmt = connection.createStatement()) {
                // Bảng KHO
                stmt.execute("CREATE TABLE IF NOT EXISTS KHO (" +
                        "MaSP VARCHAR(10) PRIMARY KEY, " +
                        "TenSP VARCHAR(50) NOT NULL, " +
                        "TonDu INTEGER, " +
                        "DonVi VARCHAR(20), " +
                        "GiaNhap INTEGER, " +
                        "DanhMuc VARCHAR(30))");

                // Bảng KHUYEN_MAI
                stmt.execute("CREATE TABLE IF NOT EXISTS KHUYEN_MAI (" +
                        "MaKM VARCHAR(10) PRIMARY KEY, " +
                        "TenChuongTrinh VARCHAR(50) NOT NULL, " +
                        "MucKM VARCHAR(20), " +
                        "TGBatDau TEXT, " +
                        "TGKetThuc TEXT, " +
                        "TrangThai VARCHAR(20) CHECK (TrangThai IN ('Đang áp dụng', 'Hết hạn', 'Chưa áp dụng')))");

                // Bảng NHAN_VIEN
                stmt.execute("CREATE TABLE IF NOT EXISTS NHAN_VIEN (" +
                        "MaNV INTEGER PRIMARY KEY, " +
                        "TenNV VARCHAR(50) NOT NULL, " +
                        "ChucVu VARCHAR(30) CHECK (ChucVu IN ('admin', 'user')), " +
                        "CaLam VARCHAR(10) CHECK (CaLam IN ('Ca1', 'Ca2', 'Ca3')), " +
                        "TinhTrang INTEGER CHECK (TinhTrang IN (0, 1)), " +
                        "SDT VARCHAR(15) CHECK (SDT GLOB '[0-9]*'))");

                // Bảng TAI_KHOAN
                stmt.execute("CREATE TABLE IF NOT EXISTS TAI_KHOAN (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "matkhau VARCHAR(50) NOT NULL, " +
                        "chucvu VARCHAR(30), " +
                        "batdauca TEXT, " +
                        "kethucca TEXT, " +
                        "tinhtrang INTEGER CHECK (tinhtrang IN (0, 1)))");

                // Bảng BAN_MAY
                stmt.execute("CREATE TABLE IF NOT EXISTS BAN_MAY (" +
                        "MaMay INTEGER PRIMARY KEY, " +
                        "SoMay VARCHAR(10) NOT NULL, " +
                        "GiaThueBan NUMERIC NOT NULL CHECK (GiaThueBan >= 0), " +
                        "TrangThai VARCHAR(20) NOT NULL CHECK (TrangThai IN ('Sẵn sàng', 'Đang sử dụng', 'Bảo trì')))");

                // Bảng HOA_DON (thêm khóa ngoại)
                stmt.execute("CREATE TABLE IF NOT EXISTS HOA_DON (" +
                        "MaHD VARCHAR(10) PRIMARY KEY, " +
                        "MaKH VARCHAR(10) NOT NULL, " +
                        "MaNV INTEGER NOT NULL, " +
                        "MaMay INTEGER NOT NULL, " +
                        "Ngay DATE NOT NULL, " +
                        "KhuyenMai NUMERIC DEFAULT 0 CHECK (KhuyenMai >= 0 AND KhuyenMai <= 1), " +
                        "SoTien NUMERIC NOT NULL CHECK (SoTien >= 0), " +
                        "HinhThucThanhToan VARCHAR(20) NOT NULL CHECK (HinhThucThanhToan IN ('Tiền mặt', 'Chuyển khoản')), "
                        +
                        "FOREIGN KEY (MaKH) REFERENCES KHACH_HANG(MaKH) ON DELETE RESTRICT, " +
                        "FOREIGN KEY (MaNV) REFERENCES NHAN_VIEN(MaNV) ON DELETE RESTRICT, " +
                        "FOREIGN KEY (MaMay) REFERENCES BAN_MAY(MaMay) ON DELETE RESTRICT)");

                // Kiểm tra số dòng trong các bảng
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

    public void insert(String table, Object[] values) throws SQLException {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Mảng giá trị không được rỗng");
        }

        String sql;
        int expectedValueCount;

        if (table.equals("HOA_DON")) {
            sql = "INSERT INTO HOA_DON (MaHD, MaKH, MaNV, MaMay, Ngay, KhuyenMai, SoTien, HinhThucThanhToan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            expectedValueCount = 8;
        } else if (table.equals("CHI_TIET_HOA_DON")) {
            sql = "INSERT INTO CHI_TIET_HOA_DON (MaHD, MaSP, SoLuong, ThanhTien) VALUES (?, ?, ?, ?)";
            expectedValueCount = 4;
        } else if (table.equals("KHACH_HANG")) {
            sql = "INSERT INTO KHACH_HANG (MaKH, TenKH, SDT, Email, DiemTichLuy) VALUES (?, ?, ?, ?, ?)";
            expectedValueCount = 5;
        } else {
            int columnCount;
            try (ResultSet rs = connection.getMetaData().getColumns(null, null, table, null)) {
                columnCount = 0;
                while (rs.next()) {
                    columnCount++;
                }
            } catch (SQLException e) {
                throw new SQLException("Lỗi khi lấy số lượng cột của bảng " + table + ": " + e.getMessage());
            }

            if (values.length != columnCount) {
                throw new IllegalArgumentException("Số lượng giá trị (" + values.length
                        + ") không khớp với số lượng cột trong bảng " + table + " (" + columnCount + ")");
            }

            sql = "INSERT INTO " + table + " VALUES (" + String.join(",", Collections.nCopies(values.length, "?"))
                    + ")";
            expectedValueCount = columnCount;
        }

        if (values.length != expectedValueCount) {
            throw new IllegalArgumentException("Số lượng giá trị (" + values.length
                    + ") không khớp với số lượng cột trong câu lệnh SQL (" + expectedValueCount + ") cho bảng "
                    + table);
        }

        System.out.println("SQL: " + sql);
        System.out.println("Values: " + Arrays.toString(values));

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] == null) {
                    stmt.setNull(i + 1, java.sql.Types.NULL);
                } else {
                    stmt.setObject(i + 1, values[i]);
                }
            }
            stmt.executeUpdate();
        }
    }

    public synchronized void update(String table, String[] columns, Object[] values, String whereClause)
            throws SQLException {
        String setClause = String.join("=?, ", columns) + "=?";
        String sql = "UPDATE " + table + " SET " + setClause + " WHERE " + whereClause;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]);
                } else if (values[i] instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) values[i]);
                } else if (values[i] instanceof String) {
                    pstmt.setString(i + 1, (String) values[i]);
                } else if (values[i] == null) {
                    pstmt.setNull(i + 1, Types.NULL);
                }
            }
            pstmt.executeUpdate();
        }
    }

    public synchronized void update(String table, String[] columns, Object[] values, String whereClause,
            Object[] whereParams)
            throws SQLException {
        String setClause = String.join("=?, ", columns) + "=?";
        String sql = "UPDATE " + table + " SET " + setClause + " WHERE " + whereClause;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Gán giá trị cho các cột trong SET
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]);
                } else if (values[i] instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) values[i]);
                } else if (values[i] instanceof String) {
                    pstmt.setString(i + 1, (String) values[i]);
                } else if (values[i] == null) {
                    pstmt.setNull(i + 1, Types.NULL);
                }
            }
            // Gán giá trị cho các tham số trong WHERE
            for (int i = 0; i < whereParams.length; i++) {
                if (whereParams[i] instanceof Integer) {
                    pstmt.setInt(values.length + i + 1, (Integer) whereParams[i]);
                } else if (whereParams[i] instanceof Double) {
                    pstmt.setDouble(values.length + i + 1, (Double) whereParams[i]);
                } else if (whereParams[i] instanceof String) {
                    pstmt.setString(values.length + i + 1, (String) whereParams[i]);
                } else if (whereParams[i] == null) {
                    pstmt.setNull(values.length + i + 1, Types.NULL);
                }
            }
            pstmt.executeUpdate();
        }
    }

    public void delete(String table, String whereClause, Object[] whereValues) throws SQLException {
        String sql = "DELETE FROM " + table;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Gán giá trị cho các tham số trong whereClause (nếu có)
            if (whereValues != null) {
                for (int i = 0; i < whereValues.length; i++) {
                    if (whereValues[i] == null) {
                        stmt.setNull(i + 1, java.sql.Types.NULL);
                    } else {
                        stmt.setObject(i + 1, whereValues[i]);
                    }
                }
            }
            stmt.executeUpdate();
        }
    }

    public synchronized ResultSet select(String table, String[] columns, String whereClause) throws SQLException {
        String columnList = String.join(",", columns);
        String sql = "SELECT " + columnList + " FROM " + table + (whereClause.isEmpty() ? "" : " WHERE " + whereClause);
        System.out.println("Thực thi truy vấn: " + sql);
        PreparedStatement pstmt = connection.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        return rs;
    }

    public synchronized ResultSet select(String table, String[] columns, String whereClause, Object[] params)
            throws SQLException {
        String columnList = String.join(",", columns);
        String sql = "SELECT " + columnList + " FROM " + table + (whereClause.isEmpty() ? "" : " WHERE " + whereClause);
        System.out.println("Thực thi truy vấn: " + sql);
        PreparedStatement pstmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) params[i]);
            } else if (params[i] instanceof Double) {
                pstmt.setDouble(i + 1, (Double) params[i]);
            } else if (params[i] instanceof String) {
                pstmt.setString(i + 1, (String) params[i]);
            } else if (params[i] == null) {
                pstmt.setNull(i + 1, Types.NULL);
            }
        }
        ResultSet rs = pstmt.executeQuery();
        return rs;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}