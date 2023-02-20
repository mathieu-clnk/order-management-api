package com.kamvity.samples.om.controller;

import com.kamvity.samples.om.service.OrderService;
import com.kamvity.samples.om.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/find_order_id")
    public Mono<OrderResponse> getOrderById (@RequestParam Optional<String> id) {
        //Order order = new Order("ds");
        //return order;

        /*
        Mono<OrdersResponse> orderResponse = orderService.findOrderById(id);
        Map<String,String> mapResult = new HashMap<>();
        orderResponse.subscribe(data -> {
            if ("success".equals(data.getStatus())) {
                mapResult.put("status","success");
            }else{
                mapResult.put("status","failed");
            }
        });

        HttpStatus httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        if("success".equals(mapResult.get("status"))) {
            httpStatus = HttpStatus.OK;
        }
        EntityResponse<Mono<OrdersResponse>> entityResponse = ResponseEntity.status(httpStatus).body(orderResponse,OrdersResponse.class);
        return entityResponse;

         */
        return orderService.findOrderById(id);
    }
}
