package com.kamvity.samples.om.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTest {

    public Order createOrder() {
        Order order = new Order("1");
        return order;
    }

    @Test
    public void testGetOrderId() {
        Order order = createOrder();
        assertEquals("1",order.getOrderId());
    }

    @Test
    public void testEquals() {
        Order order = createOrder();
        Order order2 = new Order("1");
        assert order.equals(order2);
        assert order.canEqual(order2);
    }

    @Test
    public void testSetOrderId() {
        Order order = createOrder();
        order.setOrderId("2");
        assertEquals("2",order.getOrderId());
    }

    @Test
    public void testSerial() {
        Order order = createOrder();
        Order order2 = new Order("1");
        assertEquals(order2.hashCode(),order.hashCode());
        assertEquals(order2.toString(),order.toString());
    }

    @Test
    public void testBuilder() {
        Order order = createOrder();
        Order order2 = Order.builder().orderId("1").build();
        assertEquals("1",order2.getOrderId());
        Order.OrderBuilder builder = new Order.OrderBuilder().orderId("1");
        Order order3 = builder.build();
        assertEquals(order2,order3);
        assertEquals("1",order3.getOrderId());
    }


}
