package com.example.ecom.controllers;

import com.example.ecom.dtos.PlaceOrderRequestDto;
import com.example.ecom.dtos.PlaceOrderResponseDto;
import com.example.ecom.dtos.ResponseStatus;
import com.example.ecom.exceptions.*;
import com.example.ecom.models.Order;
import com.example.ecom.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    public PlaceOrderResponseDto placeOrder(PlaceOrderRequestDto placeOrderRequestDto) {
        PlaceOrderResponseDto responseDto = new PlaceOrderResponseDto();
        try {
            responseDto.setOrder(orderService.placeOrder(placeOrderRequestDto.getUserId(), placeOrderRequestDto.getAddressId(), placeOrderRequestDto.getOrderDetails()));
            responseDto.setStatus(ResponseStatus.SUCCESS);
        } catch (UserNotFoundException | InvalidAddressException | OutOfStockException | InvalidProductException | HighDemandProductException e) {
            responseDto.setStatus(ResponseStatus.FAILURE);
        }
        return responseDto;
    }

}
