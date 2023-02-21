package com.kamvity.samples.om.response;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderResponseTest {

    public OrderResponse generateOrderResponse() {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId("1");
        Map<String,String> customer = new HashMap<>();
        customer.put("name","hello");
        orderResponse.setCustomer(customer);
        return orderResponse;
    }

    @Test
    public void testGetId() {
        assertEquals("1",generateOrderResponse().getOrderId());
    }
}
