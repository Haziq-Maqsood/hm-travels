package com.hmtravels.agency.dto; // Or whatever your package is

public class LoginRequest {
    private String cnic;
    private String password;

    public String getCnic() { return cnic; }
    public void setCnic(String cnic) { this.cnic = cnic; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}