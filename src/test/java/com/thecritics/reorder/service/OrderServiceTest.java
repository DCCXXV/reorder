package com.thecritics.reorder.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private HttpSession session;

    /**
     * Verifica que addElement añade un elemento a la primera lista del estado de la orden.
     *
     * <p>Configura un estado inicial con dos listas vacías y verifica que el elemento
     * se añade correctamente a la primera lista.</p>
     */
    @Test
    void testAddElementShouldAddElementToFirstList() {
        // 1. Arrange; creamos la las variables
        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>());
        initialState.add(new ArrayList<>());

        String elementText = "Nuevo elemento";

        // 2. Act; ejecutamos lo que vamos a testear
        when(session.getAttribute("orderState")).thenReturn(initialState);
        List<List<String>> result = orderService.addElement(elementText, session);

        // 3. Assert; comprobamos si los resultados son los esperados
        assertThat(result.get(0).size()).isEqualTo(1);
        assertThat(result.get(0).get(0)).isEqualTo(elementText);
    }

    @Test
    void deleteElement() {

    }

    @Test
    void addTier() {

    }

    @Test
    void deleteLastTier() {
    }

    @Test
    void getOrderState() {
    }

    @Test
    void updateOrderState() {
    }
}