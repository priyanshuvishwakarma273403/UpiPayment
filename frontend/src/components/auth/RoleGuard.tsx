"use client";

import React from "react";
import { useAuth } from "@/providers/AuthProvider";
import { useRouter } from "next/navigation";
import { ForbiddenContent } from "./ForbiddenContent";

export interface RoleGuardProps {
  roles?: string[];
  allowedRoles?: string[];
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export function RoleGuard({ roles: directRoles, allowedRoles, children, fallback }: RoleGuardProps) {
  const targetRoles = directRoles || allowedRoles || [];
  const { user, hasAnyRole } = useAuth();
  const router = useRouter();

  if (!user) {
    if (typeof window !== "undefined") {
      router.push("/login");
    }
    return null;
  }

  const isAllowed = hasAnyRole(targetRoles);

  if (!isAllowed) {
    return fallback ? <>{fallback}</> : <ForbiddenContent requiredRoles={targetRoles} />;
  }

  return <>{children}</>;
}
