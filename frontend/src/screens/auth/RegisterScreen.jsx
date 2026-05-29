import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { User, Mail, Phone, Lock, Eye, EyeOff, CheckSquare, UserPlus } from 'lucide-react';
import toast from 'react-hot-toast';

import { useHaptic } from '../../hooks/useHaptic';
import authApi from '../../api/authApi';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';

const registerSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  email: z.string().email('Invalid email address'),
  phone: z.string().min(10, 'Phone must be exactly 10 digits').max(10, 'Phone must be exactly 10 digits').regex(/^[0-9]+$/, 'Must contain numbers only'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string().min(6, 'Confirm password must be at least 6 characters'),
  terms: z.boolean().refine(val => val === true, 'You must accept the terms and conditions')
}).refine(data => data.password === data.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword']
});

export const RegisterScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const [isLoading, setIsLoading] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState(0); // 0 to 4
  const [strengthLabel, setStrengthLabel] = useState('Empty');

  const { register, handleSubmit, watch, formState: { errors } } = useForm({
    resolver: zodResolver(registerSchema)
  });

  const passwordVal = watch('password', '');

  // Calculate password strength
  useEffect(() => {
    if (!passwordVal) {
      setPasswordStrength(0);
      setStrengthLabel('Empty');
      return;
    }

    let strength = 0;
    if (passwordVal.length >= 6) strength += 1;
    if (passwordVal.length >= 10) strength += 1;
    if (/[0-9]/.test(passwordVal)) strength += 1;
    if (/[^A-Za-z0-9]/.test(passwordVal)) strength += 1;

    setPasswordStrength(strength);

    const labels = ['Weak', 'Fair', 'Good', 'Strong', 'Excellent'];
    setStrengthLabel(labels[strength]);
  }, [passwordVal]);

  const onSubmit = async (data) => {
    triggerHaptic('light');
    setIsLoading(true);
    const toastId = toast.loading('Creating account...');

    try {
      const payload = {
        name: data.name,
        email: data.email,
        phone: data.phone,
        password: data.password
      };

      await authApi.register(payload);
      
      toast.success('Account created successfully!', { id: toastId });
      // Redirect to OTP verification using the entered phone number
      navigate('/auth/verify-otp', { state: { phone: data.phone } });
    } catch (error) {
      console.error("Registration failed, saving locally:", error);
      setIsLoading(false);
      
      // Save user details locally in the mock database
      const registerLocalUser = useAuthStore.getState().registerLocalUser;
      registerLocalUser({
        name: data.name,
        email: data.email,
        phone: data.phone,
        password: data.password
      });

      toast.success('Account registered locally! Verifying OTP...', { id: toastId });
      navigate('/auth/verify-otp', { state: { phone: data.phone } });
    }
  };

  const getStrengthBarColor = (index) => {
    if (index >= passwordStrength) return 'bg-white/10';
    if (passwordStrength <= 1) return 'bg-danger';
    if (passwordStrength <= 3) return 'bg-warning';
    return 'bg-success';
  };

  return (
    <div className="flex-1 min-h-screen flex flex-col justify-between p-6 bg-[#0A0A0F] text-white">
      {/* Header */}
      <div className="flex flex-col items-center mt-4">
        <span className="text-xs font-extrabold text-primary tracking-widest uppercase">
          UPI MESH
        </span>
        <h2 className="text-xl font-black mt-1.5 text-white">Create Account</h2>
        <p className="text-xs text-textSecondary mt-0.5">Initialize your unified fintech wallet</p>
      </div>

      {/* Inputs Frame */}
      <div className="my-auto py-4 flex flex-col gap-4 overflow-y-auto no-scrollbar max-h-[70vh]">
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3.5">
           <Input
            label="Full Name"
            type="text"
            icon={User}
            placeholder="Enter your full name"
            error={errors.name}
            {...register('name')}
          />

          <Input
            label="Email Address"
            type="email"
            icon={Mail}
            placeholder="Enter your email address"
            error={errors.email}
            {...register('email')}
          />

          <Input
            label="Phone Number"
            type="text"
            prefix="+91"
            placeholder="10-digit mobile number"
            error={errors.phone}
            {...register('phone')}
          />

          <Input
            label="Password"
            type="password"
            icon={Lock}
            showPasswordToggle
            placeholder="Enter secure password"
            error={errors.password}
            {...register('password')}
          />

          {/* Password Strength Meter */}
          {passwordVal && (
            <div className="flex flex-col gap-1.5 px-1">
              <div className="flex justify-between items-center text-[10px] font-bold uppercase tracking-wider text-textSecondary">
                <span>Strength</span>
                <span className={passwordStrength >= 3 ? 'text-success' : passwordStrength >= 2 ? 'text-warning' : 'text-danger'}>
                  {strengthLabel}
                </span>
              </div>
              <div className="flex gap-1">
                {Array.from({ length: 4 }).map((_, index) => (
                  <div
                    key={index}
                    className={`h-1 flex-1 rounded-full transition-colors duration-200 ${getStrengthBarColor(index)}`}
                  />
                ))}
              </div>
            </div>
          )}

          <Input
            label="Confirm Password"
            type="password"
            icon={Lock}
            showPasswordToggle
            placeholder="Confirm your password"
            error={errors.confirmPassword}
            {...register('confirmPassword')}
          />

          {/* Terms checkbox */}
          <div className="flex items-start gap-2.5 mt-2 px-1">
            <input
              type="checkbox"
              id="terms"
              className="mt-0.5 rounded border-white/10 bg-white/5 text-primary focus:ring-primary focus:ring-offset-[#0A0A0F]"
              {...register('terms')}
            />
            <label htmlFor="terms" className="text-xs text-textSecondary leading-snug">
              I agree to the{' '}
              <span className="text-secondary font-semibold hover:underline cursor-pointer" onClick={() => toast('Terms of Service agreement loaded', { icon: '📄' })}>
                Terms & Conditions
              </span>{' '}
              and Privacy Policy.
            </label>
          </div>
          {errors.terms && (
            <span className="text-xs font-semibold text-danger pl-1 animate-pulse">
              {errors.terms.message}
            </span>
          )}

          {/* Submit */}
          <Button
            type="submit"
            disabled={isLoading}
            variant="gradient"
            className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2 mt-4"
          >
            <UserPlus className="w-4 h-4" />
            Create Account
          </Button>
        </form>
      </div>

      {/* Footer Link */}
      <div className="text-xs text-textSecondary text-center mt-auto">
        Already have an account?{' '}
        <Link to="/auth/login" onClick={() => triggerHaptic('light')} className="text-primary font-bold hover:underline">
          Login
        </Link>
      </div>
    </div>
  );
};

export default RegisterScreen;
