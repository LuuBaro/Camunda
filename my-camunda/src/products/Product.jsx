import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom'; // Import useNavigate để điều hướng
import './Product.css'; // Thêm file CSS để làm đẹp

const Product = ({ product }) => {
  const [quantity, setQuantity] = useState(1);
  const navigate = useNavigate(); // Khởi tạo navigate để điều hướng người dùng

  const increaseQuantity = () => {
    setQuantity(prevQuantity => prevQuantity + 1);
  };

  const decreaseQuantity = () => {
    if (quantity > 1) {
      setQuantity(prevQuantity => prevQuantity - 1);
    }
  };

  const handleAddToCart = async () => {
    const userId = localStorage.getItem('userId'); // Lấy userId từ localStorage
    if (!userId) {
      alert('Vui lòng đăng nhập để đặt hàng!');
      navigate('/login');
      return;
    }
  
    const orderItems = [{
      productId: product.id,
      quantity,
      price: product.price
    }];
  
    try {
      const response = await axios.post('http://localhost:8080/api/orders/place-order', {
        userId,
        items: orderItems
      });
  
      alert(`Đặt hàng thành công!`);
      navigate('/profile');
      console.log('Đơn hàng đã gửi vào Camunda:', response.data);
    } catch (error) {
      console.error('Lỗi khi đặt hàng:', error);
      alert('Có lỗi xảy ra khi đặt hàng.');
    }
  };
  

  return (
    <div className="product-card">
      <img src={product.imageUrl} alt={product.name} className="product-image" />
      <h3 className="product-name">{product.name}</h3>
      <p className="product-price">{product.price} VND</p>

      <div className="quantity-controls">
        <button onClick={decreaseQuantity} className="quantity-button">-</button>
        <span className="quantity-display">{quantity}</span>
        <button onClick={increaseQuantity} className="quantity-button">+</button>
      </div>

      <button onClick={handleAddToCart} className="add-to-cart-button">Đặt hàng ngay</button>
    </div>
  );
};

const ProductList = () => {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    // Gọi API để lấy danh sách sản phẩm
    axios.get('http://localhost:8080/api/products')
      .then(response => {
        setProducts(response.data); // Cập nhật state với danh sách sản phẩm
        console.log('Fetched products:', response.data);
      })
      .catch(error => {
        console.error('Error fetching products:', error);
      });
  }, []); // Chạy 1 lần khi component được mount

  return (
    <div className="product-list">
      {products.length > 0 ? (
        products.map(product => (
          <Product key={product.id} product={product} />
        ))
      ) : (
        <p>Loading products...</p>
      )}
    </div>
  );
};

export default ProductList;
