package com.project.code.Repo;

import com.project.code.Model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // All basic CRUD operations are automatically provided by JpaRepository:
    // - save(S entity) - Save or update an order item
    // - findById(ID id) - Find an order item by ID
    // - findAll() - Get all order items
    // - deleteById(ID id) - Delete an order item by ID
    // - count() - Get total number of order items
    // - existsById(ID id) - Check if an order item exists
    
    // No custom methods needed - using default Spring Data JPA functionality
}