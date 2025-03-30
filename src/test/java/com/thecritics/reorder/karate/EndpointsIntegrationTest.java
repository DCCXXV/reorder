package com.thecritics.reorder.karate;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndpointsIntegrationTest {

    @LocalServerPort
    private int port;

    @Karate.Test
    public Karate testCreateOrder() {
        System.setProperty("karate.server.port", String.valueOf(port));
        return Karate.run("classpath:features/createOrderIntegrationTest.feature")
                .systemProperty("karate.env", System.getProperty("karate.env", "dev"))
                .systemProperty("karate.server.port", String.valueOf(port));
    }

    @Karate.Test
    public Karate testPublishOrder() {
        System.setProperty("karate.server.port", String.valueOf(port));
        return Karate.run("classpath:features/publishOrderIntegrationTest.feature")
                .systemProperty("karate.env", System.getProperty("karate.env", "dev"))
                .systemProperty("karate.server.port", String.valueOf(port));
    }

    @Karate.Test
    public Karate testSearchOrder() {
        System.setProperty("karate.server.port", String.valueOf(port));
        return Karate.run("classpath:features/searchOrderIntegrationTest.feature")
                .systemProperty("karate.env", System.getProperty("karate.env", "dev"))
                .systemProperty("karate.server.port", String.valueOf(port));
    }
    
    @Karate.Test
    public Karate testDetailsOrder() {
        System.setProperty("karate.server.port", String.valueOf(port));
        return Karate.run("classpath:features/detailsOrderIntegrationTest.feature")
                .systemProperty("karate.env", System.getProperty("karate.env", "dev"))
                .systemProperty("karate.server.port", String.valueOf(port));
    }

    @Karate.Test
    public Karate testdoReorder() {
        System.setProperty("karate.server.port", String.valueOf(port));
        return Karate.run("classpath:features/doReorderIntegrationTest.feature")
                .systemProperty("karate.env", System.getProperty("karate.env", "dev"))
                .systemProperty("karate.server.port", String.valueOf(port));
    }
}