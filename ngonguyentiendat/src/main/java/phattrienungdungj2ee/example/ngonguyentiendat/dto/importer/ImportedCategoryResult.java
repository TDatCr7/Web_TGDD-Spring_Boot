package phattrienungdungj2ee.example.ngonguyentiendat.dto.importer;

public class ImportedCategoryResult {
    private final String name;
    private final String slug;
    private final String imageUrl;
    private final String sourceUrl;
    private final boolean imported;
    private final String message;

    public ImportedCategoryResult(String name, String slug, String imageUrl, String sourceUrl, boolean imported, String message) {
        this.name = name;
        this.slug = slug;
        this.imageUrl = imageUrl;
        this.sourceUrl = sourceUrl;
        this.imported = imported;
        this.message = message;
    }

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getImageUrl() { return imageUrl; }
    public String getSourceUrl() { return sourceUrl; }
    public boolean isImported() { return imported; }
    public String getMessage() { return message; }
}
