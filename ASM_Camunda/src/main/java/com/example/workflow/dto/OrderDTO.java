package com.example.workflow.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private String orderId;
    private String customerName;
    private String address;
    private String paymentMethod;
    private int quantity;
    private String productId;

    public Map<String, Object> toMap() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderId", orderId);
        variables.put("customerName", customerName);
        variables.put("address", address);
        variables.put("paymentMethod", paymentMethod);
        variables.put("quantity", quantity);
        variables.put("productId", productId);
        return variables;
    }
}
