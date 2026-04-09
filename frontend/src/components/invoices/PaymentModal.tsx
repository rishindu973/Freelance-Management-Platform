import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { InvoiceService } from "@/api/invoiceService";
import { useToast } from "@/hooks/use-toast";
import { Loader2, DollarSign } from "lucide-react";

interface PaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  invoiceId: number;
  totalAmount: number;
  onSuccess: () => void;
}

export function PaymentModal({ isOpen, onClose, invoiceId, totalAmount, onSuccess }: PaymentModalProps) {
  const [amount, setAmount] = useState<string>(totalAmount.toString());
  const [paymentMethod, setPaymentMethod] = useState("");
  const [reference, setReference] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { toast } = useToast();

  const handlePayment = async () => {
    const paymentAmount = parseFloat(amount);
    if (!paymentAmount || paymentAmount <= 0) {
      toast({ title: "Error", description: "Please enter a valid amount.", variant: "destructive" });
      return;
    }

    setIsSubmitting(true);
    try {
      if (paymentAmount >= totalAmount) {
        // Full payment -> update status to PAID
        await InvoiceService.updateInvoiceStatus(invoiceId, "PAID", paymentAmount);
      } else {
        // Partial payment -> just add payment. The backend logic will handle it not being fully PAID.
        await InvoiceService.addPayment(invoiceId, {
          amount: paymentAmount,
          paymentMethod: paymentMethod || "MANUAL",
          referenceNumber: reference,
        });
      }
      
      toast({ title: "Success", description: "Payment recorded successfully." });
      onSuccess();
      onClose();
    } catch (error: any) {
      toast({ 
        title: "Payment Error", 
        description: error.response?.data?.message || "Failed to record payment.", 
        variant: "destructive" 
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Record Payment</DialogTitle>
          <DialogDescription>
            Enter the payment details for this invoice.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="amount">Amount ($)</Label>
            <div className="relative">
              <DollarSign className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                id="amount"
                type="number"
                min="0"
                step="0.01"
                className="pl-8"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
              />
            </div>
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="method">Payment Method</Label>
            <Input
              id="method"
              placeholder="e.g. Bank Transfer, Credit Card"
              value={paymentMethod}
              onChange={(e) => setPaymentMethod(e.target.value)}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="reference">Reference Number (Optional)</Label>
            <Input
              id="reference"
              placeholder="e.g. TXN-123456"
              value={reference}
              onChange={(e) => setReference(e.target.value)}
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button onClick={handlePayment} disabled={isSubmitting}>
            {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Record Payment
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
