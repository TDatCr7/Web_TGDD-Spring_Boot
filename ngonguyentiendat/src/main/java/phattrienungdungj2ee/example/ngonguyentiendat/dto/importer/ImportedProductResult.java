package phattrienungdungj2ee.example.ngonguyentiendat.dto.importer;

import java.math.BigDecimal;

public class ImportedProductResult {
    private final String name;
    private final String slug;
    private final BigDecimal price;
    private final String imageUrl;
    private final String sourceUrl;
    private final boolean imported;
    private final String message;

    public ImportedProductResult(String name, String slug, BigDecimal price, String imageUrl, String sourceUrl, boolean imported, String message) {
        this.name = name;
        this.slug = slug;
        this.price = price;
        this.imageUrl = imageUrl;
        this.sourceUrl = sourceUrl;
        this.imported = imported;
        this.message = message;
    }

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getSourceUrl() { return sourceUrl; }
    public boolean isImported() { return imported; }
    public String getMessage() { return message; }
}
