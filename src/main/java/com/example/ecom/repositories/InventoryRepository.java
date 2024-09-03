package com.example.ecom.repositories;


import com.example.ecom.models.Inventory;
import com.example.ecom.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    List<Inventory> findByProductIdIn(List<Integer> productIds);

}
