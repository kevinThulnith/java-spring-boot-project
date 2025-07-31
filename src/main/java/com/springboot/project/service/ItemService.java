package com.springboot.project.service;

import com.springboot.project.repository.ItemRepository;
import org.springframework.stereotype.Service;
import com.springboot.project.entity.Item;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    
    private final ItemRepository itemRepository;
    
    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }
    
    public List<Item> findAllItems() {
        return itemRepository.findAll();
    }
    
    public Optional<Item> findItemById(Long id) {
        return itemRepository.findById(id);
    }
    
    public Optional<Item> findByItemCode(String itemCode) {
        return itemRepository.findByItemCode(itemCode);
    }
    
    public boolean existsByItemCode(String itemCode) {
        return itemRepository.existsByItemCode(itemCode);
    }
    
    public List<Item> findByStatus(Item.ItemStatus status) {
        return itemRepository.findByStatus(status);
    }
    
    public List<Item> findActiveItems() {
        return itemRepository.findByStatus(Item.ItemStatus.ACTIVE);
    }
    
    public List<Item> searchByName(String name) {
        return itemRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Item> findLowStockItems(Integer threshold) {
        return itemRepository.findLowStockItems(threshold);
    }
    
    public Item updateItem(Long id, Item updatedItem) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setName(updatedItem.getName());
                    item.setDescription(updatedItem.getDescription());
                    item.setPrice(updatedItem.getPrice());
                    item.setStockQuantity(updatedItem.getStockQuantity());
                    item.setStatus(updatedItem.getStatus());
                    return itemRepository.save(item);
                })
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    }
    
    public Item updateStock(Long id, Integer newQuantity) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setStockQuantity(newQuantity);
                    // Auto-update status based on stock
                    if (newQuantity <= 0) {
                        item.setStatus(Item.ItemStatus.OUT_OF_STOCK);
                    } else if (item.getStatus() == Item.ItemStatus.OUT_OF_STOCK) {
                        item.setStatus(Item.ItemStatus.ACTIVE);
                    }
                    return itemRepository.save(item);
                })
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    }
    
    public Item updatePrice(Long id, BigDecimal newPrice) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setPrice(newPrice);
                    return itemRepository.save(item);
                })
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    }
    
    public void deleteItem(Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
        } else {
            throw new RuntimeException("Item not found with id: " + id);
        }
    }
    
    public Item deactivateItem(Long id) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setStatus(Item.ItemStatus.INACTIVE);
                    return itemRepository.save(item);
                })
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    }
    
    public Item activateItem(Long id) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setStatus(Item.ItemStatus.ACTIVE);
                    return itemRepository.save(item);
                })
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    }
    
    public long countItems() {
        return itemRepository.count();
    }
    
    public long countActiveItems() {
        return itemRepository.findByStatus(Item.ItemStatus.ACTIVE).size();
    }
    
    public long countLowStockItems(Integer threshold) {
        return itemRepository.findLowStockItems(threshold).size();
    }
}
