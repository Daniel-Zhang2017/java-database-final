package com.project.code.Controller;

import com.project.code.Model.Store;
import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Repo.StoreRepository;
import com.project.code.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderService orderService;

    /**
     * Add a new store
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> addStore(@RequestBody Store store) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate store data
            if (store.getName() == null || store.getName().trim().isEmpty()) {
                response.put("message", "Store name is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (store.getAddress() == null || store.getAddress().trim().isEmpty()) {
                response.put("message", "Store address is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if store with same name already exists
            Optional<Store> existingStore = storeRepository.findByName(store.getName());
            if (existingStore.isPresent()) {
                response.put("message", "Store with name '" + store.getName() + "' already exists");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save the store
            Store savedStore = storeRepository.save(store);
            response.put("message", "Store created successfully with ID: " + savedStore.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("message", "Error creating store: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Validate if a store exists by store ID
     */
    @GetMapping("/validate/{storeId}")
    public ResponseEntity<Map<String, Boolean>> validateStore(@PathVariable Long storeId) {
        Map<String, Boolean> response = new HashMap<>();
        
        try {
            boolean storeExists = storeRepository.existsById(storeId);
            response.put("exists", storeExists);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("exists", false);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Place an order
     */
    @PostMapping("/placeOrder")
    public ResponseEntity<Map<String, String>> placeOrder(@RequestBody PlaceOrderRequestDTO placeOrderRequest) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate input data
            if (placeOrderRequest.getStoreId() == null) {
                response.put("Error", "Store ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (placeOrderRequest.getCustomerEmail() == null || placeOrderRequest.getCustomerEmail().trim().isEmpty()) {
                response.put("Error", "Customer email is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (placeOrderRequest.getPurchaseProduct() == null || placeOrderRequest.getPurchaseProduct().isEmpty()) {
                response.put("Error", "Order must contain at least one product");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate store exists before processing order
            if (!storeRepository.existsById(placeOrderRequest.getStoreId())) {
                response.put("Error", "Store not found with ID: " + placeOrderRequest.getStoreId());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Process the order through OrderService
            orderService.saveOrder(placeOrderRequest);
            
            response.put("message", "Order placed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("Error", "Error processing order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get store by ID
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<Map<String, Object>> getStore(@PathVariable Long storeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Store> store = storeRepository.findById(storeId);
            if (store.isPresent()) {
                response.put("store", store.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("Error", "Store not found with ID: " + storeId);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("Error", "Error retrieving store: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all stores
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllStores() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("stores", storeRepository.findAll());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("Error", "Error retrieving stores: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Search stores by name
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchStores(@RequestParam String name) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (name == null || name.trim().isEmpty()) {
                response.put("Error", "Search name parameter is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("stores", storeRepository.findBySubName(name));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("Error", "Error searching stores: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update store information
     */
    @PutMapping("/{storeId}")
    public ResponseEntity<Map<String, String>> updateStore(
            @PathVariable Long storeId,
            @RequestBody Store store) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if store exists
            if (!storeRepository.existsById(storeId)) {
                response.put("Error", "Store not found with ID: " + storeId);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate store data
            if (store.getName() == null || store.getName().trim().isEmpty()) {
                response.put("Error", "Store name is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (store.getAddress() == null || store.getAddress().trim().isEmpty()) {
                response.put("Error", "Store address is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Set the ID and update
            store.setId(storeId);
            storeRepository.save(store);
            
            response.put("message", "Store updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("Error", "Error updating store: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete store by ID
     */
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Map<String, String>> deleteStore(@PathVariable Long storeId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if store exists
            if (!storeRepository.existsById(storeId)) {
                response.put("Error", "Store not found with ID: " + storeId);
                return ResponseEntity.badRequest().body(response);
            }
            
            storeRepository.deleteById(storeId);
            response.put("message", "Store deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("Error", "Error deleting store: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get stores ordered by name (ascending)
     */
    @GetMapping("/sorted")
    public ResponseEntity<Map<String, Object>> getStoresSortedByName() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("stores", storeRepository.findAllByOrderByNameAsc());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("Error", "Error retrieving sorted stores: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}