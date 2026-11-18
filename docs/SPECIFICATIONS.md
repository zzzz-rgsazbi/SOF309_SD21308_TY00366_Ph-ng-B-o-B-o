# ĐẶC TẢ NGHIỆP VỤ HỆ THỐNG WEB ĐẶT ĐỒ ĂN TRỰC TUYẾN POB (SPECIFICATIONS)
> *Tài liệu đặc tả nghiệp vụ làm ngữ cảnh (Context) phục vụ kiểm thử tự động với JUnit 5, Selenium WebDriver và tích hợp CI/CD.*

---

## 1. THÔNG TIN DỰ ÁN
* **Tên đề tài:** Hệ thống Website Đặt Đồ Ăn Trực Tuyến - POB (Food Ordering System)
* **Sinh viên thực hiện:** Phùng Bảo Bảo
* **Mã sinh viên:** TY00366
* **Lớp:** SD21308
* **Môn học:** SOF309 - Kiểm thử nâng cao cùng AI
* **Công nghệ nền tảng:**
  * Backend: Java 17, Jakarta Servlet 6.0, Jakarta WebSocket, JDBC
  * Database: Microsoft SQL Server (`mssql-jdbc`)
  * Frontend: JSP, JSTL, HTML5, CSS3 (Theme Stitch/Dashboard), Leaflet Map (OpenStreetMap)
  * Server: Apache Tomcat 10+
  * Thư viện bổ trợ: BCrypt (mã hóa mật khẩu), JavaMail (gửi OTP), Apache POI (xuất Excel), iText (xuất PDF hóa đơn)
  * Framework kiểm thử: JUnit 5, Selenium WebDriver 4
* **URL chạy local:** `http://localhost:8080`

---

## 2. PHÂN QUYỀN HỆ THỐNG (ROLES & PERMISSIONS)
Hệ thống hỗ trợ 4 phân quyền chính thông qua Servlet Filter (`AuthFilter`):
1. **Super Admin (Role ID = 1):** Quản trị toàn hệ thống, duyệt yêu cầu mở quán của đối tác shop, kiểm duyệt nội dung món ăn & bình luận (lọc từ khóa cấm), quản lý voucher sàn, đối soát doanh thu, báo cáo vận hành và xem heatmap đơn hàng.
2. **Chủ quán / Shop Owner (Role ID = 2):** Quản lý hồ sơ quán (giờ mở/đóng cửa, tọa độ quán), danh mục sản phẩm, món ăn theo từng size (giá theo size, trạng thái hết hàng tạm thời), loại topping và danh mục topping liên kết, quản lý đơn hàng (`PENDING` $\rightarrow$ `CONFIRMED` $\rightarrow$ `READY_FOR_PICKUP`), bán hàng tại quầy (POS), xuất hóa đơn PDF và báo cáo Excel.
3. **Khách hàng / User (Role ID = 3):** Đăng ký/đăng nhập, tìm kiếm quán ăn và thực đơn, tùy chọn size và topping cho món ăn, quản lý giỏ hàng, đặt hàng và chọn tọa độ giao hàng trên bản đồ tương tác Leaflet, áp dụng mã giảm giá (Voucher), theo dõi hành trình đơn hàng và vị trí shipper realtime qua WebSocket, tích điểm đổi voucher, gửi khiếu nại đơn hàng.
4. **Tài xế / Shipper (Role ID = 4):** Đăng ký hồ sơ shipper, nhận đơn hàng (`READY_FOR_PICKUP` $\rightarrow$ `SHIPPING` $\rightarrow$ `DONE`), cập nhật vị trí GPS realtime qua WebSocket, định tuyến lộ trình giao hàng tối ưu OSRM trên bản đồ, quản lý ví và yêu cầu rút tiền.

---

## 3. QUY TẮC NGHIỆP VỤ CHI TIẾT (BUSINESS RULES)

### 3.1. Phân hệ Xác thực & Tài khoản (Authentication & Account)
* **Đăng ký tài khoản (`/dangky`):**
  * `username`: Bắt buộc, độ dài từ 5 đến 50 ký tự, không chứa ký tự đặc biệt nguy hiểm, không được trùng lặp trong hệ thống.
  * `email`: Bắt buộc, đúng định dạng RFC 5322 (ví dụ: `user@example.com`), duy nhất trong hệ thống.
  * `password`: Bắt buộc, độ dài tối thiểu 6 ký tự, mã hóa một chiều bằng thuật toán `BCrypt` trước khi lưu vào DB.
  * `confirmPassword`: Phải trùng khớp 100% với `password`.
  * `phone`: Bắt buộc, định dạng 10 chữ số chuẩn số điện thoại Việt Nam (bắt đầu bằng đầu số 03, 05, 07, 08, 09).
* **Đăng nhập (`/dangnhap`):**
  * Kiểm tra tài khoản và mật khẩu thông qua `BCrypt.checkpw`.
  * Nếu hợp lệ: Lưu session đối tượng `Account`, kiểm tra trạng thái hoạt động (`is_locked == false`), điều hướng người dùng theo role:
    * Role 1 $\rightarrow$ `/tong-quan` (Admin Portal)
    * Role 2 $\rightarrow$ `/shop` (Shop Dashboard)
    * Role 3 $\rightarrow$ `/` hoặc `/user/shop` (Trang chủ người dùng)
    * Role 4 $\rightarrow$ `/shipper/trangchucuashipper.jsp`
  * Nếu không hợp lệ: Trả về thông báo lỗi "Tài khoản hoặc mật khẩu không chính xác" và giữ lại tên đăng nhập trên form.
* **Xác thực bảo mật thông tin nhạy cảm (`/xac-thuc-thay-doi`):**
  * Khi tài khoản yêu cầu đổi email hoặc thay đổi thông tin số tài khoản ngân hàng, hệ thống gửi mã OTP 6 số qua email và yêu cầu xác thực trong vòng 5 phút trước khi ghi nhận thay đổi vào cơ sở dữ liệu.

### 3.2. Phân hệ Quản lý Món ăn & Topping (Shop Product & Topping CRUD)
* **Quản lý món ăn (`/shop/products`):**
  * Món ăn liên kết bắt buộc với một danh mục (`category_id`) thuộc về shop.
  * Tên món ăn không được để trống, độ dài từ 2 đến 100 ký tự.
  * Không áp dụng giá ở cấp món ăn; mỗi món ăn phải có ít nhất 1 kích cỡ (Size S/M/L) với giá bán tương ứng $> 0$ VNĐ.
  * Hỗ trợ cờ đánh dấu `outOfStock` (hết hàng tạm thời theo từng size) mà không cần xóa sản phẩm khỏi thực đơn.
* **Quản lý topping (`/shop/toppings`, `/shop/topping-categories`):**
  * Topping thuộc về một nhóm topping (`ToppingCategory`).
  * Một nhóm topping có thể áp dụng cho nhiều danh mục món ăn (quan hệ N-N qua bảng `ToppingCategory_ProductCategories`).
  * Giá topping phải $\ge 0$ VNĐ.

### 3.3. Phân hệ Giỏ hàng & Thanh toán (Cart & Checkout)
* **Giỏ hàng (`/cart`, `/cart-items`, `/user/add-to-cart`):**
  * Khách hàng thêm món ăn vào giỏ kèm lựa chọn size cụ thể và danh sách topping đính kèm.
  * Nếu shop hiện tại đang ngoài giờ hoạt động (`shop.isOpenNow() == false`), hệ thống từ chối thêm vào giỏ và thông báo "Quán hiện đang đóng cửa".
* **Đặt hàng & Thanh toán (`/checkout`):**
  * Khách hàng điền thông tin người nhận: Tên, Số điện thoại người nhận, Địa chỉ cụ thể.
  * Bản đồ Leaflet tích hợp Nominatim Search cho phép ghim chính xác tọa độ (`locationX` - Vĩ độ, `locationY` - Kinh độ).
  * Mã giảm giá (`Voucher`): Hệ thống tự động kiểm tra điều kiện giá trị đơn hàng tối thiểu, thời hạn voucher và tính toán mức chiết khấu hợp lệ (theo tỷ lệ % hoặc số tiền cố định hoặc miễn phí vận chuyển).
  * Khởi tạo đơn hàng với trạng thái ban đầu là `PENDING`.

### 3.4. Phân hệ Theo dõi đơn hàng & WebSocket GPS Realtime
* **Quy trình trạng thái đơn hàng (Order State Machine):**
  $$\text{PENDING} \longrightarrow \text{CONFIRMED} \longrightarrow \text{READY\_FOR\_PICKUP} \longrightarrow \text{SHIPPING} \longrightarrow \text{DONE}$$
* **WebSocket Realtime Tracking (`/ws/tracking`):**
  * Khi shipper di chuyển giao hàng (`SHIPPING`), thiết bị shipper gửi tọa độ GPS định kỳ lên WebSocket endpoint `TrackingEndpoint`.
  * Khách hàng theo dõi trên trang `/bill` hoặc `/user/donhang.jsp` được nhận tọa độ realtime hiển thị qua bản đồ 3 điểm (Quán ăn, Điểm giao, Vị trí shipper) kèm thời gian dự kiến đến (ETA).
