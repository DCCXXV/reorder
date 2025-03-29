package com.thecritics.reorder.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.thecritics.reorder.ReorderApplication;
import com.thecritics.reorder.TestcontainersConfiguration;
import com.thecritics.reorder.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@ContextConfiguration(classes = ReorderApplication.class)
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

    @Test
    void testFindByTitleContainingIgnoreCaseOrderByCreatedAtDesc() {
        Order order1 = new Order();
        order1.setAuthor("John Doe");
        order1.setTitle("Ranking de frutas");

        List<List<String>> content1 = new ArrayList<>();
        content1.add(new ArrayList<>());
        content1.add(new ArrayList<>());

        content1.get(1).add("Manzana");
        order1.setContent(content1);

        Order order2 = new Order();
        order2.setAuthor("Jane Doe");
        order2.setTitle("Ranking de verduras");
        
        List<List<String>> content2 = new ArrayList<>();
        content2.add(new ArrayList<>());
        content2.add(new ArrayList<>());

        content2.get(1).add("Pimiento");
        order2.setContent(content2);

        orderRepository.save(order1);
        orderRepository.save(order2);

        List<Order> orders = orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc("frutas");

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getTitle()).isEqualTo("Ranking de frutas");
    }

    @Test
    void testFindByTitleContainingIgnoreCaseOrderByCreatedAtDescEmpty() {
        Order order1 = new Order();
        order1.setAuthor("John Doe");
        order1.setTitle("Ranking de frutas");

        List<List<String>> content1 = new ArrayList<>();
        content1.add(new ArrayList<>());
        content1.add(new ArrayList<>());

        content1.get(1).add("Manzana");
        order1.setContent(content1);

        Order order2 = new Order();
        order2.setAuthor("Jane Doe");
        order2.setTitle("Ranking de verduras");
        List<List<String>> content2 = new ArrayList<>();
        content2.add(new ArrayList<>());
        content2.add(new ArrayList<>());

        content2.get(1).add("Pimiento");
        order2.setContent(content2);

        orderRepository.save(order1);
        orderRepository.save(order2);

        List<Order> orders = orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc("carne");

        assertThat(orders).isEmpty();
    }

    @Test
    void testFindByTitleContainingIgnoreCaseOrderByCreatedAtDescMultiple() {
        Order order1 = new Order();
        order1.setAuthor("John Doe");
        order1.setTitle("Ranking de frutas");

        List<List<String>> content1 = new ArrayList<>();
        content1.add(new ArrayList<>());
        content1.add(new ArrayList<>());

        content1.get(1).add("Manzana");
        order1.setContent(content1);

        Order order2 = new Order();
        order2.setAuthor("Jane Doe");
        order2.setTitle("Ranking de verduras");
        List<List<String>> content2 = new ArrayList<>();
        content2.add(new ArrayList<>());
        content2.add(new ArrayList<>());

        content2.get(1).add("Pimiento");
        order2.setContent(content2);

        orderRepository.save(order1);
        orderRepository.save(order2);

        List<Order> orders = orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc("ranking");

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getTitle()).isEqualTo("Ranking de verduras");
        assertThat(orders.get(1).getTitle()).isEqualTo("Ranking de frutas");
    }
}
