package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.MessageDto;
import com.secondhand.backend.entity.Conversation;
import com.secondhand.backend.entity.Message;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.ConversationNotFoundException;
import com.secondhand.backend.exception.UserNotFoundException;
import com.secondhand.backend.exception.BlockedUserException;
import com.secondhand.backend.repository.ConversationRepository;
import com.secondhand.backend.repository.MessageRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Service implementation responsible for sending
 * and retrieving conversation messages.
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;


    /**
     * Sends a new message in a conversation.
     *
     * @param senderId sender identifier
     * @param conversationId conversation identifier
     * @param content message content
     * @return sent message
     */
    @Override
    public MessageDto sendMessage(Long senderId, Long conversationId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));

        // A valid JWT is not enough: sender must be one of this conversation's
        // two participants (buyer or seller), never a third user.
        ensureConversationParticipant(sender, conversation);

        if (conversation.getBuyer().isBlocked() || conversation.getSeller().isBlocked()) {
            throw new BlockedUserException("Cannot send message. One or both users are blocked.");
        }

        Message message = new Message();
        message.setText(content);
        message.setSentAt(LocalDateTime.now());
        message.setSender(sender);
        message.setConversation(conversation);

        Message savedMessage = messageRepository.save(message);
        return mapToDto(savedMessage);
    }

    /**
     * Retrieves all messages of a conversation.
     *
     * @param conversationId conversation identifier
     * @return list of messages
     */
    @Override
    public List<MessageDto> getConversationMessages(Long viewerId, Long conversationId) {
        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));
        ensureConversationParticipant(viewer, conversation);

        return messageRepository.findByConversationOrderBySentAtAsc(conversation)
                .stream().map(this::mapToDto).toList();
    }

    /**
     * This is intentionally a separate write operation from GET messages.
     * The client calls it only after messages are visible in the chat UI.
     */
    @Override
    @Transactional
    public int markConversationMessagesAsSeen(Long viewerId, Long conversationId) {
        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));
        ensureConversationParticipant(viewer, conversation);

        List<Message> unreadMessages = messageRepository.findByConversationOrderBySentAtAsc(conversation)
                .stream()
                .filter(message -> !message.isSeen())
                .filter(message -> !message.getSender().getId().equals(viewer.getId()))
                .toList();

        LocalDateTime now = LocalDateTime.now();
        unreadMessages.forEach(message -> {
            message.setSeen(true);
            message.setSeenAt(now);
        });
        if (!unreadMessages.isEmpty()) {
            messageRepository.saveAll(unreadMessages);
        }
        return unreadMessages.size();
    }

    private void ensureConversationParticipant(User viewer, Conversation conversation) {
        boolean isBuyer = conversation.getBuyer().getId().equals(viewer.getId());
        boolean isSeller = conversation.getSeller().getId().equals(viewer.getId());
        if (!isBuyer && !isSeller) {
            throw new SecurityException("You are not a participant in this conversation");
        }
    }

    private MessageDto mapToDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .content(message.getText())
                .sentAt(message.getSentAt())
                .seen(message.isSeen())
                .seenAt(message.getSeenAt())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUserName())
                .conversationId(message.getConversation().getId())
                .build();
    }
}