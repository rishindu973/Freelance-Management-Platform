package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.InvoiceCreateRequest;
import com.freelance.freelancepm.dto.InvoiceLineItemRequest;
import com.freelance.freelancepm.dto.InvoiceResponse;
import com.freelance.freelancepm.dto.InvoiceUpdateRequest;
import com.freelance.freelancepm.entity.Invoice;
import com.freelance.freelancepm.entity.Project;
import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.ClientInvoiceSequenceRepository;
import com.freelance.freelancepm.repository.ClientRepository;
import com.freelance.freelancepm.entity.ClientInvoiceSequence;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ClientInvoiceSequenceRepository sequenceRepository;

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
        mockClient.setCode("ABC");

        mockProject = new Project();
        mockProject.setId(10);

        // Standard mock to return what is saved for sequences
        lenient().when(sequenceRepository.saveAndFlush(any(ClientInvoiceSequence.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        lenient().when(sequenceRepository.save(any(ClientInvoiceSequence.class)))
                .thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    void create_SequentialNumbering_SameClient_ShouldIncrement() {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setClientId(1);

        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        // First call - Sequence starts at 0, next is 1
        when(sequenceRepository.findByClientIdAndYear(anyInt(), anyInt())).thenReturn(Optional.empty());
        InvoiceResponse res1 = invoiceService.create(req);
        assertEquals("ABC-2026-0001", res1.getInvoiceNumber());

        // Second call - Mock existing sequence at 1
        when(sequenceRepository.findByClientIdAndYear(anyInt(), anyInt()))
                .thenReturn(Optional.of(new ClientInvoiceSequence(101L, mockClient, 2026, 1)));
        InvoiceResponse res2 = invoiceService.create(req);
        assertEquals("ABC-2026-0002", res2.getInvoiceNumber());

        // Third call - Mock existing sequence at 2
        when(sequenceRepository.findByClientIdAndYear(anyInt(), anyInt()))
                .thenReturn(Optional.of(new ClientInvoiceSequence(101L, mockClient, 2026, 2)));
        InvoiceResponse res3 = invoiceService.create(req);
        assertEquals("ABC-2026-0003", res3.getInvoiceNumber());
    }

    @Test
    void create_PerClientIndependence_ShouldHaveSeparateSequences() {
        Client clientB = new Client();
        clientB.setId(2);
        clientB.setCode("XYZ");

        InvoiceCreateRequest reqA = new InvoiceCreateRequest();
        reqA.setClientId(1);
        InvoiceCreateRequest reqB = new InvoiceCreateRequest();
        reqB.setClientId(2);

        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(clientRepository.findById(2)).thenReturn(Optional.of(clientB));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        // Client A already at 5
        when(sequenceRepository.findByClientIdAndYear(eq(1), anyInt()))
                .thenReturn(Optional.of(new ClientInvoiceSequence(1L, mockClient, 2026, 5)));
        // Client B is new
        when(sequenceRepository.findByClientIdAndYear(eq(2), anyInt())).thenReturn(Optional.empty());

        assertEquals("ABC-2026-0006", invoiceService.create(reqA).getInvoiceNumber());
        assertEquals("XYZ-2026-0001", invoiceService.create(reqB).getInvoiceNumber());
    }

    @Test
    void create_YearRollover_ShouldResetSequence() {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setClientId(1);
        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            // 2025: sequence at 10 -> expects 0011
            mockedLocalDate.when(LocalDate::now).thenReturn(LocalDate.of(2025, 12, 31));
            when(sequenceRepository.findByClientIdAndYear(eq(1), eq(2025)))
                    .thenReturn(Optional.of(new ClientInvoiceSequence(1L, mockClient, 2025, 10)));

            assertEquals("ABC-2025-0011", invoiceService.create(req).getInvoiceNumber());

            // 2026: reset to 0001
            mockedLocalDate.when(LocalDate::now).thenReturn(LocalDate.of(2026, 1, 1));
            // Important: we need to reset the stubbing or use different matcher if we
            // change the mock middle-test
            // But since years are different, findByClientIdAndYear(1, 2026) will return
            // empty as mocked below
            when(sequenceRepository.findByClientIdAndYear(eq(1), eq(2026))).thenReturn(Optional.empty());

            assertEquals("ABC-2026-0001", invoiceService.create(req).getInvoiceNumber());
        }
    }

    @Test
    void create_FormatValidation_ShouldMatchPattern() {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setClientId(1);
        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(sequenceRepository.findByClientIdAndYear(anyInt(), anyInt())).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        InvoiceResponse res = invoiceService.create(req);
        // Pattern: [A-Z]+-YYYY-NNNN
        assertTrue(res.getInvoiceNumber().matches("^[A-Z]+-\\d{4}-\\d{4}$"));
    }

    @Test
    void create_FinalWithoutItems_ShouldThrowIllegalStateException() {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setClientId(1);
        req.setStatus(Invoice.Status.FINAL);
        req.setLineItems(null);

        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(sequenceRepository.findByClientIdAndYear(anyInt(), anyInt())).thenReturn(Optional.empty());

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
        when(sequenceRepository.findByClientIdAndYear(anyInt(), anyInt())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> invoiceService.create(req));
        assertTrue(ex.getMessage().contains("Quantity and unit price cannot be negative"));
    }

    @Test
    void update_FinalInvoice_ShouldThrowIllegalStateException() {
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(100);
        existingInvoice.setStatus(Invoice.Status.FINAL);

        when(invoiceRepository.findById(100)).thenReturn(Optional.of(existingInvoice));

        InvoiceUpdateRequest req = new InvoiceUpdateRequest();
        req.setStatus(Invoice.Status.DRAFT);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> invoiceService.update(100, req));
        assertTrue(ex.getMessage().contains("Only DRAFT invoices can be updated"));
    }

    @Test
    void update_ValidDraft_ShouldRecalculateTotals() {
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(100);
        existingInvoice.setStatus(Invoice.Status.DRAFT);
        existingInvoice.setClient(mockClient);

        when(invoiceRepository.findById(100)).thenReturn(Optional.of(existingInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        InvoiceUpdateRequest req = new InvoiceUpdateRequest();
        InvoiceLineItemRequest newItem = new InvoiceLineItemRequest();
        newItem.setDescription("New Service");
        newItem.setQuantity(5);
        newItem.setUnitPrice(new BigDecimal("50.00"));
        req.setLineItems(Arrays.asList(newItem));

        InvoiceResponse response = invoiceService.update(100, req);

        // subtotal 250, tax 25, total 275
        assertEquals(0, new BigDecimal("250.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("25.00").compareTo(response.getTax()));
        assertEquals(0, new BigDecimal("275.00").compareTo(response.getTotal()));
    }

    @Test
    void update_ClientChange_ShouldRegenerateNumbering() {
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(100);
        existingInvoice.setStatus(Invoice.Status.DRAFT);
        existingInvoice.setClient(mockClient);
        existingInvoice.setInvoiceNumber("ABC-2026-0001");

        Client newClient = new Client();
        newClient.setId(2);
        newClient.setCode("XYZ");

        when(invoiceRepository.findById(100)).thenReturn(Optional.of(existingInvoice));
        when(clientRepository.findById(2)).thenReturn(Optional.of(newClient));
        when(sequenceRepository.findByClientIdAndYear(eq(2), anyInt())).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        InvoiceUpdateRequest req = new InvoiceUpdateRequest();
        req.setClientId(2);

        InvoiceResponse response = invoiceService.update(100, req);

        assertEquals("XYZ-2026-0001", response.getInvoiceNumber());
        assertEquals(2, response.getClientId());
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
        when(sequenceRepository.findByClientIdAndYear(anyInt(), anyInt())).thenReturn(Optional.empty());

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
        existing.setId(1);
        existing.setStatus(Invoice.Status.DRAFT);
        existing.setClient(mockClient);

        when(invoiceRepository.findById(1)).thenReturn(Optional.of(existing));
        when(invoiceRepository.save(any(Invoice.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Invoice.class, 1));

        // Act & Assert
        assertThrows(com.freelance.freelancepm.exception.ConflictException.class, () -> invoiceService.update(1, req));
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
