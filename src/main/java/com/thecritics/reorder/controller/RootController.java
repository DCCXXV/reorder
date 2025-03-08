package com.thecritics.reorder.controller;

import java.util.ArrayList;
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

import com.thecritics.reorder.service.OrderService;

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
     * Maneja las solicitudes GET a la ruta "/createOrder".
     *
     * @param model   El objeto Modelo utilizado para pasar datos a la vista.
     * @param session La sesión HTTP actual.
     * @return El nombre de la vista "createOrder".
     */
    @GetMapping("/createOrder")
    public String createOrder(Model model, HttpSession session) {
        List<Integer> tiers = orderService.getTiers(session);
        List<String> elements = orderService.getElements(session);

        model.addAttribute("tiers", tiers);
        model.addAttribute("elements", elements);
        return "createOrder";
    }

    /**
     * Añade un nuevo elemento a la lista de elementos y actualiza el modelo con la lista actual.
     *
     * @param elementTextInput El texto del elemento a añadir. Es el nombre (atributo name) del input en createOrder.
     * @param session          La sesión HTTP actual.
     * @param model            El objeto Modelo utilizado para pasar datos a la vista. La lista actualizada
     *                         de elementos se añadirá al modelo con el nombre de atributo "elements".
     * @return El nombre del fragmento de la vista a actualizar. En este caso, devuelve
     *         "createOrder :: #elementsContainer", lo que significa que solo la parte de la
     *         vista con ID "elementsContainer" debe ser actualizada. HTMX utilizará esta
     *         respuesta para refrescar únicamente el contenido del contenedor especificado.
     */
    @PostMapping("/createOrder/addElement")
    public String addElement(@RequestParam String elementTextInput, HttpSession session, Model model) {
        List<String> elements = orderService.addElement(elementTextInput, session);
        model.addAttribute("elements", elements);
        return "createOrder :: #elementsContainer";
    }

    /**
     * Elimina un elemento de la lista de elementos y actualiza el modelo con la lista actualizada.
     *
     * @param elementTextBadge El texto del elemento a eliminar. Es el nombre del badge en createOrder.
     * @param session          La sesión HTTP actual.
     * @param model            El objeto Modelo utilizado para pasar datos a la vista. La lista actualizada
     *                         de elementos se añadirá al modelo con el nombre de atributo "elements".
     * @return El nombre del fragmento de la vista a actualizar. En este caso, devuelve
     *         "createOrder :: #elementsContainer", lo que significa que solo la parte de la
     *         vista con ID "elementsContainer" debe ser actualizada. HTMX utilizará esta
     *         respuesta para refrescar únicamente el contenido del contenedor especificado.
     */
    @PostMapping("/createOrder/deleteElement")
    public String deleteElement(@RequestParam String elementTextBadge, HttpSession session, Model model) {
        List<String> elements = orderService.deleteElement(elementTextBadge, session);
        model.addAttribute("elements", elements);
        return "createOrder :: #elementsContainer";
    }


    /**
     * Maneja la solicitud POST para añadir una nueva categoría a la lista de categorías en la sesión.
     *
     * @param model El modelo que se utiliza para pasar datos a la vista.
     * @param session La sesión HTTP actual.
     * @return El nombre de la vista fragmentada que se actualizará con las nuevas categorías.
     */
    @PostMapping("/createOrder/addTier")
    public String addTier(Model model, HttpSession session) {
        List<Integer> tiers = orderService.addTiers(session);
        model.addAttribute("tiers", tiers);
        return "createOrder :: tiersContainer";
    }

}
