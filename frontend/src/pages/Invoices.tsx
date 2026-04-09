import { useEffect, useState } from "react";
import { Plus, ArrowUp, ArrowDown, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/ui/status-badge";
import { AlertCircle } from "lucide-react";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { useToast } from "@/hooks/use-toast";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { InvoiceService, InvoiceCreateRequest, InvoiceListDTO } from "@/api/invoiceService";
import { ClientService, Client } from "@/api/clientService";

export default function Invoices() {
  const [invoices, setInvoices] = useState<InvoiceListDTO[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  // Pagination & Sorting State
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [sortBy, setSortBy] = useState("date");
  const [direction, setDirection] = useState("desc");
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filters State
  const [clientFilter, setClientFilter] = useState("all");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  const { toast } = useToast();
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [newInvoice, setNewInvoice] = useState<Partial<InvoiceCreateRequest>>({
    subtotal: 0,
    tax: 0,
    total: 0,
    year: new Date().getFullYear(),
    status: "DRAFT"
  });

  const handleCreateInvoice = async () => {
    try {
      if (!newInvoice.clientId || newInvoice.total === undefined || newInvoice.total < 0) {
        toast({ title: "Error", description: "Please provide valid client and total amount", variant: "destructive" });
        return;
      }
      await InvoiceService.createInvoice(newInvoice as InvoiceCreateRequest);
      toast({ title: "Success", description: "Invoice created successfully." });
      setIsDialogOpen(false);
      setNewInvoice({ subtotal: 0, tax: 0, total: 0, year: new Date().getFullYear(), status: "DRAFT" });
      setPage(0); // Reset to first page
      fetchInvoices(0);
    } catch (e) {
      toast({ title: "Error", description: "Failed to create invoice", variant: "destructive" });
    }
  };

  useEffect(() => {
    ClientService.getAllClients().then(setClients).catch(console.error);
  }, []);

  const fetchInvoices = async (currentPage = page) => {
    setIsLoading(true);
    try {
      const clientIdParam = clientFilter !== "all" ? Number(clientFilter) : undefined;
      const data = await InvoiceService.getAllInvoices({
        clientId: clientIdParam,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        page: currentPage,
        size,
        sortBy,
        direction
      });
      setInvoices(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      setHasError(false);
    } catch (error) {
      console.error(error);
      setHasError(true);
      toast({ title: "Error", description: "Failed to fetch invoices", variant: "destructive" });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    setPage(0); // If filters or sorting change, go to page 0
  }, [clientFilter, startDate, endDate, sortBy, direction]);

  useEffect(() => {
    const handler = setTimeout(() => {
      fetchInvoices(page);
    }, 200); // Small debounce

    return () => clearTimeout(handler);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, clientFilter, startDate, endDate, sortBy, direction]);

  const clearFilters = () => {
    setClientFilter("all");
    setStartDate("");
    setEndDate("");
    setSortBy("date");
    setDirection("desc");
    setPage(0);
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-foreground">Invoices</h1>
          <p className="text-sm text-muted-foreground mt-1">Manage your {totalElements} invoices</p>
        </div>
        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button className="gap-2 shadow-sm">
              <Plus className="h-4 w-4" /> New Invoice
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create New Invoice</DialogTitle>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="space-y-2">
                <Label>Client</Label>
                <Select
                  value={newInvoice.clientId ? String(newInvoice.clientId) : ""}
                  onValueChange={(v) => setNewInvoice({ ...newInvoice, clientId: Number(v) })}
                >
                  <SelectTrigger><SelectValue placeholder="Select client" /></SelectTrigger>
                  <SelectContent>
                    {clients.map((c) => (
                      <SelectItem key={c.id} value={String(c.id)}>{c.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Total Amount ($)</Label>
                  <Input
                    type="number"
                    min="0"
                    placeholder="e.g. 1500"
                    value={newInvoice.total ?? ""}
                    onChange={(e) => setNewInvoice({ ...newInvoice, total: e.target.value ? Number(e.target.value) : undefined })}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Due Date</Label>
                  <Input
                    type="date"
                    value={newInvoice.dueDate || ""}
                    onChange={(e) => setNewInvoice({ ...newInvoice, dueDate: e.target.value })}
                  />
                </div>
              </div>
              <Button className="mt-4" onClick={handleCreateInvoice}>Create Invoice</Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      {/* Modern Filter Strip */}
      <div className="flex flex-col gap-4 rounded-xl border bg-card/50 backdrop-blur-sm p-4 sm:flex-row sm:flex-wrap sm:items-end w-full shadow-sm">
        <div className="w-full sm:w-64">
          <Label className="mb-1.5 block text-xs font-medium text-muted-foreground">Filter by Client</Label>
          <Select value={clientFilter} onValueChange={setClientFilter}>
            <SelectTrigger className="bg-background"><SelectValue placeholder="All clients" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Clients</SelectItem>
              {clients.map((c) => (
                <SelectItem key={c.id} value={String(c.id)}>{c.name}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="w-full sm:w-40">
          <Label htmlFor="start-date" className="mb-1.5 block text-xs font-medium text-muted-foreground">Start Date</Label>
          <Input
            id="start-date"
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="bg-background h-10"
          />
        </div>
        <div className="w-full sm:w-40">
          <Label htmlFor="end-date" className="mb-1.5 block text-xs font-medium text-muted-foreground">End Date</Label>
          <Input
            id="end-date"
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="bg-background h-10"
          />
        </div>
        <div className="w-full sm:w-48">
          <Label className="mb-1.5 block text-xs font-medium text-muted-foreground">Sort By</Label>
          <Select value={sortBy} onValueChange={setSortBy}>
            <SelectTrigger className="bg-background"><SelectValue placeholder="Date" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="date">Date</SelectItem>
              <SelectItem value="amount">Amount</SelectItem>
              <SelectItem value="client">Client</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="w-full sm:w-48">
          <Label className="mb-1.5 block text-xs font-medium text-muted-foreground">Direction</Label>
          <div className="flex rounded-md border text-sm">
            <button
              onClick={() => setDirection("desc")}
              className={`flex-1 flex items-center justify-center gap-1.5 py-2 px-3 rounded-l-sm transition-colors ${direction === "desc" ? "bg-primary text-primary-foreground font-medium" : "bg-background text-foreground hover:bg-muted"}`}
            >
              <ArrowDown className="w-3.5 h-3.5" /> Desc
            </button>
            <button
              onClick={() => setDirection("asc")}
              className={`flex-1 flex items-center justify-center gap-1.5 py-2 px-3 rounded-r-sm transition-colors ${direction === "asc" ? "bg-primary text-primary-foreground font-medium" : "bg-background text-foreground hover:bg-muted"}`}
            >
              <ArrowUp className="w-3.5 h-3.5" /> Asc
            </button>
          </div>
        </div>
        {(clientFilter !== "all" || startDate !== "" || endDate !== "" || sortBy !== "date" || direction !== "desc") && (
          <Button
            variant="ghost"
            onClick={clearFilters}
            className="h-10 px-3 text-muted-foreground hover:text-foreground"
          >
            <X className="h-4 w-4 mr-2" />
            Clear
          </Button>
        )}
      </div>

      {/* Invoices Table */}
      <div className="rounded-xl border bg-card shadow-sm overflow-hidden">
        <Table>
          <TableHeader className="bg-muted/50">
            <TableRow>
              <TableHead className="font-semibold text-foreground">Invoice #</TableHead>
              <TableHead className="font-semibold text-foreground">Client</TableHead>
              <TableHead className="font-semibold text-foreground">Date</TableHead>
              <TableHead className="font-semibold text-foreground">Amount</TableHead>
              <TableHead className="font-semibold text-foreground text-right pr-6">Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={5} className="h-48 text-center">
                  <div className="flex flex-col items-center justify-center text-muted-foreground">
                    <div className="h-6 w-6 animate-spin rounded-full border-2 border-primary border-t-transparent mb-2"></div>
                    Loading invoices...
                  </div>
                </TableCell>
              </TableRow>
            ) : hasError ? (
              <TableRow>
                <TableCell colSpan={5} className="h-48 text-center text-destructive">
                  <div className="flex flex-col items-center justify-center">
                    <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-destructive/10">
                      <AlertCircle className="h-6 w-6" />
                    </div>
                    Failed to load invoices. Please try again.
                  </div>
                </TableCell>
              </TableRow>
            ) : invoices.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="h-48 text-center text-muted-foreground">
                  <div className="flex flex-col items-center justify-center">
                    <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-muted/60">
                      <Plus className="h-6 w-6 opacity-40 text-foreground" />
                    </div>
                    No invoices found. Try adjusting your filters.
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              invoices.map((invoice) => {
                return (
                  <TableRow key={invoice.id} className="cursor-pointer hover:bg-muted/50 transition-colors group">
                    <TableCell className="font-medium text-foreground py-3">{invoice.invoiceNumber || `INV-${invoice.id}`}</TableCell>
                    <TableCell className="text-muted-foreground">{invoice.clientName}</TableCell>
                    <TableCell className="text-muted-foreground">
                      {invoice.createdAt ? new Date(invoice.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" }) : "N/A"}
                    </TableCell>
                    <TableCell className="font-semibold flex items-center gap-1.5">
                      <span className="text-muted-foreground/60 text-xs font-normal">AUD</span>
                      ${invoice.total.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                    </TableCell>
                    <TableCell className="text-right pr-6">
                      <StatusBadge status={invoice.status}>
                        {invoice.displayStatus}
                      </StatusBadge>
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>

      {/* Pagination Controls */}
      {!isLoading && !hasError && totalPages > 1 && (
        <div className="flex flex-col sm:flex-row items-center justify-between bg-card p-4 border rounded-xl shadow-sm gap-4">
          <p className="text-sm text-muted-foreground whitespace-nowrap">
            Showing <span className="font-medium text-foreground">{page * size + 1}</span> to <span className="font-medium text-foreground">{Math.min((page + 1) * size, totalElements)}</span> of <span className="font-medium text-foreground">{totalElements}</span> results
          </p>
          <Pagination className="justify-end w-auto mx-0">
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  className={page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                />
              </PaginationItem>

              {(() => {
                const items = [];
                const maxVisible = 5;
                let start = Math.max(0, page - Math.floor(maxVisible / 2));
                let end = Math.min(totalPages - 1, start + maxVisible - 1);
                if (end - start + 1 < maxVisible) {
                  start = Math.max(0, end - maxVisible + 1);
                }

                if (start > 0) {
                  items.push(
                    <PaginationItem key={0}>
                      <PaginationLink className="cursor-pointer" onClick={() => setPage(0)}>1</PaginationLink>
                    </PaginationItem>
                  );
                  if (start > 1) {
                    items.push(<PaginationEllipsis key="ellipsis-start" />);
                  }
                }

                for (let i = start; i <= end; i++) {
                  items.push(
                    <PaginationItem key={i}>
                      <PaginationLink className="cursor-pointer" isActive={page === i} onClick={() => setPage(i)}>{i + 1}</PaginationLink>
                    </PaginationItem>
                  );
                }

                if (end < totalPages - 1) {
                  if (end < totalPages - 2) {
                    items.push(<PaginationEllipsis key="ellipsis-end" />);
                  }
                  items.push(
                    <PaginationItem key={totalPages - 1}>
                      <PaginationLink className="cursor-pointer" onClick={() => setPage(totalPages - 1)}>{totalPages}</PaginationLink>
                    </PaginationItem>
                  );
                }
                return items;
              })()}

              <PaginationItem>
                <PaginationNext
                  onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                  className={page >= totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      )}
    </div>
  );
}
