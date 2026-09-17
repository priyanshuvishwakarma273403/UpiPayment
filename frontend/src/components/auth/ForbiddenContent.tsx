"use client";

import React from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Lock, ArrowLeft } from "lucide-react";
import Link from "next/link";

export function ForbiddenContent({ requiredRoles }: { requiredRoles?: string[] }) {
  return (
    <div className="flex min-h-[70vh] items-center justify-center p-4">
      <div className="w-full max-w-md space-y-6 text-center">
        <div className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-red-100 text-red-700 shadow-sm border border-red-200">
          <Lock className="h-8 w-8" />
        </div>

        <Card className="shadow-lg">
          <CardHeader>
            <CardTitle className="text-xl">Access Denied (HTTP 403)</CardTitle>
            <CardDescription>You do not possess the required RBAC role permissions to access this feature.</CardDescription>
          </CardHeader>

          <CardContent className="space-y-4 text-xs text-slate-600">
            {requiredRoles && (
              <div className="rounded-lg bg-slate-50 p-3 border border-slate-200 text-left">
                <span className="font-semibold text-slate-700 block mb-1">Required Role Clearance:</span>
                <div className="flex flex-wrap gap-1 mt-1">
                  {requiredRoles.map((role) => (
                    <Badge key={role} variant="danger" size="sm" className="font-mono">
                      {role}
                    </Badge>
                  ))}
                </div>
              </div>
            )}

            <div className="pt-2">
              <Link href="/dashboard" className="w-full">
                <Button variant="outline" fullWidth leftIcon={<ArrowLeft className="h-4 w-4" />}>
                  Back to Executive Overview
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
