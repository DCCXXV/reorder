package com.thecritics.reorder.service;

import com.thecritics.reorder.model.Orderer;
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

@ExtendWith(MockitoExtension.class)
public class OrdererDetailsServiceTest {

    @InjectMocks
    private OrdererDetailsService ordererDetailsService;

    @Mock
    private OrdererRepository ordererRepository;

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
            .hasMessageContaining("User not found with username: " + username);

        verify(ordererRepository, times(1)).findByUsername(username);
    }
}
