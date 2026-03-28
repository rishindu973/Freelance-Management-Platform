package com.freelance.freelancepm.email;

public class Response {
    private int status;

    public Response(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}