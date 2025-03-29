package com.thecritics.reorder.controller;

import com.thecritics.reorder.model.Order; // O una Proyección/DTO si la usas
import com.thecritics.reorder.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap; // Para mantener un orden si es necesario

@Controller
@RequestMapping("/api/search")
public class OrderSearchController {

    private final OrderService orderService;
    // Define un límite razonable para las sugerencias de autocompletado
    private static final int AUTOCOMPLETE_LIMIT = 10;

    public OrderSearchController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Endpoint optimizado para el autocompletado de órdenes.
     *
     * @param query Texto de búsqueda
     * @param model Modelo de Thymeleaf
     * @return Fragmento Thymeleaf con resultados
     */
    @GetMapping("/autocomplete")
    public String getOrderAutocompleteResults(@RequestParam(required = false) String query, Model model) {
        if (query == null || query.isBlank()) {
             return "fragments/search :: empty";
        }

        List<Order> potentialMatches = orderService.findTopOrdersStartingWith(query, AUTOCOMPLETE_LIMIT);

        Map<String, Order> uniqueOrdersMap = new LinkedHashMap<>();
        for (Order order : potentialMatches) {
            uniqueOrdersMap.putIfAbsent(order.getTitle(), order);
        }

        List<Order> uniqueOrders = List.copyOf(uniqueOrdersMap.values());

        model.addAttribute("orders", uniqueOrders);
        model.addAttribute("query", query);

        return "fragments/search :: autocomplete";
    }
}