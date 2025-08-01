package com.springboot.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class HelpController {
    
    @GetMapping("/help")
    public String help(Authentication authentication, Model model) {
        // Set page title for base template
        model.addAttribute("pageTitle", "Help - Pahana Edu");
        model.addAttribute("username", authentication.getName());
        
        return "help";
    }
}
