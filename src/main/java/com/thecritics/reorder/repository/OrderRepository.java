package com.thecritics.reorder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thecritics.reorder.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
    
} 