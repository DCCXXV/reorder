package com.thecritics.reorder.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.thecritics.reorder.ReorderApplication;
import com.thecritics.reorder.TestcontainersConfiguration;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrdererRepository;
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

    @Autowired
    private OrdererRepository ordererRepository;

    private Orderer createAndSaveOrderer(String username, String email, String password) {
        Orderer orderer = new Orderer();
        orderer.setUsername(username);
        orderer.setEmail(email);
        orderer.setPassword(password);
        return ordererRepository.save(orderer);
    }

    @Test
    void testSaveAndRetrieveOrder() {
        Orderer author = createAndSaveOrderer("johndoe", "john.doe@example.com", "password123");

        Order inputOrder = new Order();
        inputOrder.setAuthor(author);
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
        assertThat(retrievedOrder.getAuthor()).isEqualTo(author);
        assertThat(retrievedOrder.getTitle()).isEqualTo(inputOrder.getTitle());
        assertThat(retrievedOrder.getContent()).isEqualTo(inputOrder.getContent());
        assertThat(retrievedOrder.getAuthor().getUsername()).isEqualTo("johndoe");
    }

    @Test
    void testFindByTitleContainingIgnoreCaseOrderByCreatedAtDesc() {
        Orderer author1 = createAndSaveOrderer("johndoe", "john.doe@example.com", "password123");
        Orderer author2 = createAndSaveOrderer("janedoe", "jane.doe@example.com", "password456");

        Order order1 = new Order();
        order1.setAuthor(author1);
        order1.setTitle("Ranking de frutas");
        List<List<String>> content1 = new ArrayList<>();
        content1.add(new ArrayList<>());
        content1.add(new ArrayList<>());
        content1.get(1).add("Manzana");
        order1.setContent(content1);

        Order order2 = new Order();
        order2.setAuthor(author2);
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
        assertThat(orders.get(0).getAuthor()).isEqualTo(author1);
    }

    @Test
    void testFindByTitleContainingIgnoreCaseOrderByCreatedAtDescEmpty() {
        Orderer author1 = createAndSaveOrderer("johndoe", "john.doe@example.com", "password123");
        Orderer author2 = createAndSaveOrderer("janedoe", "jane.doe@example.com", "password456");

        Order order1 = new Order();
        order1.setAuthor(author1);
        order1.setTitle("Ranking de frutas");
        List<List<String>> content1 = new ArrayList<>();
        content1.add(new ArrayList<>());
        content1.add(new ArrayList<>());
        content1.get(1).add("Manzana");
        order1.setContent(content1);

        Order order2 = new Order();
        order2.setAuthor(author2);
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
        Orderer author1 = createAndSaveOrderer("johndoe", "john.doe@example.com", "password123");
        Orderer author2 = createAndSaveOrderer("janedoe", "jane.doe@example.com", "password456");

        Order order1 = new Order();
        order1.setAuthor(author1);
        order1.setTitle("Ranking de frutas");
        List<List<String>> content1 = new ArrayList<>();
        content1.add(new ArrayList<>());
        content1.add(new ArrayList<>());
        content1.get(1).add("Manzana");
        order1.setContent(content1);

        Order order2 = new Order();
        order2.setAuthor(author2);
        order2.setTitle("Ranking de verduras");
        List<List<String>> content2 = new ArrayList<>();
        content2.add(new ArrayList<>());
        content2.add(new ArrayList<>());
        content2.get(1).add("Pimiento");
        order2.setContent(content2);

        Order savedOrder1 = orderRepository.save(order1);
        Order savedOrder2 = orderRepository.save(order2);

        List<Order> orders = orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc("ranking");

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getTitle()).isEqualTo("Ranking de verduras");
        assertThat(orders.get(0).getAuthor()).isEqualTo(author2);
        assertThat(orders.get(1).getTitle()).isEqualTo("Ranking de frutas");
        assertThat(orders.get(1).getAuthor()).isEqualTo(author1);
    }

    @Test
    void testSaveAndRetrieveReOrder() {
        Orderer authorOriginal = createAndSaveOrderer("manuel", "manuel@example.com", "passManuel");
        Orderer authorReorder = createAndSaveOrderer("chang", "chang@example.com", "passChang");

        Order originalOrder = new Order();
        originalOrder.setAuthor(authorOriginal);
        originalOrder.setTitle("Order de Orders");

        List<List<String>> ocontent = new ArrayList<>();
        ocontent.add(new ArrayList<>());
        ocontent.add(new ArrayList<>());
        ocontent.add(new ArrayList<>());
        ocontent.add(new ArrayList<>());

        ocontent.get(1).add("Top 10 puertas");
        ocontent.get(2).add("Top 10 frutas");
        ocontent.get(3).add("Ranking de verduras");

        originalOrder.setContent(ocontent);

        Order savedOrder = orderRepository.save(originalOrder);
        Order retrievedOriginalOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        Order inputReOrder = new Order();
        inputReOrder.setReorderedOrder(retrievedOriginalOrder);
        inputReOrder.setAuthor(authorReorder);
        inputReOrder.setTitle("Reordenación de Orders");

        List<List<String>> rcontent = new ArrayList<>();
        rcontent.add(new ArrayList<>());
        rcontent.add(new ArrayList<>());
        rcontent.add(new ArrayList<>());
        rcontent.add(new ArrayList<>());

        rcontent.get(1).add("Top 10 frutas");
        rcontent.get(1).add("Top 10 puertas");
        rcontent.get(2).add("Ranking de verduras");

        inputReOrder.setContent(rcontent);

        Order savedReOrder = orderRepository.save(inputReOrder);
        Order retrievedReOrder = orderRepository.findById(savedReOrder.getId()).orElse(null);

        assertThat(retrievedReOrder).isNotNull();
        assertThat(retrievedReOrder.getAuthor()).isEqualTo(authorReorder);
        assertThat(retrievedReOrder.getReorderedOrder()).isEqualTo(retrievedOriginalOrder);
        assertThat(retrievedReOrder.getContent()).isEqualTo(rcontent);
        assertThat(retrievedReOrder.getContent()).isNotEqualTo(retrievedOriginalOrder.getContent());
        assertThat(retrievedReOrder.getAuthor()).isNotEqualTo(retrievedOriginalOrder.getAuthor());
    }
}
