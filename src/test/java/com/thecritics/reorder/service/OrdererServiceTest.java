package com.thecritics.reorder.service;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thecritics.reorder.model.Orderer;
// Assuming you have an Order class, import it if needed
// import com.thecritics.reorder.model.Order;
import com.thecritics.reorder.repository.OrdererRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList; // Import ArrayList
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;

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

    // Removed @Mock Orderer orderer; - It's generally better to create
    // specific instances in each test method as needed.

    @Test
    void findByUsername_ShouldReturnOrderer_WhenUsernameExists() {
        // Arrange
        String username = "Julia";
        Orderer mockOrderer = new Orderer();
        mockOrderer.setUsername(username);
        // FIX: Initialize the orders list to prevent NPE in toTransfer()
        mockOrderer.setOrders(new ArrayList<>()); // Or however orders are set/initialized
        when(ordererRepository.findByUsername(username)).thenReturn(mockOrderer);

        // Act
        Orderer.Transfer result = ordererService.findByUsername(username);

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
        Orderer.Transfer result = ordererService.findByUsername(username);

        // Assert
        assertThat(result).isNull(); // Service should handle null from repo
        verify(ordererRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByEmail_ShouldReturnOrderer_WhenEmailExists() {
        // Arrange
        String email = "correo@example.com";
        Orderer mockOrderer = new Orderer();
        mockOrderer.setEmail(email);
        // FIX: Initialize the orders list
        mockOrderer.setOrders(new ArrayList<>());
        when(ordererRepository.findByEmail(email)).thenReturn(mockOrderer);

        // Act
        Orderer.Transfer result = ordererService.findByEmail(email);

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
        Orderer.Transfer result = ordererService.findByEmail(email);

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

        Orderer savedOrderer = new Orderer();
        savedOrderer.setId(1);
        savedOrderer.setEmail(email);
        savedOrderer.setUsername(username);
        savedOrderer.setPassword(encodedPassword);
        savedOrderer.setOrders(new ArrayList<>());

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

        verify(passwordEncoder, times(1)).encode(rawPassword);

        ArgumentCaptor<Orderer> ordererCaptor = ArgumentCaptor.forClass(
            Orderer.class
        );
        verify(ordererRepository, times(1)).save(ordererCaptor.capture());
        Orderer ordererPassedToSave = ordererCaptor.getValue();

        assertThat(ordererPassedToSave.getEmail()).isEqualTo(email);
        assertThat(ordererPassedToSave.getUsername()).isEqualTo(username);
        assertThat(ordererPassedToSave.getPassword()).isEqualTo(encodedPassword);
        assertThat(ordererPassedToSave.getOrders()).isNotNull();

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
        when(ordererRepository.existsByUsernameIgnoreCase(username)).thenReturn(
            true
        );

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
        when(ordererRepository.existsByUsernameIgnoreCase(username)).thenReturn(
            false
        );

        // Act
        Boolean exists = ordererService.usernameAlreadyTaken(username);

        // Assert
        assertThat(exists).isFalse();
        verify(ordererRepository, times(1)).existsByUsernameIgnoreCase(username);
        verifyNoMoreInteractions(ordererRepository);
    }

    @Test
    void getOrderersByUsername_ShouldReturnTrue_WhenOrderersMatchingWithAPartialInput() {
        // Arrange
        String partialUsername = "an";
        String username1 = "Aniceto";
        String username2 = "Antonia";

        Orderer orderer1 = new Orderer();
        orderer1.setUsername(username1);
        // FIX: Initialize orders list
        orderer1.setOrders(new ArrayList<>());

        Orderer orderer2 = new Orderer();
        orderer2.setUsername(username2);
        // FIX: Initialize orders list
        orderer2.setOrders(new ArrayList<>());

        List<Orderer> expectedList = List.of(orderer1, orderer2);
        when(ordererRepository.findByUsernameContainingIgnoreCase(partialUsername))
            .thenReturn(expectedList);

        // Act
        List<Orderer.Transfer> result = ordererService.getOrderersByUsername(
            partialUsername
        );

        // Assert
        assertThat(result)
            .hasSize(2)
            .extracting(Orderer.Transfer::getUsername) // Easier assertion
            .containsExactlyInAnyOrder(username1, username2);
        // Original assertion also works if Orderer.Transfer has equals/hashCode
        // assertThat(result).contains(orderer1.toTransfer(), orderer2.toTransfer());
        verify(ordererRepository, times(1))
            .findByUsernameContainingIgnoreCase(partialUsername);
    }

    @Test
    void getOrderersByUsername_ShouldReturnEmptyList_WhenNoMatchFound() {
        // Arrange
        String partialUsername = "xyz";
        when(ordererRepository.findByUsernameContainingIgnoreCase(partialUsername))
            .thenReturn(List.of());

        // Act
        List<Orderer.Transfer> result = ordererService.getOrderersByUsername(
            partialUsername
        );

        // Assert
        assertThat(result).isEmpty();
        verify(ordererRepository, times(1))
            .findByUsernameContainingIgnoreCase(partialUsername);
    }

    @Test
    void getOrderersByUsername_ShouldReturnAll_WhenEmptyStringProvided() {
        // Arrange
        String partialUsername = "";
        String username1 = "Carlos";
        String username2 = "Lucía";

        Orderer orderer1 = new Orderer();
        orderer1.setUsername(username1);
        // FIX: Initialize orders list
        orderer1.setOrders(new ArrayList<>());

        Orderer orderer2 = new Orderer();
        orderer2.setUsername(username2);
        // FIX: Initialize orders list
        orderer2.setOrders(new ArrayList<>());

        List<Orderer> expectedList = List.of(orderer1, orderer2);
        when(ordererRepository.findByUsernameContainingIgnoreCase(partialUsername))
            .thenReturn(expectedList);

        // Act
        List<Orderer.Transfer> result = ordererService.getOrderersByUsername(
            partialUsername
        );

        // Assert
        assertThat(result)
            .hasSize(2)
            .extracting(Orderer.Transfer::getUsername)
            .containsExactlyInAnyOrder(username1, username2);
        // Original assertion:
        // assertThat(result).containsExactlyInAnyOrderElementsOf(expectedList.stream().map(Orderer::toTransfer).toList());
        verify(ordererRepository, times(1))
            .findByUsernameContainingIgnoreCase(partialUsername);
    }
}
