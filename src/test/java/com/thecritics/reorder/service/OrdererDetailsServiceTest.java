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
    void getOrdererDetails_ShouldReturnCorrectDetails_WhenUserExists() {
    // Arrange
    String username = "sara";
    Orderer orderer = new Orderer();
    orderer.setUsername(username);
    orderer.setJoinDate(LocalDate.of(2022, 1, 15));

    Order order1 = new Order();
    order1.setTitle("Order 1");
    order1.setItems(List.of("Item 1", "Item 2", "Item 3"));
    order1.setCreatedAt(LocalDateTime.now().minusDays(1));
    order1.setOrderer(orderer);

    Order order2 = new Order();
    order2.setTitle("Order 2");
    order2.setItems(List.of("Item A", "Item B", "Item C"));
    order2.setCreatedAt(LocalDateTime.now().minusDays(2));
    order2.setOrderer(orderer); 

    Order order3 = new Order();
    order3.setTitle("Order 3");
    order3.setItems(List.of("Item X", "Item Y", "Item Z"));
    order3.setCreatedAt(LocalDateTime.now().minusDays(3));
    order3.setOrderer(orderer);

    List<Order> orders = List.of(order1, order2, order3);

    when(ordererRepository.findByUsername(username)).thenReturn(orderer);
    when(orderRepository.findByOrdererOrderByCreatedAtDesc(orderer)).thenReturn(orders); // Act
    OrdererDetailsService result = ordererDetailsService.getOrdererDetails(username);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo(username);
    assertThat(result.getJoinDate()).isEqualTo(LocalDate.of(2022, 1, 15));
    assertThat(result.getOrderCount()).isEqualTo(3);
    assertThat(result.getRecentOrders())
        .hasSize(3)
        .extracting("title")
        .containsExactly("Order 1", "Order 2", "Order 3");
    assertThat(result.getRecentOrders().get(0).getItems())
        .containsExactly("Item 1", "Item 2", "Item 3");

    verify(ordererRepository, times(1)).findByUsername(username);
    verify(orderRepository, times(1)).findByOrdererOrderByCreatedAtDesc(orderer);
}
@Test
void getOrdererDetails_ShouldThrowException_WhenUserDoesNotExist() {
  // Arrange
        String username = "noexiste";
        when(ordererRepository.findByUsername(username)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> ordererDetailsService.getOrdererDetails(username))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found with username: " + username);

        verify(ordererRepository, times(1)).findByUsername(username);
        verifyNoInteractions(orderRepository); // Ensure orderRepository is not called
    
}
}
