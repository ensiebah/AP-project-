package com.secondhand.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "advertisements")
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;

    @Column(length = 3000)
    private String description;

    private Double price;
    @Enumerated(EnumType.STRING)
    private AdvertisementStatus status;

    @Column(length = 1000)
    private String rejectionReason;

    //each ad has 1 seller , but a seller can have many ads
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    //each cad has 1 cat , but a cat can have many ads
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdvertisementImage> images = new ArrayList<>();
}
