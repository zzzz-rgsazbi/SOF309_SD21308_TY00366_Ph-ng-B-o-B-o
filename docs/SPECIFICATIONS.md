# ĐẶC TẢ NGHIỆP VỤ HỆ THỐNG WEB ĐẶT ĐỒ ĂN TRỰC TUYẾN POB (SPECIFICATIONS)
> *Tài liệu đặc tả nghiệp vụ làm ngữ cảnh (Context) phục vụ kiểm thử tự động với JUnit 5, Selenium WebDriver và tích hợp CI/CD.*

---

## 1. THÔNG TIN DỰ ÁN
* **Tên đề tài:** Hệ thống Website Đặt Đồ Ăn Trực Tuyến - POB (Food Ordering System)
* **Sinh viên thực hiện:** Phùng Bảo Bảo
* **Mã sinh viên:** TY00366
* **Lớp:** SD21308
* **Môn học:** SOF309 - Kiểm thử nâng cao cùng AI
* **Giảng viên hướng dẫn:** ThS. Trần Quang Bình (binhtq7 - Thầy Mười Giáo)
* **Công nghệ nền tảng:**
  * Backend: Java 17, Jakarta Servlet 6.0, Jakarta WebSocket, JDBC
  * Database: Microsoft SQL Server (`mssql-jdbc`)
  * Frontend: JSP, JSTL, HTML5, CSS3 (Theme Stitch/Dashboard), Leaflet Map (OpenStreetMap)
  * Server: Apache Tomcat 10+
  * Thư viện bổ trợ: BCrypt (mã hóa mật khẩu), JavaMail (gửi OTP), Apache POI (xuất Excel), iText (xuất PDF hóa đơn)
  * Framework kiểm thử: JUnit 5 (Jupiter 5.13.4), Selenium WebDriver 4
* **URL chạy local:** `http://localhost:8080`

---

## 2. PHÂN QUYỀN HỆ THỐNG (ROLES & PERMISSIONS)
Hệ thống hỗ trợ 4 phân quyền chính thông qua Servlet Filter (`AuthFilter`):
1. **Super Admin (Role ID = 1):** Quản trị toàn hệ thống, duyệt yêu cầu mở quán của đối tác shop, kiểm duyệt nội dung món ăn & bình luận (lọc từ khóa cấm), quản lý voucher sàn, đối soát doanh thu, báo cáo vận hành và xem heatmap đơn hàng. URL điều hướng: `/tong-quan`.
2. **Chủ quán / Shop Owner (Role ID = 2):** Quản lý hồ sơ quán (giờ mở/đóng cửa, tọa độ quán), danh mục sản phẩm, món ăn theo từng size (giá theo size, trạng thái hết hàng tạm thời), loại topping và danh mục topping liên kết, quản lý đơn hàng (`PENDING` $\rightarrow$ `CONFIRMED` $\rightarrow$ `READY_FOR_PICKUP`), bán hàng tại quầy (POS), xuất hóa đơn PDF và báo cáo Excel. URL điều hướng: `/shop`.
3. **Khách hàng / User (Role ID = 3):** Đăng ký/đăng nhập, tìm kiếm quán ăn và thực đơn, tùy chọn size và topping cho món ăn, quản lý giỏ hàng, đặt hàng và chọn tọa độ giao hàng trên bản đồ tương tác Leaflet, áp dụng mã giảm giá (Voucher), theo dõi hành trình đơn hàng và vị trí shipper realtime qua WebSocket, tích điểm đổi voucher, gửi khiếu nại đơn hàng. URL điều hướng: `/user/home`.
4. **Tài xế / Shipper (Role ID = 4):** Đăng ký hồ sơ shipper, nhận đơn hàng (`READY_FOR_PICKUP` $\rightarrow$ `SHIPPING` $\rightarrow$ `DONE`), cập nhật vị trí GPS realtime qua WebSocket, định tuyến lộ trình giao hàng tối ưu OSRM trên bản đồ, quản lý ví và yêu cầu rút tiền. URL điều hướng: `/shipper/donhang`.

---

## 3. QUY TẮC NGHIỆP VỤ & RÀNG BUỘC KIỂM THỬ (BUSINESS RULES & VALIDATION CONSTRAINTS)

### 3.1. Phân hệ Xác thực & Tài khoản (Authentication & Account)
* **Đăng ký tài khoản (`/dangky` - `DangKyServlet`):**
  * `username`: Bắt buộc. Chỉ được chứa chữ cái không dấu, chữ số và dấu gạch dưới (`_`), độ dài từ **3 đến 30 ký tự** (Regex: `^[a-zA-Z0-9_]{3,30}$`). Không được trùng với tài khoản đã tồn tại.
  * `password`: Bắt buộc, độ dài từ **8 đến 16 ký tự**, không chứa khoảng trắng, loại bỏ khoảng trắng đầu/cuối trước khi kiểm tra (`trim()`). Được mã hóa an toàn bằng `BCrypt.hashpw(password, BCrypt.gensalt(12))`.
  * `confirm_password`: Bắt buộc, phải trùng khớp 100% với `password`.
  * `fullname`: Bắt buộc, không được để trống, độ dài tối đa **100 ký tự** (`InputValidationUtil.checkLine`).
  * `phone`: Bắt buộc, độ dài tối đa **20 ký tự**, chỉ chứa chữ số và các ký tự `+ ( ) - .` (Regex: `^[0-9+()\\-. ]+$`).
  * `email`: Bắt buộc, đúng định dạng email, không được trùng lặp trong hệ thống.
  * `OTP Verification`: Sau khi form hợp lệ, hệ thống sinh mã OTP 6 số ngẫu nhiên qua `SecureRandom`, có thời hạn hiệu lực **5 phút** (`OTP_TTL_MILLIS = 300.000ms`), gửi qua email kích hoạt trước khi tài khoản được lưu chính thức.
  * `Rate Limiting`: Giới hạn tối đa 10 lần yêu cầu gửi OTP trong 10 phút trên cùng 1 địa chỉ IP (`RateLimitUtil`).

* **Đăng nhập (`/dangnhap` - `DangNhapServlet`):**
  * `username`: Bắt buộc, chuẩn hóa lowercase và trim trước khi xử lý.
  * `password`: Bắt buộc, so khớp mật khẩu bằng `BCrypt.checkpw(password, account.getPassword())`.
  * `Rate Limiting brute-force`:
    * Theo IP: Tối đa **5 lần thử sai liên tiếp** trong vòng 15 phút. Khi vượt ngưỡng, IP bị khóa tạm thời 15 phút.
    * Theo Account: Tối đa **10 lần thử sai liên tiếp** trên cùng một tài khoản (chống tấn công phân tán qua botnet).
  * `Kiểm tra trạng thái tài khoản`:
    * Tài khoản bị đình chỉ mềm (`account.isDeleted() == true`): Chuyển về màn hình đăng nhập với thông báo đình chỉ kèm lý do và cho phép gửi đơn kháng nghị (`/appeal`).
    * Tài khoản bị khóa vi phạm (`status.equalsIgnoreCase("BLOCKED")`): Chặn đăng nhập với thông báo khóa do bom hàng hoặc vi phạm điều khoản.
  * `Điều hướng theo quyền (Role-based Routing)`:
    * Role 1 (Super Admin) $\rightarrow$ `/tong-quan`
    * Role 2 (Shop Owner) $\rightarrow$ `/shop`
    * Role 3 (Khách hàng) $\rightarrow$ `/user/home`
    * Role 4 (Shipper) $\rightarrow$ `/shipper/donhang`
  * `Chống Session Fixation`: Cấp mới ID phiên làm việc (`req.changeSessionId()`) ngay sau khi đăng nhập thành công.

* **Xác thực bảo mật thông tin nhạy cảm (`/xac-thuc-thay-doi`):**
  * Khi tài khoản yêu cầu thay đổi Email hoặc Số tài khoản ngân hàng / PayOS API Key, hệ thống tự động sinh mã OTP 6 số gửi qua email và yêu cầu nhập mã xác thực trong 5 phút trước khi lưu thay đổi vào CSDL.

---

### 3.2. Phân hệ Quản lý Danh mục & Món ăn (Shop Category & Product CRUD)
* **Quản lý danh mục (`/Category` - `CategoryServlet`):**
  * `shopId`: Bắt buộc $> 0$.
  * `categoryName`: Bắt buộc, không được để trống hoặc chỉ chứa khoảng trắng (`isBlank()`).
  * Các thao tác hỗ trợ: `create`, `update`, `delete`.
* **Quản lý món ăn (`/Product` - `ProductServlet` & `/shop/products` - `ShopProductServlet`):**
  * `productName`: Bắt buộc, không được để trống (`isBlank()`), độ dài từ 2 đến 100 ký tự.
  * `shopId`: Bắt buộc $> 0$.
  * `categoryId`: Bắt buộc $> 0$, phải thuộc về shop hiện tại.
  * `price`: Bắt buộc $> 0$ (kiểu `BigDecimal`).
  * `stockQuantity`: Số lượng tồn kho $\ge 0$ (nếu có cấu hình).
  * `Size & Topping`: Mỗi món ăn cấu hình kích cỡ (Size S/M/L) với giá riêng và cờ `outOfStock` (hết hàng tạm thời theo từng kích cỡ).
* **Quản lý topping (`/shop/toppings` - `ShopToppingServlet`):**
  * Tên topping không được để trống.
  * Giá bán của topping phải $\ge 0$ VNĐ.

---

### 3.3. Phân hệ Giỏ hàng & Đặt hàng (Cart, Voucher & Checkout)
* **Kiểm tra trạng thái mở cửa của quán (`isOpenNow`):**
  * Khi người dùng thêm món vào giỏ (`/cart`, `/user/add-to-cart`), hệ thống kiểm tra giờ hoạt động của quán (`shop.isOpenNow()`).
  * Nếu quán hiện đang đóng cửa ngoài khung giờ phục vụ, hệ thống lập tức từ chối và cảnh báo lỗi `shop_closed`.
* **Đặt hàng & Thanh toán (`/checkout` - `CheckoutServlet`):**
  * Thông tin người nhận: Họ tên ($\le 100$ ký tự), SĐT nhận hàng ($\le 20$ ký tự, đúng regex), Địa chỉ giao hàng ($\le 500$ ký tự).
  * Tọa độ bản đồ Leaflet: Ghi nhận vĩ độ `locationX` và kinh độ `locationY`.
  * Mã giảm giá (`Voucher`):
    * Kiểm tra ngày hết hạn (`expire_at`).
    * Kiểm tra giá trị đơn hàng tối thiểu (`min_order_amount`).
    * Tính toán mức giảm trừ: Theo số tiền cố định (`FIXED`) hoặc tỷ lệ phần trăm (`PERCENT`) kèm giới hạn tối đa.
  * Trạng thái khởi tạo đơn hàng: Luôn luôn là `PENDING`.

---

### 3.4. Vòng đời đơn hàng & Theo dõi GPS Realtime
* **Order State Machine:**
  $$\text{PENDING} \longrightarrow \text{CONFIRMED} \longrightarrow \text{READY\_FOR\_PICKUP} \longrightarrow \text{SHIPPING} \longrightarrow \text{DONE}$$
  *(Trạng thái hủy `CANCELLED` chỉ được phép khi đơn chưa chuyển sang `SHIPPING`).*
* **Tích điểm thưởng (`Loyalty Points`):**
  * Khi đơn hàng chuyển trạng thái sang `DONE`, khách hàng được cộng điểm thưởng tự động (100 điểm = đổi voucher 20.000đ). Không áp dụng cho đơn bán tại quầy POS.
* **WebSocket GPS Realtime Tracking (`/ws/tracking` - `TrackingEndpoint`):**
  * Shipper gửi tọa độ vị trí thực tế định kỳ, server chuyển tiếp tức thời qua WebSocket tới khách hàng để vẽ lộ trình shipper trên bản đồ OpenStreetMap/Leaflet.
