package com.thecritics.reorder.service;

import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrderRepository;
import com.thecritics.reorder.repository.OrdererRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class OrdererDetailsServiceTest {

    @InjectMocks
    private OrdererDetailsService ordererDetailsService;

    @Mock
    private OrdererRepository ordererRepository;
 @Mock
    private OrderRepository orderRepository;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        String username = "sara";
        String password = "secretismo";
        Orderer orderer = new Orderer();
        orderer.setUsername(username);
        orderer.setPassword(password);

        when(ordererRepository.findByUsername(username)).thenReturn(orderer);
        
        // Act
        UserDetails userDetails = ordererDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getPassword()).isEqualTo(password);
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");

        verify(ordererRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        String username = "noexiste";
        when(ordererRepository.findByUsername(username)).thenReturn(null);
        // Act & Assert
        assertThatThrownBy(() -> ordererDetailsService.loadUserByUsername(username))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found with username or email: " + username);

        verify(ordererRepository, times(1)).findByUsername(username);
    }
    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenEmailExists() {
        // Arrange
        String email = "sara@example.com";
        String password = "secretismo";
        Orderer orderer = new Orderer();
        orderer.setUsername("sara");
        orderer.setPassword(password);
        orderer.setEmail(email);
        when(ordererRepository.findByEmail(email)).thenReturn(orderer);

        // Act
        UserDetails userDetails = ordererDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(orderer.getUsername());
        assertThat(userDetails.getPassword()).isEqualTo(password);
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");

        verify(ordererRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenEmailDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(ordererRepository.findByEmail(email)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> ordererDetailsService.loadUserByUsername(email))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found with username or email: " + email);

        verify(ordererRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_ShouldHandleNullInput() {
        // Act & Assert
        assertThatThrownBy(() -> ordererDetailsService.loadUserByUsername(null))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found with username or email: null");

        verifyNoInteractions(ordererRepository);
    }

    @Test
    void loadUserByUsername_ShouldHandleEmptyStringInput() {
        // Arrange
        String emptyInput = "";

        // Act & Assert
        assertThatThrownBy(() -> ordererDetailsService.loadUserByUsername(emptyInput))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Input cannot be empty");

        verifyNoInteractions(ordererRepository);
    }
}
