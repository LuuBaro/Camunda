package com.example.workflow.service;

import com.example.workflow.moddel.OrderItem;
import com.example.workflow.moddel.Product;
import com.example.workflow.repository.OrderItemRepository;
import com.example.workflow.repository.ProductRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("stockCheckService")
public class StockCheckService implements JavaDelegate {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        processStockCheck(execution);
    }

    private void processStockCheck(DelegateExecution execution) {
        // ✅ Lấy orderId từ biến Camunda
        String orderIdStr = (String) execution.getVariable("orderId");
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ orderId không tồn tại hoặc rỗng trong execution variables!");
        }

        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("❌ orderId không hợp lệ: " + orderIdStr);
        }

        // ✅ Tìm danh sách OrderItem của OrderId
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("❌ Không tìm thấy sản phẩm nào trong đơn hàng ID: " + orderId);
        }

        // ✅ Kiểm tra tồn kho của từng sản phẩm trong OrderItem
        boolean isInStock = orderItems.stream().allMatch(orderItem -> {
            Product product = orderItem.getProduct();
            if (product == null) {
                System.out.println("⚠️ Sản phẩm không tồn tại trong hệ thống! OrderItem ID: " + orderItem.getId());
                return false;
            }

            Optional<Product> productOptional = productRepository.findById(product.getId());
            if (productOptional.isEmpty()) {
                System.out.println("⚠️ Không tìm thấy sản phẩm trong CSDL! Product ID: " + product.getId());
                return false;
            }

            int availableStock = productOptional.get().getStock();
            return availableStock >= orderItem.getQuantity();
        });

        // ✅ Gán kết quả vào biến Camunda
        execution.setVariable("isInStock", isInStock);
        execution.setVariable("isApproved", isInStock); // Chỉ phê duyệt nếu có đủ hàng

        System.out.println("🔍 Kiểm tra tồn kho: " + (isInStock ? "✅ Còn hàng" : "❌ Hết hàng"));
    }
}
