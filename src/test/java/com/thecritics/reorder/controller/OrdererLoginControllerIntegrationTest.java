package com.thecritics.reorder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrdererLoginControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Configura cualquier cosa necesaria para los tests si es necesario
    }

    @Test
    void login_ShouldReturnLoginView_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
            .andExpect(status().isOk()) // Verifica que la respuesta sea 200 OK
            .andExpect(view().name("login")); // Verifica que la vista sea 'login'
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void login_ShouldRedirectToHome_WhenAlreadyAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
            .andExpect(status().is3xxRedirection()) 
            .andExpect(redirectedUrl("/")); // Verifica que redirige a la página principal ("/")
    }
   

    @Test
    void login_ShouldReturnLoginView_WhenErrorOccurred() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login")
                .param("error", "true")) // Simula un error de login
            .andExpect(status().isOk()) // Verifica que la respuesta sea 200 OK
            .andExpect(view().name("login")) // Verifica que la vista es 'login'
            .andExpect(model().attribute("errorMessage", "Usuario o contraseña inválidos")); // Verifica que el mensaje de error esté presente
    }
}
