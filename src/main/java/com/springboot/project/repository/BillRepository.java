package com.springboot.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import  com.springboot.project.entity.Customer;
import com.springboot.project.entity.Bill;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillNumber(String billNumber);
    List<Bill> findByCustomer(Customer customer);
    List<Bill> findByStatus(Bill.BillStatus status);

    @Query("SELECT b FROM Bill b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Bill> findBillsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Bill b WHERE b.customer.id = :customerId AND b.status = :status")
    List<Bill> findByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") Bill.BillStatus status);

    @Query("SELECT b FROM Bill b WHERE b.totalAmount BETWEEN :minAmount AND :maxAmount")
    List<Bill> findByTotalAmountRange(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT SUM(b.totalAmount) FROM Bill b WHERE b.status = 'PAID' AND b.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.status = :status")
    Long countByStatus(@Param("status") Bill.BillStatus status);

    @Query("SELECT b FROM Bill b ORDER BY b.createdAt DESC")
    List<Bill> findAllOrderByBillDateDesc();
}
