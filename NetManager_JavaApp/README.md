## Dự án Java Desk App - Quản lý quán net

CustomerPanel.java / Thông tin khách hàng
DatabaseManager.java /File liên kết DB
InventoryPanel.java / Tính năng kho hàng
LoginPanel.java / Trang đăng nhập
MainFrame.java / Trang chủ - chứ Main
ManageComputer.java / Quản lý máy bàn
OrderPanel.java / Đơn đặt món
PromotionsPanel.java / Khuyến mãi
SearchPanel.java / 
ServiceMenuPanel.java / Thực đơn
StaffPanel.java / Thông tin nhân viên
StatisticsPanel.java / Báo cáo số liệu 


đang sửa để đặt món nối với khách hàng và máy bàn

# Cấu trúc dự án
NetManager_JavaApp/
├── src/                    # Thư mục chứa mã nguồn Java
│   ├── views/                # Thưu mục Giao diện 
│   │   ├── Các file giao diện
│   ├── logic/             # Thư mục Logic(Back-end)
│   │   ├── Các file xử lý logic
│   ├── database/          # Truy cập cơ sở dữ liệu 
│   │   ├── DatabaseConnection.java  # Kết nối SQLite
│   └── Main.java          # File chính chứa Main để chạy ứng dụng
|
├── resources/              # Tài nguyên tĩnh (ảnh, v.v.)
│   ├── images/            # Thư mục chứa ảnh
│   │  
│   ├── db/                # Thư mục chứa file database
│   │   └── myapp.db       # File SQLite
└── README.md              # Mô tả dự án và cách chạy
