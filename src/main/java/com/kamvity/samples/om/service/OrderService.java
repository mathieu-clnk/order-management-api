package com.kamvity.samples.om.service;

import com.kamvity.samples.om.response.OrderResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Order service which calls the Order Terminal REST API.
 *
 * <p>
 *     The fallback methods catch the exception raised if any.
 *     A fallback method can also catch an exception raise by another fallback method.
 *     Retry mechanism cannot be used with the Circuit breaker or Bulkhead mechanisms.
 * </p>
 */
@Slf4j
@Service
public class OrderService {

    private static final String CB_ORDER_CONFIG = "orderConfig";

    @Value("${endpoints.order}")
    private String orderEndpoint;

    @Autowired
    protected WebClient webClient;
    private RestTemplate restTemplate = new RestTemplate();


    /**
     * Get order by ID.
     * @param id the unique identifier of the order.
     * @return the order response.
     */
    @TimeLimiter(name = CB_ORDER_CONFIG, fallbackMethod = "getOrderFallback")
    @CircuitBreaker(name = CB_ORDER_CONFIG, fallbackMethod = "getOrderFallback")
    @Bulkhead(name = CB_ORDER_CONFIG)
    @RateLimiter(name = CB_ORDER_CONFIG, fallbackMethod = "getOrderFallback")
    public Mono<OrderResponse> findOrderById(Optional<String> id) {
        String url = orderEndpoint + "/get-by-id?orderId=" + id.get();

        Mono<OrderResponse> response = webClient.get().uri(url).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(OrderResponse.class);
        return response;

    }
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Mono<OrderResponse> getOrderFallback(Optional<String> id, NoSuchElementException ex) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setStatus(orderResponse.FAILED);
        orderResponse.errorMessage = "Missing parameter(s) while calling the Order API.";
        return Mono.just(orderResponse);
    }

    /**
     * Catch timeout exception that may be raised when calling the backend.
     * @param id the unique order identifier.
     * @param ex the exception raised by the initial method.
     * @return the order response with the error.
     */
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public Mono<OrderResponse> getOrderFallback(Optional<String> id, TimeoutException ex) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setStatus(orderResponse.FAILED);
        String url = orderEndpoint + "/get-by-id?orderId=" + id.get();
        orderResponse.errorMessage = String.format("The operation timed out. Exception: %s. Url %s.",ex.getMessage(),url);
        log.error(orderResponse.errorMessage);
        return Mono.just(orderResponse);
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<OrderResponse> getOrderFallback(Optional<String> id, NullPointerException ex) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setStatus(orderResponse.FAILED);
        String url = orderEndpoint + "/get-by-id?orderId=" + id.get();
        orderResponse.errorMessage = String.format("An error occurred while during the Order API call. Maybe the order does not exists: %s. Url %s.",ex.getMessage(),url);
        log.error("A NullPointerException error has occurred.");
        log.error(orderResponse.getErrorMessage());
        return Mono.just(orderResponse);
    }


    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<OrderResponse> getOrderFallback(Optional<String> id, HttpServerErrorException ex) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setStatus(orderResponse.FAILED);
        orderResponse.errorMessage = String.format("The following HTTP error occurred while during the Order API call: %s",ex.getMessage());
        log.error(orderResponse.getErrorMessage());
        return Mono.just(orderResponse);
    }

    /**
     * Catch the RequestNotPermitted exception that may be raised when reaching the rate limit.
     * @param id the unique order identifier.
     * @param ex the exception raised by the initial method.
     * @return the order response with the error.
     */
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Mono<OrderResponse> getOrderFallback(Optional<String> id, RequestNotPermitted ex) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setStatus(orderResponse.FAILED);
        String url = orderEndpoint + "/get-by-id?orderId=" + id.get();
        orderResponse.errorMessage = "Too many requests in the same time.";
        log.error(orderResponse.getErrorMessage());
        return Mono.just(orderResponse);
    }

    /**
     * Catch the WebClientRequestException that may be raised by a network connectivity issue.
     * An automatic retry is performed.
     * @param id id the unique order identifier.
     * @param ex the exception raised by the initial method.
     * @return the order response with the error.
     */
    public Mono<OrderResponse> getOrderFallback(Optional<String> id, WebClientRequestException ex) {
        log.error("Web client Request exception.");
        log.error(ex.toString());
        String message = ex.getMessage();
        log.error(message);
        return findOrderRetryById(id);
    }

    /**
     * Catch the WebClientResponseException that may be raised by a backend issue.
     * An automatic retry is performed if the backend has returned an 503.
     * @param id id the unique order identifier.
     * @param ex the exception raised by the initial method.
     * @return the order response with the error.
     */
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<OrderResponse> getOrderFallback(Optional<String> id, WebClientResponseException ex) {
        log.error("Web client Response exception.");
        log.error(ex.toString());
        String message = ex.getMessage();
        log.error(message);
        if (message.contains("Service Unavailable")) {
            log.warn("Service unavailable, try again.");
            return findOrderRetryById(id);
        }
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setStatus(orderResponse.FAILED);
        orderResponse.errorMessage = "A web client error occurred while during the Order API call. Please contact your administrator.";
        log.error(orderResponse.getErrorMessage());
        return Mono.just(orderResponse);
    }
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<OrderResponse> getOrderFallback(Optional<String> id, Exception ex) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setStatus(orderResponse.FAILED);
        orderResponse.errorMessage = "An error occurred while during the Order API call. Please contact your administrator.";
        log.error(orderResponse.getErrorMessage());
        log.error(ex.toString());
        return Mono.just(orderResponse);
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<OrderResponse> getOrderFallback(Optional<String> id) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setStatus(orderResponse.FAILED);
        orderResponse.errorMessage = "No Exception. Please contact your administrator.";
        return Mono.just(orderResponse);
    }

    /**
     * Retry to call the backend to get the order.
     * @param id the unique identifier of the order.
     * @return the order response.
     */
    @Retry(name = CB_ORDER_CONFIG, fallbackMethod = "getOrderRetryFallback")
    @Bulkhead(name = CB_ORDER_CONFIG)
    public Mono<OrderResponse> findOrderRetryById(Optional<String> id) {
        String url = orderEndpoint + "/get-by-id?orderId=" + id.get();
        Mono<OrderResponse> response = webClient.get().uri(url).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(OrderResponse.class);
        return response;

    }

    /**
     * The fallback method when the retry method has raised an exception.
     * @param id id the unique order identifier.
     * @param ex the exception raised by the initial method.
     * @return the order response with the error.
     */
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<OrderResponse> getOrderRetryFallback(Optional<String> id, Exception ex) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setStatus(orderResponse.FAILED);
        orderResponse.errorMessage = "An error occurred while during the Order API call after several retries. Please contact your administrator.";
        log.error(orderResponse.getErrorMessage());
        log.error(ex.toString());
        return Mono.just(orderResponse);
    }
}
