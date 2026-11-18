# BỘ KỊCH BẢN KIỂM THỬ CHI TIẾT (TEST CASES) - HỆ THỐNG POB

* **Dự án:** Hệ thống Website Đặt Đồ Ăn Trực Tuyến - POB
* **Sinh viên:** Phùng Bảo Bảo (TY00366) - Lớp SD21308
* **Giảng viên hướng dẫn:** ThS. Trần Quang Bình (binhtq7 - Thầy Mười Giáo)

---

## 1. PHÂN HỆ XÁC THỰC (AUTHENTICATION & ACCOUNT)

| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại test | Các bước thực hiện | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: |
| **TC_AUTH_01** | Đăng nhập | Đăng nhập thành công với tài khoản khách hàng hợp lệ | Positive | 1. Truy cập `/dangnhap`<br>2. Nhập username và password đúng<br>3. Bấm "Đăng nhập" | `user_test` / `Pass@1234` | Đăng nhập thành công, lưu session, chuyển hướng về trang chủ người dùng | Pass |
| **TC_AUTH_02** | Đăng nhập | Đăng nhập thất bại khi để trống mật khẩu | Negative | 1. Truy cập `/dangnhap`<br>2. Nhập username hợp lệ<br>3. Để trống mật khẩu<br>4. Bấm "Đăng nhập" | `user_test` / `""` | Không gửi request hoặc hiển thị cảnh báo "Mật khẩu không được để trống" | Pass |
| **TC_AUTH_03** | Đăng nhập | Đăng nhập thất bại với sai mật khẩu | Negative | 1. Truy cập `/dangnhap`<br>2. Nhập username đúng<br>3. Nhập password sai<br>4. Bấm "Đăng nhập" | `user_test` / `WrongPass!` | Hệ thống thông báo "Tài khoản hoặc mật khẩu không chính xác", không tạo session | Pass |
| **TC_AUTH_04** | Đăng ký | Đăng ký tài khoản mới thành công | Positive | 1. Mở trang `/dangky`<br>2. Điền đầy đủ thông tin hợp lệ<br>3. Xác nhận mật khẩu trùng khớp<br>4. Bấm "Đăng ký" | Username: `baobao_new`<br>Email: `baobao@gmail.com`<br>Pass: `Bao@2026`<br>Phone: `0912345678` | Tạo tài khoản thành công trong DB (mật khẩu mã hóa BCrypt), chuyển sang trang đăng nhập | Pass |
| **TC_AUTH_05** | Đăng ký | Đăng ký thất bại khi username dưới 5 ký tự | Negative (Biên) | 1. Mở trang `/dangky`<br>2. Nhập username 4 ký tự<br>3. Nhập các trường khác hợp lệ<br>4. Bấm "Đăng ký" | Username: `user`<br>Email: `u@gmail.com` | Báo lỗi validation "Tên đăng nhập phải từ 5 đến 50 ký tự" | Pass |
| **TC_AUTH_06** | Đăng ký | Đăng ký thất bại khi email đã tồn tại | Negative (Exception) | 1. Mở trang `/dangky`<br>2. Nhập email đã được đăng ký trước đó<br>3. Bấm "Đăng ký" | Email: `existing@gmail.com` | Hệ thống bắt ngoại lệ, báo lỗi "Email này đã được sử dụng" | Pass |

---

## 2. PHÂN HỆ QUẢN LÝ SẢN PHẨM & MÓN ĂN (SHOP PRODUCTS CRUD)

| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại test | Các bước thực hiện | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: |
| **TC_PROD_01** | Thêm món ăn | Thêm mới món ăn thành công kèm kích cỡ và giá | Positive | 1. Đăng nhập quyền Shop<br>2. Vào `/shop/products`<br>3. Bấm "Thêm món ăn"<br>4. Điền tên món, chọn loại, nhập size S (30k), size M (35k)<br>5. Bấm Lưu | Tên: `Trà sữa Trân châu POB`<br>Loại: `Trà sữa`<br>Size S: `30000`, Size M: `35000` | Món ăn được tạo mới trong DB kèm 2 bản ghi kích cỡ tương ứng, hiển thị trên danh sách | Pass |
| **TC_PROD_02** | Thêm món ăn | Thêm món ăn thất bại khi để trống tên | Negative | 1. Mở form thêm món<br>2. Bỏ trống tên món ăn<br>3. Bấm Lưu | Tên: `""`, Giá size: `25000` | Hiển thị thông báo "Tên món ăn không được để trống" | Pass |
| **TC_PROD_03** | Thêm món ăn | Thêm món ăn thất bại khi giá kích cỡ âm | Negative (Biên) | 1. Mở form thêm món<br>2. Nhập giá bán: `-5000`<br>3. Bấm Lưu | Tên: `Cà phê đá`, Giá: `-5000` | Hệ thống từ chối lưu, cảnh báo "Giá bán phải lớn hơn 0" | Pass |
| **TC_PROD_04** | Trạng thái món | Đánh dấu hết hàng tạm thời (outOfStock) theo size | Positive | 1. Chọn size L của món ăn<br>2. Tích chọn "Hết hàng tạm thời"<br>3. Lưu cập nhật | `outOfStock = true` | Trên menu khách hàng (`/user/shop`), size L bị vô hiệu hóa không cho chọn | Pass |

---

## 3. PHÂN HỆ ĐẶT HÀNG & THANH TOÁN (CART & CHECKOUT)

| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại test | Các bước thực hiện | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: |
| **TC_ORDER_01** | Thêm giỏ hàng | Chặn thêm món vào giỏ khi quán đang đóng cửa | Negative | 1. Truy cập thực đơn quán đang đóng cửa (`isOpenNow == false`)<br>2. Bấm "Thêm vào giỏ" | Shop status: `CLOSED` | Nút thêm bị vô hiệu hóa hoặc server chặn với mã lỗi `shop_closed` | Pass |
| **TC_ORDER_02** | Checkout | Đặt hàng thành công với địa chỉ và tọa độ Leaflet | Positive | 1. Vào giỏ hàng bấm "Thanh toán"<br>2. Nhập họ tên, SĐT nhận hàng<br>3. Chọn vị trí trên bản đồ Leaflet<br>4. Bấm "Xác nhận đặt hàng" | Tên: `Phùng Bảo Bảo`<br>SĐT: `0987654321`<br>Tọa độ: `(10.8532, 106.6268)` | Tạo bản ghi `Order` với trạng thái `PENDING`, lưu đúng tọa độ `locationX/locationY` | Pass |
| **TC_ORDER_03** | Voucher | Áp dụng mã giảm giá hợp lệ thành công | Positive | 1. Tại màn hình Checkout, nhập mã voucher<br>2. Bấm "Áp dụng" | Mã: `FREESHIP50`<br>Đơn hàng: `150.000đ` | Tiền giảm giá `discountAmount` được trừ chính xác vào tổng thanh toán | Pass |
| **TC_ORDER_04** | Realtime GPS | Cập nhật tọa độ shipper trên bản đồ realtime | Positive | 1. Đơn chuyển `SHIPPING`<br>2. Shipper truyền GPS qua WebSocket `/ws/tracking`<br>3. Khách hàng xem trang `/bill` | Tọa độ GPS shipper biến thiên | Marker vị trí shipper trên bản đồ Leaflet di chuyển mượt mà không cần reload trang | Pass |
