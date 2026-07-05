package com.secondhand.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="advertisement_images")
public class AdvertisementImage {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String imagePath;

        @ManyToOne
        @JoinColumn(name="advertisement_id")
        private Advertisement advertisement;
}
