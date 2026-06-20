package com.freelance.freelancepm.service;

import com.freelance.freelancepm.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface IClientService {
    Client saveClient(Client client);

    Page<Client> getAllClients(Integer managerId, Pageable pageable);

    Optional<Client> getClientById(Integer id, Integer managerId);

    void deleteClientById(Integer id, Integer managerId);
}
