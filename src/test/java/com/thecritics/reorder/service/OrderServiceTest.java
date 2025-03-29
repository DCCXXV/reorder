package com.thecritics.reorder.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.junit.jupiter.MockitoExtension;

import com.thecritics.reorder.controller.RootController;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.repository.OrderRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private HttpSession session;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Order order;

    @Test
    void addElement_ShouldAddElementToUnassignedTier() {
        // 1. Arrange; creamos las variables
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // Tier 0
        initialOrderState.add(new ArrayList<>()); // Tier 1

        String elementText = "Nuevo elemento";

        // 2. Act; ejecutamos lo que vamos a testear
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.addElement(elementText, session);

        // 3. Assert; comprobamos si los resultados son los esperados
        assertThat(result.getFirst().size()).isEqualTo(1);
        assertThat(result.getFirst().getFirst()).isEqualTo(elementText);
    }

    @Test
    void addElement_ShouldNotAddEmptyElementText() {
        // 1. Arrange; creamos las variables
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // Tier 0
        initialOrderState.add(new ArrayList<>()); // Tier 1

        String elementText = ""; // vacio

        // 2. Act; ejecutamos lo que vamos a testear
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.addElement(elementText, session);

        // 3. Assert; comprobamos si los resultados son los esperados
        assertThat(result.getFirst().size()).isEqualTo(0);
    }

    @Test
    void deleteElement_ShouldDeleteElementFromTheUnassignedTier() {
        // 1. Arrange; Creamos las variables
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());// Añadimos la Unassigned Tier
        initialOrderState.add(new ArrayList<>());// Añadimos la Tier 1

        String elementText = "Elemento a eliminar"; // Creamos un elemento
        initialOrderState.getFirst().add(elementText); // Añadimos un elemento a esa Tier

        // 2. Act; Ejecutamos lo que vamos a Testear
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.deleteElement(elementText, session);

        // 3. Assert; Comprobamos los resultado
        assertThat(result.getFirst().size()).isEqualTo(0);
        assertThat(result.get(1).size()).isEqualTo(0);
    }

    @Test
    void deleteElement_ShouldDeleteElementFromTheAssignedTier() {
        // Inicializamos las variables
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());

        String elementText = "Elemento a eliminar";
        initialOrderState.get(1).add(elementText);
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        // Ejecutamos lo que vamos a Testear
        List<List<String>> result = orderService.deleteElement(elementText, session);

        // Comprobamos los resultados
        assertThat(result.getFirst().size()).isEqualTo(0);
        assertThat(result.get(1).size()).isEqualTo(0);
    }

    @Test
    void deleteElement_ShouldDeleteElementsWithSpecialCharacters() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // Tier 0
        initialOrderState.add(new ArrayList<>()); // Tier 1
        String elementText = "¡¡ \" Elemento a eliminar con carácteres especiales <h1> \" !!"; // Creamos un elemento
        initialOrderState.getFirst().add(elementText); // Añadimos un elemento a esa Tier
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        List<List<String>> result = orderService.deleteElement(elementText, session);

        assertThat(result.getFirst().size()).isEqualTo(0);
    }

    @Test
    void addTier_ShouldAddTierToEnd() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // lista 0
        initialOrderState.add(new ArrayList<>()); // lista 1
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        List<List<String>> result = orderService.addTier(session);

        assertThat(result).hasSize(3);
    }

    @Test
    void deleteTier_ShouldNotDeleteFirtTwoTiers() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // Tier 0
        initialOrderState.add(new ArrayList<>()); // Tier 1

        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        List<List<String>> result = orderService.deleteLastTier(session); // Eliminamos la última Tier

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteLastTier_ShouldDeleteLastTierIfFirstTwoExist() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // Tier 0 (unassigned)
        initialOrderState.add(new ArrayList<>()); // Tier 1
        initialOrderState.add(new ArrayList<>()); // Tier 2
        int numberOfTiers = initialOrderState.size();
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        List<List<String>> result = orderService.deleteLastTier(session); // Eliminamos la última Tier

        assertThat(result).hasSize(numberOfTiers - 1);
    }

    @Test
    void deleteLastTier_ShouldDeleteLastTierAndAllElementsInTier() {
        // configurar estado inicial
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // Tier 0 (unassigned)
        initialOrderState.add(new ArrayList<>()); // Tier 1
        initialOrderState.add(new ArrayList<>()); // Tier 2

        // añadir elemento al estado inicial
        String elementText = "Nuevo Elemento";
        initialOrderState.getLast().add(elementText);

        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        List<List<String>> result = orderService.deleteLastTier(session);

        assertThat(result.getLast().size()).isZero();
    }

    @Test
    void getOrderState_ShouldGetOrderWithElementsAndTiersCorrectly() {
        // Arrange
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(Arrays.asList("Plátano", "Manzana"));
        initialOrderState.add(Arrays.asList("Naranja", "Pera"));
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        // Act
        List<List<String>> result = orderService.getOrderState(session);

        // Assert
        for (int i = 0; i < initialOrderState.size(); i++) {
            assertThat(result.get(i)).isEqualTo(initialOrderState.get(i));
        }
    }

    @Test
    void updateOrderState_ShouldUpdateSessionAttributeAndReturnUpdatedState() {
        // Arrange
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(Arrays.asList("Manzana", "Pera")); // Tier 0
        initialOrderState.add(Arrays.asList("Naranja")); // Tier 1

        List<List<String>> newOrderState = new ArrayList<>();
        newOrderState.add(Arrays.asList("Naranja", "Pera", "Sandía")); // nuevo Tier 0
        newOrderState.add(Arrays.asList("Manzana", "Limón")); // nuevo Tier 1
        newOrderState.add(Arrays.asList()); // nuevo Tier 2

        // Act
        // Actualizamos el Order del estado inicial al nuevo
        List<List<String>> result = orderService.updateOrderState(newOrderState, session);

        // Assert
        verify(session).setAttribute("orderState", newOrderState);
        assertThat(result).isSameAs(newOrderState);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).containsExactly("Naranja", "Pera", "Sandía");
        assertThat(result.get(1)).containsExactly("Manzana", "Limón");
    }

    @Test
    void saveOrder_ShouldSetAuthorToAnonymous_WhenAuthorIsEmpty() {
        // Arrange
        String title = "Top frutas";
        String author = "";
        List<List<String>> content = new ArrayList<>();
        content.add(Arrays.asList("Plátano", "Manzana"));
        content.add(Arrays.asList("Naranja", "Pera"));

        Order expectedOrder = new Order();

        expectedOrder.setId(2L);
        expectedOrder.setTitle(title);
        expectedOrder.setAuthor("Anónimo");
        expectedOrder.setContent(content);

        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // Act
        Order savedOrder = orderService.saveOrder(title, author, content);

        // Assert
        assertThat(savedOrder.getAuthor()).isEqualTo("Anónimo");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void saveOrder_ShouldSaveOrderWithTitleAuthorAndContentCorrectly() {
        // Arrange
        String title = "TOP FRUTAS";
        String author = "Sara";
        List<List<String>> content = new ArrayList<>();
        content.add(Arrays.asList("Manzana", "Pera"));
        content.add(Arrays.asList("Naranja"));

        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        expectedOrder.setTitle(title);
        expectedOrder.setAuthor(author);
        expectedOrder.setContent(content);

        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // Act
        Order savedOrder = orderService.saveOrder(title, author, content);

        // Assert
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isEqualTo(1L);
        assertThat(savedOrder.getTitle()).isEqualTo(title);
        assertThat(savedOrder.getAuthor()).isEqualTo(author);
        assertThat(savedOrder.getContent()).isEqualTo(content);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    // BUSCAR ORDER

    @Test
    void getOrdersByTitle_ShouldReturnOrdersWhenTitleExists() {
        // Arrange
        String title = "Top frutas";
        Order order1 = new Order();
        order1.setId(1L);
        order1.setTitle(title);
        order1.setAuthor("Alonso");

        Order order2 = new Order();
        order2.setId(2L);
        order2.setTitle("Top frutas favoritas");
        order2.setAuthor("Julia");

        List<Order> Orders = Arrays.asList(order1, order2);

        when(orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title)).thenReturn(Orders);

        // Act
        List<Order> result = orderService.getOrdersByTitle(title);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(order1, order2);
        verify(orderRepository, times(1)).findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title);
    }

    @Test
    void getOrdersByTitle_ShouldReturnEmptyListWhenTitleNotExists() {
        // Arrange
        String title = "Título inexistente";
        when(orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title)).thenReturn(new ArrayList<>());

        // Act
        List<Order> result = orderService.getOrdersByTitle(title);

        // Assert
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title);
    }

    @Test
    void getOrdersByTitle_ShouldReturnAllOrdersWhenTitleIsEmpty() {
        // Arrange
        Order order1 = new Order();
        order1.setId(1L);
        order1.setTitle("Top frutas");

        Order order2 = new Order();
        order2.setId(2L);
        order2.setTitle("Top videojuegos");

        List<Order> mockOrders = Arrays.asList(order1, order2);

        when(orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc("")).thenReturn(mockOrders);

        // Act
        List<Order> result = orderService.getOrdersByTitle("");

        // Assert
        assertThat(result).hasSize(2);
        verify(orderRepository, times(1)).findByTitleContainingIgnoreCaseOrderByCreatedAtDesc("");
    }

    @Test
    void getOrdersByTitle_ShouldReturnMatchingOrders() {
        // Arrange
        String query = "op";

        List<Order> expectedOrders = new ArrayList<>();
        Order order1 = new Order();
        order1.setTitle("Top frutas");

        Order order2 = new Order();
        order2.setTitle("Opciones favoritas");

        Order order3 = new Order();
        order3.setTitle("Frutas");

        expectedOrders.add(order1);
        expectedOrders.add(order2);

        when(orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(query)).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.getOrdersByTitle(query);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedOrders);

        verify(orderRepository, times(1)).findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(query);
    }
    
   
}