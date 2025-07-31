package com.springboot.project.service;

import com.springboot.project.entity.Bill;
import com.springboot.project.entity.Customer;
import com.springboot.project.repository.BillRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BillService {
    
    private final BillRepository billRepository;
    
    public Bill saveBill(Bill bill) {
        return billRepository.save(bill);
    }
    
    public List<Bill> findAllBills() {
        return billRepository.findAll();
    }
    
    public List<Bill> findAllBillsOrderByDateDesc() {
        return billRepository.findAllOrderByBillDateDesc();
    }
    
    public Optional<Bill> findBillById(Long id) {
        return billRepository.findById(id);
    }
    
    public Optional<Bill> findByBillNumber(String billNumber) {
        return billRepository.findByBillNumber(billNumber);
    }
    
    public List<Bill> findByCustomer(Customer customer) {
        return billRepository.findByCustomer(customer);
    }
    
    public List<Bill> findByStatus(Bill.BillStatus status) {
        return billRepository.findByStatus(status);
    }
    
    public List<Bill> findByCustomerIdAndStatus(Long customerId, Bill.BillStatus status) {
        return billRepository.findByCustomerIdAndStatus(customerId, status);
    }
    
    public List<Bill> findBillsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return billRepository.findBillsBetweenDates(startDate, endDate);
    }
    
    public List<Bill> findByTotalAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return billRepository.findByTotalAmountRange(minAmount, maxAmount);
    }
    
    public Bill updateBill(Long id, Bill updatedBill) {
        return billRepository.findById(id)
                .map(bill -> {
                    bill.setCustomer(updatedBill.getCustomer());
                    bill.setTotalAmount(updatedBill.getTotalAmount());
                    bill.setTaxAmount(updatedBill.getTaxAmount());
                    bill.setDiscountAmount(updatedBill.getDiscountAmount());
                    bill.setFinalAmount(updatedBill.getFinalAmount());
                    bill.setStatus(updatedBill.getStatus());
                    return billRepository.save(bill);
                })
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
    }
    
    public Bill updateBillStatus(Long id, Bill.BillStatus status) {
        return billRepository.findById(id)
                .map(bill -> {
                    bill.setStatus(status);
                    if (status == Bill.BillStatus.PAID && bill.getPaidAt() == null) {
                        bill.setPaidAt(LocalDateTime.now());
                    }
                    return billRepository.save(bill);
                })
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
    }
    
    public Bill confirmBill(Long id) {
        return updateBillStatus(id, Bill.BillStatus.CONFIRMED);
    }
    
    public Bill payBill(Long id) {
        return updateBillStatus(id, Bill.BillStatus.PAID);
    }
    
    public Bill cancelBill(Long id) {
        return updateBillStatus(id, Bill.BillStatus.CANCELLED);
    }
    
    public Bill calculateBillAmounts(Long id) {
        return billRepository.findById(id)
                .map(bill -> {
                    BigDecimal totalAmount = bill.getBillItems().stream()
                            .map(billItem -> billItem.getUnitPrice().multiply(BigDecimal.valueOf(billItem.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    bill.setTotalAmount(totalAmount);
                    
                    // Calculate final amount (total + tax - discount)
                    BigDecimal finalAmount = totalAmount
                            .add(bill.getTaxAmount() != null ? bill.getTaxAmount() : BigDecimal.ZERO)
                            .subtract(bill.getDiscountAmount() != null ? bill.getDiscountAmount() : BigDecimal.ZERO);
                    
                    bill.setFinalAmount(finalAmount);
                    
                    return billRepository.save(bill);
                })
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
    }
    
    public void deleteBill(Long id) {
        if (billRepository.existsById(id)) {
            billRepository.deleteById(id);
        } else {
            throw new RuntimeException("Bill not found with id: " + id);
        }
    }
    
    public long countBills() {
        return billRepository.count();
    }
    
    public Long countByStatus(Bill.BillStatus status) {
        return billRepository.countByStatus(status);
    }
    
    public BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = billRepository.getTotalRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    public List<Bill> findDraftBills() {
        return billRepository.findByStatus(Bill.BillStatus.DRAFT);
    }
    
    public List<Bill> findPaidBills() {
        return billRepository.findByStatus(Bill.BillStatus.PAID);
    }
    
    public List<Bill> findConfirmedBills() {
        return billRepository.findByStatus(Bill.BillStatus.CONFIRMED);
    }
    
    public Bill getBillWithItems(Long id) {
        return billRepository.findById(id)
                .map(bill -> {
                    // This will trigger lazy loading of bill items
                    bill.getBillItems().size(); // Force initialization
                    return bill;
                })
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
    }
    
    public boolean billExists(Long id) {
        return billRepository.existsById(id);
    }
}
