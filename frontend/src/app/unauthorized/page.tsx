"use client";

import React from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { ShieldAlert, LogIn, ArrowRight } from "lucide-react";
import Link from "next/link";

export default function UnauthorizedPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
      <div className="w-full max-w-md space-y-6 text-center">
        <div className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-amber-100 text-amber-800 shadow-sm border border-amber-200">
          <ShieldAlert className="h-8 w-8" />
        </div>

        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="text-xl">Authentication Required (HTTP 401)</CardTitle>
            <CardDescription>Your session has expired or requires valid investigator credentials</CardDescription>
          </CardHeader>

          <CardContent className="space-y-4 text-xs text-slate-600">
            <div className="rounded-lg bg-amber-50 p-3 border border-amber-200 text-left">
              <span className="font-semibold text-amber-900">Security Invalidation:</span> Access token missing or invalid. Please re-authenticate via the secure login portal.
            </div>

            <div className="pt-2">
              <Link href="/login" className="w-full">
                <Button variant="primary" fullWidth leftIcon={<LogIn className="h-4 w-4" />} rightIcon={<ArrowRight className="h-4 w-4" />}>
                  Return to Sign In Portal
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>

        <p className="text-xs text-slate-400">SentinelX Multi-Factor Session Security</p>
      </div>
    </div>
  );
}
