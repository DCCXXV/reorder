package com.thecritics.reorder.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.service.OrderService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class RootController {

    private static final Logger log = LogManager.getLogger(RootController.class);

    private final ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    private final int MAX_CHARACTERS = 30;
    private final int MAX_ELEMENTS = 500;
    private final int MAX_TIERS = 50;

    // @Autowired implícito
    public RootController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Añade atributos comunes al modelo desde la sesión HTTP.
     *
     * @param session La sesión HTTP actual.
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     */
    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

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
        model.addAttribute("publishEnabled", false);
        model.addAttribute("orderState", orderState);
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
    @PostMapping("/createOrder/addElement")
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
    @PostMapping("/createOrder/deleteElement")
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
    @PostMapping("/createOrder/addTier")
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
    @PostMapping("/createOrder/keepElementsAndDeleteLastTier")
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
    @PostMapping("/createOrder/deleteLastTier")
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
    @PostMapping("/createOrder/updateOrderState")
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
     *
     * @param title   El título del Order.
     * @param author  El autor del Order.
     * @param session La sesión HTTP actual, utilizada para obtener el estado del
     *                Order.
     * @return El nombre de la vista "index".
     */
@PostMapping("/createOrder/PublishOrder")
    public ResponseEntity<?> PublishOrder(
        @RequestParam String title,
        @RequestParam(required = false) String author,
        HttpSession session,
        RedirectAttributes redirectAttributes) {

        if (title == null || title.trim().isEmpty()) {
            return new ResponseEntity<>("El título no puede estar vacío.", HttpStatus.BAD_REQUEST);
        }

        String finalAuthor = (author == null || author.trim().isEmpty()) ? "Anónimo" : author.trim();
        List<List<String>> orderState = orderService.getOrderState(session);

        boolean hasElementsInTiers = orderState != null && orderState.size() > 1 &&
            orderState.stream()
                      .skip(1)
                      .anyMatch(tier -> tier != null && !tier.isEmpty());

        if (!hasElementsInTiers) {
            return new ResponseEntity<>("Debe haber al menos un elemento en un Tier para publicar.", HttpStatus.BAD_REQUEST);
        }

        try {
            Order savedOrder = orderService.saveOrder(title.trim(), finalAuthor, orderState);

            redirectAttributes.addFlashAttribute("toastMessage", "¡Tu Order ha sido publicado correctamente!");

            String redirectUrl = "/order/" + savedOrder.getId();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", redirectUrl);
            headers.add("HX-Redirect", redirectUrl);

            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        } catch (Exception e) {
            log.error("Error al publicar el Order con título '{}' por autor '{}'", title.trim(), finalAuthor, e);
            return new ResponseEntity<>("Ocurrió un error inesperado al publicar el Order.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    @GetMapping("/order/{id}")
    public String getOrderDetail(@PathVariable Integer id,
            @RequestParam(name = "fromQuery", required = false) String fromQuery, Model model, HttpSession session) {
        model.addAttribute("order", orderService.getOrderById(id));
        if (fromQuery != null && !fromQuery.isEmpty()) {
            model.addAttribute("searchQuery", fromQuery);
            session.setAttribute("searchQuery", fromQuery);
        }
        return "order";
    }

    @GetMapping("/reorder")
    public String showReorderPage(@RequestParam("idInput") Integer originalOrderId,
            @RequestParam(name = "fromQuery", required = false) String fromQueryParam,
            Model model, HttpSession session) {

        log.info("Accediendo a /reorder para Order ID: {}, fromQueryParam: {}", originalOrderId, fromQueryParam);

        Order originalOrder = orderService.getOrderById(originalOrderId);
        if (originalOrder == null) {
            log.error("No se encontró la Order original con ID: {}", originalOrderId);
            model.addAttribute("errorMessage", "El Order no fue encontrado.");
            return "redirect:/error";
        }

        List<List<String>> initialReorderState = originalOrder.getContent();

        session.setAttribute("reorderOriginalId", originalOrderId);
        session.setAttribute("reOrderState", initialReorderState);

        String searchQuery = null;
        if (fromQueryParam != null && !fromQueryParam.isEmpty()) {
            searchQuery = fromQueryParam;
            session.setAttribute("searchQuery", searchQuery); // Guardar/Actualizar en sesión
            log.debug("Recibido y guardado searchQuery desde parámetro: {}", searchQuery);
        } else {
            searchQuery = (String) session.getAttribute("searchQuery");
            log.debug("Recuperado searchQuery desde sesión: {}", searchQuery);
        }

        model.addAttribute("originalOrder", originalOrder);
        model.addAttribute("reOrderState", initialReorderState);

        if (searchQuery != null && !searchQuery.isEmpty()) {
            model.addAttribute("searchQuery", searchQuery);
            log.debug("Añadiendo searchQuery al modelo: {}", searchQuery);
        } else {
            log.debug("No se añadió searchQuery al modelo (era nulo o vacío).");
        }
        log.info("Retornando vista 'reorder' para Order ID: {}", originalOrderId);
        return "reorder";
    }

    /**
     * Endpoint para actualizar el estado de orden (drag & drop).
     * Se espera recibir un JSON que representa la nueva lista de tiers.
     *
     * @param orderStateJson El JSON que representa la nueva lista de tiers.
     * @param session        La sesión HTTP actual.
     * @return El nombre de la vista "createOrder" o "error" en caso de fallo.
     */
    @PostMapping("/reorder/updateOrderState")
    public String reorderUpdateOrderState(@RequestParam String reOrderStateJson, HttpSession session, Model model) {
        Integer originalOrderId = (Integer) session.getAttribute("reorderOriginalId");
        String searchQuery = (String) session.getAttribute("searchQuery");

        if (originalOrderId == null) {
            log.error(
                    "Error crítico en POST /reorder/updateOrderState: Falta 'reorderOriginalId' en la sesión (ID: {}).",
                    session.getId());
            model.addAttribute("globalError", "Error de sesión inesperado. Intente de nuevo.");
            return "error";
        }

        Order originalOrder = null;

        try {
            originalOrder = orderService.getOrderById(originalOrderId);
            if (originalOrder == null) {
                log.error(
                        "Error crítico en POST /reorder/updateOrderState: No se encontró Order con ID {} (obtenido de sesión).",
                        originalOrderId);
                model.addAttribute("globalError", "No se pudo encontrar la orden original asociada a esta sesión.");
                return "error";
            }

            log.debug("Recibido reOrderStateJson: {}", reOrderStateJson);
            List<List<String>> newOrderState = objectMapper.readValue(reOrderStateJson,
                    new TypeReference<List<List<String>>>() {
                    });
            log.debug("Enviado newOrderState: {}", newOrderState);

            if (newOrderState.isEmpty()) {
                log.warn("newOrderState está vacío después de deserializar en POST /reorder/updateOrderState.");
                model.addAttribute("originalOrder", originalOrder);
                model.addAttribute("reOrderState", newOrderState);
                if (searchQuery != null)
                    model.addAttribute("searchQuery", searchQuery);
                model.addAttribute("errorMessage", "El estado recibido no puede estar vacío.");
                return "error";
            }

            orderService.updateReOrderState(newOrderState, session);

            model.addAttribute("originalOrder", originalOrder);
            model.addAttribute("reOrderState", newOrderState);
            if (searchQuery != null) {
                model.addAttribute("searchQuery", searchQuery);
            }

            return "reorder";

        } catch (Exception e) {
            log.error("Error actualizando estado en POST /reorder/updateOrderState", e);
            if (originalOrder != null) {
                model.addAttribute("originalOrder", originalOrder);
            }

            if (searchQuery != null)
                model.addAttribute("searchQuery", searchQuery);
            model.addAttribute("globalError", "Ocurrió un error inesperado al guardar los cambios.");
            return "error";
        }
    }

    /**
     * Endpoint para publicar un Order, guardándola en la base de datos.
     *
     * @param title   El título del Order.
     * @param author  El autor del Order.
     * @param session La sesión HTTP actual, utilizada para obtener el estado del
     *                Order.
     * @return El nombre de la vista "index".
     */
    @PostMapping("/reorder/PublishOrder")
    public String reorderPublishOrder(
            @RequestParam String rtitle,
            @RequestParam(required = false) String rauthor,
            @RequestParam Integer originalOrderId,
            HttpSession session,
            Model model) {

        Order originalOrder = orderService.getOrderById(originalOrderId);
        @SuppressWarnings("unchecked")
        List<List<String>> currentReorderState = (List<List<String>>) session.getAttribute("reOrderState");

        if (originalOrder == null || currentReorderState == null) {
            log.error("Error crítico: Faltan datos originales o de sesión para ID {}", originalOrderId);
            model.addAttribute("globalError", "Error inesperado al procesar. Intenta de nuevo.");
            return "redirect:/error";
        }
        if (rtitle == null || rtitle.trim().isEmpty()) {
            log.warn("Publicación cancelada: Título vacío para original ID {}", originalOrderId);
            model.addAttribute("reorderError", "El título no puede estar vacío.");
            model.addAttribute("originalOrder", originalOrder);
            model.addAttribute("reOrderState", currentReorderState);
            String searchQuery = (String) session.getAttribute("searchQuery");
            if (searchQuery != null)
                model.addAttribute("searchQuery", searchQuery);
            
            return "reorder";
        }

        if (originalOrder.getContent().equals(currentReorderState)) {
            log.warn("Publicación cancelada: No hay cambios en el contenido para original ID {}", originalOrderId);

            model.addAttribute("errorMessage", "No has realizado cambios en el contenido.");

            model.addAttribute("originalOrder", originalOrder);
            model.addAttribute("reOrderState", currentReorderState);
            String searchQuery = (String) session.getAttribute("searchQuery");
            if (searchQuery != null) {
                model.addAttribute("searchQuery", searchQuery);
            }

            return "reorder";
        }

        log.info("Contenido diferente, guardando reorder para original ID {}", originalOrderId);
        try {
            String finalAuthor = (rauthor == null || rauthor.trim().isEmpty()) ? "Anónimo" : rauthor.trim();
            Order savedReorder = orderService.saveReOrder(rtitle.trim(), finalAuthor, currentReorderState,
                    originalOrder);

            session.removeAttribute("reOrderState");
            session.removeAttribute("reorderOriginalId");

            return "redirect:/order/" + savedReorder.getId();

        } catch (Exception e) {
            log.error("Error al guardar el reorder para original ID {}", originalOrderId, e);
            model.addAttribute("reorderError", "Error inesperado al guardar.");
            model.addAttribute("originalOrder", originalOrder);
            model.addAttribute("reOrderState", currentReorderState);
            String searchQuery = (String) session.getAttribute("searchQuery");
            if (searchQuery != null)
                model.addAttribute("searchQuery", searchQuery);
            return "reorder";
        }
    }
}