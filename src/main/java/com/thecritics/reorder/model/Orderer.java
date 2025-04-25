package com.thecritics.reorder.model;

import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "`orderer`")
@NoArgsConstructor
public class Orderer implements Transferable<Orderer.Transfer>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private java.sql.Timestamp createdAt;
    
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, unique = false)
    private String password;

    @OneToMany
    private List<Order> orders;

    @Getter
    @AllArgsConstructor
    public static class Transfer {
        private String username;
        private String email;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(username, email);
    }

    @Override
    public String toString() {
        return username;
    }
}
