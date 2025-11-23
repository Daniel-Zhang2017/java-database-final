package com.project.code.Controller;

import com.project.code.Model.*;
import com.project.code.Repo.*;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ServiceClass serviceClass;

    /**
     * Update inventory for a product
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateInventory(@RequestBody CombinedRequest combinedRequest) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate product ID
            if (!serviceClass.validateProductId(combinedRequest.getProduct().getId())) {
                response.put("message", "Product not found with ID: " + combinedRequest.getProduct().getId());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get existing inventory
            Inventory existingInventory = serviceClass.getInventoryByIds(
                combinedRequest.getProduct().getId(), 
                combinedRequest.getInventory().getStore().getId()
            );
            
            if (existingInventory != null) {
                // Update inventory
                existingInventory.setStockLevel(combinedRequest.getInventory().getStockLevel());
                inventoryRepository.save(existingInventory);
                response.put("message", "Inventory updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "No inventory data available for update");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("message", "Error updating inventory: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Save a new inventory entry
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveInventory(@RequestBody Inventory inventory) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate if inventory already exists
            if (!serviceClass.validateInventory(inventory)) {
                response.put("message", "Inventory already exists for this product and store");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save new inventory
            inventoryRepository.save(inventory);
            response.put("message", "Inventory saved successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("message", "Error saving inventory: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all products for a specific store
     */
    @GetMapping("/store/{storeId}/products")
    public ResponseEntity<Map<String, Object>> getAllProducts(@PathVariable Long storeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = productRepository.findProductsByStoreId(storeId);
            response.put("products", products);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error retrieving products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Filter products by category and name
     */
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> getProductName(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> filteredProducts;
            
            if ("null".equals(category) && "null".equals(name)) {
                // Return all products if both parameters are "null"
                filteredProducts = productRepository.findAll();
            } else if ("null".equals(category)) {
                // Filter by name only
                filteredProducts = productRepository.findByNameContainingIgnoreCase(name);
            } else if ("null".equals(name)) {
                // Filter by category only
                filteredProducts = productRepository.findByCategory(category);
            } else {
                // Filter by both category and name
                filteredProducts = productRepository.findByCategoryAndNameContainingIgnoreCase(category, name);
            }
            
            response.put("product", filteredProducts);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error filtering products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Search for products by name within a specific store
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProduct(
            @RequestParam String name,
            @RequestParam Long storeId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = productRepository.findByNameLikeInStore(storeId, name);
            response.put("product", products);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error searching products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete a product by ID
     */
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Map<String, String>> removeProduct(@PathVariable Long productId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate if product exists
            if (!serviceClass.validateProductId(productId)) {
                response.put("message", "Product not found with ID: " + productId);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Delete related inventory entries
            inventoryRepository.deleteByProductId(productId);
            
            // Delete product
            productRepository.deleteById(productId);
            
            response.put("message", "Product and related inventory deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("message", "Error deleting product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Validate if specified quantity of a product is available in stock
     */
    @GetMapping("/validate-quantity")
    public ResponseEntity<Map<String, Boolean>> validateQuantity(
            @RequestParam Long productId,
            @RequestParam Long storeId,
            @RequestParam Integer quantity) {
        
        Map<String, Boolean> response = new HashMap<>();
        
        try {
            boolean isValid = serviceClass.validateStockAvailability(productId, storeId, quantity);
            response.put("available", isValid);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("available", false);
            return ResponseEntity.ok(response);
        }
    }
}

// Supporting DTO class for combined product and inventory requests
class CombinedRequest {
    private Product product;
    private Inventory inventory;

    // Constructors
    public CombinedRequest() {}

    public CombinedRequest(Product product, Inventory inventory) {
        this.product = product;
        this.inventory = inventory;
    }

    // Getters and Setters
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}