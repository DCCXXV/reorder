package com.thecritics.reorder.karate;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndpointsIntegrationTest {

    @Karate.Test
    public Karate testCreateOrder() {
        return Karate.run("classpath:features/createOrderIntegrationTest.feature")
                .systemProperty("karate.env", System.getProperty("karate.env", "dev"));
    }

    @Karate.Test
    public Karate testPublishOrder() {
        return Karate.run("classpath:features/publishOrderIntegrationTest.feature")
                .systemProperty("karate.env", System.getProperty("karate.env", "dev"));
    }

    @Karate.Test
    public Karate testSearchOrder() {
        return Karate.run("classpath:features/searchOrderIntegrationTest.feature")
                .systemProperty("karate.env", System.getProperty("karate.env", "dev"));
    }
    
    @Karate.Test
    public Karate testDetailsOrder() {
        return Karate.run("classpath:features/detailsOrderIntegrationTest.feature")
                .systemProperty("karate.env", System.getProperty("karate.env", "dev"));
    }
}