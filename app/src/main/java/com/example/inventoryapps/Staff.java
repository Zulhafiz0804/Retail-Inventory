package com.example.inventoryapps;

public class Staff {
    private String email;
    private String username;
    private String password;
    private String role;

    // Required empty constructor (for Firebase)
    public Staff() {}

    public Staff(String email, String username, String password, String role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}

