import React, { useState } from 'react';
import axios from 'axios';
import { Button, Form, Container } from 'react-bootstrap'; // Thêm các component từ Bootstrap
import { useNavigate } from 'react-router-dom'; // Thêm useNavigate

const RegisterForm = ({ onSwitchForm }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');  // Thêm state cho phone
  const navigate = useNavigate(); // Khởi tạo navigate

  const handleRegister = () => {
    axios.post('http://localhost:8080/api/users/register', { name, email, password, phone })
      .then(response => {
        alert('Đăng ký thành công');
        console.log(response.data);
        // Chuyển hướng về trang đăng nhập
        navigate('/login'); // Sử dụng navigate từ useNavigate
      })
      .catch(error => {
        alert('Đăng ký thất bại');
        console.error(error);
      });
  };

  return (
    <Container className="mt-5 d-flex justify-content-center">
      <div className="auth-form w-100" style={{ maxWidth: '400px' }}>
        <h2 className="text-center mb-4">Đăng ký</h2>
        <Form>
          <Form.Group className="mb-3 text-start">
            <Form.Label>Tên đầy đủ</Form.Label>
            <Form.Control 
              type="text" 
              placeholder="Nhập tên đầy đủ" 
              value={name} 
              onChange={(e) => setName(e.target.value)} 
              required 
            />
          </Form.Group>
          
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

          <Form.Group className="mb-3 text-start">
            <Form.Label>Số điện thoại</Form.Label>
            <Form.Control 
              type="text" 
              placeholder="Nhập số điện thoại" 
              value={phone} 
              onChange={(e) => setPhone(e.target.value)} 
              required 
            />
          </Form.Group>

          <Button variant="primary" onClick={handleRegister} className="w-100">Đăng ký</Button>
        </Form>
      </div>
    </Container>
  );
};

export default RegisterForm;
