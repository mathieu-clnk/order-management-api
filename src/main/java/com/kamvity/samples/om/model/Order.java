package com.kamvity.samples.om.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
public class Order {

    public Order(String orderId) {
        this.orderId = orderId;
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "Order ID")
    private String orderId;

    /*
    private Double price;

    private Timestamp orderTimestamp;

    private List<Terminal> terminals;

    private Order.Customer customer;

    public class Terminal {
        private String terminalId;
        private String manufacturer;
        private String model;
        private String version;
        private String serialNumber;
        private String orderTerminal;
    }

    public class Customer {
        private String customerId;
        private String title;
        private String firstname;
        private String lastname;
        private String address;
        private String zipCode;
    }
    */

}
