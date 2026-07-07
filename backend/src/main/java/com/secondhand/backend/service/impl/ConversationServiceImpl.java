package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.ConversationDto;
import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.Conversation;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.AdvertisementNotFoundException;
import com.secondhand.backend.exception.BlockedUserException;
import com.secondhand.backend.exception.UserNotFoundException;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.ConversationRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;

    @Override
    public ConversationDto createConversation(Long buyerId, Long advertisementId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new UserNotFoundException("Buyer not found"));

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("Advertisement not found"));

        User seller = advertisement.getSeller();

        if (buyer.isBlocked() || seller.isBlocked()) {
            throw new BlockedUserException("Blocked users cannot create conversations");
        }

        if (buyer.getId().equals(seller.getId())) {
            throw new IllegalArgumentException("You cannot create conversation for your own advertisement");
        }

        Optional<Conversation> existingConversation = conversationRepository
                .findByBuyerAndSellerAndAdvertisement(buyer, seller, advertisement);

        if (existingConversation.isPresent()) {
            return mapToDto(existingConversation.get());
        }

        Conversation conversation = new Conversation();
        conversation.setBuyer(buyer);
        conversation.setSeller(seller);
        conversation.setAdvertisement(advertisement);

        Conversation savedConversation = conversationRepository.save(conversation);
        return mapToDto(savedConversation);
    }

    @Override
    public List<ConversationDto> getUserConversations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Conversation> conversations = conversationRepository.findConversationsByUser(user);
        return conversations.stream().map(this::mapToDto).toList();
    }


    private ConversationDto mapToDto(Conversation conversation) {
        return ConversationDto.builder()
                .id(conversation.getId())
                .buyerId(conversation.getBuyer().getId())
                .buyerUsername(conversation.getBuyer().getUserName())
                .sellerId(conversation.getSeller().getId())
                .sellerUsername(conversation.getSeller().getUserName())
                .advertisementId(conversation.getAdvertisement().getId())
                .advertisementTitle(conversation.getAdvertisement().getTitle())
                .build();
    }
}