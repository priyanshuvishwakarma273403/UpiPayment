/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./app/**/*.{js,jsx,ts,tsx}",
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      colors: {
        background: "#030712",
        surface: "#0f172a",
        border: "rgba(255, 255, 255, 0.08)",
        primary: {
          DEFAULT: "#2563eb",
          hover: "#1d4ed8",
        },
        accent: "#6366f1",
        risk: {
          critical: "#ef4444",
          high: "#f97316",
          medium: "#eab308",
          safe: "#10b981",
        },
      },
    },
  },
  plugins: [],
};
