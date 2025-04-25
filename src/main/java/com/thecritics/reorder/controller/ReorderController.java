package com.thecritics.reorder.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.service.OrderService;
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

import java.util.List;
import java.util.Objects;

/**
 * Controlador para la funcionalidad de reordenar un Order existente.
 */
@Controller
@RequestMapping("/reorder")
public class ReorderController {

    private static final Logger log = LogManager.getLogger(ReorderController.class);

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public ReorderController(ObjectMapper objectMapper, OrderService orderService) {
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @GetMapping
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
            session.setAttribute("searchQuery", searchQuery);
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
    @PostMapping("/updateOrderState")
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
     * Endpoint para publicar un ReOrder, guardándolo en la base de datos.
     * Devuelve ResponseEntity para manejar redirecciones y errores con HTMX.
     *
     * @param rtitle            El título del ReOrder.
     * @param rauthor           El autor del ReOrder (opcional).
     * @param originalOrderId   ID del Order original que se está reordenando.
     * @param session           La sesión HTTP actual.
     * @param redirectAttributes Para pasar mensajes flash en la redirección.
     * @return ResponseEntity con redirección (302) o error (400/500).
     */
    @PostMapping("/PublishOrder")
    public ResponseEntity<?> reorderPublishOrder(
            @RequestParam String rtitle,
            @RequestParam(required = false) String rauthor,
            @RequestParam Integer originalOrderId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Order originalOrder = orderService.getOrderById(originalOrderId);
        @SuppressWarnings("unchecked")
        List<List<String>> currentReorderState = (List<List<String>>) session.getAttribute("reOrderState");

        if (originalOrder == null || currentReorderState == null) {
            log.error("Error crítico: Faltan datos originales o de sesión para ID {}", originalOrderId);
            HttpHeaders headers = createErrorHeaders("#reorder-error-message");
            return new ResponseEntity<>("Error inesperado al procesar. Intenta de nuevo.", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (rtitle == null || rtitle.trim().isEmpty()) {
            log.warn("Publicación cancelada: Título vacío para original ID {}", originalOrderId);
            HttpHeaders headers = createErrorHeaders("#reorder-error-message");
            return new ResponseEntity<>("El título no puede estar vacío.", headers, HttpStatus.BAD_REQUEST);
        }

        if (originalOrder.getContent().equals(currentReorderState)) {
            log.warn("Publicación cancelada: No hay cambios en el contenido para original ID {}", originalOrderId);
            HttpHeaders headers = createErrorHeaders("#reorder-error-message");
            return new ResponseEntity<>("No has realizado cambios en el contenido.", headers, HttpStatus.BAD_REQUEST);
        }

        log.info("Contenido diferente, guardando reorder para original ID {}", originalOrderId);
        try {
            String finalAuthor = (rauthor == null || rauthor.trim().isEmpty()) ? "Anónimo" : rauthor.trim();
            Order.Transfer savedReorder = orderService.saveReOrder(rtitle.trim(), finalAuthor, currentReorderState, originalOrder);

            session.removeAttribute("reOrderState");
            session.removeAttribute("reorderOriginalId");

            redirectAttributes.addFlashAttribute("toastMessage", "¡Tu ReOrder ha sido publicado correctamente!");
            String redirectUrl = "/order/" + savedReorder.getId();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", redirectUrl);
            headers.add("HX-Redirect", redirectUrl);

            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        } catch (Exception e) {
            log.error("Error al guardar el reorder para original ID {}", originalOrderId, e);
            HttpHeaders headers = createErrorHeaders("#reorder-error-message");
            return new ResponseEntity<>("Error inesperado al guardar el ReOrder.", headers, HttpStatus.INTERNAL_SERVER_ERROR);
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
        headers.setContentType(MediaType.valueOf("text/plain;charset=UTF-8"));
        headers.add("HX-Retarget", targetSelector);
        headers.add("HX-Reswap", "innerHTML");
        return headers;
    }
}
