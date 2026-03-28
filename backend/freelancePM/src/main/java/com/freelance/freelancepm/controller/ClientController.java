package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// Open/Closed Principle: Controller handles HTTP requests, can be extended without modifying existing code
@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<Client> addClient(@RequestBody Client client) {
        Client savedClient = clientService.saveClient(client);
        return ResponseEntity.ok(savedClient);
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        List<Client> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClient(@PathVariable Integer id) {
        Optional<Client> client = clientService.getClientById(id);
        return client.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Integer id, @RequestBody Client clientDetails) {
        Optional<Client> clientOpt = clientService.getClientById(id);
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
    public ResponseEntity<String> deleteClient(@PathVariable Integer id) {
        Optional<Client> clientOptional = clientService.getClientById(id);
        if (clientOptional.isPresent()) {
            clientService.deleteClientById(id);
            return ResponseEntity.ok("Client deleted successfully");
        } else {
            return ResponseEntity.status(404).body("Client not found");
        }
    }
}