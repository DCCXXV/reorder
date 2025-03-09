package com.thecritics.reorder.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private HttpSession session;

    @Test
    void addElement() {
        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>());
        initialState.add(new ArrayList<>());
        when(session.getAttribute("orderState")).thenReturn(initialState);

        String elementText = "Nuevo elemento";
        List<List<String>> result = orderService.addElement(elementText, session);

        assertEquals(1, result.get(0).size());
        assertEquals(elementText, result.get(0).get(0));
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