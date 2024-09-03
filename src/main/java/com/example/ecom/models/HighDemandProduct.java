package com.example.ecom.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class HighDemandProduct extends BaseModel{

    @OneToOne
    private Product product;
    private int maxQuantity;
}
