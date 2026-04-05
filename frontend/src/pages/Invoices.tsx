import { useEffect, useState } from "react";
import { Search, FileText, Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
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
import { InvoiceService, InvoiceResponse, InvoiceCreateRequest } from "@/api/invoiceService";
import { ClientService, Client } from "@/api/clientService";

const statusConfig: Record<string, { label: string; className: string }> = {
  DRAFT: { label: "Draft", className: "bg-muted text-muted-foreground border-border" },
  FINAL: { label: "Final", className: "bg-info/15 text-info border-info/30" },
  OVERDUE: { label: "Overdue", className: "bg-destructive/15 text-destructive border-destructive/30" },
  PAID: { label: "Paid", className: "bg-success/15 text-success border-success/30" },
  PARTIALLY_PAID: { label: "Partially Paid", className: "bg-warning/15 text-warning border-warning/30" },
  OVERPAID: { label: "Overpaid", className: "bg-success/30 text-success border-success/50" },
};

export default function Invoices() {
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Filters
  const [search, setSearch] = useState("");
  const [clientFilter, setClientFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");

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
      fetchInvoices();
    } catch (e) {
      toast({ title: "Error", description: "Failed to create invoice", variant: "destructive" });
    }
  };

  useEffect(() => {
    ClientService.getAllClients().then(setClients).catch(console.error);
  }, []);

  const fetchInvoices = async () => {
    setIsLoading(true);
    try {
      const data = await InvoiceService.getAllInvoices();
      let filtered = data;
      if (clientFilter !== "all") {
        filtered = filtered.filter(inv => inv.clientId.toString() === clientFilter);
      }
      if (statusFilter !== "all") {
        filtered = filtered.filter(inv => inv.status === statusFilter);
      }
      if (search) {
        filtered = filtered.filter(inv => inv.invoiceNumber?.toLowerCase().includes(search.toLowerCase()) || inv.description?.toLowerCase().includes(search.toLowerCase()));
      }
      setInvoices(filtered);
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchInvoices();
  }, [search, clientFilter, statusFilter]);

  const getClientName = (id: number) => {
    return clients.find(c => c.id === id)?.name || `Client #${id}`;
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">Invoices</h1>
          <p className="text-sm text-muted-foreground">{invoices.length} total invoices</p>
        </div>
        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button className="gap-2">
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
              <div className="space-y-2">
                <Label>Description</Label>
                <Input
                  value={newInvoice.description || ""}
                  onChange={(e) => setNewInvoice({ ...newInvoice, description: e.target.value })}
                  placeholder="Invoice description..."
                />
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

      <div className="flex flex-col gap-3 rounded-lg border bg-card p-4 sm:flex-row sm:items-end w-full">
        <div className="flex-1">
          <Label className="mb-1.5 block text-xs text-muted-foreground">Search</Label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search by invoice number or description..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-9"
            />
          </div>
        </div>
        <div className="w-full sm:w-44">
          <Label className="mb-1.5 block text-xs text-muted-foreground">Client</Label>
          <Select value={clientFilter} onValueChange={setClientFilter}>
            <SelectTrigger><SelectValue placeholder="All clients" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All clients</SelectItem>
              {clients.map((c) => (
                <SelectItem key={c.id} value={String(c.id)}>{c.name}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="w-full sm:w-40">
          <Label className="mb-1.5 block text-xs text-muted-foreground">Status</Label>
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger><SelectValue placeholder="All statuses" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All statuses</SelectItem>
              {Object.keys(statusConfig).map(k => (
                <SelectItem key={k} value={k}>{statusConfig[k].label}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="rounded-lg border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Invoice #</TableHead>
              <TableHead>Client</TableHead>
              <TableHead>Description</TableHead>
              <TableHead>Due Date</TableHead>
              <TableHead>Amount</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} className="h-32 text-center text-muted-foreground">
                  Loading invoices...
                </TableCell>
              </TableRow>
            ) : invoices.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="h-32 text-center text-muted-foreground">
                  <FileText className="mx-auto mb-2 h-8 w-8 opacity-40" />
                  No invoices match your filters.
                </TableCell>
              </TableRow>
            ) : (
              invoices.map((invoice) => {
                const cfg = statusConfig[invoice.status] || statusConfig.DRAFT;
                return (
                  <TableRow key={invoice.id} className="cursor-pointer">
                    <TableCell className="font-medium text-foreground">{invoice.invoiceNumber || `INV-${invoice.id}`}</TableCell>
                    <TableCell className="text-muted-foreground">{getClientName(invoice.clientId)}</TableCell>
                    <TableCell className="max-w-xs truncate">{invoice.description || "N/A"}</TableCell>
                    <TableCell className="text-muted-foreground">
                      {invoice.dueDate ? new Date(invoice.dueDate).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" }) : "N/A"}
                    </TableCell>
                    <TableCell className="font-semibold">${invoice.total.toLocaleString(undefined, { minimumFractionDigits: 2 })}</TableCell>
                    <TableCell>
                      <Badge variant="outline" className={cfg.className}>{cfg.label}</Badge>
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
