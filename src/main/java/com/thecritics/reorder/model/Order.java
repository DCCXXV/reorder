package com.thecritics.reorder.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "`order`")
@NoArgsConstructor
public class Order implements Transferable<Order.Transfer>{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;
    
    @Convert(converter = ListOfListsConverter.class)
    @Lob
    @Column(columnDefinition = "TEXT")
    private List<List<String>> content;
    
    private String title;
    private String author;

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
