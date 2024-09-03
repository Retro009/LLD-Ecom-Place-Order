package com.example.ecom.services;

import com.example.ecom.exceptions.*;
import com.example.ecom.models.*;
import com.example.ecom.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class OrderServiceImpl implements OrderService{
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
    @Transactional(rollbackFor = Exception.class)
    public Order placeOrder(int userId, int addressId, List<Pair<Integer, Integer>> orderDetails) throws UserNotFoundException, InvalidAddressException, OutOfStockException, InvalidProductException, HighDemandProductException {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User Not Found!!"));
        Address address = addressRepository.findById(addressId).orElseThrow(()-> new InvalidAddressException("Address Not Found"));
        if (!user.getAddresses().contains(address)) {
            throw new InvalidAddressException("Address does not belong to the user");
        }
        List<Inventory> inventories = inventoryRepository.findByProductIdIn(orderDetails.stream().map(orderDetail -> orderDetail.getFirst()).collect(Collectors.toList()));
        List<HighDemandProduct> highDemandProductList = highDemandProductRepository.findByProductIdIn(orderDetails.stream().map(orderDetail -> orderDetail.getFirst()).collect(Collectors.toList()));
        Order order = new Order();
        List<OrderDetail> orderDetailList = new ArrayList<>();
        order.setUser(user);
        order.setDeliveryAddress(address);
        order=orderRepository.save(order);
        for(Pair<Integer, Integer>  pair:orderDetails){
            int productId = pair.getFirst();
            int orderedQuantity = pair.getSecond();
            Inventory in_inventory = inventories.stream().filter(inventory -> inventory.getProduct().getId() == productId).findFirst().orElseThrow(() -> new InvalidProductException("Product not Available in Inventory"));
            if(in_inventory.getQuantity() < orderedQuantity )
                throw new OutOfStockException("Product Out Of Stock");
            if(highDemandProductList.stream().filter(hdp -> hdp.getProduct().getId() == productId && orderedQuantity > hdp.getMaxQuantity()).findFirst().isPresent())
                throw new OutOfStockException("Cant order more than Specified Limit");
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(in_inventory.getProduct());
            orderDetail.setQuantity(orderedQuantity);
            orderDetailList.add(orderDetailRepository.save(orderDetail));
        }
        order.setOrderDetails(orderDetailList);
        order.setOrderStatus(OrderStatus.PLACED);

        return orderRepository.save(order);
    }
}
