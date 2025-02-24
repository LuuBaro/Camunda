package com.example.workflow.service;


import com.example.workflow.dto.request.OrderRequest;
import com.example.workflow.moddel.Order;
import com.example.workflow.moddel.OrderItem;
import com.example.workflow.moddel.Product;
import com.example.workflow.moddel.User;
import com.example.workflow.repository.OrderItemRepository;
import com.example.workflow.repository.OrderRepository;
import com.example.workflow.repository.ProductRepository;
import com.example.workflow.repository.UserRepository;
import com.example.workflow.util.Constants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(UUID id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(UUID id) {
        orderRepository.deleteById(id);
    }


    public ResponseEntity<?> placeOrder(OrderRequest orderRequest) {
        Optional<User> userOptional = userRepository.findById(orderRequest.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOptional.get();
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemRequest : orderRequest.getItems()) {
            Optional<Product> productOptional = productRepository.findById(itemRequest.getProductId());
            if (productOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Product not found");
            }

            Product product = productOptional.get();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(itemRequest.getPrice());
            orderItem.setSubtotal(itemRequest.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            totalAmount = totalAmount.add(orderItem.getSubtotal());
            orderItems.add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // Gửi Order vào Camunda, sử dụng order.getId() làm business key
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderId", order.getId().toString());
        variables.put("userId", user.getId().toString());
        variables.put("totalAmount", totalAmount);

        // Chỉ gọi một lần hàm startProcessInstanceByKey, sử dụng order.getId() làm business key
        runtimeService.startProcessInstanceByKey("orderProcess", order.getId().toString(), variables);

        return ResponseEntity.ok("Order placed successfully with ID: " + order.getId());
    }


    public ResponseEntity<?> cancelOrder(String orderId, String taskId) {
        try {
            // 1. Kiểm tra order có tồn tại không
            UUID orderUUID = UUID.fromString(orderId);
            Optional<Order> optionalOrder = orderRepository.findById(orderUUID);
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.badRequest().body("Order not found");
            }

            // 2. Cập nhật trạng thái đơn hàng
            Order order = optionalOrder.get();
            order.setStatus(Order.OrderStatus.CANCELED);
            orderRepository.save(order);

            // 3. Chuẩn bị biến cho Camunda
            // Gán orderCanceled = false để biểu thị rằng khách hàng muốn hủy đơn.
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderCanceled", true);

            Task task = null;
            // Nếu taskId không được truyền, tìm kiếm task dựa trên business key (orderId) và task definition key
            if (taskId == null || taskId.trim().isEmpty()) {
                task = taskService.createTaskQuery()
                        .processInstanceBusinessKey(order.getId().toString())
                        .taskDefinitionKey(Constants.USER_TASK_CANCEL_ORDER)
                        .singleResult();
                if (task == null) {
                    // Log thông tin để kiểm tra
                    System.out.println("Không tìm thấy task với business key: " + order.getId().toString() +
                            " và task definition key: " + Constants.USER_TASK_CANCEL_ORDER);
                    return ResponseEntity.badRequest().body("Task not found for order cancellation");
                }
                taskId = task.getId();
            } else {
                // Nếu taskId đã được truyền, truy vấn task đó
                task = taskService.createTaskQuery().taskId(taskId).singleResult();
                if (task == null) {
                    return ResponseEntity.badRequest().body("Task not found");
                }
            }

            // 4. Hoàn thành task, Camunda sẽ rẽ nhánh theo điều kiện (ví dụ: ${orderCanceled == false})
            taskService.complete(taskId, variables);

            return ResponseEntity.ok("Order canceled successfully.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid orderId format");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }


    public ResponseEntity<?> deleteOrder(String orderId, String taskId) {
        try {
            // 1. Kiểm tra order có tồn tại không
            UUID orderUUID = UUID.fromString(orderId);
            Optional<Order> optionalOrder = orderRepository.findById(orderUUID);
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.badRequest().body("Order not found");
            }

            Order order = optionalOrder.get();
            // 2. Cập nhật trạng thái đơn hàng nếu cần (ví dụ: chuyển thành DELETED hoặc giữ nguyên trạng thái)
            // Nếu bạn muốn cập nhật trạng thái, hãy chắc chắn rằng enum OrderStatus có giá trị này.
            order.setStatus(Order.OrderStatus.DELETED); // Hoặc bạn có thể không thay đổi trạng thái
            orderRepository.save(order);

            // 3. Chuẩn bị biến cho Camunda để báo hiệu rằng đơn đã bị xóa (hoàn tất task)
            Map<String, Object> variables = new HashMap<>();
            variables.put("deleted", true);

            Task task = null;
            // Nếu taskId không được truyền, tìm kiếm task dựa trên business key (orderId) và task definition key
            if (taskId == null || taskId.trim().isEmpty()) {
                task = taskService.createTaskQuery()
                        .processInstanceBusinessKey(order.getId().toString())
                        .taskDefinitionKey(Constants.USER_TASK_DELETE_ORDER)
                        .singleResult();
                if (task == null) {
                    // Log để kiểm tra
                    System.out.println("Không tìm thấy task với business key: " + order.getId().toString() +
                            " và task definition key: " + Constants.USER_TASK_DELETE_ORDER);
                    return ResponseEntity.badRequest().body("Task not found for order deletion");
                }
                taskId = task.getId();
            } else {
                // Nếu taskId đã được truyền, truy vấn task đó
                task = taskService.createTaskQuery().taskId(taskId).singleResult();
                if (task == null) {
                    return ResponseEntity.badRequest().body("Task not found");
                }
            }

            // 4. Hoàn thành task, Camunda sẽ tiến hành end-task (rẽ nhánh theo biến deleted)
            taskService.complete(taskId, variables);

            return ResponseEntity.ok("Order deleted successfully.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid orderId format");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    public ResponseEntity<?> approveOrder(String orderId, String taskId) {
        try {
            UUID orderUUID = UUID.fromString(orderId);
            Optional<Order> optionalOrder = orderRepository.findById(orderUUID);
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order not found"));
            }
            Order order = optionalOrder.get();

            // Cập nhật trạng thái duyệt
            order.setStatus(Order.OrderStatus.APPROVING);
            orderRepository.save(order);

            // Thiết lập biến cho Camunda
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderCanceled", false); // Đặt false vì admin duyệt đơn
            variables.put("orderId", order.getId().toString());

            Task task = taskService.createTaskQuery()
                    .processInstanceBusinessKey(order.getId().toString())
                    .taskDefinitionKey(Constants.USER_TASK_CANCEL_ORDER)
                    .singleResult();

            if (task == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Task not found"));
            }

            taskService.complete(task.getId(), variables);

            // ✅ Trả về trạng thái mới của đơn hàng
            return ResponseEntity.ok(Map.of("status", order.getStatus().toString()));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid orderId format"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> rejectStock(String orderId) {
        try {
            // 1️⃣ Chuyển đổi orderId sang UUID để đảm bảo hợp lệ
            UUID orderUUID;
            try {
                orderUUID = UUID.fromString(orderId);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body("❌ Invalid orderId format");
            }

            // 2️⃣ Kiểm tra đơn hàng có tồn tại không
            Optional<Order> optionalOrder = orderRepository.findById(orderUUID);
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Order not found");
            }

            Order order = optionalOrder.get();

            // Cập nhật trạng thái duyệt
            order.setStatus(Order.OrderStatus.REJECTED);
            orderRepository.save(order);

            // 3️⃣ Tìm Task "Hết hàng" liên quan đến đơn hàng
            Task task = taskService.createTaskQuery()
                    .processInstanceBusinessKey(orderUUID.toString()) // Tìm theo businessKey (orderId)
                    .taskDefinitionKey(Constants.USER_TASK_REJECT_ORDER) // Định danh task "Hết hàng"
                    .singleResult();

            if (task == null) {
                // Log thông tin để debug nếu cần
                System.out.println("Không tìm thấy task 'Hết hàng' cho đơn hàng: " + orderId);
                return ResponseEntity.badRequest().body("❌ Không tìm thấy User Task 'Hết hàng' cho đơn hàng: " + orderId);
            }

            // 4️⃣ Hoàn thành Task "Hết hàng" với biến báo hiệu hết hàng
            Map<String, Object> variables = new HashMap<>();
            variables.put("isInStock", false); // Xác nhận hết hàng
            taskService.complete(task.getId(), variables);

            return ResponseEntity.ok(Map.of(
                    "message", "🚨 Đơn hàng đã bị hủy do hết hàng!",
                    "orderId", orderId
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Internal Server Error: " + e.getMessage());
        }
    }

    public ResponseEntity<String> approveStock(String orderId) {
        try {
            UUID orderUUID;
            try {
                orderUUID = UUID.fromString(orderId);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body("❌ Invalid orderId format");
            }

            Optional<Order> optionalOrder = orderRepository.findById(orderUUID);
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Order not found");
            }

            // 🔍 Kiểm tra Process Instance trước
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceBusinessKey(orderId)
                    .singleResult();

            if (instance == null) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy ProcessInstance với orderId: " + orderId);
            }

            Order order = optionalOrder.get();

            // Cập nhật trạng thái duyệt
            order.setStatus(Order.OrderStatus.APPROVING);
            orderRepository.save(order);

            // 🔍 Tìm Task "Còn hàng"
            Task task = taskService.createTaskQuery()
                    .processInstanceId(instance.getId())
                    .taskDefinitionKey(Constants.USER_TASK_APPROVE_ORDER)
                    .singleResult();

            if (task == null) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy User Task 'Còn hàng' trong quy trình.");
            }

            // ✅ Hoàn thành Task
            Map<String, Object> variables = new HashMap<>();
            variables.put("isInStock", true);
            taskService.complete(task.getId(), variables);

            return ResponseEntity.ok("✅ Đơn hàng đã xác nhận còn hàng và chuyển sang kiểm tra thanh toán!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Internal Server Error: " + e.getMessage());
        }
    }

    public ResponseEntity<?> completePaymentSuccess( String orderId) {
        try {
            UUID orderUUID;
            try {
                orderUUID = UUID.fromString(orderId);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body("❌ Invalid orderId format");
            }

            Optional<Order> optionalOrder = orderRepository.findById(orderUUID);
            Order order = optionalOrder.get();

            if (optionalOrder.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Order not found");
            }

            // Cập nhật trạng thái duyệt
            order.setStatus(Order.OrderStatus.APPROVED);
            orderRepository.save(order);

            // Tìm Task "Thanh toán thành công"
            Task task = taskService.createTaskQuery()
                    .processInstanceBusinessKey(orderUUID.toString()) // Tìm theo orderId
                    .taskDefinitionKey("Activity_0ardm9h") // ID của User Task "Thanh toán thành công"
                    .singleResult();

            if (task == null) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy User Task 'Thanh toán thành công' cho orderId: " + orderId);
            }

            // Hoàn thành Task => Camunda sẽ tự động đi đến End Event
            taskService.complete(task.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Đơn hàng đã hoàn tất, bắt đầu giao hàng!",
                    "orderId", orderId
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Internal Server Error: " + e.getMessage());
        }
    }

    public ResponseEntity<?> completePaymentFailure(String orderId) {
        try {
            UUID orderUUID;
            try {
                orderUUID = UUID.fromString(orderId);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body("❌ Invalid orderId format");
            }

            Optional<Order> optionalOrder = orderRepository.findById(orderUUID);
            Order order = optionalOrder.get();

            if (optionalOrder.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Order not found");
            }

            // Cập nhật trạng thái duyệt
            order.setStatus(Order.OrderStatus.REJECTED);
            orderRepository.save(order);

            // Tìm Task "Thanh toán thất bại"
            Task task = taskService.createTaskQuery()
                    .processInstanceBusinessKey(orderUUID.toString()) // Tìm theo orderId
                    .taskDefinitionKey("Activity_0vqplu0") // ID của User Task "Thanh toán thất bại"
                    .singleResult();

            if (task == null) {
                return ResponseEntity.badRequest().body("❌ Không tìm thấy User Task 'Thanh toán thất bại' cho orderId: " + orderId);
            }

            // Hoàn thành Task => Camunda sẽ tự động kết thúc quy trình
            taskService.complete(task.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "❌ Thanh toán thất bại, đơn hàng đã bị hủy!",
                    "orderId", orderId
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Internal Server Error: " + e.getMessage());
        }
    }
}
