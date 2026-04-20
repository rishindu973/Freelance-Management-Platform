import React from 'react';

// ─── Color palette (mirrors PdfStyle.java exactly) ───────────────────────────
const C = {
  HEADER_BG:     '#FAE588', // header bar + total box background
  SECTION_LABEL: '#F9DC5C', // "BILLED TO", "PAYMENT INFO", "NOTES:"
  TABLE_HEADER:  '#FCEFB4', // table header row background
  FOOTER_HL:     '#FCEFB4', // footer verified-line highlight
  STATUS:        '#FFA726', // warm orange — invoice status badge
  TEXT:          '#000000', // all body text
  MUTED:         '#BDBDBD', // secondary label ("Powered Via")
  WHITE:         '#FFFFFF',
};

interface InvoiceLineItem {
  id?: number;
  description?: string;
  quantity: number;
  unitPrice: number;
  amount: number;
}

export interface InvoicePreviewProps {
  invoice: {
    invoiceNumber?: string;
    createdAt?: string;
    dueDate?: string;
    status?: string;
    total?: number;
    subtotal?: number;
    tax?: number;
    description?: string;      // Notes

    // Client
    clientName?: string;
    clientAddress?: string;
    clientEmail?: string;
    clientPhone?: string;

    // Project
    projectName?: string;
    projectId?: number;

    // Company branding
    companyName?: string;
    companyEmail?: string;
    companyPhone?: string;
    companyAddress?: string;
    logoUrl?: string;

    lineItems?: InvoiceLineItem[];
    [key: string]: any;
  } | null;
}

const fmt = {
  currency: (v?: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(v ?? 0),

  date: (d?: string) => {
    if (!d || d === 'N/A') return 'N/A';
    try {
      return new Intl.DateTimeFormat('en-US', {
        year: 'numeric', month: 'long', day: 'numeric',
      }).format(new Date(d));
    } catch { return d; }
  },
};

const InvoicePreview: React.FC<InvoicePreviewProps> = ({ invoice }) => {
  if (!invoice) {
    return <div className="p-8 text-center text-gray-500">No invoice data available.</div>;
  }

  const items        = invoice.lineItems ?? [];
  const invoiceNum   = invoice.invoiceNumber ?? 'DRAFT';
  const issueDate    = invoice.createdAt;
  const dueDate      = invoice.dueDate;
  const clientName   = invoice.clientName   ?? 'Client';
  const clientEmail  = invoice.clientEmail  ?? '';
  const clientAddr   = invoice.clientAddress ?? '';
  const clientPhone  = invoice.clientPhone  ?? '';
  const status       = invoice.status ?? 'DRAFT';
  const projectName  = invoice.projectName  ?? (invoice.projectId ? `Project #${invoice.projectId}` : null);
  const companyName  = invoice.companyName  ?? 'FREELANCEFLOW';
  const companyEmail = invoice.companyEmail ?? 'contact@freelanceflow.io';
  const notes        = invoice.description  ?? '';
  const subtotal     = invoice.subtotal;
  const tax          = invoice.tax;
  const total        = invoice.total;

  return (
    <div style={{ fontFamily: 'sans-serif', color: C.TEXT, backgroundColor: C.WHITE, maxWidth: 800, margin: '0 auto', padding: '0', boxShadow: '0 2px 16px rgba(0,0,0,0.10)' }}>

      {/* ── 1. HEADER ── */}
      <div style={{ backgroundColor: C.HEADER_BG, padding: '28px 40px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        {/* Left */}
        <div>
          <div style={{ fontSize: 32, fontWeight: 900, letterSpacing: '0.12em', textTransform: 'uppercase', lineHeight: 1 }}>INVOICE</div>
          <div style={{ fontSize: 11, marginTop: 8 }}>No: <strong>{invoiceNum}</strong></div>
          <div style={{ fontSize: 10, marginTop: 4 }}>Issue Date: &nbsp;<strong>{fmt.date(issueDate)}</strong></div>
          <div style={{ fontSize: 10, marginTop: 3 }}>Due Date: &nbsp;&nbsp;&nbsp;<strong>{fmt.date(dueDate)}</strong></div>
        </div>
        {/* Right — branding */}
        <div style={{ textAlign: 'right' }}>
          {invoice.logoUrl && (
            <img src={invoice.logoUrl} alt="logo" style={{ height: 36, marginBottom: 6, marginLeft: 'auto' }} />
          )}
          <div style={{ fontSize: 9, color: C.MUTED, letterSpacing: '0.08em' }}>Powered Via</div>
          <div style={{ fontSize: 15, fontWeight: 900, letterSpacing: '0.06em', textTransform: 'uppercase' }}>{companyName}</div>
          <div style={{ fontSize: 10, marginTop: 2 }}>{companyEmail}</div>
        </div>
      </div>

      <div style={{ padding: '28px 40px' }}>

        {/* ── 2. BILLING SECTION ── */}
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 28 }}>
          {/* BILLED TO */}
          <div>
            <div style={{ fontSize: 8, fontWeight: 900, color: C.SECTION_LABEL, letterSpacing: '0.18em', textTransform: 'uppercase', marginBottom: 8 }}>BILLED TO</div>
            <div style={{ fontWeight: 700, fontSize: 13 }}>{clientName}</div>
            {clientAddr  && <div style={{ fontSize: 10, marginTop: 3 }}>{clientAddr}</div>}
            {clientPhone && <div style={{ fontSize: 10, marginTop: 2 }}>{clientPhone}</div>}
            {clientEmail && <div style={{ fontSize: 10, marginTop: 2 }}>{clientEmail}</div>}
          </div>
          {/* PAYMENT INFO */}
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: 8, fontWeight: 900, color: C.SECTION_LABEL, letterSpacing: '0.18em', textTransform: 'uppercase', marginBottom: 8 }}>PAYMENT INFO</div>
            {/* Status badge */}
            <div style={{
              display: 'inline-block', backgroundColor: C.STATUS, color: C.WHITE,
              fontWeight: 700, fontSize: 10, borderRadius: 4, padding: '2px 10px', letterSpacing: '0.05em'
            }}>{status}</div>
            {projectName && (
              <div style={{ fontSize: 10, marginTop: 6 }}>Project: <strong>{projectName}</strong></div>
            )}
          </div>
        </div>

        {/* ── 3. TABLE ── */}
        <div style={{ borderRadius: 4, overflow: 'hidden', marginBottom: 24 }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 10 }}>
            <thead>
              <tr style={{ backgroundColor: C.TABLE_HEADER }}>
                <th style={{ padding: '8px 10px', textAlign: 'left', fontWeight: 900, textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: 9 }}>#</th>
                <th style={{ padding: '8px 10px', textAlign: 'left', fontWeight: 900, textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: 9 }}>Description</th>
                <th style={{ padding: '8px 10px', textAlign: 'right', fontWeight: 900, textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: 9 }}>Qty</th>
                <th style={{ padding: '8px 10px', textAlign: 'right', fontWeight: 900, textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: 9 }}>Unit Price</th>
                <th style={{ padding: '8px 10px', textAlign: 'right', fontWeight: 900, textTransform: 'uppercase', letterSpacing: '0.08em', fontSize: 9 }}>Amount</th>
              </tr>
            </thead>
            <tbody>
              {items.length > 0 ? items.map((item, i) => (
                <tr key={i} style={{ borderBottom: '1px solid #E2E8F0' }}>
                  <td style={{ padding: '9px 10px' }}>{i + 1}</td>
                  <td style={{ padding: '9px 10px' }}>{item.description ?? '-'}</td>
                  <td style={{ padding: '9px 10px', textAlign: 'right' }}>{item.quantity}</td>
                  <td style={{ padding: '9px 10px', textAlign: 'right' }}>{fmt.currency(item.unitPrice)}</td>
                  <td style={{ padding: '9px 10px', textAlign: 'right', fontWeight: 700 }}>{fmt.currency(item.amount ?? item.quantity * item.unitPrice)}</td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={5} style={{ padding: '20px', textAlign: 'center', color: '#888', fontStyle: 'italic' }}>No line items.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* ── 4. NOTES + TOTALS ── */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 32 }}>
          {/* Notes */}
          <div style={{ flex: 1, paddingRight: 32, maxWidth: '55%' }}>
            <div style={{ fontSize: 8, fontWeight: 900, color: C.SECTION_LABEL, letterSpacing: '0.18em', textTransform: 'uppercase', marginBottom: 6 }}>NOTES</div>
            <div style={{ fontSize: 10, lineHeight: 1.6, whiteSpace: 'pre-wrap' }}>
              {notes || 'Thank you for your business.'}
            </div>
          </div>
          {/* Totals */}
          <div style={{ width: 220 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '5px 0', borderBottom: '1px solid #E2E8F0', fontSize: 10 }}>
              <span style={{ fontWeight: 700}}>Subtotal</span>
              <span>{fmt.currency(subtotal)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '5px 0', borderBottom: '1px solid #E2E8F0', fontSize: 10 }}>
              <span style={{ fontWeight: 700 }}>Tax (10%)</span>
              <span>{fmt.currency(tax)}</span>
            </div>
            {/* Total box */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 12px', marginTop: 8, backgroundColor: C.HEADER_BG, borderRadius: 4 }}>
              <span style={{ fontWeight: 900, fontSize: 11, textTransform: 'uppercase', letterSpacing: '0.08em' }}>TOTAL DUE</span>
              <span style={{ fontWeight: 900, fontSize: 16 }}>{fmt.currency(total)}</span>
            </div>
          </div>
        </div>

        {/* ── 5. FOOTER ── */}
        <div style={{ marginTop: 16 }}>
          {/* Verified bar */}
          <div style={{ backgroundColor: C.FOOTER_HL, padding: '6px 12px', borderRadius: 4, textAlign: 'center', fontSize: 8, marginBottom: 12 }}>
            Verified By FreelanceFlow Secure Infrastructure · {invoiceNum}
          </div>
          {/* Branding */}
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: 7, color: C.MUTED, letterSpacing: '0.1em' }}>Powered Via</div>
            <div style={{ fontSize: 10, fontWeight: 900, letterSpacing: '0.08em', textTransform: 'uppercase' }}>{companyName}</div>
            <div style={{ fontSize: 8, marginTop: 2, color: '#555' }}>{companyEmail}</div>
          </div>
          <div style={{ textAlign: 'center', fontSize: 7, color: C.MUTED, marginTop: 8 }}>
            © 2026 FreelanceFlow PM OS | Confidential Document
          </div>
        </div>

      </div>
    </div>
  );
};

export default InvoicePreview;
