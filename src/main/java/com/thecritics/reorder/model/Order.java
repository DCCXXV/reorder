package com.thecritics.reorder.model;

import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "`orders`")
@NoArgsConstructor
public class Order implements Transferable<Order.Transfer>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private java.sql.Timestamp createdAt;
    
    @Convert(converter = ListOfListsConverter.class)
    @Column(columnDefinition = "TEXT")
    @Basic(fetch = FetchType.EAGER)
    private List<List<String>> content;

    private List<String> previewElements;
    
    private String title;
    private String author;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reordered_order_id")
    private Order reorderedOrder;

    @OneToMany(mappedBy = "reorderedOrder", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Order> reorders;

    public boolean isReorder() {
        return this.reorderedOrder != null;
    }

    @Getter
    @AllArgsConstructor
    public static class Transfer {
        private List<List<String>> content;
        private String title;
        private String author;   
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(content, title, author);
    }

    @Override
    public String toString() {
        return title;
    }
}
