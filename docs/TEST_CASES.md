# BỘ KỊCH BẢN KIỂM THỬ CHI TIẾT (TEST CASES) - HỆ THỐNG POB

* **Dự án:** Hệ thống Website Đặt Đồ Ăn Trực Tuyến - POB
* **Sinh viên thực hiện:** Phùng Bảo Bảo (MSSV: TY00366) - Lớp SD21308
* **Giảng viên hướng dẫn:** ThS. Trần Quang Bình (binhtq7 - Thầy Mười Giáo)
* **Phiên bản:** 2.0 (Hoàn thiện đầy đủ kỹ thuật BVA, EP và Exception Testing)

---

## 1. PHÂN HỆ XÁC THỰC & TÀI KHOẢN (AUTHENTICATION & ACCOUNT)

| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại test | Kỹ thuật kiểm thử | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :---: | :--- | :---: | :---: | :--- | :--- | :---: |
| **TC_AUTH_01** | Đăng nhập | Đăng nhập thành công với tài khoản khách hàng hợp lệ | Positive | Phân vùng tương đương (EP) | `user_test` / `Pass@1234` | Xác thực BCrypt thành công, cấp session, chuyển hướng `/user/home` | Pass |
| **TC_AUTH_02** | Đăng nhập | Đăng nhập thất bại khi để trống mật khẩu | Negative | Giá trị biên (BVA) | `user_test` / `""` | Báo lỗi "Mật khẩu không được để trống", giữ lại username trên form | Pass |
| **TC_AUTH_03** | Đăng nhập | Đăng nhập thất bại khi nhập sai mật khẩu | Negative | Phân vùng tương đương (EP) | `user_test` / `WrongPass999` | Báo lỗi "Tài khoản hoặc mật khẩu không chính xác", tăng bộ đếm rate-limit | Pass |
| **TC_AUTH_04** | Đăng nhập | Điều hướng chuẩn xác theo phân quyền 4 Roles | Positive | Phân vùng tương đương (EP) | Role 1 (Admin), Role 2 (Shop), Role 3 (User), Role 4 (Shipper) | Role 1 $\rightarrow$ `/tong-quan`<br>Role 2 $\rightarrow$ `/shop`<br>Role 3 $\rightarrow$ `/user/home`<br>Role 4 $\rightarrow$ `/shipper/donhang` | Pass |
| **TC_AUTH_05** | Đăng nhập | Chặn đăng nhập khi tài khoản bị khóa do vi phạm | Negative | Kiểm thử ngoại lệ (EXP) | Tài khoản có status = `BLOCKED` | Báo lỗi tài khoản bị khoá do bom hàng, không cấp session | Pass |
| **TC_AUTH_06** | Đăng nhập | Cảnh báo đình chỉ và chuyển hướng kháng nghị | Negative | Kiểm thử ngoại lệ (EXP) | Tài khoản có `isDeleted = true` | Đánh dấu `suspended = true`, hiển thị form gửi kháng nghị `/appeal` | Pass |
| **TC_AUTH_07** | Đăng ký | Đăng ký tài khoản mới thành công | Positive | Phân vùng tương đương (EP) | Username: `baobao_2026`<br>Pass: `Bao@2026`<br>Confirm: `Bao@2026`<br>Email: `bao@example.com`<br>Phone: `0912345678` | Sinh OTP 6 số gửi qua email, lưu thông tin tạm trong session | Pass |
| **TC_AUTH_08** | Đăng ký | Đăng ký thất bại khi username dưới 3 ký tự | Negative | Giá trị biên (BVA) | Username: `ab` (2 ký tự) | Báo lỗi "Tên đăng nhập chỉ được chứa chữ không dấu, số, gạch dưới, dài 3–30 ký tự" | Pass |
| **TC_AUTH_09** | Đăng ký | Đăng ký thất bại khi username vượt quá 30 ký tự | Negative | Giá trị biên (BVA) | Username: 31 ký tự chữ số | Báo lỗi validation độ dài username vượt quá 30 ký tự | Pass |
| **TC_AUTH_10** | Đăng ký | Đăng ký thất bại khi username chứa ký tự đặc biệt | Negative | Phân vùng tương đương (EP) | Username: `user@123#` | Báo lỗi username chỉ cho phép chữ, số và dấu gạch dưới | Pass |
| **TC_AUTH_11** | Đăng ký | Đăng ký thất bại khi mật khẩu dưới 8 ký tự | Negative | Giá trị biên (BVA) | Password: `Pass123` (7 ký tự) | Báo lỗi "Mật khẩu phải có độ dài từ 8 đến 16 ký tự!" | Pass |
| **TC_AUTH_12** | Đăng ký | Đăng ký thất bại khi mật khẩu vượt quá 16 ký tự | Negative | Giá trị biên (BVA) | Password: `Pass1234567890123` (17 ký tự) | Báo lỗi "Mật khẩu phải có độ dài từ 8 đến 16 ký tự!" | Pass |
| **TC_AUTH_13** | Đăng ký | Đăng ký thất bại khi mật khẩu chứa khoảng trắng | Negative | Phân vùng tương đương (EP) | Password: `Pass 1234` | Báo lỗi "Mật khẩu không được chứa khoảng trắng!" | Pass |
| **TC_AUTH_14** | Đăng ký | Đăng ký thất bại khi confirm password không khớp | Negative | Phân vùng tương đương (EP) | Pass: `Pass@1234`<br>Confirm: `Pass@9999` | Báo lỗi "Mật khẩu không khớp!" | Pass |
| **TC_AUTH_15** | Đăng ký | Đăng ký thất bại khi email đã được đăng ký trước | Negative | Kiểm thử ngoại lệ (EXP) | Email: `existing@gmail.com` | Ném lỗi nghiệp vụ "Email đã được đăng ký!" | Pass |
| **TC_AUTH_16** | Đăng ký | Đăng ký thất bại khi số điện thoại chứa ký tự chữ | Negative | Phân tích giá trị biên | Phone: `0912abc789` | Báo lỗi "Số điện thoại chỉ được chứa chữ số và các ký tự + ( ) - ." | Pass |

---

## 2. PHÂN HỆ QUẢN LÝ DANH MỤC & MÓN ĂN (SHOP PRODUCT & CATEGORY CRUD)

| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại test | Kỹ thuật kiểm thử | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :---: | :--- | :---: | :---: | :--- | :--- | :---: |
| **TC_CAT_01** | Thêm Category | Thêm mới danh mục món ăn thành công | Positive | Phân vùng tương đương (EP) | ShopId: `1`, CategoryName: `Trà Trái Cây Đậm Vị` | Tạo mới category thành công trong CSDL, điều hướng danh sách | Pass |
| **TC_CAT_02** | Thêm Category | Thêm danh mục thất bại khi để trống tên | Negative | Giá trị biên (BVA) | ShopId: `1`, CategoryName: `   ` (khoảng trắng) | Báo lỗi "Ten category khong duoc de trong", không lưu vào DB | Pass |
| **TC_CAT_03** | Thêm Category | Thêm danh mục thất bại khi ShopId không hợp lệ | Negative | Giá trị biên (BVA) | ShopId: `0` hoặc `-1`, CategoryName: `Cà Phê` | Báo lỗi "Shop ID khong hop le" | Pass |
| **TC_PROD_01** | Thêm Món ăn | Thêm mới món ăn thành công với dữ liệu hợp lệ | Positive | Phân vùng tương đương (EP) | ShopId: `1`, CatId: `1`, Tên: `Trà Đào Cam Sả`, Giá: `35000`, Tồn: `100` | Tạo món thành công, lưu bản ghi vào bảng Products | Pass |
| **TC_PROD_02** | Thêm Món ăn | Thêm món thất bại khi để trống tên món | Negative | Giá trị biên (BVA) | Tên: `""`, Giá: `30000`, ShopId: `1`, CatId: `1` | Báo lỗi "Ten san pham khong duoc de trong" | Pass |
| **TC_PROD_03** | Thêm Món ăn | Thêm món thất bại khi giá bán bằng 0 | Negative | Giá trị biên (BVA) | Tên: `Trà Tắc`, Giá: `0`, ShopId: `1`, CatId: `1` | Báo lỗi "Gia san pham phai lon hon 0" | Pass |
| **TC_PROD_04** | Thêm Món ăn | Thêm món thất bại khi giá bán là số âm | Negative | Giá trị biên (BVA) | Tên: `Trà Sữa`, Giá: `-15000`, ShopId: `1`, CatId: `1` | Báo lỗi "Gia san pham phai lon hon 0" | Pass |
| **TC_PROD_05** | Thêm Món ăn | Thêm món thất bại khi số lượng tồn kho âm | Negative | Giá trị biên (BVA) | Tên: `Bánh Ngọt`, Giá: `25000`, Tồn: `-5` | Báo lỗi "So luong ton khong hop le" | Pass |
| **TC_PROD_06** | Thêm Món ăn | Thêm món thất bại khi CategoryId không hợp lệ | Negative | Giá trị biên (BVA) | Tên: `Mì Cay`, Giá: `40000`, ShopId: `1`, CatId: `0` | Báo lỗi "Category khong hop le" | Pass |
| **TC_PROD_07** | Trạng thái Size | Đánh dấu hết hàng tạm thời theo từng kích cỡ | Positive | Phân vùng tương đương (EP) | Size M, `outOfStock = true` | Món size M bị vô hiệu hoá trên menu đặt món của khách hàng | Pass |
| **TC_PROD_08** | Cập nhật Món | Ném ngoại lệ khi cập nhật món ăn với ID không tồn tại | Negative | Kiểm thử ngoại lệ (EXP) | Update món có ID: `999999` (không tồn tại) | Báo lỗi "Khong tim thay san pham", không thực thi câu lệnh UPDATE | Pass |

---

## 3. PHÂN HỆ GIỎ HÀNG, VOUCHER & ĐẶT HÀNG (CART & ORDER)

| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại test | Kỹ thuật kiểm thử | Dữ liệu kiểm thử (Test Data) | Kết quả mong đợi | Trạng thái |
| :---: | :---: | :--- | :---: | :---: | :--- | :--- | :---: |
| **TC_ORD_01** | Giỏ hàng | Từ chối thêm món khi quán đang đóng cửa | Negative | Kiểm thử ngoại lệ (EXP) | Quán có `isOpenNow() == false` | Chặn thêm giỏ hàng, thông báo cảnh báo "Quán hiện đang đóng cửa" | Pass |
| **TC_ORD_02** | Đặt hàng | Đặt hàng thành công với thông tin và tọa độ Leaflet | Positive | Phân vùng tương đương (EP) | Tên: `Phùng Bảo Bảo`, SĐT: `0987654321`, Tọa độ: `(10.8532, 106.6268)` | Tạo bản ghi Order mới với trạng thái `PENDING`, lưu tọa độ GPS chuẩn | Pass |
| **TC_ORD_03** | Voucher | Áp dụng mã giảm giá hợp lệ thành công | Positive | Phân vùng tương đương (EP) | Voucher: `POBGIAM30`, Đơn: `120.000đ`, MinOrder: `100.000đ` | Giảm trừ chính xác `30.000đ` vào tổng số tiền thanh toán của đơn | Pass |
| **TC_ORD_04** | Voucher | Từ chối áp dụng voucher khi chưa đủ đơn tối thiểu | Negative | Giá trị biên (BVA) | Voucher min 150.000đ, Đơn: `149.000đ` (giá trị biên cận kề) | Báo lỗi "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher này" | Pass |
| **TC_ORD_05** | Voucher | Từ chối voucher đã quá hạn sử dụng | Negative | Kiểm thử ngoại lệ (EXP) | Voucher có `expire_at` trong quá khứ | Báo lỗi "Mã voucher đã hết hạn sử dụng!" | Pass |
| **TC_ORD_06** | Order Status | Chuyển đổi trạng thái đơn hàng theo đúng Order State Machine | Positive | Phân vùng tương đương (EP) | `PENDING` $\rightarrow$ `CONFIRMED` $\rightarrow$ `READY_FOR_PICKUP` $\rightarrow$ `SHIPPING` $\rightarrow$ `DONE` | Trạng thái chuyển đổi tuần tự chính xác, kích hoạt tích điểm Loyalty ở `DONE` | Pass |
| **TC_ORD_07** | Realtime GPS | Relay vị trí shipper qua WebSocket endpoint `/ws/tracking` | Positive | Kiểm thử tương tranh realtime | Tọa độ GPS Shipper gửi liên tục | Khách hàng nhận được tọa độ qua WebSocket và vẽ marker di chuyển trên bản đồ | Pass |
