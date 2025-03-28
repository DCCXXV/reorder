package com.thecritics.reorder.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.thecritics.reorder.model.Order;
import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{

    @Query("SELECT o FROM Order o WHERE LOWER(o.title) LIKE LOWER(CONCAT(:query, '%')) ORDER BY o.title ASC")
    List<Order> findTopTitlesStartingWith(@Param("query") String query, Pageable pageable);

    List<Order> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);
    List<Order> findTop5ByTitleContainingIgnoreCase(String title);
    Order findById(int id);
}