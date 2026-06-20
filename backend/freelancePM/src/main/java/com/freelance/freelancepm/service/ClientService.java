package com.freelance.freelancepm.service;

import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

// Single Responsibility: Handles business logic for Clients
@Service
public class ClientService implements IClientService {

    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // Create or update a client
    @Override
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    // Retrieve all clients
    @Override
    public Page<Client> getAllClients(Integer managerId, Pageable pageable) {
        return clientRepository.findAllByManagerId(managerId, pageable);
    }

    // Retrieve client by ID
    @Override
    public Optional<Client> getClientById(Integer id, Integer managerId) {
        return clientRepository.findByIdAndManagerId(id, managerId);
    }

    // Delete client by ID
    @Override
    public void deleteClientById(Integer id, Integer managerId) {
        Optional<Client> clientOpt = clientRepository.findByIdAndManagerId(id, managerId);
        clientOpt.ifPresent(client -> clientRepository.deleteById(client.getId()));
    }
}