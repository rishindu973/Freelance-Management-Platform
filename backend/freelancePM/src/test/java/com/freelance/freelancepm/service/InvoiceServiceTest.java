package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.InvoiceCreateRequest;
import com.freelance.freelancepm.dto.InvoiceLineItemRequest;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.dto.InvoiceUpdateRequest;
import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.ClientRepository;
import com.freelance.freelancepm.repository.InvoiceRepository;
import com.freelance.freelancepm.repository.ProjectRepository;
import com.freelance.freelancepm.mapper.InvoiceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Spy
    private InvoiceMapper invoiceMapper = new InvoiceMapper();

    @InjectMocks
    private InvoiceService invoiceService;

    private Client mockClient;
    private Project mockProject;

    @BeforeEach
    void setUp() {
        mockClient = new Client();
        mockClient.setId(1);

        mockProject = new Project();
        mockProject.setId(10);
    }

    @Test
    void create_ValidDraft_ShouldSucceed() {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setClientId(1);
        req.setStatus(Invoice.Status.DRAFT);
        
        InvoiceLineItemRequest item = new InvoiceLineItemRequest();
        item.setDescription("Service 1");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100.00"));
        req.setLineItems(Arrays.asList(item));

        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        InvoiceResponse response = invoiceService.create(req);

        assertNotNull(response);
        assertEquals(Invoice.Status.DRAFT, response.getStatus());
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getTax()));
        assertEquals(0, new BigDecimal("220.00").compareTo(response.getTotal()));
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void create_FinalWithoutItems_ShouldThrowIllegalStateException() {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setClientId(1);
        req.setStatus(Invoice.Status.FINAL);
        req.setLineItems(null);

        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> invoiceService.create(req));
        assertTrue(ex.getMessage().contains("Finalized invoices must have at least one line item"));
    }

    @Test
    void create_NegativeValues_ShouldThrowIllegalArgumentException() {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setClientId(1);
        
        InvoiceLineItemRequest item = new InvoiceLineItemRequest();
        item.setDescription("Bad item");
        item.setQuantity(-1);
        item.setUnitPrice(new BigDecimal("100.00"));
        req.setLineItems(Arrays.asList(item));

        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> invoiceService.create(req));
        assertTrue(ex.getMessage().contains("Quantity and unit price cannot be negative"));
    }

    @Test
    void update_FinalInvoice_ShouldThrowIllegalStateException() {
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(100L);
        existingInvoice.setStatus(Invoice.Status.FINAL);

        when(invoiceRepository.findById(100L)).thenReturn(Optional.of(existingInvoice));

        InvoiceUpdateRequest req = new InvoiceUpdateRequest();
        req.setStatus(Invoice.Status.DRAFT);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> invoiceService.update(100L, req));
        assertTrue(ex.getMessage().contains("Only DRAFT invoices can be updated"));
    }

    @Test
    void update_ValidDraft_ShouldRecalculateTotals() {
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(100L);
        existingInvoice.setStatus(Invoice.Status.DRAFT);
        existingInvoice.setClient(mockClient);

        when(invoiceRepository.findById(100L)).thenReturn(Optional.of(existingInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        InvoiceUpdateRequest req = new InvoiceUpdateRequest();
        InvoiceLineItemRequest newItem = new InvoiceLineItemRequest();
        newItem.setDescription("New Service");
        newItem.setQuantity(5);
        newItem.setUnitPrice(new BigDecimal("50.00"));
        req.setLineItems(Arrays.asList(newItem));

        InvoiceResponse response = invoiceService.update(100L, req);

        // subtotal 250, tax 25, total 275
        assertEquals(0, new BigDecimal("250.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("25.00").compareTo(response.getTax()));
        assertEquals(0, new BigDecimal("275.00").compareTo(response.getTotal()));
    }

    @Test
    void create_ShouldThrowException_WhenProjectDoesNotBelongToClient() {
        // Arrange
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setClientId(1);
        req.setProjectId(2);
        req.setLineItems(List.of(createLineItemRequest()));

        Client client = new Client();
        client.setId(1);
        Project project = new Project();
        project.setId(2);
        Client otherClient = new Client();
        otherClient.setId(99); 
        project.setClient(otherClient);

        when(clientRepository.findById(1)).thenReturn(Optional.of(client));
        when(projectRepository.findById(2)).thenReturn(Optional.of(project));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> invoiceService.create(req));
        assertTrue(ex.getMessage().contains("Project does not belong to the selected client"));
    }

    @Test
    void update_ShouldThrowConflictException_WhenOptimisticLockingFails() {
        // Arrange
        InvoiceUpdateRequest req = new InvoiceUpdateRequest();
        req.setLineItems(List.of(createLineItemRequest()));

        Invoice existing = new Invoice();
        existing.setId(1L);
        existing.setStatus(Invoice.Status.DRAFT);
        existing.setClient(mockClient);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(invoiceRepository.save(any(Invoice.class))).thenThrow(new ObjectOptimisticLockingFailureException(Invoice.class, 1L));

        // Act & Assert
        assertThrows(com.freelance.freelancepm.exception.ConflictException.class, () -> invoiceService.update(1L, req));
    }

    private InvoiceLineItemRequest createLineItemRequest() {
        InvoiceLineItemRequest item = new InvoiceLineItemRequest();
        item.setDescription("Test Service");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setAmount(new BigDecimal("200.00"));
        return item;
    }
}
