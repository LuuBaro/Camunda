import React, { useState } from 'react';
import axios from 'axios';
import { Button, Form, Container } from 'react-bootstrap'; // Thêm các component từ Bootstrap
import { useNavigate } from 'react-router-dom'; // Thêm useNavigate

const LoginForm = ({ onSwitchForm }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate(); // Khởi tạo navigate

  const handleLogin = () => {
    axios.post('http://localhost:8080/api/users/login', { email, password })
      .then(response => {
        alert('Đăng nhập thành công');
        console.log(response.data);

        // Lưu thông tin đăng nhập vào localStorage
        localStorage.setItem('userId', response.data.id); // Lưu userId
        localStorage.setItem('userEmail', response.data.email); // Lưu email
        localStorage.setItem('userName', response.data.name); // Lưu tên
        localStorage.setItem('userPhone', response.data.phone); // Lưu số điện thoại
        localStorage.setItem('userRole', response.data.role); // Lưu số điện thoại

        // Điều hướng về trang chính sau khi đăng nhập thành công
        if (response.data.role === 'ADMIN') {
          navigate('/admin');
        } else {
          navigate('/');
        }
      })
      .catch(error => {
        alert('Email hoặc mật khẩu không hợp lệ');
        console.error(error);
      });
  };

  return (
    <Container className="mt-5 d-flex justify-content-center">
      <div className="auth-form w-100" style={{ maxWidth: '400px' }}>
        <h2 className="text-center mb-4">Đăng nhập</h2>
        <Form>
          <Form.Group className="mb-3 text-start">
            <Form.Label>Email</Form.Label>
            <Form.Control 
              type="email" 
              placeholder="Nhập email" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              required 
            />
          </Form.Group>
          
          <Form.Group className="mb-3 text-start">
            <Form.Label>Mật khẩu</Form.Label>
            <Form.Control 
              type="password" 
              placeholder="Nhập mật khẩu" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              required 
            />
          </Form.Group>

          <Button variant="primary" onClick={handleLogin} className="w-100">Đăng nhập</Button>
        </Form>
        
      </div>
    </Container>
  );
};

export default LoginForm;
