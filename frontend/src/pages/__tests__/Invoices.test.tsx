import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Invoices from '../Invoices';
import { InvoiceService } from '@/api/invoiceService';
import { ClientService } from '@/api/clientService';
import { Toaster } from '@/components/ui/toaster';

// Mock the services
vi.mock('@/api/invoiceService', () => ({
  InvoiceService: {
    getAllInvoices: vi.fn(),
    createInvoice: vi.fn()
  }
}));

vi.mock('@/api/clientService', () => ({
  ClientService: {
    getAllClients: vi.fn(),
  }
}));

// Mock ResizeObserver for Radix UI select components mapped in standard JSDOM interactions
class ResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}
global.ResizeObserver = ResizeObserver;

const mockClients = [
  { id: 1, name: 'Client A' },
  { id: 2, name: 'Client B' }
];

const createMockInvoicesPage = (totalElements = 2) => ({
  content: Array.from({ length: totalElements }).map((_, i) => ({
    id: i + 1,
    invoiceNumber: `INV-${i + 1}`,
    clientName: i % 2 === 0 ? 'Client A' : 'Client B',
    createdAt: `2026-04-0${i + 1}`,
    total: 100 * (i + 1),
    status: i % 2 === 0 ? 'PAID' : 'DRAFT',
    displayStatus: i % 2 === 0 ? 'Paid' : 'Draft'
  })),
  totalPages: 1 + Math.floor((totalElements - 1) / 10),
  totalElements,
  pageable: { pageNumber: 0, pageSize: 10 }
});

describe('Invoices Listing Page', () => {
  const originalConsoleError = console.error;

  beforeEach(() => {
    vi.clearAllMocks();
    console.error = vi.fn(); // Mock console.error securely to avoid unhandled logging failures
    (ClientService.getAllClients as any).mockResolvedValue(mockClients);
  });

  afterAll(() => {
    console.error = originalConsoleError;
  });

  it('renders loading state initially', () => {
    (InvoiceService.getAllInvoices as any).mockImplementation(() => new Promise(() => {})); // Never resolves
    
    render(<Invoices />);
    expect(screen.getByText(/Loading invoices\.\.\./i)).toBeInTheDocument();
  });

  it('renders invoices successfully without filters (default view)', async () => {
    const mockData = createMockInvoicesPage(2);
    (InvoiceService.getAllInvoices as any).mockResolvedValue(mockData);

    render(<Invoices />);

    // Wait until loading finishes and table renders
    await waitFor(() => {
      expect(screen.queryByText(/Loading invoices\.\.\./i)).not.toBeInTheDocument();
    });

    // Check mapping integrity
    expect(screen.getByText('INV-1')).toBeInTheDocument();
    expect(screen.getByText('INV-2')).toBeInTheDocument();
    expect(screen.getByText('Client A')).toBeInTheDocument();
    
    // Check Status Badge fallback
    expect(screen.getByText('Paid')).toBeInTheDocument();
    

  });

  it('displays empty state when no results populate', async () => {
    (InvoiceService.getAllInvoices as any).mockResolvedValue({
      content: [],
      totalPages: 0,
      totalElements: 0
    });

    render(<Invoices />);

    await waitFor(() => {
      expect(screen.getByText(/No invoices found\. Try adjusting your filters\./i)).toBeInTheDocument();
    });
  });

  it('displays error state when API call structurally rejects', async () => {
    (InvoiceService.getAllInvoices as any).mockRejectedValue(new Error('Network fault'));

    render(
      <>
        <Toaster />
        <Invoices />
      </>
    );

    await waitFor(() => {
      expect(screen.getByText(/Failed to load invoices\. Please try again\./i)).toBeInTheDocument();
    });
  });

  it('calls backend API with mapped parameters during debounce filtering changes', async () => {
    (InvoiceService.getAllInvoices as any).mockResolvedValue(createMockInvoicesPage(0));

    render(<Invoices />);

    // API is immediately called on mount with defaults
    await waitFor(() => {
      expect(InvoiceService.getAllInvoices).toHaveBeenCalledWith(
        expect.objectContaining({
          page: 0,
          sortBy: 'date',
          direction: 'desc'
        })
      );
    });

    const callsBeforeInput = (InvoiceService.getAllInvoices as any).mock.calls.length;

    // Simulate input typing into the Start Date DOM field using Label lookups
    const startDateInput = screen.getByLabelText(/Start Date/i);
    fireEvent.change(startDateInput, { target: { value: '2026-04-10' } });

    // Wait for internal component debounce interval (200ms) to sync and fire API again
    await waitFor(() => {
      expect((InvoiceService.getAllInvoices as any).mock.calls.length).toBeGreaterThan(callsBeforeInput);
    }, { timeout: 1000 });

    expect(InvoiceService.getAllInvoices).toHaveBeenLastCalledWith(
      expect.objectContaining({
        startDate: '2026-04-10'
      })
    );
  });
});
