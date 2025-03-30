package com.thecritics.reorder.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.thecritics.reorder.TestcontainersConfiguration;
import com.thecritics.reorder.model.Orderer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)

public class OrdererRepositoryTest {
    
    @Autowired
    private OrdererRepository ordererRepository;

    @Test
    void testSaveandRetrieveOrderer() {
        Orderer inputOrderer = new Orderer();
        inputOrderer.setEmail("fulanito@gmail.com");
        inputOrderer.setUsername("fulanit0");
        inputOrderer.setPassword("Fulanit0Tal");

        Orderer savedOrderer = ordererRepository.save(inputOrderer);
        Orderer retrievedOrderer = ordererRepository.findById(savedOrderer.getId()).orElse(null);

        assertThat(retrievedOrderer).isNotNull();
        assertThat(retrievedOrderer.getEmail()).isEqualTo(inputOrderer.getEmail());
        assertThat(retrievedOrderer.getUsername()).isEqualTo(inputOrderer.getUsername());
        assertThat(retrievedOrderer.getPassword()).isEqualTo(inputOrderer.getPassword());
    }

}
