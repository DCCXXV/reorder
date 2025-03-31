package com.thecritics.reorder.controller;

import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.service.OrderService;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para visualizar detalles de un Order existente.
 */
@Controller
public class OrderViewController {

    private static final Logger log = LogManager.getLogger(OrderViewController.class);

    private final OrderService orderService;

    @Autowired
    public OrderViewController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Muestra la página de detalles de un Order específico.
     * @param id El ID del Order a mostrar.
     * @param fromQuery Término de búsqueda original (opcional, para volver a la búsqueda).
     * @param model El modelo para la vista.
     * @param session La sesión HTTP.
     * @return El nombre de la vista "order".
     */
    @GetMapping("/order/{id}")
    public String getOrderDetail(@PathVariable Integer id,
                                 @RequestParam(name = "fromQuery", required = false) String fromQuery,
                                 Model model, HttpSession session) {
        log.debug("Solicitando detalles para Order ID: {}", id);
        Order order = orderService.getOrderById(id);

        if (order == null) {
            log.warn("No se encontró Order con ID: {}", id);
            model.addAttribute("errorMessage", "Order no encontrado.");
            return "error"; 
        }

        model.addAttribute("order", order);

        if (fromQuery != null && !fromQuery.trim().isEmpty()) {
            String trimmedQuery = fromQuery.trim();
            log.debug("Recibido 'fromQuery': {}", trimmedQuery);
            model.addAttribute("searchQuery", trimmedQuery);
            session.setAttribute("searchQueryContext", trimmedQuery);
        } else {
            String sessionQuery = (String) session.getAttribute("searchQueryContext");
            if (sessionQuery != null) {
                 log.debug("Recuperado 'searchQueryContext' de sesión: {}", sessionQuery);
                 model.addAttribute("searchQuery", sessionQuery);
            }
        }

        return "order";
    }

    /**
     * Limpia el estado del Order, removiendo todos los elementos y restableciendo
     * los tiers iniciales.
     *
     * @param orderState El estado del Order a limpiar.
     * @return El estado del Order limpio, con dos tiers vacíos.
     */
    public List<List<String>> clearOrder(List<List<String>> orderState) {
        orderState.clear();
        orderState.add(new ArrayList<>());
        orderState.add(new ArrayList<>());
        return orderState;
    }
}
