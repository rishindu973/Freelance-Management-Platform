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
}