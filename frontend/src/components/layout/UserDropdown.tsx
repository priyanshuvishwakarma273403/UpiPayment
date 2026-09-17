"use client";

import React from "react";
import { Dropdown, DropdownItem } from "@/components/ui/Dropdown";
import { useAuth } from "@/providers/AuthProvider";
import { useRouter } from "next/navigation";
import { User, Settings, Shield, LogOut } from "lucide-react";

export function UserDropdown() {
  const { user, logout } = useAuth();
  const router = useRouter();

  const handleSignOut = () => {
    logout();
    router.push("/login");
  };

  const menuItems: DropdownItem[] = [
    {
      label: "Analyst Profile",
      icon: <User className="h-4 w-4" />,
      onClick: () => router.push("/settings"),
    },
    {
      label: "Workspace Preferences",
      icon: <Settings className="h-4 w-4" />,
      onClick: () => router.push("/settings"),
    },
    {
      label: "Security & MFA Credentials",
      icon: <Shield className="h-4 w-4" />,
      onClick: () => router.push("/settings"),
    },
    {
      label: "Sign out",
      icon: <LogOut className="h-4 w-4" />,
      danger: true,
      divider: true,
      onClick: handleSignOut,
    },
  ];

  const trigger = (
    <div className="flex items-center gap-3 border-l border-slate-200 pl-4 hover:opacity-90 transition-opacity">
      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-900 text-white font-semibold text-xs shadow-xs">
        {user?.username?.substring(0, 2).toUpperCase() || "SA"}
      </div>
      <div className="hidden md:block text-left">
        <p className="text-xs font-semibold text-slate-900 leading-tight">
          {user?.username || "Sarah Analyst"}
        </p>
        <p className="text-[10px] text-slate-500 font-mono">{user?.role || "SENIOR_INVESTIGATOR"}</p>
      </div>
    </div>
  );

  return <Dropdown trigger={trigger} items={menuItems} align="right" />;
}
