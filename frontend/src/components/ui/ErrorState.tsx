import React from "react";
import { AlertTriangle, RefreshCw } from "lucide-react";
import { Button } from "./Button";

export interface ErrorStateProps {
  title?: string;
  message?: string;
  onRetry?: () => void;
}

export function ErrorState({
  title = "Failed to load data",
  message = "An error occurred while fetching information from the backend service.",
  onRetry,
}: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-red-200 bg-red-50/50 p-8 text-center">
      <div className="mb-3 rounded-full bg-red-100 p-3 text-red-600">
        <AlertTriangle className="h-6 w-6" />
      </div>
      <h3 className="text-sm font-semibold text-red-900">{title}</h3>
      <p className="mt-1 max-w-md text-xs text-red-700">{message}</p>
      {onRetry && (
        <div className="mt-4">
          <Button variant="outline" size="sm" onClick={onRetry} className="border-red-300 text-red-700 hover:bg-red-50">
            <RefreshCw className="mr-2 h-3.5 w-3.5" />
            Retry Request
          </Button>
        </div>
      )}
    </div>
  );
}
