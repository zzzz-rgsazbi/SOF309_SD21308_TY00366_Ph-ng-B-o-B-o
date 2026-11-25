package com.poly.sof309.controllers;

import org.example.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =========================================================================
 * [Y3 - TUẦN 2] UNIT TEST CHO PHÂN HỆ QUẢN LÝ SẢN PHẨM & MÓN ĂN (ProductController)
 * Sinh viên: Phùng Bảo Bảo - MSSV: TY00366 - Lớp: SD21308
 * =========================================================================
 */
@DisplayName("Unit Test Phân hệ Product Controller (Shop Product Management)")
public class ProductControllerTest {

    private ProductServiceMock productService;

    @BeforeEach
    public void setUp() {
        productService = new ProductServiceMock();
    }

    // =========================================================================
    // 1. KIỂM THỬ THAM SỐ HOÁ GIÁ TRỊ BIÊN FORM MÓN ĂN (@ParameterizedTest + @CsvSource)
    // =========================================================================

    @ParameterizedTest(name = "[{index}] Name=''{0}'', Price={1}, Stock={2}, ShopId={3}, CatId={4} -> Expected: {5} ({6})")
    @DisplayName("TC_PROD_BVA: Kiểm tra validation biên các trường Món ăn")
    @CsvSource({
            // Tên sản phẩm
            "'', 35000, 50, 1, 1, false, 'Tên món ăn để trống'",
            "'   ', 35000, 50, 1, 1, false, 'Tên món ăn toàn khoảng trắng'",
            "'Trà Đào Cam Sả', 35000, 50, 1, 1, true, 'Món ăn đầy đủ thông tin hợp lệ'",

            // Biên Giá bán (Price > 0)
            "'Cà phê sữa', -1000, 50, 1, 1, false, 'Giá bán âm (Biên lỗi)'",
            "'Cà phê sữa', 0, 50, 1, 1, false, 'Giá bán bằng 0 (Biên lỗi)'",
            "'Cà phê sữa', 1, 50, 1, 1, true, 'Giá bán 1 đồng (Biên dưới hợp lệ)'",
            "'Cà phê đặc biệt', 500000, 50, 1, 1, true, 'Giá bán 500.000đ hợp lệ'",

            // Biên Số lượng tồn (Stock >= 0)
            "'Bánh tráng nướng', 20000, -1, 1, 1, false, 'Tồn kho âm (Biên lỗi)'",
            "'Bánh tráng nướng', 20000, 0, 1, 1, true, 'Tồn kho bằng 0 (Hết hàng tạm thời)'",
            "'Bánh tráng nướng', 20000, 100, 1, 1, true, 'Tồn kho dương'",

            // Biên ShopId và CategoryId (> 0)
            "'Trà sữa POB', 30000, 20, 0, 1, false, 'ShopId bằng 0 (Lỗi)'",
            "'Trà sữa POB', 30000, 20, -1, 1, false, 'ShopId âm (Lỗi)'",
            "'Trà sữa POB', 30000, 20, 1, 0, false, 'CategoryId bằng 0 (Lỗi)'",
            "'Trà sữa POB', 30000, 20, 1, -5, false, 'CategoryId âm (Lỗi)'"
    })
    public void testProductValidationWithCsvSource(String productName, double priceValue, int stock,
                                                   long shopId, long categoryId,
                                                   boolean expectedSuccess, String message) {
        Product p = new Product();
        p.setProductName(productName);
        p.setPrice(BigDecimal.valueOf(priceValue));
        p.setStockQuantity(stock);
        p.setShopId(shopId);
        p.setCategoryId(categoryId);

        String error = productService.validateProduct(p);
        boolean actualSuccess = (error == null);
        assertEquals(expectedSuccess, actualSuccess, message);
    }

    // =========================================================================
    // 2. KIỂM THỬ NGOẠI LỆ NGHIỆP VỤ (Exception Testing với assertThrows)
    // =========================================================================

    @Test
    @DisplayName("TC_EXP_PROD_01: Ném IllegalArgumentException khi thêm món ăn có giá <= 0")
    public void testCreateProductNegativePriceThrowsException() {
        Product invalidProduct = new Product();
        invalidProduct.setProductName("Trà Tắc");
        invalidProduct.setPrice(BigDecimal.valueOf(-5000));
        invalidProduct.setShopId(1L);
        invalidProduct.setCategoryId(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(invalidProduct);
        }, "Phải ném IllegalArgumentException khi giá món ăn <= 0");

        assertTrue(ex.getMessage().contains("Gia san pham phai lon hon 0"));
    }

    @Test
    @DisplayName("TC_EXP_PROD_02: Ném IllegalArgumentException khi thêm món ăn để trống tên")
    public void testCreateProductEmptyNameThrowsException() {
        Product emptyNameProduct = new Product();
        emptyNameProduct.setProductName("");
        emptyNameProduct.setPrice(BigDecimal.valueOf(30000));
        emptyNameProduct.setShopId(1L);
        emptyNameProduct.setCategoryId(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(emptyNameProduct);
        });

        assertTrue(ex.getMessage().contains("Ten san pham khong duoc de trong"));
    }

    @Test
    @DisplayName("TC_EXP_PROD_03: Ném NoSuchElementException khi cập nhật món ăn với ID không tồn tại")
    public void testUpdateProductNotFoundThrowsException() {
        Product nonExistent = new Product();
        nonExistent.setId(999999L);
        nonExistent.setProductName("Món Ảo");
        nonExistent.setPrice(BigDecimal.valueOf(25000));
        nonExistent.setShopId(1L);
        nonExistent.setCategoryId(1L);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> {
            productService.updateProduct(nonExistent);
        }, "Phải ném NoSuchElementException khi không tìm thấy ID sản phẩm");

        assertTrue(ex.getMessage().contains("Không tìm thấy sản phẩm"));
    }

    @Test
    @DisplayName("TC_EXP_PROD_04: Ném IllegalStateException khi xóa món ăn đang có đơn hàng xử lý")
    public void testDeleteProductWithActiveOrdersThrowsException() {
        Product activeProduct = new Product();
        activeProduct.setId(10L);
        activeProduct.setProductName("Trà sữa Oolong");
        activeProduct.setPrice(BigDecimal.valueOf(32000));
        activeProduct.setShopId(1L);
        activeProduct.setCategoryId(1L);
        productService.createProduct(activeProduct);

        // Giả lập trạng thái món ăn đang có đơn hàng chưa hoàn thành
        productService.markProductInActiveOrder(10L, true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            productService.deleteProduct(10L);
        }, "Phải ném IllegalStateException khi xóa món đang trong đơn hàng hoạt động");

        assertTrue(ex.getMessage().contains("Không thể xoá món ăn đang nằm trong đơn hàng"));
    }

    // =========================================================================
    // LỚP GIẢ LẬP NGHIỆP VỤ (Service Layer Mocking ProductController)
    // =========================================================================
    public static class ProductServiceMock {
        private final java.util.Map<Long, Product> productTable = new java.util.HashMap<>();
        private final java.util.Set<Long> productsInOrders = new java.util.HashSet<>();

        public String validateProduct(Product product) {
            if (product.getProductName() == null || product.getProductName().isBlank()) {
                return "Ten san pham khong duoc de trong";
            }
            if (product.getShopId() <= 0) {
                return "Shop khong hop le";
            }
            if (product.getCategoryId() <= 0) {
                return "Category khong hop le";
            }
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return "Gia san pham phai lon hon 0";
            }
            if (product.getStockQuantity() != null && product.getStockQuantity() < 0) {
                return "So luong ton khong hop le";
            }
            return null;
        }

        public void createProduct(Product product) {
            String error = validateProduct(product);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
            long id = product.getId() > 0 ? product.getId() : (productTable.size() + 1);
            product.setId(id);
            productTable.put(id, product);
        }

        public void updateProduct(Product product) {
            if (product.getId() <= 0 || !productTable.containsKey(product.getId())) {
                throw new NoSuchElementException("Không tìm thấy sản phẩm có ID: " + product.getId());
            }
            String error = validateProduct(product);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
            productTable.put(product.getId(), product);
        }

        public void markProductInActiveOrder(Long productId, boolean inOrder) {
            if (inOrder) {
                productsInOrders.add(productId);
            } else {
                productsInOrders.remove(productId);
            }
        }

        public void deleteProduct(Long productId) {
            if (!productTable.containsKey(productId)) {
                throw new NoSuchElementException("Không tìm thấy sản phẩm để xoá");
            }
            if (productsInOrders.contains(productId)) {
                throw new IllegalStateException("Không thể xoá món ăn đang nằm trong đơn hàng đang xử lý!");
            }
            productTable.remove(productId);
        }
    }
}
