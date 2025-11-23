package com.project.code.Repo;

import com.project.code.Model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find an inventory record by product ID and store ID
     * @param productId the product ID
     * @param storeId the store ID
     * @return the inventory record matching both product and store
     */
    Optional<Inventory> findByProductIdAndStoreId(Long productId, Long storeId);

    /**
     * Find all inventory records for a specific store
     * @param storeId the store ID
     * @return list of inventory records for the specified store
     */
    List<Inventory> findByStoreId(Long storeId);

    /**
     * Find all inventory records for a specific product
     * @param productId the product ID
     * @return list of inventory records for the specified product
     */
    List<Inventory> findByProductId(Long productId);

    /**
     * Find inventory records by store ID with stock level greater than specified value
     * @param storeId the store ID
     * @param stockLevel the minimum stock level
     * @return list of inventory records meeting the criteria
     */
    List<Inventory> findByStoreIdAndStockLevelGreaterThan(Long storeId, Integer stockLevel);

    /**
     * Find inventory records by product ID with stock level less than specified value
     * @param productId the product ID
     * @param stockLevel the maximum stock level
     * @return list of inventory records meeting the criteria
     */
    List<Inventory> findByProductIdAndStockLevelLessThan(Long productId, Integer stockLevel);

    /**
     * Find inventory records with low stock (stock level less than or equal to threshold)
     * @param threshold the stock level threshold
     * @return list of inventory records with low stock
     */
    List<Inventory> findByStockLevelLessThanEqual(Integer threshold);

    /**
     * Delete all inventory records for a specific product
     * @param productId the product ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Inventory i WHERE i.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    /**
     * Delete all inventory records for a specific store
     * @param storeId the store ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Inventory i WHERE i.store.id = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);

    /**
     * Update stock level for a specific product and store
     * @param productId the product ID
     * @param storeId the store ID
     * @param newStockLevel the new stock level
     * @return number of records updated
     */
    @Modifying
    @Transactional
    @Query("UPDATE Inventory i SET i.stockLevel = :newStockLevel WHERE i.product.id = :productId AND i.store.id = :storeId")
    int updateStockLevel(@Param("productId") Long productId, @Param("storeId") Long storeId, @Param("newStockLevel") Integer newStockLevel);

    /**
     * Check if inventory record exists for product and store
     * @param productId the product ID
     * @param storeId the store ID
     * @return true if record exists, false otherwise
     */
    boolean existsByProductIdAndStoreId(Long productId, Long storeId);

    /**
     * Get total stock quantity for a specific product across all stores
     * @param productId the product ID
     * @return total stock quantity
     */
    @Query("SELECT SUM(i.stockLevel) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalStockByProductId(@Param("productId") Long productId);

    /**
     * Get total stock quantity for a specific store across all products
     * @param storeId the store ID
     * @return total stock quantity
     */
    @Query("SELECT SUM(i.stockLevel) FROM Inventory i WHERE i.store.id = :storeId")
    Integer getTotalStockByStoreId(@Param("storeId") Long storeId);
}