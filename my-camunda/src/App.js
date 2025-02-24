import './App.css';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import LoginForm from './auth/Login';
import RegisterForm from './auth/Register';
import Header from './header/Header';
import ProductList from './products/Product';
import 'bootstrap/dist/css/bootstrap.min.css';
import Profile from './profile/Profile';
import AdminPage from './admin/Admin';

const AppLayout = () => {
  const location = useLocation();
  // Nếu URL bắt đầu bằng /admin thì không hiển thị Header
  const isAdminRoute = location.pathname.startsWith('/admin');

  return (
    <div className="App">
      {!isAdminRoute && <Header />}
      <Routes>
        <Route path="/" element={<ProductList />} />
        <Route path="/login" element={<LoginForm />} />
        <Route path="/register" element={<RegisterForm />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/admin/*" element={<AdminPage />} />
      </Routes>
    </div>
  );
};

function App() {
  return (
    <Router>
      <AppLayout />
    </Router>
  );
}

export default App;
