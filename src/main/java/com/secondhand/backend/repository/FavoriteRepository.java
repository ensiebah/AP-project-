package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.Favorite;
import com.secondhand.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite,Long> {

    List<Favorite> findByUser(User user);

    boolean existsByUserAndAdvertisement(User user,
                                         Advertisement advertisement);

}