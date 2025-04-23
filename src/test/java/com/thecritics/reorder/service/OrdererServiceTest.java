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

import com.thecritics.reorder.model.Order;

import com.thecritics.reorder.repository.OrderRepository;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentCaptor;

import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class OrdererServiceTest {

    @InjectMocks
    private OrdererService ordererService;

    @Mock
    private OrdererRepository ordererRepository;

    @Mock 
    private PasswordEncoder passwordEncoder;

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
        String email = "test@example.com";
        String username = "testuser";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        Orderer ordererToSave = new Orderer();
        ordererToSave.setEmail(email);
        ordererToSave.setUsername(username);

        Orderer savedOrderer = new Orderer();
        savedOrderer.setId(1);
        savedOrderer.setEmail(email);
        savedOrderer.setUsername(username);
        savedOrderer.setPassword(encodedPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(ordererRepository.save(any(Orderer.class))).thenReturn(savedOrderer);

        // Act
        Orderer result = ordererService.saveOrderer(email, username, rawPassword);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedOrderer.getId());
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);

        // Verifica que el método encode fue llamado con la contraseña cruda
        verify(passwordEncoder, times(1)).encode(rawPassword);

        ArgumentCaptor<Orderer> ordererCaptor = ArgumentCaptor.forClass(Orderer.class);
        verify(ordererRepository, times(1)).save(ordererCaptor.capture());
        Orderer ordererPassedToSave = ordererCaptor.getValue();
        assertThat(ordererPassedToSave.getEmail()).isEqualTo(email);
        assertThat(ordererPassedToSave.getUsername()).isEqualTo(username);
        assertThat(ordererPassedToSave.getPassword()).isEqualTo(encodedPassword);

        verifyNoMoreInteractions(passwordEncoder, ordererRepository);
    }

    @Test
    void emailAlreadyTaken_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        String email = "sara@example.com";
        when(ordererRepository.existsByEmailIgnoreCase(email)).thenReturn(true);

        // Act
        Boolean exists = ordererService.emailAlreadyTaken(email);

        // Assert
        assertThat(exists).isTrue();
        verify(ordererRepository, times(1)).existsByEmailIgnoreCase(email);
        verifyNoMoreInteractions(ordererRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void emailAlreadyTaken_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(ordererRepository.existsByEmailIgnoreCase(email)).thenReturn(false);

        // Act
        Boolean exists = ordererService.emailAlreadyTaken(email);

        // Assert
        assertThat(exists).isFalse();
        verify(ordererRepository, times(1)).existsByEmailIgnoreCase(email);
        verifyNoMoreInteractions(ordererRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void usernameAlreadyTaken_ShouldReturnTrue_WhenUsernameExists() {
        // Arrange
        String username = "sara";
        when(ordererRepository.existsByUsernameIgnoreCase(username)).thenReturn(true);

        // Act
        Boolean exists = ordererService.usernameAlreadyTaken(username);

        // Assert
        assertThat(exists).isTrue();
        verify(ordererRepository, times(1)).existsByUsernameIgnoreCase(username);
        verifyNoMoreInteractions(ordererRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void usernameAlreadyTaken_ShouldReturnFalse_WhenUsernameDoesNotExist() {
        // Arrange
        String username = "nonexistentUser";
        when(ordererRepository.existsByUsernameIgnoreCase(username)).thenReturn(false);

        // Act
        Boolean exists = ordererService.usernameAlreadyTaken(username);

        // Assert
        assertThat(exists).isFalse();
        verify(ordererRepository, times(1)).existsByUsernameIgnoreCase(username);
        verifyNoMoreInteractions(ordererRepository);
    }
}
