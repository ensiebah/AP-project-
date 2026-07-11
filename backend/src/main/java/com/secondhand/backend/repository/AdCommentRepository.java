package com.secondhand.backend.repository;

import com.secondhand.backend.entity.AdComment;
import com.secondhand.backend.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdCommentRepository extends JpaRepository<AdComment, Long> {
    List<AdComment> findByAdvertisementOrderByCreatedAtDesc(Advertisement advertisement);
}