package com.thecritics.reorder.service;

import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrdererRepository;
import com.thecritics.reorder.controller.HomeController;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Servicio que gestiona las operaciones relacionadas con las órdenes
 * ({@link Orderer}). Esto incluye
 * manipulación del estado de la orden en la sesión, guardado de órdenes,
 * búsqueda de órdenes y
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
     * Comprueba si ya existe un Orderer con la dirección de correo electrónico
     * especificada
     * en el repositorio, ignorando mayúsculas y minúsculas.
     *
     * @param email La dirección de correo cuya existencia se desea comprobar. No
     *              debe ser nulo.
     * @return {@code true} si existe un Orderer con el correo electrónico
     *         proporcionado (ignorando mayúsculas/minúsculas),
     *         {@code false} en caso contrario.
     */
    public Boolean emailAlreadyTaken(String email) {
        return ordererRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Comprueba si ya existe un Orderer con el nombre de usuario especificado
     * en el repositorio, ignorando mayúsculas y minúsculas.
     *
     * @param username El nombre de usuario cuya existencia se desea comprobar. No
     *                 debe ser nulo.
     * @return {@code true} si existe un Orderer con el nombre de usuario
     *         proporcionado (ignorando mayúsculas/minúsculas),
     *         {@code false} en caso contrario.
     */
    public Boolean usernameAlreadyTaken(String username) {
        return ordererRepository.existsByUsernameIgnoreCase(username);
    }

    /**
     * Guarda un Orderer con el username, email y contraseña.
     *
     * @param email    El email del Orderer.
     * @param username El username de Orderer.
     * @param password La contraseña de la cuenta del orderer
     * @return Orderer guardado, incluyendo su ID asignado.
     */
    public Orderer saveOrderer(String email, String username, String password) {
        Orderer orderer = new Orderer();
        orderer.setEmail(email);
        orderer.setUsername(username);
        orderer.setPassword(this.passwordEncoder.encode(password));
        orderer.setOrders(new ArrayList<>());

        Orderer savedOrderer = ordererRepository.save(orderer);

        return savedOrderer;
    }

    /**
     * Obtiene una lista de Orderers cuyos nombres de usuario contienen la cadena
     * especificada,
     * sin distinguir entre mayúsculas y minúsculas.
     *
     * @param username Subcadena del nombre de usuario que se desea buscar.
     * @return Lista de Orderers cuyos usernames contienen la subcadena especificada
     *         (ignorando mayúsculas/minúsculas).
     */
    public List<Orderer.Transfer> getOrderersByUsername(String username) {
        return ordererRepository.findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(Orderer::toTransfer)
                .toList();
    }

    /**
     * Busca un Orderer exacto por su nombre de usuario.
     *
     * @param username El nombre de usuario del Orderer a buscar.
     * @return El Transfer del Orderer correspondiente al nombre de usuario proporcionado, o
     *         {@code null} si no se encuentra.
     */
    public Orderer.Transfer findByUsername(String username) {
        Orderer orderer =  ordererRepository.findByUsername(username);
        if (orderer == null) {
            return null;
        }
        return orderer.toTransfer();
    }

    /**
     * Busca un Orderer exacto por su dirección de correo electrónico.
     *
     * @param email La dirección de correo electrónico del Orderer a buscar.
     * @return El Orderer correspondiente al correo electrónico proporcionado, o
     *         {@code null} si no se encuentra.
     */
    public Orderer.Transfer findByEmail(String email) {
        Orderer orderer =  ordererRepository.findByEmail(email);
        if (orderer == null) {
            return null;
        }
        return orderer.toTransfer();
    }

    
    public List<Orderer.Transfer> findTopOrderersStartingWith(String query, int limit) {
        Pageable pageRequest = PageRequest.of(0, limit);
        return ordererRepository.findTopUsernamesStartingWith(query, pageRequest)
                .stream()
                .map(Orderer::toTransfer)
                .toList();
    }

    /**
     * Busca una orden por su ID
     * 
     * @param id El ID de la orden a buscar
     * @return La orden encontrada, o null si no existe.
     */

     public Orderer getOrdererById(Integer id){
        return ordererRepository.findById(id);


     }

     public Orderer getOrdererByUsername(String username){
        return ordererRepository.findByUsername(username);
     }

        /**
     * Comprueba si un usuario sigue a otro.
     *
     * @param followerUsername El nombre de usuario del seguidor.
     * @param followedUsername El nombre de usuario del usuario seguido.
     * @return {@code true} si followerUsername sigue a followedUsername, {@code false} en caso contrario.
     */
    public boolean isFollowing(String followerUsername, String followedUsername) {
        Orderer follower = ordererRepository.findByUsername(followerUsername);
        Orderer followed = ordererRepository.findByUsername(followedUsername);

        if (follower == null || followed == null) {
            return false;
        }

        return follower.getFollowing().stream()
                .anyMatch(o -> o.getId() == followed.getId());
    }

    /**
     * Permite que un usuario siga a otro.
     *
     * @param followerUsername El nombre de usuario del seguidor.
     * @param followedUsername El nombre de usuario del usuario a seguir.
     */
    public void follow(String followerUsername, String followedUsername) {
        Orderer follower = ordererRepository.findByUsername(followerUsername);
        Orderer followed = ordererRepository.findByUsername(followedUsername);

        if (follower != null && followed != null && !isFollowing(followerUsername, followedUsername)) {
            follower.getFollowing().add(followed);
            followed.getFollowers().add(follower);
            ordererRepository.save(follower);
            ordererRepository.save(followed);
        }
    }

    /**
     * Permite que un usuario deje de seguir a otro.
     *
     * @param followerUsername El nombre de usuario del seguidor.
     * @param followedUsername El nombre de usuario del usuario al que se desea dejar de seguir.
     */
    public void unfollow(String followerUsername, String followedUsername) {
        Orderer follower = ordererRepository.findByUsername(followerUsername);
        Orderer followed = ordererRepository.findByUsername(followedUsername);

        if (follower != null && followed != null && isFollowing(followerUsername, followedUsername)) {
            follower.getFollowing().removeIf(o -> o.getId() == followed.getId());
            followed.getFollowers().removeIf(o -> o.getId() == follower.getId());
            ordererRepository.save(follower);
            ordererRepository.save(followed);
        }
    }
}