package com.thecritics.reorder.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class OrderService {

    public List<String> getElements(HttpSession session) {
        List<String> elements = (List<String>) session.getAttribute("elements");
        if (elements == null) {
            elements = new ArrayList<>();
            session.setAttribute("elements", elements);
        }
        return elements;
    }

    public List<String> addElement(String elementTextInput, HttpSession session) {
        List<String> elements = getElements(session);
        if (!elementTextInput.trim().isEmpty()) {
            elements.add(elementTextInput.trim());
        }
        return elements;
    }

    public List<String> deleteElement(String elementTextBadge, HttpSession session) {
        List<String> elements = getElements(session);
        elements.remove(elementTextBadge);
        return elements;
    }
}

