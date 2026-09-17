'use client';

import React from 'react';
import { Search, Bell, HelpCircle, Menu, Sun, Moon } from 'lucide-react';
import { motion } from 'framer-motion';
import { useUIStore } from '@/stores/uiStore';
import { useTheme } from '@/providers/ThemeProvider';
import { SystemStatusIndicator } from './SystemStatusIndicator';
import { UserDropdown } from './UserDropdown';
import { StreamStatusTicker } from './StreamStatusTicker';
import { QuickActionMenu } from './QuickActionMenu';

export function Topbar() {
  const setCommandPaletteOpen = useUIStore((state) => state.setCommandPaletteOpen);
  const setNotificationsDrawerOpen = useUIStore((state) => state.setNotificationsDrawerOpen);
  const setHelpModalOpen = useUIStore((state) => state.setHelpModalOpen);
  const toggleMobileSidebar = useUIStore((state) => state.toggleMobileSidebar);
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="sticky top-0 z-40 flex h-16 w-full items-center justify-between border-b border-slate-200/80 dark:border-slate-800/80 bg-white/85 dark:bg-slate-900/85 backdrop-blur-md px-4 md:px-6 transition-colors shadow-xs">
      {/* Left Section: Mobile Menu Toggle & Real-time Live Stream Ticker */}
      <div className="flex items-center gap-3">
        <button
          onClick={toggleMobileSidebar}
          className="md:hidden rounded-lg p-1.5 text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-100 transition-colors"
          aria-label="Open mobile navigation"
        >
          <Menu className="h-5 w-5" />
        </button>

        {/* Real-time Fraud Stream Status Ticker */}
        <StreamStatusTicker />
      </div>

      {/* Center Section: Global Search Command Trigger */}
      <div className="flex-1 max-w-md mx-4 hidden sm:block">
        <div
          onClick={() => setCommandPaletteOpen(true)}
          className="group flex w-full items-center gap-2 rounded-lg border border-slate-200 dark:border-slate-700/80 bg-slate-50/80 dark:bg-slate-800/50 px-3.5 py-1.5 text-xs text-slate-500 dark:text-slate-400 hover:bg-white dark:hover:bg-slate-800 hover:border-indigo-400 dark:hover:border-indigo-500 cursor-pointer transition-all shadow-xs"
        >
          <Search className="h-4 w-4 text-slate-400 group-hover:text-indigo-500 transition-colors shrink-0" />
          <span className="truncate flex-1 text-slate-400 group-hover:text-slate-600 dark:group-hover:text-slate-300">
            Search Transaction, Customer, Case, IP...
          </span>
          <kbd className="inline-flex items-center gap-0.5 rounded-md bg-slate-200/70 dark:bg-slate-700 px-1.5 py-0.5 text-[10px] font-mono text-slate-600 dark:text-slate-300 group-hover:bg-indigo-100 dark:group-hover:bg-indigo-900/60 group-hover:text-indigo-700 dark:group-hover:text-indigo-300 transition-colors">
            ⌘K
          </kbd>
        </div>
      </div>

      {/* Right Section: Quick Action, System Status, Theme Toggle, Notifications, User Dropdown */}
      <div className="flex items-center gap-2.5">
        {/* Quick Enforcement Dropdown */}
        <QuickActionMenu />

        {/* Divider */}
        <div className="hidden sm:block h-4 w-px bg-slate-200 dark:bg-slate-800 my-auto" />

        {/* System Health Status Indicator */}
        <div className="hidden sm:block">
          <SystemStatusIndicator />
        </div>

        {/* Theme Toggle Button with Rotate Animation */}
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          type="button"
          onClick={toggleTheme}
          className="rounded-lg p-1.5 text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-100 transition-colors"
          title={`Switch to ${theme === 'light' ? 'Dark' : 'Light'} Mode`}
          aria-label="Toggle theme"
        >
          {theme === 'dark' ? (
            <Sun className="h-4 w-4 text-amber-400" />
          ) : (
            <Moon className="h-4 w-4 text-slate-600" />
          )}
        </motion.button>

        {/* Notifications Drawer Trigger */}
        <button
          type="button"
          onClick={() => setNotificationsDrawerOpen(true)}
          className="relative rounded-lg p-1.5 text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-100 transition-colors"
          aria-label="View notifications"
        >
          <Bell className="h-4 w-4" />
          <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-red-600 animate-pulse" />
        </button>

        {/* Help Modal Trigger */}
        <button
          type="button"
          onClick={() => setHelpModalOpen(true)}
          className="hidden md:flex rounded-lg p-1.5 text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-100 transition-colors"
          aria-label="Help and documentation"
        >
          <HelpCircle className="h-4 w-4" />
        </button>

        {/* User Profile Dropdown */}
        <UserDropdown />
      </div>
    </header>
  );
}
