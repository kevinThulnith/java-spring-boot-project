package com.springboot.project.controller;

import com.springboot.project.entity.Bill;
import com.springboot.project.entity.Customer;
import com.springboot.project.entity.Item;
import com.springboot.project.entity.BillItem;
import com.springboot.project.service.BillService;
import com.springboot.project.service.CustomerService;
import com.springboot.project.service.ItemService;
import com.springboot.project.dto.BillCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;
    private final CustomerService customerService;
    private final ItemService itemService;

    @GetMapping
    public String viewBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            Model model,
            Authentication authentication) {

        try {
            // Create sort object
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);

            // Get bills with pagination
            Page<Bill> billPage = billService.findAllBills(pageable);

            // Get all customers for the dropdown
            List<Customer> customers = customerService.findAllCustomers();
            
            // Get all active items for bill creation
            List<Item> items = itemService.findActiveItems();

            // Add attributes to model
            model.addAttribute("bills", billPage.getContent());
            model.addAttribute("customers", customers);
            model.addAttribute("items", items);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", billPage.getTotalPages());
            model.addAttribute("totalElements", billPage.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("search", search);
            model.addAttribute("username", authentication.getName());

            return "bill";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading bills: " + e.getMessage());
            return "bill";
        }
    }

    @PostMapping
    public String createBill(@RequestParam Long customerId,
                           @RequestParam(required = false) BigDecimal taxAmount,
                           @RequestParam(required = false) BigDecimal discountAmount,
                           @RequestParam String status,
                           @RequestParam("itemIds") List<Long> itemIds,
                           @RequestParam("quantities") List<Integer> quantities,
                           @RequestParam("unitPrices") List<BigDecimal> unitPrices,
                           RedirectAttributes redirectAttributes,
                           Authentication authentication) {
        
        try {
            // Validate inputs
            if (itemIds.isEmpty() || quantities.isEmpty() || unitPrices.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select at least one item.");
                return "redirect:/bills";
            }
            
            if (itemIds.size() != quantities.size() || itemIds.size() != unitPrices.size()) {
                redirectAttributes.addFlashAttribute("error", "Item data mismatch. Please try again.");
                return "redirect:/bills";
            }

            // Get customer
            Customer customer = customerService.findCustomerById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Create bill
            Bill bill = new Bill();
            bill.setCustomer(customer);
            bill.setTaxAmount(taxAmount != null ? taxAmount : BigDecimal.ZERO);
            bill.setDiscountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO);
            bill.setStatus(Bill.BillStatus.valueOf(status));
            bill.setCreatedAt(LocalDateTime.now());

            // Calculate total amount from items
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<BillItem> billItems = new ArrayList<>();

            for (int i = 0; i < itemIds.size(); i++) {
                Long itemId = itemIds.get(i);
                Integer quantity = quantities.get(i);
                BigDecimal unitPrice = unitPrices.get(i);

                if (quantity > 0) { // Only add items with positive quantity
                    Item item = itemService.findItemById(itemId)
                            .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

                    BillItem billItem = new BillItem();
                    billItem.setBill(bill);
                    billItem.setItem(item);
                    billItem.setQuantity(quantity);
                    billItem.setUnitPrice(unitPrice);
                    billItem.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)));

                    billItems.add(billItem);
                    totalAmount = totalAmount.add(billItem.getTotalPrice());
                }
            }

            if (billItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please add at least one item with quantity > 0.");
                return "redirect:/bills";
            }

            // Set amounts on bill
            bill.setTotalAmount(totalAmount);
            bill.setBillItems(billItems);
            
            // Calculate final amount
            BigDecimal finalAmount = totalAmount
                    .add(bill.getTaxAmount() != null ? bill.getTaxAmount() : BigDecimal.ZERO)
                    .subtract(bill.getDiscountAmount() != null ? bill.getDiscountAmount() : BigDecimal.ZERO);
            bill.setFinalAmount(finalAmount);

            billService.saveBill(bill);
            redirectAttributes.addFlashAttribute("success", "Bill created successfully with " + billItems.size() + " items!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating bill: " + e.getMessage());
        }

        return "redirect:/bills";
    }

    @PostMapping("/{id}/edit")
    public String updateBill(@PathVariable Long id,
                           @Valid @ModelAttribute Bill bill,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please fill in all required fields correctly.");
            return "redirect:/bills";
        }

        try {
            Bill existingBill = billService.findBillById(id)
                    .orElseThrow(() -> new RuntimeException("Bill not found"));

            // Update bill fields
            existingBill.setCustomer(bill.getCustomer());
            existingBill.setTotalAmount(bill.getTotalAmount());
            existingBill.setTaxAmount(bill.getTaxAmount());
            existingBill.setDiscountAmount(bill.getDiscountAmount());
            existingBill.setFinalAmount(bill.getFinalAmount());
            existingBill.setStatus(bill.getStatus());

            // Update paid date if status is PAID
            if (bill.getStatus() == Bill.BillStatus.PAID && existingBill.getPaidAt() == null) {
                existingBill.setPaidAt(LocalDateTime.now());
            }

            billService.saveBill(existingBill);
            redirectAttributes.addFlashAttribute("success", "Bill updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating bill: " + e.getMessage());
        }

        return "redirect:/bills";
    }

    @PostMapping("/{id}/delete")
    public String deleteBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            billService.deleteBill(id);
            redirectAttributes.addFlashAttribute("success", "Bill deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting bill: " + e.getMessage());
        }

        return "redirect:/bills";
    }

    @PostMapping("/{id}/confirm")
    public String confirmBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            billService.confirmBill(id);
            redirectAttributes.addFlashAttribute("success", "Bill confirmed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error confirming bill: " + e.getMessage());
        }

        return "redirect:/bills";
    }

    @PostMapping("/{id}/pay")
    public String payBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            billService.payBill(id);
            redirectAttributes.addFlashAttribute("success", "Bill marked as paid successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error marking bill as paid: " + e.getMessage());
        }

        return "redirect:/bills";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            billService.cancelBill(id);
            redirectAttributes.addFlashAttribute("success", "Bill cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling bill: " + e.getMessage());
        }

        return "redirect:/bills";
    }
}
