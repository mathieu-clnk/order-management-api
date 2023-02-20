package com.kamvity.samples.om.response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Map;
@Getter
@Setter
public class OrderResponse extends Response{

    private String orderId;
    private Double price;
    private Timestamp orderTimestamp;
    private Map<String,String> customer;
}
