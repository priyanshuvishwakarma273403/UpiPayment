"use client";

import React, { useState } from "react";
import { Table, TableHeader, TableBody, TableHead, TableRow, TableCell } from "./Table";
import { Input } from "./Input";
import { Button } from "./Button";
import { Checkbox } from "./Checkbox";
import { Pagination } from "./Pagination";
import { TableSkeleton } from "./SkeletonLoader";
import { EmptyState } from "./EmptyState";
import { ErrorState } from "./ErrorState";
import { Search, ArrowUpDown, ChevronUp, ChevronDown, SlidersHorizontal, ShieldCheck } from "lucide-react";
import { cn } from "@/lib/utils";

export interface Column<T> {
  key?: string;
  header: string;
  accessor: (row: T) => React.ReactNode;
  sortable?: boolean;
  sortValue?: (row: T) => string | number;
  hideable?: boolean;
  className?: string;
}

export interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  keyExtractor?: (row: T) => string;
  title?: string;
  searchPlaceholder?: string;
  isLoading?: boolean;
  error?: string | null;
  onRetry?: () => void;
  pageSize?: number;
  pagination?: boolean;
  emptyTitle?: string;
  emptyDescription?: string;
  selectable?: boolean;
  onSelectionChange?: (selectedKeys: string[]) => void;
  onRowClick?: (row: T) => void;
  className?: string;
}

export function DataTable<T>({
  columns: rawColumns,
  data,
  keyExtractor: customKeyExtractor,
  title,
  searchPlaceholder = "Filter entries...",
  isLoading = false,
  error = null,
  onRetry,
  pageSize = 10,
  pagination = true,
  emptyTitle = "No records found",
  emptyDescription = "No data entries match the specified query.",
  selectable = false,
  onSelectionChange,
  onRowClick,
  className,
}: DataTableProps<T>) {
  const columns = React.useMemo(
    () =>
      rawColumns.map((col, idx) => ({
        ...col,
        key: col.key || col.header || `col-${idx}`,
      })),
    [rawColumns]
  );

  const defaultKeyExtractor = (row: any) => row?.id || row?.key || row?.paymentId || row?.caseId || row?.version || String(Math.random());
  const keyExtractor = customKeyExtractor || defaultKeyExtractor;
  const [searchTerm, setSearchTerm] = useState("");
  const [sortKey, setSortKey] = useState<string | null>(null);
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("asc");
  const [currentPage, setCurrentPage] = useState(1);
  const [currentPageSize, setCurrentPageSize] = useState(pageSize);
  const [selectedKeys, setSelectedKeys] = useState<Set<string>>(new Set());
  const [hiddenColumns, setHiddenColumns] = useState<Set<string>>(new Set());
  const [columnToggleOpen, setColumnToggleOpen] = useState(false);

  // 1. Filtering
  const filteredData = React.useMemo(() => {
    if (!searchTerm.trim()) return data;
    const lower = searchTerm.toLowerCase();
    return data.filter((row) =>
      columns.some((col) => {
        const val = col.accessor(row);
        return typeof val === "string" ? val.toLowerCase().includes(lower) : false;
      })
    );
  }, [data, searchTerm, columns]);

  // 2. Sorting
  const sortedData = React.useMemo(() => {
    if (!sortKey) return filteredData;
    const targetCol = columns.find((c) => c.key === sortKey);
    if (!targetCol) return filteredData;

    return [...filteredData].sort((a, b) => {
      const valA = targetCol.sortValue ? targetCol.sortValue(a) : String(targetCol.accessor(a));
      const valB = targetCol.sortValue ? targetCol.sortValue(b) : String(targetCol.accessor(b));

      if (valA < valB) return sortOrder === "asc" ? -1 : 1;
      if (valA > valB) return sortOrder === "asc" ? 1 : -1;
      return 0;
    });
  }, [filteredData, sortKey, sortOrder, columns]);

  // 3. Pagination
  const totalPages = Math.ceil(sortedData.length / currentPageSize) || 1;
  const paginatedData = React.useMemo(() => {
    const start = (currentPage - 1) * currentPageSize;
    return sortedData.slice(start, start + currentPageSize);
  }, [sortedData, currentPage, currentPageSize]);

  // Visible columns
  const visibleColumns = columns.filter((col) => !hiddenColumns.has(col.key));

  // Selection handlers
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      const allKeys = new Set(paginatedData.map(keyExtractor));
      setSelectedKeys(allKeys);
      onSelectionChange?.(Array.from(allKeys));
    } else {
      setSelectedKeys(new Set());
      onSelectionChange?.([]);
    }
  };

  const handleSelectRow = (key: string, checked: boolean) => {
    const next = new Set(selectedKeys);
    if (checked) next.add(key);
    else next.delete(key);
    setSelectedKeys(next);
    onSelectionChange?.(Array.from(next));
  };

  const handleSort = (key: string) => {
    if (sortKey === key) {
      if (sortOrder === "asc") setSortOrder("desc");
      else {
        setSortKey(null);
        setSortOrder("asc");
      }
    } else {
      setSortKey(key);
      setSortOrder("asc");
    }
  };

  const toggleColumnVisibility = (key: string) => {
    const next = new Set(hiddenColumns);
    if (next.has(key)) next.delete(key);
    else next.add(key);
    setHiddenColumns(next);
  };

  if (error) {
    return <ErrorState message={error} onRetry={onRetry} />;
  }

  return (
    <div className={cn("space-y-4", className)}>
      {/* Search & Action Controls Toolbar */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-2 flex-1 max-w-md">
          <Input
            placeholder={searchPlaceholder}
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(1);
            }}
            leftIcon={<Search className="h-4 w-4 text-slate-400" />}
          />
        </div>

        <div className="flex items-center gap-2">
          {/* Column Toggle Dropdown */}
          <div className="relative">
            <Button
              variant="outline"
              size="sm"
              leftIcon={<SlidersHorizontal className="h-3.5 w-3.5" />}
              onClick={() => setColumnToggleOpen((prev) => !prev)}
            >
              Columns
            </Button>

            {columnToggleOpen && (
              <div className="absolute right-0 z-30 mt-1.5 w-48 rounded-md border border-slate-200 bg-white p-3 shadow-lg">
                <p className="mb-2 text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Toggle Columns</p>
                <div className="space-y-1.5">
                  {columns.map((col) => (
                    <Checkbox
                      key={col.key}
                      label={col.header}
                      checked={!hiddenColumns.has(col.key)}
                      onChange={() => toggleColumnVisibility(col.key)}
                    />
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Selected Counter Bar */}
      {selectedKeys.size > 0 && (
        <div className="flex items-center justify-between rounded-md bg-blue-50 border border-blue-200 px-4 py-2 text-xs text-blue-900 font-medium">
          <span>{selectedKeys.size} row(s) selected</span>
          <Button variant="ghost" size="sm" onClick={() => setSelectedKeys(new Set())}>
            Clear Selection
          </Button>
        </div>
      )}

      {/* Table Body / Loading / Empty State */}
      {isLoading ? (
        <div className="rounded-lg border border-slate-200 bg-white p-6">
          <TableSkeleton rows={5} cols={visibleColumns.length} />
        </div>
      ) : paginatedData.length === 0 ? (
        <div className="rounded-lg border border-slate-200 bg-white p-8">
          <EmptyState
            title={emptyTitle}
            description={emptyDescription}
            icon={<ShieldCheck className="h-8 w-8 text-slate-400" />}
          />
        </div>
      ) : (
        <>
          {/* Desktop Table View */}
          <div className="hidden md:block rounded-lg border border-slate-200 bg-white shadow-xs overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow className="bg-slate-50">
                  {selectable && (
                    <TableHead className="w-10">
                      <Checkbox
                        checked={paginatedData.length > 0 && paginatedData.every((r) => selectedKeys.has(keyExtractor(r)))}
                        onChange={(e) => handleSelectAll(e.target.checked)}
                      />
                    </TableHead>
                  )}
                  {visibleColumns.map((col) => (
                    <TableHead key={col.key} className={col.className}>
                      {col.sortable ? (
                        <button
                          type="button"
                          onClick={() => handleSort(col.key)}
                          className="flex items-center gap-1.5 text-xs font-semibold text-slate-700 hover:text-slate-900 focus:outline-none"
                        >
                          <span>{col.header}</span>
                          {sortKey === col.key ? (
                            sortOrder === "asc" ? (
                              <ChevronUp className="h-3.5 w-3.5 text-blue-600" />
                            ) : (
                              <ChevronDown className="h-3.5 w-3.5 text-blue-600" />
                            )
                          ) : (
                            <ArrowUpDown className="h-3 w-3 text-slate-400" />
                          )}
                        </button>
                      ) : (
                        <span>{col.header}</span>
                      )}
                    </TableHead>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>
                {paginatedData.map((row) => {
                  const key = keyExtractor(row);
                  const isSelected = selectedKeys.has(key);
                  return (
                    <TableRow
                      key={key}
                      className={cn(isSelected && "bg-blue-50/50", onRowClick && "cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-800/50")}
                      onClick={() => onRowClick?.(row)}
                    >
                      {selectable && (
                        <TableCell className="w-10">
                          <Checkbox
                            checked={isSelected}
                            onChange={(e) => handleSelectRow(key, e.target.checked)}
                          />
                        </TableCell>
                      )}
                      {visibleColumns.map((col) => (
                        <TableCell key={col.key} className={col.className}>
                          {col.accessor(row)}
                        </TableCell>
                      ))}
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </div>

          {/* Mobile Stacked Card Transformation View */}
          <div className="block md:hidden space-y-3">
            {paginatedData.map((row) => {
              const key = keyExtractor(row);
              return (
                <div key={key} className="rounded-lg border border-slate-200 bg-white p-4 space-y-2 shadow-xs">
                  {visibleColumns.map((col) => (
                    <div key={col.key} className="flex justify-between items-center text-xs">
                      <span className="font-semibold text-slate-500">{col.header}:</span>
                      <span className="text-slate-900">{col.accessor(row)}</span>
                    </div>
                  ))}
                </div>
              );
            })}
          </div>
        </>
      )}

      {/* Pagination Footer */}
      {!isLoading && paginatedData.length > 0 && pagination && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={sortedData.length}
          pageSize={currentPageSize}
          onPageChange={(p) => setCurrentPage(p)}
          onPageSizeChange={(s) => {
            setCurrentPageSize(s);
            setCurrentPage(1);
          }}
        />
      )}
    </div>
  );
}
