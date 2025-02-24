import React, { useState, useEffect } from "react";
import axios from "axios";
import "../profile/Profile.css";

const Profile = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const userId = localStorage.getItem("userId");

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`http://localhost:8080/api/orders/user/${userId}`);
      // ✅ Sắp xếp đơn hàng mới nhất lên đầu
      setOrders(response.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
    } catch (error) {
      console.error("Lỗi load đơn hàng:", error);
      alert("Có lỗi khi load đơn hàng.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (userId) {
      fetchOrders();
    }
  }, [userId]);

  const handleCancelOrder = async (orderId, taskId) => {
    try {
      await axios.put("http://localhost:8080/api/orders/cancel-order", null, { params: { orderId, taskId } });
      alert("✅ Đơn hàng đã được hủy!");
      fetchOrders();
    } catch (error) {
      console.error("Lỗi khi hủy đơn hàng:", error);
      alert("❌ Có lỗi khi hủy đơn hàng.");
    }
  };

  const handleDeleteOrder = async (orderId, taskId) => {
    try {
      await axios.put("http://localhost:8080/api/orders/delete-order", null, { params: { orderId, taskId } });
      alert("✅ Đơn hàng đã được xóa!");
      fetchOrders();
    } catch (error) {
      console.error("Lỗi khi xóa đơn hàng:", error);
      alert("❌ Có lỗi khi xóa đơn hàng.");
    }
  };

  // ✅ Biên dịch trạng thái sang tiếng Việt & thêm màu sắc
  const getStatusText = (status) => {
    const statusMap = {
      PENDING: { text: "Chờ xác nhận", color: "orange" },
      APPROVED: { text: "Bắt đầu giao hàng", color: "green" },
      REJECTED: { text: "Bị từ chối", color: "red" },
      CANCELED: { text: "Đã hủy", color: "gray" },
      DELETED: { text: "Đã xóa", color: "black" },
      APPROVING: { text: "Đang xét duyệt", color: "blue" },
      INSTOCK: { text: "Có hàng", color: "green" },
      OUTSTOCK: { text: "Hết hàng", color: "red" },
      COMPLETED: { text: "Hoàn thành", color: "green" }
    };
    return statusMap[status] || { text: status, color: "black" };
  };

  return (
    <div className="profile-container">
      <h3>📦 Đơn hàng của bạn</h3>
      {loading ? (
        <p>🔄 Đang tải...</p>
      ) : orders.length > 0 ? (
        <table className="orders-table">
          <thead>
            <tr>
              <th>Mã đơn hàng</th>
              <th>Trạng thái</th>
              <th>Tổng tiền</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => {
              const { text, color } = getStatusText(order.status);
              return (
                <tr key={order.id}>
                  <td>{order.id}</td>
                  <td style={{ color, fontWeight: "bold" }}>{text}</td>
                  <td>{order.totalAmount.toLocaleString()} VND</td>
                  <td>
                    {order.status === "PENDING" && (
                      <button className="cancel-button" onClick={() => handleCancelOrder(order.id, order.taskId)}>
                        ❌ Hủy đơn
                      </button>
                    )}
                    {order.status === "CANCELED" && (
                      <button className="delete-button" onClick={() => handleDeleteOrder(order.id, order.taskId)}>
                        🗑️ Xóa đơn
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      ) : (
        <p>❌ Không có đơn hàng nào được tìm thấy.</p>
      )}
    </div>
  );
};

export default Profile;
