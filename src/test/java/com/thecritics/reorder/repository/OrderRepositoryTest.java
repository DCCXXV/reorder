package com.thecritics.reorder.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.thecritics.reorder.TestcontainersConfiguration;
import com.thecritics.reorder.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testSaveAndRetrieveOrder() {
        Order inputOrder = new Order();
        inputOrder.setAuthor("John Doe");
        inputOrder.setTitle("Ranking de frutas");

        List<List<String>> content = new ArrayList<>();
        content.add(new ArrayList<>());
        content.add(new ArrayList<>());
        content.add(new ArrayList<>());

        content.get(1).add("Manzana");
        content.get(1).add("Pera");
        content.get(2).add("Plátano");

        inputOrder.setContent(content);

        Order savedOrder = orderRepository.save(inputOrder);
        Order retrievedOrder = orderRepository.findById(savedOrder.getId()).orElse(null);

        assertThat(retrievedOrder).isNotNull();
        assertThat(retrievedOrder.getAuthor()).isEqualTo(inputOrder.getAuthor());
        assertThat(retrievedOrder.getTitle()).isEqualTo(inputOrder.getTitle());
        assertThat(retrievedOrder.getContent()).isEqualTo(inputOrder.getContent());
    }
}
