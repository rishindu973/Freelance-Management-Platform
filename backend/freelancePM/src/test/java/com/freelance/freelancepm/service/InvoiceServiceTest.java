package com.freelance.freelancepm.service;

import com.freelance.freelancepm.dto.*;
import com.freelance.freelancepm.entity.*;
import com.freelance.freelancepm.entity.InvoiceStatus;
import com.freelance.freelancepm.exception.ConflictException;
import com.freelance.freelancepm.exception.NotFoundException;
import com.freelance.freelancepm.mapper.InvoiceMapper;
import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private EmailDispatcherService emailDispatcherService;

    @Mock
    private InvoicePdfService pdfService;

    @Mock
    private InvoiceEditValidator editValidator;

    @Mock
    private InvoiceCalculationService calculationService;

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

        // Assert: dispatch delegated to EmailDispatcherService
        verify(emailDispatcherService, times(1)).dispatchInvoices(
                eq(invoice),
                eq(manager),
                eq(List.of("client@example.com")),
                anyString(),
                any(byte[].class),
                anyString());
    }

    @Test
    void create_FinalWithoutItems_ShouldThrowIllegalStateException() {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setClientId(1);
        req.setStatus(InvoiceStatus.FINAL);
        req.setLineItems(null);

        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));
        when(sequenceRepository.findByClientIdAndYear(anyInt(), anyInt())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> invoiceService.create(req));
    }

    @Test
    void update_SentInvoice_ShouldThrowException() {
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(100);
        existingInvoice.setStatus(InvoiceStatus.SENT);

        when(invoiceRepository.findById(100)).thenReturn(Optional.of(existingInvoice));
        doThrow(new com.freelance.freelancepm.exception.InvoiceEditNotAllowedException("Editing not allowed"))
                .when(editValidator).validateEditable(existingInvoice);

        InvoiceUpdateRequest req = new InvoiceUpdateRequest();
        req.setStatus(InvoiceStatus.DRAFT);

        assertThrows(com.freelance.freelancepm.exception.InvoiceEditNotAllowedException.class,
                () -> invoiceService.update(100, req));
    }

    @Test
    void update_DraftInvoice_ShouldUpdateAndRegeneratePdf() {
        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(100);
        existingInvoice.setStatus(InvoiceStatus.DRAFT);
        existingInvoice.setClient(mockClient);

        when(invoiceRepository.findById(100)).thenReturn(Optional.of(existingInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArguments()[0]);

        InvoiceUpdateRequest req = new InvoiceUpdateRequest();
        req.setStatus(InvoiceStatus.DRAFT);
        req.setLineItems(List.of(createLineItemRequest()));

        // Act
        InvoiceResponse response = invoiceService.update(100, req);

        // Assert
        verify(editValidator).validateEditable(existingInvoice);
        verify(calculationService).recalculateInvoice(existingInvoice);
        verify(pdfService, times(1)).generateInvoicePdf(100);
        assertNotNull(response);
    }

    private InvoiceLineItemRequest createLineItemRequest() {
        InvoiceLineItemRequest item = new InvoiceLineItemRequest();
        item.setDescription("Test Service");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setAmount(new BigDecimal("200.00"));
        return item;
    }

    @Test
    void listAll_NoFilters_ShouldReturnPageOfInvoices() {
        // Arrange
        Invoice invoice1 = new Invoice();
        invoice1.setId(1);
        invoice1.setClient(mockClient);
        invoice1.setTotal(new BigDecimal("100.00"));
        invoice1.setStatus(InvoiceStatus.DRAFT);

        Invoice invoice2 = new Invoice();
        invoice2.setId(2);
        invoice2.setClient(mockClient);
        invoice2.setTotal(new BigDecimal("200.00"));
        invoice2.setStatus(InvoiceStatus.FINAL);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Invoice> page = new PageImpl<>(List.of(invoice1, invoice2));

        when(invoiceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<InvoiceListDTO> result = invoiceService.listAll(null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().get(0).getId());
        assertEquals("John Doe", result.getContent().get(0).getClientName());
        verify(invoiceRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void listAll_WithFilters_ShouldUseSpecification() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 5);
        Page<Invoice> emptyPage = new PageImpl<>(List.of());

        when(invoiceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        // Act
        Page<InvoiceListDTO> result = invoiceService.listAll(1, startDate, endDate, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(invoiceRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void listAll_StartDateAfterEndDate_ShouldThrowException() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        IllegalArgumentException act = assertThrows(IllegalArgumentException.class,
                () -> invoiceService.listAll(1, startDate, endDate, pageable));
        assertEquals("startDate must not be after endDate", act.getMessage());
    }

    @Test
    void listAll_EmptyResult_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(invoiceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty());

        Page<InvoiceListDTO> result = invoiceService.listAll(null, null, null, pageable);

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}
