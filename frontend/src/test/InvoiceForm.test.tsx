import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import InvoiceForm from '../components/invoices/InvoiceForm';
import { InvoiceService } from '@/api/invoiceService';

const mockToast = vi.fn();

// Mock dependencies
vi.mock('@/api/invoiceService', () => ({
  InvoiceService: {
    createInvoice: vi.fn(),
  },
}));

vi.mock('@/components/ui/use-toast', () => ({
  useToast: () => ({
    toast: mockToast,
  }),
}));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const renderWithProviders = (ui: React.ReactElement) => {
  return render(
    <QueryClientProvider client={queryClient}>
      {ui}
    </QueryClientProvider>
  );
};

describe('InvoiceForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders correctly initially', () => {
    renderWithProviders(<InvoiceForm />);
    expect(screen.getByText(/Create Invoice/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Client ID/i)).toBeInTheDocument();
  });

  it('adds and removes line items', async () => {
    renderWithProviders(<InvoiceForm />);
    
    const addButton = screen.getByRole('button', { name: /Add Item/i });
    fireEvent.click(addButton);

    // Initial item + 1 new item = 2 total
    await waitFor(() => {
      const items = screen.getAllByLabelText(/Service Reference/i);
      expect(items).toHaveLength(2);
    });

    const removeButtons = screen.getAllByRole('button').filter(btn => btn.className.includes('text-red-400'));
    fireEvent.click(removeButtons[0]);

    await waitFor(() => {
      expect(screen.getAllByLabelText(/Service Reference/i)).toHaveLength(1);
    });
  });

  it('updates calculations when quantity/price changes', async () => {
    const { container } = renderWithProviders(<InvoiceForm />);
    
    // Fill required fields to ensure form is in a stable state
    fireEvent.change(screen.getByLabelText(/Client ID/i), { target: { value: '123' } });
    fireEvent.change(screen.getByLabelText(/Service Reference/i), { target: { value: 'Test Service' } });
    fireEvent.change(screen.getByLabelText(/Qty/i), { target: { value: '2' } });
    fireEvent.change(screen.getByLabelText(/Unit Price/i), { target: { value: '100' } });

    // Wait for the total display to update (subtotal 200, tax 20, total 220)
    await waitFor(() => {
      const text = container.textContent || '';
      expect(text.includes('200.00')).toBe(true);
      expect(text.includes('20.00')).toBe(true);
      expect(text.includes('220.00')).toBe(true);
    }, { timeout: 4000 });
  });

  it('opens preview modal with correct data', async () => {
    renderWithProviders(<InvoiceForm />);
    
    fireEvent.change(screen.getByLabelText(/Client ID/i), { target: { value: '123' } });
    fireEvent.change(screen.getByLabelText(/Service Reference/i), { target: { value: 'Test Service' } });
    fireEvent.change(screen.getByLabelText(/Qty/i), { target: { value: '2' } });
    fireEvent.change(screen.getByLabelText(/Unit Price/i), { target: { value: '50' } });

    const previewButton = screen.getByRole('button', { name: /Preview & Validate/i });
    await waitFor(() => expect(previewButton).toBeEnabled(), { timeout: 4000 });
    fireEvent.click(previewButton);

    // Modal should appear
    expect(await screen.findByText(/ANTIGRAVITY SOLUTIONS/i)).toBeInTheDocument();
    expect(await screen.findByText(/Billed To/i)).toBeInTheDocument();
    
    // Check values in modal using flexible includes
    await waitFor(() => {
        expect(screen.getAllByText((_, el) => el?.textContent?.includes('Test Service') ?? false).length).toBeGreaterThan(0);
        expect(screen.getAllByText((_, el) => el?.textContent?.includes('123') ?? false).length).toBeGreaterThan(0);
        expect(screen.getAllByText((_, el) => el?.textContent?.includes('110.00') ?? false).length).toBeGreaterThan(0);
    });
  });

  it('shows error toast on 409 conflict', async () => {
    vi.mocked(InvoiceService.createInvoice).mockRejectedValueOnce({
      response: {
        status: 409,
        data: { message: 'Conflict' }
      }
    });

    renderWithProviders(<InvoiceForm />);
    
    // Fill required fields with valid data
    fireEvent.change(screen.getByLabelText(/Client ID/i), { target: { value: '123' } });
    fireEvent.change(screen.getByLabelText(/Service Reference/i), { target: { value: 'Test' } });
    fireEvent.change(screen.getByLabelText(/Qty/i), { target: { value: '1' } });
    fireEvent.change(screen.getByLabelText(/Unit Price/i), { target: { value: '100' } });
    
    // Open preview - wait for button to be enabled first
    const previewButton = await screen.findByRole('button', { name: /Preview & Validate/i });
    await waitFor(() => expect(previewButton).not.toBeDisabled());
    fireEvent.click(previewButton);
    
    // Confirm save - use findByText or more specific matcher
    const saveButton = await screen.findByRole('button', { name: /Finalize & Commit/i });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(mockToast).toHaveBeenCalledWith(expect.objectContaining({
        title: 'Concurrent Update Error',
        variant: 'destructive',
      }));
    });
  });
});
