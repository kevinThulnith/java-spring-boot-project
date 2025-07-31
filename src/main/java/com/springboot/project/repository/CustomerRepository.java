package com.springboot.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.springboot.project.entity.Customer;
import java.util.Optional;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    List<Customer> findByNameContainingIgnoreCase(String name);
    
    // Pagination support
    Page<Customer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelephoneContainingIgnoreCase(
            String name, String email, String telephone, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.accountNumber LIKE %:keyword% OR c.email LIKE %:keyword% OR c.telephone LIKE %:keyword%")
    List<Customer> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.accountNumber LIKE %:keyword% OR c.email LIKE %:keyword% OR c.telephone LIKE %:keyword%")
    Page<Customer> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
