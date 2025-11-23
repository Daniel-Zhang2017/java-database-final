package com.project.code.Repo;

import com.project.code.Model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * Find a store by ID
     * @param id the store ID
     * @return the store with the specified ID
     */
    Optional<Store> findById(Long id);

    /**
     * Find stores whose name contains the given substring (case-insensitive)
     * @param name the substring to search for in store names
     * @return list of stores with names containing the substring
     */
    @Query("SELECT s FROM Store s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Store> findBySubName(@Param("name") String name);

    /**
     * Find stores by exact name match
     * @param name the store name
     * @return the store with the exact name match
     */
    Optional<Store> findByName(String name);

    /**
     * Find stores by name containing the given string (case-insensitive) - derived query
     * @param name the name pattern to search for
     * @return list of stores whose name contains the given string
     */
    List<Store> findByNameContainingIgnoreCase(String name);

    /**
     * Find stores by address containing the given string (case-insensitive)
     * @param address the address pattern to search for
     * @return list of stores whose address contains the given string
     */
    List<Store> findByAddressContainingIgnoreCase(String address);

    /**
     * Find stores by city (extracted from address)
     * @param city the city to search for
     * @return list of stores in the specified city
     */
    @Query("SELECT s FROM Store s WHERE LOWER(s.address) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<Store> findByCity(@Param("city") String city);

    /**
     * Check if a store exists with the given name
     * @param name the store name to check
     * @return true if a store with the name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Find stores with inventory for a specific product
     * @param productId the product ID
     * @return list of stores that have the product in stock
     */
    @Query("SELECT DISTINCT s FROM Store s JOIN s.inventories i WHERE i.product.id = :productId AND i.stockLevel > 0")
    List<Store> findStoresWithProductInStock(@Param("productId") Long productId);

    /**
     * Find stores with inventory for a specific product with minimum stock level
     * @param productId the product ID
     * @param minStockLevel the minimum stock level required
     * @return list of stores that have the product with at least the specified stock level
     */
    @Query("SELECT DISTINCT s FROM Store s JOIN s.inventories i WHERE i.product.id = :productId AND i.stockLevel >= :minStockLevel")
    List<Store> findStoresWithProductAndMinStock(@Param("productId") Long productId, @Param("minStockLevel") Integer minStockLevel);

    /**
     * Count the number of stores
     * @return total number of stores
     */
    long count();

    /**
     * Delete a store by name
     * @param name the name of the store to delete
     */
    void deleteByName(String name);

    /**
     * Find all stores ordered by name ascending
     * @return list of stores sorted by name
     */
    List<Store> findAllByOrderByNameAsc();

    /**
     * Find all stores ordered by name descending
     * @return list of stores sorted by name descending
     */
    List<Store> findAllByOrderByNameDesc();
}