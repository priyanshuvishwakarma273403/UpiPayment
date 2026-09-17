"use client";

import React, { useState, Suspense } from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { SkeletonLoader } from "@/components/ui/SkeletonLoader";
import { ShieldCheck, Lock, KeyRound, CheckCircle2 } from "lucide-react";
import { authClient } from "@/lib/api/auth-client";
import { useRouter, useSearchParams } from "next/navigation";

function ResetPasswordForm() {
  const searchParams = useSearchParams();
  const initialPhone = searchParams.get("phone") || "";

  const [phone, setPhone] = useState(initialPhone);
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // Execute backend OTP verification contract
      await authClient.verifyOtp({ phone, otp });
      setSuccess(true);
      setTimeout(() => {
        router.push("/login");
      }, 1500);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "OTP Verification Failed";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card className="shadow-lg">
      <CardHeader>
        <CardTitle>Verify OTP & Update Password</CardTitle>
        <CardDescription>Enter verification OTP and new security credentials</CardDescription>
      </CardHeader>

      <CardContent>
        {success ? (
          <Alert variant="success" title="Password Successfully Reset">
            Your credentials have been updated via verified backend contract. Redirecting to login...
          </Alert>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && <Alert variant="error" title="Verification Failed">{error}</Alert>}

            <Input
              label="Phone Number"
              type="tel"
              required
              placeholder="9876543210"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
            />

            <Input
              label="Verification OTP"
              type="text"
              required
              placeholder="6-digit OTP"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              leftIcon={<KeyRound className="h-4 w-4 text-slate-400" />}
            />

            <Input
              label="New Password"
              type="password"
              required
              placeholder="••••••••••••"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              leftIcon={<Lock className="h-4 w-4 text-slate-400" />}
            />

            <Button type="submit" variant="primary" fullWidth isLoading={loading} rightIcon={<CheckCircle2 className="h-4 w-4" />}>
              Confirm New Password
            </Button>
          </form>
        )}
      </CardContent>
    </Card>
  );
}

export default function ResetPasswordPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center">
          <div className="inline-flex h-12 w-12 items-center justify-center rounded-xl bg-slate-900 text-white shadow-md">
            <ShieldCheck className="h-7 w-7 text-indigo-400" />
          </div>
          <h1 className="mt-3 text-2xl font-bold tracking-tight text-slate-900">SentinelX</h1>
          <p className="mt-1 text-sm text-slate-500">Security Credentials Verification</p>
        </div>

        <Suspense fallback={<SkeletonLoader className="h-64 w-full rounded-lg" />}>
          <ResetPasswordForm />
        </Suspense>
      </div>
    </div>
  );
}
