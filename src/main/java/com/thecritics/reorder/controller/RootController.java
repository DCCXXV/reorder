package com.thecritics.reorder.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class RootController {

    private static final Logger log = LogManager.getLogger(RootController.class);

    private List<String> elements = new ArrayList<>();

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

    // =================================================================
    // Luego haremos un createOrderController y todo lo que empieza por
    // /createOrder irá ahí. De momento como es poco se queda en este.
    // =================================================================
    
    /**
     * Maneja las solicitudes GET a la ruta "/createOrder".
     * 
     * @param model El objeto Modelo utilizado para pasar datos a la vista.
     * @return El nombre de la vista "createOrder".
     */
    @GetMapping("/createOrder")
    public String createOrder(Model model) {
        model.addAttribute("elements", elements);
        return "createOrder";
    }

    /**
     * Añade un nuevo elemento a la lista de elementos y actualiza el modelo con la lista actual.
     * Este método se activa mediante una solicitud POST a la ruta "/createOrder/addElement".

     *
     * @param elementText El texto del elemento a añadir. es el nombre (atributo name) del input en createOrder
     * @param model       El objeto Modelo utilizado para pasar datos a la vista. La lista actualizada
     *                    de elementos se añadirá al modelo con el nombre de atributo "elements".
     * @return El nombre del fragmento de la vista a actualizar. En este caso, devuelve
     *         "createOrder :: #elementsContainer", lo que significa que solo la parte de la
     *         vista con ID "elementsContainer" debe ser actualizada. HTMX utilizará esta
     *         respuesta para refrescar únicamente el contenido del contenedor especificado.
     */
    @PostMapping("/createOrder/addElement")
    public String addElement(@RequestParam String elementTextInput, Model model) {
        if (!elementTextInput.trim().isEmpty()) {
            elements.add(elementTextInput.trim());
        }
        model.addAttribute("elements", elements);
        return "createOrder :: #elementsContainer";
    }

    /**
     * Elimina un elemento de la lista de elementos y actualiza el modelo con la lista actualizada.
     * Este método se activa mediante una solicitud POST a la ruta "/createOrder/deleteElement".
     *
     * @param elementTextBadge El texto del elemento a eliminar. Es el nombre del badge en createOrder.
     * @param model            El objeto Modelo utilizado para pasar datos a la vista. La lista actualizada
     *                         de elementos se añadirá al modelo con el nombre de atributo "elements".
     * @return El nombre del fragmento de la vista a actualizar. En este caso, devuelve
     *         "createOrder :: #elementsContainer", lo que significa que solo la parte de la
     *         vista con ID "elementsContainer" debe ser actualizada. HTMX utilizará esta
     *         respuesta para refrescar únicamente el contenido del contenedor especificado.
     */
    @PostMapping("/createOrder/deleteElement")
    public String deleteElement(@RequestParam String elementTextBadge, Model model) {
        elements.remove(elementTextBadge.trim());
        model.addAttribute("elements", elements);
        return "createOrder :: #elementsContainer";
    }
    
}
