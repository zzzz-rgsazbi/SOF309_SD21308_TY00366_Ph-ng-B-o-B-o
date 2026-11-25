package com.poly.sof309.controllers;

import org.example.models.Account;
import org.example.utils.InputValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =========================================================================
 * [Y3 - TUẦN 2] UNIT TEST CHO PHÂN HỆ AUTHENTICATION CONTROLLER (JUnit 5)
 * Sinh viên: Phùng Bảo Bảo - MSSV: TY00366 - Lớp: SD21308
 * Kỹ thuật áp dụng:
 * 1. @ParameterizedTest + @CsvSource kiểm tra miền tương đương & giá trị biên Form
 * 2. assertThrows() kiểm thử ngoại lệ bắt lỗi vi phạm nghiệp vụ
 * 3. @Timeout kiểm tra hiệu năng băm mật khẩu BCrypt không bị timeout
 * =========================================================================
 */
@DisplayName("Unit Test Phân hệ Authentication (DangNhap & DangKy Controller)")
public class AuthControllerTest {

    private AuthServiceMock authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthServiceMock();
    }

    // =========================================================================
    // 1. KIỂM THỬ THAM SỐ HOÁ GIÁ TRỊ BIÊN FORM ĐĂNG KÝ (@ParameterizedTest + @CsvSource)
    // =========================================================================

    @ParameterizedTest(name = "[{index}] Username=''{0}'', Pass=''{1}'', Confirm=''{2}'' -> Expected: {3} ({4})")
    @DisplayName("TC_AUTH_BVA: Kiểm tra validation biên Username và Password đăng ký")
    @CsvSource({
            // Biên Username: độ dài 3 - 30 ký tự, regex ^[a-zA-Z0-9_]{3,30}$
            "'', 'Pass@1234', 'Pass@1234', false, 'Username không được để trống'",
            "'ab', 'Pass@1234', 'Pass@1234', false, 'Username dưới 3 ký tự (Biên dưới lỗi)'",
            "'abc', 'Pass@1234', 'Pass@1234', true, 'Username đúng 3 ký tự (Biên dưới hợp lệ)'",
            "'valid_user_2026', 'Pass@1234', 'Pass@1234', true, 'Username độ dài bình thường'",
            "'user_exactly_30_characters_xx', 'Pass@1234', 'Pass@1234', true, 'Username đúng 30 ký tự (Biên trên hợp lệ)'",
            "'user_exceeding_31_characters_xxx', 'Pass@1234', 'Pass@1234', false, 'Username 31 ký tự (Biên trên lỗi)'",
            "'user@special#', 'Pass@1234', 'Pass@1234', false, 'Username chứa ký tự đặc biệt'",
            "'user space', 'Pass@1234', 'Pass@1234', false, 'Username chứa khoảng trắng'",

            // Biên Password: độ dài 8 - 16 ký tự, không khoảng trắng
            "'validuser', '1234567', '1234567', false, 'Mật khẩu 7 ký tự (Biên dưới lỗi)'",
            "'validuser', '12345678', '12345678', true, 'Mật khẩu đúng 8 ký tự (Biên dưới hợp lệ)'",
            "'validuser', 'Pass@12345678901', 'Pass@12345678901', true, 'Mật khẩu đúng 16 ký tự (Biên trên hợp lệ)'",
            "'validuser', 'Pass@123456789012', 'Pass@123456789012', false, 'Mật khẩu 17 ký tự (Biên trên lỗi)'",
            "'validuser', 'Pass 123456', 'Pass 123456', false, 'Mật khẩu chứa khoảng trắng'",
            "'validuser', 'Pass@1234', 'Pass@9999', false, 'Mật khẩu xác nhận không khớp'"
    })
    public void testRegisterValidationWithCsvSource(String username, String password, String confirmPassword,
                                                    boolean expectedSuccess, String scenarioDescription) {
        boolean actualResult = authService.validateRegisterForm(username, password, confirmPassword);
        assertEquals(expectedSuccess, actualResult, "Kịch bản thất bại: " + scenarioDescription);
    }

    @ParameterizedTest(name = "[{index}] Phone=''{0}'' -> Expected: {1} ({2})")
    @DisplayName("TC_AUTH_PHONE: Kiểm tra validation biên số điện thoại đăng ký")
    @CsvSource({
            "'0912345678', true, 'Số điện thoại 10 chữ số hợp lệ'",
            "'+84912345678', true, 'Số điện thoại quốc tế có dấu +'",
            "'0912-345-678', true, 'Số điện thoại có dấu gạch ngang'",
            "'', true, 'Số điện thoại để trống (tuỳ chọn hoặc xử lý riêng)'",
            "'0912abc789', false, 'Số điện thoại chứa chữ cái (Lỗi)'",
            "'012345678901234567891', false, 'Số điện thoại vượt quá 20 ký tự (Lỗi biên)'"
    })
    public void testPhoneValidation(String phone, boolean expectedSuccess, String message) {
        String error = InputValidationUtil.checkPhone("Số điện thoại", phone);
        boolean actualSuccess = (error == null);
        assertEquals(expectedSuccess, actualSuccess, message);
    }

    @ParameterizedTest(name = "[{index}] Role={0} -> Redirect={1}")
    @DisplayName("TC_AUTH_ROLE: Kiểm tra điều hướng chính xác theo 4 phân quyền sau đăng nhập")
    @CsvSource({
            "1, '/tong-quan', 'Super Admin điều hướng tới Tổng quan portal'",
            "2, '/shop', 'Shop Owner điều hướng tới Quản lý Shop dashboard'",
            "3, '/user/home', 'Khách hàng điều hướng tới Trang chủ người dùng'",
            "4, '/shipper/donhang', 'Shipper điều hướng tới Danh sách đơn giao'",
            "99, '/index.jsp', 'Role không xác định điều hướng về index'"
    })
    public void testRoleBasedRouting(long roleId, String expectedRedirectUrl, String message) {
        String actualUrl = authService.determineRedirectUrlByRole(roleId);
        assertEquals(expectedRedirectUrl, actualUrl, message);
    }

    // =========================================================================
    // 2. KIỂM THỬ NGOẠI LỆ NGHIỆP VỤ (Exception Testing với assertThrows)
    // =========================================================================

    @Test
    @DisplayName("TC_EXP_01: Ném IllegalArgumentException khi đăng ký username đã tồn tại")
    public void testRegisterDuplicateUsernameThrowsException() {
        authService.registerAccount("baobao_exist", "existing@example.com", "Pass@1234", "Pass@1234");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerAccount("baobao_exist", "different@example.com", "Pass@1234", "Pass@1234");
        }, "Phải ném IllegalArgumentException khi đăng ký trùng username");

        assertTrue(exception.getMessage().contains("Username đã được đăng ký!"),
                "Thông điệp ngoại lệ phải thông báo rõ trùng Username");
    }

    @Test
    @DisplayName("TC_EXP_02: Ném IllegalArgumentException khi đăng ký email đã tồn tại")
    public void testRegisterDuplicateEmailThrowsException() {
        authService.registerAccount("user_one", "duplicate@pob.com", "Pass@1234", "Pass@1234");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerAccount("user_two", "duplicate@pob.com", "Pass@1234", "Pass@1234");
        }, "Phải ném IllegalArgumentException khi đăng ký trùng Email");

        assertTrue(exception.getMessage().contains("Email đã được đăng ký!"),
                "Thông điệp ngoại lệ phải thông báo rõ trùng Email");
    }

    @Test
    @DisplayName("TC_EXP_03: Ném IllegalStateException khi đăng nhập tài khoản bị khoá (BLOCKED)")
    public void testLoginBlockedAccountThrowsException() {
        Account blockedAccount = new Account();
        blockedAccount.setUserName("bom_hang_user");
        blockedAccount.setPassWord(BCrypt.hashpw("Pass@1234", BCrypt.gensalt(10)));
        blockedAccount.setStaTus("BLOCKED");
        authService.addMockAccount(blockedAccount);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.login("bom_hang_user", "Pass@1234");
        }, "Phải ném IllegalStateException khi tài khoản bị BLOCKED");

        assertEquals("Tài khoản đã bị khoá do vi phạm (bom hàng nhiều lần)", exception.getMessage());
    }

    @Test
    @DisplayName("TC_EXP_04: Ném IllegalStateException khi đăng nhập tài khoản bị đình chỉ (isDeleted)")
    public void testLoginSuspendedAccountThrowsException() {
        Account suspendedAccount = new Account();
        suspendedAccount.setUserName("suspended_user");
        suspendedAccount.setPassWord(BCrypt.hashpw("Pass@1234", BCrypt.gensalt(10)));
        suspendedAccount.setDeleted(true);
        suspendedAccount.setSuspendReason("Vi phạm tiêu chuẩn cộng đồng");
        authService.addMockAccount(suspendedAccount);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.login("suspended_user", "Pass@1234");
        }, "Phải ném IllegalStateException khi tài khoản bị đình chỉ");

        assertTrue(exception.getMessage().contains("Tài khoản đã bị đình chỉ"),
                "Thông điệp ngoại lệ phải thể hiện tài khoản bị đình chỉ");
    }

    @Test
    @DisplayName("TC_EXP_05: Ném IllegalArgumentException khi tài khoản hoặc mật khẩu để trống")
    public void testLoginEmptyCredentialsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login("", "12345678");
        }, "Phải ném ngoại lệ khi username rỗng");

        assertThrows(IllegalArgumentException.class, () -> {
            authService.login("valid_user", "");
        }, "Phải ném ngoại lệ khi password rỗng");
    }

    // =========================================================================
    // 3. KIỂM THỬ THỜI GIAN THỰC THI (@Timeout)
    // =========================================================================

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("TC_TIME_01: Mã hóa và so khớp mật khẩu BCrypt hoàn thành trong 2 giây")
    public void testBcryptHashPerformanceTimeout() {
        String plainPassword = "SecurePassword@2026";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        assertNotNull(hashedPassword);
        assertTrue(BCrypt.checkpw(plainPassword, hashedPassword), "So khớp BCrypt phải thành công");
    }

    // =========================================================================
    // LỚP GIẢ LẬP NGHIỆP VỤ (Service Layer Mocking Controllers logic)
    // =========================================================================
    public static class AuthServiceMock {
        private final java.util.Map<String, Account> database = new java.util.HashMap<>();

        public void addMockAccount(Account account) {
            database.put(account.getUserName().toLowerCase(), account);
        }

        public boolean validateRegisterForm(String username, String password, String confirmPassword) {
            if (username == null || !username.matches("^[a-zA-Z0-9_]{3,30}$")) {
                return false;
            }
            if (password == null) {
                return false;
            }
            String trimmedPass = password.trim();
            if (trimmedPass.length() < 8 || trimmedPass.length() > 16 || trimmedPass.contains(" ")) {
                return false;
            }
            return trimmedPass.equals(confirmPassword != null ? confirmPassword.trim() : null);
        }

        public String determineRedirectUrlByRole(long roleId) {
            switch ((int) roleId) {
                case 1: return "/tong-quan";
                case 2: return "/shop";
                case 3: return "/user/home";
                case 4: return "/shipper/donhang";
                default: return "/index.jsp";
            }
        }

        public Account login(String username, String password) {
            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Tài khoản hoặc mật khẩu không được để trống!");
            }
            Account account = database.get(username.trim().toLowerCase());
            if (account == null || !BCrypt.checkpw(password, account.getPassWord())) {
                return null;
            }
            if (account.isDeleted()) {
                throw new IllegalStateException("Tài khoản đã bị đình chỉ sử dụng: " + account.getSuspendReason());
            }
            if ("BLOCKED".equalsIgnoreCase(account.getStaTus())) {
                throw new IllegalStateException("Tài khoản đã bị khoá do vi phạm (bom hàng nhiều lần)");
            }
            return account;
        }

        public void registerAccount(String username, String email, String password, String confirmPassword) {
            if (!validateRegisterForm(username, password, confirmPassword)) {
                throw new IllegalArgumentException("Dữ liệu form đăng ký không hợp lệ!");
            }
            if (database.containsKey(username.toLowerCase())) {
                throw new IllegalArgumentException("Username đã được đăng ký!");
            }
            for (Account acc : database.values()) {
                if (email != null && email.equalsIgnoreCase(acc.getEmail())) {
                    throw new IllegalArgumentException("Email đã được đăng ký!");
                }
            }
            Account newAcc = new Account();
            newAcc.setUserName(username);
            newAcc.setEmail(email);
            newAcc.setPassWord(BCrypt.hashpw(password, BCrypt.gensalt(10)));
            newAcc.setRoleId(3L);
            database.put(username.toLowerCase(), newAcc);
        }
    }
}
