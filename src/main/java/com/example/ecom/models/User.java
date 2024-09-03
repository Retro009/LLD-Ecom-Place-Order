package com.example.ecom.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User extends BaseModel{
    private String name;
    private String email;
    @OneToMany
    private List<Address> addresses;
    @OneToMany
    private List<Order> orders;
}
