import React, { useState } from 'react';
import { Mail, Plus, X, Loader2 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { InvoiceService } from '@/api/invoiceService';
import { Client } from '@/api/clientService';
import { useToast } from '@/components/ui/use-toast';

interface SendInvoiceModalProps {
  isOpen: boolean;
  onClose: () => void;
  invoiceId: number;
  invoiceNumber: string;
  client: Client | null;
  onSuccess?: () => void;
}

export const SendInvoiceModal: React.FC<SendInvoiceModalProps> = ({
  isOpen,
  onClose,
  invoiceId,
  invoiceNumber,
  client,
  onSuccess,
}) => {
  const [recipients, setRecipients] = useState<string[]>([]);
  const [newEmail, setNewEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { toast } = useToast();

  const validateEmail = (email: string) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  };

  const handleAddEmail = () => {
    if (!newEmail) return;
    if (!validateEmail(newEmail)) {
      toast({
        title: "Invalid Email",
        description: "Please enter a valid email address.",
        variant: "destructive",
      });
      return;
    }
    if (recipients.includes(newEmail)) {
      setNewEmail('');
      return;
    }
    setRecipients([...recipients, newEmail]);
    setNewEmail('');
  };

  const handleRemoveEmail = (email: string) => {
    setRecipients(recipients.filter((r) => r !== email));
  };

  const handleToggleClientEmail = (checked: boolean) => {
    if (!client?.email) return;
    if (checked) {
      if (!recipients.includes(client.email)) {
        setRecipients([...recipients, client.email]);
      }
    } else {
      setRecipients(recipients.filter((r) => r !== client.email));
    }
  };

  const handleSend = async () => {
    if (recipients.length === 0) {
      toast({
        title: "No Recipients",
        description: "Please add at least one email address.",
        variant: "destructive",
      });
      return;
    }

    setIsSubmitting(true);
    try {
      await InvoiceService.sendInvoice(invoiceId, recipients);
      toast({
        title: "Success",
        description: `Invoice ${invoiceNumber} has been queued for delivery.`,
      });
      if (onSuccess) onSuccess();
      onClose();
    } catch (error: any) {
      console.error(error);
      toast({
        title: "Error Sending Invoice",
        description: error.response?.data?.message || "Something went wrong. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Mail className="h-5 w-5" /> Send Invoice
          </DialogTitle>
          <DialogDescription>
            Send invoice {invoiceNumber} to your client.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6 py-4">
          {client?.email && (
            <div className="flex items-center space-x-2 bg-slate-50 p-3 rounded-lg border border-slate-100">
              <Checkbox
                id="client-email"
                checked={recipients.includes(client.email)}
                onCheckedChange={(checked) => handleToggleClientEmail(!!checked)}
              />
              <div className="grid gap-1.5 leading-none">
                <label
                  htmlFor="client-email"
                  className="text-sm font-bold leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                >
                  Client Primary Contact
                </label>
                <p className="text-xs text-slate-500 font-mono">
                  {client.email}
                </p>
              </div>
            </div>
          )}

          <div className="space-y-3">
            <Label className="text-xs font-black uppercase tracking-widest text-slate-400">
              Additional Recipients
            </Label>
            <div className="flex gap-2">
              <Input
                placeholder="Enter email address..."
                value={newEmail}
                onChange={(e) => setNewEmail(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddEmail();
                  }
                }}
              />
              <Button type="button" onClick={handleAddEmail} size="icon" variant="outline">
                <Plus className="h-4 w-4" />
              </Button>
            </div>
          </div>

          <div className="flex flex-wrap gap-2 min-h-12 border border-dashed rounded-lg p-2 bg-slate-50/50">
            {recipients.length === 0 ? (
              <span className="text-xs text-slate-400 italic p-2">No recipients selected</span>
            ) : (
              recipients.map((email) => (
                <Badge key={email} variant="secondary" className="pl-3 gap-1 py-1">
                  {email}
                  <button
                    onClick={() => handleRemoveEmail(email)}
                    className="ml-1 hover:text-destructive transition-colors"
                  >
                    <X className="h-3 w-3" />
                  </button>
                </Badge>
              ))
            )}
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button onClick={handleSend} disabled={isSubmitting || recipients.length === 0} className="gap-2">
            {isSubmitting ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Sending...
              </>
            ) : (
              <>
                <Mail className="h-4 w-4" />
                Send Invoice
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
