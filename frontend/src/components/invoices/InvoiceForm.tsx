import React, { useMemo, useState, useEffect } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Trash2, Loader2, FileText, AlertTriangle, AlertCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useToast } from '@/components/ui/use-toast';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from '@/components/ui/dialog';
import { InvoiceService } from '@/api/invoiceService';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import InvoicePreview from '@/components/invoices/InvoicePreview';
import { ClientService } from '@/api/clientService';

// --- Validation Schemas ---
const lineItemSchema = z.object({
  description: z.string().min(1, 'Description is required').max(255, 'Description too long'),
  quantity: z.coerce.number().min(1, 'Quantity must be 1+').max(999999, 'Quantity too large'),
  unitPrice: z.coerce.number().min(0.01, 'Unit price must be positive').max(9999999, 'Price too large'),
});

const invoiceSchema = z.object({
  clientId: z.coerce.number().min(1, 'Client ID is required'),
  projectId: z.coerce.number().optional(),
  status: z.enum(['DRAFT', 'FINAL', 'SENT', 'PAID', 'OVERDUE']).default('DRAFT'),
  description: z.string().max(1000, 'Notes too long').optional(),
  lineItems: z.array(lineItemSchema).min(1, 'At least one line item is required'),
}).refine((data) => {
  if (data.status === 'FINAL' || data.status === 'SENT') {
    return data.lineItems.every(item => (Number(item.quantity) * Number(item.unitPrice)) > 0);
  }
  return true;
}, {
  message: "Finalized invoices cannot have items with zero or negative total amounts",
  path: ["lineItems"]
});

type InvoiceFormValues = z.infer<typeof invoiceSchema>;

const TAX_RATE = 0.10;

interface InvoiceFormProps {
  initialData?: any;
}

export default function InvoiceForm({ initialData }: InvoiceFormProps) {
  const { toast } = useToast();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);
  const [validatedData, setValidatedData] = useState<InvoiceFormValues | null>(null);
  const [clientData, setClientData] = useState<any | null>(null);

  const isEditMode = !!initialData;
  const isReadOnly = useMemo(() => {
    if (!initialData) return false;
    const restricted = ['SENT', 'PAID', 'OVERDUE', 'PARTIALLY_PAID', 'OVERPAID'];
    return restricted.includes(initialData.status);
  }, [initialData]);

  const showFinalizedWarning = initialData?.status === 'FINAL';

  const form = useForm<InvoiceFormValues>({
    resolver: zodResolver(invoiceSchema),
    mode: 'onChange',
    defaultValues: {
      clientId: initialData?.clientId || 0,
      projectId: initialData?.projectId || undefined,
      status: (initialData?.status as any) || 'DRAFT',
      description: initialData?.description || '',
      lineItems: initialData?.lineItems?.map((item: any) => ({
        description: item.description,
        quantity: item.quantity,
        unitPrice: item.unitPrice,
      })) || [{ description: '', quantity: 1, unitPrice: 0 }],
    },
  });

  const { control, handleSubmit, watch, formState: { isSubmitting, isValid }, reset } = form;

  const watchClientId = watch('clientId');

  // Fetch client details when clientId changes for live branding in preview
  useEffect(() => {
    const id = Number(watchClientId);
    if (!id || id < 1) { setClientData(null); return; }
    ClientService.getClientById(id)
      .then(setClientData)
      .catch(() => setClientData(null));
  }, [watchClientId]);

  useEffect(() => {
    if (initialData) {
      reset({
        clientId: initialData.clientId,
        projectId: initialData.projectId,
        status: initialData.status,
        description: initialData.description || '',
        lineItems: initialData.lineItems.map((item: any) => ({
          description: item.description,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
        })),
      });
    }
  }, [initialData, reset]);

  const { fields, append, remove } = useFieldArray({ name: 'lineItems', control });
  const watchLineItems = watch('lineItems');
  const watchDescription = watch('description');

  const { subtotal, tax, total } = useMemo(() => {
    let s = 0;
    (watchLineItems || []).forEach(item => {
      s += Math.max(0, Number(item?.quantity) || 0) * Math.max(0, Number(item?.unitPrice) || 0);
    });
    const t = s * TAX_RATE;
    return { subtotal: s, tax: t, total: s + t };
  }, [watchLineItems]);

  // Build a live invoice preview object from current form state
  const livePreviewInvoice = useMemo(() => ({
    invoiceNumber: initialData?.invoiceNumber ?? '[DRAFT]',
    createdAt: initialData?.createdAt ?? new Date().toISOString(),
    dueDate: initialData?.dueDate ?? undefined,
    status: form.getValues('status'),
    description: watchDescription ?? '',
    subtotal,
    tax,
    total,
    clientName:   clientData?.name    ?? `Client #${watchClientId}`,
    clientEmail:  clientData?.email   ?? '',
    clientAddress: clientData?.address ?? '',
    clientPhone:  clientData?.phone   ?? '',
    projectId:    form.getValues('projectId'),
    projectName:  initialData?.projectName ?? undefined,
    companyName:  initialData?.companyName ?? 'FREELANCEFLOW',
    companyEmail: initialData?.companyEmail ?? 'contact@freelanceflow.io',
    logoUrl:      initialData?.logoUrl ?? undefined,
    lineItems: (watchLineItems || []).map(item => ({
      description: item.description,
      quantity: Number(item.quantity),
      unitPrice: Number(item.unitPrice),
      amount: Math.max(0, Number(item.quantity)) * Math.max(0, Number(item.unitPrice)),
    })),
  }), [watchLineItems, watchDescription, clientData, watchClientId, subtotal, tax, total, initialData, form]);

  const mutation = useMutation({
    mutationFn: async (data: InvoiceFormValues) => {
      if (isEditMode) {
        return await InvoiceService.updateInvoice(initialData.id, {
          ...data,
          version: initialData.version
        } as any);
      } else {
        return await InvoiceService.createInvoice(data as any);
      }
    },
    onSuccess: (data: any) => {
      const invNum = data.invoiceNumber || 'N/A';
      toast({
        title: isEditMode ? 'Invoice Updated' : 'Invoice Created',
        description: `Successfully ${isEditMode ? 'updated' : 'generated'} invoice: ${invNum}`,
        action: (
          <Button onClick={() => navigate(`/invoices/${data.id}`)} variant="outline" size="sm">
            View Details
          </Button>
        )
      });
      if (!isEditMode) form.reset();
      queryClient.invalidateQueries({ queryKey: ['invoice', data.id] });
      setIsPreviewOpen(false);
    },
    onError: (error: any) => {
      const status = error?.response?.status;
      const message = error?.response?.data?.message || `Failed to ${isEditMode ? 'update' : 'create'} invoice`;
      if (status === 409) {
        toast({ title: 'Concurrent Update Error', description: 'This invoice was modified by another user. Please refresh.', variant: 'destructive' });
      } else if (status === 403) {
        toast({ title: 'Permission Denied', description: message, variant: 'destructive' });
      } else {
        toast({ title: 'Error', description: message, variant: 'destructive' });
      }
    },
  });

  const onSubmit = (data: InvoiceFormValues) => {
    if (isReadOnly) return;
    setValidatedData(data);
    setIsPreviewOpen(true);
  };

  const onConfirmSave = () => {
    if (validatedData) mutation.mutate(validatedData);
  };

  return (
    <Card className="max-w-4xl mx-auto shadow-md">
      <CardHeader>
        <CardTitle className="text-2xl flex items-center gap-2">
          <FileText className="h-6 w-6 text-primary" />
          {isEditMode ? `Edit Invoice #${initialData.invoiceNumber || initialData.id}` : 'Create Invoice'}
        </CardTitle>
        <CardDescription>
          {isEditMode ? 'Modify existing invoice details and line items.' : 'Fill all fields and use "Preview & Validate" to review before saving.'}
        </CardDescription>
      </CardHeader>

      <CardContent>
        {isReadOnly && (
          <Alert variant="destructive" className="mb-6 bg-red-50 border-red-200">
            <AlertCircle className="h-4 w-4 text-red-600" />
            <AlertTitle className="text-red-800 font-bold">Editing Restricted</AlertTitle>
            <AlertDescription className="text-red-700">
              Cannot edit a sent invoice. Please create a credit note.
            </AlertDescription>
          </Alert>
        )}

        {showFinalizedWarning && !isReadOnly && (
          <Alert className="mb-6 bg-amber-50 border-amber-200 text-amber-800">
            <AlertTriangle className="h-4 w-4 text-amber-600" />
            <AlertTitle className="font-bold">Finalized Invoice</AlertTitle>
            <AlertDescription>Warning: You are editing a finalized invoice.</AlertDescription>
          </Alert>
        )}

        <Form {...form}>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">

            {/* Invoice Meta */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <FormField
                control={control}
                name="clientId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Client ID <span className="text-red-500">*</span></FormLabel>
                    <FormControl>
                      <Input type="number" placeholder="Client ID" {...field} disabled={isReadOnly} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={control}
                name="projectId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Project ID (Optional)</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        placeholder="Project ID..."
                        {...field}
                        value={field.value || ''}
                        disabled={isReadOnly}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={control}
                name="status"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Status</FormLabel>
                    <Select onValueChange={field.onChange} value={field.value} disabled={isReadOnly}>
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select status" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="DRAFT">DRAFT</SelectItem>
                        <SelectItem value="FINAL">FINAL</SelectItem>
                        {isReadOnly && (
                          <>
                            <SelectItem value="SENT">SENT</SelectItem>
                            <SelectItem value="PAID">PAID</SelectItem>
                            <SelectItem value="OVERDUE">OVERDUE</SelectItem>
                          </>
                        )}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            {/* Notes / Description */}
            <FormField
              control={control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Notes <span className="text-xs text-muted-foreground">(optional — appears on invoice)</span></FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder="e.g. Bank: XYZ Bank • Account: 1234567890 • Thank you for your business!"
                      className="resize-none h-24"
                      {...field}
                      disabled={isReadOnly}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Line Items */}
            <div className={`mt-2 border rounded-lg p-5 ${isReadOnly ? 'bg-slate-100/50' : 'bg-slate-50/50'}`}>
              <div className="flex justify-between items-center mb-4">
                <h3 className="font-semibold text-lg text-slate-800">Line Items <span className="text-red-500 text-sm">*</span></h3>
                {!isReadOnly && (
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => append({ description: '', quantity: 1, unitPrice: 0 })}
                    className="gap-2"
                  >
                    <Plus className="h-4 w-4" /> Add Item
                  </Button>
                )}
              </div>

              {fields.length === 0 && (
                <p className="text-sm text-muted-foreground text-center py-4 italic">
                  Add at least one line item to enable preview.
                </p>
              )}

              <div className="space-y-4">
                {fields.map((field, index) => {
                  const itemQ = Number(watchLineItems[index]?.quantity) || 0;
                  const itemP = Number(watchLineItems[index]?.unitPrice) || 0;
                  const autoAmount = Math.max(0, itemQ) * Math.max(0, itemP);

                  return (
                    <div
                      key={field.id}
                      className="flex flex-col md:flex-row gap-3 items-start bg-white p-3 rounded-md shadow-sm border border-slate-100/80"
                    >
                      <div className="flex-1 w-full">
                        <FormField
                          control={control}
                          name={`lineItems.${index}.description`}
                          render={({ field }) => (
                            <FormItem className="space-y-1">
                              <FormLabel className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Service Reference</FormLabel>
                              <FormControl>
                                <Input placeholder="Brief task description" className="h-9 text-sm" {...field} disabled={isReadOnly} />
                              </FormControl>
                              <FormMessage className="text-[10px]" />
                            </FormItem>
                          )}
                        />
                      </div>

                      <div className="w-full md:w-20">
                        <FormField
                          control={control}
                          name={`lineItems.${index}.quantity`}
                          render={({ field }) => (
                            <FormItem className="space-y-1">
                              <FormLabel className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Qty</FormLabel>
                              <FormControl>
                                <Input type="number" min="1" className="h-9 text-sm" {...field} disabled={isReadOnly} />
                              </FormControl>
                              <FormMessage className="text-[10px]" />
                            </FormItem>
                          )}
                        />
                      </div>

                      <div className="w-full md:w-28">
                        <FormField
                          control={control}
                          name={`lineItems.${index}.unitPrice`}
                          render={({ field }) => (
                            <FormItem className="space-y-1">
                              <FormLabel className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Unit Price</FormLabel>
                              <FormControl>
                                <Input type="number" step="0.01" min="0" className="h-9 text-sm" {...field} disabled={isReadOnly} />
                              </FormControl>
                              <FormMessage className="text-[10px]" />
                            </FormItem>
                          )}
                        />
                      </div>

                      <div className="w-full md:w-24 pt-1 md:pt-[26px] flex md:justify-center">
                        <span className="font-semibold text-slate-600 text-sm font-mono">
                          ${autoAmount.toFixed(2)}
                        </span>
                      </div>

                      {!isReadOnly && (
                        <div className="pt-1 md:pt-[22px] w-full md:w-auto flex justify-end">
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            onClick={() => remove(index)}
                            className="h-8 w-8 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-full"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              {form.formState.errors.lineItems?.root && (
                <p className="text-sm font-medium text-destructive mt-3">
                  {form.formState.errors.lineItems.root.message}
                </p>
              )}
            </div>

            {/* Totals summary */}
            <div className="flex justify-end pt-2">
              <div className="w-full md:w-1/3 space-y-3 bg-slate-50 p-5 rounded-lg border">
                <div className="flex justify-between text-sm text-slate-600">
                  <span>Subtotal:</span>
                  <span className="font-medium">${subtotal.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-sm text-slate-600">
                  <span>Tax (10%):</span>
                  <span className="font-medium">${tax.toFixed(2)}</span>
                </div>
                <div className="pt-3 flex justify-between border-t border-slate-200 text-lg font-bold">
                  <span>Total Due:</span>
                  <span className="text-primary">${total.toFixed(2)}</span>
                </div>
              </div>
            </div>

            {/* Submit */}
            {!isReadOnly && (
              <div className="flex justify-end">
                <Button
                  type="submit"
                  className="w-full md:w-auto mt-4 font-semibold"
                  disabled={isSubmitting || !isValid}
                  title={!isValid ? 'Fill all required fields before previewing' : ''}
                >
                  {isSubmitting ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <FileText className="mr-2 h-4 w-4" />}
                  {isEditMode ? 'Update & Preview' : 'Preview & Validate'}
                </Button>
              </div>
            )}
          </form>
        </Form>

        {/* ── Preview Modal — renders InvoicePreview (mirrors PDF) ── */}
        <Dialog open={isPreviewOpen} onOpenChange={setIsPreviewOpen}>
          <DialogContent className="max-w-4xl w-[95vw] max-h-[92vh] overflow-y-auto p-0 border-none bg-transparent shadow-none [&>button]:bg-white [&>button]:rounded-full [&>button]:p-1.5 [&>button]:shadow-sm">
            <DialogHeader className="sr-only">
              <DialogTitle>Invoice Preview</DialogTitle>
              <DialogDescription>Review your invoice before saving.</DialogDescription>
            </DialogHeader>

            <div className="rounded-lg overflow-hidden shadow-2xl">
              <InvoicePreview invoice={livePreviewInvoice} />
            </div>

            <DialogFooter className="bg-white px-6 py-4 border-t border-slate-100 flex items-center justify-between rounded-b-lg pointer-events-auto">
              <Button type="button" variant="outline" onClick={() => setIsPreviewOpen(false)} className="bg-white">
                Back to Editor
              </Button>
              <Button
                type="button"
                onClick={onConfirmSave}
                disabled={mutation.isPending}
                className="min-w-[160px]"
              >
                {mutation.isPending ? (
                  <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Saving...</>
                ) : (
                  isEditMode ? 'Commit Changes' : 'Finalize & Commit'
                )}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </CardContent>
    </Card>
  );
}
