
import { useEffect, useState } from "react";
import { Search, CreditCard, Plus } from "lucide-react";
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
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { PaymentService, PaymentResponse, PaymentCreateRequest } from "@/api/paymentService";
import { InvoiceService, InvoiceResponse } from "@/api/invoiceService";

const statusConfig: Record<string, { label: string; className: string }> = {
    completed: { label: "Completed", className: "bg-success/15 text-success border-success/30" },
    pending: { label: "Pending", className: "bg-muted text-muted-foreground border-border" },
    failed: { label: "Failed", className: "bg-destructive/15 text-destructive border-destructive/30" },
    refunded: { label: "Refunded", className: "bg-warning/15 text-warning border-warning/30" },
};

export default function Payments() {
    const [payments, setPayments] = useState<PaymentResponse[]>([]);
    const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    // Filters
    const [statusFilter, setStatusFilter] = useState("all");

    const { toast } = useToast();
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [newPayment, setNewPayment] = useState<Partial<PaymentCreateRequest>>({
        amount: 0,
        status: "completed"
    });

    const handleCreatePayment = async () => {
        try {
            if (!newPayment.invoiceId || newPayment.amount === undefined || newPayment.amount <= 0) {
                toast({ title: "Error", description: "Please provide valid invoice and amount", variant: "destructive" });
                return;
            }
            await PaymentService.createPayment(newPayment as PaymentCreateRequest);
            toast({ title: "Success", description: "Payment recorded successfully." });
            setIsDialogOpen(false);
            setNewPayment({ amount: 0, status: "completed" });
            fetchPayments();
        } catch (e) {
            toast({ title: "Error", description: "Failed to create payment", variant: "destructive" });
        }
    };

    useEffect(() => {
        InvoiceService.getAllInvoices().then(setInvoices).catch(console.error);
    }, []);

    const fetchPayments = async () => {
        setIsLoading(true);
        try {
            const data = await PaymentService.getAllPayments();
            let filtered = data;
            if (statusFilter !== "all") {
                filtered = filtered.filter(p => p.status === statusFilter);
            }
            setPayments(filtered);
        } catch (error) {
            console.error(error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchPayments();
    }, [statusFilter]);

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-semibold text-foreground">Payments</h1>
                    <p className="text-sm text-muted-foreground">{payments.length} recorded payments</p>
                </div>
                <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                    <DialogTrigger asChild>
                        <Button className="gap-2">
                            <Plus className="h-4 w-4" /> Record Payment
                        </Button>
                    </DialogTrigger>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Record New Payment</DialogTitle>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="space-y-2">
                                <Label>Invoice</Label>
                                <Select
                                    value={newPayment.invoiceId ? String(newPayment.invoiceId) : ""}
                                    onValueChange={(v) => setNewPayment({ ...newPayment, invoiceId: Number(v) })}
                                >
                                    <SelectTrigger><SelectValue placeholder="Select invoice" /></SelectTrigger>
                                    <SelectContent>
                                        {invoices.map((inv) => (
                                            <SelectItem key={inv.id} value={String(inv.id)}>
                                                {inv.invoiceNumber || `INV-${inv.id}`} - ${inv.total}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                            <div className="space-y-2">
                                <Label>Amount Paid ($)</Label>
                                <Input
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    placeholder="e.g. 500.00"
                                    value={newPayment.amount ?? ""}
                                    onChange={(e) => setNewPayment({ ...newPayment, amount: e.target.value ? Number(e.target.value) : undefined })}
                                />
                            </div>
                            <div className="space-y-2">
                                <Label>Status</Label>
                                <Select
                                    value={newPayment.status}
                                    onValueChange={(v) => setNewPayment({ ...newPayment, status: v })}
                                >
                                    <SelectTrigger><SelectValue placeholder="Select status" /></SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="completed">Completed</SelectItem>
                                        <SelectItem value="pending">Pending</SelectItem>
                                        <SelectItem value="failed">Failed</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                            <Button className="mt-4" onClick={handleCreatePayment}>Record Payment</Button>
                        </div>
                    </DialogContent>
                </Dialog>
            </div>

            <div className="flex flex-col gap-3 rounded-lg border bg-card p-4 sm:flex-row sm:items-end w-full">
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
                            <TableHead>Payment ID</TableHead>
                            <TableHead>Invoice Reference</TableHead>
                            <TableHead>Amount</TableHead>
                            <TableHead>Payment Date</TableHead>
                            <TableHead>Status</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={5} className="h-32 text-center text-muted-foreground">
                                    Loading payments...
                                </TableCell>
                            </TableRow>
                        ) : payments.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} className="h-32 text-center text-muted-foreground">
                                    <CreditCard className="mx-auto mb-2 h-8 w-8 opacity-40" />
                                    No payments found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            payments.map((payment) => {
                                const cfg = statusConfig[payment.status?.toLowerCase()] || statusConfig.pending;
                                const inv = invoices.find(i => i.id === payment.invoiceId);
                                return (
                                    <TableRow key={payment.id} className="cursor-pointer">
                                        <TableCell className="font-medium text-foreground">PAY-{payment.id}</TableCell>
                                        <TableCell className="text-muted-foreground">
                                            {inv ? (inv.invoiceNumber || `INV-${inv.id}`) : `INV-${payment.invoiceId}`}
                                        </TableCell>
                                        <TableCell className="font-semibold">${payment.amount?.toLocaleString(undefined, { minimumFractionDigits: 2 })}</TableCell>
                                        <TableCell className="text-muted-foreground">
                                            {payment.paymentDate ? new Date(payment.paymentDate).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" }) : "N/A"}
                                        </TableCell>
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
