package com.thecritics.reorder.config;

import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrderRepository;
import com.thecritics.reorder.repository.OrdererRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    @Autowired
    private OrdererRepository ordererRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Executing DEV profile data initialization via CommandLineRunner...");

        if (ordererRepository.count() == 0 && orderRepository.count() == 0) {
            Orderer devOrderer = new Orderer();
            devOrderer.setUsername("a");
            devOrderer.setEmail("a@a");
            devOrderer.setCreatedAt(null);
            devOrderer.setOrders(new ArrayList<>());
            devOrderer.setPassword(passwordEncoder.encode("aaaAAA123"));
            ordererRepository.save(devOrderer);
            log.info("Saved Orderer: {}", devOrderer.getUsername());

            Order originalOrder = new Order();
            originalOrder.setTitle("top frutas");
            originalOrder.setAuthor(devOrderer);
            List<List<String>> content1 = Arrays.asList(
                Arrays.asList("pera", "manzana"),
                Arrays.asList("plátano")
            );
            originalOrder.setPreviewElements(Arrays.asList("pera", "manzana", "plátano"));
            originalOrder.setContent(content1);
            Order savedOriginalOrder = orderRepository.save(originalOrder);

            Order reorder = new Order();
            reorder.setTitle("top frutas (version mala)");
            reorder.setAuthor(devOrderer);
            List<List<String>> content2 = Arrays.asList(
                Arrays.asList("plátanao"),
                Arrays.asList("pera", "manzana")
            );
            reorder.setContent(content2);
            reorder.setPreviewElements(Arrays.asList("plátano", "pera", "manzana"));
            reorder.setReorderedOrder(savedOriginalOrder);
            orderRepository.save(reorder);

            log.info("DEV profile data initialization complete.");
        } else {
            log.info("Dev data already exists, skipping seeding.");
        }
    }
}
