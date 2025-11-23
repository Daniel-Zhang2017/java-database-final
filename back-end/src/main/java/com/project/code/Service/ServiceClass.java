package com.project.code.Service;

import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServiceClass {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Checks if an inventory record exists for a given product and store combination
     * @param inventory The inventory object containing product and store information
     * @return false if inventory exists, otherwise true
     */
    public boolean validateInventory(Inventory inventory) {
        if (inventory == null || inventory.getProduct() == null || inventory.getStore() == null) {
            throw new IllegalArgumentException("Inventory, Product, and Store must not be null");
        }
        
        Long productId = inventory.getProduct().getId();
        Long storeId = inventory.getStore().getId();
        
        boolean exists = inventoryRepository.existsByProductIdAndStoreId(productId, storeId);
        
        // Return false if inventory exists, otherwise true
        return !exists;
    }

    /**
     * Checks if a product exists by its name
     * @param product The product to check
     * @return false if a product with the same name exists, otherwise true
     */
    public boolean validateProduct(Product product) {
        if (product == null || product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product and product name must not be null or empty");
        }
        
        Optional<Product> existingProduct = productRepository.findByName(product.getName());
        
        // Return false if product with same name exists, otherwise true
        return !existingProduct.isPresent();
    }

    /**
     * Checks if a product exists by its ID
     * @param id The product ID to check
     * @return false if the product does not exist with the given ID, otherwise true
     */
    public boolean validateProductId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        
        boolean exists = productRepository.existsById(id);
        
        // Return false if product does not exist, otherwise true
        return exists;
    }

    /**
     * Fetches the inventory record for a given product and store combination
     * @param inventory The inventory object containing product and store information
     * @return The inventory record for the product-store combination
     */
    public Inventory getInventoryId(Inventory inventory) {
        if (inventory == null || inventory.getProduct() == null || inventory.getStore() == null) {
            throw new IllegalArgumentException("Inventory, Product, and Store must not be null");
        }
        
        Long productId = inventory.getProduct().getId();
        Long storeId = inventory.getStore().getId();
        
        return inventoryRepository.findByProductIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new RuntimeException(
                    "Inventory not found for product ID: " + productId + " and store ID: " + storeId));
    }

    /**
     * Alternative method to get inventory by explicit product and store IDs
     * @param productId The product ID
     * @param storeId The store ID
     * @return The inventory record for the product-store combination
     */
    public Inventory getInventoryByIds(Long productId, Long storeId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException("Store ID must be a positive number");
        }
        
        return inventoryRepository.findByProductIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new RuntimeException(
                    "Inventory not found for product ID: " + productId + " and store ID: " + storeId));
    }

    /**
     * Validates if there is sufficient stock for a given product and store combination
     * @param productId The product ID
     * @param storeId The store ID
     * @param requestedQuantity The quantity requested
     * @return true if sufficient stock exists, false otherwise
     */
    public boolean validateStockAvailability(Long productId, Long storeId, Integer requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new IllegalArgumentException("Requested quantity must be a positive number");
        }
        
        Inventory inventory = getInventoryByIds(productId, storeId);
        return inventory.getStockLevel() >= requestedQuantity;
    }

    /**
     * Validates if a product with given SKU already exists
     * @param product The product to check
     * @return false if a product with the same SKU exists, otherwise true
     */
    public boolean validateProductSku(Product product) {
        if (product == null || product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("Product and SKU must not be null or empty");
        }
        
        boolean exists = productRepository.existsBySku(product.getSku());
        
        // Return false if product with same SKU exists, otherwise true
        return !exists;
    }
}