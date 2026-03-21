package com.freelance.freelancepm.email;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String taskTemplate(String name, String task) {
        return "<h2>Hello " + name + "</h2><p>Task: " + task + "</p>";
    }

    public String projectTemplate(String name, String project) {
        return "<h2>Hello " + name + "</h2><p>Project: " + project + "</p>";
    }

    public String welcomeTemplate(String name, String password) {
        return "<h2>Welcome to FreelanceFlow!</h2>" +
                "<p>Your temporary password is: <strong>" + password + "</strong></p>" +
                "<p>Please log in and change it immediately.</p>";
    }
}