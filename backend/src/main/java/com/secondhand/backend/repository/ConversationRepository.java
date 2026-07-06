package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.Conversation;
import com.secondhand.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation,Long> {
    List<Conversation> findByBuyer(User buyer) ;
    List<Conversation> findBySeller(User seller) ;
    Optional<Conversation> findByBuyerAndSellerAndAdvertisement(
            User buyer,
            User seller,
            Advertisement advertisement
    );
}

