package com.project.code.Controller;

import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ServiceClass serviceClass;

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Add a new product
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> addProduct(@RequestBody Product product) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate if product already exists (by name)
            if (!serviceClass.validateProduct(product)) {
                response.put("message", "Product with name '" + product.getName() + "' already exists");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate if SKU already exists
            if (!serviceClass.validateProductSku(product)) {
                response.put("message", "Product with SKU '" + product.getSku() + "' already exists");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save the product
            Product savedProduct = productRepository.save(product);
            response.put("message", "Product added successfully with ID: " + savedProduct.getId());
            return ResponseEntity.ok(response);
            
        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity violation: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("message", "Error adding product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductbyId(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent()) {
                response.put("products", product.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Product not found with ID: " + id);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", "Error retrieving product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update an existing product
     */
    @PutMapping
    public ResponseEntity<Map<String, String>> updateProduct(@RequestBody Product product) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate if product exists
            if (!serviceClass.validateProductId(product.getId())) {
                response.put("message", "Product not found with ID: " + product.getId());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save the updated product
            productRepository.save(product);
            response.put("message", "Product updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("message", "Error updating product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Filter products by name and category
     */
    @GetMapping("/category/{name}/{category}")
    public ResponseEntity<Map<String, Object>> filterbyCategoryProduct(
            @PathVariable String name,
            @PathVariable String category) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products;
            
            if ("null".equals(name) && "null".equals(category)) {
                // Return all products if both parameters are "null"
                products = productRepository.findAll();
            } else if ("null".equals(name)) {
                // Filter by category only
                products = productRepository.findByCategory(category);
            } else if ("null".equals(category)) {
                // Filter by name only
                products = productRepository.findByNameContainingIgnoreCase(name);
            } else {
                // Filter by both name and category
                products = productRepository.findByCategoryAndNameContainingIgnoreCase(category, name);
            }
            
            response.put("products", products);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error filtering products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all products
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listProduct() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = productRepository.findAll();
            response.put("products", products);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error retrieving products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Filter products by category and store ID
     */
    @GetMapping("/filter/{category}/{storeid}")
    public ResponseEntity<Map<String, Object>> getProductbyCategoryAndStoreId(
            @PathVariable String category,
            @PathVariable Long storeid) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = productRepository.findProductsByStoreId(storeid);
            
            // Filter by category if not "null"
            if (!"null".equals(category)) {
                products = products.stream()
                        .filter(product -> category.equalsIgnoreCase(product.getCategory()))
                        .toList();
            }
            
            response.put("product", products);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error filtering products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete product by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate if product exists
            if (!serviceClass.validateProductId(id)) {
                response.put("message", "Product not found with ID: " + id);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Remove product from inventory first
            inventoryRepository.deleteByProductId(id);
            
            // Remove product
            productRepository.deleteById(id);
            
            response.put("message", "Product deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("message", "Error deleting product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Search products by name
     */
    @GetMapping("/searchProduct/{name}")
    public ResponseEntity<Map<String, Object>> searchProduct(@PathVariable String name) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
            response.put("products", products);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error searching products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable String category) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = productRepository.findByCategory(category);
            response.put("products", products);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error retrieving products by category: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get products by price range
     */
    @GetMapping("/price-range")
    public ResponseEntity<Map<String, Object>> getProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
            response.put("products", products);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error retrieving products by price range: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}