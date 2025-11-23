package com.project.code.Repo;

import com.project.code.Model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    /**
     * Find reviews by store ID and product ID
     * @param storeId the store ID
     * @param productId the product ID
     * @return list of reviews for the specified product and store
     */
    List<Review> findByStoreIdAndProductId(Long storeId, Long productId);

    /**
     * Find reviews by product ID
     * @param productId the product ID
     * @return list of reviews for the specified product
     */
    List<Review> findByProductId(Long productId);

    /**
     * Find reviews by store ID
     * @param storeId the store ID
     * @return list of reviews for the specified store
     */
    List<Review> findByStoreId(Long storeId);

    /**
     * Find reviews by customer ID
     * @param customerId the customer ID
     * @return list of reviews by the specified customer
     */
    List<Review> findByCustomerId(Long customerId);

    /**
     * Count reviews by product ID and store ID
     * @param productId the product ID
     * @param storeId the store ID
     * @return number of reviews for the product at the store
     */
    long countByProductIdAndStoreId(Long productId, Long storeId);

    /**
     * Find reviews by store ID, product ID and rating range
     * @param storeId the store ID
     * @param productId the product ID
     * @param minRating the minimum rating
     * @param maxRating the maximum rating
     * @return list of reviews within the rating range for the product and store
     */
    List<Review> findByStoreIdAndProductIdAndRatingBetween(Long storeId, Long productId, Integer minRating, Integer maxRating);

    /**
     * Find reviews by rating greater than or equal to specified value
     * @param rating the minimum rating
     * @return list of reviews with rating >= specified value
     */
    List<Review> findByRatingGreaterThanEqual(Integer rating);

    /**
     * Find reviews by rating less than or equal to specified value
     * @param rating the maximum rating
     * @return list of reviews with rating <= specified value
     */
    List<Review> findByRatingLessThanEqual(Integer rating);

    /**
     * Find reviews by rating between min and max values
     * @param minRating the minimum rating
     * @param maxRating the maximum rating
     * @return list of reviews within the rating range
     */
    List<Review> findByRatingBetween(Integer minRating, Integer maxRating);

    /**
     * Find reviews by product ID and rating
     * @param productId the product ID
     * @param rating the rating
     * @return list of reviews for the product with specified rating
     */
    List<Review> findByProductIdAndRating(Long productId, Integer rating);

    /**
     * Find reviews by customer ID and product ID
     * @param customerId the customer ID
     * @param productId the product ID
     * @return list of reviews by the customer for the specified product
     */
    List<Review> findByCustomerIdAndProductId(Long customerId, Long productId);

    /**
     * Find reviews by customer ID and store ID
     * @param customerId the customer ID
     * @param storeId the store ID
     * @return list of reviews by the customer for the specified store
     */
    List<Review> findByCustomerIdAndStoreId(Long customerId, Long storeId);

    /**
     * Check if a customer has already reviewed a product at a specific store
     * @param customerId the customer ID
     * @param productId the product ID
     * @param storeId the store ID
     * @return true if review exists, false otherwise
     */
    boolean existsByCustomerIdAndProductIdAndStoreId(Long customerId, Long productId, Long storeId);

    /**
     * Find reviews with comments (non-empty comments)
     * @return list of reviews that have comments
     */
    List<Review> findByCommentIsNotNull();

    /**
     * Find reviews with comments containing the given text (case-insensitive)
     * @param text the text to search for in comments
     * @return list of reviews with comments containing the text
     */
    @Query("{ 'comment': { $regex: ?0, $options: 'i' } }")
    List<Review> findByCommentContainingIgnoreCase(String text);

    /**
     * Calculate average rating for a product
     * @param productId the product ID
     * @return average rating for the product
     */
    @Query(value = "{ 'productId': ?0 }", fields = "{ 'rating': 1 }")
    default Double findAverageRatingByProductId(Long productId) {
        List<Review> reviews = findByProductId(productId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculate average rating for a product at a specific store
     * @param productId the product ID
     * @param storeId the store ID
     * @return average rating for the product at the store
     */
    @Query(value = "{ 'productId': ?0, 'storeId': ?1 }", fields = "{ 'rating': 1 }")
    default Double findAverageRatingByProductIdAndStoreId(Long productId, Long storeId) {
        List<Review> reviews = findByStoreIdAndProductId(storeId, productId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    /**
     * Count reviews by product ID
     * @param productId the product ID
     * @return number of reviews for the product
     */
    long countByProductId(Long productId);

    /**
     * Count reviews by store ID
     * @param storeId the store ID
     * @return number of reviews for the store
     */
    long countByStoreId(Long storeId);

    /**
     * Count reviews by customer ID
     * @param customerId the customer ID
     * @return number of reviews by the customer
     */
    long countByCustomerId(Long customerId);

    /**
     * Delete reviews by product ID
     * @param productId the product ID
     */
    void deleteByProductId(Long productId);

    /**
     * Delete reviews by store ID
     * @param storeId the store ID
     */
    void deleteByStoreId(Long storeId);

    /**
     * Delete reviews by customer ID
     * @param customerId the customer ID
     */
    void deleteByCustomerId(Long customerId);
}