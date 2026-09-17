import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: 'class',
  content: [
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/features/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "var(--background)",
        foreground: "var(--foreground)",
        card: {
          DEFAULT: "var(--card-bg)",
          foreground: "var(--card-fg)",
        },
        primary: {
          DEFAULT: "#1e40af",
          foreground: "#ffffff",
          hover: "#1e3a8a",
        },
        secondary: {
          DEFAULT: "#f1f5f9",
          foreground: "#0f172a",
        },
        accent: {
          DEFAULT: "#0284c7",
          foreground: "#ffffff",
        },
        surface: {
          subtle: "#f8fafc",
          border: "#e2e8f0",
          hover: "#f1f5f9",
        },
        risk: {
          critical: "#dc2626",
          high: "#ea580c",
          medium: "#d97706",
          low: "#16a34a",
          safe: "#059669",
        }
      },
      fontFamily: {
        sans: ["var(--font-sans)", "Segoe UI", "system-ui", "sans-serif"],
        mono: ["Consolas", "Monaco", "monospace"],
      },
    },
  },
  plugins: [],
};

export default config;
