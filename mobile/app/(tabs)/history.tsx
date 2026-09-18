import React from 'react';
import { View, Text } from 'react-native';

export default function HistoryScreen() {
  return (
    <View className="flex-1 bg-background px-4 pt-12">
      <Text className="text-white text-xl font-bold font-mono">Transaction Ledger</Text>
      <Text className="text-slate-400 text-xs mt-1">Immutable record of all debits, credits, and syncs</Text>
    </View>
  );
}
