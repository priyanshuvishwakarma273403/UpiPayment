import { useUIStore } from "@/stores/uiStore";

export function useToast() {
  const addToast = useUIStore((state) => state.addToast);

  return {
    toast: (title: string, description?: string, variant: "success" | "warning" | "danger" | "info" | "neutral" = "info") => {
      addToast({ title, description, variant });
    },
    success: (title: string, description?: string) => {
      addToast({ title, description, variant: "success" });
    },
    warning: (title: string, description?: string) => {
      addToast({ title, description, variant: "warning" });
    },
    danger: (title: string, description?: string) => {
      addToast({ title, description, variant: "danger" });
    },
    info: (title: string, description?: string) => {
      addToast({ title, description, variant: "info" });
    },
  };
}
