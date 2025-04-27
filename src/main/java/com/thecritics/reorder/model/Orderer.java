package com.thecritics.reorder.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
public class Orderer implements Transferable<Orderer.Transfer> {

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

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
        joinColumns = @JoinColumn(name = "orderer_id"),
        inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private List<Orderer> followers = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
        joinColumns = @JoinColumn(name = "orderer_id"),
        inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private List<Orderer> following = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Order> orders;

    @Getter
    @AllArgsConstructor
    public static class Transfer {
        private String username;
        private String email;
        private int numOrders;
        private java.sql.Timestamp createdAt;
        private List<Order.Transfer> orders;
        private int followersCount;
        private int followingCount;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(username, email, orders.size(), createdAt, orders.stream().map(Order::toTransfer).toList(), followers.size(), following.size());
    }

    @Override
    public String toString() {
        return username;
    }
}
