package com.thecritics.reorder.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.service.OrderService;
import com.thecritics.reorder.service.OrdererService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


/**
 * Controlador para todas las operaciones relacionadas con la creación de un nuevo Order.
 */

@Controller
@RequestMapping("/createOrder")
public class OrderCreationController {

    private static final Logger log = LogManager.getLogger(OrderCreationController.class);

    @Autowired
    private OrderService orderService;
    private final ObjectMapper objectMapper;

    private final int MAX_CHARACTERS = 30;
    private final int MAX_ELEMENTS = 500;
    private final int MAX_TIERS = 50;

    public OrderCreationController(ObjectMapper objectMapper, OrderService orderService) {
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }
    /**
     * Maneja las solicitudes GET para la vista de crear un nuevo Order.
     *
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     * @param session La sesión HTTP actual.
     * @return El nombre de la vista "createOrder".
     */
    @GetMapping
    public String createOrder(Model model, HttpSession session) {
        List<List<String>> orderState = orderService.getOrderState(session);
        model.addAttribute("publishEnabled", false);
        model.addAttribute("orderState", orderState);
        model.addAttribute("u", session.getAttribute("username"));
        return "createOrder";
    }

    /**
     * Maneja las solicitudes POST para añadir un elemento.
     *
     * @param elementTextInput El texto del elemento a añadir.
     * @param session          La sesión HTTP actual.
     * @param model            El objeto Modelo utilizado para pasar datos a la
     *                         vista.
     * @return El nombre de la vista "createOrder".
     */
    @PostMapping("/addElement")
    public String addElement(@RequestParam String elementTextInput, HttpSession session, Model model) {
        List<List<String>> orderState = orderService.getOrderState(session);
        String trimmed = elementTextInput.trim();

        if (trimmed.length() > MAX_CHARACTERS) {
            model.addAttribute("errorMessage", "¡El elemento debe tener menos de 30 caracteres!");
        } else {
            if (trimmed.isEmpty()) {
                model.addAttribute("errorMessage", "¡El elemento no puede estar vacío!");
            } else {
                boolean exists = orderState.stream().anyMatch(tier -> tier.contains(trimmed));
                if (exists) {
                    model.addAttribute("errorMessage", "¡El elemento ya existe!");
                } else {
                    Integer elementCount = (Integer) session.getAttribute("elementCount");

                    if (elementCount == null) {
                        elementCount = 1;
                        session.setAttribute("elementCount", elementCount);
                    } else {
                        elementCount++;
                        session.setAttribute("elementCount", elementCount);
                    }

                    if (elementCount < MAX_ELEMENTS) {
                        orderState.get(0).add(trimmed);
                    } else {
                        model.addAttribute("errorMessage", "¡Máximo número de elementos superado (500)!");
                        elementCount--;
                        session.setAttribute("elementCount", elementCount);
                    }
                }
            }
        }

        model.addAttribute("orderState", orderState);
        return "createOrder";
    }

    /**
     * Maneja las solicitudes POST para eliminar un elemento.
     *
     * @param elementTextBadge El texto del elemento a eliminar.
     * @param session          La sesión HTTP actual.
     * @param model            El objeto Modelo utilizado para pasar datos a la
     *                         vista.
     * @return El nombre del fragmento de la vista "createOrder ::
     *         #elementsContainer".
     */
    @PostMapping("/deleteElement")
    public String deleteElement(@RequestParam String elementTextBadge, HttpSession session, Model model) {
        log.debug(elementTextBadge);
        List<List<String>> orderState = orderService.deleteElement(elementTextBadge, session);
        model.addAttribute("orderState", orderState);

        Integer elementCount = (Integer) session.getAttribute("elementCount");
        elementCount--;
        session.setAttribute("elementCount", elementCount);

        return "createOrder";
    }

    /**
     * Maneja las solicitudes POST para añadir un nuevo tier al Order.
     *
     * @param model    El objeto Modelo utilizado para pasar datos a la vista.
     * @param session  La sesión HTTP actual.
     * @param response La respuesta a la petición http.
     * @returnEl nombre de la vista "createOrder".
     */
    @PostMapping("/addTier")
    public String addTier(Model model, HttpSession session, HttpServletResponse response) {
        List<List<String>> orderState = orderService.getOrderState(session);

        if (orderState.size() > MAX_TIERS) {
            model.addAttribute("errorToastMessage", "No se pueden añadir más de 50 Tiers");
        } else {
            orderService.addTier(session);
            model.addAttribute("orderState", orderState);
            response.setHeader("HX-Trigger", "tierAdded");
        }

        return "createOrder";
    }

    /**
     * Maneja las solicitudes POST para eliminar el último tier del estado del
     * Order.
     *
     * @param session La sesión HTTP actual.
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista "createOrder".
     */
    @PostMapping("/keepElementsAndDeleteLastTier")
    public String keepElementsAndDeleteLastTier(HttpSession session, Model model) {
        List<List<String>> orderState = orderService.keepElementsAndDeleteLastTier(session);
        model.addAttribute("orderState", orderState);
        return "createOrder";
    }

    /**
     * Maneja las solicitudes POST para eliminar el último tier del estado del
     * Order.
     *
     * @param session La sesión HTTP actual.
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista "createOrder".
     */
    @PostMapping("/deleteLastTier")
    public String deleteLastTier(HttpSession session, Model model) {
        List<List<String>> orderState = orderService.getOrderState(session);
        Integer elementCount = (Integer) session.getAttribute("elementCount");

        if (elementCount != null) {
            int n = orderState.size();
            List<String> elements = orderState.get(n - 1);
            elementCount -= elements.size();
            session.setAttribute("elementCount", elementCount);
        }

        orderState = orderService.deleteLastTier(session);
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
    @PostMapping("/updateOrderState")
    public String updateOrderState(@RequestParam String orderStateJson, HttpSession session, Model model) {
        try {
            log.debug("Recibido orderStateJson: {}", orderStateJson);
            List<List<String>> newOrderState = objectMapper.readValue(orderStateJson,
                    new TypeReference<List<List<String>>>() {
                    });
            log.debug("Enviado newOrderState: {}", newOrderState);

            if (newOrderState.isEmpty()) {
                return "error";
            }

            model.addAttribute("orderState", newOrderState);
            orderService.updateOrderState(newOrderState, session);

            return "createOrder";

        } catch (Exception e) {
            log.error("Error actualizando estado", e);
            return "error";
        }
    }

  /**
     * Endpoint para publicar un Order, guardándola en la base de datos.
     * Devuelve ResponseEntity para manejar redirecciones y errores con HTMX.
     *
     * @param title            El título del Order.
     * @param author           El autor del Order (opcional).
     * @param session          La sesión HTTP actual.
     * @param redirectAttributes Para pasar mensajes flash en la redirección.
     * @return ResponseEntity con redirección (302) o error (400/500).
     */
    @PostMapping("/PublishOrder")
    public ResponseEntity<?> PublishOrder(
        @RequestParam String title,
        @RequestParam(required = false) String author,
        HttpSession session,
        RedirectAttributes redirectAttributes) {

        if (title == null || title.trim().isEmpty()) {
            HttpHeaders headers = createErrorHeaders("#publish-error-message");
            return new ResponseEntity<>("El título no puede estar vacío.", headers, HttpStatus.BAD_REQUEST);
        }

        String finalAuthor = (author == null || author.trim().isEmpty()) ? "Anónimo" : author.trim();
        List<List<String>> orderState = orderService.getOrderState(session);

        boolean hasElementsInTiers = orderState != null && orderState.size() > 1 &&
            orderState.stream()
                      .skip(1)
                      .anyMatch(tier -> tier != null && !tier.isEmpty());

        if (!hasElementsInTiers) {
            HttpHeaders headers = createErrorHeaders("#publish-error-message");
            return new ResponseEntity<>("Debe haber al menos un elemento en un Tier para publicar.", headers, HttpStatus.BAD_REQUEST);
        }

        try {
            Order.Transfer savedOrder = orderService.saveOrder(title.trim(), finalAuthor, orderState);

            orderState = clearOrder(orderState);

            redirectAttributes.addFlashAttribute("toastMessage", "¡Tu Order ha sido publicado correctamente!");

            String redirectUrl = "/order/" + savedOrder.getId();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", redirectUrl);
            headers.add("HX-Redirect", redirectUrl);

            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        } catch (Exception e) {
            log.error("Error al publicar el Order con título '{}' por autor '{}'", title.trim(), finalAuthor, e);
            HttpHeaders errorHeaders = createErrorHeaders("#publish-error-message");
            return new ResponseEntity<>("Ocurrió un error inesperado al publicar el Order.", errorHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- Métodos Helper ---

    /**
     * Helper privado para crear las cabeceras estándar de error para HTMX.
     * @param targetSelector El selector CSS (ej. "#error-div") donde HTMX debe poner el mensaje.
     * @return HttpHeaders configuradas para errores HTMX.
     */
    private HttpHeaders createErrorHeaders(String targetSelector) {
        HttpHeaders headers = new HttpHeaders();
        // Asegurarse de que el content type sea text/html o text/plain para que HTMX lo interprete
        headers.setContentType(MediaType.valueOf("text/plain;charset=UTF-8"));
        headers.add("HX-Retarget", targetSelector);
        headers.add("HX-Reswap", "innerHTML");
        return headers;
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

