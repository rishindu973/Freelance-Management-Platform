package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.service.ClientService;
import com.freelance.freelancepm.service.IManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.security.Principal;

// Open/Closed Principle: Controller handles HTTP requests, can be extended without modifying existing code
@RestController
@RequestMapping("/api/clients")

public class ClientController {

    private final ClientService clientService;
    private final IManagerService managerService;

    @Autowired
    public ClientController(ClientService clientService, IManagerService managerService) {
        this.clientService = clientService;
        this.managerService = managerService;
    }

    private Integer requireManagerId(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return managerService.getManagerIdByEmail(principal.getName());
    }

    @PostMapping
    public ResponseEntity<Client> addClient(@RequestBody Client client, Principal principal) {
        Integer managerId = requireManagerId(principal);
        client.setManagerId(managerId);
        Client savedClient = clientService.saveClient(client);
        return ResponseEntity.ok(savedClient);
    }

    @GetMapping
    public ResponseEntity<Page<Client>> getAllClients(
            Principal principal,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Integer managerId = requireManagerId(principal);
        Pageable pageable = PageRequest.of(page, size);
        Page<Client> clients = clientService.getAllClients(managerId, pageable);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClient(@PathVariable("id") Integer id, Principal principal) {
        Integer managerId = requireManagerId(principal);
        Optional<Client> client = clientService.getClientById(id, managerId);
        return client.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable("id") Integer id, @RequestBody Client clientDetails, Principal principal) {
        Integer managerId = requireManagerId(principal);
        Optional<Client> clientOpt = clientService.getClientById(id, managerId);
        if (clientOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Client client = clientOpt.get();
        client.setName(clientDetails.getName());
        client.setEmail(clientDetails.getEmail());
        client.setPhone(clientDetails.getPhone());
        client.setAddress(clientDetails.getAddress());

        return ResponseEntity.ok(clientService.saveClient(client));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClient(@PathVariable("id") Integer id, Principal principal) {
        Integer managerId = requireManagerId(principal);
        Optional<Client> clientOptional = clientService.getClientById(id, managerId);
        if (clientOptional.isPresent()) {
            clientService.deleteClientById(id, managerId);
            return ResponseEntity.ok("Client deleted successfully");
        } else {
            return ResponseEntity.status(404).body("Client not found");
        }
    }
}