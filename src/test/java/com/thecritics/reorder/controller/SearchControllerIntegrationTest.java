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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerIntegrationTest {

    private static final String SEARCH_QUERY = "Test Query";
    private static final String EMPTY_QUERY = "";
    private static final String WHITESPACE_QUERY = "   ";
    private static final String AUTOCOMPLETE_QUERY = "Tes";
    private static final int AUTOCOMPLETE_LIMIT = 5;

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
    private Order mockOrderAutocomplete1;
    private Order mockOrderAutocomplete2;
    private Order mockOrderAutocompleteDuplicateTitle;
    private Orderer mockOrderer1;
    private Orderer mockOrderer2;

    @BeforeEach
    void setUp() {
        mockOrderer1 = new Orderer();
        mockOrderer1.setId(101);
        mockOrderer1.setUsername("Test Query User 1");

        mockOrderer2 = new Orderer();
        mockOrderer2.setId(102);
        mockOrderer2.setUsername("Another Test User");

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
        // --- Setup para Mocks de Servicios ---

        // Para POST /search
        when(orderService.getOrdersByTitle(eq(SEARCH_QUERY)))
                .thenReturn(Arrays.asList(mockOrder1, mockOrder2));
        when(ordererService.getOrderersByUsername(eq(SEARCH_QUERY)))
                .thenReturn(Arrays.asList(mockOrderer1.toTransfer(), mockOrderer2.toTransfer()));
        when(orderService.getOrdersByTitle(eq(EMPTY_QUERY)))
                .thenReturn(Collections.emptyList());
        when(ordererService.getOrderersByUsername(eq(EMPTY_QUERY)))
                .thenReturn(Collections.emptyList());

        // Para GET /api/search/autocomplete
        when(orderService.findTopOrdersStartingWith(eq(AUTOCOMPLETE_QUERY), eq(AUTOCOMPLETE_LIMIT)))
                .thenReturn(Arrays.asList(mockOrderAutocomplete1, mockOrderAutocomplete2));
        // Caso con duplicados
        when(orderService.findTopOrdersStartingWith(eq("Duplicate"), eq(AUTOCOMPLETE_LIMIT)))
                .thenReturn(Arrays.asList(mockOrderAutocomplete1, mockOrderAutocompleteDuplicateTitle, mockOrderAutocomplete2));
        // Caso sin resultados
         when(orderService.findTopOrdersStartingWith(eq("NoMatch"), eq(AUTOCOMPLETE_LIMIT)))
                .thenReturn(Collections.emptyList());
    }

    // --- Tests para POST /search ---

    @Test
    void searchByTitle_ShouldReturnSearchViewWithResults() throws Exception {
        mockMvc.perform(post("/search")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("query", SEARCH_QUERY)
                        .with(csrf())) // Importante añadir CSRF para POST
                .andExpect(status().isOk())
                .andExpect(view().name(SEARCH_VIEW_NAME))
                .andExpect(model().attributeExists("query", "orderList", "ordererList"))
                .andExpect(model().attribute("query", SEARCH_QUERY))
                .andExpect(model().attribute("orderList", hasSize(2)))
                .andExpect(model().attribute("orderList", containsInAnyOrder(mockOrder1, mockOrder2)))
                .andExpect(model().attribute("ordererList", hasSize(2)))
                .andExpect(model().attribute("ordererList", containsInAnyOrder(mockOrderer1, mockOrderer2)));

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

    // --- Test para GET /search ---

    @Test
    void searchRefresh_ShouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isFound()) // 302 Redirect
                .andExpect(redirectedUrl(REDIRECT_HOME_URL));
    }

    // --- Tests para GET /api/search/autocomplete ---

    @Test
    void getOrderAutocompleteResults_ShouldReturnAutocompleteFragmentWithMatches() throws Exception {
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", AUTOCOMPLETE_QUERY))
                .andExpect(status().isOk())
                .andExpect(view().name(AUTOCOMPLETE_FRAGMENT))
                .andExpect(model().attributeExists("orders", "query"))
                .andExpect(model().attribute("query", AUTOCOMPLETE_QUERY))
                .andExpect(model().attribute("orders", hasSize(2)))
                .andExpect(model().attribute("orders", contains(
                        mockOrderAutocomplete1,
                        mockOrderAutocomplete2
                )));

        verify(orderService, times(1)).findTopOrdersStartingWith(AUTOCOMPLETE_QUERY, AUTOCOMPLETE_LIMIT);
    }

     @Test
    void getOrderAutocompleteResults_ShouldReturnUniqueOrdersByTitle() throws Exception {
        String duplicateQuery = "Duplicate";
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", duplicateQuery))
                .andExpect(status().isOk())
                .andExpect(view().name(AUTOCOMPLETE_FRAGMENT))
                .andExpect(model().attributeExists("orders", "query"))
                .andExpect(model().attribute("query", duplicateQuery))
                // Esperamos 2 resultados únicos aunque el servicio devolvió 3 (2 con el mismo título)
                .andExpect(model().attribute("orders", hasSize(2)))
                // Verifica que el primer título 'Test Autocomplete A' aparezca solo una vez
                .andExpect(model().attribute("orders", contains(
                        mockOrderAutocomplete1, // La primera instancia de "Test Autocomplete A"
                        mockOrderAutocomplete2  // La única instancia de "Test Autocomplete B"
                )));

        verify(orderService, times(1)).findTopOrdersStartingWith(duplicateQuery, AUTOCOMPLETE_LIMIT);
    }

    @Test
    void getOrderAutocompleteResults_WithNoMatches_ShouldReturnAutocompleteFragmentWithEmptyList() throws Exception {
        String noMatchQuery = "NoMatch";
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", noMatchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name(AUTOCOMPLETE_FRAGMENT))
                .andExpect(model().attributeExists("orders", "query"))
                .andExpect(model().attribute("query", noMatchQuery))
                .andExpect(model().attribute("orders", hasSize(0)));

        verify(orderService, times(1)).findTopOrdersStartingWith(noMatchQuery, AUTOCOMPLETE_LIMIT);
    }

    @Test
    void getOrderAutocompleteResults_WithEmptyQuery_ShouldReturnEmptyFragment() throws Exception {
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", EMPTY_QUERY))
                .andExpect(status().isOk())
                .andExpect(view().name(EMPTY_FRAGMENT))
                .andExpect(model().attributeDoesNotExist("orders", "query")); // No debería haber atributos en el modelo

        verify(orderService, never()).findTopOrdersStartingWith(anyString(), anyInt()); // El servicio no debe ser llamado
    }

    @Test
    void getOrderAutocompleteResults_WithWhitespaceQuery_ShouldReturnEmptyFragment() throws Exception {
        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", WHITESPACE_QUERY))
                .andExpect(status().isOk())
                .andExpect(view().name(EMPTY_FRAGMENT))
                .andExpect(model().attributeDoesNotExist("orders", "query"));

        verify(orderService, never()).findTopOrdersStartingWith(anyString(), anyInt()); // El servicio no debe ser llamado
    }

     @Test
    void getOrderAutocompleteResults_WithNullQuery_ShouldReturnEmptyFragment() throws Exception {
        // No se pasa el parámetro "query"
        mockMvc.perform(get("/api/search/autocomplete"))
                .andExpect(status().isOk())
                .andExpect(view().name(EMPTY_FRAGMENT))
                .andExpect(model().attributeDoesNotExist("orders", "query"));

        verify(orderService, never()).findTopOrdersStartingWith(anyString(), anyInt());
    }

    @Test
    void getOrderAutocompleteResults_ShouldTrimQueryBeforeSearch() throws Exception {
        String queryWithWhitespace = "  " + AUTOCOMPLETE_QUERY + "  ";
        // Aseguramos que el mock esté configurado para el valor *trimmeado*
        when(orderService.findTopOrdersStartingWith(eq(AUTOCOMPLETE_QUERY), eq(AUTOCOMPLETE_LIMIT)))
                .thenReturn(Arrays.asList(mockOrderAutocomplete1));

        mockMvc.perform(get("/api/search/autocomplete")
                        .param("query", queryWithWhitespace))
                .andExpect(status().isOk())
                .andExpect(view().name(AUTOCOMPLETE_FRAGMENT))
                 // El modelo debería tener la query trimmeada
                .andExpect(model().attribute("query", AUTOCOMPLETE_QUERY))
                .andExpect(model().attribute("orders", hasSize(1)));

        // Verificamos que el servicio fue llamado con la query *trimmeada*
        verify(orderService, times(1)).findTopOrdersStartingWith(AUTOCOMPLETE_QUERY, AUTOCOMPLETE_LIMIT);
         // Verificamos que NO fue llamado con la query original con espacios
        verify(orderService, never()).findTopOrdersStartingWith(queryWithWhitespace, AUTOCOMPLETE_LIMIT);
    }
}