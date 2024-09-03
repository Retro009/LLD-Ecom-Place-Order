package com.example.ecom.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Order extends BaseModel{
    @ManyToOne
    private User user;
    @ManyToOne
    private Address deliveryAddress;
    @OneToMany
    private List<OrderDetail> orderDetails;
    @Enumerated(EnumType.ORDINAL)
    private OrderStatus orderStatus;
}
