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
  Hash
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
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

export default function InvoiceDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [client, setClient] = useState<Client | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchInvoice = async () => {
      try {
        if (id) {
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
    fetchInvoice();
  }, [id, toast]);

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
          <Button variant="outline" size="sm" className="gap-2" onClick={() => window.print()}>
            <Printer className="h-4 w-4" /> Print
          </Button>
          <Button variant="outline" size="sm" className="gap-2">
            <Download className="h-4 w-4" /> PDF
          </Button>
          <Button size="sm" className="gap-2">
            <Mail className="h-4 w-4" /> Send Invoice
          </Button>
        </div>
      </div>

      <Card className="max-w-4xl mx-auto shadow-2xl border-slate-200 overflow-hidden bg-white print:shadow-none print:border-none">
        {/* Professional Header */}
        <div className="bg-slate-900 p-12 text-white flex justify-between items-start print:bg-white print:text-slate-900 print:p-0 print:border-b print:pb-8">
          <div>
            <h2 className="text-4xl font-black tracking-tight uppercase tracking-[0.25em] mb-2">Invoice</h2>
            <div className="text-xl font-bold mb-4">
              Invoice No: {invoice.invoiceNumber || 'DRAFT-TBD'}
            </div>
            <div className="flex items-center gap-4 text-slate-400 text-xs font-mono print:text-slate-500">
               <div className="flex items-center gap-1.5 font-bold">
                 <Calendar className="h-3 w-3" /> DATE: {invoice.createdAt ? new Date(invoice.createdAt).toLocaleDateString() : 'N/A'}
               </div>
            </div>
          </div>
          <div className="text-right">
            <p className="font-black text-xl tracking-tighter uppercase">ANTIGRAVITY SOLUTIONS</p>
            <p className="text-xs text-slate-400 print:text-slate-500">123 Workspace Dr, Innovation Park</p>
            <p className="text-xs text-slate-400 print:text-slate-500">Innovation Ave | NY 10001</p>
            <p className="text-xs text-slate-400 print:text-slate-500 underline underline-offset-4 decoration-primary">finance@antigravity.io</p>
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
                <div className="flex gap-4">
                  <span className="text-slate-400 font-bold uppercase tracking-widest text-[9px]">STATUS:</span>
                  <Badge variant="outline" className={invoice.status === 'FINAL' ? "bg-slate-900 text-white border-slate-900" : "bg-orange-50 text-orange-600 border-orange-200"}>
                    {invoice.status}
                  </Badge>
                </div>
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
              <TableHeader className="bg-slate-50/80">
                <TableRow className="hover:bg-transparent">
                  <TableHead className="text-xs font-black uppercase text-slate-400 tracking-widest py-4 px-6">Description / Service Rendered</TableHead>
                  <TableHead className="text-right text-xs font-black uppercase text-slate-400 tracking-widest py-4 px-6">Qty</TableHead>
                  <TableHead className="text-right text-xs font-black uppercase text-slate-400 tracking-widest py-4 px-6">Unit Price</TableHead>
                  <TableHead className="text-right text-xs font-black uppercase text-slate-400 tracking-widest py-4 px-6">Total Amount</TableHead>
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
                <p className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Notes</p>
                <p className="text-xs text-slate-500 leading-relaxed">Please ensure payment is made within 30 days of the invoice date. Late payments may be subject to a 1.5% monthly interest fee.</p>
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
              <div className="flex justify-between items-center p-6 pt-2 bg-slate-900 text-white rounded-xl shadow-lg print:shadow-none print:text-slate-900 print:bg-slate-50">
                <span className="font-black text-xs uppercase tracking-[0.25em] text-slate-400 print:text-slate-500">Gross Total</span>
                <span className="text-4xl font-black font-mono tabular-nums tracking-tighter leading-none">${Number(invoice.total).toFixed(2)}</span>
              </div>
            </div>
          </div>

          {/* Professional Branding Footer */}
          <div className="mt-20 pt-12 border-t border-slate-50 text-center space-y-3">
             <p className="text-[10px] text-slate-300 font-black uppercase tracking-[0.5em] mb-4">Official Document</p>
             <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-slate-50 border border-slate-100">
                <FileCheck className="h-3 w-3 text-primary" />
                <span className="text-[9px] font-black text-slate-500 uppercase tracking-[0.2em] flex items-center gap-1.5">
                   Verified By Antigravity Secure Infrastructure 
                   <div className="h-1 w-1 rounded-full bg-slate-300" /> 
                   {invoice.invoiceNumber}
                </span>
             </div>
             <p className="text-[8px] text-slate-200 uppercase tracking-widest pt-2">© 2026 Antigravity Freelance PM OS | Confidential Document</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
