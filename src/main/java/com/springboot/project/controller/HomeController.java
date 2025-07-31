package com.springboot.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.springboot.project.service.CustomerService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final CustomerService customerService;
    
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
        
        return "home";
    }
}