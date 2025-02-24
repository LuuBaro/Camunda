package com.example.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingDTO {
    private String orderId;
    private String trackingNumber;
    private String status; // SHIPPED, DELIVERED
}
