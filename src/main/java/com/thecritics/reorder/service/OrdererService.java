package com.thecritics.reorder.service;

import com.thecritics.reorder.controller.RootController;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrdererRepository;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Servicio que gestiona las operaciones relacionadas con las órdenes ({@link Orderer}). Esto incluye
 * manipulación del estado de la orden en la sesión, guardado de órdenes, búsqueda de órdenes y
 * otras operaciones relacionadas.
 */
@Service
public class OrdererService {

    private static final Logger log = LogManager.getLogger(RootController.class);

    @Autowired
    private OrdererRepository ordererRepository;

   

    /**
     * Guarda un Orderer con el username, email y contraseña.
     *
     * @param email El email del Orderer.
     * @param username El username de Orderer.
     * @param password La contraseña de la cuenta del orderer
     * @return Orderer guardado, incluyendo su ID asignado.
     */
    public Orderer saveOrderer(String email, String username, String password) {
        Orderer orderer = new Orderer();
        orderer.setEmail(email);
        orderer.setUsername(username);
        orderer.setPassword(password);

        Orderer savedOrderer = ordererRepository.save(orderer);

        return savedOrderer;
    }

    public Orderer findByUsername(String username) {
        return ordererRepository.findByUsername(username);
    }
    
    public Orderer findByEmail(String email) {
        return ordererRepository.findByEmail(email);
    }

}