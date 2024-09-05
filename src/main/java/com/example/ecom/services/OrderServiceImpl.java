package com.example.ecom.services;

import com.example.ecom.exceptions.*;
import com.example.ecom.models.*;
import com.example.ecom.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private HighDemandProductRepository highDemandProductRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Order placeOrder(int userId, int addressId, List<Pair<Integer, Integer>> orderDetails) throws UserNotFoundException, InvalidAddressException, OutOfStockException, InvalidProductException, HighDemandProductException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User Not Found!!"));
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new InvalidAddressException("Address Not Found"));
        if (!address.getUser().equals(user))
            throw new InvalidAddressException("Address doesn't belong to the user");

        Map<Integer,Integer> orderDetailMap = new HashMap<>();
        for(Pair<Integer, Integer> pair:orderDetails){
            int productId = pair.getFirst();
            int orderedQuantity = pair.getSecond();
            orderDetailMap.put(productId, orderedQuantity);
        }
        List<Inventory> inventories = inventoryRepository.findAllByProductIdIn(orderDetailMap.keySet().stream().toList());
        Map<Integer,Integer> inventoryMap = new HashMap<>();
        for(Inventory inventory:inventories){
            int productId = inventory.getProduct().getId();
            int availableQuantity = inventory.getQuantity();
            inventoryMap.put(productId,availableQuantity);
        }


        for(Pair<Integer, Integer> pair:orderDetails){
            int productId = pair.getFirst();
            int orderedQuantity = pair.getSecond();
            if(!inventoryMap.containsKey(productId))
                throw new InvalidProductException("Product not available in inventory");
            if(inventoryMap.get(productId)<orderedQuantity)
                throw new OutOfStockException("Product is Out Of Stock");
        }

        List<HighDemandProduct> highDemandProductList = highDemandProductRepository.findAllByProductIdIn(orderDetailMap.keySet().stream().toList());
        if(highDemandProductList.size()>0){
            for(HighDemandProduct hpd:highDemandProductList){
                if(hpd.getMaxQuantity()<orderDetailMap.get(hpd.getProduct().getId()))
                    throw new OutOfStockException("Cant Order More than given Limit");
            }
        }

        for(Inventory inventory:inventories){
            inventory.setQuantity(inventory.getQuantity()-orderDetailMap.get(inventory.getProduct().getId()));
        }
        inventoryRepository.saveAll(inventories);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(Inventory inventory:inventories){
            Product product = inventory.getProduct();
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProduct(product);
            orderDetail.setQuantity(orderDetailMap.get(product.getId()));
            orderDetailList.add(orderDetail);
        }
        Order order = new Order();
        order.setUser(user);
        order.setOrderDetails(orderDetailList);
        order.setDeliveryAddress(address);
        order.setOrderStatus(OrderStatus.PLACED);

        order = orderRepository.save(order);

        for(OrderDetail orderDetail:orderDetailList)
            orderDetail.setOrder(order);
        orderDetailRepository.saveAll(orderDetailList);

        return order;
    }
}
