package com.thecritics.reorder.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.thecritics.reorder.service.OrderService;
import com.thecritics.reorder.service.OrdererService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.service.OrderService;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class OrdererViewController {
    private static final Logger log = LogManager.getLogger(OrdererViewController.class);

    private final OrdererService ordererService;

    public OrdererViewController(OrdererService ordererService) {
        this.ordererService = ordererService;
    }

    /**
     * Muestra la página de detalles de un Orderer específico.
     * @param username El nombre del usuario a mostrar.
     * @param fromQuery Término de búsqueda original (opcional, para volver a la búsqueda).
     * @param model El modelo para la vista.
     * @param session La sesión HTTP.
     * @return El nombre de la vista "orderer".
     */
    @GetMapping("/orderer/{username}")
    public String getOrdererDetail(@PathVariable String username,
        @RequestParam (name = "fromQuery", required = false) String fromQuery,
        Model model, HttpSession session){
            log.debug("Solicitando detalles para Orderer ID: {}", username);
            Orderer orderer = ordererService.getOrdererByUsername(username);

            if (orderer == null){
                model.addAttribute("errorMessage", "Orderer no encontrado");
                return "error";
            }

            model.addAttribute("orderer", orderer);
            if (fromQuery != null && !fromQuery.trim().isEmpty()){
                String trimmedQuery = fromQuery.trim();
                log.debug("Recibido 'fromQuery': {}", trimmedQuery);
                model.addAttribute("searchQuery", trimmedQuery);
                session.setAttribute("searchQueryContext", trimmedQuery);
            } else {
                String sessionQuery = (String) session.getAttribute("searchQueryContext");
                if (sessionQuery != null){
                    log.debug("Recuperado 'searchQueryContext' de sesión: {}", sessionQuery);
                    model.addAttribute("searchQuery", sessionQuery);
                }
            }
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;

            
            model.addAttribute("followers", orderer.getFollowers());
            model.addAttribute("following", orderer.getFollowing());

            
            boolean isFollowing = false;
            if (currentUser != null) {
                isFollowing = ordererService.isFollowing(currentUser, username);
            }
            model.addAttribute("isFollowing", isFollowing);


            return "orderer";
        }
  
    }
    

