/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class', // support toggling if needed, default dark
  theme: {
    extend: {
      colors: {
        darkBg: '#0A0A0F',
        cardBg: '#12121A',
        surface: '#1A1A2E',
        primary: '#6C63FF',
        secondary: '#00D2FF',
        success: '#00E676',
        danger: '#FF4757',
        warning: '#FFD93D',
        textPrimary: '#FFFFFF',
        textSecondary: '#8B8B9E',
        borderColor: 'rgba(255,255,255,0.08)',
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        mono: ['DM Mono', 'monospace'],
      },
      backgroundImage: {
        'header-grad': 'linear-gradient(135deg, #6C63FF 0%, #00D2FF 100%)',
        'success-grad': 'linear-gradient(135deg, #00E676 0%, #00B248 100%)',
        'wallet-grad': 'linear-gradient(135deg, #1A1A2E 0%, #16213E 50%, #0F3460 100%)',
        'btn-grad': 'linear-gradient(135deg, #6C63FF 0%, #00D2FF 100%)',
      },
      boxShadow: {
        'glow-primary': '0 0 20px rgba(108, 99, 255, 0.4)',
        'glow-secondary': '0 0 20px rgba(0, 210, 255, 0.4)',
        'glow-success': '0 0 20px rgba(0, 230, 118, 0.4)',
        'glass': 'inset 0 1px 1px rgba(255, 255, 255, 0.1)',
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'shine': 'shine 2s infinite',
        'scan-line': 'scan 2s linear infinite',
      },
      keyframes: {
        shine: {
          '0%': { transform: 'translateX(-100%)' },
          '100%': { transform: 'translateX(100%)' },
        },
        scan: {
          '0%': { top: '0%' },
          '50%': { top: '100%' },
          '100%': { top: '0%' },
        }
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
