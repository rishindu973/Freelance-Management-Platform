import React from 'react';

// Using a flexible structure for the invoice prop to accommodate different backend shapes
interface InvoiceLineItem {
  id?: number;
  item?: string;
  name?: string;
  description?: string;
  quantity: number;
  unitPrice: number;
  amount: number;
}

export interface InvoicePreviewProps {
  invoice: {
    invoiceNumber?: string;
    issueDate?: string;
    createdAt?: string; // Fallback for issue date
    dueDate?: string;
    total?: number;
    subtotal?: number;
    notes?: string;
    description?: string;
    client?: {
      name?: string;
      address?: string;
      email?: string;
    };
    clientName?: string; // fallback if client object is missing
    items?: InvoiceLineItem[];
    lineItems?: InvoiceLineItem[];
    [key: string]: any;
  } | null;
}

const InvoicePreview: React.FC<InvoicePreviewProps> = ({ invoice }) => {
  if (!invoice) {
    return <div className="p-8 text-center text-gray-500">No invoice data available.</div>;
  }

  // Fallbacks and data normalization
  const items = invoice.items || invoice.lineItems || [];
  const invoiceNumber = invoice.invoiceNumber || 'INV-XXXX';
  const issueDate = invoice.issueDate || invoice.createdAt || new Date().toISOString().split('T')[0];
  const dueDate = invoice.dueDate || 'N/A';
  const clientName = invoice.client?.name || invoice.clientName || 'Unknown Client';
  const clientEmail = invoice.client?.email || '';
  const clientAddress = invoice.client?.address || '';
  const notes = invoice.notes || invoice.description || 'Thank you for your business.';

  const formatCurrency = (amount: number | undefined) => {
    if (amount === undefined) return '$0.00';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const formatDate = (dateStr: string) => {
    if (!dateStr || dateStr === 'N/A') return 'N/A';
    try {
      const date = new Date(dateStr);
      return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      }).format(date);
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="bg-white text-black p-8 md:p-12 max-w-4xl mx-auto shadow-sm border border-gray-200 font-sans print:shadow-none print:border-none print:p-0">
      {/* 1. HEADER */}
      <div className="flex justify-between items-start border-b-2 border-black pb-8 mb-8">
        <div>
          <h1 className="text-4xl font-bold tracking-widest uppercase">Invoice</h1>
        </div>
        <div className="text-right">
          <div className="font-bold text-xl mb-1 flex items-center justify-end gap-2">
            {/* Generic Logo Placeholder */}
            <div className="w-6 h-6 bg-black rounded-sm print:print-color-adjust-exact"></div>
            Company Name
          </div>
          <p className="text-sm">123 Business Road</p>
          <p className="text-sm">Business City, BC 12345</p>
          <p className="text-sm">contact@company.com</p>
        </div>
      </div>

      {/* 2. BILLING SECTION */}
      <div className="flex justify-between items-start mb-10">
        <div>
          <h2 className="text-sm font-bold uppercase text-gray-500 mb-2">Billed To</h2>
          <p className="font-bold text-lg">{clientName}</p>
          {clientAddress && <p className="text-sm whitespace-pre-wrap">{clientAddress}</p>}
          {clientEmail && <p className="text-sm">{clientEmail}</p>}
        </div>
        <div className="text-right">
          <div className="mb-2">
            <span className="text-sm font-bold uppercase text-gray-500 mr-4">Invoice No.</span>
            <span className="font-bold">{invoiceNumber}</span>
          </div>
          <div className="mb-2">
            <span className="text-sm font-bold uppercase text-gray-500 mr-4">Issue Date</span>
            <span>{formatDate(issueDate)}</span>
          </div>
          <div>
            <span className="text-sm font-bold uppercase text-gray-500 mr-4">Due Date</span>
            <span>{formatDate(dueDate)}</span>
          </div>
        </div>
      </div>

      {/* 3. TABLE */}
      <div className="mb-8 overflow-hidden rounded-sm">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-y-2 border-black">
              <th className="py-3 px-2 font-bold uppercase text-sm w-1/4">Item</th>
              <th className="py-3 px-2 font-bold uppercase text-sm w-1/3">Description</th>
              <th className="py-3 px-2 font-bold uppercase text-sm text-right">Qty</th>
              <th className="py-3 px-2 font-bold uppercase text-sm text-right">Unit Price</th>
              <th className="py-3 px-2 font-bold uppercase text-sm text-right">Amount</th>
            </tr>
          </thead>
          <tbody>
            {items.length > 0 ? (
              items.map((item, index) => (
                <tr key={index} className="border-b border-gray-300 last:border-b-2 last:border-black">
                  <td className="py-3 px-2 text-sm">{item.item || item.name || `Item ${index + 1}`}</td>
                  <td className="py-3 px-2 text-sm text-gray-600">{item.description || '-'}</td>
                  <td className="py-3 px-2 text-sm text-right">{item.quantity}</td>
                  <td className="py-3 px-2 text-sm text-right">{formatCurrency(item.unitPrice)}</td>
                  <td className="py-3 px-2 text-sm text-right font-medium">{formatCurrency(item.amount)}</td>
                </tr>
              ))
            ) : (
              <tr className="border-b-2 border-black">
                <td colSpan={5} className="py-6 text-center text-sm text-gray-500 italic">
                  No line items found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* 4. NOTES + TOTAL */}
      <div className="flex justify-between items-start mb-16">
        <div className="w-1/2 pr-8">
          <h3 className="text-sm font-bold uppercase text-gray-500 mb-2">Notes</h3>
          <p className="text-sm text-gray-700 whitespace-pre-wrap">{notes}</p>
        </div>
        <div className="w-64">
          <div className="flex justify-between py-2 border-b border-gray-300">
            <span className="text-sm font-bold">Subtotal</span>
            <span className="text-sm">{formatCurrency(invoice.subtotal || invoice.total)}</span>
          </div>
          <div className="flex justify-between py-3 border-b-2 border-black bg-gray-50 px-2 mt-2 print:print-color-adjust-exact print:bg-gray-50">
            <span className="text-base font-bold uppercase">Total Due</span>
            <span className="text-lg font-bold">{formatCurrency(invoice.total)}</span>
          </div>
        </div>
      </div>

      {/* 5. FOOTER */}
      <div className="pt-8 flex flex-col items-center justify-center text-center text-xs text-gray-500 border-t border-gray-200">
        <p className="font-bold tracking-widest text-black mb-1">Powered by FREELANCEFLOW</p>
        <p>This invoice was generated using FREELANCEFLOW. Visit https://freelanceflow.com</p>
      </div>
    </div>
  );
};

export default InvoicePreview;
