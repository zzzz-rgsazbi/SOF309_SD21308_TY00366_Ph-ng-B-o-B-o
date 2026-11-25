# NHẬT KÝ MINH CHỨNG TƯƠNG TÁC CÙNG AI (AI LOG 02)
## THIẾT KẾ VÀ SINH MÃ NGUỒN UNIT TEST CHO CONTROLLER VỚI JUNIT 5 NÂNG CAO (Y3)

* **Môn học:** SOF309 - Kiểm thử nâng cao cùng AI
* **Sinh viên thực hiện:** Phùng Bảo Bảo (MSSV: TY00366)
* **Lớp:** SD21308
* **Ngày thực hiện:** 25/11/2026
* **Mục tiêu:** Áp dụng AI để tự động hóa sinh bộ mã nguồn Unit Test đạt chuẩn JUnit 5 cho các Controller của hệ thống Web Đặt Đồ Ăn Trực Tuyến POB, đảm bảo áp dụng đầy đủ Parameterized Testing (`@ParameterizedTest` + `@CsvSource`), Exception Testing (`assertThrows()`) và Timeout Testing (`@Timeout`).

---

### 1. Câu lệnh Prompt gửi cho AI (Prompt Engineering)

```text
Bạn là một Chuyên gia Kỹ thuật Kiểm thử Phần mềm (Automation QA Engineer). 
Dưới đây là bộ Test Case chi tiết từ file docs/TEST_CASES.md và đặc tả nghiệp vụ docs/SPECIFICATIONS.md của dự án Web POB (Java Servlet/JSP, BCrypt):

Hãy viết các class Unit Test bằng JUnit 5 trong thư mục src/test/java/com/poly/sof309/controllers/ bao gồm:
1. AuthControllerTest:
   - Sử dụng @ParameterizedTest + @CsvSource để kiểm tra các giá trị biên của Form Đăng ký:
     + Username: độ dài 3 đến 30 ký tự (regex ^[a-zA-Z0-9_]{3,30}$), không chứa ký tự đặc biệt, không khoảng trắng.
     + Password: độ dài 8 đến 16 ký tự, không chứa khoảng trắng.
     + Confirm password: phải khớp 100% với password.
     + Số điện thoại: kiểm tra định dạng regex và độ dài tối đa 20 ký tự (InputValidationUtil).
   - Kiểm tra điều hướng chính xác theo 4 quyền (Role 1: /tong-quan, Role 2: /shop, Role 3: /user/home, Role 4: /shipper/donhang).
   - Sử dụng assertThrows() kiểm thử các ngoại lệ nghiệp vụ: trùng Username, trùng Email, tài khoản bị khoá (BLOCKED), tài khoản bị đình chỉ (isDeleted).
   - Sử dụng @Timeout(2s) kiểm tra hiệu năng mã hóa BCrypt.

2. ProductControllerTest:
   - Sử dụng @ParameterizedTest + @CsvSource kiểm tra các trường món ăn: tên món không rỗng, giá bán > 0, số lượng tồn >= 0, shopId > 0, categoryId > 0.
   - Sử dụng assertThrows() kiểm tra ném ngoại lệ khi thêm món giá âm, tên rỗng, cập nhật sản phẩm không tồn tại và xoá sản phẩm đang có đơn hàng xử lý.

3. CategoryControllerTest:
   - Kiểm tra validation tên danh mục và shopId qua @ParameterizedTest.
   - Kiểm tra ngoại lệ assertThrows() khi xoá danh mục đang chứa sản phẩm.

Yêu cầu: Mã nguồn sạch, chú thích rõ ràng bằng tiếng Việt, độc lập không phụ thuộc DB sống và đạt 100% Pass khi chạy với lệnh 'mvn test'.
```

---

### 2. Kết quả AI sinh (AI Output Summary)
AI đã phân tích và sinh ra 3 class kiểm thử hoàn chỉnh:
1. `AuthControllerTest.java`:
   - 14 bộ dữ liệu kiểm tra biên cho username và password trong `@CsvSource`.
   - 6 bộ dữ liệu kiểm tra biên số điện thoại.
   - 5 kịch bản phân quyền điều hướng ứng với các Role ID.
   - 5 ca kiểm thử ngoại lệ nghiệp vụ bằng `assertThrows()`: phát hiện trùng lặp tài khoản, chặn tài khoản bị đình chỉ/khoá vi phạm.
   - 1 ca kiểm thử `@Timeout` cho thuật toán BCrypt.
2. `ProductControllerTest.java`:
   - 14 bộ dữ liệu kiểm tra giá trị biên cho giá món ăn, tồn kho, tên món, mã quán và mã loại.
   - 4 ca kiểm thử ngoại lệ `assertThrows()` bắt lỗi nghiệp vụ món ăn.
3. `CategoryControllerTest.java`:
   - 7 bộ dữ liệu kiểm tra danh mục món ăn.
   - 4 ca kiểm thử ngoại lệ `assertThrows()`.

---

### 3. Tự đánh giá & Tinh chỉnh của Sinh viên (Human Review & Refinement)
* **Đánh giá ban đầu:** Mã nguồn do AI sinh tuân thủ đúng cấu trúc JUnit 5 Jupiter, chia tách rõ ràng giữa Test Data (`@CsvSource`) và Assertion (`assertEquals`, `assertTrue`).
* **Các điểm sinh viên đã tự tinh chỉnh lại:**
  - Đồng bộ hoá các thông điệp lỗi chính xác với logic của `DangKyServlet`, `DangNhapServlet`, `ProductServlet`, `CategoryServlet` và `InputValidationUtil`.
  - Bổ sung cấu trúc Service Mock trong các class test để mô phỏng chính xác hành vi của tầng nghiệp vụ Controller, giúp bộ test chạy siêu nhanh và hoàn toàn độc lập với việc kết nối cơ sở dữ liệu thật.
