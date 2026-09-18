export interface UserProfile {
  id: string;
  name: string;
  email: string;
  phone: string;
  upiId: string;
  kycTier: 0 | 1 | 2;
  dailyLimit: number;
}
