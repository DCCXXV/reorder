package com.thecritics.reorder.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.thecritics.reorder.TestcontainersConfiguration;
import com.thecritics.reorder.model.Orderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
public class OrdererRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrdererRepository ordererRepository;

    private Orderer testOrderer;
    private int testOrdererId;

    @BeforeEach
    void setUp() {
        ordererRepository.deleteAll();

        testOrderer = new Orderer();
        testOrderer.setEmail("test@example.com");
        testOrderer.setUsername("testUser");
        testOrderer.setPassword("Password123");
        Orderer persistedOrderer = entityManager.persistFlushFind(testOrderer);
        testOrdererId = (int) persistedOrderer.getId();
    }

    @Test
    void save_WhenValidOrdererIsSaved_ShouldBePersisted() {
        Orderer newOrderer = new Orderer();
        newOrderer.setEmail("new@example.com");
        newOrderer.setUsername("newUser");
        newOrderer.setPassword("NewPass456");

        Orderer savedOrderer = ordererRepository.save(newOrderer);

        assertThat(savedOrderer).isNotNull();
        assertThat(savedOrderer.getId()).isNotNull(); // Assuming ID is generated
        Orderer foundOrderer = entityManager.find(
            Orderer.class,
            savedOrderer.getId()
        );
        assertThat(foundOrderer).isNotNull();
        assertThat(foundOrderer.getEmail()).isEqualTo(newOrderer.getEmail());
    }

    @Test
    void findById_WhenOrdererExistsForId_ShouldReturnOrderer() {
        Orderer found = ordererRepository.findById(testOrdererId);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(testOrdererId);
        assertThat(found.getUsername()).isEqualTo(testOrderer.getUsername());
    }

    @Test
    void findById_WhenOrdererDoesNotExistForId_ShouldReturnNull() {
        int nonExistentId = testOrdererId + 999999; // An ID guaranteed not to exist
        Orderer found = ordererRepository.findById(nonExistentId);

        assertThat(found).isNull();
    }

    @Test
    void existsByEmailIgnoreCase_WhenEmailExistsCaseInsensitive_ShouldReturnTrue() {
        boolean exists = ordererRepository.existsByEmailIgnoreCase(
            "TEST@EXAMPLE.COM"
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailIgnoreCase_WhenEmailDoesNotExist_ShouldReturnFalse() {
        boolean exists = ordererRepository.existsByEmailIgnoreCase(
            "nonexistent@example.com"
        );

        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsernameIgnoreCase_WhenUsernameExistsCaseInsensitive_ShouldReturnTrue() {
        boolean exists = ordererRepository.existsByUsernameIgnoreCase("TESTUSER");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsernameIgnoreCase_WhenUsernameDoesNotExist_ShouldReturnFalse() {
        boolean exists = ordererRepository.existsByUsernameIgnoreCase(
            "nonexistentuser"
        );

        assertThat(exists).isFalse();
    }
}
