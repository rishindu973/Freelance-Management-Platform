import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { SendInvoiceModal } from '../components/invoices/SendInvoiceModal';
import { InvoiceService } from '@/api/invoiceService';

const mockToast = vi.fn();

vi.mock('@/api/invoiceService', () => ({
  InvoiceService: {
    sendInvoice: vi.fn(),
  },
}));

vi.mock('@/components/ui/use-toast', () => ({
  useToast: () => ({
    toast: mockToast,
  }),
}));

const defaultProps = {
  isOpen: true,
  onClose: vi.fn(),
  invoiceId: 1,
  invoiceNumber: 'INV-2026-0001',
  client: {
    id: 1,
    name: 'Test Client',
    email: 'client@example.com',
    phone: '123456',
  },
  onSuccess: vi.fn(),
};

describe('SendInvoiceModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ── Rendering ──────────────────────────────────────────────────────

  it('renders the modal with title and client email', () => {
    render(<SendInvoiceModal {...defaultProps} />);

    // "Send Invoice" appears in the title and the button
    expect(screen.getAllByText(/Send Invoice/i).length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('client@example.com')).toBeInTheDocument();
    expect(screen.getByText('Client Primary Contact')).toBeInTheDocument();
  });

  it('shows "No recipients selected" when empty', () => {
    render(<SendInvoiceModal {...defaultProps} />);
    expect(screen.getByText('No recipients selected')).toBeInTheDocument();
  });

  // ── Email Validation ──────────────────────────────────────────────

  it('rejects invalid email addresses', async () => {
    render(<SendInvoiceModal {...defaultProps} />);

    const input = screen.getByPlaceholderText('Enter email address...');
    fireEvent.change(input, { target: { value: 'not-an-email' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    await waitFor(() => {
      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'Invalid Email',
          variant: 'destructive',
        })
      );
    });
  });

  it('accepts valid email addresses and shows badge', async () => {
    render(<SendInvoiceModal {...defaultProps} />);

    const input = screen.getByPlaceholderText('Enter email address...');
    fireEvent.change(input, { target: { value: 'test@valid.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    await waitFor(() => {
      expect(screen.getByText('test@valid.com')).toBeInTheDocument();
    });
  });

  it('prevents duplicate email entries', async () => {
    render(<SendInvoiceModal {...defaultProps} />);

    const input = screen.getByPlaceholderText('Enter email address...');

    // Add once
    fireEvent.change(input, { target: { value: 'dup@test.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    // Try to add again
    fireEvent.change(input, { target: { value: 'dup@test.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    await waitFor(() => {
      const badges = screen.getAllByText('dup@test.com');
      expect(badges).toHaveLength(1);
    });
  });

  // ── Client Email Toggle ──────────────────────────────────────────

  it('adds client email when checkbox is toggled on', async () => {
    render(<SendInvoiceModal {...defaultProps} />);

    const checkbox = screen.getByRole('checkbox');
    fireEvent.click(checkbox);

    await waitFor(() => {
      // The email should now appear as a badge as well
      const badges = screen.getAllByText('client@example.com');
      expect(badges.length).toBeGreaterThanOrEqual(1);
    });
  });

  // ── Submission: No Recipients ──────────────────────────────────────

  it('blocks sending when no recipients selected', async () => {
    render(<SendInvoiceModal {...defaultProps} />);
    
    // The Send Invoice button should be disabled with no recipients
    const sendButton = screen.getByRole('button', { name: /Send Invoice/i });
    expect(sendButton).toBeDisabled();
  });

  // ── Submission: API Call Flow ──────────────────────────────────────

  it('calls API with correct recipients on send', async () => {
    vi.mocked(InvoiceService.sendInvoice).mockResolvedValueOnce(undefined);

    render(<SendInvoiceModal {...defaultProps} />);

    // Add an email
    const input = screen.getByPlaceholderText('Enter email address...');
    fireEvent.change(input, { target: { value: 'send@test.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    // Click send
    const sendButton = await screen.findByRole('button', { name: /Send Invoice/i });
    await waitFor(() => expect(sendButton).not.toBeDisabled());
    fireEvent.click(sendButton);

    await waitFor(() => {
      expect(InvoiceService.sendInvoice).toHaveBeenCalledWith(1, ['send@test.com']);
      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: 'Success' })
      );
      expect(defaultProps.onSuccess).toHaveBeenCalled();
      expect(defaultProps.onClose).toHaveBeenCalled();
    });
  });

  it('sends to multiple recipients', async () => {
    vi.mocked(InvoiceService.sendInvoice).mockResolvedValueOnce(undefined);

    render(<SendInvoiceModal {...defaultProps} />);

    const input = screen.getByPlaceholderText('Enter email address...');

    // Add first email
    fireEvent.change(input, { target: { value: 'a@test.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    // Add second email
    fireEvent.change(input, { target: { value: 'b@test.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    const sendButton = await screen.findByRole('button', { name: /Send Invoice/i });
    await waitFor(() => expect(sendButton).not.toBeDisabled());
    fireEvent.click(sendButton);

    await waitFor(() => {
      expect(InvoiceService.sendInvoice).toHaveBeenCalledWith(
        1,
        expect.arrayContaining(['a@test.com', 'b@test.com'])
      );
    });
  });

  // ── Submission: Error Handling ──────────────────────────────────────

  it('shows error toast on API failure', async () => {
    vi.mocked(InvoiceService.sendInvoice).mockRejectedValueOnce({
      response: { data: { message: 'Manager not found' } },
    });

    render(<SendInvoiceModal {...defaultProps} />);

    const input = screen.getByPlaceholderText('Enter email address...');
    fireEvent.change(input, { target: { value: 'fail@test.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    const sendButton = await screen.findByRole('button', { name: /Send Invoice/i });
    await waitFor(() => expect(sendButton).not.toBeDisabled());
    fireEvent.click(sendButton);

    await waitFor(() => {
      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'Error Sending Invoice',
          variant: 'destructive',
        })
      );
      // Modal should NOT be closed on error
      expect(defaultProps.onClose).not.toHaveBeenCalled();
    });
  });

  it('shows generic error on network failure', async () => {
    vi.mocked(InvoiceService.sendInvoice).mockRejectedValueOnce(new Error('Network Error'));

    render(<SendInvoiceModal {...defaultProps} />);

    const input = screen.getByPlaceholderText('Enter email address...');
    fireEvent.change(input, { target: { value: 'net@test.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    const sendButton = await screen.findByRole('button', { name: /Send Invoice/i });
    await waitFor(() => expect(sendButton).not.toBeDisabled());
    fireEvent.click(sendButton);

    await waitFor(() => {
      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({
          description: 'Something went wrong. Please try again.',
        })
      );
    });
  });

  // ── Removing Recipient ──────────────────────────────────────────────

  it('removes email when X button is clicked', async () => {
    render(<SendInvoiceModal {...defaultProps} />);

    const input = screen.getByPlaceholderText('Enter email address...');
    fireEvent.change(input, { target: { value: 'removeme@test.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    await waitFor(() => {
      expect(screen.getByText('removeme@test.com')).toBeInTheDocument();
    });

    // Click the X button next to the badge
    const removeButton = screen.getByText('removeme@test.com').parentElement?.querySelector('button');
    if (removeButton) fireEvent.click(removeButton);

    await waitFor(() => {
      expect(screen.queryByText('removeme@test.com')).not.toBeInTheDocument();
    });
  });
});
