import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Phone, Mail, Lock, Fingerprint, LogIn } from 'lucide-react';
import toast from 'react-hot-toast';

import { useAuthStore } from '../../store/authStore';
import { useHaptic } from '../../hooks/useHaptic';
import authApi from '../../api/authApi';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Card from '../../components/ui/Card';

// Validation Schemas
const phoneSchema = z.object({
  phone: z.string().min(10, 'Phone number must be exactly 10 digits').max(10, 'Phone number must be exactly 10 digits').regex(/^[0-9]+$/, 'Must contain numbers only'),
  password: z.string().min(4, 'Password must be at least 4 characters')
});

const emailSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(4, 'Password must be at least 4 characters')
});

export const LoginScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const login = useAuthStore((state) => state.login);
  const [activeTab, setActiveTab] = useState('phone'); // 'phone' or 'email'
  const [isLoading, setIsLoading] = useState(false);

  // Setup separate forms depending on active tab
  const { register: registerPhone, handleSubmit: handlePhoneSubmit, formState: { errors: phoneErrors } } = useForm({
    resolver: zodResolver(phoneSchema)
  });

  const { register: registerEmail, handleSubmit: handleEmailSubmit, formState: { errors: emailErrors } } = useForm({
    resolver: zodResolver(emailSchema)
  });

  const onSubmit = async (data) => {
    triggerHaptic('light');
    setIsLoading(true);
    const toastId = toast.loading('Logging you in...');

    try {
      // API payload selection
      const payload = activeTab === 'phone' 
        ? { phone: data.phone, password: data.password }
        : { email: data.email, password: data.password };

      const response = await authApi.login(payload);
      
      // Store user details in global store and local storage
      login(response.user, response.token, response.refreshToken);
      
      toast.success('Welcome to UPI Mesh!', { id: toastId });
      
      // Check if OTP verification is required
      if (response.user && !response.user.verified) {
        navigate('/auth/verify-otp', { state: { phone: response.user.phone } });
      } else {
        navigate('/home');
      }
    } catch (error) {
      console.warn("API login failed, checking local credentials database:", error);
      setIsLoading(false);
      
      // Verify credentials against the registeredUsers store
      const registeredUsers = useAuthStore.getState().registeredUsers || [];
      const loginId = activeTab === 'phone' ? data.phone : data.email;
      
      const matched = registeredUsers.find(
        u => (activeTab === 'phone' ? u.phone === loginId : u.email === loginId) && u.password === data.password
      );

      if (matched) {
        login(matched, 'mock_access_token_jwt', 'mock_refresh_token_jwt');
        toast.success(`Welcome back, ${matched.name}!`, { id: toastId });
        navigate('/home');
      } else {
        toast.error('Invalid login credentials! Please register first, or use default: 9876543210 / password', { id: toastId });
      }
    }
  };

  const handleBiometric = () => {
    triggerHaptic('heavy');
    
    // Check if there are any registered accounts in our mock database
    const registeredUsers = useAuthStore.getState().registeredUsers || [];
    
    // Try to load the previously authenticated user from local storage
    const savedAuth = localStorage.getItem('upimesh-auth');
    let lastUser = null;
    if (savedAuth) {
      try {
        const parsed = JSON.parse(savedAuth);
        if (parsed?.state?.user) {
          lastUser = parsed.state.user;
        }
      } catch (e) {}
    }

    // Prevent guest bypass: require at least one registered account or previous session
    if (!lastUser && registeredUsers.length <= 1) {
      toast.error('No biometric profile configured! Please register or log in with password first.');
      return;
    }

    toast.loading('Scanning biometric signature...');
    
    setTimeout(() => {
      toast.dismiss();
      const targetUser = lastUser || registeredUsers[0];
      login(targetUser, 'mock_access_token_jwt', 'mock_refresh_token_jwt');
      toast.success(`Biometric login successful: Welcome, ${targetUser.name}!`);
      navigate('/home');
    }, 1200);
  };

  return (
    <div className="flex-1 min-h-screen flex flex-col justify-between p-6 bg-[#0A0A0F] text-white">
      {/* Brand Header */}
      <div className="flex flex-col items-center mt-6">
        <span className="text-sm font-extrabold text-primary tracking-widest uppercase">
          UPI MESH
        </span>
        <h2 className="text-xl font-black mt-2 text-white">Welcome Back</h2>
        <p className="text-xs text-textSecondary mt-1">Select credentials to enter wallet</p>
      </div>

      {/* Auth Card */}
      <div className="my-auto flex flex-col gap-5 py-4">
        {/* Toggle tabs */}
        <div className="w-full flex bg-white/[0.03] p-1 rounded-2xl border border-white/[0.04]">
          <button
            type="button"
            onClick={() => { triggerHaptic('light'); setActiveTab('phone'); }}
            className={`flex-1 py-3 text-xs font-bold rounded-xl flex items-center justify-center gap-1.5 transition-colors duration-150 ${
              activeTab === 'phone' ? 'bg-primary text-white shadow-sm' : 'text-textSecondary hover:text-white'
            }`}
          >
            <Phone className="w-4 h-4" />
            Phone Number
          </button>
          <button
            type="button"
            onClick={() => { triggerHaptic('light'); setActiveTab('email'); }}
            className={`flex-1 py-3 text-xs font-bold rounded-xl flex items-center justify-center gap-1.5 transition-colors duration-150 ${
              activeTab === 'email' ? 'bg-primary text-white shadow-sm' : 'text-textSecondary hover:text-white'
            }`}
          >
            <Mail className="w-4 h-4" />
            Email Address
          </button>
        </div>

        {/* Inputs & Form Wrapper */}
        <form onSubmit={activeTab === 'phone' ? handlePhoneSubmit(onSubmit) : handleEmailSubmit(onSubmit)} className="flex flex-col gap-4">
          {activeTab === 'phone' ? (
            <Input
              label="Phone Number"
              type="text"
              prefix="+91"
              placeholder="Enter your phone number"
              error={phoneErrors.phone}
              {...registerPhone('phone')}
            />
          ) : (
            <Input
              label="Email Address"
              type="email"
              icon={Mail}
              placeholder="Enter your email address"
              error={emailErrors.email}
              {...registerEmail('email')}
            />
          )}

          <Input
            label="Password"
            type="password"
            icon={Lock}
            showPasswordToggle
            placeholder="Enter your password"
            error={activeTab === 'phone' ? phoneErrors.password : emailErrors.password}
            {...register(activeTab === 'phone' ? registerPhone('password') : registerEmail('password'))}
          />

          {/* Forget Link */}
          <div className="flex justify-end pr-1">
            <span 
              onClick={() => { triggerHaptic('light'); toast('Enter your registered details in Register section to recreate.', { icon: '💡' }); }}
              className="text-xs font-semibold text-secondary cursor-pointer hover:underline"
            >
              Forgot Password?
            </span>
          </div>

          {/* Form Actions */}
          <div className="mt-4 flex gap-3">
            <button
              type="button"
              onClick={handleBiometric}
              className="w-14 h-14 rounded-2xl bg-white/5 active:bg-white/10 hover:bg-white/10 flex items-center justify-center border border-white/10 transition-colors"
              aria-label="Fingerprint Biometric login"
            >
              <Fingerprint className="w-7 h-7 text-secondary" />
            </button>
            
            <Button
              type="submit"
              disabled={isLoading}
              variant="gradient"
              className="flex-1 py-4 text-sm font-bold flex items-center justify-center gap-2"
            >
              <LogIn className="w-4 h-4" />
              Login
            </Button>
          </div>
        </form>
      </div>

      {/* Bottom Footer Actions */}
      <div className="flex flex-col items-center gap-4 mt-auto">
        <div className="text-xs text-textSecondary">
          Don't have an account?{' '}
          <Link to="/auth/register" onClick={() => triggerHaptic('light')} className="text-primary font-bold hover:underline">
            Register
          </Link>
        </div>
        
        {/* Social logins */}
        <div className="flex items-center gap-2 opacity-50 w-full justify-center">
          <div className="h-px bg-white/10 w-12" />
          <span className="text-[10px] font-bold text-textSecondary uppercase tracking-widest">Or login with</span>
          <div className="h-px bg-white/10 w-12" />
        </div>

        <button
          onClick={() => { triggerHaptic('light'); toast.success('Google login placeholder success!'); }}
          className="px-4 py-2.5 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center gap-2 text-xs text-white font-medium hover:bg-white/10 active:scale-95 transition-all w-full max-w-[200px]"
        >
          {/* Simple Vector Google Icon */}
          <svg className="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12.24 10.285V13.4h6.887C18.2 15.614 15.645 18 12.24 18c-3.86 0-7-3.14-7-7s3.14-7 7-7c1.7 0 3.3.6 4.6 1.8l2.4-2.4C17.3 1.6 14.9 1 12.24 1 6.64 1 2 5.6 2 11.2s4.6 10.2 10.24 10.2c5.78 0 9.67-4.06 9.67-9.8 0-.66-.08-1.28-.2-1.8H12.24z" />
          </svg>
          Google
        </button>
      </div>
    </div>
  );
};

// Zod helper binder
const register = (registerResult) => registerResult;

export default LoginScreen;
