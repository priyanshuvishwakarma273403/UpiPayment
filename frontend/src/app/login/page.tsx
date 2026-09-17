'use client';

import React, { useState } from 'react';
import { Card, CardHeader, CardTitle, CardDescription } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Alert } from '@/components/ui/Alert';
import { Badge } from '@/components/ui/Badge';
import { ShieldCheck, Lock, Mail, KeyRound, ArrowRight } from 'lucide-react';
import { useAuth } from '@/providers/AuthProvider';
import { authClient, AuthResponse } from '@/lib/api/auth-client';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otpPrompt, setOtpPrompt] = useState(false);
  const [otp, setOtp] = useState('');
  const [phoneForOtp, setPhoneForOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { login: setAuthSession } = useAuth();
  const router = useRouter();

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // Call authoritative backend Auth microservice endpoint: POST /auth/login
      const response: AuthResponse = await authClient.login({
        email: email.includes('@') ? email : undefined,
        phone: !email.includes('@') ? email : undefined,
        password,
      });

      const token = response.accessToken || response.token;
      const refreshToken = response.refreshToken;
      const userInfo = response.user || {
        id: response.userId || 'USR-001',
        name: response.fullName || 'Lead Investigator',
        email: response.email || email,
        phone: response.phoneNumber,
        upiId: response.upiId,
        roles: response.roles ? response.roles.split(',') : ['ROLE_ADMIN', 'INVESTIGATOR'],
      };

      if (!token) {
        throw new Error('Authentication response did not return a valid JWT access token.');
      }

      setAuthSession(token, refreshToken, {
        id: userInfo.id,
        username: userInfo.name || userInfo.email || 'Analyst',
        email: userInfo.email,
        role: userInfo.roles?.[0] || 'INVESTIGATOR',
        roles: userInfo.roles,
        upiId: userInfo.upiId,
      });

      // Navigate to onboarding overview first
      router.push('/onboarding');
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Authentication failed';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  const handleOtpVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await authClient.verifyOtp({ phone: phoneForOtp, otp });
      const token = response.accessToken || response.token;
      if (token) {
        setAuthSession(token, response.refreshToken, {
          id: response.userId || 'USR-001',
          username: response.fullName || 'Analyst',
          email: response.email,
          role: 'INVESTIGATOR',
          roles: ['INVESTIGATOR'],
        });
        router.push('/onboarding');
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'OTP Verification Failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center">
          <div className="inline-flex h-12 w-12 items-center justify-center rounded-xl bg-slate-900 text-white shadow-md">
            <ShieldCheck className="h-7 w-7 text-indigo-400" />
          </div>
          <h1 className="mt-3 text-2xl font-bold tracking-tight text-slate-900">SentinelX</h1>
          <p className="mt-1 text-sm text-slate-500">Enterprise Financial Fraud Intelligence Platform</p>
        </div>

        <Card className="shadow-lg">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Sign in to Portal</CardTitle>
                <CardDescription>Enter credentials for backend Auth microservice verification</CardDescription>
              </div>
              <Badge variant="success" size="sm" className="font-mono text-[9px]">
                AUTH_VERIFIED
              </Badge>
            </div>
          </CardHeader>

          {error && (
            <div className="mb-4">
              <Alert variant="error" title="Authentication Error">
                {error}
              </Alert>
            </div>
          )}

          {otpPrompt ? (
            <form onSubmit={handleOtpVerify} className="space-y-4">
              <Alert variant="info" title="Two-Factor OTP Required">
                Enter the 6-digit verification OTP sent to registered phone number.
              </Alert>
              <Input
                label="OTP Verification Code"
                type="text"
                required
                placeholder="6-digit OTP"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                leftIcon={<KeyRound className="h-4 w-4 text-slate-400" />}
              />
              <Button type="submit" variant="primary" fullWidth isLoading={loading} rightIcon={<ArrowRight className="h-4 w-4" />}>
                Verify OTP & Sign In
              </Button>
            </form>
          ) : (
            <form onSubmit={handleLoginSubmit} className="space-y-4">
              <div>
                <Input
                  label="Email or Phone Number"
                  type="text"
                  required
                  placeholder="analyst@sentinelx.io or 9876543210"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  leftIcon={<Mail className="h-4 w-4 text-slate-400" />}
                />
              </div>

              <div>
                <Input
                  label="Password"
                  type="password"
                  required
                  placeholder="••••••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  leftIcon={<Lock className="h-4 w-4 text-slate-400" />}
                />
              </div>

              <div className="flex items-center justify-between text-xs pt-1">
                <Link href="/forgot-password" className="font-semibold text-blue-700 hover:text-blue-900">
                  Forgot password?
                </Link>
              </div>

              <Button type="submit" variant="primary" fullWidth isLoading={loading} rightIcon={<ArrowRight className="h-4 w-4" />}>
                Sign In to Platform
              </Button>
            </form>
          )}
        </Card>

        <p className="text-center text-xs text-slate-500">
          Backend Microservice Target: <code className="font-mono text-slate-700">POST /auth/login</code>
        </p>
      </div>
    </div>
  );
}
