import React from "react";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";

interface PaginationComponentProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export const PaginationComponent: React.FC<PaginationComponentProps> = ({
  currentPage,
  totalPages,
  onPageChange,
}) => {
  if (totalPages <= 1) return null;

  const handlePrevious = () => {
    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (currentPage < totalPages - 1) {
      onPageChange(currentPage + 1);
    }
  };

  const generatePages = () => {
    const pages = [];
    const maxVisible = 5;

    if (totalPages <= maxVisible) {
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      let start = Math.max(0, currentPage - 2);
      let end = Math.min(totalPages - 1, currentPage + 2);

      if (currentPage <= 2) {
        end = 4;
      } else if (currentPage >= totalPages - 3) {
        start = totalPages - 5;
      }

      if (start > 0) {
        pages.push(0);
        if (start > 1) pages.push("ellipsis-start");
      }

      for (let i = start; i <= end; i++) {
        pages.push(i);
      }

      if (end < totalPages - 1) {
        if (end < totalPages - 2) pages.push("ellipsis-end");
        pages.push(totalPages - 1);
      }
    }
    return pages;
  };

  return (
    <Pagination className="mt-4">
      <PaginationContent>
        <PaginationItem>
          <PaginationPrevious
            href="#"
            onClick={(e) => {
              e.preventDefault();
              handlePrevious();
            }}
            className={currentPage === 0 ? "pointer-events-none opacity-50" : ""}
          />
        </PaginationItem>

        {generatePages().map((page, index) => {
          if (page === "ellipsis-start" || page === "ellipsis-end") {
            return (
              <PaginationItem key={`${page}-${index}`}>
                <PaginationEllipsis />
              </PaginationItem>
            );
          }

          return (
            <PaginationItem key={page}>
              <PaginationLink
                href="#"
                isActive={currentPage === page}
                onClick={(e) => {
                  e.preventDefault();
                  onPageChange(page as number);
                }}
              >
                {(page as number) + 1}
              </PaginationLink>
            </PaginationItem>
          );
        })}

        <PaginationItem>
          <PaginationNext
            href="#"
            onClick={(e) => {
              e.preventDefault();
              handleNext();
            }}
            className={currentPage === totalPages - 1 ? "pointer-events-none opacity-50" : ""}
          />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  );
};
