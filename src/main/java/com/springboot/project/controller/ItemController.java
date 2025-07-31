package com.springboot.project.controller;

import com.springboot.project.entity.Item;
import com.springboot.project.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
    
    @GetMapping
    public String itemsPage(
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
            Page<Item> itemPage;
            
            if (search != null && !search.trim().isEmpty()) {
                itemPage = itemService.searchItems(search.trim(), pageable);
                model.addAttribute("search", search);
            } else {
                itemPage = itemService.findAllItems(pageable);
            }
            
            model.addAttribute("items", itemPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", itemPage.getTotalPages());
            model.addAttribute("totalElements", itemPage.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
            
            // Add empty item for the create form
            model.addAttribute("newItem", new Item());
            
            // Add item statuses for the form
            model.addAttribute("itemStatuses", Item.ItemStatus.values());
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading items: " + e.getMessage());
            model.addAttribute("items", List.of());
            model.addAttribute("totalElements", 0);
        }
        
        return "item";
    }
    
    @PostMapping
    public String createItem(
            @Valid @ModelAttribute("newItem") Item item,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", 
                "Validation errors: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/items";
        }
        
        try {
            // Generate unique item code if not provided
            if (item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
                item.setItemCode(generateItemCode());
            }
            
            // Check if item code already exists
            if (itemService.existsByItemCode(item.getItemCode())) {
                redirectAttributes.addFlashAttribute("error", 
                    "Item code '" + item.getItemCode() + "' already exists!");
                return "redirect:/items";
            }
            
            itemService.saveItem(item);
            redirectAttributes.addFlashAttribute("success", 
                "Item '" + item.getName() + "' created successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error creating item: " + e.getMessage());
        }
        
        return "redirect:/items";
    }
    
    @GetMapping("/{id}")
    public String viewItem(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Item item = itemService.findItemById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
            
            model.addAttribute("item", item);
            model.addAttribute("username", authentication.getName());
            
            return "item-details"; // We'll create this view later
            
        } catch (Exception e) {
            model.addAttribute("error", "Item not found");
            return "redirect:/items";
        }
    }
    
    @PostMapping("/{id}/edit")
    public String updateItem(
            @PathVariable Long id,
            @Valid @ModelAttribute Item item,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", 
                "Validation errors: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/items";
        }
        
        try {
            Item existingItem = itemService.findItemById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
            
            // Update fields but keep the creation date
            existingItem.setItemCode(item.getItemCode());
            existingItem.setName(item.getName());
            existingItem.setDescription(item.getDescription());
            existingItem.setPrice(item.getPrice());
            existingItem.setStockQuantity(item.getStockQuantity());
            existingItem.setStatus(item.getStatus());
            
            itemService.saveItem(existingItem);
            redirectAttributes.addFlashAttribute("success", 
                "Item '" + item.getName() + "' updated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating item: " + e.getMessage());
        }
        
        return "redirect:/items";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Item item = itemService.findItemById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
            
            String itemName = item.getName();
            itemService.deleteItem(id);
            
            redirectAttributes.addFlashAttribute("success", 
                "Item '" + itemName + "' deleted successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting item: " + e.getMessage());
        }
        
        return "redirect:/items";
    }
    
    @GetMapping("/search")
    @ResponseBody
    public List<Item> searchItems(@RequestParam String query) {
        try {
            return itemService.searchByKeyword(query);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    private String generateItemCode() {
        // Generate item code like ITM-YYYYMMDD-XXXX where XXXX is random
        return "ITM-" + java.time.LocalDate.now().toString().replace("-", "") + 
               "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
