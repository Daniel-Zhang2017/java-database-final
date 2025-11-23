package com.project.code.Repo;

import com.project.code.Model.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Long> {
    
    // All basic CRUD operations are automatically provided by JpaRepository:
    // - save(S entity) - Save or update an order
    // - findById(ID id) - Find an order by ID
    // - findAll() - Get all orders
    // - deleteById(ID id) - Delete an order by ID
    // - count() - Get total number of orders
    // - existsById(ID id) - Check if an order exists
    
    // No custom methods needed - using default Spring Data JPA functionality
}