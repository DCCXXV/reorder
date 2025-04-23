
package com.thecritics.reorder;

import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrdererRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger log = LogManager.getLogger(LoginSuccessHandler.class);

    @Autowired 
    private HttpSession session;
    
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	
    @Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
	   
        addSameSiteCookieAttribute(response);

        String username = authentication.getName();
        request.getSession().setAttribute("username", username);

        log.info("User '{}' successfully authenticated. Session ID: {}",
            username, request.getSession().getId());

        String targetUrl = "/";

        log.debug("Redirecting authenticated user {} to {}", username, targetUrl);
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    /**
     * Adds SameSite=Lax attribute to the JSESSIONID cookie.
     * Adapt this if your session cookie name is different.
     * @param response The HttpServletResponse
     */
    private void addSameSiteCookieAttribute(HttpServletResponse response) {
        String header = response.getHeader("Set-Cookie");
        if (header != null && header.contains("JSESSIONID")) {
            response.setHeader("Set-Cookie", header + "; SameSite=Lax");
            log.debug("Added SameSite=Lax to JSESSIONID cookie.");
        } else {
            log.warn("Could not find JSESSIONID in Set-Cookie header to add SameSite attribute.");
        }
    }
}
