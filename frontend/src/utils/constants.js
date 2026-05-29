export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
export const APP_NAME = import.meta.env.VITE_APP_NAME || 'UPI Mesh';
export const DEFAULT_CURRENCY = import.meta.env.VITE_DEFAULT_CURRENCY || 'INR';

export const MOCK_BANKS = [
  { id: 'bank_sbi', name: 'State Bank of India', code: 'SBI', logo: '🏛️', suffix: '@sbi' },
  { id: 'bank_hdfc', name: 'HDFC Bank', code: 'HDFC', logo: '🏦', suffix: '@hdfc' },
  { id: 'bank_icici', name: 'ICICI Bank', code: 'ICICI', logo: '💳', suffix: '@icici' },
  { id: 'bank_gpay', name: 'UPI Mesh Wallet', code: 'UPIMESH', logo: '⚡', suffix: '@upimesh' }
];

export const MOCK_CONTACTS = [
  { id: 'c1', name: 'Aarav Sharma', phone: '+91 9876543210', upiId: 'aarav@upimesh', avatar: 'AS' },
  { id: 'c2', name: 'Ishita Patel', phone: '+91 9123456789', upiId: 'ishita@upimesh', avatar: 'IP' },
  { id: 'c3', name: 'Kabir Verma', phone: '+91 8765432109', upiId: 'kabir@upimesh', avatar: 'KV' },
  { id: 'c4', name: 'Diya Sen', phone: '+91 7654321098', upiId: 'diya@upimesh', avatar: 'DS' },
  { id: 'c5', name: 'Rohan Gupta', phone: '+91 6543210987', upiId: 'rohan@upimesh', avatar: 'RG' },
  { id: 'c6', name: 'Meera Nair', phone: '+91 9988776655', upiId: 'meera@upimesh', avatar: 'MN' }
];

export const EXPENSE_CATEGORIES = [
  { name: 'Food & Dining', value: 30, color: '#6C63FF' },
  { name: 'Shopping', value: 25, color: '#00D2FF' },
  { name: 'Travel', value: 20, color: '#00E676' },
  { name: 'Entertainment', value: 15, color: '#FFD93D' },
  { name: 'Other', value: 10, color: '#FF4757' }
];
