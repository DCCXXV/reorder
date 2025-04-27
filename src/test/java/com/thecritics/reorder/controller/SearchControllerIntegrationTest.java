package com.thecritics.reorder.controller;

import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.service.OrderService;
import com.thecritics.reorder.service.OrdererService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerIntegrationTest {

    private static final String SEARCH_QUERY = "Test Query";
    private static final String EMPTY_QUERY = "";
    private static final String WHITESPACE_QUERY = "   ";
    private static final String AUTOCOMPLETE_QUERY = "Tes";
    private static final int AUTOCOMPLETE_LIMIT = 3;
    private static final String DUPLICATE_QUERY = "Duplicate";
    private static final String NO_MATCH_QUERY = "NoMatch";

    private static final String SEARCH_VIEW_NAME = "search";
    private static final String AUTOCOMPLETE_FRAGMENT = "fragments/search :: autocomplete";
    private static final String EMPTY_FRAGMENT = "fragments/search :: empty";
    private static final String REDIRECT_HOME_URL = "/";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrdererService ordererService;

    private Order mockOrder1;
    private Order mockOrder2;
    private Orderer mockOrderer1;
    private Orderer mockOrderer2;
    private Orderer.Transfer mockOrdererTransfer1; 
    private Orderer.Transfer mockOrdererTransfer2; 

    private Order mockOrderAutocomplete1;
    private Order mockOrderAutocomplete2;
    private Order mockOrderAutocompleteDuplicateTitle;

    private Orderer.Transfer mockOrdererAutocompleteTransfer1;
    private Orderer.Transfer mockOrdererAutocompleteTransfer2;
    private Orderer.Transfer mockOrdererAutocompleteTransferDuplicateUsername;


    @BeforeEach
    void setUp() {
        mockOrderer1 = new Orderer();
        mockOrderer1.setId(101);
        mockOrderer1.setUsername("Test Query User 1");
        mockOrderer1.setEmail("user1@example.com");
        mockOrderer1.setOrders(new ArrayList<>());

        mockOrderer2 = new Orderer();
        mockOrderer2.setId(102);
        mockOrderer2.setUsername("Another Test User");
        mockOrderer2.setEmail("user2@example.com");
        mockOrderer2.setOrders(new ArrayList<>());

        mockOrdererTransfer1 = mockOrderer1.toTransfer();
        mockOrdererTransfer2 = mockOrderer2.toTransfer();

        mockOrder1 = new Order();
        mockOrder1.setId(1);
        mockOrder1.setTitle("Test Query Order 1");
        mockOrder1.setAuthor(mockOrderer1);

        mockOrder2 = new Order();
        mockOrder2.setId(2);
        mockOrder2.setTitle("Another Test Query Order");
        mockOrder2.setAuthor(mockOrderer2);

        mockOrderAutocomplete1 = new Order();
        mockOrderAutocomplete1.setId(10);
        mockOrderAutocomplete1.setTitle("Test Autocomplete A");

        mockOrderAutocomplete2 = new Order();
        mockOrderAutocomplete2.setId(11);
        mockOrderAutocomplete2.setTitle("Test Autocomplete B");

        mockOrderAutocompleteDuplicateTitle = new Order();
        mockOrderAutocompleteDuplicateTitle.setId(12);
        mockOrderAutocompleteDuplicateTitle.setTitle("Test Autocomplete A");

        mockOrdererAutocompleteTransfer1 = new Orderer.Transfer("TestAutocompleteUser1", "auto1@example.com", 0);
        mockOrdererAutocompleteTransfer2 = new Orderer.Transfer("TestAutocompleteUser2", "auto2@example.com", 0);
        mockOrdererAutocompleteTransferDuplicateUsername = new Orderer.Transfer("TestAutocompleteUser1", "auto3@example.com", 0); 

        when(orderService.getOrdersByTitle(eq(SEARCH_QUERY)))
                .thenReturn(Arrays.asList(mockOrder1, mockOrder2));
        when(ordererService.getOrderersByUsername(eq(SEARCH_QUERY)))
                .thenReturn(Arrays.asList(mockOrdererTransfer1, mockOrdererTransfer2));
        when(orderService.getOrdersByTitle(eq(EMPTY_QUERY)))
                .thenReturn(Collections.emptyList());
        when(ordererService.getOrderersByUsername(eq(EMPTY_QUERY)))
                .thenReturn(Collections.emptyList());

        when(orderService.findTopOrdersStartingWith(eq(AUTOCOMPLETE_QUERY), eq(AUTOCOMPLETE_LIMIT)))
                .thenReturn(Arrays.asList(mockOrderAutocomplete1, mockOrderAutocomplete2));
        when(orderService.findTopOrdersStartingWith(eq(DUPLICATE_QUERY), eq(AUTOCOMPLETE_LIMIT)))
                .thenReturn(Arrays.asList(mockOrderAutocomplete1, mockOrderAutocompleteDuplicateTitle, mockOrderAutocomplete2));
        when(orderService.findTopOrdersStartingWith(eq(NO_MATCH_QUERY), eq(AUTOCOMPLETE_LIMIT)))
                .thenReturn(Collections.emptyList());

        when(ordererService.findTopOrderersStartingWith(eq(AUTOCOMPLETE_QUERY), eq(AUTOCOMPLETE_LIMIT)))
                .thenReturn(Arrays.asList(mockOrdererAutocompleteTransfer1, mockOrdererAutocompleteTransfer2));
        when(ordererService.findTopOrderersStartingWith(eq(DUPLICATE_QUERY), eq(AUTOCOMPLETE_LIMIT)))
                .thenReturn(Arrays.asList(mockOrdererAutocompleteTransfer1, mockOrdererAutocompleteTransferDuplicateUsername, mockOrdererAutocompleteTransfer2));
        when(ordererService.findTopOrderersStartingWith(eq(NO_MATCH_QUERY), eq(AUTOCOMPLETE_LIMIT)))
                .thenReturn(Collections.emptyList());
    }


    @Test
    void searchByTitle_ShouldReturnSearchViewWithResults() throws Exception {
        mockMvc.perform(post("/search")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("query", SEARCH_QUERY)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SEARCH_VIEW_NAME))
                .andExpect(model().attributeExists("query", "orderList", "ordererList"))
                .andExpect(model().attribute("query", SEARCH_QUERY))
                .andExpect(model().attribute("orderList", hasSize(2)))
                .andExpect(model().attribute("orderList", containsInAnyOrder(mockOrder1, mockOrder2)))
                .andExpect(model().attribute("ordererList", hasSize(2)))
                .andExpect(model().attribute("ordererList", containsInAnyOrder(
                        mockOrdererTransfer1,
                        mockOrdererTransfer2 
                )));

        verify(orderService, times(1)).getOrdersByTitle(SEARCH_QUERY);
        verify(ordererService, times(1)).getOrderersByUsername(SEARCH_QUERY);
    }

    @Test
    void searchByTitle_WithEmptyQuery_ShouldReturnSearchViewWithEmptyResults() throws Exception {
        mockMvc.perform(post("/search")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("query", EMPTY_QUERY)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SEARCH_VIEW_NAME))
                .andExpect(model().attributeExists("query", "orderList", "ordererList"))
                .andExpect(model().attribute("query", EMPTY_QUERY))
                .andExpect(model().attribute("orderList", hasSize(0)))
                .andExpect(model().attribute("ordererList", hasSize(0)));

        verify(orderService, times(1)).getOrdersByTitle(EMPTY_QUERY);
        verify(ordererService, times(1)).getOrderersByUsername(EMPTY_QUERY);
    }


    @Test
    void searchRefresh_ShouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isFound()) // 302 Redirect
                .andExpect(redirectedUrl(REDIRECT_HOME_URL));
    }

    @Test
    void getOrderAutocompleteResults_ShouldReturnAutocompleteFragmentWithMatches() throws Exception {
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", AUTOCOMPLETE_QUERY))
                .andExpect(status().isOk())
                .andExpect(view().name(AUTOCOMPLETE_FRAGMENT))
                .andExpect(model().attributeExists("orders", "orderers", "query"))
                .andExpect(model().attribute("query", AUTOCOMPLETE_QUERY))
                .andExpect(model().attribute("orders", hasSize(2)))
                .andExpect(model().attribute("orders", contains(
                        mockOrderAutocomplete1,
                        mockOrderAutocomplete2
                )))
                .andExpect(model().attribute("orderers", hasSize(2)))
                .andExpect(model().attribute("orderers", contains(
                        mockOrdererAutocompleteTransfer1,
                        mockOrdererAutocompleteTransfer2
                )));

        verify(orderService, times(1)).findTopOrdersStartingWith(AUTOCOMPLETE_QUERY, AUTOCOMPLETE_LIMIT);
        verify(ordererService, times(1)).findTopOrderersStartingWith(AUTOCOMPLETE_QUERY, AUTOCOMPLETE_LIMIT);
    }

    @Test
    void getOrderAutocompleteResults_ShouldReturnUniqueResults() throws Exception {
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", DUPLICATE_QUERY))
                .andExpect(status().isOk())
                .andExpect(view().name(AUTOCOMPLETE_FRAGMENT))
                .andExpect(model().attributeExists("orders", "orderers", "query"))
                .andExpect(model().attribute("query", DUPLICATE_QUERY))
                .andExpect(model().attribute("orders", hasSize(2)))
                .andExpect(model().attribute("orders", contains(
                        mockOrderAutocomplete1,
                        mockOrderAutocomplete2
                )))
                .andExpect(model().attribute("orderers", hasSize(2)))
                .andExpect(model().attribute("orderers", contains(
                        mockOrdererAutocompleteTransfer1,
                        mockOrdererAutocompleteTransfer2
                )));

        verify(orderService, times(1)).findTopOrdersStartingWith(DUPLICATE_QUERY, AUTOCOMPLETE_LIMIT);
        verify(ordererService, times(1)).findTopOrderersStartingWith(DUPLICATE_QUERY, AUTOCOMPLETE_LIMIT);
    }

    @Test
    void getOrderAutocompleteResults_WithNoMatches_ShouldReturnAutocompleteFragmentWithEmptyLists() throws Exception {
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", NO_MATCH_QUERY))
                .andExpect(status().isOk())
                .andExpect(view().name(AUTOCOMPLETE_FRAGMENT))
                .andExpect(model().attributeExists("orders", "orderers", "query"))
                .andExpect(model().attribute("query", NO_MATCH_QUERY))
                .andExpect(model().attribute("orders", hasSize(0)))
                .andExpect(model().attribute("orderers", hasSize(0)));

        verify(orderService, times(1)).findTopOrdersStartingWith(NO_MATCH_QUERY, AUTOCOMPLETE_LIMIT);
        verify(ordererService, times(1)).findTopOrderersStartingWith(NO_MATCH_QUERY, AUTOCOMPLETE_LIMIT);
    }

    @Test
    void getOrderAutocompleteResults_WithEmptyQuery_ShouldReturnEmptyFragment() throws Exception {
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", EMPTY_QUERY))
                .andExpect(status().isOk())
                .andExpect(view().name(EMPTY_FRAGMENT))
                .andExpect(model().attributeDoesNotExist("orders", "orderers", "query"));

        verify(orderService, never()).findTopOrdersStartingWith(anyString(), anyInt());
        verify(ordererService, never()).findTopOrderersStartingWith(anyString(), anyInt());
    }

    @Test
    void getOrderAutocompleteResults_WithWhitespaceQuery_ShouldReturnEmptyFragment() throws Exception {
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", WHITESPACE_QUERY))
                .andExpect(status().isOk())
                .andExpect(view().name(EMPTY_FRAGMENT))
                .andExpect(model().attributeDoesNotExist("orders", "orderers", "query"));

        verify(orderService, never()).findTopOrdersStartingWith(anyString(), anyInt());
        verify(ordererService, never()).findTopOrderersStartingWith(anyString(), anyInt());
    }

     @Test
    void getOrderAutocompleteResults_WithNullQuery_ShouldReturnEmptyFragment() throws Exception {
        mockMvc.perform(get("/api/search/autocomplete"))
                .andExpect(status().isOk())
                .andExpect(view().name(EMPTY_FRAGMENT))
                .andExpect(model().attributeDoesNotExist("orders", "orderers", "query"));

        verify(orderService, never()).findTopOrdersStartingWith(anyString(), anyInt());
        verify(ordererService, never()).findTopOrderersStartingWith(anyString(), anyInt());
    }

    @Test
    void getOrderAutocompleteResults_ShouldTrimQueryBeforeSearch() throws Exception {
        String queryWithWhitespace = "  " + AUTOCOMPLETE_QUERY + "  ";

        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", queryWithWhitespace))
                .andExpect(status().isOk())
                .andExpect(view().name(AUTOCOMPLETE_FRAGMENT))
                .andExpect(model().attributeExists("orders", "orderers", "query"))
                .andExpect(model().attribute("query", AUTOCOMPLETE_QUERY))
                .andExpect(model().attribute("orders", hasSize(2)))
                .andExpect(model().attribute("orders", contains(mockOrderAutocomplete1, mockOrderAutocomplete2)))
                .andExpect(model().attribute("orderers", hasSize(2)))
                .andExpect(model().attribute("orderers", contains(mockOrdererAutocompleteTransfer1, mockOrdererAutocompleteTransfer2)));


        verify(orderService, times(1)).findTopOrdersStartingWith(AUTOCOMPLETE_QUERY, AUTOCOMPLETE_LIMIT);
        verify(ordererService, times(1)).findTopOrderersStartingWith(AUTOCOMPLETE_QUERY, AUTOCOMPLETE_LIMIT);
        verify(orderService, never()).findTopOrdersStartingWith(queryWithWhitespace, AUTOCOMPLETE_LIMIT);
        verify(ordererService, never()).findTopOrderersStartingWith(queryWithWhitespace, AUTOCOMPLETE_LIMIT);
    }
}
