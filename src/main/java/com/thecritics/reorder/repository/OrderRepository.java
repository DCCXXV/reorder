package com.thecritics.reorder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thecritics.reorder.model.Order;
import java.util.List;
import java.sql.Timestamp;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);
    List<Order> findTop5ByTitleContainingIgnoreCase(String title);
    Order findById(int id);
}