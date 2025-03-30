package com.thecritics.reorder.service;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrdererRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

public class OrdererServiceTest {

    @InjectMocks
    private OrdererService ordererService;

    @Mock
    private OrdererRepository ordererRepository;

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

}