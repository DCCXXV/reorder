package com.thecritics.reorder.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private HttpSession session;

    /**
     * Verifica que addElement añade un elemento a la tier no ordenada del estado del Order.
     *
     * <p>Configura un estado inicial con dos tier vacías y verifica que el elemento
     * se añade correctamente a la primera lista.</p>
     */
    @Test
    void addElement_ShouldAddElementToUnassignedTier() {
        // 1. Arrange; creamos las variables
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // Tier 0
        initialOrderState.add(new ArrayList<>()); // Tier 1

        String elementText = "Nuevo elemento";

        // 2. Act; ejecutamos lo que vamos a testear
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.addElement(elementText, session);

        // 3. Assert; comprobamos si los resultados son los esperados
        assertThat(result.getFirst().size()).isEqualTo(1);
        assertThat(result.getFirst().getFirst()).isEqualTo(elementText);
    }

    @Test
    void addElement_ShouldNotAddEmptyElementText() {
        // 1. Arrange; creamos las variables
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); //Tier 0
        initialOrderState.add(new ArrayList<>()); //Tier 1

        String elementText = "";   //vacio

        // 2. Act; ejecutamos lo que vamos a testear
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.addElement(elementText, session);

        // 3. Assert; comprobamos si los resultados son los esperados
        assertThat(result.getFirst().size()).isEqualTo(0);
    }

    /**
     * Verificamos que se elimina un elemento de la assigned tier
     * 
     * <p>Configura un estado inicial con dos tier vacías y verifica que el elemento
     * se elimina correctamente de la assigned tier.</p>
     * 
     */
    @Test
    void deleteElement_ShouldDeleteElementFromTheUnassignedTier() {
        // 1. Arrange; Creamos las variables
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());//Añadimos la Unassigned Tier
        initialOrderState.add(new ArrayList<>());//Añadimos la Tier 1

        String elementText = "Elemento a eliminar"; //Creamos un elemento
        initialOrderState.getFirst().add(elementText); //Añadimos un elemento a esa Tier

        // 2. Act; Ejecutamos lo que vamos a Testear
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.deleteElement(elementText, session);

        // 3. Assert; Comprobamos los resultado
        assertThat(result.getFirst().size()).isEqualTo(0);
        assertThat(result.get(1).size()).isEqualTo(0);
    }

    @Test
    void deleteElement_ShouldDeleteElementFromTheAssignedTier() {
        //Inicializamos las variables
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());
        initialOrderState.add(new ArrayList<>());

        String elementText = "Elemento a eliminar";
        initialOrderState.get(1).add(elementText);
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        //Ejecutamos lo que vamos a Testear
        List<List<String>> result = orderService.deleteElement(elementText, session);

        //Comprobamos los resultados
        assertThat(result.getFirst().size()).isEqualTo(0);
        assertThat(result.get(1).size()).isEqualTo(0);
    }

    @Test
    void deleteElement_ShouldDeleteElementsWithSpecialCharacters(){
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // Tier 0
        initialOrderState.add(new ArrayList<>()); // Tier 1
        String elementText = "¡¡ \" Elemento a eliminar con carácteres especiales <h1> \" !!"; // Creamos un elemento
        initialOrderState.getFirst().add(elementText); // Añadimos un elemento a esa Tier
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        List<List<String>> result = orderService.deleteElement(elementText, session);

        assertThat(result.getFirst().size()).isEqualTo(0);
    }

    @Test
    void addTier_ShouldAddTierToEnd() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // lista 0
        initialOrderState.add(new ArrayList<>()); // lista 1
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        List<List<String>> result = orderService.addTier(session);

        assertThat(result).hasSize(3);
    }


    @Test
    void deleteLastTier_ShouldDeleteLastTierIfFirstTwoExist() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // Tier 0 (unassigned)
        initialOrderState.add(new ArrayList<>()); // Tier 1
        initialOrderState.add(new ArrayList<>()); // Tier 2
        int numberOfTiers = initialOrderState.size();
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        List<List<String>> result = orderService.deleteLastTier(session); //Eliminamos la última Tier

        assertThat(result).hasSize(numberOfTiers-1);
    }

    @Test
    void deleteLastTier_ShouldDeleteLastTierAndAllElementsInTier(){
        // configurar estado inicial
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>()); // Tier 0  (unassigned)
        initialOrderState.add(new ArrayList<>()); // Tier 1
        initialOrderState.add(new ArrayList<>()); // Tier 2

        // añadir elemento al estado inicial
        String elementText = "Nuevo Elemento";
        initialOrderState.getLast().add(elementText);

        when(session.getAttribute("orderState")).thenReturn(initialOrderState);

        List<List<String>> result = orderService.deleteLastTier(session);

        assertThat(result.getLast().size()).isZero();
    }

    @Test
    void getOrderState_ShouldGetOrderWithElementsAndTiersCorrectly() {
        //Arrange
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(new ArrayList<>());

        initialOrderState.add(Arrays.asList("Plátano", "Manzana")); //Tier 0
        initialOrderState.add(Arrays.asList("Naranja", "Pera")); //Tier 1

        //Act
        when(session.getAttribute("orderState")).thenReturn(initialOrderState);
        List<List<String>> result = orderService.getOrderState(session);

        //Assert
        for (int i = 0; i < initialOrderState.size(); i++){
            assertThat(result.get(i)).isEqualTo(initialOrderState.get(i));
        }

    }

    @Test
    void updateOrderState_ShouldUpdateSessionAttributeAndReturnUpdatedState() {
        List<List<String>> initialOrderState = new ArrayList<>();
        initialOrderState.add(Arrays.asList("Manzana", "Pera"));        // Tier 0
        initialOrderState.add(Arrays.asList("Naranja"));                // Tier 1
        
        List<List<String>> newOrderState = new ArrayList<>();
        newOrderState.add(Arrays.asList("Naranja", "Pera", "Sandía"));  // nuevo Tier 0
        newOrderState.add(Arrays.asList("Manzana", "Limón"));           // nuevo Tier 1
        newOrderState.add(Arrays.asList());                             // nuevo Tier 2
                
        // Actualizamos el Order del estado inicial al nuevo
        List<List<String>> result = orderService.updateOrderState(newOrderState, session);
        
        verify(session).setAttribute("orderState", newOrderState);
        assertThat(result).isSameAs(newOrderState);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).containsExactly("Naranja", "Pera", "Sandía");
        assertThat(result.get(1)).containsExactly("Manzana", "Limón");
    }
}