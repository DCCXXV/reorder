package com.thecritics.reorder.controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrdererRepository;
import com.thecritics.reorder.service.OrdererService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/login")
public class OrdererLoginController {

    private static final Logger log = LogManager.getLogger(OrdererLoginController.class);

    @Autowired
    private OrdererService ordererService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Maneja las solicitudes GET para mostrar la página de inicio de sesión (login).
     * También maneja la visualización de un mensaje de error si un intento de inicio de sesión anterior falló.
     * Spring Security redirige a "/login?error" en caso de fallo de autenticación.
     *
     * @param model El modelo de UI de Spring.
     * @param error Parámetro que indica un error de inicio de sesión (poblado automáticamente por Spring Security).
     * @return El nombre de la plantilla de vista de inicio de sesión ("login").
     */
    @GetMapping
    public String login(Model model, @RequestParam(value = "error", required = false) String error,  Authentication authentication){
        if (error != null) {
            model.addAttribute("errorMessage", "Usuario o contraseña inválidos");
        }

        if (authentication != null && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken)) {
        log.debug("Usuario {} ya autenticado, redirigiendo al home", authentication.getName());
        return "redirect:/";
    }

        return "login";
    }
}