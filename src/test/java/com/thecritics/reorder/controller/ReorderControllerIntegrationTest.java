package com.thecritics.reorder.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReorderControllerIntegrationTest {

    private static final int VALID_ORIGINAL_ORDER_ID = 1;
    private static final int NON_EXISTENT_ORDER_ID = 999999;
    private static final String SESSION_ATTR_REORDER_STATE = "reOrderState";
    private static final String SESSION_ATTR_REORDER_ORIGINAL_ID = "reorderOriginalId";

    private static final String REORDER_TITLE = "Mi Reorden Válido";
    private static final String REORDER_AUTHOR = "Autor Prueba";
    private static final String ANONYMOUS_AUTHOR = "Anónimo";
    private static final String ERROR_MSG_PROCESS =
        "Error inesperado al procesar. Intenta de nuevo.";
    private static final String ERROR_MSG_EMPTY_TITLE =
        "El título no puede estar vacío.";
    private static final String ERROR_MSG_NO_CHANGES =
        "No has realizado cambios en el contenido.";
    private static final String ERROR_MSG_SAVE =
        "Error inesperado al guardar el ReOrder.";
    private static final String SUCCESS_REDIRECT_URL_BASE = "/order/";
    private static final String HEADER_LOCATION = "Location";
    private static final String HEADER_HX_REDIRECT = "HX-Redirect";
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
    private Orderer mockOriginalReOrderer;
    private List<List<String>> originalContent;
    private List<List<String>> changedContent;
    private Order mockSavedReorder;

    @BeforeEach
    void setUp() {
        mockSession = new MockHttpSession();

        originalContent = Arrays.asList(
            Arrays.asList("Elemento C"),
            Arrays.asList("Elemento A", "Elemento B")
        );
        changedContent = Arrays.asList(
            Arrays.asList("Elemento B"),
            Arrays.asList("Elemento C", "Elemento A")
        );

        mockOriginalOrderer = new Orderer();
        mockOriginalOrderer.setId(VALID_ORIGINAL_ORDER_ID);
        mockOriginalOrderer.setEmail("usuario@gmail.com");
        mockOriginalOrderer.setPassword("aaaAAA111");
        mockOriginalOrderer.setUsername("Autor Original");

        mockOriginalOrder = new Order();
        mockOriginalOrder.setId(VALID_ORIGINAL_ORDER_ID);
        mockOriginalOrder.setTitle("Título Original");
        mockOriginalOrder.setAuthor(mockOriginalOrderer);
        mockOriginalOrder.setContent(originalContent);

        mockOriginalReOrderer = new Orderer();
        mockOriginalReOrderer.setId(VALID_ORIGINAL_ORDER_ID);
        mockOriginalReOrderer.setEmail("usuario1@gmail.com");
        mockOriginalReOrderer.setPassword("aaaAAA111");
        mockOriginalReOrderer.setUsername(REORDER_AUTHOR);
        
        mockSavedReorder = new Order();
        mockSavedReorder.setId(100);
        mockSavedReorder.setTitle(REORDER_TITLE);
        mockSavedReorder.setAuthor(mockOriginalReOrderer);
        mockSavedReorder.setContent(changedContent);
        mockSavedReorder.setReorderedOrder(mockOriginalOrder);

        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);
        when(orderService.getOrderById(NON_EXISTENT_ORDER_ID)).thenReturn(null);
        when(orderService.saveReOrder(
            anyString(),
            anyString(),
            anyList(),
            any(Order.class)
        ))
            .thenReturn(mockSavedReorder.toTransfer());

        when(orderService.updateReOrderState(anyList(), any(HttpSession.class)))
            .thenAnswer(invocation -> {
                List<List<String>> newState = invocation.getArgument(0);
                HttpSession sessionArg = invocation.getArgument(1);
                if (sessionArg.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID) !=
                    null) {
                    sessionArg.setAttribute(SESSION_ATTR_REORDER_STATE, newState);
                }
                return newState;
            });

        mockSession.removeAttribute(SESSION_ATTR_REORDER_STATE);
        mockSession.removeAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID);
    }

    @Test
    void reorderShowReorderPage_ShouldOpenReorderViewAndSetSessionWhenSuccessful()
        throws Exception {
        mockMvc
            .perform(get("/reorder")
                .param("idInput", String.valueOf(VALID_ORIGINAL_ORDER_ID))
                .session(mockSession))
            .andExpect(status().isOk())
            .andExpect(view().name(REORDER_VIEW_NAME))
            .andExpect(model().attributeExists("originalOrder"))
            .andExpect(model().attribute("originalOrder", mockOriginalOrder))
            .andExpect(model().attributeExists("reOrderState"))
            .andExpect(model().attribute("reOrderState", originalContent))
            .andExpect(request().sessionAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                is(VALID_ORIGINAL_ORDER_ID)
            ))
            .andExpect(request().sessionAttribute(
                SESSION_ATTR_REORDER_STATE,
                is(originalContent)
            ));

        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID))
            .isEqualTo(VALID_ORIGINAL_ORDER_ID);
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
            .isEqualTo(originalContent);
    }

    @Test
    void reorderShowReorderPage_ShouldReturnNotFoundWhenOrderDoesNotExist()
        throws Exception {
        mockMvc
            .perform(get("/reorder")
                .param("idInput", String.valueOf(NON_EXISTENT_ORDER_ID))
                .with(csrf())
                .session(mockSession))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl(REDIRECT_ERROR_URL));

        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID))
            .isNull();
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
            .isNull();

        verify(orderService, times(1)).getOrderById(NON_EXISTENT_ORDER_ID);
    }

    @Test
    void reorderUpdateState_ShouldUpdateSessionAndReturnOk() throws Exception {
        mockSession.setAttribute(
            SESSION_ATTR_REORDER_ORIGINAL_ID,
            VALID_ORIGINAL_ORDER_ID
        );
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, originalContent);

        String changedContentJson = objectMapper.writeValueAsString(
            changedContent
        );

        mockMvc
            .perform(post("/reorder/updateOrderState")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("reOrderStateJson", changedContentJson))
            .andExpect(status().isOk())
            .andExpect(view().name(REORDER_VIEW_NAME))
            .andExpect(model().attributeExists("originalOrder"))
            .andExpect(model().attributeExists("reOrderState"))
            .andExpect(model().attribute("reOrderState", changedContent));

        verify(orderService, times(1))
            .updateReOrderState(eq(changedContent), eq(mockSession));

        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
            .isEqualTo(changedContent);
    }

    @Test
    void reorderUpdateState_ShouldReturnErrorIfSessionStateMissing()
        throws Exception {
        mockSession.setAttribute(
            SESSION_ATTR_REORDER_ORIGINAL_ID,
            VALID_ORIGINAL_ORDER_ID
        );

        String changedContentJson = objectMapper.writeValueAsString(
            changedContent
        );

        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);

        mockMvc
            .perform(post("/reorder/updateOrderState")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("reOrderStateJson", changedContentJson))
            .andExpect(status().isOk())
            .andExpect(view().name(REORDER_VIEW_NAME))
            .andExpect(model().attributeExists("originalOrder"))
            .andExpect(model().attributeExists("reOrderState"))
            .andExpect(model().attribute("reOrderState", changedContent));

        verify(orderService, times(1))
            .updateReOrderState(eq(changedContent), eq(mockSession));
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
            .isEqualTo(changedContent);
    }

    @Test
    void reorderUpdateState_ShouldReturnErrorIfSessionIdMissing()
        throws Exception {
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, originalContent);

        String changedContentJson = objectMapper.writeValueAsString(
            changedContent
        );

        mockMvc
            .perform(post("/reorder/updateOrderState")
                .session(mockSession)
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
    }

    @Test
    void reorderUpdateState_ShouldReturnBadRequestIfBodyInvalid()
        throws Exception {
        mockSession.setAttribute(
            SESSION_ATTR_REORDER_ORIGINAL_ID,
            VALID_ORIGINAL_ORDER_ID
        );
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, originalContent);

        String invalidJson = "{\"key\": not_json}";

        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);

        mockMvc
            .perform(post("/reorder/updateOrderState")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("reOrderStateJson", invalidJson))
            .andExpect(status().isOk())
            .andExpect(view().name(ERROR_VIEW_NAME))
            .andExpect(model().attributeExists("globalError"));

        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
            .isEqualTo(originalContent);
        verify(orderService, never())
            .updateReOrderState(anyList(), any(HttpSession.class));
    }

    @Test
    void reorderUpdateState_ShouldHandleEmptyContentUpdate() throws Exception {
        mockSession.setAttribute(
            SESSION_ATTR_REORDER_ORIGINAL_ID,
            VALID_ORIGINAL_ORDER_ID
        );
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, originalContent);

        List<List<String>> emptyContent = Collections.emptyList();
        String emptyContentJson = objectMapper.writeValueAsString(emptyContent);

        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);

        mockMvc
            .perform(post("/reorder/updateOrderState")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("reOrderStateJson", emptyContentJson))
            .andExpect(status().isOk())
            .andExpect(view().name(ERROR_VIEW_NAME))
            .andExpect(model().attributeExists("errorMessage"))
            .andExpect(model().attribute("reOrderState", emptyContent));

        verify(orderService, never())
            .updateReOrderState(anyList(), any(HttpSession.class));
    }

    @Test
    void reorderPublishOrder_ShouldReturnBadRequestWhenTitleIsEmpty()
        throws Exception {
        mockSession.setAttribute(
            SESSION_ATTR_REORDER_ORIGINAL_ID,
            VALID_ORIGINAL_ORDER_ID
        );
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, changedContent);
        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);

        mockMvc
            .perform(post("/reorder/PublishOrder")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("rtitle", "")
                .param("rauthor", REORDER_AUTHOR)
                .param(
                    "originalOrderId",
                    String.valueOf(VALID_ORIGINAL_ORDER_ID)
                ))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(ERROR_MSG_EMPTY_TITLE));

        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
            .isNotNull();
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID))
            .isNotNull();

        verify(orderService, never())
            .saveReOrder(any(), any(), any(), any());
    }

    @Test
    void reorderPublishOrder_ShouldReturnInternalServerErrorWhenSessionStateIsMissing()
        throws Exception {
        mockSession.setAttribute(
            SESSION_ATTR_REORDER_ORIGINAL_ID,
            VALID_ORIGINAL_ORDER_ID
        );
        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);

        mockMvc
            .perform(post("/reorder/PublishOrder")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("rtitle", REORDER_TITLE)
                .param("rauthor", REORDER_AUTHOR)
                .param(
                    "originalOrderId",
                    String.valueOf(VALID_ORIGINAL_ORDER_ID)
                ))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(ERROR_MSG_PROCESS));

        verify(orderService, never())
            .saveReOrder(any(), any(), any(), any());
    }

    @Test
    void reorderPublishOrder_ShouldReturnInternalServerErrorWhenOriginalOrderIsNotFound()
        throws Exception {
        mockSession.setAttribute(
            SESSION_ATTR_REORDER_ORIGINAL_ID,
            NON_EXISTENT_ORDER_ID
        );
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, changedContent);
        when(orderService.getOrderById(NON_EXISTENT_ORDER_ID)).thenReturn(null);

        mockMvc
            .perform(post("/reorder/PublishOrder")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("rtitle", REORDER_TITLE)
                .param("rauthor", REORDER_AUTHOR)
                .param(
                    "originalOrderId",
                    String.valueOf(NON_EXISTENT_ORDER_ID)
                ))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(ERROR_MSG_PROCESS));

        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
            .isNotNull();
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID))
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
            VALID_ORIGINAL_ORDER_ID
        );
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, originalContent);
        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);

        mockMvc
            .perform(post("/reorder/PublishOrder")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("rtitle", REORDER_TITLE)
                .param("rauthor", REORDER_AUTHOR)
                .param(
                    "originalOrderId",
                    String.valueOf(VALID_ORIGINAL_ORDER_ID)
                ))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(ERROR_MSG_NO_CHANGES));

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
            VALID_ORIGINAL_ORDER_ID
        );
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, changedContent);
        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);

        mockMvc
            .perform(post("/reorder/PublishOrder")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("rtitle", " " + REORDER_TITLE + " ")
                .param("rauthor", " " + REORDER_AUTHOR + " ")
                .param(
                    "originalOrderId",
                    String.valueOf(VALID_ORIGINAL_ORDER_ID)
                ))
            .andExpect(status().isFound())
            .andExpect(header().string(
                HEADER_LOCATION,
                SUCCESS_REDIRECT_URL_BASE + mockSavedReorder.getId()
            ))
            .andExpect(header().string(
                HEADER_HX_REDIRECT,
                SUCCESS_REDIRECT_URL_BASE + mockSavedReorder.getId()
            ))
            .andExpect(flash().attribute(
                "toastMessage",
                "¡Tu ReOrder ha sido publicado correctamente!"
            ))
            .andExpect(request().sessionAttribute(
                SESSION_ATTR_REORDER_STATE,
                is(nullValue())
            ))
            .andExpect(request().sessionAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                is(nullValue())
            ));

        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
        verify(orderService, times(1)).saveReOrder(
            eq(REORDER_TITLE),
            eq(REORDER_AUTHOR),
            eq(changedContent),
            eq(mockOriginalOrder)
        );
    }

    @Test
    void reorderPublishOrder_ShouldUseAnonymousAndSucceedWhenAuthorIsEmpty()
        throws Exception {
        mockSession.setAttribute(
            SESSION_ATTR_REORDER_ORIGINAL_ID,
            VALID_ORIGINAL_ORDER_ID
        );
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, changedContent);
        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);

        mockMvc
            .perform(post("/reorder/PublishOrder")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("rtitle", REORDER_TITLE)
                .param("rauthor", "")
                .param(
                    "originalOrderId",
                    String.valueOf(VALID_ORIGINAL_ORDER_ID)
                ))
            .andExpect(status().isFound())
            .andExpect(header().string(
                HEADER_LOCATION,
                SUCCESS_REDIRECT_URL_BASE + mockSavedReorder.getId()
            ))
            .andExpect(header().string(
                HEADER_HX_REDIRECT,
                SUCCESS_REDIRECT_URL_BASE + mockSavedReorder.getId()
            ))
            .andExpect(flash().attributeExists("toastMessage"))
            .andExpect(request().sessionAttribute(
                SESSION_ATTR_REORDER_STATE,
                is(nullValue())
            ))
            .andExpect(request().sessionAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                is(nullValue())
            ));

        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
        verify(orderService, times(1)).saveReOrder(
            eq(REORDER_TITLE),
            eq(ANONYMOUS_AUTHOR),
            eq(changedContent),
            eq(mockOriginalOrder)
        );
    }

    @Test
    void reorderPublishOrder_ShouldUseAnonymousAndSucceedWhenAuthorIsNull()
        throws Exception {
        mockSession.setAttribute(
            SESSION_ATTR_REORDER_ORIGINAL_ID,
            VALID_ORIGINAL_ORDER_ID
        );
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, changedContent);
        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);

        mockMvc
            .perform(post("/reorder/PublishOrder")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("rtitle", REORDER_TITLE)
                .param(
                    "originalOrderId",
                    String.valueOf(VALID_ORIGINAL_ORDER_ID)
                ))
            .andExpect(status().isFound())
            .andExpect(header().string(
                HEADER_LOCATION,
                SUCCESS_REDIRECT_URL_BASE + mockSavedReorder.getId()
            ))
            .andExpect(header().string(
                HEADER_HX_REDIRECT,
                SUCCESS_REDIRECT_URL_BASE + mockSavedReorder.getId()
            ))
            .andExpect(flash().attributeExists("toastMessage"))
            .andExpect(request().sessionAttribute(
                SESSION_ATTR_REORDER_STATE,
                is(nullValue())
            ))
            .andExpect(request().sessionAttribute(
                SESSION_ATTR_REORDER_ORIGINAL_ID,
                is(nullValue())
            ));

        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
        verify(orderService, times(1)).saveReOrder(
            eq(REORDER_TITLE),
            eq(ANONYMOUS_AUTHOR),
            eq(changedContent),
            eq(mockOriginalOrder)
        );
    }

    @Test
    void reorderPublishOrder_ShouldReturnInternalServerErrorWhenSaveOperationFails()
        throws Exception {
        mockSession.setAttribute(
            SESSION_ATTR_REORDER_ORIGINAL_ID,
            VALID_ORIGINAL_ORDER_ID
        );
        mockSession.setAttribute(SESSION_ATTR_REORDER_STATE, changedContent);
        when(orderService.getOrderById(VALID_ORIGINAL_ORDER_ID))
            .thenReturn(mockOriginalOrder);
        when(orderService.saveReOrder(
            anyString(),
            anyString(),
            anyList(),
            any(Order.class)
        ))
            .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc
            .perform(post("/reorder/PublishOrder")
                .session(mockSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(csrf())
                .param("rtitle", REORDER_TITLE)
                .param("rauthor", REORDER_AUTHOR)
                .param(
                    "originalOrderId",
                    String.valueOf(VALID_ORIGINAL_ORDER_ID)
                ))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(ERROR_MSG_SAVE));

        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_STATE))
            .isNotNull();
        assertThat(mockSession.getAttribute(SESSION_ATTR_REORDER_ORIGINAL_ID))
            .isNotNull();

        verify(orderService, times(1)).getOrderById(VALID_ORIGINAL_ORDER_ID);
        verify(orderService, times(1)).saveReOrder(
            eq(REORDER_TITLE),
            eq(REORDER_AUTHOR),
            eq(changedContent),
            eq(mockOriginalOrder)
        );
    }
    
    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        // Arrange
        String validCredentials = """
            {
                "username": "testuser",
                "password": "password123"
            }
        """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCredentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        String invalidCredentials = """
            {
                "username": "invaliduser",
                "password": "wrongpassword"
            }
        """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCredentials))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenRequestBodyIsInvalid() throws Exception {
        // Arrange
        String invalidRequestBody = """
            {
                "username": "testuser"
                // Missing password field
            }
        """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestBody))
                .andExpect(status().isBadRequest());
    }
}
