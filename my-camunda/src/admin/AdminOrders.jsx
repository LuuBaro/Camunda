import React, { useState, useEffect } from "react";
import axios from "axios";
import { Table, Button, Badge, Spinner } from "react-bootstrap";

const API_URL = "http://localhost:8080/api/orders";

const AdminOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [processing, setProcessing] = useState(null);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const response = await axios.get(API_URL);
      const sortedOrders = response.data.sort(
        (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
      );
      setOrders(sortedOrders);
    } catch (error) {
      console.error("Error fetching orders:", error);
      alert("Có lỗi khi tải danh sách đơn hàng.");
    } finally {
      setLoading(false);
    }
  };

  const getApiUrl = (status) => {
    const apiRoutes = {
      PENDING: "/approve-order",
      INSTOCK: "/approve-stock",
      OUTSTOCK: "/reject-stock",
      PAYMENT_SUCCESS: "/complete-payment-success",
      PAYMENT_FAILED: "/complete-payment-failure",
    };
    return apiRoutes[status] ? `${API_URL}${apiRoutes[status]}` : null;
  };

  const handleProcessOrder = async (orderId, status) => {
    const apiUrl = getApiUrl(status);
    if (!apiUrl) {
      alert("Không tìm thấy API phù hợp!");
      return;
    }

    setProcessing(orderId);
    try {
      const response = await axios.put(apiUrl, null, { params: { orderId } });
      if (response.data) {
        alert("Cập nhật đơn hàng thành công!");
        fetchOrders();
      } else {
        alert("Cập nhật đơn hàng thất bại!");
      }
    } catch (error) {
      console.error("Error processing order:", error);
      alert("Có lỗi khi xử lý đơn hàng.");
    } finally {
      setProcessing(null);
    }
  };

  const getStatusLabel = (status) => {
    const statusMap = {
      PENDING: { text: "Chờ duyệt", variant: "warning" },
      APPROVING: { text: "Đang xét duyệt", variant: "primary" },
      INSTOCK: { text: "Còn hàng", variant: "success" },
      OUTSTOCK: { text: "Hết hàng", variant: "danger" },
      APPROVED: { text: "Bắt đầu giao hàng", variant: "info" },
      REJECTED: { text: "Đã hủy đơn", variant: "danger" },
      PAYMENT_SUCCESS: { text: "Thanh toán thành công", variant: "success" },
      PAYMENT_FAILED: { text: "Thanh toán thất bại", variant: "danger" },
      CANCELED: { text: "Khách hàng đã hủy", variant: "danger" },
      DELETED: { text: "Khách hàng đã hủy", variant: "dark" },
    };

    return statusMap[status] ? (
      <Badge bg={statusMap[status].variant}>{statusMap[status].text}</Badge>
    ) : (
      <Badge bg="secondary">{status}</Badge>
    );
  };

  const canProcessOrder = (status) => {
    return ["PENDING", "INSTOCK","OUTSTOCK", "PAYMENT_SUCCESS", "PAYMENT_FAILED"].includes(status);
  };

  return (
    <div className="admin-orders-container" style={{ padding: "20px" }}>
      <h2>Quản lý đơn hàng</h2>
      {loading ? (
        <p>Đang tải...</p>
      ) : (
        <Table striped bordered hover responsive>
          <thead>
            <tr>
              <th>Mã đơn</th>
              <th>Trạng thái</th>
              <th>Tổng tiền</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td>{order.id}</td>
                <td>{getStatusLabel(order.status)}</td>
                <td>{order.totalAmount.toLocaleString()} VND</td>
                <td>
                  {canProcessOrder(order.status) && (
                    <Button
                      variant="success"
                      onClick={() => handleProcessOrder(order.id, order.status)}
                      disabled={processing === order.id}
                    >
                      {processing === order.id ? (
                        <Spinner animation="border" size="sm" />
                      ) : (
                        "Duyệt đơn"
                      )}
                    </Button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}
    </div>
  );
};

export default AdminOrders;
