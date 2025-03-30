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
    
    @Query("SELECT o FROM Orderer o WHERE LOWER(o.email) = LOWER(:email)")
    Orderer existsByEmail(@Param ("email") String email);

    @Query("SELECT o FROM Orderer o WHERE LOWER(o.username) = LOWER(:username)")
    Orderer existsByUsername(@Param ("username") String username);

}