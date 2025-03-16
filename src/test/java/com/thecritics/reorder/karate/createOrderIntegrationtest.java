package com.thecritics.reorder.karate;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class createOrderIntegrationtest {
    
    @Karate.Test
    public Karate testCreateOrderFlujoNormal() {
        return Karate.run("classpath:features/createOrderIntegrationTest.feature")
                    .systemProperty("karate.env", System.getProperty("karate.env", "dev"));
    }
}