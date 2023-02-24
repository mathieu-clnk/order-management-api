package com.kamvity.samples.om;

import com.kamvity.samples.om.config.OpenApiConfigTest;
import com.kamvity.samples.om.config.OrderManagementConfig;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { OrderManagementConfig.class, OpenApiConfigTest.class })
public class GenerateSwaggerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int randomServerPort;

    @Test
    void contextLoads() throws IOException {
        String swagger = this.restTemplate.getForObject("http://localhost:"+randomServerPort+"/v3/api-docs", String.class);

        FileUtils.writeStringToFile(new File("swagger.json"), swagger, Charset.forName("UTF-8"));
    }
}
