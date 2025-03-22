package com.thecritics.reorder.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.service.OrderService;


@Controller
@RequestMapping("/api/search")
public class OrderSearchController {

    private final OrderService orderService;

    public OrderSearchController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Endpoint para el autocompletado de órdenes
     * @param query Texto de búsqueda
     * @param model Modelo de Thymeleaf
     * @return Fragmento Thymeleaf con resultados
     */
    @GetMapping("/autocomplete")
    public String getOrderAutocompleteResults(@RequestParam(required = false) String query, Model model) {
        if (query == null || query.isBlank()) {
            return "fragments/search :: empty";
        }
        
        // Obtener todos los resultados
        List<Order> allResults = orderService.searchOrdersByTitle(query);
        
        // Filtrar para incluir solo los que comienzan con la consulta
        String queryLower = query.toLowerCase();
        List<Order> filteredResults = allResults.stream()
            .filter(order -> order.getTitle().toLowerCase().startsWith(queryLower))
            .collect(Collectors.toList());
        
        // Contar frecuencias de títulos
        Map<String, Long> titleFrequency = new HashMap<>();
        for (Order order : filteredResults) {
            String title = order.getTitle();
            titleFrequency.put(title, titleFrequency.getOrDefault(title, 0L) + 1);
        }
        
        // Crear una lista de títulos únicos
        List<String> uniqueTitles = new ArrayList<>(titleFrequency.keySet());
        
        // Ordenar por frecuencia (descendente)
        uniqueTitles.sort((t1, t2) -> {
            int freqCompare = titleFrequency.get(t2).compareTo(titleFrequency.get(t1));
            // Si las frecuencias son iguales, ordenar alfabéticamente
            return freqCompare != 0 ? freqCompare : t1.compareTo(t2);
        });
        
        // Crear lista de resultados únicos ordenados
        List<Order> uniqueOrders = uniqueTitles.stream()
            .map(title -> filteredResults.stream()
                .filter(order -> order.getTitle().equals(title))
                .findFirst()
                .orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        model.addAttribute("orders", uniqueOrders);

        model.addAttribute("query", query);
        model.addAttribute("orderList", orderService.getOrdersByTitle(query));
        
        return "fragments/search :: autocomplete";
    }
}