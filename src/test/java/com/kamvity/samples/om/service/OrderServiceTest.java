
package com.kamvity.samples.om.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kamvity.samples.om.config.OrderManagementConfig;
import com.kamvity.samples.om.response.OrderResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpError.error;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = OrderManagementConfig.class)
@ActiveProfiles("dev")
public class OrderServiceTest {

    @Autowired
    OrderService orderService;
    private static ClientAndServer mockServer;


    @BeforeAll
    public static void startServer() {
        mockServer = startClientAndServer(8090);
    }


    @BeforeEach
    public void reset() throws InterruptedException {
        mockServer.reset();
        //Make sure the Time limiter is passed
        Thread.sleep(2000);
        int i = 0;
        while ( ! mockServer.isRunning() && i < 30) {
            Thread.sleep(1000);
            i++;
        }
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }





    String order1 = "{\n" +
            "    \"orderId\": 1,\n" +
            "    \"price\": 200.0,\n" +
            "    \"orderTimestamp\": \"2023-01-09T14:12:13.931+00:00\",\n" +
            "    \"customer\": {\n" +
            "        \"customerId\": 1,\n" +
            "        \"title\": \"Majesty\",\n" +
            "        \"firstname\": \"Soma\",\n" +
            "        \"lastname\": \"Leavyi\",\n" +
            "        \"email\": \"soma.leavyi@email.org\",\n" +
            "        \"address\": \"1 street of Majesty, Cambodia\",\n" +
            "        \"zipCode\": \"2222\"\n" +
            "    }\n" +
            "}";

    String order2 = "{ \"order\" : \"soma\" }";

    public void mockOrderGetByIdOK() {
        Header header = Header.header("Content-Type","application/json");
        new MockServerClient("127.0.0.1",8090)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/order-terminal/get-by-id")
                )
                .respond(
                    response()
                        .withStatusCode(200)
                            .withHeader(header)
                        .withBody(order1)
                );
    }

    public void mockOrderGetByIdRetry503OK(int retry) {
        Header header = Header.header("Content-Type","application/json");
        MockServerClient mockServerClient = new MockServerClient("127.0.0.1",8090);
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/order-terminal/get-by-id"),
                        Times.exactly(retry)
                )
                .respond(
                        response()
                                .withStatusCode(503)
                );
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/order-terminal/get-by-id"),
                        Times.exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(header)
                                .withBody(order1)

                );
    }

    public void mockOrderGetByIdRetryDropOK(int retry) {
        Header header = Header.header("Content-Type","application/json");
        MockServerClient mockServerClient = new MockServerClient("127.0.0.1",8090);
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/order-terminal/get-by-id"),
                        Times.exactly(retry)
                )
                .error(
                        error().withDropConnection(true)
                );
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/order-terminal/get-by-id"),
                        Times.exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(header)
                                .withBody(order1)

                );
    }

    @Test
    public void testFindByIdOk() {
        mockOrderGetByIdOK();
        Mono<OrderResponse> mreor = orderService.findOrderById(Optional.of("1"));
        OrderResponse orderResponse = mreor.block();
        assertEquals("1",orderResponse.getOrderId());
    }

    @Test
    public void testFindByIdTimeOut() {
        new MockServerClient("127.0.0.1",8090)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/order-terminal/get-by-id")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(order1)
                                .withDelay(TimeUnit.MINUTES,2)
                );
        Mono<OrderResponse> mreor = orderService.findOrderById(Optional.of("1"));
        assert mreor.block() != null;
        assert mreor.block().getStatus() == OrderResponse.FAILED;
        assert mreor.block().getErrorMessage().contains("The operation timed out.");
    }

    @Test
    public void testFindRetryById() {
       String orderResponse2 = "{ }";
        Header header = Header.header("Content-Type","application/json");
        MockServerClient mockServerClient = new MockServerClient("127.0.0.1",8090);
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/order-terminal/get-by-id"),
                       Times.exactly(1)
                )
               .respond(
                       response()
                               .withBody(orderResponse2)

               );
            mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v1/order-terminal/get-by-id"),
                        Times.exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(header)
                                .withBody(order1)

                );

        Mono<OrderResponse> mreor = orderService.findOrderRetryById(Optional.of("1"));
        OrderResponse orderResponse = mreor.block();
        assertEquals("1",orderResponse.getOrderId());
    }
    @Test
    public void testFindByIdRetry() {
        mockOrderGetByIdRetry503OK(2);
        Mono<OrderResponse> mreor = orderService.findOrderRetryById(Optional.of("1"));
        OrderResponse orderResponse = mreor.block();
        assertEquals("1",orderResponse.getOrderId());
    }
    @Test
    public void testTooManyRetryFindById() {
        mockOrderGetByIdRetry503OK(10);
        Mono<OrderResponse> mreor = orderService.findOrderRetryById(Optional.of("1"));
        OrderResponse orderResponse = mreor.block();
        String expectedError = "An error occurred while during the Order API call after several retries. Please contact your administrator.";
        assertEquals(expectedError,orderResponse.getErrorMessage());
    }

    @Test
    public void testFindByIdRetryDrop() {
        mockOrderGetByIdRetryDropOK(2);
        Mono<OrderResponse> mreor = orderService.findOrderRetryById(Optional.of("1"));
        OrderResponse orderResponse = mreor.block();
        assertEquals("1",orderResponse.getOrderId());
    }

    @Test
    public void testTooManyRetryDropFindById() {
        mockOrderGetByIdRetryDropOK(10);
        Mono<OrderResponse> mreor = orderService.findOrderRetryById(Optional.of("1"));
        OrderResponse orderResponse = mreor.block();
        String expectedError = "An error occurred while during the Order API call after several retries. Please contact your administrator.";
        assertEquals(expectedError,orderResponse.getErrorMessage());
    }

    @Test
    public void testFindByIdRateLimiter() {
        mockOrderGetByIdOK();
        for (int i = 0; i < 15 ; i++) {
            Mono<OrderResponse> mreor = orderService.findOrderById(Optional.of("1"));
            OrderResponse orderResponse = mreor.block();
            String errorMessage = "Too many requests in the same time.";
            if ( i < 11) {
                assertEquals("1",orderResponse.getOrderId());
            }else {
                assertEquals(errorMessage, orderResponse.getErrorMessage());
            }
        }

    }
}
