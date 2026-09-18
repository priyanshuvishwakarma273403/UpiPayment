import React from 'react';
import { View, Text, ScrollView } from 'react-native';

export default function HomeScreen() {
  return (
    <ScrollView className="flex-1 bg-background px-4 pt-12">
      <Text className="text-white text-2xl font-bold font-mono">UPI Mesh</Text>
      <Text className="text-slate-400 text-xs mt-1">Real-Time Sovereign Digital Wallet</Text>
    </ScrollView>
  );
}
