package com.thecritics.reorder.controller;

import static org.mockito.ArgumentMatchers.floatThat;
import static org.mockito.ArgumentMatchers.isNull;

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

@Controller
public class RootController {

    private static final Logger log = LogManager.getLogger(RootController.class);

    private final ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrdererService ordererService;

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
        for (String name : new String[] {"u", "url", "ws"}) {
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
     * @param model            El objeto Modelo utilizado para pasar datos a la vista.
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
     * @param model            El objeto Modelo utilizado para pasar datos a la vista.
     * @return El nombre del fragmento de la vista "createOrder :: #elementsContainer".
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
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     * @param session La sesión HTTP actual.
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
        List<List<String>> orderState = orderService.getOrderState(session);
        Integer elementCount = (Integer) session.getAttribute("elementCount");

        if (elementCount != null){
            int n  = orderState.size();
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
            List<List<String>> newOrderState = objectMapper.readValue(orderStateJson, new TypeReference<List<List<String>>>() {});
            log.debug("Enviado newOrderState: {}", newOrderState);

            if (newOrderState.isEmpty()) {
                return "error";
            }

            //model.addAttribute("publishEnabled", elementsFound);
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
     * @param session La sesión HTTP actual, utilizada para obtener el estado del Order.
     * @return El nombre de la vista "index".
     */
    @PostMapping("/createOrder/PublishOrder")
    public String PublishOrder(@RequestParam String title, @RequestParam String author, HttpSession session, Model model) {
        if (title == null || title.isEmpty()) {
            return "error";
        }

        List<List<String>> orderState = orderService.getOrderState(session);

        orderService.saveOrder(title, author, orderState);
        orderState = clearOrder(orderState);

        model.addAttribute("toastMessage", "¡Tu Order ha sido publicado correctamente!");

        return "redirect:/";
    }

    /**
     * Limpia el estado del Order, removiendo todos los elementos y restableciendo los tiers iniciales.
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
    public String searchRefresh( Model model) {
        return "redirect:/";
    }
    
    @GetMapping("/order/{id}")
    public String getOrderDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        return "order"; 

    }

    @GetMapping("/register")
    public String registerUser() {
        return "register"; 
    }
    
    @GetMapping("/logIn")
    public String login(Model model){
        return "logIn";
    }


    @GetMapping("/reorder")
        public String reorder(@RequestParam Integer idInput, Model model , HttpSession session) {
        List<List<String>> reOrderState = orderService.getOrderById(idInput).getContent();

        session.setAttribute("reOrderState",  reOrderState);
        model.addAttribute("reOrderState", reOrderState);

        model.addAttribute("publishEnabled", false);

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
        try {
            log.debug("Recibido reOrderStateJson: {}", reOrderStateJson);
            List<List<String>> newOrderState = objectMapper.readValue(reOrderStateJson, new TypeReference<List<List<String>>>() {});
            log.debug("Enviado newOrderState: {}", newOrderState);

            if (newOrderState.isEmpty()) {
                return "error";
            }

            //model.addAttribute("publishEnabled", elementsFound);
            model.addAttribute("reOrderState", newOrderState);
            orderService.updateReOrderState(newOrderState, session);

            return "reorder";

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
     * @param session La sesión HTTP actual, utilizada para obtener el estado del Order.
     * @return El nombre de la vista "index".
     */
    @PostMapping("/reorder/PublishOrder")
    public String reorderPublishOrder(@RequestParam String rtitle, @RequestParam String rauthor, HttpSession session, Model model) {
        if (rtitle == null || rtitle.isEmpty()) {
            return "error";
        }

        List<List<String>> reOrderState = orderService.getReOrderState(session);
        
        orderService.saveOrder(rtitle, rauthor, reOrderState);
        reOrderState = clearOrder(reOrderState);

        model.addAttribute("toastMessage", "¡Tu Order ha sido publicado correctamente!");

        return "redirect:/";
    }


    @PostMapping("/uploadRegister")
    public String uploadRegister(@RequestParam String username, @RequestParam String email, @RequestParam String password, Model model){
        if (username == null || username.isEmpty()){
            return "error";
        }

        if (email == null || email.isEmpty()){
            return "error";
        }

        if (password == null || password.isEmpty()){
            return "error";
        }

        ordererService.saveOrderer(email, username, password);

        return "redirect:/";
    }

    @PostMapping("/uploadLogin")
    public String loginUser(@RequestParam String identifier, @RequestParam String password, Model model,HttpSession session) {
        if (identifier == null || identifier.isEmpty()){
            model.addAttribute("errorMessage", "introduzca su usuario o email");
            return "logIn";        }
        if (password == null || password.isEmpty()){
            model.addAttribute("errorMessage", "introduzca su contraseña");
            return "logIn";        }

        boolean isEmail = identifier.contains("@");
        Orderer orderer = null;
        if(isEmail) {
            orderer = ordererService.findByEmail(identifier);
        } else {
            orderer = ordererService.findByUsername(identifier);
        }
        
        if(orderer == null) {
            model.addAttribute("errorMessage", "¡El usuario no existe!");
            return "logIn";
        } else if (!orderer.getPassword().equals(password)) {
            model.addAttribute("errorMessage", "¡Contraseña incorrecta!");
            return "logIn";
        } else {
            session.setAttribute("u", orderer.getUsername());
            session.setAttribute("ws", orderer.getId());
            return "redirect:/";
        }
        
    }
}