package com.springboot.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.springboot.project.service.CustomerService;
import com.springboot.project.service.ItemService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final CustomerService customerService;
    private final ItemService itemService;
    
    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
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
        
        return "home";
    }
}