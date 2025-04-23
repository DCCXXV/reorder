package com.thecritics.reorder.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controlador para las rutas generales y la página de inicio.
 */
@Controller
public class HomeController {

    private static final Logger log = LogManager.getLogger(HomeController.class);

    /**
     * Añade atributos comunes al modelo desde la sesión HTTP.
     * Aplica a todos los métodos de este controlador (y potencialmente otros si se mueve a @ControllerAdvice).
     * @param session La sesión HTTP actual.
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     */
    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws" }) {
            if (session.getAttribute(name) != null) {
                model.addAttribute(name, session.getAttribute(name));
            }
        }
    }

    /**
     * Maneja las solicitudes GET a la ruta raíz "/".
     * @param model El objeto Modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista "index".
     */
    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }
}
