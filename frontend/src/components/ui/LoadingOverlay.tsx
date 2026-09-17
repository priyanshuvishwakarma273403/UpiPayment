import React from "react";

export function LoadingOverlay({ message = "Loading data..." }: { message?: string }) {
  return (
    <div className="flex h-64 w-full flex-col items-center justify-center space-y-3 rounded-lg border border-slate-200 bg-white p-8">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
      <p className="text-xs font-medium text-slate-600">{message}</p>
    </div>
  );
}
