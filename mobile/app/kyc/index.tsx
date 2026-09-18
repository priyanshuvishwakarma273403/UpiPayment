import React from 'react';
import { View, Text } from 'react-native';

export default function KycScreen() {
  return (
    <View className="flex-1 bg-background justify-center items-center px-6">
      <Text className="text-white text-xl font-bold font-mono">KYC Verification</Text>
      <Text className="text-slate-400 text-xs mt-2 text-center">
        Tier 0 (Unverified) • Tier 1 (Aadhaar OTP) • Tier 2 (Face Liveness & PAN)
      </Text>
    </View>
  );
}
