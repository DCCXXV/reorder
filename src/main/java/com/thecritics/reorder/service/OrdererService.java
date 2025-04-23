package com.thecritics.reorder.service;

import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrdererRepository;
import com.thecritics.reorder.SecurityConfig;
import com.thecritics.reorder.controller.HomeController;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio que gestiona las operaciones relacionadas con las órdenes ({@link Orderer}). Esto incluye
 * manipulación del estado de la orden en la sesión, guardado de órdenes, búsqueda de órdenes y
 * otras operaciones relacionadas.
 */
@Service
public class OrdererService {

    private static final Logger log = LogManager.getLogger(HomeController.class);

    private final OrdererRepository ordererRepository;
    private final PasswordEncoder passwordEncoder;

    public OrdererService(OrdererRepository ordererRepository, PasswordEncoder passwordEncoder) {
        this.ordererRepository = ordererRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Comprueba si ya existe un Orderer con la dirección de correo electrónico especificada
     * en el repositorio, ignorando mayúsculas y minúsculas.
     *
     * @param email La dirección de correo cuya existencia se desea comprobar. No debe ser nulo.
     * @return {@code true} si existe un Orderer con el correo electrónico proporcionado (ignorando mayúsculas/minúsculas),
     *         {@code false} en caso contrario.
     */
    public Boolean emailAlreadyTaken(String email) {
        return ordererRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Comprueba si ya existe un Orderer con el nombre de usuario especificado
     * en el repositorio, ignorando mayúsculas y minúsculas.
     *
     * @param username El nombre de usuario cuya existencia se desea comprobar. No debe ser nulo.
     * @return {@code true} si existe un Orderer con el nombre de usuario proporcionado (ignorando mayúsculas/minúsculas),
     *         {@code false} en caso contrario.
     */
    public Boolean usernameAlreadyTaken(String username) {
        return ordererRepository.existsByUsernameIgnoreCase(username);
    }

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
        orderer.setPassword(this.passwordEncoder.encode(password));

        Orderer savedOrderer = ordererRepository.save(orderer);

        return savedOrderer;
    }

    public List<Orderer> getOrderersByUsername(String username) {
        return ordererRepository.findByUsernameContainingIgnoreCase(username);
    }
}