package com.springboot.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.springboot.project.entity.Item;
import java.util.Optional;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByItemCode(String itemCode);
    boolean existsByItemCode(String itemCode);
    List<Item> findByStatus(Item.ItemStatus status);
    List<Item> findByNameContainingIgnoreCase(String name);

    @Query("SELECT i FROM Item i WHERE i.stockQuantity <= :threshold")
    List<Item> findLowStockItems(@Param("threshold") Integer threshold);

    @Query("SELECT i FROM Item i WHERE i.name LIKE %:keyword% OR i.itemCode LIKE %:keyword%")
    List<Item> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT i FROM Item i WHERE i.status = 'ACTIVE' ORDER BY i.name")
    List<Item> findActiveItemsOrderByName();
}
