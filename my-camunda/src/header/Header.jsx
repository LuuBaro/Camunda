import React from 'react';
import { Link, useNavigate } from 'react-router-dom'; // Import Link for routing and useNavigate for navigation
import './Header.css'; // Thêm file CSS để làm đẹp

const Header = () => {
  const navigate = useNavigate();
  const userName = localStorage.getItem('userName'); // Lấy tên người dùng từ localStorage
  
  const handleLogout = () => {
    // Xóa thông tin người dùng khỏi localStorage khi đăng xuất
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');
    localStorage.removeItem('userPhone');
    
    // Điều hướng về trang đăng nhập sau khi đăng xuất
    navigate('/login');
  };

  return (
    <header className="header">
      <div className="logo">
        <h1>LuuBaro Shop</h1>
      </div>
      <nav className="nav-links">
        <ul>
          <li><Link to="/">Trang chủ</Link></li>
          <li><Link to="/about">Giới thiệu</Link></li>
          <li><Link to="/services">Dịch vụ</Link></li>
          <li><Link to="/profile">Hồ sơ</Link></li>
        </ul>
      </nav>
      <div className="auth-buttons">
        {userName ? (
          // Nếu người dùng đã đăng nhập, hiển thị tên và nút Đăng xuất
          <>
            <span>Hello, {userName}</span>
            <button className="logout-btn" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          // Nếu không có người dùng đăng nhập, hiển thị nút Đăng nhập và Đăng ký
          <>
            <Link to="/login">
              <button className="login-btn">Login</button>
            </Link>
            <Link to="/register">
              <button className="signup-btn">Sign Up</button>
            </Link>
          </>
        )}
      </div>
    </header>
  );
};

export default Header;
