package com.poly.sof309.controllers;

import org.example.models.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =========================================================================
 * [Y3 - TUẦN 2] UNIT TEST CHO PHÂN HỆ DANH MỤC SẢN PHẨM (CategoryController)
 * Sinh viên: Phùng Bảo Bảo - MSSV: TY00366 - Lớp: SD21308
 * =========================================================================
 */
@DisplayName("Unit Test Phân hệ Category Controller (Shop Category CRUD)")
public class CategoryControllerTest {

    private CategoryServiceMock categoryService;

    @BeforeEach
    public void setUp() {
        categoryService = new CategoryServiceMock();
    }

    // =========================================================================
    // 1. KIỂM THỬ THAM SỐ HOÁ GIÁ TRỊ BIÊN DANH MỤC (@ParameterizedTest + @CsvSource)
    // =========================================================================

    @ParameterizedTest(name = "[{index}] ShopId={0}, Name=''{1}'' -> Expected: {2} ({3})")
    @DisplayName("TC_CAT_BVA: Kiểm tra validation biên các trường Danh mục")
    @CsvSource({
            // Biên ShopId
            "0, 'Cà phê rang xay', false, 'Shop ID bằng 0 (Lỗi biên)'",
            "-1, 'Cà phê rang xay', false, 'Shop ID âm (Lỗi biên)'",
            "1, 'Cà phê rang xay', true, 'Shop ID dương hợp lệ'",

            // Biên Tên Category
            "1, '', false, 'Tên danh mục rỗng'",
            "1, '   ', false, 'Tên danh mục chỉ có khoảng trắng'",
            "1, 'Trà Trái Cây', true, 'Tên danh mục hợp lệ'",
            "1, 'Trà Sữa & Topping Đặc Biệt POB 2026', true, 'Tên danh mục độ dài lớn hợp lệ'"
    })
    public void testCategoryValidationWithCsvSource(long shopId, String categoryName,
                                                    boolean expectedSuccess, String message) {
        Category cat = new Category();
        cat.setShopId(shopId);
        cat.setCategoryName(categoryName);

        String error = categoryService.validateCategory(cat);
        boolean actualSuccess = (error == null);
        assertEquals(expectedSuccess, actualSuccess, message);
    }

    // =========================================================================
    // 2. KIỂM THỬ NGOẠI LỆ NGHIỆP VỤ (Exception Testing với assertThrows)
    // =========================================================================

    @Test
    @DisplayName("TC_EXP_CAT_01: Ném IllegalArgumentException khi tạo Category với ShopId <= 0")
    public void testCreateCategoryInvalidShopIdThrowsException() {
        Category invalidCat = new Category();
        invalidCat.setShopId(0L);
        invalidCat.setCategoryName("Nước Ép Trái Cây");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.createCategory(invalidCat);
        }, "Phải ném IllegalArgumentException khi ShopId <= 0");

        assertEquals("Shop ID khong hop le", ex.getMessage());
    }

    @Test
    @DisplayName("TC_EXP_CAT_02: Ném IllegalArgumentException khi tạo Category để trống tên")
    public void testCreateCategoryEmptyNameThrowsException() {
        Category emptyCat = new Category();
        emptyCat.setShopId(1L);
        emptyCat.setCategoryName("   ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.createCategory(emptyCat);
        });

        assertEquals("Ten category khong duoc de trong", ex.getMessage());
    }

    @Test
    @DisplayName("TC_EXP_CAT_03: Ném IllegalStateException khi xoá Category đang chứa sản phẩm")
    public void testDeleteCategoryWithActiveProductsThrowsException() {
        Category cat = new Category();
        cat.setId(5L);
        cat.setShopId(1L);
        cat.setCategoryName("Đồ Uống Đá Xay");
        categoryService.createCategory(cat);

        // Giả lập danh mục đang có sản phẩm liên kết
        categoryService.setProductCountForCategory(5L, 3);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            categoryService.deleteCategory(5L);
        }, "Phải ném IllegalStateException khi xoá danh mục còn sản phẩm");

        assertTrue(ex.getMessage().contains("Không thể xoá danh mục đang chứa 3 sản phẩm"));
    }

    @Test
    @DisplayName("TC_EXP_CAT_04: Ném NoSuchElementException khi cập nhật danh mục không tồn tại")
    public void testUpdateNonExistentCategoryThrowsException() {
        Category nonExistent = new Category();
        nonExistent.setId(99999L);
        nonExistent.setShopId(1L);
        nonExistent.setCategoryName("Danh mục không có");

        assertThrows(NoSuchElementException.class, () -> {
            categoryService.updateCategory(nonExistent);
        });
    }

    // =========================================================================
    // LỚP GIẢ LẬP NGHIỆP VỤ (Service Layer Mocking CategoryController)
    // =========================================================================
    public static class CategoryServiceMock {
        private final java.util.Map<Long, Category> categoryTable = new java.util.HashMap<>();
        private final java.util.Map<Long, Integer> categoryProductCounts = new java.util.HashMap<>();

        public String validateCategory(Category category) {
            if (category.getShopId() <= 0) {
                return "Shop ID khong hop le";
            }
            if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
                return "Ten category khong duoc de trong";
            }
            return null;
        }

        public void createCategory(Category category) {
            String error = validateCategory(category);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
            long id = category.getId() > 0 ? category.getId() : (categoryTable.size() + 1);
            category.setId(id);
            categoryTable.put(id, category);
        }

        public void updateCategory(Category category) {
            if (category.getId() <= 0 || !categoryTable.containsKey(category.getId())) {
                throw new NoSuchElementException("Không tìm thấy Category có ID: " + category.getId());
            }
            String error = validateCategory(category);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
            categoryTable.put(category.getId(), category);
        }

        public void setProductCountForCategory(Long catId, int count) {
            categoryProductCounts.put(catId, count);
        }

        public void deleteCategory(Long catId) {
            if (!categoryTable.containsKey(catId)) {
                throw new NoSuchElementException("Không tìm thấy Category để xoá");
            }
            int count = categoryProductCounts.getOrDefault(catId, 0);
            if (count > 0) {
                throw new IllegalStateException("Không thể xoá danh mục đang chứa " + count + " sản phẩm!");
            }
            categoryTable.remove(catId);
        }
    }
}
