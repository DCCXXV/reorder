package com.thecritics.reorder.controller;

import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.service.OrderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

/**
 * Controlador para la búsqueda de Orders y sugerencias de autocompletado.
 */
@Controller
public class OrderSearchController {

    private static final Logger log = LogManager.getLogger(OrderSearchController.class);

    private final OrderService orderService;

    private static final int AUTOCOMPLETE_LIMIT = 5;

    public OrderSearchController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/search")
    public String searchByTitle(@RequestParam String query, Model model) {
        model.addAttribute("query", query);
        model.addAttribute("orderList", orderService.getOrdersByTitle(query));
        return "search";
    }

    @GetMapping("/search")
    public String searchRefresh(Model model) {
        return "redirect:/";
    }

    /**
     * Endpoint optimizado para el autocompletado de órdenes (vía HTMX GET).
     * Mapeado a /api/search/autocomplete para diferenciarlo de la búsqueda completa.
     *
     * @param query Texto de búsqueda parcial.
     * @param model Modelo de Thymeleaf.
     * @return Fragmento Thymeleaf con sugerencias de autocompletado.
     */
    @GetMapping("/api/search/autocomplete")
    public String getOrderAutocompleteResults(@RequestParam(required = false) String query, Model model) {
        String trimmedQuery = (query != null) ? query.trim() : "";
        log.debug("Buscando autocompletado para query: '{}'", trimmedQuery);

        if (trimmedQuery.isEmpty()) {
             log.debug("Query vacío para autocompletado, devolviendo fragmento vacío.");
             return "fragments/search :: empty"; 
        }

        List<Order> potentialMatches = orderService.findTopOrdersStartingWith(trimmedQuery, AUTOCOMPLETE_LIMIT);

        Map<String, Order> uniqueOrdersMap = new LinkedHashMap<>();
        for (Order order : potentialMatches) {
            uniqueOrdersMap.putIfAbsent(order.getTitle(), order);
        }
        List<Order> uniqueOrders = List.copyOf(uniqueOrdersMap.values());

        model.addAttribute("orders", uniqueOrders);
        model.addAttribute("query", trimmedQuery);

        log.debug("Devolviendo {} sugerencias para autocompletado.", uniqueOrders.size());
        return "fragments/search :: autocomplete";
    }
}
