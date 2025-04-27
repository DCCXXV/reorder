package com.thecritics.reorder.controller;

import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderViewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private Order sampleOrder;
    private final Integer ORDER_ID = 1;
    private final Integer NON_EXISTENT_ORDER_ID = 99;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setId(ORDER_ID);
        sampleOrder.setTitle("Test Order");
        sampleOrder.setContent(Arrays.asList(
            Arrays.asList("Item A1", "Item A2"),
            Collections.singletonList("Item B1")
        ));
    }

    @Test
    @DisplayName("GET /order/{id} - Should return order view and model when order exists")
    void getOrderDetail_ShouldReturnOrderViewAndModelWhenOrderExists() throws Exception {
        // Arrange: Configura el mock para devolver el order de ejemplo
        when(orderService.getOrderById(ORDER_ID)).thenReturn(sampleOrder);

        // Act & Assert
        mockMvc.perform(get("/order/{id}", ORDER_ID))
            .andExpect(status().isOk())
            .andExpect(view().name("order"))
            .andExpect(model().attributeExists("order"))
            .andExpect(model().attribute("order", sampleOrder))
            .andExpect(model().attributeDoesNotExist("searchQuery"))
            .andExpect(request().sessionAttributeDoesNotExist("searchQueryContext"));
    }

    @Test
    @DisplayName("GET /order/{id} - Should return error view when order does not exist")
    void getOrderDetail_ShouldReturnErrorViewWhenOrderNotFound() throws Exception {
        // Arrange: Configura el mock para devolver null (order no encontrado)
        when(orderService.getOrderById(NON_EXISTENT_ORDER_ID)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/order/{id}", NON_EXISTENT_ORDER_ID))
            .andExpect(status().isOk())
            .andExpect(view().name("error"))
            .andExpect(model().attributeExists("errorMessage"))
            .andExpect(model().attributeDoesNotExist("order"));
    }

    @Test
    @DisplayName("GET /order/{id}?fromQuery=... - Should add searchQuery to model and session")
    void getOrderDetail_ShouldAddQueryToModelAndSessionWhenFromQueryProvided() throws Exception {
        // Arrange
        String query = "search term";
        when(orderService.getOrderById(ORDER_ID)).thenReturn(sampleOrder);
        MockHttpSession session = new MockHttpSession();

        // Act & Assert
        mockMvc.perform(get("/order/{id}", ORDER_ID)
                .param("fromQuery", query)
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("order"))
            .andExpect(model().attribute("order", sampleOrder))
            .andExpect(model().attribute("searchQuery", query))
            .andExpect(request().sessionAttribute("searchQueryContext", query));
    }

     @Test
    @DisplayName("GET /order/{id} - Should use searchQuery from session if present and no fromQuery param")
    void getOrderDetail_ShouldUseSessionQueryWhenSessionQueryExistsAndNoParam() throws Exception {
        // Arrange
        String sessionQuery = "previous search";
        when(orderService.getOrderById(ORDER_ID)).thenReturn(sampleOrder);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("searchQueryContext", sessionQuery);

        // Act & Assert
        mockMvc.perform(get("/order/{id}", ORDER_ID)
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("order"))
            .andExpect(model().attribute("order", sampleOrder))
            .andExpect(model().attribute("searchQuery", sessionQuery))
            .andExpect(request().sessionAttribute("searchQueryContext", sessionQuery));
    }

     @Test
    @DisplayName("GET /order/{id}?fromQuery=... - Should prioritize fromQuery over session query")
    void getOrderDetail_ShouldPrioritizeParamWhenBothQueryParamAndSessionExist() throws Exception {
        // Arrange
        String paramQuery = "new search";
        String sessionQuery = "old search";
        when(orderService.getOrderById(ORDER_ID)).thenReturn(sampleOrder);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("searchQueryContext", sessionQuery);

        // Act & Assert
        mockMvc.perform(get("/order/{id}", ORDER_ID)
                .param("fromQuery", paramQuery)
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("order"))
            .andExpect(model().attribute("order", sampleOrder))
            .andExpect(model().attribute("searchQuery", paramQuery))
            .andExpect(request().sessionAttribute("searchQueryContext", paramQuery));
    }
}

