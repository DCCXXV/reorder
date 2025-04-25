package com.thecritics.reorder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;

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
    void addSameSiteCookieAttribute_ShouldAddHeader_WhenHeaderExistsAndHasJSessionId() throws IOException {
        // Arrange
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getHeader("Set-Cookie")).thenReturn("JSESSIONID=abc123");

        // Act
      //  loginSuccessHandler.onAuthenticationSuccess(request, mockResponse, authentication);

        // Assert
        verify(mockResponse).setHeader(eq("Set-Cookie"), contains("SameSite=Lax"));
    }

    @Test
    void addSameSiteCookieAttribute_ShouldLogWarning_WhenNoJSessionId() throws IOException {
        // Arrange
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getHeader("Set-Cookie")).thenReturn("OTHER_COOKIE=value");

        // Act
       // loginSuccessHandler.onAuthenticationSuccess(request, mockResponse, authentication);

        // Assert
        verify(mockResponse, never()).setHeader(contains("Set-Cookie"), contains("SameSite=Lax"));
    }
}
