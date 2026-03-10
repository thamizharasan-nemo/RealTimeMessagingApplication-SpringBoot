package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.ChatEvent;
import com.example.RealTimeChat.DTO.ChatMessageDTO;
import com.example.RealTimeChat.DTO.ConversationResponseDTO;
import com.example.RealTimeChat.DTO.MessageResponseDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.*;
import com.example.RealTimeChat.component.ChatEventPublisher;
import com.example.RealTimeChat.model.Conversation;
import com.example.RealTimeChat.model.Message;
import com.example.RealTimeChat.security.SecurityUtils;
import com.example.RealTimeChat.service.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;
    private final ConversationService conversationService;
    private final BlockedService blockedService;
    private final ReadReceiptService readReceiptService;
    private final NotificationService notificationService;
    private final PresenceService presenceService;
    private final ChatEventPublisher chatPublisher;
    private final UserService userService;


    public ChatController(SimpMessagingTemplate simpMessagingTemplate, MessageService messageService, ConversationService conversationService, BlockedService blockedService, ReadReceiptService readReceiptService, NotificationService notificationService, PresenceService presenceService, ChatEventPublisher chatPublisher, UserService userService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.messageService = messageService;
        this.conversationService = conversationService;
        this.blockedService = blockedService;
        this.readReceiptService = readReceiptService;
        this.notificationService = notificationService;
        this.presenceService = presenceService;
        this.chatPublisher = chatPublisher;
        this.userService = userService;
    }


    @MessageMapping("chat.send")
    public void processGroupMessage(@Payload ChatMessageDTO chatMessageDTO) {
        int userId = SecurityUtils.getUserId();
        chatMessageDTO.setSenderId(userId);
        MessageResponseDTO savedMessage = messageService.saveMessage(chatMessageDTO);
        Conversation conversation = conversationService.getConversationById(chatMessageDTO.getConversationId());
        chatPublisher.broadcastToConversation(
                chatMessageDTO.getConversationId(),
                userId,
                new ChatEvent<>("MESSAGE_SENT", savedMessage)
        );

        notificationService.notifyParticipants(conversation, savedMessage);
    }

    @MessageMapping("chat.edit")
    public void editMessage(@Payload EditMessageDTO editMessageDTO){
        int userId = SecurityUtils.getUserId();
        editMessageDTO.setSenderId(userId);
        MessageResponseDTO updated = messageService.editMessage(editMessageDTO);
        chatPublisher.broadcastToConversation(
                editMessageDTO.getConversationId(),
                userId,
                new ChatEvent<>("MESSAGE_EDITED", updated)
        );
    }

    @MessageMapping("chat.softDelete")
    public void deleteMessage(@Payload DeleteMessageDTO deleteMessageDTO) {
        int userId = SecurityUtils.getUserId();
        deleteMessageDTO.setUserId(userId);
       messageService.softDeleteMessage(deleteMessageDTO);
       MessageResponseDTO softDeleted = messageService.getMessageResponseById(deleteMessageDTO.getMessageId());
        chatPublisher.broadcastToConversation(
                deleteMessageDTO.getConversationId(),
                userId,
                new ChatEvent<>("MESSAGE_SOFT_DELETED", softDeleted)
        );
    }

    @MessageMapping("chat.hardDelete")
    public void hardDeleteMessage(@Payload DeleteMessageDTO deleteMessageDTO) {
        int userId = SecurityUtils.getUserId();
        deleteMessageDTO.setUserId(userId);
        messageService.deleteMessagePermanently(deleteMessageDTO);
        chatPublisher.broadcastToConversation(
                deleteMessageDTO.getConversationId(),
                userId,
                "MESSAGE_HARD_DELETED"
        );
    }

    @MessageMapping("chat.pin")
    public void pinMessage(@Payload PinMessageDTO pinMessageDTO) {
        int userId = SecurityUtils.getUserId();
        pinMessageDTO.setUserId(userId);
        MessageResponseDTO pinned = messageService.pinMessage(pinMessageDTO);
        chatPublisher.broadcastToConversation(
                pinMessageDTO.getConversationId(),
                userId,
                new ChatEvent<>("MESSAGE_PINNED", pinned)
        );
    }

    @MessageMapping("chat.unpin")
    public void unPinMessage(@Payload PinMessageDTO pinMessageDTO) {
        int userId = SecurityUtils.getUserId();
        pinMessageDTO.setUserId(userId);
        MessageResponseDTO unpinned = messageService.unpinMessage(pinMessageDTO);
        chatPublisher.broadcastToConversation(
                pinMessageDTO.getConversationId(),
                userId,
                new ChatEvent<>("MESSAGE_UNPINNED", unpinned)
        );
    }

    @MessageMapping("chat.updateAvatar")
    public void updateConvAvatarUrl(@Payload UploadAvatarUrlDTO avatarUrlDTO) {
        int userId = SecurityUtils.getUserId();
        avatarUrlDTO.setUserId(userId);
        conversationService.uploadAvatarUrl(avatarUrlDTO);
        ConversationResponseDTO conv = conversationService.getConversationResponseById(avatarUrlDTO.getConversationId());

        chatPublisher.broadcastToConversation(
                avatarUrlDTO.getConversationId(),
                userId,
                new ChatEvent<>("AVATAR_UPDATED", conv)
        );
    }

    @MessageMapping("chat.addParticipant")
    public void addParticipant(@Payload AddParticipantDTO addParticipantDTO) {
        int userId = SecurityUtils.getUserId();
        addParticipantDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.addParticipant(addParticipantDTO);
        chatPublisher.broadcastToConversation(
                addParticipantDTO.getConversationId(),
                userId,
                new ChatEvent<>("PARTICIPANT_ADDED", conv)
        );
    }

    @MessageMapping("chat.removeParticipant")
    public void removeParticipant(@Payload RemoveParticipantDTO removeParticipantDTO) {
        int userId = SecurityUtils.getUserId();
        removeParticipantDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.removeParticipant(removeParticipantDTO);
        chatPublisher.broadcastToConversation(
                removeParticipantDTO.getConversationId(),
                userId,
                new ChatEvent<>("PARTICIPANT_REMOVED", conv)
        );
    }

    @MessageMapping("chat.leave")
    public void leaveConversation(@Payload LeaveConversationDTO leaveConversationDTO) {
        int userId = SecurityUtils.getUserId();
        leaveConversationDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.leaveConversation(leaveConversationDTO);
        chatPublisher.broadcastToConversation(
                leaveConversationDTO.getConversationId(),
                userId,
                new ChatEvent<>("PARTICIPANT_LEFT", conv)
        );
    }

    @MessageMapping("chat.renameTitle")
    public void renameConversationTitle(@Payload RenameConversationTitleDTO renameConvTitleDTO) {
        int userId = SecurityUtils.getUserId();
        renameConvTitleDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.renameConversationTitle(renameConvTitleDTO);
        chatPublisher.broadcastToConversation(
                renameConvTitleDTO.getConversationId(),
                userId,
                new ChatEvent<>("TITLE_RENAMED", conv)
        );
    }

    @MessageMapping("chat.description")
    public void updateConversationDescription(@Payload UpdateConversationDescriptionDTO descriptionDTO) {
        int userId = SecurityUtils.getUserId();
        descriptionDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.updateConversationDescription(descriptionDTO);
        chatPublisher.broadcastToConversation(
                descriptionDTO.getConversationId(),
                userId,
                new ChatEvent<>("DESCRIPTION_UPDATED", conv)
        );
    }

    @MessageMapping("chat.changeRole")
    public void changeParticipantRole(@Payload ChangeParticipantRoleDTO changeRoleDTO) {
        int adminId = SecurityUtils.getUserId();
        changeRoleDTO.setCurrAdminId(adminId);
        conversationService.changeParticipantRole(changeRoleDTO);
        ConversationResponseDTO conv = conversationService.getConversationResponseById(changeRoleDTO.getConversationId());
        chatPublisher.broadcastToConversation(
                changeRoleDTO.getConversationId(),
                adminId,
                new ChatEvent<>("ROLE_CHANGED", conv)
        );
    }

    @MessageMapping("chat.mute")
    public void muteConversation(@Payload MuteConvDTO muteConvDTO) {
        int userId = SecurityUtils.getUserId();
        muteConvDTO.setUserId(userId);
        conversationService.muteConversation(muteConvDTO);
        chatPublisher.broadcastToConversation(
                muteConvDTO.getConversationId(),
                userId,
                "GROUP_MUTED"
        );
    }

    @MessageMapping("chat.unMute")
    public void UnMuteConversation(@Payload UnMuteConvDTO unMuteConvDTO) {
        int userId = SecurityUtils.getUserId();
        unMuteConvDTO.setUserId(userId);
        conversationService.unMuteConversation(unMuteConvDTO);

        chatPublisher.broadcastToConversation(
                unMuteConvDTO.getConversationId(),
                userId,
                "GROUP_UN_MUTED"
        );
    }

    @MessageMapping("chat.favorite")
    public void addOrRemoveAsFavorites(@Payload FavoriteConvDTO favoriteConvDTO) {
        int userId = SecurityUtils.getUserId();
        favoriteConvDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.addOrRemoveAsFavorites(favoriteConvDTO);
        chatPublisher.broadcastToConversation(
                favoriteConvDTO.getConversationId(),
                userId,
                new ChatEvent<>("ADDED_TO_FAV", conv)
        );
    }

    @MessageMapping("chat.archive")
    public void addOrRemoveAsArchive(@Payload ArchiveConvDTO archiveConvDTO) {
        int userId = SecurityUtils.getUserId();
        archiveConvDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.addOrRemoveAsArchive(archiveConvDTO);
        chatPublisher.broadcastToConversation(
                archiveConvDTO.getConversationId(),
                userId,
                new ChatEvent<>("ADDED_TO_ARCHIVE", conv)
        );
    }

    @MessageMapping("chat.block")
    public void blockUser(@Payload BlockingDTO blockingDTO) {
        int blockerId = SecurityUtils.getUserId();
        blockingDTO.setBlockerId(blockerId);
        blockedService.blockUser(blockingDTO);

        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(blockerId),
                "/queue/block",
                "Blocked user Id: "+ blockingDTO.getBlockedId()
        );
    }

    @MessageMapping("chat.unBlock")
    public void unBlockUser(@Payload BlockingDTO blockingDTO) {
        int blockerId = SecurityUtils.getUserId();
        blockingDTO.setBlockerId(blockerId);
        blockedService.unBlockUser(blockingDTO);

        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(blockerId),
                "/queue/unblock",
                "Unblocked user Id: "+blockingDTO.getBlockedId()
        );
    }

    @MessageMapping("chat.moderate")
    public void moderateUser(@Payload GroupModerationDTO moderationDTO){
        int moderatorId = SecurityUtils.getUserId();
        moderationDTO.setModeratorId(moderatorId);
        conversationService.moderateGroupUser(moderationDTO);

        simpMessagingTemplate.convertAndSend(
                "/topic/conversation." + moderationDTO.getConversationId()
        );
    }

    @MessageMapping("chat.read")
    public void markReadReceipt(@Payload ReadReceiptDTO readReceiptDTO){
        Message message = messageService.getMessageByIdWithDetails(readReceiptDTO.getMessageId());
        readReceiptService.markAsRead(readReceiptDTO);
        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(message.getSender().getUserId()),
                "/queue/conversation/"+message.getConversation().getConversationId(),
                readReceiptDTO
        );
    }

}
