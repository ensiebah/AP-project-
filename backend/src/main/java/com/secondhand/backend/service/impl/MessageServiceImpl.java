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
    public List<MessageDto> getConversationMessages(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));

        return messageRepository.findByConversationOrderBySentAtAsc(conversation)
                .stream().map(this::mapToDto).toList();
    }


    private MessageDto mapToDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .content(message.getText())
                .sentAt(message.getSentAt())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUserName())
                .conversationId(message.getConversation().getId())
                .build();
    }
}