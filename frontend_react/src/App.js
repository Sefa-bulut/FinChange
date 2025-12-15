import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// Layouts & Pages
import DashboardLayout from './components/layouts/DashboardLayout';
import LoginPage from './pages/LoginPage';
import ForceChangePasswordPage from './pages/ForceChangePasswordPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import UserManagementPage from './pages/UserManagementPage';
import NewUserPage from './pages/NewUserPage';
import UserDetailPage from './pages/UserDetailPage';
import CustomerListPage from './pages/CustomerListPage';
import CustomerDetailPage from './pages/CustomerDetailPage';
import CustomerEditPage from './pages/CustomerEditPage';
import CustomerManagementPage from './pages/CustomerManagementPage';
import IndividualClientFormPage from './pages/IndividualCustomerPage';
import AssetManagementPage from "./pages/AssetManagementPage";
import AssetListPage from "./pages/AssetListPage";
import CorporateClientFormPage from './pages/CorporateCustomerPage';
import PortfolioGroupsPage from './pages/PortfolioGroupsPage';
import ReportPage from './pages/ReportPage';
import CustomerAccountPage from './pages/CustomerAccountPage';
import CreateAccountPage from './pages/CreateAccountPage';
import AccountDetailPage from './pages/AccountDetailPage';
import MarketDataPage from './pages/MarketDataPage';
import MarketWatchPage from './pages/MarketWatchPage';
import HolidayManagementPage from './pages/HolidayManagementPage';
import SystemDateManagementPage from './pages/SystemDateManagementPage';
import FinbotPage from './pages/FinbotPage';
import TradingPage from './pages/TradingPage';
import OrderListPage from './pages/OrderListPage';
import DashboardPage from './pages/DashboardPage';
import BrokerageFirmListPage from './pages/BrokerageFirmListPage';
import BrokerageFirmFormPage from './pages/BrokerageFirmFormPage';

// Route Guards
const ProtectedRoute = ({ children }) => {
    const { isLoggedIn } = useAuth();
    return isLoggedIn ? children : <Navigate to="/login" replace />;
};

const PublicRoute = ({ children }) => {
    const { isLoggedIn } = useAuth();
    return isLoggedIn ? <Navigate to="/dashboard" replace /> : children;
};

// Yetki bazlı dashboard yönlendirmesi
const DashboardRedirect = () => {
    const { permissions } = useAuth();
    if (permissions?.includes('user:read:all')) return <Navigate to="/dashboard/users" replace />;
    if (permissions?.includes('client:read:all')) return <Navigate to="/dashboard/clients" replace />;
    if (permissions?.includes('order:create') || permissions?.includes('order:read:all')) return <Navigate to="/dashboard/trading" replace />;
    return <Navigate to="/login" replace />;
};

// DashboardLayout ve ProtectedRoute kullanımı için helper fonksiyon
const withDashboard = (component) => (
    <ProtectedRoute>
        <DashboardLayout>{component}</DashboardLayout>
    </ProtectedRoute>
);

const AppRoutes = () => {
    return (
        <Router>
            <Routes>
                {/* PUBLIC ROUTES */}
                <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
                <Route path="/forgot-password" element={<PublicRoute><ForgotPasswordPage /></PublicRoute>} />
                <Route path="/reset-password" element={<PublicRoute><ResetPasswordPage /></PublicRoute>} />

                {/* PROTECTED STANDALONE */}
                <Route path="/force-change-password" element={<ProtectedRoute><ForceChangePasswordPage /></ProtectedRoute>} />

                {/* Dashboard ana rotası - yetki bazlı yönlendirme */}
                <Route path="/dashboard" element={<ProtectedRoute><DashboardRedirect /></ProtectedRoute>} />

                {/* Dashboard alt rotalar */}
                <Route path="/dashboard/users" element={withDashboard(<UserManagementPage />)} />
                <Route path="/dashboard/users/new" element={withDashboard(<NewUserPage />)} />
                <Route path="/dashboard/users/:userId" element={withDashboard(<UserDetailPage />)} />

                <Route path="/dashboard/clients" element={withDashboard(<CustomerListPage />)} />
                <Route path="/dashboard/clients/new" element={withDashboard(<CustomerManagementPage />)} />
                <Route path="/dashboard/clients/individual" element={withDashboard(<IndividualClientFormPage />)} />
                <Route path="/dashboard/clients/corporate" element={withDashboard(<CorporateClientFormPage />)} />
                <Route path="/dashboard/clients/:id" element={withDashboard(<CustomerDetailPage />)} />
                <Route path="/dashboard/clients/:id/edit" element={withDashboard(<CustomerEditPage />)} />

                <Route path="/dashboard/assets/create" element={withDashboard(<AssetManagementPage />)} />
                <Route path="/dashboard/assets/list" element={withDashboard(<AssetListPage />)} />

                <Route path="/dashboard/account-records" element={withDashboard(<CustomerAccountPage />)} />
                <Route path="/dashboard/account-records/create" element={withDashboard(<CreateAccountPage />)} />
                <Route path="/dashboard/account-records/:accountId" element={withDashboard(<AccountDetailPage />)} />

                <Route path="/dashboard/portfolio-groups" element={withDashboard(<PortfolioGroupsPage />)} />
                <Route path="/dashboard/reports" element={withDashboard(<ReportPage />)} />
                <Route path="/dashboard/market-data" element={withDashboard(<MarketDataPage />)} />
                <Route path="/dashboard/market-watch" element={withDashboard(<MarketWatchPage />)} />
                <Route path="/dashboard/finbot" element={withDashboard(<FinbotPage />)} />
                <Route path="/dashboard/trading" element={withDashboard(<TradingPage />)} />
                <Route path="/dashboard/orders" element={withDashboard(<OrderListPage />)} />
                <Route path="/dashboard/holidays" element={withDashboard(<HolidayManagementPage />)} />
                <Route path="/dashboard/system-date" element={withDashboard(<SystemDateManagementPage />)} />

                <Route path="/dashboard/brokerage-firms" element={withDashboard(<BrokerageFirmListPage />)} />
                <Route path="/dashboard/brokerage-firms/new" element={withDashboard(<BrokerageFirmFormPage />)} />
                <Route path="/dashboard/brokerage-firms/:id" element={withDashboard(<BrokerageFirmFormPage />)} />

                {/* DEFAULT & CATCH-ALL */}
                <Route path="/" element={<Navigate to="/dashboard" replace />} />
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </Router>
    );
};

export default function App() {
    return (
        <AuthProvider>
            <AppRoutes />
            <ToastContainer position="bottom-right" autoClose={5000} />
        </AuthProvider>
    );
}
