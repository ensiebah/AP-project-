package com.secondhand.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ad_comments")

public class AdComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;

    @Column(nullable = false , length = 1000)
    private String content ;

    private LocalDateTime createdAt = LocalDateTime.now(); // مطمئن شوید d در آخر آن وجود دارد

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user ;

    @ManyToOne
    @JoinColumn(name = "advertisement_id")
    private Advertisement advertisement ;


}
