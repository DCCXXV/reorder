package com.thecritics.reorder.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thecritics.reorder.controller.RootController;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.repository.OrderRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class OrderService {

    private static final Logger log = LogManager.getLogger(RootController.class);

    @Autowired
    OrderRepository orderRepository;

    /**
     * Añade un elemento al primer tier (sin asignar) del estado del Order en la sesión.
     *
     * @param elementText El texto del elemento a añadir.
     * @param session La sesión HTTP actual.
     * @return El estado actualizado del Order con el nuevo elemento añadido.
     */
    public List<List<String>> addElement(String elementText, HttpSession session) {
        List<List<String>> orderState = getOrderState(session);

        String trimmed = elementText.trim();
        if (!trimmed.isEmpty()) {
            boolean exists = orderState.stream().anyMatch(tier -> tier.contains(trimmed));
            if (!exists) {
                orderState.get(0).add(trimmed);
            } else {
                log.debug("El elemento ya existe: " + trimmed);
            }
        }
        return orderState;
    }

    /**
     * Elimina un elemento del estado del Order en la sesión.
     *
     * @param elementText El texto del elemento a eliminar.
     * @param session La sesión HTTP actual.
     * @return El estado actualizado del Order con el elemento eliminado.
     */
    public List<List<String>> deleteElement(String elementText, HttpSession session) {
        List<List<String>> orderState = getOrderState(session);
        // Busca y elimina el elemento en cada tier
        for (List<String> tier : orderState) {
            tier.remove(elementText);
        }
        return orderState;
    }

    /**
     * Añade un nuevo tier vacío al estado del Order en la sesión.
     *
     * @param session La sesión HTTP actual.
     * @return El estado actualizado del Order con el nuevo tier añadido.
     */
    public List<List<String>> addTier(HttpSession session) {
        List<List<String>> orderState = getOrderState(session);
        orderState.add(new ArrayList<>());
        return orderState;
    }

    /**  
     * Elimina el último tier del estado del Order en la sesión guardando todos sus elementos en el tier 0
     * (El de los unassigned) siempre y cuando haya más de dos tiers.
     * Asegura que al menos el tier 0 (elementos no asignados) y la primera categoría no se eliminen.
     * 
     * @param session La sesión HTTP actual.
     * @return El estado actualizado del Order con el último tier eliminado, si es posible.
     */
    public List<List<String>> keepElementsAndDeleteLastTier(HttpSession session) {
        List<List<String>> orderState = getOrderState(session);
        if (orderState.size() > 2) {
            int n = orderState.size();
            List<String> elements = orderState.get(n-1);

            orderState.get(0).addAll(elements);
            orderState.get(n-1).clear();

            orderState.remove(n-1);
        }
        return orderState;
    }

    /**
     * Elimina el último tier del estado del Order en la sesión, siempre y cuando haya más de dos tiers.
     * Asegura que al menos el tier 0 (elementos no asignados) y la primera categoría no se eliminen.
     *
     * @param session La sesión HTTP actual.
     * @return El estado actualizado del Order con el último tier eliminado, si es posible.
     */
    public List<List<String>> deleteLastTier(HttpSession session) {
        List<List<String>> orderState = getOrderState(session);
        if (orderState.size() > 2) { // Mantiene tier 0 y al menos 1 categoría
            orderState.remove(orderState.size() - 1);
        }
        return orderState;
    }

    /**
     * Obtiene el estado actual del Order de la sesión. Si no existe, inicializa un nuevo estado.
     *
     * @param session La sesión HTTP actual.
     * @return El estado actual del Order.
     */
    @SuppressWarnings("unchecked")
    public List<List<String>> getOrderState(HttpSession session) {
        List<List<String>> orderState = (List<List<String>>) session.getAttribute("orderState");
        if (orderState == null) {
            orderState = new ArrayList<>();
            // tier 0 el "sin asignar"
            orderState.add(new ArrayList<>());
            // tier 1
            orderState.add(new ArrayList<>());
            session.setAttribute("orderState", orderState);
        }
        return orderState;
    }

    /**
     * Actualiza el estado de orden con una nueva organización de tiers y elementos.
     *
     * @param newOrderState La nueva organización de tiers y elementos.
     * @param session La sesión HTTP actual.
     * @return El nuevo estado del Order actualizado.
     */
    public List<List<String>> updateOrderState(List<List<String>> newOrderState, HttpSession session) {
        session.setAttribute("orderState", newOrderState);
        return newOrderState;
    }

    /**
     * Guarda un Order con el título, autor y contenido especificados.
     *
     * @param title  El título de Order.
     * @param author El autor de Order. Si está vacío, se establece como "Anónimo".
     * @param content El contenido del Order, organizado en tiers y elementos.
     * @return Order guardado, incluyendo su ID asignado.
     */
    public Order saveOrder(String title, String author, List<List<String>> content) {
        Order order = new Order();
        order.setContent(content);
        order.setTitle(title);
        order.setAuthor((author=="")? "Anónimo" : author);

        Order savedOrder = orderRepository.save(order);

        return savedOrder;
    }

    public List<Order> getOrdersByTitle(String title) {
        return orderRepository.findByTitleContainingIgnoreCase(title);
    }
}