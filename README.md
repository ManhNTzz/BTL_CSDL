# Quản lý Ký túc xá (BTL Cơ sở dữ liệu)

Hệ thống Quản lý Ký túc xá được xây dựng với kiến trúc hiện đại:
- **Backend:** Spring Boot (Java 17), Spring Data JPA, Hibernate, MySQL Connector.
- **Frontend:** React, Vite, giao diện chuẩn Tiếng Việt.
- **Database:** MySQL 8.0, đầy đủ ràng buộc toàn vẹn, Trigger, Procedure và View thống kê báo cáo.

---

## 🚀 Hướng dẫn cài đặt và chạy thử

### 1. Yêu cầu môi trường
- **Java**: JDK 17 trở lên.
- **MySQL**: MySQL 8.0 trở lên (đang chạy ở cổng 3306).
- *(Không bắt buộc cài Maven hay Node.js/npm vì dự án đã tích hợp sẵn Maven Wrapper và giao diện web pre-built).*

### 2. Tạo Cơ sở dữ liệu
1. Mở file `database/01_tao_csdl_moi.sql` trong **MySQL Workbench** (hoặc DBeaver / Navicat).
2. Chạy toàn bộ file (biểu tượng tia sét) để tạo database `QuanLyKTX`, cấu trúc bảng và dữ liệu mẫu đầy đủ.

### 3. Đóng gói ứng dụng (Build JAR)
- Nhấp đúp file **`Build_App.bat`** (chỉ cần chạy một lần khi mới tải từ GitHub về).
- Script sẽ tự động dùng Maven Wrapper đóng gói ứng dụng thành file thực thi `backend/target/QuanLyKTX.jar`.

### 4. Khởi động ứng dụng
1. Nhấp đúp file **`Chay_App.bat`**.
2. Ở lần đầu tiên chạy, chương trình sẽ tự tạo và mở file cấu hình `config/application-local.properties` bằng Notepad:
   - Điền mật khẩu MySQL của bạn vào dòng `spring.datasource.password=...`
   - Lưu file (`Ctrl + S`) và đóng Notepad.
3. Chạy lại **`Chay_App.bat`**. Ứng dụng sẽ tự động kết nối CSDL và mở giao diện quản lý trên trình duyệt (Microsoft Edge / Google Chrome).

### 5. Tắt ứng dụng
- Khi dùng xong, nhấp đúp file **`Tat_App.bat`** để dừng an toàn dịch vụ backend.

---

## 📁 Cấu trúc thư mục dự án
- `backend/`: Mã nguồn Spring Boot, REST APIs, các service xử lý nghiệp vụ, kiểm tra ràng buộc toàn vẹn và tích hợp giao diện web.
- `database/`: File SQL khởi tạo cơ sở dữ liệu, các bảng và dữ liệu mẫu (`01_tao_csdl_moi.sql`).
- `frontend/`: Mã nguồn giao diện người dùng React, Vite, hệ thống component chuẩn Tiếng Việt.
- `config/`: Chứa file mẫu cấu hình kết nối CSDL cá nhân (`application-local.example.properties`).
- `scripts/`: Các PowerShell script tự động hóa khởi động, tắt và đóng gói ứng dụng.
- `Chay_App.bat`, `Tat_App.bat`, `Build_App.bat`: Các phím tắt tiện ích 1-click trên Windows.
