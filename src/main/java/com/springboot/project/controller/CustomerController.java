package com.springboot.project.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import com.springboot.project.service.CustomerService;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import com.springboot.project.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    private final CustomerService customerService;
    
    @GetMapping
    public String customersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Model model,
            Authentication authentication) {
        
        // Add username to model for navigation
        model.addAttribute("username", authentication.getName());
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Customer> customerPage;
            
            if (search != null && !search.trim().isEmpty()) {
                customerPage = customerService.searchCustomers(search.trim(), pageable);
                model.addAttribute("search", search);
            } else {
                customerPage = customerService.findAllCustomers(pageable);
            }
            
            model.addAttribute("customers", customerPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", customerPage.getTotalPages());
            model.addAttribute("totalElements", customerPage.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
            
            // Add empty customer for the create form
            model.addAttribute("newCustomer", new Customer());
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading customers: " + e.getMessage());
            model.addAttribute("customers", List.of());
            model.addAttribute("totalElements", 0);
        }
        
        return "customer";
    }
    
    @PostMapping
    public String createCustomer(
            @Valid @ModelAttribute("newCustomer") Customer customer,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", 
                "Validation errors: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/customers";
        }
        
        try {
            // Generate unique account number
            customer.setAccountNumber(generateAccountNumber());
            
            // Check if account number already exists (unlikely but safety check)
            while (customerService.existsByAccountNumber(customer.getAccountNumber())) {
                customer.setAccountNumber(generateAccountNumber());
            }
            
            customerService.saveCustomer(customer);
            redirectAttributes.addFlashAttribute("success", 
                "Customer '" + customer.getName() + "' created successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error creating customer: " + e.getMessage());
        }
        
        return "redirect:/customers";
    }
    
    @GetMapping("/{id}")
    public String viewCustomer(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Customer customer = customerService.findCustomerById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            model.addAttribute("customer", customer);
            model.addAttribute("username", authentication.getName());
            
            return "customer-details"; // We'll create this view later
            
        } catch (Exception e) {
            model.addAttribute("error", "Customer not found");
            return "redirect:/customers";
        }
    }
    
    @PostMapping("/{id}/edit")
    public String updateCustomer(
            @PathVariable Long id,
            @Valid @ModelAttribute Customer customer,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", 
                "Validation errors: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/customers";
        }
        
        try {
            Customer existingCustomer = customerService.findCustomerById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            // Update fields but keep the account number and creation date
            existingCustomer.setName(customer.getName());
            existingCustomer.setEmail(customer.getEmail());
            existingCustomer.setTelephone(customer.getTelephone());
            existingCustomer.setAddress(customer.getAddress());
            
            customerService.saveCustomer(existingCustomer);
            redirectAttributes.addFlashAttribute("success", 
                "Customer '" + customer.getName() + "' updated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating customer: " + e.getMessage());
        }
        
        return "redirect:/customers";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.findCustomerById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            String customerName = customer.getName();
            customerService.deleteCustomer(id);
            
            redirectAttributes.addFlashAttribute("success", 
                "Customer '" + customerName + "' deleted successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting customer: " + e.getMessage());
        }
        
        return "redirect:/customers";
    }
    
    @GetMapping("/search")
    @ResponseBody
    public List<Customer> searchCustomers(@RequestParam String query) {
        try {
            return customerService.searchByKeyword(query);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    private String generateAccountNumber() {
        // Generate account number like ACC-YYYYMMDD-XXXX where XXXX is random
        return "ACC-" + java.time.LocalDate.now().toString().replace("-", "") + 
               "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
