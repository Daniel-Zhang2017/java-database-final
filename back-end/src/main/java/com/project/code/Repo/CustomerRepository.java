package com.project.code.Repo;

import com.project.code.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find a customer by their email address
     * @param email the email address to search for
     * @return the customer with the specified email, or null if not found
     */
    Customer findByEmail(String email);

    /**
     * Find a customer by their ID
     * Note: This method is already provided by JpaRepository, but we're explicitly defining it for clarity
     * @param id the customer ID to search for
     * @return the customer with the specified ID, or null if not found
     */
    Customer findById(long id);

    /**
     * Find customers by name (exact match)
     * @param name the name to search for
     * @return list of customers with the specified name
     */
    List<Customer> findByName(String name);

    /**
     * Find customers by name containing the given string (case-insensitive)
     * @param name the name pattern to search for
     * @return list of customers whose name contains the given string
     */
    List<Customer> findByNameContainingIgnoreCase(String name);

    /**
     * Find customers by phone number
     * @param phone the phone number to search for
     * @return the customer with the specified phone number, or null if not found
     */
    Customer findByPhone(String phone);

    /**
     * Check if a customer exists with the given email
     * @param email the email to check
     * @return true if a customer with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find customers by name and email
     * @param name the name to search for
     * @param email the email to search for
     * @return list of customers matching both name and email
     */
    List<Customer> findByNameAndEmail(String name, String email);

    /**
     * Delete a customer by email
     * @param email the email of the customer to delete
     * @return number of deleted records
     */
    long deleteByEmail(String email);
}