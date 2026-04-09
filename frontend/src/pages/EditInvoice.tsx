import React from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { InvoiceService } from '@/api/invoiceService';
import InvoiceForm from '@/components/invoices/InvoiceForm';
import { 
  Breadcrumb, 
  BreadcrumbItem, 
  BreadcrumbLink, 
  BreadcrumbList, 
  BreadcrumbPage, 
  BreadcrumbSeparator 
} from "@/components/ui/breadcrumb";
import { Loader2 } from 'lucide-react';

export default function EditInvoice() {
  const { id } = useParams<{ id: string }>();
  const invoiceId = parseInt(id || '0', 10);

  const { data: invoice, isLoading, error } = useQuery({
    queryKey: ['invoice', invoiceId],
    queryFn: () => InvoiceService.getInvoiceById(invoiceId),
    enabled: !!invoiceId,
  });

  if (isLoading) {
    return (
      <div className="flex h-[400px] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (error || !invoice) {
    return (
      <div className="flex h-[400px] flex-col items-center justify-center space-y-4">
        <h3 className="text-xl font-semibold text-destructive">Error Loading Invoice</h3>
        <p className="text-muted-foreground">The invoice could not be found or there was a server error.</p>
        <Link to="/invoices">
          <BreadcrumbLink>Back to Invoices</BreadcrumbLink>
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Breadcrumb>
        <BreadcrumbList>
          <BreadcrumbItem>
            <BreadcrumbLink asChild>
              <Link to="/dashboard">Dashboard</Link>
            </BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbLink asChild>
              <Link to="/invoices">Invoices</Link>
            </BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbLink asChild>
              <Link to={`/invoices/${invoiceId}`}>#{invoice.invoiceNumber || invoiceId}</Link>
            </BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbPage>Edit Invoice</BreadcrumbPage>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>

      <InvoiceForm initialData={invoice} />
    </div>
  );
}
