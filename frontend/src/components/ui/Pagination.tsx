import React from "react";
import { Button } from "./Button";
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-react";
import { Select } from "./Select";

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  totalItems?: number;
  pageSize?: number;
  onPageChange: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
  pageSizeOptions?: number[];
  className?: string;
}

export function Pagination({
  currentPage,
  totalPages,
  totalItems,
  pageSize = 10,
  onPageChange,
  onPageSizeChange,
  pageSizeOptions = [10, 25, 50, 100],
  className,
}: PaginationProps) {
  const startItem = totalItems ? Math.min((currentPage - 1) * pageSize + 1, totalItems) : 0;
  const endItem = totalItems ? Math.min(currentPage * pageSize, totalItems) : 0;

  return (
    <div className={`flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between text-xs text-slate-600 ${className || ""}`}>
      {/* Item Summary */}
      <div>
        {totalItems ? (
          <span>
            Showing <strong className="font-semibold text-slate-900">{startItem}</strong> to{" "}
            <strong className="font-semibold text-slate-900">{endItem}</strong> of{" "}
            <strong className="font-semibold text-slate-900">{totalItems}</strong> entries
          </span>
        ) : (
          <span>
            Page <strong className="font-semibold text-slate-900">{currentPage}</strong> of{" "}
            <strong className="font-semibold text-slate-900">{totalPages || 1}</strong>
          </span>
        )}
      </div>

      <div className="flex items-center gap-4">
        {/* Page Size Selector */}
        {onPageSizeChange && (
          <div className="flex items-center gap-2">
            <span>Rows:</span>
            <Select
              value={String(pageSize)}
              onChange={(e) => onPageSizeChange(Number(e.target.value))}
              options={pageSizeOptions.map((size) => ({ label: String(size), value: String(size) }))}
              className="h-8 py-0 px-2 w-16 text-xs"
            />
          </div>
        )}

        {/* Page Navigation Buttons */}
        <div className="flex items-center space-x-1">
          <Button
            variant="outline"
            size="sm"
            disabled={currentPage <= 1}
            onClick={() => onPageChange(1)}
            aria-label="First page"
          >
            <ChevronsLeft className="h-3.5 w-3.5" />
          </Button>
          <Button
            variant="outline"
            size="sm"
            disabled={currentPage <= 1}
            onClick={() => onPageChange(currentPage - 1)}
            aria-label="Previous page"
          >
            <ChevronLeft className="h-3.5 w-3.5" />
          </Button>

          <span className="px-2 font-mono font-medium text-slate-700">
            {currentPage} / {totalPages || 1}
          </span>

          <Button
            variant="outline"
            size="sm"
            disabled={currentPage >= totalPages}
            onClick={() => onPageChange(currentPage + 1)}
            aria-label="Next page"
          >
            <ChevronRight className="h-3.5 w-3.5" />
          </Button>
          <Button
            variant="outline"
            size="sm"
            disabled={currentPage >= totalPages}
            onClick={() => onPageChange(totalPages)}
            aria-label="Last page"
          >
            <ChevronsRight className="h-3.5 w-3.5" />
          </Button>
        </div>
      </div>
    </div>
  );
}
