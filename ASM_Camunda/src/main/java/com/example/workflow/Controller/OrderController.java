package com.example.workflow.Controller;


import com.example.workflow.dto.request.OrderRequest;
import com.example.workflow.moddel.Order;
import com.example.workflow.repository.OrderRepository;
import com.example.workflow.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    // API lấy tất cả đơn hàng
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    // API lấy đơn hàng theo userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable("userId") String userId) {
        try {
            UUID userUUID = UUID.fromString(userId);
            List<Order> orders = orderRepository.findAllByUser_Id(userUUID);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid userId format");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    // API tạo đơn hàng
    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest orderRequest) {

       return ResponseEntity.ok(orderService.placeOrder(orderRequest));
    }

    // API hủy đơn hàng của khách hàng
    @PutMapping("/cancel-order")
    public ResponseEntity<?> cancelOrder(
            @RequestParam("orderId") String orderId,
            @RequestParam(value = "taskId", required = false) String taskId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, taskId));
    }

    // API xóa đơn hàng của khách hàng
    @PutMapping("/delete-order")
    public ResponseEntity<?> deleteOrder(
            @RequestParam("orderId") String orderId,
            @RequestParam(value = "taskId", required = false) String taskId
    ) {
        return ResponseEntity.ok(orderService.deleteOrder(orderId, taskId));
    }

    // API xác nhận đơn hàng của admin
    @PutMapping("/approve-order")
    public ResponseEntity<?> approveOrder(
            @RequestParam("orderId") String orderId,
            @RequestParam(value = "taskId", required = false) String taskId
    ) {
        return ResponseEntity.ok(orderService.approveOrder(orderId, taskId));
    }

    // API xác nhận từ chối đơn hàng của admin
    @PutMapping("/reject-stock")
    public ResponseEntity<?> rejectStock(@RequestParam("orderId") String orderId) {
        return orderService.rejectStock(orderId);
    }

    // API xác nhận đồng ý đơn hàng của admin
    @PutMapping("/approve-stock")
    public ResponseEntity<String> approveStock(@RequestParam("orderId") String orderId) {
       return orderService.approveStock(orderId);
    }

    // API xác nhận thanh toán thành công
    @PutMapping("/complete-payment-success")
    public ResponseEntity<?> completePaymentSuccess(@RequestParam("orderId") String orderId) {
        return orderService.completePaymentSuccess(orderId);
    }

    // API xác nhận thanh toán thất bại
    @PutMapping("/complete-payment-failure")
    public ResponseEntity<?> completePaymentFailure(@RequestParam("orderId") String orderId) {
        return orderService.completePaymentFailure(orderId);
    }

}
