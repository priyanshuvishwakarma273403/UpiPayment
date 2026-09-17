"use client";

import React, { useState } from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Alert } from "@/components/ui/Alert";
import { ShieldCheck, Mail, Phone, ArrowLeft, CheckCircle2 } from "lucide-react";
import { authClient } from "@/lib/api/auth-client";
import Link from "next/link";

export default function ForgotPasswordPage() {
  const [phone, setPhone] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await authClient.resendOtp({ phone });
      setSent(true);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Failed to send reset code";
      setError(msg);
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
          <p className="mt-1 text-sm text-slate-500">Security Credentials Recovery</p>
        </div>

        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle>Reset your password</CardTitle>
            <CardDescription>Enter registered phone number to receive security verification OTP</CardDescription>
          </CardHeader>

          <CardContent>
            {sent ? (
              <div className="space-y-4 text-xs">
                <Alert variant="success" title="Verification OTP Dispatched">
                  OTP reset code sent to <strong>{phone}</strong>. Use the verification code to finalize password reset.
                </Alert>
                <Link href={`/reset-password?phone=${encodeURIComponent(phone)}`} className="w-full">
                  <Button variant="primary" fullWidth rightIcon={<CheckCircle2 className="h-4 w-4" />}>
                    Proceed to Reset Password Form
                  </Button>
                </Link>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                {error && <Alert variant="error" title="Recovery Failed">{error}</Alert>}

                <Input
                  label="Registered Phone Number"
                  type="tel"
                  required
                  placeholder="9876543210"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  leftIcon={<Phone className="h-4 w-4 text-slate-400" />}
                />

                <Button type="submit" variant="primary" fullWidth isLoading={loading}>
                  Send Verification OTP
                </Button>
              </form>
            )}

            <div className="mt-4 pt-3 border-t border-slate-100 text-center">
              <Link href="/login" className="text-xs font-semibold text-blue-700 hover:text-blue-900 inline-flex items-center gap-1">
                <ArrowLeft className="h-3.5 w-3.5" /> Back to Sign In
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
