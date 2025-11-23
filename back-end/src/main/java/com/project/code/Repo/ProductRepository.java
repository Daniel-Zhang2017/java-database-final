package com.project.code.Repo;

import com.project.code.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find all products
     * @return list of all products
     */
    List<Product> findAll();

    /**
     * Find products by category
     * @param category the product category
     * @return list of products in the specified category
     */
    List<Product> findByCategory(String category);

    /**
     * Find products by category ignoring case
     * @param category the product category
     * @return list of products in the specified category (case-insensitive)
     */
    List<Product> findByCategoryIgnoreCase(String category);

    /**
     * Find products within a price range
     * @param minPrice the minimum price
     * @param maxPrice the maximum price
     * @return list of products within the price range
     */
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    /**
     * Find products with price less than specified value
     * @param price the maximum price
     * @return list of products with price less than specified value
     */
    List<Product> findByPriceLessThan(Double price);

    /**
     * Find products with price greater than specified value
     * @param price the minimum price
     * @return list of products with price greater than specified value
     */
    List<Product> findByPriceGreaterThan(Double price);

    /**
     * Find a product by SKU
     * @param sku the product SKU
     * @return the product with the specified SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find a product by name (exact match)
     * @param name the product name
     * @return the product with the specified name
     */
    Optional<Product> findByName(String name);

    /**
     * Find products by name containing the given string (case-insensitive)
     * @param name the name pattern to search for
     * @return list of products whose name contains the given string
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products by name pattern for a specific store
     * @param storeId the store ID
     * @param namePattern the name pattern to search for
     * @return list of products available in the specified store matching the name pattern
     */
    @Query("SELECT DISTINCT p FROM Product p JOIN p.inventories i WHERE i.store.id = :storeId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :namePattern, '%'))")
    List<Product> findByNameLikeInStore(@Param("storeId") Long storeId, @Param("namePattern") String namePattern);

    /**
     * Find products by category and price range
     * @param category the product category
     * @param minPrice the minimum price
     * @param maxPrice the maximum price
     * @return list of products matching both category and price range
     */
    List<Product> findByCategoryAndPriceBetween(String category, Double minPrice, Double maxPrice);

    /**
     * Find products by category and name containing the given string (case-insensitive)
     * @param category the product category
     * @param name the name pattern to search for
     * @return list of products matching both category and name pattern
     */
    List<Product> findByCategoryAndNameContainingIgnoreCase(String category, String name);
    
    /**
     * Check if a product exists with the given SKU
     * @param sku the SKU to check
     * @return true if a product with the SKU exists, false otherwise
     */
    boolean existsBySku(String sku);

    /**
     * Find products that are out of stock across all stores
     * @return list of products with zero or negative stock in all stores
     */
    @Query("SELECT DISTINCT p FROM Product p WHERE p.id NOT IN (SELECT i.product.id FROM Inventory i WHERE i.stockLevel > 0)")
    List<Product> findOutOfStockProducts();

    /**
     * Find products available in a specific store
     * @param storeId the store ID
     * @return list of products available in the specified store
     */
    @Query("SELECT DISTINCT p FROM Product p JOIN p.inventories i WHERE i.store.id = :storeId AND i.stockLevel > 0")
    List<Product> findProductsByStoreId(@Param("storeId") Long storeId);

    /**
     * Delete a product by SKU
     * @param sku the SKU of the product to delete
     */
    void deleteBySku(String sku);

    /**
     * Count products by category
     * @param category the product category
     * @return number of products in the category
     */
    long countByCategory(String category);
}