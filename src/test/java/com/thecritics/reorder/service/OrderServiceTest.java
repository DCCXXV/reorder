package com.thecritics.reorder.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private HttpSession session;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Orderer authorOrderer;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private Orderer createMockOrderer(long id, String username, String email) {
        Orderer orderer = mock(Orderer.class);
        when(orderer.getId()).thenReturn(id);
        when(orderer.getUsername()).thenReturn(username);
        when(orderer.getEmail()).thenReturn(email);
        return orderer;
    }

    @BeforeEach
    void setUp() {}

    @Test
    void addElement_ShouldAddElementToUnassignedTier() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        String elementText = "Nuevo elemento";
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.addElement(elementText, session);
        assertThat(result.getFirst()).hasSize(1);
        assertThat(result.getFirst().getFirst()).isEqualTo(elementText);
    }

    @Test
    void addElement_ShouldNotAddEmptyElementText() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        String elementText = "";
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.addElement(elementText, session);
        assertThat(result.getFirst()).isEmpty();
    }

    @Test
    void deleteElement_ShouldDeleteElementFromTheUnassignedTier() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        String elementText = "Elemento a eliminar";
        initialOrderState.getFirst().add(elementText);
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.deleteElement(elementText, session);
        assertThat(result.getFirst()).isEmpty();
        assertThat(result.get(1)).isEmpty();
    }

    @Test
    void deleteElement_ShouldDeleteElementFromTheAssignedTier() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        String elementText = "Elemento a eliminar";
        initialOrderState.get(1).add(elementText);
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.deleteElement(elementText, session);
        assertThat(result.getFirst()).isEmpty();
        assertThat(result.get(1)).isEmpty();
    }

    @Test
    void deleteElement_ShouldDeleteElementsWithSpecialCharacters() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        String elementText =
            "¡¡ \" Elemento a eliminar con carácteres especiales <h1> \" !!";
        initialOrderState.getFirst().add(elementText);
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.deleteElement(elementText, session);
        assertThat(result.getFirst()).isEmpty();
    }

    @Test
    void addTier_ShouldAddTierToEnd() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.addTier(session);
        assertThat(result).hasSize(3);
    }

    @Test
    void deleteTier_ShouldNotDeleteFirtTwoTiers() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.deleteLastTier(session);
        assertThat(result).hasSize(2);
    }

    @Test
    void deleteLastTier_ShouldDeleteLastTierIfFirstTwoExist() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        int numberOfTiers = initialOrderState.size();
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.deleteLastTier(session);
        assertThat(result).hasSize(numberOfTiers - 1);
    }

    @Test
    void deleteLastTier_ShouldDeleteLastTierAndAllElementsInTier() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());
        String elementText = "Nuevo Elemento";
        initialOrderState.getLast().add(elementText);
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.deleteLastTier(session);
        assertThat(result.getLast()).isEmpty();
    }

    @Test
    void getOrderState_ShouldGetOrderWithElementsAndTiersCorrectly() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(Arrays.asList("Plátano", "Manzana"));
        initialOrderState.add(Arrays.asList("Naranja", "Pera"));
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.getOrderState(session);
        assertThat(result).isEqualTo(initialOrderState);
    }

    @Test
    void updateOrderState_ShouldUpdateSessionAttributeAndReturnUpdatedState() {
        List<List<String>> newOrderState = new ArrayList<>();
        newOrderState.add(Arrays.asList("Naranja", "Pera", "Sandía"));
        newOrderState.add(Arrays.asList("Manzana", "Limón"));
        newOrderState.add(new ArrayList<>());
        List<List<String>> result = orderService.updateOrderState(
            newOrderState,
            session
        );
        verify(session).setAttribute("orderState", newOrderState);
        assertThat(result).isSameAs(newOrderState);
    }

    @Test
    void saveOrder_ShouldSaveOrderWithTitleAuthorAndContentCorrectly() {
        String title = "TOP FRUTAS";
        String authorUsername = "Sara";

        List<List<String>> content = List.of(
            List.of("Manzana", "Pera"),
            List.of("Naranja")
        );

        when(authorOrderer.getUsername()).thenReturn(authorUsername);

        Order expectedSavedOrder = mock(Order.class);
        when(expectedSavedOrder.getId()).thenReturn(1L);
        when(expectedSavedOrder.getTitle()).thenReturn(title);
        when(expectedSavedOrder.getAuthor()).thenReturn(authorOrderer);
        when(expectedSavedOrder.getContent()).thenReturn(content);

        when(orderRepository.save(any(Order.class)))
            .thenReturn(expectedSavedOrder);

        Order.Transfer savedOrder = orderService.saveOrder(title, authorUsername, content);

        verify(orderRepository, times(1)).save(orderCaptor.capture());
        Order orderPassedToSave = orderCaptor.getValue();

        assertThat(orderPassedToSave.getTitle()).isEqualTo(title);
        assertThat(orderPassedToSave.getContent()).isEqualTo(content);

        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getId()).isEqualTo(1L);
        assertThat(savedOrder.getTitle()).isEqualTo(title);
        assertThat(savedOrder.getContent()).isEqualTo(content);
        assertThat(savedOrder.getAuthor()).isNotNull();
        assertThat(savedOrder.getAuthor()).isEqualTo(authorOrderer);
        assertThat(savedOrder.getAuthor()).isEqualTo(authorUsername);
    }

    @Test
    void getOrdersByTitle_ShouldReturnOrdersWhenTitleExists() {
        String title = "Top frutas";
        Orderer author1 = createMockOrderer(10, "Alonso", "alonso@a.com");
        Orderer author2 = createMockOrderer(11, "Julia", "julia@j.com");
        Order order1 = mock(Order.class);
        when(order1.getId()).thenReturn(1L);
        when(order1.getTitle()).thenReturn(title);
        when(order1.getAuthor()).thenReturn(author1);
        Order order2 = mock(Order.class);
        when(order2.getId()).thenReturn(2L);
        when(order2.getTitle()).thenReturn("Top frutas favoritas");
        when(order2.getAuthor()).thenReturn(author2);
        List<Order> mockOrders = Arrays.asList(order1, order2);
        when(
            orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                title
            )
        )
            .thenReturn(mockOrders);
        List<Order> result = orderService.getOrdersByTitle(title);
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(order1, order2);
        verify(orderRepository, times(1))
            .findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title);
    }

    @Test
    void getOrdersByTitle_ShouldReturnEmptyListWhenTitleNotExists() {
        String title = "Título inexistente";
        when(
            orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                title
            )
        )
            .thenReturn(new ArrayList<>());
        List<Order> result = orderService.getOrdersByTitle(title);
        assertThat(result).isEmpty();
        verify(orderRepository, times(1))
            .findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title);
    }

    @Test
    void getOrdersByTitle_ShouldReturnAllOrdersWhenTitleIsEmpty() {
        Orderer author1 = createMockOrderer(10, "Alonso", "alonso@a.com");
        Orderer author2 = createMockOrderer(11, "Julia", "julia@j.com");
        Order order1 = mock(Order.class);
        when(order1.getId()).thenReturn(1L);
        when(order1.getTitle()).thenReturn("Top frutas");
        when(order1.getAuthor()).thenReturn(author1);
        Order order2 = mock(Order.class);
        when(order2.getId()).thenReturn(2L);
        when(order2.getTitle()).thenReturn("Top videojuegos");
        when(order2.getAuthor()).thenReturn(author2);
        List<Order> mockOrders = Arrays.asList(order1, order2);
        when(
            orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc("")
        )
            .thenReturn(mockOrders);
        List<Order> result = orderService.getOrdersByTitle("");
        assertThat(result).hasSize(2);
        verify(orderRepository, times(1))
            .findByTitleContainingIgnoreCaseOrderByCreatedAtDesc("");
    }

    @Test
    void getOrdersByTitle_ShouldReturnMatchingOrders() {
        String query = "op";
        Orderer author1 = createMockOrderer(10, "Alonso", "alonso@a.com");
        Orderer author2 = createMockOrderer(11, "Julia", "julia@j.com");
        Order order1 = mock(Order.class);
        when(order1.getTitle()).thenReturn("Top frutas");
        when(order1.getAuthor()).thenReturn(author1);
        Order order2 = mock(Order.class);
        when(order2.getTitle()).thenReturn("Opciones favoritas");
        when(order2.getAuthor()).thenReturn(author2);
        List<Order> expectedOrders = Arrays.asList(order1, order2);
        when(
            orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                query
            )
        )
            .thenReturn(expectedOrders);
        List<Order> result = orderService.getOrdersByTitle(query);
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedOrders);
        verify(orderRepository, times(1))
            .findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(query);
    }

    @Test
    void getReOrderState_ShouldInitializeStateIfNull() {
        List<List<String>> expectedReOrderState = new ArrayList<>();
        expectedReOrderState.add(new ArrayList<>());
        expectedReOrderState.add(new ArrayList<>());
        when(session.getAttribute("reOrderState")).thenReturn(null);
        List<List<String>> result = orderService.getReOrderState(session);
        assertThat(result).isEqualTo(expectedReOrderState);
    }

    @Test
    void getReOrderState_ShouldReturnExistingStateIfNotNull() {
        List<List<String>> existingReOrderState = new ArrayList<>();
        existingReOrderState.add(Arrays.asList("Manzana", "Pera"));
        existingReOrderState.add(Arrays.asList("Naranja"));
        when(session.getAttribute("reOrderState")).thenReturn(existingReOrderState);
        List<List<String>> result = orderService.getReOrderState(session);
        assertThat(result).isEqualTo(existingReOrderState);
    }

    @Test
    void updateReOrderState_ShouldUpdateStateInSession() {
        List<List<String>> newReOrderState = new ArrayList<>();
        newReOrderState.add(Arrays.asList("Manzana", "Pera"));
        newReOrderState.add(Arrays.asList("Naranja"));
        List<List<String>> result = orderService.updateReOrderState(
            newReOrderState,
            session
        );
        assertThat(result).isEqualTo(newReOrderState);
    }

    @Test
    void updateReOrderState_ShouldSetAttributeInSession() {
        List<List<String>> newReOrderState = new ArrayList<>();
        newReOrderState.add(Arrays.asList("Manzana", "Pera"));
        newReOrderState.add(Arrays.asList("Naranja"));
        orderService.updateReOrderState(newReOrderState, session);
        verify(session).setAttribute("reOrderState", newReOrderState);
    }
}