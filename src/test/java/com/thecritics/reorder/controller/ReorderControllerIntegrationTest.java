package com.thecritics.reorder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult; 

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
class ReorderControllerIntegrationTest {
    
    private static final int VALID_ORIGINAL_ORDER_ID = 1;
    private static final int NON_EXISTENT_ORDER_ID = 999999;
    private static final String SESSION_ATTR_USERNAME = "username"; 
    private static final String SESSION_ATTR_REORDER_STATE = "reOrderState";
    private static final String SESSION_ATTR_REORDER_ORIGINAL_ID = "reorderOriginalId";

    private static final String REORDER_TITLE = "Mi Reorden Válido";
    private static final String LOGGED_IN_USERNAME = "UsuarioReorder"; 
    private static final String ERROR_MSG_PROCESS = "Error inesperado al procesar. Intenta de nuevo.";
    private static final String ERROR_MSG_EMPTY_TITLE = "El título no puede estar vacío.";
    private static final String ERROR_MSG_NO_CHANGES = "No has realizado cambios en el contenido.";
    private static final String ERROR_MSG_SAVE = "Error inesperado al guardar el ReOrder.";
    private static final String SUCCESS_REDIRECT_URL_BASE = "/order/";
    private static final String HEADER_LOCATION = "Location";
    private static final String HEADER_HX_REDIRECT = "HX-Redirect";
    private static final String HEADER_HX_RETARGET = "HX-Retarget";
    private static final String HEADER_HX_RESWAP = "HX-Reswap";
    private static final String ERROR_TARGET_SELECTOR = "#reorder-error-message"; 
    private static final String ERROR_RESWAP_VALUE = "innerHTML"; 
    private static final String REORDER_VIEW_NAME = "reorder";
    private static final String ERROR_VIEW_NAME = "error";
    private static final String REDIRECT_ERROR_URL = "/error"; 

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession mockSession;
    private Order mockOriginalOrder;
    private Orderer mockOriginalOrderer;
    private Orderer mockUserForAuthentication; 
    private List<List<String>> originalContent;
    private List<List<String>> changedContent;
    private Order mockSavedReorder;

    @BeforeEach
    void setUp() {
        mockSession = new MockHttpSession();

        originalContent = Arrays.asList(
                Arrays.asList("Elemento C"),
                Arrays.asList("Elemento A", "Elemento B"));
        changedContent = Arrays.asList(
                Arrays.asList("Elemento B"),
                Arrays.asList("Elemento C", "Elemento A"));

        mockOriginalOrderer = new Orderer();
        mockOriginalOrderer.setId(1L);
        mockOriginalOrderer.setEmail("original@gmail.com");
        mockOriginalOrderer.setPassword("aaaAAA111");
        mockOriginalOrderer.setUsername("Autor Original");
        
        mockUserForAuthentication = new Orderer();
        mockUserForAuthentication.setId(2L);
        mockUserForAuthentication.setEmail("reorderer@gmail.com");
        mockUserForAuthentication.setPassword("bbbBBB222");
        mockUserForAuthentication.setUsername(LOGGED_IN_USERNAME); 

        mockOriginalOrder = new Order();
        mockOriginalOrder.setId(VALID_ORIGINAL_ORDER_ID);
        mockOriginalOrder.setTitle("Título Original");
        mockOriginalOrder.setAuthor(mockOriginalOrderer);
        mockOriginalOrder.setContent(originalContent);

        
        Orderer authorFromSession = new Orderer();
        authorFromSession.setUsername(LOGGED_IN_USERNAME);

        mockSavedReorder = new Order();
        mockSavedReorder.setId(100);
        mockSavedReorder.setTitle(REORDER_TITLE);
        mockSavedReorder.setAuthor(authorFromSession);
        mockSavedReorder.setContent(changedContent);
        mockSavedReorder.setReorderedOrder(mockOriginalOrder);

        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
                .thenReturn(mockOriginalOrder);
        when(orderService.getOrderById(NON_EXISTENT_ORDER_ID)).thenReturn(null);

        when(orderService.saveReOrder(
            anyString(),
            eq(LOGGED_IN_USERNAME),
            anyList(),
            any(Order.class)
        ))
            .thenReturn(mockSavedReorder.toTransfer());

        doAnswer(invocation -> {
            List<List<String>> newState = invocation.getArgument(0);
            HttpSession sessionArg = invocation.getArgument(1);
            if (sessionArg.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID) != null) {
                sessionArg.setAttribute(SESSION_ATTR_REORDER_STATE, newState);
            }
            return null;
        }).when(orderService).updateReOrderState(anyList(), any(HttpSession.class));

        mockSession.clearAttributes();
    }

    @Test
    void showReorderPage_ShouldOpenReorderViewAndSetSessionWhenSuccessful()
            throws Exception {
        mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        mockMvc
                .perform(get("/reorder")
                        .param("idInput", String.valueOf(VALID_ORIGINAL_ORDER_ID))
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername())))
                .andExpect(status().isOk())
                .andExpect(view().name(REORDER_VIEW_NAME))
                .andExpect(model().attributeExists("originalOrder"))
                .andExpect(model().attribute("originalOrder", mockOriginalOrder))
                .andExpect(model().attributeExists("reOrderState"))
                .andExpect(model().attribute("reOrderState", originalContent))
                .andExpect(model().attributeExists("u"))
                .andExpect(model().attribute("u", LOGGED_IN_USERNAME))
                .andExpect(request().sessionAttribute(
                        SESSION_ATTR_REORDER_ORIGINAL_ID,
                        is(VALID_ORIGINAL_ORDER_ID)))
                .andExpect(request().sessionAttribute(
                        SESSION_ATTR_REORDER_STATE,
                        is(originalContent)));

        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID))
                .isEqualTo(VALID_ORIGINAL_ORDER_ID);
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
                .isEqualTo(originalContent);
        assertThat(mockSession.getAttribute(SESSION_ATTR_USERNAME))
                .isEqualTo(LOGGED_IN_USERNAME);
    }

    @Test
    void showReorderPage_ShouldRedirectToErrorWhenOrderDoesNotExist()
            throws Exception {
        mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        mockMvc
                .perform(get("/reorder")
                        .param("idInput", String.valueOf(NON_EXISTENT_ORDER_ID))
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername())))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(REDIRECT_ERROR_URL));

        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID))
                .isNull();
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
                .isNull();

        verify(orderService, times(1)).getOrderById(NON_EXISTENT_ORDER_ID);
    }

    @Test
    void reorderUpdateState_ShouldUpdateSessionAndReturnReorderView() throws Exception {
        mockSession.setAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                VALID_ORIGINAL_ORDER_ID);
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, originalContent);

        String changedContentJson = objectMapper.writeValueAsString(
                changedContent);

        mockMvc
                .perform(post("/reorder/updateOrderState")
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("reOrderStateJson", changedContentJson))
                .andExpect(status().isOk())
                .andExpect(view().name(REORDER_VIEW_NAME))
                .andExpect(model().attributeExists("originalOrder"))
                .andExpect(model().attribute("originalOrder", mockOriginalOrder))
                .andExpect(model().attributeExists("reOrderState"))
                .andExpect(model().attribute("reOrderState", changedContent));

        verify(orderService, times(1))
                .updateReOrderState(eq(changedContent), eq(mockSession));
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
                .isEqualTo(changedContent);
        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
    }

    @Test
    void reorderUpdateState_ShouldReturnErrorViewIfSessionIdMissing()
            throws Exception {
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, originalContent);
        mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        String changedContentJson = objectMapper.writeValueAsString(
                changedContent);

        mockMvc
                .perform(post("/reorder/updateOrderState")
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("reOrderStateJson", changedContentJson))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW_NAME))
                .andExpect(model().attributeExists("globalError"));

        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
                .isEqualTo(originalContent);
        verify(orderService, never())
                .updateReOrderState(anyList(), any(HttpSession.class));
         verify(orderService, never()).getOrderById(anyInt());
    }

    @Test
    void reorderUpdateState_ShouldReturnErrorViewIfBodyInvalidJson()
            throws Exception {
        mockSession.setAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                VALID_ORIGINAL_ORDER_ID);
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, originalContent);
        mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        String invalidJson = "{\"key\": not_json}";

        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
                .thenReturn(mockOriginalOrder);

        mockMvc
                .perform(post("/reorder/updateOrderState")
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("reOrderStateJson", invalidJson))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW_NAME))
                .andExpect(model().attributeExists("globalError"))
                .andExpect(model().attributeExists("originalOrder"));

        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
                .isEqualTo(originalContent);
        verify(orderService, never())
                .updateReOrderState(anyList(), any(HttpSession.class));
        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
    }

     @Test
    void reorderUpdateState_ShouldReturnErrorViewIfDeserializedStateIsEmpty() throws Exception {
        mockSession.setAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                VALID_ORIGINAL_ORDER_ID);
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, originalContent);
        mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        List<List<String>> emptyContent = Collections.emptyList();
        String emptyContentJson = objectMapper.writeValueAsString(emptyContent);

        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
                .thenReturn(mockOriginalOrder);

        mockMvc
                .perform(post("/reorder/updateOrderState")
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("reOrderStateJson", emptyContentJson))
                .andExpect(status().isOk())
                .andExpect(view().name(ERROR_VIEW_NAME))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("originalOrder"))
                .andExpect(model().attribute("reOrderState", emptyContent));

        verify(orderService, never())
                .updateReOrderState(anyList(), any(HttpSession.class));
        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
                .isEqualTo(originalContent);
    }

    @Test
    void reorderPublishOrder_ShouldReturnBadRequestWhenTitleIsEmpty()
            throws Exception {
        mockSession.setAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                VALID_ORIGINAL_ORDER_ID);
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, changedContent);
        mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        MvcResult result = mockMvc
                .perform(post("/reorder/PublishOrder")
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("rtitle", "")
                        .param(
                                "originalOrderId",
                                String.valueOf(VALID_ORIGINAL_ORDER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HEADER_HX_RETARGET, ERROR_TARGET_SELECTOR))
                .andExpect(header().string(HEADER_HX_RESWAP, ERROR_RESWAP_VALUE))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(ERROR_MSG_EMPTY_TITLE);

        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
                .isNotNull();
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID))
                .isNotNull();

        verify(orderService, never())
                .saveReOrder(any(), any(), any(), any());
        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
    }

    @Test
    void reorderPublishOrder_ShouldReturnInternalServerErrorWhenSessionStateIsMissing()
            throws Exception {
        mockSession.setAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                VALID_ORIGINAL_ORDER_ID);
         mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        MvcResult result = mockMvc
                .perform(post("/reorder/PublishOrder")
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("rtitle", REORDER_TITLE)
                        .param(
                                "originalOrderId",
                                String.valueOf(VALID_ORIGINAL_ORDER_ID)))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string(HEADER_HX_RETARGET, ERROR_TARGET_SELECTOR))
                .andExpect(header().string(HEADER_HX_RESWAP, ERROR_RESWAP_VALUE))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(ERROR_MSG_PROCESS);

        verify(orderService, never())
                .saveReOrder(any(), any(), any(), any());
        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
    }

    @Test
    void reorderPublishOrder_ShouldReturnInternalServerErrorWhenOriginalOrderIsNotFound()
            throws Exception {
        mockSession.setAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                VALID_ORIGINAL_ORDER_ID);
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, changedContent);
        mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        when(orderService.getOrderById(NON_EXISTENT_ORDER_ID)).thenReturn(null);

        MvcResult result = mockMvc
                .perform(post("/reorder/PublishOrder")
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("rtitle", REORDER_TITLE)
                        .param(
                                "originalOrderId",
                                String.valueOf(NON_EXISTENT_ORDER_ID)))
                .andExpect(status().isInternalServerError()) 
                .andExpect(header().string(HEADER_HX_RETARGET, ERROR_TARGET_SELECTOR))
                .andExpect(header().string(HEADER_HX_RESWAP, ERROR_RESWAP_VALUE))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(ERROR_MSG_PROCESS);

        
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
                .isNotNull();

        verify(orderService, never()) 
                .saveReOrder(any(), any(), any(), any());
        verify(orderService, times(1)).getOrderById(NON_EXISTENT_ORDER_ID); 
    }

    @Test
    void reorderPublishOrder_ShouldReturnBadRequestWhenContentHasNotChanged()
            throws Exception {
        
        mockSession.setAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                VALID_ORIGINAL_ORDER_ID);
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, originalContent); 
        mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        MvcResult result = mockMvc
                .perform(post("/reorder/PublishOrder")
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("rtitle", REORDER_TITLE)
                        .param(
                                "originalOrderId",
                                String.valueOf(VALID_ORIGINAL_ORDER_ID)))
                .andExpect(status().isBadRequest()) 
                .andExpect(header().string(HEADER_HX_RETARGET, ERROR_TARGET_SELECTOR))
                .andExpect(header().string(HEADER_HX_RESWAP, ERROR_RESWAP_VALUE))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(ERROR_MSG_NO_CHANGES);

        
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
                .isNotNull();
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID))
                .isNotNull();

        verify(orderService, never()) 
                .saveReOrder(any(), any(), any(), any());
        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID); 
    }

    @Test
    void reorderPublishOrder_ShouldRedirectAndClearSessionWhenSuccessful()
            throws Exception {
        
        mockSession.setAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                VALID_ORIGINAL_ORDER_ID);
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, changedContent);
        mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        String titleWithSpaces = " " + REORDER_TITLE + " "; 

        mockMvc
                .perform(post("/reorder/PublishOrder")
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("rtitle", titleWithSpaces) 
                        .param(
                                "originalOrderId",
                                String.valueOf(VALID_ORIGINAL_ORDER_ID)))
                .andExpect(status().isFound()) 
                .andExpect(header().string( 
                        HEADER_LOCATION,
                        SUCCESS_REDIRECT_URL_BASE + mockSavedReorder.getId()))
                .andExpect(header().string( 
                        HEADER_HX_REDIRECT,
                        SUCCESS_REDIRECT_URL_BASE + mockSavedReorder.getId()))
                .andExpect(flash().attribute( 
                        "toastMessage",
                        "¡Tu ReOrder ha sido publicado correctamente!"))
                
                .andExpect(request().sessionAttribute(SESSION_ATTR_REORDER_STATE, is(nullValue())))
                .andExpect(request().sessionAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID, is(nullValue())));


        
        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
        
        verify(orderService, times(1)).saveReOrder(
                eq(REORDER_TITLE), 
                eq(LOGGED_IN_USERNAME), 
                eq(changedContent),
                eq(mockOriginalOrder));

        
        
        
        
         assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE)).isNull();
         assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID)).isNull();
         
         
    }

    @Test
    void reorderPublishOrder_ShouldReturnInternalServerErrorWhenSaveOperationFails()
            throws Exception {
        
        mockSession.setAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                VALID_ORIGINAL_ORDER_ID);
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, changedContent);
        mockSession.setAttribute(SESSION_ATTR_USERNAME, LOGGED_IN_USERNAME);

        
        when(orderService.saveReOrder(
                eq(REORDER_TITLE), 
                eq(LOGGED_IN_USERNAME), 
                eq(changedContent),
                eq(mockOriginalOrder)))
                .thenThrow(new RuntimeException("Database connection failed"));

        MvcResult result = mockMvc
                .perform(post("/reorder/PublishOrder")
                        .session(mockSession)
                        .with(user(mockUserForAuthentication.getUsername()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("rtitle", REORDER_TITLE)
                        .param(
                                "originalOrderId",
                                String.valueOf(VALID_ORIGINAL_ORDER_ID)))
                .andExpect(status().isInternalServerError()) 
                .andExpect(header().string(HEADER_HX_RETARGET, ERROR_TARGET_SELECTOR))
                .andExpect(header().string(HEADER_HX_RESWAP, ERROR_RESWAP_VALUE))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(ERROR_MSG_SAVE);

        
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
                .isNotNull();
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID))
                .isNotNull();

        
        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
        
        verify(orderService, times(1)).saveReOrder(
                eq(REORDER_TITLE),
                eq(LOGGED_IN_USERNAME),
                eq(changedContent),
                eq(mockOriginalOrder));
    }
}
