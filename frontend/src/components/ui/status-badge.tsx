import React from "react";
import { Badge } from "@/components/ui/badge";

export interface StatusBadgeProps {
  status: string;
  className?: string;
  children?: React.ReactNode;
}

const statusColorClass: Record<string, string> = {
  DRAFT: "bg-muted text-muted-foreground border-border",
  FINAL: "bg-info/15 text-info border-info/30",
  OVERDUE: "bg-destructive/15 text-destructive border-destructive/30",
  PAID: "bg-success/15 text-success border-success/30",
  PARTIALLY_PAID: "bg-warning/15 text-warning border-warning/30",
  OVERPAID: "bg-success/30 text-success border-success/50",
  SENT: "bg-primary/15 text-primary border-primary/30",
  FAILED: "bg-destructive/15 text-destructive border-destructive/30"
};

export function StatusBadge({ status, className, children }: StatusBadgeProps) {
  const colorClass = statusColorClass[status?.toUpperCase()] || statusColorClass.DRAFT;

  return (
    <Badge
      variant="outline"
      className={`py-1 px-2.5 font-medium shadow-none transition-all duration-200 ${colorClass} ${className || ""}`}
    >
      {children || status}
    </Badge>
  );
}
