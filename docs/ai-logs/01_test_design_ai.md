# NHẬT KÝ MINH CHỨNG TƯƠNG TÁC CÙNG AI (AI LOG 01)
## THIẾT KẾ TEST PLAN VÀ BỘ TEST CASE BẰNG KỸ THUẬT AI RAG

* **Môn học:** SOF309 - Kiểm thử nâng cao cùng AI
* **Sinh viên:** Phùng Bảo Bảo (MSSV: TY00366)
* **Lớp:** SD21308
* **Ngày thực hiện:** 18/11/2026
* **Mục tiêu:** Áp dụng AI RAG để sinh Kế hoạch kiểm thử (Test Plan) và Bộ kịch bản kiểm thử (Test Cases) chi tiết cho hệ thống Web đặt đồ ăn trực tuyến POB.

---

### 1. Câu lệnh Prompt gửi cho AI (Prompt Context & Engineering)

```text
Bạn là một Chuyên gia Kiểm thử phần mềm (QA Lead). Dưới đây là tài liệu đặc tả nghiệp vụ chi tiết của dự án Web Đặt Đồ Ăn Trực Tuyến - POB của tôi (xây dựng bằng Java Servlet/JSP, SQL Server, Tomcat):

[Dán toàn bộ nội dung file docs/SPECIFICATIONS.md]

Dựa trên tài liệu đặc tả nghiệp vụ này, hãy:
1. Áp dụng kỹ thuật Phân vùng tương đương (Equivalence Partitioning) và Phân tích giá trị biên (Boundary Value Analysis) để thiết kế Test Plan toàn diện cho hệ thống.
2. Xây dựng Bộ kịch bản Test Case chi tiết bao gồm cả ca kiểm thử tích cực (Positive) và tiêu cực (Negative/Exception) cho:
   - Module Xác thực tài khoản (Đăng nhập, Đăng ký, Đổi thông tin).
   - Module Quản lý Món ăn & Topping của Shop (CRUD, hết hàng tạm thời theo size).
   - Module Giỏ hàng & Thanh toán (Kiểm tra giờ mở cửa của quán, chọn tọa độ Leaflet, tính mã giảm giá voucher).
   - Module Theo dõi đơn hàng & WebSocket GPS Realtime.
3. Xuất kết quả dưới định dạng bảng chuẩn Markdown và nêu rõ kỹ thuật kiểm thử áp dụng cho từng ca.
```

---

### 2. Kết quả AI sinh (AI Output Summary)
* AI đã phân tích tài liệu đặc tả và sinh ra:
  * File `docs/TEST_PLAN.md`: Xác định rõ mục tiêu, phạm vi in-scope / out-of-scope, môi trường thực thi và tiêu chí nghiệm thu.
  * File `docs/TEST_CASES.md`: Bộ kiểm thử gồm các ca kiểm thử:
    * `TC_AUTH_01` $\rightarrow$ `TC_AUTH_06`: Kiểm tra đăng nhập, đăng ký và ràng buộc mật khẩu BCrypt.
    * `TC_PROD_01` $\rightarrow$ `TC_PROD_04`: Kiểm tra nghiệp vụ thêm món ăn theo kích cỡ và trạng thái `outOfStock`.
    * `TC_ORDER_01` $\rightarrow$ `TC_ORDER_04`: Kiểm tra kiểm soát giờ đóng cửa của shop, tọa độ Leaflet và truyền nhận GPS realtime qua WebSocket.

---

### 3. Tự đánh giá & Tinh chỉnh của Sinh viên (Human Review & Refinement)
* **Đánh giá ban đầu:** AI sinh cấu trúc kịch bản mạch lạc, bao phủ tốt các giá trị biên như độ dài ký tự username, giá sản phẩm âm và trường hợp ngoại lệ email trùng lặp.
* **Các điểm sinh viên đã tự tinh chỉnh lại:**
  * Bổ sung trường hợp đặc thù của dự án: Giá món ăn không đặt ở cấp sản phẩm mà tính theo từng size (Size S/M/L) và topping áp dụng đa danh mục.
  * Bổ sung kiểm tra nghiệp vụ chặn đặt hàng khi quán đóng cửa (`shop.isOpenNow() == false`).
  * Chuẩn hóa bảng Test Case với đầy đủ mã ID, loại test (Positive/Negative), các bước thực hiện và dữ liệu kiểm thử thực tế.
