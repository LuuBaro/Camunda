import React from 'react';
import { Container, Navbar, Nav } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import AdminOrders from './AdminOrders';

const AdminPage = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  return (
    <>
      <Navbar bg="dark" variant="dark" expand="lg">
        <Container>
          <Navbar.Brand>Admin Dashboard</Navbar.Brand>
          <Nav className="me-auto">
            <Nav.Link href="/admin/orders">Quản lý đơn hàng</Nav.Link>
            <Nav.Link href="/admin/products">Quản lý sản phẩm</Nav.Link>
            <Nav.Link href="/admin/users">Quản lý người dùng</Nav.Link>
          </Nav>
          <Nav>
            <Nav.Link onClick={handleLogout}>Đăng xuất</Nav.Link>
          </Nav>
        </Container>
      </Navbar>
      <Container className="mt-4">
        {/* Tùy theo route, hiển thị AdminOrders */}
        <AdminOrders />
      </Container>
    </>
  );
};

export default AdminPage;
