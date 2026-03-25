package phattrienungdungj2ee.example.ngonguyentiendat.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;
import phattrienungdungj2ee.example.ngonguyentiendat.model.ProductStockStatus;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.CategoryRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataSeeder(CategoryRepository categoryRepository,
                      ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0 || categoryRepository.count() > 0) {
            return;
        }

        Category phone = new Category();
        phone.setName("Điện thoại");
        phone.setSlug("dien-thoai");
        categoryRepository.save(phone);

        Category laptop = new Category();
        laptop.setName("Laptop");
        laptop.setSlug("laptop");
        categoryRepository.save(laptop);

        Category accessory = new Category();
        accessory.setName("Phụ kiện");
        accessory.setSlug("phu-kien");
        accessory.setMenuGroup("PHU_KIEN_DI_DONG");
        accessory.setImageUrl("/images/tab-flashsale.png");
        categoryRepository.save(accessory);

        saveProduct("Samsung Galaxy S25 Edge 5G 12GB/512GB", "samsung-galaxy-s25-edge",
                new BigDecimal("32890000"), 31, "DISCOUNT", 10, ProductStockStatus.CON_HANG,
                new BigDecimal("4.9"), "/images/product/p1.png", "Điện thoại Samsung cao cấp.", phone);

        saveProduct("OPPO Reno13 5G 12GB/256GB", "oppo-reno13-5g",
                new BigDecimal("15700000"), 22, "DISCOUNT", 10, ProductStockStatus.CON_HANG,
                new BigDecimal("4.8"), "/images/product/p2.png", "Điện thoại OPPO Reno.", phone);

        saveProduct("Samsung Galaxy A16 5G 8GB/256GB", "samsung-galaxy-a16-5g",
                new BigDecimal("6870000"), 17, "DISCOUNT", 20, ProductStockStatus.SAP_HET_HANG,
                new BigDecimal("4.7"), "/images/product/p3.png", "Điện thoại Samsung phổ thông.", phone);

        saveProduct("Máy in nhiệt HPRT FT800 Wifi", "may-in-nhiet-hprt-ft800",
                new BigDecimal("2290000"), 30, "DISCOUNT", 5, ProductStockStatus.CON_HANG,
                new BigDecimal("4.9"), "/images/product/p4.png", "Máy in nhiệt HPRT.", accessory);

        saveProduct("Chuột sạc Bluetooth Asus ROG Harpe Ace Mini", "chuot-asus-rog-harpe-ace-mini",
                new BigDecimal("2935000"), 6, "DISCOUNT", 5, ProductStockStatus.CON_HANG,
                new BigDecimal("4.9"), "/images/product/p5.png", "Chuột gaming Asus.", accessory);

        saveProduct("HONOR X9c Smart 5G 12GB/256GB", "honor-x9c-smart-5g",
                new BigDecimal("7850000"), 21, "DISCOUNT", 4, ProductStockStatus.SAP_HET_HANG,
                new BigDecimal("4.8"), "/images/product/p6.png", "Điện thoại Honor.", phone);

        saveProduct("realme 15 5G 12GB/256GB", "realme-15-5g",
                new BigDecimal("11490000"), 13, "DISCOUNT", 10, ProductStockStatus.CON_HANG,
                new BigDecimal("4.9"), "/images/product/p7.png", "Điện thoại Realme.", phone);

        saveProduct("MacBook Pro 14 inch Nano M5 16GB/512GB", "macbook-pro-14-m5",
                new BigDecimal("45690000"), 7, "DISCOUNT", 10, ProductStockStatus.CON_HANG,
                new BigDecimal("4.9"), "/images/product/p8.png", "MacBook Pro 14 inch.", laptop);

        saveProduct("Asus Vivobook S 16 OLED S5606MA Ultra 5 125H", "asus-vivobook-s16-oled",
                new BigDecimal("27490000"), 9, "DISCOUNT", 5, ProductStockStatus.CON_HANG,
                new BigDecimal("4.8"), "/images/product/p9.png", "Laptop Asus Vivobook.", laptop);

        saveProduct("Chuột Có dây Gaming Rapoo V10SE", "chuot-rapoo-v10se",
                new BigDecimal("105000"), 28, "DISCOUNT", 10, ProductStockStatus.CON_HANG,
                new BigDecimal("4.9"), "/images/product/p10.png", "Chuột gaming Rapoo.", accessory);

        saveProduct("Garmin Forerunner 55 42mm dây silicone", "garmin-forerunner-55",
                new BigDecimal("2990000"), 13, "DISCOUNT", 5, ProductStockStatus.CON_HANG,
                new BigDecimal("4.9"), "/images/product/p11.png", "Đồng hồ Garmin.", accessory);

        saveProduct("Xiaomi Redmi Note 14 Pro+ 5G 12GB/512GB", "xiaomi-redmi-note-14-pro-plus",
                new BigDecimal("12760000"), 28, "DISCOUNT", 10, ProductStockStatus.CON_HANG,
                new BigDecimal("4.8"), "/images/product/p12.png", "Điện thoại Xiaomi.", phone);
    }

    private void saveProduct(String name,
                             String slug,
                             BigDecimal originalPrice,
                             int discountPercent,
                             String promotionType,
                             Integer promotionStock,
                             ProductStockStatus stockStatus,
                             BigDecimal rating,
                             String image,
                             String description,
                             Category category) {

        Product product = new Product();
        product.setName(name);
        product.setSlug(slug);
        product.setOriginalPrice(originalPrice);
        product.setDiscountPercent(discountPercent);
        product.setPromotionType(promotionType);
        product.setPromotionStock(promotionStock);
        product.setPrice(calculateFinalPrice(originalPrice, promotionType, discountPercent));
        product.setStockStatus(stockStatus);
        product.setRating(rating);
        product.setThumbnailUrl(image);
        product.setDescription(description);
        product.setCategory(category);
        productRepository.save(product);
    }

    private BigDecimal calculateFinalPrice(BigDecimal originalPrice, String promotionType, Integer discountPercent) {
        if (!"DISCOUNT".equalsIgnoreCase(promotionType) || discountPercent == null || discountPercent <= 0) {
            return originalPrice;
        }
        return originalPrice.subtract(
                originalPrice.multiply(BigDecimal.valueOf(discountPercent))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );
    }
}