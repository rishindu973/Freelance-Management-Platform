package com.freelance.freelancepm.controller;

import com.freelance.freelancepm.model.Client;
import com.freelance.freelancepm.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;

    //Show Add Client Form
    @GetMapping("/addClientForm")
    public String showAddClientForm(Model model) {
        model.addAttribute("client", new Client());
        return "addClients";
    }

    //Save Clientiu  '
    // - ADD
    @PostMapping("/addClient")
    public String addClient(@ModelAttribute("client") Client client) {
        clientService.saveClient(client);
        return "redirect:/clientList";
    }

    //Show All Clients
    @GetMapping("/clientList")
    public String showAllClients(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
        return "clientList";
    }

    //Delete Client
    @GetMapping("/deleteClient/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientService.deleteClientById(id);
        return "redirect:/clientList";
    }
}