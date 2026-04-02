package com.freelance.freelancepm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Single Responsibility: This class only represents the Client entity
@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String email;

    @jakarta.persistence.Column(name = "contact_number")
    private String phone;

    @jakarta.persistence.Column(length = 255)
    private String address;

    @jakarta.persistence.Column(length = 10)
    private String code;

    // Default constructor
    public Client() {
    }

    // Parameterized constructor
    public Client(String name, String email, String phone, String code) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.code = code;
    }

    // Getters and Setters (Encapsulation)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}