package com.springboot.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import com.springboot.project.service.CustomerService;
import com.springboot.project.service.ItemService;
import com.springboot.project.service.BillService;
import com.springboot.project.entity.Bill;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final CustomerService customerService;
    private final ItemService itemService;
    private final BillService billService;
    
    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        // Set page title for base template
        model.addAttribute("pageTitle", "Home - Pahana Edu");
        model.addAttribute("username", authentication.getName());
        
        // Add customer statistics for dashboard
        try {
            long totalCustomers = customerService.countCustomers();
            model.addAttribute("totalCustomers", totalCustomers);
        } catch (Exception e) {
            model.addAttribute("totalCustomers", 0);
        }
        
        // Add item statistics for dashboard
        try {
            long totalItems = itemService.countItems();
            long activeItems = itemService.countActiveItems();
            long lowStockItems = itemService.countLowStockItems(10); // Items with stock <= 10
            
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("activeItems", activeItems);
            model.addAttribute("lowStockItems", lowStockItems);
        } catch (Exception e) {
            model.addAttribute("totalItems", 0);
            model.addAttribute("activeItems", 0);
            model.addAttribute("lowStockItems", 0);
        }
        
        // Add bill statistics for dashboard
        try {
            long totalBills = billService.countBills();
            long paidBills = billService.countByStatus(Bill.BillStatus.PAID);
            long draftBills = billService.countByStatus(Bill.BillStatus.DRAFT);
            
            model.addAttribute("totalBills", totalBills);
            model.addAttribute("paidBills", paidBills);
            model.addAttribute("draftBills", draftBills);
        } catch (Exception e) {
            model.addAttribute("totalBills", 0);
            model.addAttribute("paidBills", 0);
            model.addAttribute("draftBills", 0);
        }
        
        return "home";
    }
}