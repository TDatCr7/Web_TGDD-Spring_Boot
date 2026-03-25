package phattrienungdungj2ee.example.ngonguyentiendat.service.importer;

import phattrienungdungj2ee.example.ngonguyentiendat.dto.importer.ImportedCategoryResult;
import phattrienungdungj2ee.example.ngonguyentiendat.dto.importer.ImportedProductResult;

import java.util.List;

public interface ExternalCatalogScraperService {
    List<ImportedCategoryResult> importCategoriesFromUrl(String url);
    List<ImportedProductResult> importProductsFromCategoryUrl(String url, Long categoryId, int limit);
}
