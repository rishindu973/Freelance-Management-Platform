package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.*;
import com.freelance.freelancepm.entity.*;
import com.freelance.freelancepm.exception.ConflictException;
import com.freelance.freelancepm.exception.NotFoundException;
import com.freelance.freelancepm.mapper.InvoiceMapper;
import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private ClientInvoiceSequenceRepository sequenceRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private InvoicePdfService pdfService;

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
        mockClient.setName("John Doe");

        mockProject = new Project();
        mockProject.setId(10);

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
    }

    @Test
    void sendInvoice_Success() throws Exception {
        // Arrange
        Integer invoiceId = 1;
        SendInvoiceRequest request = new SendInvoiceRequest(List.of("client@example.com"));
        
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setInvoiceNumber("INV-001");
        invoice.setClient(mockClient);
        invoice.setTotal(new BigDecimal("100.00"));
        invoice.setDueDate(LocalDate.now().plusDays(30));
        
        Project project = new Project();
        project.setManagerId(5);
        invoice.setProject(project);

        Manager manager = new Manager();
        manager.setCompanyName("Manager Co");
        manager.setContactNumber("123456789");
        manager.setLogoUrl("http://logo.com");
        manager.setAddress("123 Street");
        com.freelance.freelancepm.entity.User user = new com.freelance.freelancepm.entity.User();
        user.setEmail("manager@example.com");
        manager.setUser(user);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(pdfService.generateInvoicePdf(invoiceId)).thenReturn("pdf content".getBytes());
        when(managerRepository.findById(5)).thenReturn(Optional.of(manager));

        // Act
        invoiceService.sendInvoice(invoiceId, request);

        // Assert
        verify(emailService, times(1)).sendInvoiceEmail(
                eq("client@example.com"),
                eq("John Doe"),
                eq("INV-001"),
                eq("100.00"),
                eq("$"),
                anyString(),
                eq("Manager Co"),
                eq("manager@example.com"),
                eq("123456789"),
                eq("http://logo.com"),
                eq("123 Street"),
                anyString(),
                any(),
                anyString()
        );
        assertEquals(Invoice.Status.SENT, invoice.getStatus());
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void create_FinalWithoutItems_ShouldThrowIllegalStateException() {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setClientId(1);
        req.setStatus(Invoice.Status.FINAL);
        req.setLineItems(null);

        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(sequenceRepository.findByClientIdAndYear(anyInt(), anyInt())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> invoiceService.create(req));
    }

    @Test
    void update_FinalInvoice_ShouldThrowIllegalStateException() {
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(100);
        existingInvoice.setStatus(Invoice.Status.FINAL);

        when(invoiceRepository.findById(100)).thenReturn(Optional.of(existingInvoice));

        InvoiceUpdateRequest req = new InvoiceUpdateRequest();
        req.setStatus(Invoice.Status.DRAFT);

        assertThrows(IllegalStateException.class, () -> invoiceService.update(100, req));
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
