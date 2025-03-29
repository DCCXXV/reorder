package com.thecritics.reorder.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ListOfListsConverterTest {

	private ListOfListsConverter converter;

	@BeforeEach
  	void setUp() {
    	converter = new ListOfListsConverter();
  	}

  	@Test
  	void convertToDatabaseColumn_ShouldReturnJsonStringForValidList() {
    	List<List<String>> inputList = Arrays.asList(Arrays.asList("Manzana", "Pera"), Arrays.asList("Naranja", "Plátano"));
    	String expectedJson = "[[\"Manzana\",\"Pera\"],[\"Naranja\",\"Plátano\"]]";

    	String actualJson = converter.convertToDatabaseColumn(inputList);

    	assertThat(actualJson).isEqualTo(expectedJson);
  	}

  	@Test
    void convertToDatabaseColumn_ShouldReturnNullForNullList() {
		List<List<String>> nullList = null;

		String actualJson = converter.convertToDatabaseColumn(nullList);

    	assertThat(actualJson).isNull();
  	}

    @Test
    void convertToEntityAttribute_ShouldReturnListForValidJson() {
        String inputJson = "[[\"Matrix\",\"Inception\"],[\"Parásitos\",\"Memorias de un Caracol\"]]";
  		List<List<String>> expectedList = Arrays.asList(Arrays.asList("Matrix", "Inception"), Arrays.asList("Parásitos", "Memorias de un Caracol"));
    	List<List<String>> actualList = converter.convertToEntityAttribute(inputJson);

    	assertThat(actualList).isEqualTo(expectedList);
  	}

    @Test
    void convertToEntityAttribute_ShouldReturnEmptyListForNullJson() {
		String nullJson = null;

		List<List<String>> actualList = converter.convertToEntityAttribute(nullJson);
		
        assertThat(actualList).isEmpty();
    }

    @Test
    void convertToEntityAttribute_ShouldReturnEmptyListForEmptyJson() {
        String emptyJson = "";
		
        List<List<String>> actualList = converter.convertToEntityAttribute(emptyJson);

        assertThat(actualList).isEmpty();
    }

  	@Test
  	void convertToEntityAttribute_ShouldThrowExceptionForInvalidJson() {
    	String invalidJson = "[[Cerezas,Limón],[Melón,Grosellas]]";
      	assertThrows(
        IllegalArgumentException.class,
        () -> {
            converter.convertToEntityAttribute(invalidJson);
        });
	}
}
