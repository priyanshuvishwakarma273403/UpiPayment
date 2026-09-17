import { cn } from "@/lib/utils";

export interface SkeletonLoaderProps extends React.HTMLAttributes<HTMLDivElement> {
  count?: number;
  height?: number;
}

export function SkeletonLoader({ className, count, height, style, ...props }: SkeletonLoaderProps) {
  if (count && count > 1) {
    return (
      <div className="space-y-2">
        {Array.from({ length: count }).map((_, i) => (
          <div
            key={i}
            className={cn("animate-pulse rounded-md bg-slate-200/80", className)}
            style={{ height: height ? `${height}px` : undefined, ...style }}
            {...props}
          />
        ))}
      </div>
    );
  }
  return (
    <div
      className={cn("animate-pulse rounded-md bg-slate-200/80", className)}
      style={{ height: height ? `${height}px` : undefined, ...style }}
      {...props}
    />
  );
}

export function TableSkeleton({ rows = 5, cols = 4 }: { rows?: number; cols?: number }) {
  return (
    <div className="w-full space-y-3">
      <div className="flex gap-4 border-b border-slate-200 pb-3">
        {Array.from({ length: cols }).map((_, idx) => (
          <SkeletonLoader key={idx} className="h-4 flex-1" />
        ))}
      </div>
      {Array.from({ length: rows }).map((_, rIdx) => (
        <div key={rIdx} className="flex gap-4 py-2">
          {Array.from({ length: cols }).map((_, cIdx) => (
            <SkeletonLoader key={cIdx} className="h-5 flex-1" />
          ))}
        </div>
      ))}
    </div>
  );
}

export function CardSkeleton() {
  return (
    <div className="rounded-lg border border-slate-200 p-5 space-y-4">
      <div className="flex items-center justify-between">
        <SkeletonLoader className="h-5 w-32" />
        <SkeletonLoader className="h-4 w-12" />
      </div>
      <SkeletonLoader className="h-8 w-24" />
      <SkeletonLoader className="h-3 w-48" />
    </div>
  );
}

export function AvatarSkeleton() {
  return <SkeletonLoader className="h-9 w-9 rounded-full shrink-0" />;
}
