package com.thecritics.reorder.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecritics.reorder.service.OrderService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class RootController {

    private static final Logger log = LogManager.getLogger(RootController.class);

    /**
     * Añade atributos comunes al modelo desde la sesión HTTP.
     *
     * @param session La sesión HTTP actual.
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     */
    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] {"u", "url", "ws"}) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    @Autowired
    private OrderService orderService;

    /**
     * Maneja las solicitudes GET a la ruta raíz "/".
     *
     * @param model El objeto Modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista "index".
     */
    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    /**
     * Maneja las solicitudes GET para la vista de crear un nuevo Order.
     *
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     * @param session La sesión HTTP actual.
     * @return El nombre de la vista "createOrder".
     */
    @GetMapping("/createOrder")
    public String createOrder(Model model, HttpSession session) {
        List<List<String>> orderState = orderService.getOrderState(session);
        model.addAttribute("orderState", orderState);
        return "createOrder";
    }

    /**
     * Maneja las solicitudes POST para añadir un elemento.
     *
     * @param elementTextInput El texto del elemento a añadir.
     * @param session          La sesión HTTP actual.
     * @param model            El objeto Modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista "createOrder".
     */
    @PostMapping("/createOrder/addElement")
    public String addElement(@RequestParam String elementTextInput, HttpSession session, Model model) {
        List<List<String>> orderState = orderService.addElement(elementTextInput, session);
        model.addAttribute("orderState", orderState);
        return "createOrder";
    }

    /**
     * Maneja las solicitudes POST para eliminar un elemento.
     *
     * @param elementTextBadge El texto del elemento a eliminar.
     * @param session          La sesión HTTP actual.
     * @param model            El objeto Modelo utilizado para pasar datos a la vista.
     * @return El nombre del fragmento de la vista "createOrder :: #elementsContainer".
     */
    @PostMapping("/createOrder/deleteElement")
    public String deleteElement(@RequestParam String elementTextBadge, HttpSession session, Model model) {
        List<List<String>> orderState = orderService.deleteElement(elementTextBadge, session);
        model.addAttribute("orderState", orderState);
        return "createOrder :: #elementsContainer";
    }

    /**
     * Maneja las solicitudes POST para añadir un nuevo tier al Order.
     *
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     * @param session La sesión HTTP actual.
     * @param response La respuesta a la petición http.
     * @returnEl nombre de la vista "createOrder".
     */
    @PostMapping("/createOrder/addTier")
    public String addTier(Model model, HttpSession session, HttpServletResponse response) {
        List<List<String>> orderState = orderService.addTier(session);
        model.addAttribute("orderState", orderState);
        response.setHeader("HX-Trigger", "tierAdded");
        return "createOrder";
    }

    /**
     * Maneja las solicitudes POST para eliminar el último tier del estado del Order.
     *
     * @param session La sesión HTTP actual.
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista "createOrder".
     */
    @PostMapping("/createOrder/keepElementsAndDeleteLastTier")
    public String keepElementsAndDeleteLastTier(HttpSession session, Model model) {
        List<List<String>> orderState = orderService.keepElementsAndDeleteLastTier(session);
        model.addAttribute("orderState", orderState);
        return "createOrder";
    }

    /**
     * Maneja las solicitudes POST para eliminar el último tier del estado del Order.
     *
     * @param session La sesión HTTP actual.
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista "createOrder".
     */
    @PostMapping("/createOrder/deleteLastTier")
    public String deleteLastTier(HttpSession session, Model model) {
        List<List<String>> orderState = orderService.deleteLastTier(session);
        model.addAttribute("orderState", orderState);
        return "createOrder";
    }

    /**
     * Endpoint para actualizar el estado de orden (drag & drop).
     * Se espera recibir un JSON que representa la nueva lista de tiers.
     *
     * @param orderStateJson El JSON que representa la nueva lista de tiers.
     * @param session        La sesión HTTP actual.
     * @return El nombre de la vista "createOrder" o "error" en caso de fallo.
     */
    @PostMapping("/createOrder/updateOrderState")
    public String updateOrderState(@RequestParam String orderStateJson, HttpSession session) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<List<String>> newOrderState = mapper.readValue(orderStateJson, new TypeReference<List<List<String>>>() {});
            if (newOrderState.isEmpty() || newOrderState.get(0) == null) {
                return "error";
            }
            orderService.updateOrderState(newOrderState, session);
            return "createOrder";
        } catch (Exception e) {
            log.error("Error actualizando estado", e);
            return "error";
        }
    }
}
