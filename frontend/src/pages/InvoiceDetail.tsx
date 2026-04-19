import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Printer,
  Download,
  Mail,
  FileCheck,
  AlertCircle,
  Building2,
  Calendar,
  CreditCard,
  Hash,
  Loader2,
  RefreshCw,
  CheckCircle2,
  XCircle,
  Clock,
  Eye
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { InvoiceService, Invoice } from '@/api/invoiceService';
import { ClientService, Client } from '@/api/clientService';
import { useToast } from '@/components/ui/use-toast';
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator
} from "@/components/ui/breadcrumb";
import { SendInvoiceModal } from '@/components/invoices/SendInvoiceModal';
import { PaymentModal } from '@/components/invoices/PaymentModal';
import InvoicePreviewModal from '@/components/invoices/InvoicePreviewModal';
import { downloadInvoicePdf } from '@/lib/utils';

export default function InvoiceDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [client, setClient] = useState<Client | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isDownloading, setIsDownloading] = useState(false);
  const [isSendModalOpen, setIsSendModalOpen] = useState(false);
  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
  const [isPreviewModalOpen, setIsPreviewModalOpen] = useState(false);

  const fetchInvoice = async () => {
    try {
      if (id) {
        setIsLoading(true);
        const data = await InvoiceService.getInvoiceById(Number(id));
        setInvoice(data);
        const clientData = await ClientService.getClientById(data.clientId);
        setClient(clientData);
      }
    } catch (error) {
      console.error(error);
      toast({ title: "Error", description: "Failed to load invoice details.", variant: "destructive" });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchInvoice();
  }, [id]);

  const handleDownloadPdf = async () => {
    if (!id || !invoice) return;

    setIsDownloading(true);
    try {
      const blob = await InvoiceService.fetchInvoicePdf(Number(id));
      downloadInvoicePdf(blob, Number(id));

      toast({
        title: "Download Successful",
        description: `Invoice PDF generated and downloaded successfully.`,
      });
    } catch (error: any) {
      console.error(error);
      const errorMessage = error.response?.data?.message || "Failed to generate PDF. Please try again later.";
      toast({
        title: "Download Error",
        description: errorMessage,
        variant: "destructive"
      });
    } finally {
      setIsDownloading(false);
    }
  };

  if (isLoading) return <div className="py-24 text-center">Loading invoice...</div>;
  if (!invoice) return <div className="py-24 text-center">Invoice not found.</div>;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link to="/invoices">Invoices</Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbPage>{invoice.invoiceNumber || `Invoice #${invoice.id}`}</BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>

        <div className="flex gap-2">
          <Button variant="outline" size="sm" className="gap-2" onClick={() => setIsPreviewModalOpen(true)}>
            <Eye className="h-4 w-4" /> Preview
          </Button>
          <Button variant="outline" size="sm" className="gap-2" onClick={() => window.print()}>
            <Printer className="h-4 w-4" /> Print
          </Button>
          <Button
            variant="outline"
            size="sm"
            className="gap-2 relative overflow-hidden"
            onClick={handleDownloadPdf}
            disabled={isDownloading}
          >
            {isDownloading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                <span>Downloading...</span>
              </>
            ) : (
              <>
                <Download className="h-4 w-4" />
                <span>Download PDF</span>
              </>
            )}
          </Button>
          {invoice.status === 'FAILED' ? (
            <Button
              size="sm"
              className="gap-2 bg-red-600 hover:bg-red-700 text-white"
              onClick={() => setIsSendModalOpen(true)}
            >
              <RefreshCw className="h-4 w-4" /> Retry Sending
            </Button>
          ) : (
            <Button
              size="sm"
              className="gap-2"
              onClick={() => setIsSendModalOpen(true)}
              disabled={invoice.status === 'SENT'}
            >
              <Mail className="h-4 w-4" />
              {invoice.status === 'SENT' ? 'Already Sent' : 'Send Invoice'}
            </Button>
          )}

          {invoice.status !== 'PAID' && invoice.status !== 'DRAFT' && (
            <Button
              size="sm"
              className="gap-2 bg-emerald-600 hover:bg-emerald-700 text-white"
              onClick={() => setIsPaymentModalOpen(true)}
            >
              <CreditCard className="h-4 w-4" /> Record Payment
            </Button>
          )}

          {invoice.status === 'DRAFT' && (
            <Button
              size="sm"
              className="gap-2 bg-emerald-600 hover:bg-emerald-700 text-white"
              onClick={() => setIsPaymentModalOpen(true)}
            >
              <CheckCircle2 className="h-4 w-4" /> Mark as Paid
            </Button>
          )}
        </div>
      </div>

      <SendInvoiceModal
        isOpen={isSendModalOpen}
        onClose={() => setIsSendModalOpen(false)}
        invoiceId={Number(id)}
        invoiceNumber={invoice.invoiceNumber || `Invoice #${invoice.id}`}
        client={client}
        onSuccess={fetchInvoice}
      />

      <PaymentModal
        isOpen={isPaymentModalOpen}
        onClose={() => setIsPaymentModalOpen(false)}
        invoiceId={Number(id)}
        totalAmount={invoice.total}
        onSuccess={fetchInvoice}
      />

      <Card className="max-w-4xl mx-auto shadow-2xl border-slate-200 overflow-hidden bg-white print:shadow-none print:border-none">
        {/* Header — warm yellow palette */}
        <div style={{ backgroundColor: '#FAE588' }} className="p-12 flex justify-between items-start print:p-0 print:border-b print:pb-8">
          <div>
            <h2 className="text-4xl font-black tracking-tight uppercase tracking-[0.25em] mb-2 text-black">Invoice</h2>
            <div className="text-base font-bold mb-2 text-black">
              No: {invoice.invoiceNumber || 'DRAFT-TBD'}
            </div>
            <div className="flex flex-col gap-1 text-xs font-mono text-black">
              <div className="flex items-center gap-1.5">
                <Calendar className="h-3 w-3" /> Issue Date: {invoice.createdAt ? new Date(invoice.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) : 'N/A'}
              </div>
              <div className="flex items-center gap-1.5">
                <Calendar className="h-3 w-3" /> Due Date: {invoice.dueDate ? new Date(invoice.dueDate).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) : 'N/A'}
              </div>
            </div>
          </div>
          <div className="text-right">
            <p className="text-[9px] text-[#BDBDBD] tracking-widest uppercase">Powered Via</p>
            <p className="text-lg font-black tracking-wider uppercase text-black">{invoice.companyName ?? 'FREELANCEFLOW'}</p>
            <p className="text-xs text-black mt-1">{invoice.companyEmail ?? 'contact@freelanceflow.io'}</p>
          </div>
        </div>

        <CardContent className="p-12 space-y-12 print:p-0 print:pt-12">
          {/* Billing Context */}
          <div className="grid grid-cols-2 gap-12">
            <div className="space-y-4">
              <h4 className="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em] flex items-center gap-2">
                <Building2 className="h-3 w-3" /> Billed To
              </h4>
              <div className="space-y-1">
                <p className="font-black text-slate-800 text-lg leading-tight">{client?.name || `Client #${invoice.clientId}`}</p>
                <p className="text-sm text-slate-500">{client?.address || 'Registered Client Information'}</p>
                <p className="text-sm text-slate-500">{client?.email || ''}</p>
              </div>
            </div>
            <div className="text-right space-y-4">
              <h4 className="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em] flex items-center justify-end gap-2">
                <CreditCard className="h-3 w-3" /> Payment Info
              </h4>
              <div className="space-y-1 text-sm font-mono flex flex-col items-end">
                <div className="flex gap-4 items-center">
                  <span className="text-slate-400 font-bold uppercase tracking-widest text-[9px]">STATUS:</span>
                  <Badge variant="outline" className={
                    invoice.status === 'SENT' ? "bg-emerald-50 text-emerald-700 border-emerald-200" :
                      invoice.status === 'FAILED' ? "bg-red-50 text-red-700 border-red-200" :
                        invoice.status === 'FINAL' ? "bg-slate-900 text-white border-slate-900" :
                          "bg-orange-50 text-orange-600 border-orange-200"
                  }>
                    <span className="flex items-center gap-1">
                      {invoice.status === 'SENT' && <CheckCircle2 className="h-3 w-3" />}
                      {invoice.status === 'FAILED' && <XCircle className="h-3 w-3" />}
                      {invoice.status}
                    </span>
                  </Badge>
                </div>
                {invoice.lastSentAt && (
                  <div className="flex gap-4 items-center">
                    <span className="text-slate-400 font-bold uppercase tracking-widest text-[9px]">LAST SENT:</span>
                    <span className="font-bold text-slate-600 text-xs flex items-center gap-1">
                      <Clock className="h-3 w-3" />
                      {new Date(invoice.lastSentAt).toLocaleString()}
                    </span>
                  </div>
                )}
                {invoice.status === 'FAILED' && invoice.failureReason && (
                  <div className="flex gap-4 items-start mt-1">
                    <span className="text-red-400 font-bold uppercase tracking-widest text-[9px]">ERROR:</span>
                    <span className="text-xs text-red-600 font-medium leading-snug">{invoice.failureReason}</span>
                  </div>
                )}
                {invoice.projectId && (
                  <div className="flex gap-4">
                    <span className="text-slate-400 font-bold uppercase tracking-widest text-[9px]">REFERENCE:</span>
                    <span className="font-bold text-slate-800">PROJECT-ID: #{invoice.projectId}</span>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Line Items Table */}
          <div className="border border-slate-100 rounded-xl overflow-hidden shadow-sm">
            <Table>
              <TableHeader style={{ backgroundColor: '#FCEFB4' }}>
                <TableRow className="hover:bg-transparent">
                  <TableHead className="text-xs font-black uppercase text-black tracking-widest py-4 px-6">Description / Service Rendered</TableHead>
                  <TableHead className="text-right text-xs font-black uppercase text-black tracking-widest py-4 px-6">Qty</TableHead>
                  <TableHead className="text-right text-xs font-black uppercase text-black tracking-widest py-4 px-6">Unit Price</TableHead>
                  <TableHead className="text-right text-xs font-black uppercase text-black tracking-widest py-4 px-6">Total Amount</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {invoice.lineItems.map((item, idx) => (
                  <TableRow key={idx} className="hover:bg-slate-50/30 transition-colors border-b border-slate-50">
                    <TableCell className="font-bold text-slate-700 py-6 px-6">{item.description}</TableCell>
                    <TableCell className="text-right font-mono text-slate-600 py-6 px-6 font-bold">{item.quantity}</TableCell>
                    <TableCell className="text-right font-mono text-slate-600 py-6 px-6 font-bold">${Number(item.unitPrice).toFixed(2)}</TableCell>
                    <TableCell className="text-right font-black text-slate-900 py-6 px-6 text-base tracking-tight">
                      ${(Number(item.quantity) * Number(item.unitPrice)).toFixed(2)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          {/* Totals Summary */}
          <div className="flex flex-col sm:flex-row justify-between pt-4 gap-8">
            <div className="flex-1 max-w-sm p-6 rounded-xl bg-slate-50/50 border border-slate-100 flex items-start gap-4 h-fit">
              <AlertCircle className="h-5 w-5 text-slate-400 mt-0.5" />
              <div className="space-y-1">
                <p className="text-[10px] font-black uppercase tracking-widest" style={{ color: '#F9DC5C' }}>Notes</p>
                <p className="text-xs text-slate-600 leading-relaxed">
                  {invoice.description || 'Thank you for your business.'}
                </p>
              </div>
            </div>

            <div className="w-full sm:w-80 space-y-4">
              <div className="space-y-3 p-6 pt-0 border-b border-slate-100">
                <div className="flex justify-between items-center text-sm">
                  <span className="font-bold text-slate-400 uppercase tracking-widest text-[9px]">SUBTOTAL</span>
                  <span className="font-black text-slate-700 font-mono tracking-tight text-base">${Number(invoice.subtotal).toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="font-bold text-slate-400 uppercase tracking-widest text-[9px]">TAX (10.0%)</span>
                  <span className="font-black text-slate-700 font-mono tracking-tight text-base">${Number(invoice.tax).toFixed(2)}</span>
                </div>
              </div>
              <div className="flex justify-between items-center p-5 pt-3 rounded-xl shadow-sm" style={{ backgroundColor: '#FAE588' }}>
                <span className="font-black text-xs uppercase tracking-[0.25em] text-black">Total Due</span>
                <span className="text-4xl font-black font-mono tabular-nums tracking-tighter leading-none text-black">${Number(invoice.total).toFixed(2)}</span>
              </div>
            </div>
          </div>

          {/* Footer — mirrors PDF footer exactly */}
          <div className="mt-16 pt-8 border-t border-slate-50 text-center space-y-4">
            {/* Verified bar — #FCEFB4 highlight */}
            <div className="inline-flex items-center gap-2 px-5 py-2 rounded-full" style={{ backgroundColor: '#FCEFB4' }}>
              <FileCheck className="h-3 w-3 text-black" />
              <span className="text-[9px] font-black text-black uppercase tracking-[0.2em] flex items-center gap-1.5">
                Verified By FreelanceFlow Secure Infrastructure
                <div className="h-1 w-1 rounded-full bg-black opacity-40" />
                {invoice.invoiceNumber}
              </span>
            </div>
            {/* Branding */}
            <div>
              <p className="text-[7px] uppercase tracking-widest" style={{ color: '#BDBDBD' }}>Powered Via</p>
              <p className="text-sm font-black uppercase tracking-wider text-black">{invoice.companyName ?? 'FREELANCEFLOW'}</p>
              <p className="text-[9px] text-slate-500 mt-1">{invoice.companyEmail ?? 'contact@freelanceflow.io'}</p>
            </div>
            <p className="text-[8px] uppercase tracking-widest" style={{ color: '#BDBDBD' }}>© 2026 FreelanceFlow PM OS | Confidential Document</p>
          </div>
        </CardContent>
      </Card>

      <InvoicePreviewModal
        invoiceId={Number(id)}
        isOpen={isPreviewModalOpen}
        onClose={() => setIsPreviewModalOpen(false)}
      />
    </div>
  );
}
