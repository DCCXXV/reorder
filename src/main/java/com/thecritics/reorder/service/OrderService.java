package com.thecritics.reorder.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class OrderService {

    /**
     * Obtiene la lista de elementos almacenados en la sesión.
     * Si la lista no existe, se crea una nueva y se almacena en la sesión.
     *
     * @param session La sesión HTTP actual.
     * @return Una lista de elementos almacenados en la sesión.
     */
    public List<String> getElements(HttpSession session) {
        List<String> elements = (List<String>) session.getAttribute("elements");
        if (elements == null) {
            elements = new ArrayList<>();
            session.setAttribute("elements", elements);
        }
        return elements;
    }

    /**
     * Añade un nuevo elemento a la lista de elementos almacenados en la sesión.
     * El elemento se añade solo si no está vacío o compuesto únicamente por espacios en blanco.
     *
     * @param elementTextInput El texto del elemento a añadir.
     * @param session La sesión HTTP actual.
     * @return La lista actualizada de elementos almacenados en la sesión.
     */
    public List<String> addElement(String elementTextInput, HttpSession session) {
        List<String> elements = getElements(session);
        if (!elementTextInput.trim().isEmpty()) {
            elements.add(elementTextInput.trim());
        }
        return elements;
    }

    /**
     * Elimina un elemento de la lista de elementos almacenados en la sesión.
     *
     * @param elementTextBadge El texto del elemento a eliminar.
     * @param session La sesión HTTP actual.
     * @return La lista actualizada de elementos almacenados en la sesión.
     */
    public List<String> deleteElement(String elementTextBadge, HttpSession session) {
        List<String> elements = getElements(session);
        elements.remove(elementTextBadge);
        return elements;
    }

    /**
     * Obtiene la lista de categorías (tiers) almacenados en la sesión.
     * Si la lista no existe, se crea una nueva con un nivel inicial de 1 y se almacena en la sesión.
     *
     * @param session La sesión HTTP actual.
     * @return Una lista de niveles almacenados en la sesión.
     */
    public List<Integer> getTiers(HttpSession session) {
        List<Integer> tiers = (List<Integer>) session.getAttribute("tiers");
        if (tiers == null) {
            tiers = new ArrayList<>();
            tiers.add(1);
        }
        session.setAttribute("tiers", tiers);
        return tiers;
    }

    /**
     * Añade un nuevo nivel a la lista de categorías almacenados en la sesión.
     * El nuevo nivel se establece como el siguiente número entero consecutivo.
     *
     * @param session La sesión HTTP actual.
     * @return La lista actualizada de niveles almacenados en la sesión.
     */
    public List<Integer> addTiers(HttpSession session) {
        List<Integer> tiers = getTiers(session);
        tiers.add(tiers.size() + 1);
        return tiers;
    }
}

