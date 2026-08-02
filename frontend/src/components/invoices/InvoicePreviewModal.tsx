import React, { useEffect, useState } from 'react';
import {
  Dialog,
  DialogContent,
} from '@/components/ui/dialog';
import { InvoiceService, Invoice } from '@/api/invoiceService';
import InvoicePreview from './InvoicePreview';
import { Loader2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';

export interface InvoicePreviewModalProps {
  invoiceId: number | null;
  isOpen: boolean;
  onClose: () => void;
}

export const InvoicePreviewModal: React.FC<InvoicePreviewModalProps> = ({
  invoiceId,
  isOpen,
  onClose,
}) => {
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen && invoiceId) {
      setIsLoading(true);
      setError(null);
      InvoiceService.getInvoiceById(invoiceId)
        .then((data) => setInvoice(data))
        .catch((err) => {
          console.error("Failed to fetch invoice for preview:", err);
          setError("Failed to load invoice details.");
        })
        .finally(() => setIsLoading(false));
    } else {
      setInvoice(null);
    }
  }, [isOpen, invoiceId]);

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      {/* Shadcn DialogContent handles background overlay, prevented scrolling, and close button automatically */}
      <DialogContent className="max-w-5xl w-[90vw] max-h-[90vh] overflow-y-auto p-0 border-none bg-transparent shadow-none [&>button]:bg-card [&>button]:rounded-full [&>button]:p-1.5 [&>button]:shadow-sm">
        {isLoading && (
          <div className="flex flex-col items-center justify-center h-64 bg-card rounded-md shadow-lg overflow-hidden relative">
            <Loader2 className="h-8 w-8 animate-spin text-primary mb-4" />
            <p className="text-muted-foreground font-medium">Loading preview...</p>
          </div>
        )}
        
        {error && (
          <div className="flex flex-col items-center justify-center h-64 bg-card rounded-md shadow-lg p-6 text-center relative">
            <AlertCircle className="h-10 w-10 text-destructive mb-4" />
            <p className="text-lg font-semibold text-foreground mb-2">Error</p>
            <p className="text-muted-foreground mb-4">{error}</p>
            <Button variant="outline" onClick={onClose}>Close Preview</Button>
          </div>
        )}

        {!isLoading && !error && invoice && (
          <div className="bg-white rounded-md shadow-2xl relative overflow-hidden">
            <InvoicePreview invoice={invoice} />
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default InvoicePreviewModal;
