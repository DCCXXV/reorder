package com.thecritics.reorder.service;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrdererRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;


import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.thecritics.reorder.controller.RootController;
import com.thecritics.reorder.model.Order;

import com.thecritics.reorder.repository.OrderRepository;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;


@ExtendWith(MockitoExtension.class)
public class OrdererServiceTest {

    @InjectMocks
    private OrdererService ordererService;

    @Mock
    private OrdererRepository ordererRepository;

    @Mock
    private Orderer orderer;

    @Test
    void findByUsername_ShouldReturnOrderer_WhenUsernameExists() {
        // Arrange
        String username = "Julia";
        Orderer mockOrderer = new Orderer();
        mockOrderer.setUsername(username);
        when(ordererRepository.findByUsername(username)).thenReturn(mockOrderer);

        // Act
        Orderer result = ordererService.findByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        verify(ordererRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_ShouldReturnNull_WhenUsernameDoesNotExist() {
        // Arrange
        String username = "noExiste";
        when(ordererRepository.findByUsername(username)).thenReturn(null);

        // Act
        Orderer result = ordererService.findByUsername(username);

        // Assert
        assertThat(result).isNull();
        verify(ordererRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByEmail_ShouldReturnOrderer_WhenEmailExists() {
        // Arrange
        String email = "correo@example.com";
        Orderer mockOrderer = new Orderer();
        mockOrderer.setEmail(email);
        when(ordererRepository.findByEmail(email)).thenReturn(mockOrderer);

        // Act
        Orderer result = ordererService.findByEmail(email);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(ordererRepository, times(1)).findByEmail(email);
    }

    @Test
    void findByEmail_ShouldReturnNull_WhenEmailDoesNotExist() {
        // Arrange
        String email = "inexistente@example.com";
        when(ordererRepository.findByEmail(email)).thenReturn(null);

        // Act
        Orderer result = ordererService.findByEmail(email);

        // Assert
        assertThat(result).isNull();
        verify(ordererRepository, times(1)).findByEmail(email);
    }






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
