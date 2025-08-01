package com.springboot.project.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class BillCreateRequest {
    private Long customerId;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String status = "DRAFT";
    private List<BillItemRequest> items;
    
    @Data
    public static class BillItemRequest {
        private Long itemId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
