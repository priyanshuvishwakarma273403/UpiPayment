import React from 'react';
import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import { Toaster } from 'react-hot-toast';

import { useAuthStore } from './store/authStore';
import AppShell from './components/layout/AppShell';

// Auth Screen imports
import SplashScreen from './screens/auth/SplashScreen';
import OnboardingScreen from './screens/auth/OnboardingScreen';
import LoginScreen from './screens/auth/LoginScreen';
import RegisterScreen from './screens/auth/RegisterScreen';
import OtpScreen from './screens/auth/OtpScreen';

// Main Screen imports
import HomeScreen from './screens/home/HomeScreen';
import WalletScreen from './screens/wallet/WalletScreen';
import AddMoneyScreen from './screens/wallet/AddMoneyScreen';
import SendMoneyScreen from './screens/payment/SendMoneyScreen';
import ScanScreen from './screens/payment/ScanScreen';
import RequestScreen from './screens/payment/RequestScreen';
import MerchantScreen from './screens/merchant/MerchantScreen';
import ChatScreen from './screens/ai/ChatScreen';
import ExpenseScreen from './screens/ai/ExpenseScreen';
import NotificationsScreen from './screens/notifications/NotificationsScreen';
import ProfileScreen from './screens/profile/ProfileScreen';
import SettingsScreen from './screens/settings/SettingsScreen';
import FraudAlertScreen from './screens/fraud/FraudAlertScreen';
import OfflineSyncScreen from './screens/payment/OfflineSyncScreen';

// Protected Route Guard Component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const user = useAuthStore(state => state.user);

  if (!isAuthenticated) {
    return <Navigate to="/auth/login" replace />;
  }

  // Check roles (e.g. ROLE_MERCHANT validation)
  if (allowedRoles && !allowedRoles.some(role => user?.roles?.includes(role))) {
    return <Navigate to="/home" replace />;
  }

  return children;
};

// Animation Page Wrapper
const PageWrapper = ({ children }) => {
  const location = useLocation();
  
  return (
    <motion.div
      key={location.pathname}
      initial={{ opacity: 0, x: 25 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -25 }}
      transition={{ type: 'tween', ease: 'easeInOut', duration: 0.18 }}
      className="flex-1 flex flex-col h-full w-full"
    >
      {children}
    </motion.div>
  );
};

export const App = () => {
  const location = useLocation();

  return (
    <AppShell>
      {/* Toast Notification Mount */}
      <Toaster
        position="top-center"
        toastOptions={{
          style: {
            background: '#12121A',
            color: '#FFFFFF',
            border: '1px solid rgba(255,255,255,0.08)',
            borderRadius: '16px',
            fontSize: '12px',
            fontWeight: 'bold',
            padding: '12px 18px'
          }
        }}
      />
      
      <AnimatePresence mode="wait">
        <Routes location={location} key={location.pathname}>
          {/* Public & Walkthrough Routes */}
          <Route path="/splash" element={<PageWrapper><SplashScreen /></PageWrapper>} />
          <Route path="/onboarding" element={<PageWrapper><OnboardingScreen /></PageWrapper>} />
          
          {/* Auth Gates */}
          <Route path="/auth/login" element={<PageWrapper><LoginScreen /></PageWrapper>} />
          <Route path="/auth/register" element={<PageWrapper><RegisterScreen /></PageWrapper>} />
          <Route path="/auth/verify-otp" element={<PageWrapper><OtpScreen /></PageWrapper>} />

          {/* Protected Main Hubs */}
          <Route path="/home" element={
            <ProtectedRoute>
              <PageWrapper><HomeScreen /></PageWrapper>
            </ProtectedRoute>
          } />
          
          <Route path="/wallet" element={
            <ProtectedRoute>
              <PageWrapper><WalletScreen /></PageWrapper>
            </ProtectedRoute>
          } />
          
          <Route path="/wallet/add-money" element={
            <ProtectedRoute>
              <PageWrapper><AddMoneyScreen /></PageWrapper>
            </ProtectedRoute>
          } />
          
          <Route path="/send" element={
            <ProtectedRoute>
              <PageWrapper><SendMoneyScreen /></PageWrapper>
            </ProtectedRoute>
          } />
          
          <Route path="/scan" element={
            <ProtectedRoute>
              <PageWrapper><ScanScreen /></PageWrapper>
            </ProtectedRoute>
          } />
          
          <Route path="/request" element={
            <ProtectedRoute>
              <PageWrapper><RequestScreen /></PageWrapper>
            </ProtectedRoute>
          } />

          {/* Role Guarded Merchant Hub */}
          <Route path="/merchant" element={
            <ProtectedRoute allowedRoles={['ROLE_MERCHANT']}>
              <PageWrapper><MerchantScreen /></PageWrapper>
            </ProtectedRoute>
          } />

          {/* AI Services */}
          <Route path="/ai/chat" element={
            <ProtectedRoute>
              <PageWrapper><ChatScreen /></PageWrapper>
            </ProtectedRoute>
          } />
          
          <Route path="/ai/expenses" element={
            <ProtectedRoute>
              <PageWrapper><ExpenseScreen /></PageWrapper>
            </ProtectedRoute>
          } />

          {/* Account Metrics & Settings */}
          <Route path="/notifications" element={
            <ProtectedRoute>
              <PageWrapper><NotificationsScreen /></PageWrapper>
            </ProtectedRoute>
          } />
          
          <Route path="/profile" element={
            <ProtectedRoute>
              <PageWrapper><ProfileScreen /></PageWrapper>
            </ProtectedRoute>
          } />
          
          <Route path="/settings" element={
            <ProtectedRoute>
              <PageWrapper><SettingsScreen /></PageWrapper>
            </ProtectedRoute>
          } />
          
          <Route path="/fraud-alert" element={
            <ProtectedRoute>
              <PageWrapper><FraudAlertScreen /></PageWrapper>
            </ProtectedRoute>
          } />
          
          <Route path="/offline-sync" element={
            <ProtectedRoute>
              <PageWrapper><OfflineSyncScreen /></PageWrapper>
            </ProtectedRoute>
          } />

          {/* Root Fallbacks */}
          <Route path="/" element={<Navigate to="/splash" replace />} />
          <Route path="*" element={<Navigate to="/home" replace />} />
        </Routes>
      </AnimatePresence>
    </AppShell>
  );
};

export default App;
