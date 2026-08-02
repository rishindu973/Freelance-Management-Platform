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
@RequestMapping("/clients")
@CrossOrigin(origins = "http://localhost:5173")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // Add new client
    @PostMapping("/add")
    public ResponseEntity<Client> addClient(@RequestBody Client client) {
        Client savedClient = clientService.saveClient(client);
        return ResponseEntity.ok(savedClient);
    }

    // Get all clients
    @GetMapping("/all")
    public ResponseEntity<List<Client>> getAllClients() {
        List<Client> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    // Delete client by ID
    @DeleteMapping("/delete/{id}")
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