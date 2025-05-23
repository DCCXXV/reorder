package com.thecritics.reorder.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thecritics.reorder.model.Orderer;
import java.util.List;


@Repository
public interface OrdererRepository extends JpaRepository<Orderer, Long>{
    Orderer findByUsername(String username);
    Orderer findByEmail(String email);
    
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    List <Orderer> findByUsernameContainingIgnoreCase(String username);
    Orderer findById(int id);
    
    @Query("SELECT o FROM Orderer o WHERE LOWER(o.username) LIKE LOWER(CONCAT(:query, '%')) ORDER BY o.username ASC")
    List<Orderer> findTopUsernamesStartingWith(@Param("query") String query, Pageable pageable);
}