package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.MessageDto;
import com.secondhand.backend.entity.Conversation;
import com.secondhand.backend.entity.Message;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.ConversationNotFoundException;
import com.secondhand.backend.exception.UserNotFoundException;
import com.secondhand.backend.repository.ConversationRepository;
import com.secondhand.backend.repository.MessageRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.MessageService;
import com.secondhand.backend.exception.BlockedUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @Override
    public MessageDto sendMessage(
            Long senderId,
            Long conversationId,
            String content
    ) {

        User sender = userRepository.findById(senderId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"));

        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new ConversationNotFoundException(
                                        "Conversation not found"));

        // 👇 تیکه کد جدید و دقیق برای بررسی بلاک بودن اعضای چت هنگام ارسال پیام
        User buyer = conversation.getBuyer();
        User seller = conversation.getSeller();

        if (buyer.isBlocked() || seller.isBlocked()) {
            throw new BlockedUserException(
                    "Cannot send message. One or both users in this conversation are blocked.");
        }

        Message message = new Message();

        message.setText(content);
        message.setSentAt(LocalDateTime.now());

        message.setSender(sender);
        message.setConversation(conversation);

        Message savedMessage =
                messageRepository.save(message);

        return mapToDto(savedMessage);
    }

    @Override
    public List<MessageDto> getConversationMessages(
            Long conversationId
    ) {

        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new ConversationNotFoundException(
                                        "Conversation not found"));

        return messageRepository
                .findByConversationOrderBySentAtAsc(
                        conversation
                )
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private MessageDto mapToDto(
            Message message
    ) {

        return MessageDto.builder()
                .id(message.getId())

                .content(
                        message.getText()
                )

                .sentAt(
                        message.getSentAt()
                )

                .senderId(
                        message.getSender().getId()
                )

                .senderUsername(
                        message.getSender().getUserName()
                )

                .conversationId(
                        message.getConversation().getId()
                )

                .build();
    }
}