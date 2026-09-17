import {
  LayoutDashboard,
  ArrowLeftRight,
  ShieldAlert,
  ShieldCheck,
  Briefcase,
  Users,
  Store,
  Network,
  FileSearch,
  Bot,
  BarChart3,
  FileSpreadsheet,
  BookOpen,
  Cpu,
  PlaySquare,
  Bell,
  ScrollText,
  Settings,
  Shield,
  BrainCircuit,
  Radio,
} from "lucide-react";

export interface NavItem {
  title: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  badge?: string;
  category?: 'primary' | 'secondary';
}

export interface NavSection {
  section: string;
  items: NavItem[];
}

export const PRIMARY_NAVIGATION: NavItem[] = [
  { title: "Overview", href: "/dashboard", icon: LayoutDashboard, category: 'primary' },
  { title: "Transactions", href: "/transactions", icon: ArrowLeftRight, category: 'primary' },
  { title: "Risk", href: "/risk", icon: ShieldAlert, category: 'primary' },
  { title: "Fraud", href: "/fraud", icon: ShieldCheck, category: 'primary' },
  { title: "Cases", href: "/cases", icon: Briefcase, category: 'primary' },
  { title: "Customers", href: "/customers", icon: Users, category: 'primary' },
  { title: "Merchants", href: "/merchants", icon: Store, category: 'primary' },
  { title: "Network", href: "/network", icon: Network, category: 'primary' },
  { title: "Investigations", href: "/investigations", icon: FileSearch, category: 'primary' },
  { title: "AI Investigator", href: "/ai-investigator", icon: Bot, category: 'primary' },
  { title: "Analytics", href: "/analytics", icon: BarChart3, category: 'primary' },
  { title: "Reports", href: "/reports", icon: FileSpreadsheet, category: 'primary' },
];

export const SECONDARY_NAVIGATION: NavItem[] = [
  { title: "War Room", href: "/war-room", icon: Radio, category: 'secondary' },
  { title: "Sanctions", href: "/sanctions", icon: ShieldCheck, category: 'secondary' },
  { title: "SAR Reports", href: "/sar-reports", icon: FileSpreadsheet, category: 'secondary' },
  { title: "Knowledge", href: "/knowledge", icon: BookOpen, category: 'secondary' },
  { title: "MCP", href: "/mcp", icon: Cpu, category: 'secondary' },
  { title: "Simulations", href: "/simulations", icon: PlaySquare, category: 'secondary' },
  { title: "Notifications", href: "/notifications", icon: Bell, category: 'secondary' },
  { title: "Audit", href: "/audit", icon: ScrollText, category: 'secondary' },
  { title: "Settings", href: "/settings", icon: Settings, category: 'secondary' },
];

export const NAVIGATION_CONFIG: NavSection[] = [
  {
    section: "Primary Operations",
    items: PRIMARY_NAVIGATION,
  },
  {
    section: "Platform & Intelligence",
    items: SECONDARY_NAVIGATION,
  },
  {
    section: "Governance & Admin",
    items: [
      { title: "ML Models", href: "/models", icon: BrainCircuit, category: 'secondary' },
      { title: "Admin Console", href: "/admin", icon: Shield, category: 'secondary' },
    ],
  },
];
