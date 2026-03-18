package com.freelance.freelancepm.service;

import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Single Responsibility: Handles business logic for Clients
@Service
public class ClientService {

    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // Create or update a client
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    // Retrieve all clients
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    // Retrieve client by ID
    public Optional<Client> getClientById(Integer id) {
        return clientRepository.findById(id);
    }

    // Delete client by ID
    public void deleteClientById(Integer id) {
        clientRepository.deleteById(id);
    }
}