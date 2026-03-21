package com.freelance.freelancepm.service;

import com.freelance.freelancepm.model.Client;
import java.util.List;
import java.util.Optional;

public interface IClientService {
    Client saveClient(Client client);

    List<Client> getAllClients();

    Optional<Client> getClientById(Integer id);

    void deleteClientById(Integer id);
}
