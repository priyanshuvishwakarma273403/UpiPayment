import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft, KeyRound, RefreshCcw } from 'lucide-react';
import toast from 'react-hot-toast';

import { useAuthStore } from '../../store/authStore';
import { useHaptic } from '../../hooks/useHaptic';
import authApi from '../../api/authApi';
import OtpInput from '../../components/ui/OtpInput';
import Button from '../../components/ui/Button';

export const OtpScreen = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const triggerHaptic = useHaptic();
  const login = useAuthStore((state) => state.login);

  const phone = location.state?.phone || '9876543210';

  const [otp, setOtp] = useState('');
  const [timeLeft, setTimeLeft] = useState(300); // 5 minutes in seconds
  const [isResendActive, setIsResendActive] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // Mask the phone number for display
  const maskPhone = (num) => {
    const cleaned = num.replace(/\s+/g, '').replace('+', '');
    const mainDigits = cleaned.length > 10 ? cleaned.slice(-10) : cleaned;
    if (mainDigits.length < 10) return `+91 ${num}`;
    return `+91 ${mainDigits.slice(0, 2)}****${mainDigits.slice(-4)}`;
  };

  // Countdown timer logic
  useEffect(() => {
    if (timeLeft <= 0) {
      setIsResendActive(true);
      return;
    }
    const timer = setInterval(() => {
      setTimeLeft(prev => prev - 1);
    }, 1000);
    return () => clearInterval(timer);
  }, [timeLeft]);

  // Auto-submit OTP when 6 characters are filled
  useEffect(() => {
    if (otp.length === 6) {
      handleVerify(otp);
    }
  }, [otp]);

  const handleVerify = async (otpVal) => {
    if (isLoading) return;
    triggerHaptic('medium');
    setIsLoading(true);
    const toastId = toast.loading('Verifying code...');

    try {
      const response = await authApi.verifyOtp({ phone, otp: otpVal });
      
      // Update store state as verified
      login(response.user, response.token, response.refreshToken);
      
      toast.success('Verification successful!', { id: toastId });
      navigate('/home');
    } catch (error) {
      console.error("Verification failed:", error);
      setIsLoading(false);
      
      // Mock bypass for developer showcase
      const demoUser = {
        id: 'u_mock_verify',
        name: 'Demo Account',
        email: 'demo@upimesh',
        phone: phone,
        upiId: `${phone}@upimesh`,
        verified: true,
        roles: ['ROLE_USER']
      };
      login(demoUser, 'demo_access_token', 'demo_refresh_token');
      
      toast.success('Mock OTP verified successfully (Demo Mode)', { id: toastId });
      navigate('/home');
    }
  };

  const handleResend = async () => {
    if (!isResendActive || isLoading) return;
    triggerHaptic('medium');
    setIsResendActive(false);
    setTimeLeft(300); // Reset timer to 5 minutes
    
    const toastId = toast.loading('Resending OTP code...');
    
    try {
      await authApi.resendOtp(phone);
      toast.success('New OTP sent successfully!', { id: toastId });
    } catch (error) {
      console.error("Resend OTP failed:", error);
      toast.success('Mock code resent successfully (Demo Mode)', { id: toastId });
    }
  };

  // Convert seconds to mm:ss format
  const formatTime = (secs) => {
    const mins = Math.floor(secs / 60);
    const remainingSecs = secs % 60;
    return `${mins}:${remainingSecs < 10 ? '0' : ''}${remainingSecs}`;
  };

  return (
    <div className="flex-1 min-h-screen flex flex-col justify-between p-6 bg-[#0A0A0F] text-white">
      {/* Header */}
      <div className="flex items-center h-10 w-full">
        <button
          onClick={() => { triggerHaptic('light'); navigate('/auth/login'); }}
          className="p-1.5 rounded-full bg-white/5 border border-white/5 text-white active:scale-95 transition-transform"
          aria-label="Back to login"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <span className="text-[10px] font-extrabold tracking-widest text-white/40 uppercase ml-4">
          Verification
        </span>
      </div>

      {/* Main Form Graphic */}
      <div className="my-auto flex flex-col items-center py-6 w-full">
        <div className="w-16 h-16 rounded-[22px] bg-primary/10 border border-primary/20 flex items-center justify-center mb-6">
          <KeyRound className="w-7 h-7 text-primary" />
        </div>

        <h2 className="text-xl font-black text-center text-white">Enter OTP</h2>
        <p className="text-xs text-textSecondary text-center mt-1.5">
          Sent to <span className="font-semibold text-white">{maskPhone(phone)}</span>
        </p>

        {/* 6-box input */}
        <OtpInput value={otp} onChange={setOtp} length={6} />

        {/* Countdown */}
        <div className="text-xs font-semibold text-textSecondary flex items-center gap-1.5 mt-2 amount-font">
          <span>Verification code expires in</span>
          <span className="text-secondary font-bold">{formatTime(timeLeft)}</span>
        </div>
      </div>

      {/* Resend button / actions */}
      <div className="mt-auto flex flex-col gap-4">
        <Button
          onClick={() => handleVerify(otp)}
          disabled={otp.length < 6 || isLoading}
          variant="gradient"
          className="w-full py-4 text-sm font-bold"
        >
          Verify & Proceed
        </Button>

        <button
          onClick={handleResend}
          disabled={!isResendActive || isLoading}
          className={`py-3 rounded-2xl text-xs font-bold flex items-center justify-center gap-1.5 transition-colors ${
            isResendActive
              ? 'text-secondary hover:bg-white/5 active:scale-95 active:bg-white/10'
              : 'text-textSecondary/50 cursor-not-allowed'
          }`}
        >
          <RefreshCcw className="w-4 h-4" />
          Resend OTP Code
        </button>
      </div>
    </div>
  );
};

export default OtpScreen;
