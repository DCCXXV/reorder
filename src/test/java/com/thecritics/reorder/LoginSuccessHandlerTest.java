package com.thecritics.reorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
public class LoginSuccessHandlerTest {

    @InjectMocks
    private LoginSuccessHandler loginSuccessHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginSuccessHandler = new LoginSuccessHandler();
    }

    @Test
    void onAuthenticationSuccess_ShouldSetUsernameInSession_AndRedirect() throws Exception {
        // Arrange
        when(authentication.getName()).thenReturn("juan");
        when(request.getSession()).thenReturn(session);
        when(response.getHeader("Set-Cookie")).thenReturn("JSESSIONID=abc123");
        // Act
        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        // Assert
        verify(session).setAttribute("username", "juan");
        verify(response).setHeader(eq("Set-Cookie"), contains("SameSite=Lax"));
    }

    @Test
    void addSameSiteCookieAttribute_ShouldAddSameSiteLax_WhenJSessionIdExists() {
        // Arrange
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getHeader("Set-Cookie")).thenReturn("JSESSIONID=abc123");

        // Act
        loginSuccessHandler.addSameSiteCookieAttribute(response);

        // Assert
        verify(response).setHeader(eq("Set-Cookie"), contains("SameSite=Lax"));
    }

    @Test
    void addSameSiteCookieAttribute_ShouldNotAddSameSiteLax_WhenJSessionIdDoesNotExist() {
        // Arrange
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getHeader("Set-Cookie")).thenReturn("OTHER_COOKIE=xyz456");

        // Act
        loginSuccessHandler.addSameSiteCookieAttribute(response);

        // Assert
        verify(response, never()).setHeader(anyString(), anyString());
    }

    @Test
    void addSameSiteCookieAttribute_ShouldNotAddSameSiteLax_WhenHeaderIsNull() {
        // Arrange
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getHeader("Set-Cookie")).thenReturn(null);

        // Act
        loginSuccessHandler.addSameSiteCookieAttribute(response);

        // Assert
        verify(response, never()).setHeader(anyString(), anyString());
    }
    
}
