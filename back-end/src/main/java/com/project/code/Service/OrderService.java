package com.project.code.Service;

import com.project.code.Model.*;
import com.project.code.Repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Processes a customer's order, including saving the order details and associated items
     * @param placeOrderRequest Request data for placing an order
     */
    public void saveOrder(com.project.code.Model.PlaceOrderRequestDTO placeOrderRequest) {
        // 1. Retrieve or Create the Customer
        Customer customer = retrieveOrCreateCustomer(placeOrderRequest);

        // 2. Retrieve the Store
        Store store = retrieveStore(placeOrderRequest.getStoreId());

        // 3. Create OrderDetails
        OrderDetails orderDetails = createOrderDetails(customer, store, placeOrderRequest);

        // 4. Create and Save OrderItems
        createAndSaveOrderItems(placeOrderRequest, orderDetails);
    }

    /**
     * Retrieve existing customer or create a new one
     */
    private Customer retrieveOrCreateCustomer(com.project.code.Model.PlaceOrderRequestDTO placeOrderRequest) {
        Customer customer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());
        
        if (customer == null) {
            // Create new customer
            customer = new Customer();
            customer.setName(placeOrderRequest.getCustomerName());
            customer.setEmail(placeOrderRequest.getCustomerEmail());
            customer.setPhone(placeOrderRequest.getCustomerPhone());
            customer = customerRepository.save(customer);
        }
        
        return customer;
    }

    /**
     * Retrieve store by ID, throw exception if not found
     */
    private Store retrieveStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));
    }

    /**
     * Create and save OrderDetails
     */
    private OrderDetails createOrderDetails(Customer customer, Store store, com.project.code.Model.PlaceOrderRequestDTO placeOrderRequest) {
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setDate(LocalDateTime.now());
        
        // Use the totalPrice from the request or calculate it if not provided
        Double totalPrice = placeOrderRequest.getTotalPrice();
        if (totalPrice == null) {
            totalPrice = calculateTotalPrice(placeOrderRequest.getPurchaseProduct());
        }
        orderDetails.setTotalPrice(totalPrice);
        
        return orderDetailsRepository.save(orderDetails);
    }

    /**
     * Create and save OrderItems, update inventory
     */
    private void createAndSaveOrderItems(com.project.code.Model.PlaceOrderRequestDTO placeOrderRequest, OrderDetails orderDetails) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (PurchaseProductDTO itemDTO : placeOrderRequest.getPurchaseProduct()) {
            // Retrieve product - using getId() instead of getProductId()
            Product product = productRepository.findById(itemDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDTO.getId()));
            
            // Check and update inventory
            updateInventory(orderDetails.getStore().getId(), product.getId(), itemDTO.getQuantity());
            
            // Create order item
            OrderItem orderItem = createOrderItem(orderDetails, product, itemDTO);
            orderItems.add(orderItem);
        }
        
        // Save all order items
        orderItemRepository.saveAll(orderItems);
        
        // Associate order items with order details
        orderDetails.setOrderItems(orderItems);
        orderDetailsRepository.save(orderDetails);
    }

    /**
     * Update inventory stock levels
     */
    private void updateInventory(Long storeId, Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new RuntimeException(
                    "Inventory not found for product ID: " + productId + " and store ID: " + storeId));
        
        if (inventory.getStockLevel() < quantity) {
            throw new RuntimeException("Insufficient stock for product ID: " + productId + 
                                     ". Available: " + inventory.getStockLevel() + ", Requested: " + quantity);
        }
        
        // Update stock level
        inventory.setStockLevel(inventory.getStockLevel() - quantity);
        inventoryRepository.save(inventory);
    }

    /**
     * Create an OrderItem
     */
    private OrderItem createOrderItem(OrderDetails orderDetails, Product product, PurchaseProductDTO itemDTO) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(orderDetails);
        orderItem.setProduct(product);
        orderItem.setQuantity(itemDTO.getQuantity());
        orderItem.setPrice(product.getPrice()); // Use current product price
        
        return orderItem;
    }

    /**
     * Calculate total price from order items
     */
    private Double calculateTotalPrice(List<PurchaseProductDTO> purchaseProducts) {
        return purchaseProducts.stream()
                .mapToDouble(itemDTO -> {
                    Product product = productRepository.findById(itemDTO.getId())
                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDTO.getId()));
                    return product.getPrice() * itemDTO.getQuantity();
                })
                .sum();
    }
}