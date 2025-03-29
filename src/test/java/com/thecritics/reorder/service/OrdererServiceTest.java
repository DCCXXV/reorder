package com.thecritics.reorder.service;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.junit.jupiter.MockitoExtension;

import com.thecritics.reorder.controller.RootController;
import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrderRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.thecritics.reorder.repository.OrdererRepository;

@ExtendWith(MockitoExtension.class) 
public class OrdererServiceTest {

    @Mock
    private OrdererRepository ordererRepository;

    @Mock
    private Orderer orderer;

    @InjectMocks
    private OrdererService ordererService;


    @Test
    void saveOrderer_ShouldSaveOrdererCorrectly() {
        // Arrange
        String email = "sara@example.com";
        String username = "sara";
        String password = "contraseña";

        Orderer expectedOrderer = new Orderer();
        expectedOrderer.setId(1L);
        expectedOrderer.setEmail(email);
        expectedOrderer.setUsername(username);
        expectedOrderer.setPassword(password);

        when(ordererRepository.save(any(Orderer.class))).thenReturn(expectedOrderer);

        // Act
        Orderer savedOrderer = ordererService.saveOrderer(email, username, password);

        // Assert
        assertThat(savedOrderer).isNotNull();
        assertThat(savedOrderer.getId()).isEqualTo(1L);
        assertThat(savedOrderer.getEmail()).isEqualTo(email);
        assertThat(savedOrderer.getUsername()).isEqualTo(username);
        assertThat(savedOrderer.getPassword()).isEqualTo(password);
        verify(ordererRepository, times(1)).save(any(Orderer.class));
    }

    @Test
    void existsByEmail_ShouldReturnOrdererIfExists() {
        // Arrange
        String email = "sara@example.com";
        Orderer orderer = new Orderer();
        orderer.setEmail(email);

        when(ordererRepository.existsByEmail(email)).thenReturn(orderer);

        // Act
        Orderer foundOrderer = ordererService.existsByEmail(email);

        // Assert
        assertThat(foundOrderer).isNotNull();
        assertThat(foundOrderer.getEmail()).isEqualTo(email);
        verify(ordererRepository, times(1)).existsByEmail(email);
    }
    
    @Test
    void existsByEmail_ShouldReturnNullIfNotExists() {
        // Arrange
        String email = "nonexistent@example.com";

        when(ordererRepository.existsByEmail(email)).thenReturn(null);

        // Act
        Orderer foundOrderer = ordererService.existsByEmail(email);

        // Assert
        assertThat(foundOrderer).isNull();
        verify(ordererRepository, times(1)).existsByEmail(email);
    }

    @Test
    void existsByUsername_ShouldReturnOrdererIfExists() {
        // Arrange
        String username = "sara";
        Orderer orderer = new Orderer();
        orderer.setUsername(username);

        when(ordererRepository.existsByUsername(username)).thenReturn(orderer);

        // Act
        Orderer foundOrderer = ordererService.existsByUsername(username);

        // Assert
        assertThat(foundOrderer).isNotNull();
        assertThat(foundOrderer.getUsername()).isEqualTo(username);
        verify(ordererRepository, times(1)).existsByUsername(username);
    }

    @Test
    void existsByUsername_ShouldReturnNullIfNotExists() {
        // Arrange
        String username = "nonexistentUser";

        when(ordererRepository.existsByUsername(username)).thenReturn(null);

        // Act
        Orderer foundOrderer = ordererService.existsByUsername(username);

        // Assert
        assertThat(foundOrderer).isNull();
        verify(ordererRepository, times(1)).existsByUsername(username);
    }


}
