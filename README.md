![Coverage](.github/badges/jacoco.svg)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/2ef04f7899824df5a9469cda3f566aa0)](https://www.codacy.com/gh/mathieu-clnk/order-management-api/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mathieu-clnk/order-management-api&amp;utm_campaign=Badge_Grade)

# Order Management API

_DISCLAIMER_: THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE

## Overview

This application is Spring Boot 3 Circuit Breaker. 
The Circuit Breaker design pattern is used to handle the failure of the downstream components.
As organizations opts more and more for Microservices, it becomes vital to implement such pattern to avoid cascading failures.
This sample calls the [Order-Terminal-API](https://github.com/mathieu-clnk/order-terminal-api) to retrieve an order.
It provides few examples of how to handle this downstream application failure and return a JSON response to the caller.

## Testing
The class `OrderServiceTest` can be tested either by using the embedded Mockserver or with a forked one.

If you want to fork the server to avoid the server initialization when the test starts you shall do the following:
```java
// Comment the method below
@BeforeAll
public static void startServer() {
        mockServer = startClientAndServer(8090);
}
```

Then run the following commands
```bash
mvn mockserver:runForked
# Once you are done with your tests.
mvn mockserver:stopForked
```

## Note
Please note that the JDK 11 is not supported. 
This application is intentionally not using Oracle OpenJDK or any `javax` library.
JUnit4 shall not be added in the POM or you may encounter issue with the test as classes will enter in conflict.

If you want to use swagger you need to add the following to the POM
```xml
<dependencies>
    <dependency>
                <groupId>org.hibernate.validator</groupId>
                <artifactId>hibernate-validator</artifactId>
                <version>8.0.0.Final</version>
            </dependency>
    <!-- Needed by hibernate validator -->
    <!-- https://mvnrepository.com/artifact/org.glassfish/jakarta.el -->
    <dependency>
        <groupId>org.glassfish</groupId>
        <artifactId>jakarta.el</artifactId>
        <version>5.0.0-M1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```
