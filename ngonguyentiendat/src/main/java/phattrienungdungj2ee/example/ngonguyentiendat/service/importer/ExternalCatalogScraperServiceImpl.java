package phattrienungdungj2ee.example.ngonguyentiendat.service.importer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.importer.ImportedCategoryResult;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.importer.ImportedProductResult;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Category;
import phattrienungdungj2ee.example.ngonguyentiendat.model.Product;
import phattrienungdungj2ee.example.ngonguyentiendat.model.ProductStockStatus;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.CategoryRepository;
import phattrienungdungj2ee.example.ngonguyentiendat.repository.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ExternalCatalogScraperServiceImpl implements ExternalCatalogScraperService {

    private static final int TIMEOUT_MILLIS = (int) Duration.ofSeconds(20).toMillis();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0 Safari/537.36";
    private static final Set<String> PRODUCT_SEGMENTS = Set.of(
            "dtdd", "laptop", "may-tinh-bang", "tai-nghe", "dong-ho-thong-minh", "dong-ho", "smartwatch",
            "sac-du-phong", "cap-sac", "chuot-may-tinh", "ban-phim", "loa", "camera", "router", "usb", "o-cung"
    );

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ExternalCatalogScraperServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<ImportedCategoryResult> importCategoriesFromUrl(String url) {
        Document doc = fetch(url);
        URI baseUri = URI.create(doc.location());

        Map<String, ImportedCategoryCandidate> candidates = new LinkedHashMap<>();
        for (Element link : doc.select("a[href]")) {
            String href = normalizeUrl(baseUri, link.attr("href"));
            if (href == null || !isSameHost(baseUri, href)) {
                continue;
            }

            String path = pathOf(href);
            if (path == null || path.isBlank() || path.equals("/")) {
                continue;
            }

            String[] segments = trimmedSegments(path);
            if (segments.length != 1) {
                continue;
            }

            String key = segments[0].toLowerCase(Locale.ROOT);
            if (!PRODUCT_SEGMENTS.contains(key)) {
                continue;
            }

            String name = cleanText(link.text());
            if (name.isBlank()) {
                name = inferCategoryName(key);
            }

            String imageUrl = firstNonBlank(
                    absUrl(link, "img[src]", "src"),
                    absUrl(link, "img[data-src]", "data-src"),
                    absUrl(link, "img[data-original]", "data-original")
            );

            String menuGroup = inferMenuGroup(key, name);
            candidates.putIfAbsent(key, new ImportedCategoryCandidate(name, key, imageUrl, href, menuGroup));
        }

        List<ImportedCategoryResult> results = new ArrayList<>();
        for (ImportedCategoryCandidate candidate : candidates.values()) {
            if (candidate.name == null || candidate.name.isBlank()) {
                continue;
            }
            Optional<Category> bySlug = categoryRepository.findBySlug(candidate.slug);
            if (bySlug.isPresent()) {
                results.add(new ImportedCategoryResult(candidate.name, candidate.slug, candidate.imageUrl, candidate.sourceUrl, false, "Slug đã tồn tại"));
                continue;
            }
            if (categoryRepository.existsByName(candidate.name)) {
                results.add(new ImportedCategoryResult(candidate.name, candidate.slug, candidate.imageUrl, candidate.sourceUrl, false, "Tên danh mục đã tồn tại"));
                continue;
            }

            Category category = new Category();
            category.setName(candidate.name);
            category.setSlug(candidate.slug);
            category.setMenuGroup(candidate.menuGroup);
            category.setImageUrl(candidate.imageUrl);
            categoryRepository.save(category);

            results.add(new ImportedCategoryResult(category.getName(), category.getSlug(), category.getImageUrl(), candidate.sourceUrl, true, "Đã import"));
        }
        return results;
    }
    private String normalizeImportedImage(String imageUrl) {
        String value = firstNonBlank(imageUrl, "/images/product/default-product.png");
        if (value.isBlank()) {
            return "/images/product/default-product.png";
        }
        return value;
    }
    @Override
    public List<ImportedProductResult> importProductsFromCategoryUrl(String url, Long categoryId, int limit) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục đích."));

        int safeLimit = Math.max(1, Math.min(limit, 100));
        Document doc = fetch(url);
        URI baseUri = URI.create(doc.location());

        List<ListingProductCandidate> listingProducts = extractListingProducts(doc, baseUri, category, safeLimit);

        List<ImportedProductResult> results = new ArrayList<>();
        Set<String> seenNamesInBatch = new LinkedHashSet<>();
        Set<String> seenSlugsInBatch = new LinkedHashSet<>();

        for (ListingProductCandidate listing : listingProducts) {
            try {
                ProductCandidate candidate = enrichProductFromDetail(listing, category);

                if (candidate.name == null || candidate.name.isBlank()) {
                    results.add(new ImportedProductResult("", "", BigDecimal.ZERO, "", listing.sourceUrl, false, "Không đọc được tên sản phẩm"));
                    continue;
                }

                String normalizedName = normalizeProductIdentity(candidate.name);
                String normalizedSlugBase = normalizeProductIdentity(candidate.slugBase);

                if (normalizedName.isBlank() || normalizedSlugBase.isBlank()) {
                    results.add(new ImportedProductResult(candidate.name, candidate.slugBase, candidate.price, candidate.imageUrl, listing.sourceUrl, false, "Không chuẩn hóa được sản phẩm"));
                    continue;
                }

                if (seenNamesInBatch.contains(normalizedName) || seenSlugsInBatch.contains(normalizedSlugBase)) {
                    results.add(new ImportedProductResult(candidate.name, candidate.slugBase, candidate.price, candidate.imageUrl, listing.sourceUrl, false, "Sản phẩm bị trùng trong danh sách import"));
                    continue;
                }

                if (productRepository.existsByNameIgnoreCase(candidate.name)
                        || productRepository.existsBySlug(candidate.slugBase)
                        || productRepository.existsBySlugStartingWith(candidate.slugBase + "-")) {
                    results.add(new ImportedProductResult(candidate.name, candidate.slugBase, candidate.price, candidate.imageUrl, listing.sourceUrl, false, "Sản phẩm đã tồn tại"));
                    continue;
                }

                String finalSlug = ensureUniqueProductSlug(candidate.slugBase);

                Product product = new Product();
                product.setName(candidate.name);
                product.setSlug(finalSlug);
                product.setPrice(candidate.price);
                product.setOriginalPrice(candidate.originalPrice);
                product.setThumbnailUrl(normalizeImportedImage(candidate.imageUrl));
                product.setDescription(candidate.description);
                product.setCategory(category);
                product.setPromotionType("NONE");
                product.setPromotionStock(null);
                product.setStockStatus(ProductStockStatus.CON_HANG);
                product.setRating(candidate.rating);
                productRepository.save(product);

                seenNamesInBatch.add(normalizedName);
                seenSlugsInBatch.add(normalizedSlugBase);

                results.add(new ImportedProductResult(
                        product.getName(),
                        product.getSlug(),
                        product.getPrice(),
                        product.getThumbnailUrl(),
                        listing.sourceUrl,
                        true,
                        "Đã import"
                ));
            } catch (Exception ex) {
                results.add(new ImportedProductResult("", "", BigDecimal.ZERO, "", listing.sourceUrl, false, "Lỗi đọc trang: " + ex.getMessage()));
            }
        }

        return results;
    }
    private static class ListingProductCandidate {
        private final String name;
        private final String sourceUrl;
        private final BigDecimal price;
        private final BigDecimal originalPrice;
        private final String imageUrl;
        private final BigDecimal rating;

        private ListingProductCandidate(String name,
                                        String sourceUrl,
                                        BigDecimal price,
                                        BigDecimal originalPrice,
                                        String imageUrl,
                                        BigDecimal rating) {
            this.name = name;
            this.sourceUrl = sourceUrl;
            this.price = price;
            this.originalPrice = originalPrice;
            this.imageUrl = imageUrl;
            this.rating = rating;
        }
    }
    private List<ListingProductCandidate> extractListingProducts(Document doc, URI baseUri, Category category, int limit) {
        List<ListingProductCandidate> items = new ArrayList<>();
        Set<String> seenUrls = new LinkedHashSet<>();

        Elements productAnchors = doc.select(
                "a.main-contain[href], " +
                        "li.item a[href], .item a[href], " +
                        ".listproduct a[href], .listProduct a[href], " +
                        ".product-item a[href], .product-list a[href], " +
                        "[data-selenium='product-link'][href], " +
                        "[data-name='product'] a[href]"
        );

        if (productAnchors.size() < limit) {
            Elements fallbackAnchors = doc.select("a[href]");
            for (Element anchor : fallbackAnchors) {
                if (!productAnchors.contains(anchor)) {
                    productAnchors.add(anchor);
                }
            }
        }

        for (Element anchor : productAnchors) {
            if (items.size() >= limit) {
                break;
            }

            String href = normalizeUrl(baseUri, anchor.attr("href"));
            if (href == null || !isSameHost(baseUri, href)) {
                continue;
            }

            String path = pathOf(href);
            if (path == null || path.isBlank() || path.equals("/")) {
                continue;
            }

            String[] segments = trimmedSegments(path);
            if (segments.length < 2) {
                continue;
            }

            String firstSegment = segments[0].toLowerCase(Locale.ROOT);
            if (!PRODUCT_SEGMENTS.contains(firstSegment)) {
                continue;
            }

            if (!sameCategoryFamily(category, firstSegment)) {
                continue;
            }

            if (!seenUrls.add(href)) {
                continue;
            }

            String rawName = firstNonBlank(
                    cleanText(anchor.select("h3").text()),
                    cleanText(anchor.select(".item-txt-online").text()),
                    cleanText(anchor.attr("title")),
                    cleanText(anchor.text())
            );

            String name = sanitizeProductName(rawName);
            if (name.isBlank()) {
                continue;
            }

            String priceText = firstNonBlank(
                    cleanText(anchor.select(".price").text()),
                    cleanText(anchor.select(".price-current").text()),
                    cleanText(anchor.select(".box-price-present").text()),
                    cleanText(anchor.select(".price-sale").text()),
                    cleanText(anchor.text())
            );

            BigDecimal price = parseMoney(priceText);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            String originalPriceText = firstNonBlank(
                    cleanText(anchor.select(".price-old").text()),
                    cleanText(anchor.select(".box-price-old").text()),
                    cleanText(anchor.select(".old-price").text())
            );
            BigDecimal originalPrice = parseMoney(originalPriceText);
            if (originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                originalPrice = price;
            }

            String imageUrl = extractBestListingImage(anchor, baseUri);

            String ratingText = firstNonBlank(
                    cleanText(anchor.select(".vote-txt").text()),
                    cleanText(anchor.select(".rating").text())
            );

            items.add(new ListingProductCandidate(
                    name,
                    href,
                    price,
                    originalPrice,
                    imageUrl,
                    parseRating(ratingText)
            ));
        }

        return items;
    }

    private String extractBestListingImage(Element anchor, URI baseUri) {
        Elements scopedImages = anchor.select(".item-img img, picture img, img.thumb");
        Elements images = scopedImages.isEmpty() ? anchor.select("img") : scopedImages;

        String best = "";
        int bestScore = Integer.MIN_VALUE;

        for (Element img : images) {
            String candidate = normalizeImageCandidate(baseUri, firstNonBlank(
                    cleanText(img.attr("data-src")),
                    cleanText(img.attr("data-original")),
                    cleanText(img.attr("src")),
                    cleanText(img.attr("data-lazy-src")),
                    cleanText(img.attr("srcset"))
            ));
            if (candidate.isBlank() || isBannerLikeImage(candidate)) {
                continue;
            }

            String lower = candidate.toLowerCase(Locale.ROOT);
            String alt = normalizeProductIdentity(firstNonBlank(img.attr("alt"), img.attr("title")));
            String anchorName = normalizeProductIdentity(firstNonBlank(
                    cleanText(anchor.select("h3").text()),
                    cleanText(anchor.attr("title"))
            ));

            int score = 0;
            if (looksLikeProductImage(lower)) {
                score += 5;
            }
            if (!anchorName.isBlank() && !alt.isBlank()) {
                if (alt.equals(anchorName)) {
                    score += 20;
                } else if (alt.contains(anchorName) || anchorName.contains(alt)) {
                    score += 12;
                }
            }
            if (img.parents().stream().anyMatch(el -> el.className().toLowerCase(Locale.ROOT).contains("item-img"))) {
                score += 10;
            }
            if (lower.contains("thumb") || lower.contains("avatar") || lower.contains("icon")) {
                score -= 5;
            }

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best;
    }

    private boolean looksLikeProductImage(String url) {
        return url.contains("/products/")
                || url.contains("/images/")
                || url.contains("/img/")
                || url.contains("cdn.tgdd.vn")
                || url.contains(".jpg")
                || url.contains(".jpeg")
                || url.contains(".png")
                || url.contains(".webp");
    }

    private ProductCandidate enrichProductFromDetail(ListingProductCandidate listing, Category category) {
        String finalName = listing.name;
        String finalImage = listing.imageUrl;
        String finalDescription = "";
        BigDecimal finalRating = listing.rating != null ? listing.rating : new BigDecimal("4.9");

        try {
            Document doc = fetch(listing.sourceUrl);

            String detailName = firstNonBlank(
                    cleanText(doc.select("h1").text()),
                    metaContent(doc, "meta[property=og:title]")
            );
            detailName = sanitizeProductName(detailName);

            if (!detailName.isBlank() && looksLikeRealProductName(detailName)) {
                finalName = detailName;
            }

            // TGDD thường có banner/promo trong trang chi tiết nên luôn ưu tiên ảnh card ở trang danh mục.
            // Chỉ fallback sang trang chi tiết nếu listing không lấy được ảnh nào hợp lệ.
            if (finalImage == null || finalImage.isBlank()) {
                String detailImage = extractBestDetailImage(doc, finalName, listing.sourceUrl);
                if (!detailImage.isBlank()) {
                    finalImage = detailImage;
                }
            }

            finalDescription = firstNonBlank(
                    metaContent(doc, "meta[name=description]"),
                    collectParagraphs(doc.select(".description p, .article-content p, .box-detail p, .content-article p"), 3)
            );

            String detailRatingText = firstNonBlank(
                    ownText(doc, ".vote-txt"),
                    cleanText(doc.select(".point, .rating-score, .comment-point").text())
            );
            if (!detailRatingText.isBlank()) {
                finalRating = parseRating(detailRatingText);
            }
        } catch (Exception ignored) {
        }

        String slugBase = slugify(firstNonBlank(finalName, category.getSlug() + "-item"));

        return new ProductCandidate(
                finalName,
                slugBase,
                listing.price,
                listing.originalPrice,
                finalImage,
                finalDescription,
                finalRating
        );
    }

    private String extractBestDetailImage(Document doc, String productName, String pageUrl) {
        URI baseUri = URI.create(doc.location().isBlank() ? pageUrl : doc.location());
        String normalizedName = normalizeProductIdentity(productName);

        Element heroImage = doc.selectFirst(".detail-slider img, .owl-carousel img, .swiper-slide img, .gallery-img img, .product-slider img");
        String best = "";
        int bestScore = Integer.MIN_VALUE;

        for (Element img : doc.select(".detail-slider img, .owl-carousel img, .swiper-slide img, .gallery-img img, .product-slider img, picture img")) {
            String candidate = firstNonBlank(
                    cleanText(img.attr("data-src")),
                    cleanText(img.attr("data-original")),
                    cleanText(img.attr("data-lazy-src")),
                    cleanText(img.attr("srcset")),
                    cleanText(img.attr("src"))
            );

            candidate = normalizeImageCandidate(baseUri, candidate);
            if (candidate.isBlank() || isBannerLikeImage(candidate)) {
                continue;
            }

            int score = 0;
            String lower = candidate.toLowerCase(Locale.ROOT);
            String alt = normalizeProductIdentity(firstNonBlank(img.attr("alt"), img.attr("title"), img.attr("aria-label")));
            String parentClass = img.parent() != null ? normalizeProductIdentity(img.parent().className()) : "";

            if (heroImage != null && (img == heroImage || img.parent() == heroImage.parent())) {
                score += 5;
            }
            if (looksLikeProductImage(lower)) {
                score += 4;
            }
            if (!normalizedName.isBlank() && !alt.isBlank()) {
                if (alt.equals(normalizedName)) {
                    score += 25;
                } else if (alt.contains(normalizedName) || normalizedName.contains(alt)) {
                    score += 15;
                }
            }
            if (parentClass.contains("slide") || parentClass.contains("gallery") || parentClass.contains("owl") || parentClass.contains("swiper")) {
                score += 4;
            }
            if (lower.contains("/product/") || lower.contains("/products/")) {
                score += 3;
            }
            if (lower.contains("thumb") || lower.contains("avatar") || lower.contains("icon")) {
                score -= 4;
            }

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best;
    }

    private Document fetch(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .referrer("https://www.google.com/")
                    .header("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
                    .timeout(TIMEOUT_MILLIS)
                    .get();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tải URL: " + url, ex);
        }
    }

    private String metaContent(Document doc, String selector) {
        Element element = doc.selectFirst(selector);
        return element != null ? cleanText(element.attr("content")) : "";
    }

    private String ownText(Document doc, String selector) {
        Element element = doc.selectFirst(selector);
        return element != null ? cleanText(element.text()) : "";
    }

    private String absUrl(Element scope, String selector, String attr) {
        Element element = scope.selectFirst(selector);
        if (element == null) {
            return "";
        }
        String value = firstNonBlank(element.absUrl(attr), element.attr(attr));
        return value == null ? "" : value.trim();
    }

    private String cleanText(String input) {
        return input == null ? "" : input.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private String collectParagraphs(Elements paragraphs, int maxParagraphs) {
        List<String> chunks = new ArrayList<>();
        for (Element paragraph : paragraphs) {
            String text = cleanText(paragraph.text());
            if (!text.isBlank()) {
                chunks.add(text);
            }
            if (chunks.size() >= maxParagraphs) {
                break;
            }
        }
        return String.join(" ", chunks);
    }

    private BigDecimal parseMoney(String text) {
        String digits = text == null ? "" : text.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(digits);
    }

    private BigDecimal parseRating(String text) {
        if (text == null || text.isBlank()) {
            return new BigDecimal("4.9");
        }
        String normalized = text.replace(',', '.');
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("([0-5](?:\\.[0-9])?)").matcher(normalized);
        if (!matcher.find()) {
            return new BigDecimal("4.9");
        }
        return new BigDecimal(matcher.group(1)).setScale(1, RoundingMode.HALF_UP);
    }

    private boolean isSameHost(URI baseUri, String url) {
        try {
            URI uri = URI.create(url);
            return uri.getHost() != null && baseUri.getHost() != null && uri.getHost().equalsIgnoreCase(baseUri.getHost());
        } catch (Exception ex) {
            return false;
        }
    }

    private String normalizeUrl(URI baseUri, String href) {
        if (href == null || href.isBlank()) {
            return null;
        }
        String trimmed = href.trim();
        if (trimmed.startsWith("javascript:") || trimmed.startsWith("mailto:") || trimmed.startsWith("tel:")) {
            return null;
        }
        try {
            URI resolved = baseUri.resolve(trimmed);
            String scheme = resolved.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return null;
            }
            String normalized = resolved.toString();
            int hashIndex = normalized.indexOf('#');
            if (hashIndex >= 0) {
                normalized = normalized.substring(0, hashIndex);
            }
            return normalized;
        } catch (Exception ex) {
            return null;
        }
    }

    private String pathOf(String url) {
        try {
            return URI.create(url).getPath();
        } catch (Exception ex) {
            return null;
        }
    }

    private String[] trimmedSegments(String path) {
        return path.replaceAll("^/+|/+$", "").split("/");
    }

    private String inferCategoryName(String key) {
        return switch (key) {
            case "dtdd" -> "Điện thoại";
            case "laptop" -> "Laptop";
            case "may-tinh-bang" -> "Máy tính bảng";
            case "tai-nghe" -> "Tai nghe";
            case "dong-ho-thong-minh", "dong-ho", "smartwatch" -> "Đồng hồ thông minh";
            case "sac-du-phong" -> "Sạc dự phòng";
            case "cap-sac" -> "Cáp sạc";
            case "chuot-may-tinh" -> "Chuột máy tính";
            case "ban-phim" -> "Bàn phím";
            case "loa" -> "Loa";
            case "camera" -> "Camera";
            case "router" -> "Router";
            case "usb" -> "USB";
            case "o-cung" -> "Ổ cứng";
            default -> key.replace('-', ' ');
        };
    }

    private String inferMenuGroup(String key, String name) {
        String source = (key + " " + name).toLowerCase(Locale.ROOT);
        if (source.contains("tai nghe") || source.contains("loa")) return "THIET_BI_AM_THANH";
        if (source.contains("camera")) return "CAMERA";
        if (source.contains("chuot") || source.contains("ban phim") || source.contains("laptop") || source.contains("usb") || source.contains("o cung")) {
            return "PHU_KIEN_LAPTOP_PC";
        }
        if (source.contains("dong ho") || source.contains("sac") || source.contains("cap")) return "PHU_KIEN_DI_DONG";
        return "PHU_KIEN_KHAC";
    }

    private String ensureUniqueProductSlug(String base) {
        String normalizedBase = base == null || base.isBlank() ? "san-pham" : base;
        String candidate = normalizedBase;
        int index = 1;
        while (productRepository.existsBySlug(candidate)) {
            candidate = normalizedBase + "-" + index++;
        }
        return candidate;
    }

    private String slugify(String input) {
        if (input == null) return "";
        return input.toLowerCase(Locale.ROOT)
                .replace("đ", "d")
                .replace("á", "a").replace("à", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a")
                .replace("ă", "a").replace("ắ", "a").replace("ằ", "a").replace("ẳ", "a").replace("ẵ", "a").replace("ặ", "a")
                .replace("â", "a").replace("ấ", "a").replace("ầ", "a").replace("ẩ", "a").replace("ẫ", "a").replace("ậ", "a")
                .replace("é", "e").replace("è", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e")
                .replace("ê", "e").replace("ế", "e").replace("ề", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
                .replace("í", "i").replace("ì", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i")
                .replace("ó", "o").replace("ò", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o")
                .replace("ô", "o").replace("ố", "o").replace("ồ", "o").replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
                .replace("ơ", "o").replace("ớ", "o").replace("ờ", "o").replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
                .replace("ú", "u").replace("ù", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u")
                .replace("ư", "u").replace("ứ", "u").replace("ừ", "u").replace("ử", "u").replace("ữ", "u").replace("ự", "u")
                .replace("ý", "y").replace("ỳ", "y").replace("ỷ", "y").replace("ỹ", "y").replace("ỵ", "y")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private String normalizeProductIdentity(String value) {
        return slugify(cleanText(value));
    }

    private boolean isBannerLikeImage(String url) {
        String lower = url == null ? "" : url.toLowerCase(Locale.ROOT);
        return lower.isBlank()
                || lower.startsWith("data:")
                || lower.contains("banner")
                || lower.contains("promo")
                || lower.contains("voucher")
                || lower.contains("ads")
                || lower.contains("campaign")
                || lower.contains("event")
                || lower.contains("slider")
                || lower.contains("site-ver")
                || lower.contains("sticky")
                || lower.contains("label")
                || lower.contains("sticker")
                || lower.contains("frame")
                || lower.contains("icon")
                || lower.contains("gift");
    }

    private String normalizeImageCandidate(URI baseUri, String raw) {
        String value = cleanText(raw);
        if (value.isBlank()) {
            return "";
        }

        if (value.contains(",")) {
            String[] srcsetParts = value.split(",");
            value = srcsetParts[srcsetParts.length - 1].trim().split("\s+")[0];
        }

        String normalized = normalizeUrl(baseUri, value);
        return normalized == null ? "" : normalized;
    }

    private String firstNonBlank(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static class ImportedCategoryCandidate {
        private final String name;
        private final String slug;
        private final String imageUrl;
        private final String sourceUrl;
        private final String menuGroup;

        private ImportedCategoryCandidate(String name, String slug, String imageUrl, String sourceUrl, String menuGroup) {
            this.name = name;
            this.slug = slug;
            this.imageUrl = imageUrl;
            this.sourceUrl = sourceUrl;
            this.menuGroup = menuGroup;
        }
    }

    private static class ProductCandidate {
        private final String name;
        private final String slugBase;
        private final BigDecimal price;
        private final BigDecimal originalPrice;
        private final String imageUrl;
        private final String description;
        private final BigDecimal rating;

        private ProductCandidate(String name, String slugBase, BigDecimal price, BigDecimal originalPrice, String imageUrl, String description, BigDecimal rating) {
            this.name = name;
            this.slugBase = slugBase;
            this.price = price;
            this.originalPrice = originalPrice;
            this.imageUrl = imageUrl;
            this.description = description;
            this.rating = rating;
        }
    }

    private boolean sameCategoryFamily(Category category, String sourceSegment) {
        if (category == null || category.getSlug() == null) {
            return true;
        }

        String target = category.getSlug().toLowerCase(Locale.ROOT);

        if (target.contains("dien-thoai") || target.equals("dtdd")) {
            return sourceSegment.equals("dtdd");
        }
        if (target.contains("tai-nghe")) {
            return sourceSegment.equals("tai-nghe");
        }
        if (target.contains("laptop")) {
            return sourceSegment.equals("laptop");
        }
        if (target.contains("may-tinh-bang")) {
            return sourceSegment.equals("may-tinh-bang");
        }
        if (target.contains("dong-ho")) {
            return sourceSegment.equals("dong-ho") || sourceSegment.equals("dong-ho-thong-minh") || sourceSegment.equals("smartwatch");
        }

        return true;
    }

    private String sanitizeProductName(String raw) {
        String text = cleanText(raw);
        if (text.isBlank()) {
            return "";
        }

        text = text.replaceAll("(?i)\\b(trả chậm|trả trước|mẫu mới|hotsale|online giá rẻ quá|quà.*)$", "").trim();
        text = text.replaceAll("(?i)\\bgiá tốt.*$", "").trim();
        text = text.replaceAll("(?i)\\bgiảm ngay.*$", "").trim();
        text = text.replaceAll("(?i)\\bthu cũ.*$", "").trim();
        text = text.replaceAll("(?i)\\bmua ngay.*$", "").trim();
        text = text.replaceAll("\\s{2,}", " ").trim();

        return text;
    }

    private boolean looksLikeRealProductName(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return !lower.contains("giảm ngay")
                && !lower.contains("mua ngay")
                && !lower.contains("trợ giá")
                && !lower.contains("thu cũ")
                && !lower.contains("trả chậm");
    }
}
