package com.thecritics.reorder.service;

import com.thecritics.reorder.controller.RootController;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Servicio que gestiona las operaciones relacionadas con las órdenes ({@link Order}). Esto incluye
 * manipulación del estado de la orden en la sesión, guardado de órdenes, búsqueda de órdenes y
 * otras operaciones relacionadas.
 */
@Service
public class OrderService {

    private static final Logger log = LogManager.getLogger(RootController.class);

    @Autowired
    private OrderRepository orderRepository;

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
     * Elimina el último tier del estado del Order en la sesión guardando todos sus elementos en el
     * tier 0 (El de los unassigned) siempre y cuando haya más de dos tiers. Asegura que al menos el
     * tier 0 y la primera categoría no se eliminen.
     *
     * @param session La sesión HTTP actual.
     * @return El estado actualizado del Order con el último tier eliminado, si es posible.
     */
    public List<List<String>> keepElementsAndDeleteLastTier(HttpSession session) {
        List<List<String>> orderState = getOrderState(session);
        if (orderState.size() > 2) {
            int n = orderState.size();
            List<String> elements = orderState.get(n - 1);

            orderState.get(0).addAll(elements);
            orderState.get(n - 1).clear();

            orderState.remove(n - 1);
        }
        return orderState;
    }

    /**
     * Elimina el último tier del estado del Order en la sesión, siempre y cuando haya más de dos
     * tiers. Asegura que al menos el tier 0 (elementos no asignados) y la primera categoría no se
     * eliminen.
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
        List<List<String>> orderState =
                (List<List<String>>) session.getAttribute("orderState");
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
    public List<List<String>> updateOrderState(
            List<List<String>> newOrderState, HttpSession session) {
        session.setAttribute("orderState", newOrderState);
        return newOrderState;
    }

    /**
     * Guarda un Order con el título, autor y contenido especificados.
     *
     * @param title El título de Order.
     * @param author El autor de Order. Si está vacío, se establece como "Anónimo".
     * @param content El contenido del Order, organizado en tiers y elementos.
     * @return Order guardado, incluyendo su ID asignado.
     */
    public Order saveOrder(String title, String author, List<List<String>> content) {
        Order order = new Order();
        order.setContent(content);
        order.setTitle(title);
        order.setAuthor((author == "") ? "Anónimo" : author);

        Order savedOrder = orderRepository.save(order);

        return savedOrder;
    }

    /**
     * Obtiene el estado actual del Order de la sesión. Si no existe, inicializa un nuevo estado.
     *
     * @param session La sesión HTTP actual.
     * @return El estado actual del Order.
     */
    @SuppressWarnings("unchecked")
    public List<List<String>> getReOrderState(HttpSession session) {
        List<List<String>> reOrderState = (List<List<String>>) session.getAttribute("reOrderState");
        if (reOrderState == null) {
            reOrderState = new ArrayList<>();
            // tier 0 el "sin asignar"
            reOrderState.add(new ArrayList<>());
            // tier 1
            reOrderState.add(new ArrayList<>());
            session.setAttribute("reOrderState", reOrderState);
        }
        return reOrderState;
    }

    /**
     * Actualiza el estado de orden con una nueva organización de tiers y elementos.
     *
     * @param newReOrderState La nueva organización de tiers y elementos.
     * @param session La sesión HTTP actual.
     * @return El nuevo estado del Order actualizado.
     */
    public List<List<String>> updateReOrderState(
            List<List<String>> newReOrderState, HttpSession session) {
        session.setAttribute("reOrderState", newReOrderState);
        return newReOrderState;
    }

    /**
     * Busca órdenes por título (ignorando mayúsculas y minúsculas) y las ordena por fecha de
     * creación descendente.
     *
     * @param title El título a buscar (puede ser parcial).
     * @return Lista de órdenes que coinciden con el título, ordenadas por fecha de creación.
     */
    public List<Order> getOrdersByTitle(String title) {
        return orderRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title);
    }

    /**
     * Busca los títulos de órdenes que comienzan con una query dada (ignorando mayúsculas y
     * minúsculas), limitando el número de resultados. Utiliza paginación para limitar la consulta.
     *
     * @param query La query con la que deben comenzar los títulos.
     * @param limit El número máximo de resultados a retornar.
     * @return Lista de órdenes cuyos títulos coinciden con la query, limitada por el parámetro
     *     `limit`.
     */
    public List<Order> findTopOrdersStartingWith(String query, int limit) {
        Pageable pageRequest = PageRequest.of(0, limit);
        return orderRepository.findTopTitlesStartingWith(query, pageRequest);
    }

    /**
     * Busca órdenes por título (ignorando mayúsculas y minúsculas) y retorna los primeros 5
     * resultados.
     *
     * @param title Texto de búsqueda.
     * @return Lista de órdenes que coinciden con la búsqueda.
     */
    public List<Order> searchOrdersByTitle(String title) {
        return orderRepository.findTop5ByTitleContainingIgnoreCase(title);
    }

    /**
     * Busca una orden por su ID.
     *
     * @param id El ID de la orden a buscar.
     * @return La orden encontrada, o null si no existe.
     */
    public Order getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    public List<List<String>> getOrderContent (HttpSession session){
        Order order = (Order) session.getAttribute("order");
        if (order == null) {
            System.out.println("order es null, se crea uno nuevo");
        }
        List<List<String>> orderContent = order.getContent();
        return orderContent;
    }

    
}