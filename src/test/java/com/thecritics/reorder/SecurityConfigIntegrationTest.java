package com.thecritics.reorder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenUnauthenticated_thenRedirectToLogin() throws Exception {
        mockMvc.perform(get("/reorder"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void whenAuthenticated_thenCanAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/reorder"))
                .andExpect(status().isOk());
    }

    @Test
    void publicEndpointsShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/css/"))
                .andExpect(status().isOk());
    }
    

    @Test
    void whenH2ConsoleInDevProfile_thenAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/h2/"))
                .andExpect(status().isOk()); // Solo en perfil "dev"
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void whenAccessDenied_thenRedirectTo403Page() throws Exception {
        // Intentamos acceder a un recurso que necesita rol ADMIN (pero tú no configuraste roles específicos en filterChain)
        mockMvc.perform(get("/admin/secret"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void csrfShouldBeRequiredNormally() throws Exception {
        mockMvc.perform(post("/some-protected-endpoint"))
                .andExpect(status().isForbidden()); // falta token CSRF → 403
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void csrfShouldBeDisabledForKarateRequests() throws Exception {
        mockMvc.perform(post("/reorder/save")
                .header("X-Test-Framework", "Karate"))
                .andExpect(status().is4xxClientError()) // Puede ser 404 porque no existe endpoint real, PERO **no** da 403 Forbidden
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 403) {
                        throw new AssertionError("Expected NOT 403 Forbidden (CSRF ignored for Karate)");
                    }
                });
    }
}
