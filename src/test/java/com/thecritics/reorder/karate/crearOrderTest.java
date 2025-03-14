package com.thecritics.reorder.karate;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class crearOrderTest {
    
    @Karate.Test
    public Karate testCreateOrder() {
        return Karate.run("classpath:features/createOrder.feature")
                    .systemProperty("karate.env", System.getProperty("karate.env", "dev"));
    }
}