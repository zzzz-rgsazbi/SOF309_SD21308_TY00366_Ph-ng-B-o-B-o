# KẾ HOẠCH KIỂM THỬ TỰ ĐỘNG (TEST PLAN) - HỆ THỐNG POB

* **Tên dự án:** Hệ thống Website Đặt Đồ Ăn Trực Tuyến - POB
* **Sinh viên thực hiện:** Phùng Bảo Bảo (MSSV: TY00366)
* **Lớp:** SD21308
* **Giảng viên hướng dẫn:** ThS. Trần Quang Bình (binhtq7 - Thầy Mười Giáo)
* **Môn học:** SOF309 - Kiểm thử nâng cao cùng AI
* **Phiên bản tài liệu:** 2.0 (Cập nhật hoàn thiện Y2)

---

## 1. MỤC TIÊU KIỂM THỬ (TEST OBJECTIVES)
1. **Kiểm thử đơn vị (Unit Testing với JUnit 5 nâng cao):**
   * Đảm bảo tính chính xác của toàn bộ quy tắc nghiệp vụ và ràng buộc xác thực dữ liệu tại Controller, Service và DAO.
   * Áp dụng kỹ thuật kiểm thử tham số hóa `@ParameterizedTest` kết hợp `@CsvSource` để bao phủ tối đa miền tương đương và các giá trị biên (Boundary Value Analysis) của các form nhập liệu: Đăng ký, Đăng nhập, Thêm/Sửa Món ăn, Thêm/Sửa Danh mục.
   * Áp dụng kỹ thuật Exception Testing với `assertThrows()` để thẩm định khả năng phát hiện và chặn đứng các hành vi vi phạm nghiệp vụ: tài khoản trùng lặp, tài khoản bị đình chỉ, truy vấn dữ liệu không hợp lệ.
   * Áp dụng `@Timeout` nhằm phát hiện và phòng chống các lỗi vòng lặp vô hạn hoặc truy vấn treo.
2. **Kiểm thử giao diện tự động (UI Automation Testing với Selenium WebDriver 4 POM):**
   * Thiết kế kiến trúc kiểm thử hướng đối tượng chuẩn Page Object Model (POM) tách bạch hoàn toàn giữa việc quản lý phần tử giao diện (`Locator`) và kịch bản thực thi kiểm thử (`Test Script`).
   * Tự động hóa các luồng nghiệp vụ end-to-end then chốt: Đăng nhập hợp lệ và không hợp lệ, Đăng ký tài khoản, Quản lý CRUD Món ăn / Danh mục.
   * Áp dụng triệt để cơ chế chờ thông minh `WebDriverWait` (Explicit Wait), loại bỏ hoàn toàn `Thread.sleep()`.
3. **Kiểm thử tích hợp liên tục (CI/CD với GitHub Actions):**
   * Tự động kích hoạt toàn bộ quy trình build và test mỗi khi có thao tác `push` hoặc `pull_request`.
   * Hỗ trợ thực thi Selenium ở chế độ không đầu (Headless mode: `--headless=new`).
   * Tự động lưu trữ báo cáo kiểm thử Surefire Reports và ảnh chụp màn hình khi thất bại dưới dạng Artifacts trên GitHub.

---

## 2. PHẠM VI KIỂM THỬ (TEST SCOPE)

### 2.1. Trong phạm vi kiểm thử (In-Scope)
* **Phân hệ 1 - Xác thực & Tài khoản (Authentication & Account):**
  * `DangNhapServlet`: Đăng nhập đúng/sai mật khẩu, rate-limit brute-force, chuyển hướng theo 4 quyền (Super Admin, Shop, User, Shipper), xử lý tài khoản bị khóa (`BLOCKED`) hoặc đình chỉ (`isDeleted`).
  * `DangKyServlet`: Kiểm tra validation độ dài username (3-30 ký tự, regex `^[a-zA-Z0-9_]{3,30}$`), password (8-16 ký tự, không chứa khoảng trắng), confirm password trùng khớp, validation số điện thoại và họ tên, kiểm tra trùng lặp email/username, quy trình cấp phát OTP 6 số.
  * `InputValidationUtil`: Kiểm thử giá trị biên độ dài chuỗi một dòng, nhiều dòng, số điện thoại.
* **Phân hệ 2 - Quản lý Danh mục & Món ăn (Shop Product & Category CRUD):**
  * `CategoryServlet`: Thêm mới danh mục, cập nhật danh mục, kiểm tra `shopId > 0` và `categoryName` không rỗng.
  * `ProductServlet` & `ShopProductServlet`: Tạo và chỉnh sửa món ăn, kiểm tra giá `price > 0`, số lượng tồn kho `stockQuantity >= 0`, phân loại size và cờ `outOfStock`.
* **Phân hệ 3 - Giỏ hàng, Voucher & Đặt hàng (Cart & Order):**
  * Kiểm soát trạng thái mở cửa của quán (`shop.isOpenNow()`).
  * Tính toán mức chiết khấu của voucher hợp lệ / hết hạn / không đủ giá trị đơn tối thiểu.
  * Xử lý chuyển đổi trạng thái đơn hàng trong `Order State Machine`.

### 2.2. Ngoài phạm vi kiểm thử (Out-of-Scope)
* Kiểm thử chịu tải cao (Load/Stress Testing trên 10.000 người dùng đồng thời).
* Giao dịch thực tế qua cổng ngân hàng/PayOS thực ở môi trường Production.

---

## 3. CHIẾN LƯỢC & KỸ THUẬT KIỂM THỬ (TEST STRATEGY & TECHNIQUES)

| Phân hệ / Thành phần | Kỹ thuật kiểm thử áp dụng | Công cụ & Framework | Mục tiêu độ phủ |
| :--- | :--- | :--- | :---: |
| **Form Đăng ký / Đăng nhập** | Phân vùng tương đương (EP)<br>Phân tích giá trị biên (BVA) | JUnit 5 (`@ParameterizedTest`, `@CsvSource`) | 100% các giá trị biên |
| **Logic nghiệp vụ & Ràng buộc** | Kiểm thử ngoại lệ (Exception Testing) | JUnit 5 (`assertThrows()`) | 100% các nhánh lỗi nghiệp vụ |
| **Giao diện Web (UI)** | Kiểm thử chức năng tự động (Functional UI Testing) | Selenium WebDriver 4, Page Object Model (POM), WebDriverWait | Toàn bộ luồng Login & CRUD |
| **Hệ thống CI/CD** | Tự động hóa kiểm thử liên tục (Continuous Testing) | GitHub Actions (`ubuntu-latest`, Chrome Headless) | Toàn bộ commit & PR |

---

## 4. MÔI TRƯỜNG & CÔNG CỤ (TEST ENVIRONMENT & SPECIFICATIONS)
* **Hệ điều hành:** Windows 11 (Môi trường Local Dev/Test), Ubuntu 22.04 LTS (GitHub Actions CI/CD Runner).
* **JDK:** OpenJDK 17 / JetBrains JBR 17+.
* **Build Tool:** Apache Maven 3.9+.
* **Testing Libraries:**
  * JUnit Jupiter 5.13.4 (`junit-jupiter-api`, `junit-jupiter-params`, `junit-jupiter-engine`).
  * Selenium WebDriver 4.20.0 (`selenium-java`, ChromeDriver).
* **Web Server:** Apache Tomcat 10.1+.
* **Database Engine:** Microsoft SQL Server 2019/2022.

---

## 5. TIÊU CHÍ NGHIỆM THU (EXIT CRITERIA)
1. **Tiêu chí thực thi Unit Test:** 100% các kịch bản Unit Test cho Controller và Service chạy Pass (`BUILD SUCCESS`), không có test case nào bị `Error` hoặc `Failure`.
2. **Tiêu chí thực thi UI Test:** Kịch bản Selenium mở Chrome, tương tác các phần tử qua `WebDriverWait` và assert URL/thông báo giao diện thành công.
3. **Tiêu chí CI/CD:** Toàn bộ pipeline trên tab GitHub Actions hoàn thành với trạng thái xanh lá (Success).
