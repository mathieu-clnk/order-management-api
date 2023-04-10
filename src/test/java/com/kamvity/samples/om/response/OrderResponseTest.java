package com.kamvity.samples.om.response;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderResponseTest {

    public OrderResponse generateOrderResponse(Timestamp timestamp) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId("1");
        Map<String,String> customer = new HashMap<>();
        customer.put("name","hello");
        orderResponse.setCustomer(customer);
        orderResponse.setSensitiveMessage("Password");
        orderResponse.setStatus("Success");
        orderResponse.setOrderTimestamp(timestamp);
        orderResponse.setPrice(200.00);
        orderResponse.setErrorMessage("Error");
        return orderResponse;
    }

    @Test
    public void testGets() {
        Timestamp timestamp = Timestamp.from(Instant.now());
        OrderResponse orderResponse = generateOrderResponse(timestamp);
        assertEquals("1",orderResponse.getOrderId());
        assertEquals("hello",orderResponse.getCustomer().get("name"));
        assertEquals("Password",orderResponse.getSensitiveMessage());
        assertEquals("Success",orderResponse.getStatus());
        assertEquals(timestamp,orderResponse.getOrderTimestamp());
        assertEquals(200.00,orderResponse.getPrice());
        assertEquals("Error",orderResponse.getErrorMessage());
    }
    @Test
    public void testSets() {
        Timestamp timestamp = Timestamp.from(Instant.now());
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId("1");
        Map<String,String> customer = new HashMap<>();
        customer.put("name","hello");
        orderResponse.setCustomer(customer);
        orderResponse.setSensitiveMessage("Password");
        orderResponse.setStatus("Success");
        orderResponse.setOrderTimestamp(timestamp);
        orderResponse.setPrice(200.00);
        orderResponse.setErrorMessage("Error");
        assertEquals("1",orderResponse.getOrderId());
        assertEquals("hello",orderResponse.getCustomer().get("name"));
        assertEquals("Password",orderResponse.getSensitiveMessage());
        assertEquals("Success",orderResponse.getStatus());
        assertEquals(timestamp,orderResponse.getOrderTimestamp());
        assertEquals(200.00,orderResponse.getPrice());
        assertEquals("Error",orderResponse.getErrorMessage());
    }


}
