package com.thecritics.reorder.service; 

import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrdererRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service("ordererDetailsService")
public class OrdererDetailsService implements UserDetailsService {

    private static final Logger log = LogManager.getLogger(OrdererDetailsService.class);

    @Autowired
    private OrdererRepository ordererRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user by username/identifier: {}", username);

        Orderer orderer = ordererRepository.findByUsername(username);

        if (orderer == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        log.info("User found: {}", orderer.getUsername());

        return User.builder()
            .username(orderer.getUsername())
            .password(orderer.getPassword())
            .roles("USER")
            .build();
    }
}
