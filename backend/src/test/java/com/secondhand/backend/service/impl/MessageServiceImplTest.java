package com.secondhand.backend.service.impl;

import com.secondhand.backend.entity.Conversation;
import com.secondhand.backend.entity.Message;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.ConversationRepository;
import com.secondhand.backend.repository.MessageRepository;
import com.secondhand.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock private MessageRepository messageRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private MessageServiceImpl messageService;

    @Test
    void sendMessage_WhenSenderIsNotConversationParticipant_ShouldRejectWithoutSaving() {
        User buyer = user(1L, "buyer");
        User seller = user(2L, "seller");
        User outsider = user(3L, "outsider");
        Conversation conversation = conversation(buyer, seller);

        when(userRepository.findById(3L)).thenReturn(Optional.of(outsider));
        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));

        assertThrows(
                SecurityException.class,
                () -> messageService.sendMessage(3L, 10L, "I should not be here")
        );

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void sendMessage_WhenBuyerIsConversationParticipant_CanSaveMessage() {
        User buyer = user(1L, "buyer");
        User seller = user(2L, "seller");
        Conversation conversation = conversation(buyer, seller);
        Message saved = new Message();
        saved.setId(20L);
        saved.setText("Hello");
        saved.setSender(buyer);
        saved.setConversation(conversation);

        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(saved);

        messageService.sendMessage(1L, 10L, "Hello");

        verify(messageRepository).save(any(Message.class));
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUserName(username);
        user.setBlocked(false);
        return user;
    }

    private Conversation conversation(User buyer, User seller) {
        Conversation conversation = new Conversation();
        conversation.setId(10L);
        conversation.setBuyer(buyer);
        conversation.setSeller(seller);
        return conversation;
    }
}
