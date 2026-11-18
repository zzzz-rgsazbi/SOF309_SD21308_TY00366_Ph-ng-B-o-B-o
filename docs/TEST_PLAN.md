# KẾ HOẠCH KIỂM THỬ TỰ ĐỘNG (TEST PLAN) - HỆ THỐNG POB

* **Tên dự án:** Hệ thống Website Đặt Đồ Ăn Trực Tuyến - POB
* **Sinh viên thực hiện:** Phùng Bảo Bảo (MSSV: TY00366)
* **Lớp:** SD21308
* **Giảng viên hướng dẫn:** ThS. Trần Quang Bình (binhtq7 - Thầy Mười Giáo)
* **Môn học:** SOF309 - Kiểm thử nâng cao cùng AI

---

## 1. MỤC TIÊU KIỂM THỬ (TEST OBJECTIVES)
1. **Kiểm thử Unit Test (JUnit 5):**
   * Kiểm tra tính đúng đắn của logic xử lý nghiệp vụ tại Controller/Service/DAO.
   * Áp dụng kỹ thuật kiểm thử tham số hóa `@ParameterizedTest` kết hợp `@CsvSource` để bao phủ miền giá trị biên của form đăng ký, đăng nhập và validation dữ liệu.
   * Áp dụng kỹ thuật kiểm thử ngoại lệ `assertThrows()` để bắt chính xác các lỗi nghiệp vụ (trùng username/email, tài khoản bị khóa, vi phạm ràng buộc dữ liệu).
   * Áp dụng `@Timeout` nhằm phát hiện các nguy cơ treo truy vấn hoặc lặp vô tận.
2. **Kiểm thử giao diện người dùng tự động (Selenium WebDriver 4):**
   * Xây dựng kiến trúc Page Object Model (POM) tách biệt Locator và Test Logic.
   * Tự động hóa các luồng người dùng cốt lõi: Đăng ký tài khoản, Đăng nhập hệ thống, Thêm món vào giỏ hàng và Đặt hàng.
   * Sử dụng `WebDriverWait` (Explicit Wait) để xử lý các hành vi bất đồng bộ của DOM, tuyệt đối không dùng `Thread.sleep()`.
3. **Kiểm thử tích hợp liên tục (CI/CD với GitHub Actions):**
   * Tự động hóa quá trình build và chạy test trên máy ảo Ubuntu (`ubuntu-latest`).
   * Hỗ trợ thực thi kịch bản Selenium ở chế độ Chrome Headless (`--headless=new`).
   * Tự động đóng gói và lưu trữ báo cáo kiểm thử tại tab Artifacts.

---

## 2. PHẠM VI KIỂM THỬ (TEST SCOPE)

### 2.1. Trong phạm vi kiểm thử (In-Scope)
* **Module Authentication:** Đăng nhập, Đăng ký, Đổi mật khẩu, Xác thực OTP.
* **Module Shop & Product Management:** Thêm/sửa món ăn, định cấu hình kích thước và giá bán, quản lý topping.
* **Module Cart & Checkout:** Thêm món ăn vào giỏ, kiểm tra giờ hoạt động của quán (`isOpenNow`), áp dụng voucher giảm giá, chọn địa chỉ và vị trí trên bản đồ.
* **Module Order Management:** Chuyển đổi trạng thái đơn hàng từ `PENDING` đến `DONE`.

### 2.2. Ngoài phạm vi kiểm thử (Out-of-Scope)
* Kiểm thử tải trọng cao và hiệu năng đồng thời quy mô lớn (Stress/Load Test > 10.000 users).
* Tích hợp cổng thanh toán thực tế của bên thứ ba ở môi trường Production.

---

## 3. MÔI TRƯỜNG & CÔNG CỤ KIỂM THỬ (TEST ENVIRONMENT & TOOLS)
* **Hệ điều hành kiểm thử:** Windows 11 (Local) / Ubuntu Linux (GitHub Actions CI/CD)
* **Ngôn ngữ:** Java (JDK 17+)
* **Build Tool:** Apache Maven 3.9+
* **Framework:** JUnit 5 (Jupiter 5.10.2), Selenium WebDriver 4.20.0
* **Trình duyệt kiểm thử:** Google Chrome (v120+), ChromeDriver
* **CI/CD Platform:** GitHub Actions

---

## 4. TIÊU CHÍ NGHIỆM THU (EXIT CRITERIA)
* Tỷ lệ thực thi Unit Test trên Controller/Service đạt **100% Pass** (`mvn test` báo `BUILD SUCCESS`).
* Không phát hiện lỗi nghiêm trọng (Blocker/Critical) trong luồng đăng nhập, đăng ký và đặt hàng.
* Pipeline GitHub Actions (`.github/workflows/test.yml`) chạy hoàn toàn thành công và sáng đèn xanh.
