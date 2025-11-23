package com.project.code.Controller;

import com.project.code.Model.Customer;
import com.project.code.Model.Review;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Get customer name by customer ID - FIXED VERSION
     */
    private String getCustomerName(Long customerId) {
        if (customerId == null) {
            return "Anonymous";
        }
        
        try {
            Optional<Customer> customerOptional = customerRepository.findById(customerId);
            return customerOptional.map(Customer::getName).orElse("Unknown Customer");
        } catch (Exception e) {
            return "Unknown Customer";
        }
    }

    /**
     * Get reviews for a specific product in a store
     */
    @GetMapping("/{storeId}/{productId}")
    public ResponseEntity<Map<String, Object>> getReviews(
            @PathVariable Long storeId,
            @PathVariable Long productId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Fetch reviews for the specific product and store
            List<Review> reviews = reviewRepository.findByStoreIdAndProductId(storeId, productId);
            
            // Create a list to store filtered review data
            List<Map<String, Object>> filteredReviews = new ArrayList<>();
            
            // Process each review to include comment, rating, and customer name
            for (Review review : reviews) {
                Map<String, Object> reviewData = new HashMap<>();
                reviewData.put("comment", review.getComment());
                reviewData.put("rating", review.getRating());
                
                // Get customer name from CustomerRepository
                String customerName = getCustomerName(review.getCustomerId());
                reviewData.put("customerName", customerName);
                
                // Include additional useful information
                reviewData.put("reviewId", review.getId());
                
                filteredReviews.add(reviewData);
            }
            
            response.put("reviews", filteredReviews);
            response.put("totalReviews", filteredReviews.size());
            response.put("storeId", storeId);
            response.put("productId", productId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error retrieving reviews: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get average rating for a product in a store
     */
    @GetMapping("/{storeId}/{productId}/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRating(
            @PathVariable Long storeId,
            @PathVariable Long productId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get reviews for the specific product and store
            List<Review> reviews = reviewRepository.findByStoreIdAndProductId(storeId, productId);
            
            // Calculate average rating manually
            double averageRating = 0.0;
            long reviewCount = reviews.size();
            
            if (reviewCount > 0) {
                double totalRating = reviews.stream()
                        .mapToInt(Review::getRating)
                        .sum();
                averageRating = totalRating / reviewCount;
            }
            
            response.put("averageRating", Math.round(averageRating * 100.0) / 100.0); // Round to 2 decimal places
            response.put("reviewCount", reviewCount);
            response.put("storeId", storeId);
            response.put("productId", productId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error calculating average rating: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get reviews by rating range
     */
    @GetMapping("/rating-range")
    public ResponseEntity<Map<String, Object>> getReviewsByRatingRange(
            @RequestParam Long storeId,
            @RequestParam Long productId,
            @RequestParam Integer minRating,
            @RequestParam Integer maxRating) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get all reviews for the store and product first
            List<Review> allReviews = reviewRepository.findByStoreIdAndProductId(storeId, productId);
            
            // Then filter by rating range in memory
            List<Review> filteredReviews = allReviews.stream()
                    .filter(review -> review.getRating() >= minRating && review.getRating() <= maxRating)
                    .toList();
            
            List<Map<String, Object>> formattedReviews = new ArrayList<>();
            
            for (Review review : filteredReviews) {
                Map<String, Object> reviewData = new HashMap<>();
                reviewData.put("comment", review.getComment());
                reviewData.put("rating", review.getRating());
                reviewData.put("customerName", getCustomerName(review.getCustomerId()));
                reviewData.put("reviewId", review.getId());
                
                formattedReviews.add(reviewData);
            }
            
            response.put("reviews", formattedReviews);
            response.put("storeId", storeId);
            response.put("productId", productId);
            response.put("minRating", minRating);
            response.put("maxRating", maxRating);
            response.put("totalReviews", formattedReviews.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error retrieving reviews by rating range: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get reviews by customer ID
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Map<String, Object>> getReviewsByCustomer(@PathVariable Long customerId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Review> reviews = reviewRepository.findByCustomerId(customerId);
            List<Map<String, Object>> customerReviews = new ArrayList<>();
            
            for (Review review : reviews) {
                Map<String, Object> reviewData = new HashMap<>();
                reviewData.put("reviewId", review.getId());
                reviewData.put("comment", review.getComment());
                reviewData.put("rating", review.getRating());
                reviewData.put("productId", review.getProductId());
                reviewData.put("storeId", review.getStoreId());
                
                customerReviews.add(reviewData);
            }
            
            response.put("reviews", customerReviews);
            response.put("customerId", customerId);
            response.put("totalReviews", customerReviews.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error retrieving customer reviews: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Create a new review
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createReview(@RequestBody Review review) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if customer already reviewed this product at this store
            boolean alreadyReviewed = reviewRepository.existsByCustomerIdAndProductIdAndStoreId(
                review.getCustomerId(), review.getProductId(), review.getStoreId());
            
            if (alreadyReviewed) {
                response.put("message", "Customer has already reviewed this product at this store");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate rating range
            if (review.getRating() < 1 || review.getRating() > 5) {
                response.put("message", "Rating must be between 1 and 5");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save the review
            Review savedReview = reviewRepository.save(review);
            response.put("message", "Review created successfully");
            response.put("reviewId", savedReview.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("message", "Error creating review: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get reviews with comments only
     */
    @GetMapping("/{storeId}/{productId}/with-comments")
    public ResponseEntity<Map<String, Object>> getReviewsWithComments(
            @PathVariable Long storeId,
            @PathVariable Long productId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Review> allReviews = reviewRepository.findByStoreIdAndProductId(storeId, productId);
            List<Map<String, Object>> reviewsWithComments = new ArrayList<>();
            
            for (Review review : allReviews) {
                if (review.getComment() != null && !review.getComment().trim().isEmpty()) {
                    Map<String, Object> reviewData = new HashMap<>();
                    reviewData.put("comment", review.getComment());
                    reviewData.put("rating", review.getRating());
                    reviewData.put("customerName", getCustomerName(review.getCustomerId()));
                    reviewData.put("reviewId", review.getId());
                    
                    reviewsWithComments.add(reviewData);
                }
            }
            
            response.put("reviews", reviewsWithComments);
            response.put("storeId", storeId);
            response.put("productId", productId);
            response.put("totalReviewsWithComments", reviewsWithComments.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Error retrieving reviews with comments: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}