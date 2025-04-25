package com.thecritics.reorder.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;

import com.thecritics.reorder.ReorderApplication;
import com.thecritics.reorder.TestcontainersConfiguration;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@ContextConfiguration(classes = ReorderApplication.class)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testSaveAndRetrieveOrder() {

        Orderer inputOrderer = new Orderer();
        inputOrderer.setEmail("autor@gmail.com");
        inputOrderer.setId(224342);
        inputOrderer.setPassword("AAAaaa111");
        inputOrderer.setUsername("John Doe");

        Order inputOrder = new Order();
        inputOrder.setAuthor(inputOrderer);
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
        Orderer orderer1 = new Orderer();
        orderer1.setEmail("author@gmail.com");
        orderer1.setId(4366);
        orderer1.setPassword("ewiqt33AA");
        orderer1.setUsername("John Doe");

        Order order1 = new Order();
        order1.setAuthor(orderer1);
        order1.setTitle("Ranking de frutas");

        List<List<String>> content1 = new ArrayList<>();
        content1.add(new ArrayList<>());
        content1.add(new ArrayList<>());

        content1.get(1).add("Manzana");
        order1.setContent(content1);

        Orderer orderer2 = new Orderer();
        orderer2.setEmail("sauthor@gmail.com");
        orderer2.setId(43866);
        orderer2.setPassword("ewiqt33AA");
        orderer2.setUsername("Jane Doe");

        Order order2 = new Order();
        order2.setAuthor(orderer2);
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

        Orderer orderer1 = new Orderer();
        orderer1.setEmail("author@gmail.com");
        orderer1.setId(4366);
        orderer1.setPassword("ewiqt33AA");
        orderer1.setUsername("John Doe");


        Order order1 = new Order();
        order1.setAuthor(orderer1);
        order1.setTitle("Ranking de frutas");

        List<List<String>> content1 = new ArrayList<>();
        content1.add(new ArrayList<>());
        content1.add(new ArrayList<>());

        content1.get(1).add("Manzana");
        order1.setContent(content1);

        Orderer orderer2 = new Orderer();
        orderer2.setEmail("autho2r@gmail.com");
        orderer2.setId(45366);
        orderer2.setPassword("ewiqt33AA");
        orderer2.setUsername("Jane Doe");


        Order order2 = new Order();
        order2.setAuthor(orderer2);
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

        Orderer orderer1 = new Orderer();
        orderer1.setEmail("author@gmail.com");
        orderer1.setId(4366);
        orderer1.setPassword("ewiqt33AA");
        orderer1.setUsername("John Doe");


        Order order1 = new Order();
        order1.setAuthor(orderer1);
        order1.setTitle("Ranking de frutas");

        List<List<String>> content1 = new ArrayList<>();
        content1.add(new ArrayList<>());
        content1.add(new ArrayList<>());

        content1.get(1).add("Manzana");
        order1.setContent(content1);

        Orderer orderer2 = new Orderer();
        orderer2.setEmail("author2@gmail.com");
        orderer2.setId(43566);
        orderer2.setPassword("ewiqt33AA");
        orderer2.setUsername("Jane Doe");

        Order order2 = new Order();
        order2.setAuthor(orderer2);
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

    @Test
    void testSaveAndRetrieveReOrder() {
        // ------------ ORDER ORIGINAL -------------------------
        Orderer originalOrderer = new Orderer();
        originalOrderer.setEmail("author@gmail.com");
        originalOrderer.setId(4366);
        originalOrderer.setPassword("ewiqt33AA");
        originalOrderer.setUsername("Manuel");


        Order originalOrder = new Order();
        originalOrder.setAuthor(originalOrderer);
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
        Order retrievedOrder = orderRepository.findById(savedOrder.getId()).orElse(null);

        // ------------ REORDER -------------------------------

        Orderer inputOrderer = new Orderer();
        inputOrderer.setEmail("author1@gmail.com");
        inputOrderer.setId(43366);
        inputOrderer.setPassword("ewiqt33AA");
        inputOrderer.setUsername("Chang");


        Order inputReOrder = new Order();

        inputReOrder.setReorderedOrder(retrievedOrder);
        inputReOrder.setAuthor(inputOrderer);
        inputReOrder.setTitle("Order de Orders");

        List<List<String>> rcontent = new ArrayList<>();
        rcontent.add(new ArrayList<>());
        rcontent.add(new ArrayList<>());
        rcontent.add(new ArrayList<>());
        rcontent.add(new ArrayList<>());

        rcontent.get(1).add("Top 10 fuertas");
        rcontent.get(1).add("Top 10 puertas");
        rcontent.get(2).add("Ranking de verduras");

        inputReOrder.setContent(rcontent);

        Order savedReOrder = orderRepository.save(inputReOrder);
        Order retrievedReOrder = orderRepository.findById(savedReOrder.getId()).orElse(null);

        assertThat(retrievedReOrder).isNotNull();
        assertThat(retrievedReOrder.getReorderedOrder()).isEqualTo(retrievedOrder);
        assertThat(retrievedReOrder.getContent()).isNotEqualTo(retrievedOrder.getContent());
    }
}
