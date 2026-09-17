import React from "react";
import { cn } from "@/lib/utils";

export const Table = ({ className, children, ...props }: React.HTMLAttributes<HTMLTableElement>) => (
  <div className="w-full overflow-x-auto">
    <table className={cn("w-full text-left text-sm text-slate-800", className)} {...props}>
      {children}
    </table>
  </div>
);

export const TableHeader = ({ className, children, ...props }: React.HTMLAttributes<HTMLTableSectionElement>) => (
  <thead className={cn("border-b border-slate-200 bg-slate-50/80 text-xs font-semibold uppercase tracking-wider text-slate-500", className)} {...props}>
    {children}
  </thead>
);

export const TableBody = ({ className, children, ...props }: React.HTMLAttributes<HTMLTableSectionElement>) => (
  <tbody className={cn("divide-y divide-slate-100 bg-white", className)} {...props}>
    {children}
  </tbody>
);

export const TableRow = ({ className, children, ...props }: React.HTMLAttributes<HTMLTableRowElement>) => (
  <tr className={cn("transition-colors hover:bg-slate-50/60", className)} {...props}>
    {children}
  </tr>
);

export const TableHead = ({ className, children, ...props }: React.ThHTMLAttributes<HTMLTableCellElement>) => (
  <th className={cn("px-4 py-3 font-semibold text-slate-600", className)} {...props}>
    {children}
  </th>
);

export const TableCell = ({ className, children, ...props }: React.TdHTMLAttributes<HTMLTableCellElement>) => (
  <td className={cn("px-4 py-3 text-slate-700 whitespace-nowrap", className)} {...props}>
    {children}
  </td>
);
