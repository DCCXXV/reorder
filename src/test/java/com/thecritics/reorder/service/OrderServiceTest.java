package com.thecritics.reorder.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
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
        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>()); // Tier 0
        initialState.add(new ArrayList<>()); // Tier 1

        String elementText = "Nuevo elemento";

        // 2. Act; ejecutamos lo que vamos a testear
        when(session.getAttribute("orderState")).thenReturn(initialState);
        List<List<String>> result = orderService.addElement(elementText, session);

        // 3. Assert; comprobamos si los resultados son los esperados
        assertThat(result.getFirst().size()).isEqualTo(1);
        assertThat(result.getFirst().getFirst()).isEqualTo(elementText);
    }

    @Test
    void addElement_ShouldNotAddEmptyElementText() {
        // 1. Arrange; creamos las variables
        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>()); //Tier 0
        initialState.add(new ArrayList<>()); //Tier 1

        String elementText = "";   //vacio

        // 2. Act; ejecutamos lo que vamos a testear
        when(session.getAttribute("orderState")).thenReturn(initialState);
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
        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>());//Añadimos la Unassigned Tier
        initialState.add(new ArrayList<>());//Añadimos la Tier 1

        String elementText = "Elemento a eliminar"; //Creamos un elemento
        initialState.getFirst().add(elementText); //Añadimos un elemento a esa Tier

        // 2. Act; Ejecutamos lo que vamos a Testear
        when(session.getAttribute("orderState")).thenReturn(initialState);
        List<List<String>> result = orderService.deleteElement(elementText, session);

        // 3. Assert; Comprobamos los resultado
        assertThat(result.getFirst().size()).isEqualTo(0);
        assertThat(result.get(1).size()).isEqualTo(0);
    }

    @Test
    void deleteElement_ShouldDeleteElementFromTheAssignedTier() {
        //Inicializamos las variables
        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>());
        initialState.add(new ArrayList<>());

        String elementText = "Elemento a eliminar";
        initialState.get(1).add(elementText);

        //Ejecutamos lo que vamos a Testear
        when(session.getAttribute("orderState")).thenReturn(initialState);
        List<List<String>> result = orderService.deleteElement(elementText, session);

        //Comprobamos los resultados
        assertThat(result.getFirst().size()).isEqualTo(0);
        assertThat(result.get(1).size()).isEqualTo(0);
    }

    @Test
    void deleteElement_ShouldDeleteElementsWithSpecialCharacters(){

        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>());//Añadimos una Tier
        initialState.add(new ArrayList<>());//Añadimos una Tier
        String elementText = "¡¡ \" Elemento a eliminar con carácteres especiales <br> \" !!"; //Creamos un elemento
        initialState.getFirst().add(elementText); //Añadimos un elemento a esa Tier

        when(session.getAttribute("orderState")).thenReturn(initialState);
        List<List<String>> result = orderService.deleteElement(elementText, session);

        assertThat(result.getFirst().size()).isEqualTo(0);

    }

    @Test
    void addTier_ShouldAddTierToEnd() {
        // Arrange
        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>()); // lista 0
        initialState.add(new ArrayList<>()); // lista 1

        when(session.getAttribute("orderState")).thenReturn(initialState);

        // Act
        List<List<String>> result = orderService.addTier(session);

        // Assert
        assertThat(result).hasSize(3); // Se agregó una nueva lista
       //assertThat(result.get(2)).isEmpty(); // la lista/fila  debe estar vacia vacia???
    }


    @Test
    void deleteLastTier_ShouldDeleteLastTierIfFirstTwoExist() {

        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>());//Añadimos una Tier
        initialState.add(new ArrayList<>());//Añadimos otra Tier
        initialState.add(new ArrayList<>());//Añadimos una Tier
        int size = initialState.size();

        when(session.getAttribute("orderState")).thenReturn(initialState);
        List<List<String>> result = orderService.deleteLastTier(session); //Eliminamos la última Tier

        assertThat(result.size()).isEqualTo(size-1); //Comparamos

    }

    @Test
    void deleteLastTier_ShouldDeleteAllElementsInTier(){
        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>()); // Tier 0
        initialState.add(new ArrayList<>()); // Tier 1
        String elementText = "Nuevo Elemento";
        List<String> tier = initialState.getLast();
        initialState.getLast().add(elementText); // Añadimos un elemento a esa Tier
        int size = initialState.size();

        when(session.getAttribute("orderState")).thenReturn(initialState);
        List<List<String>> result = orderService.deleteLastTier(session); // Eliminamos la última Tier (con elemento)

        assertThat(tier.size()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(size-1);
    }


    @Test
    void getOrderState() {
    }

    @Test
    void updateOrderState() {
        
        List<List<String>> initialState = new ArrayList<>();
        initialState.add(new ArrayList<>()); // lista 0
        initialState.add(new ArrayList<>()); // lista 1
        when(session.getAttribute("orderState")).thenReturn(initialState);
        List<List<String>> newState = new ArrayList<>();
        initialState.add(new ArrayList<>()); // lista 0
        List<List<String>> result = orderService.updateOrderState(newState, session);
        assertThat(result).hasSize(1); // 


    }
}