# NHẬT KÝ MINH CHỨNG TƯƠNG TÁC CÙNG AI (AI LOG 01)
## THIẾT KẾ TEST PLAN VÀ BỘ TEST CASE BẰNG KỸ THUẬT AI RAG

* **Môn học:** SOF309 - Kiểm thử nâng cao cùng AI
* **Sinh viên thực hiện:** Phùng Bảo Bảo (MSSV: TY00366)
* **Lớp:** SD21308
* **Ngày thực hiện:** 18/11/2026 (Cập nhật phiên bản hoàn thiện Y2)
* **Mục tiêu:** Áp dụng kỹ thuật AI RAG (Retrieval-Augmented Generation) kết hợp tài liệu đặc tả nghiệp vụ chi tiết của dự án Web Đặt Đồ Ăn Trực Tuyến POB để sinh Kế hoạch kiểm thử (Test Plan) và Bộ kịch bản kiểm thử (Test Cases) đạt chuẩn công nghiệp.

---

### 1. Câu lệnh Prompt gửi cho AI (Prompt Engineering & Context Injection)

```text
Bạn là một Chuyên gia Kiểm thử phần mềm (QA Lead) giàu kinh nghiệm. Dưới đây là toàn bộ tài liệu đặc tả nghiệp vụ chi tiết (Context) của dự án Web Đặt Đồ Ăn Trực Tuyến - POB của tôi (xây dựng bằng Java Servlet/JSP, SQL Server, Tomcat, BCrypt, WebSocket):

[Dán toàn bộ nội dung file docs/SPECIFICATIONS.md]

Dựa trên ngữ cảnh nghiệp vụ thực tế này, hãy thực hiện các yêu cầu sau:
1. Áp dụng kỹ thuật Phân tích giá trị biên (Boundary Value Analysis - BVA) và Phân vùng tương đương (Equivalence Partitioning - EP) để lập Kế hoạch kiểm thử (Test Plan) toàn diện cho dự án.
2. Xây dựng Bộ kịch bản Test Case chi tiết (đánh số ID theo chuẩn TC_AUTH, TC_CAT, TC_PROD, TC_ORD) gồm đầy đủ:
   - Ca kiểm thử tích cực (Positive Cases)
   - Ca kiểm thử tiêu cực quét qua các giá trị biên (Negative Boundary Cases: độ dài username 3–30, mật khẩu 8–16, giá món ăn > 0, tồn kho >= 0, sđt regex...)
   - Ca kiểm thử ngoại lệ bắt lỗi nghiệp vụ (Exception Testing: email/username trùng lặp, tài khoản bị khoá, ngoài giờ mở cửa của quán...)
3. Xuất kết quả dưới định dạng bảng Markdown chuẩn, chỉ rõ Kỹ thuật kiểm thử áp dụng cho từng ca để chuẩn bị làm đầu vào cho việc tự động hoá bằng JUnit 5 ParameterizedTest và Selenium WebDriver.
```

---

### 2. Kết quả AI sinh (AI Output Summary)
* AI đã phân tích tài liệu đặc tả và sinh ra:
  * File `docs/TEST_PLAN.md`: Xác định rõ mục tiêu kiểm thử, phạm vi in-scope / out-of-scope, chiến lược kiểm thử kết hợp Parameterized Testing và Exception Testing, môi trường thực thi và tiêu chí nghiệm thu 100% Pass.
  * File `docs/TEST_CASES.md`: Bộ kiểm thử gồm hơn 30 ca kiểm thử bao phủ toàn diện:
    * `TC_AUTH_01` $\rightarrow$ `TC_AUTH_16`: Kiểm tra đăng nhập theo 4 phân quyền (Admin, Shop, User, Shipper), rate-limit brute-force, xử lý tài khoản `BLOCKED`/`isDeleted`, kiểm tra validation biên của username, password, confirm password, định dạng số điện thoại và cấp phát OTP 6 số an toàn.
    * `TC_CAT_01` $\rightarrow$ `TC_CAT_03`: Kiểm tra ràng buộc danh mục món ăn (`shopId > 0`, `categoryName` không rỗng).
    * `TC_PROD_01` $\rightarrow$ `TC_PROD_08`: Kiểm tra ràng buộc thêm/sửa món ăn (tên món, giá bán $> 0$, tồn kho $\ge 0$, liên kết category hợp lệ, trạng thái `outOfStock` theo size).
    * `TC_ORD_01` $\rightarrow$ `TC_ORD_07`: Kiểm tra chặn đặt hàng khi shop đóng cửa (`isOpenNow`), áp dụng voucher và tính tiền chiết khấu, Order State Machine và truyền nhận GPS realtime qua WebSocket `/ws/tracking`.

---

### 3. Tự đánh giá & Tinh chỉnh của Sinh viên (Human Review & Refinement)
* **Đánh giá ban đầu:** AI sinh cấu trúc kịch bản mạch lạc, các ca kiểm thử bám sát quy tắc nghiệp vụ thực tế của dự án POB.
* **Các điểm sinh viên đã tự tinh chỉnh lại:**
  * Bổ sung quy tắc đặc thù của `DangKyServlet`: Mật khẩu không chỉ giới hạn từ 8-16 ký tự mà còn bị cấm chứa khoảng trắng.
  * Bổ sung quy tắc kiểm tra điều hướng chính xác theo 4 quyền khác nhau tại `DangNhapServlet` (Super Admin vào `/tong-quan`, Shop vào `/shop`, User vào `/user/home`, Shipper vào `/shipper/donhang`).
  * Bổ sung kiểm tra ngoại lệ cập nhật sản phẩm với ID không tồn tại để phục vụ bài test ngoại lệ `assertThrows()` cho Y3.
