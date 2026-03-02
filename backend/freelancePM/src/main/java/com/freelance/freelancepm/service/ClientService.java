package com.freelance.freelancepm.service;

import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    // ADD client
    public void saveClient(Client client) {
        clientRepository.save(client);
    }

    // SHOW all clients
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    // DELETE client by ID
    public void deleteClientById(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client not found");
        }
        clientRepository.deleteById(id);
    }
}