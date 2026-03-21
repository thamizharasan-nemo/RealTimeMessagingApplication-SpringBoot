package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.ChatEvent;
import com.example.RealTimeChat.DTO.ChatMessageDTO;
import com.example.RealTimeChat.DTO.ConversationResponseDTO;
import com.example.RealTimeChat.DTO.MessageResponseDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.*;
import com.example.RealTimeChat.component.ChatEventPublisher;
import com.example.RealTimeChat.model.Conversation;
import com.example.RealTimeChat.model.Message;
import com.example.RealTimeChat.security.CustomUserDetails;
import com.example.RealTimeChat.security.CustomUserDetailsService;
import com.example.RealTimeChat.security.SecurityUtils;
import com.example.RealTimeChat.security.SocketSecurityUtils;
import com.example.RealTimeChat.service.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final ChatEventPublisher chatPublisher;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate, MessageService messageService, ConversationService conversationService, BlockedService blockedService, ReadReceiptService readReceiptService, NotificationService notificationService, ChatEventPublisher chatPublisher) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.messageService = messageService;
        this.conversationService = conversationService;
        this.blockedService = blockedService;
        this.readReceiptService = readReceiptService;
        this.notificationService = notificationService;
        this.chatPublisher = chatPublisher;
    }


    @MessageMapping("chat.send")
    public void processGroupMessage(@Payload ChatMessageDTO chatMessageDTO, Principal principal) {
        System.out.println("====== Message reached the controller "+ chatMessageDTO.getContent()+" =======");
        int userId = SocketSecurityUtils.getUserId(principal);
        chatMessageDTO.setSenderId(userId);
        MessageResponseDTO savedMessage = messageService.saveMessage(chatMessageDTO);
        Conversation conversation = conversationService.getConversationById(chatMessageDTO.getConversationId());
        chatPublisher.broadcastToConversation(
                chatMessageDTO.getConversationId(),
                userId,
                new ChatEvent<>("MESSAGE_SENT", savedMessage)
        );

        System.out.println("====== Message is stored "+ chatMessageDTO.getContent()+" =======");

        notificationService.notifyParticipants(conversation, savedMessage);
    }

    @MessageMapping("chat.edit")
    public void editMessage(@Payload EditMessageDTO editMessageDTO, Principal principal){
        int userId = SocketSecurityUtils.getUserId(principal);
        editMessageDTO.setSenderId(userId);
        MessageResponseDTO updated = messageService.editMessage(editMessageDTO);
        chatPublisher.broadcastToConversation(
                editMessageDTO.getConversationId(),
                userId,
                new ChatEvent<>("MESSAGE_EDITED", updated)
        );
    }

    @MessageMapping("chat.softDelete")
    public void deleteMessage(@Payload DeleteMessageDTO deleteMessageDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
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
    public void hardDeleteMessage(@Payload DeleteMessageDTO deleteMessageDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        deleteMessageDTO.setUserId(userId);
        messageService.deleteMessagePermanently(deleteMessageDTO);
        chatPublisher.broadcastToConversation(
                deleteMessageDTO.getConversationId(),
                userId,
                "MESSAGE_HARD_DELETED"
        );
    }

    @MessageMapping("chat.pin")
    public void pinMessage(@Payload PinMessageDTO pinMessageDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        pinMessageDTO.setUserId(userId);
        MessageResponseDTO pinned = messageService.pinMessage(pinMessageDTO);
        chatPublisher.broadcastToConversation(
                pinMessageDTO.getConversationId(),
                userId,
                new ChatEvent<>("MESSAGE_PINNED", pinned)
        );
    }

    @MessageMapping("chat.unpin")
    public void unPinMessage(@Payload PinMessageDTO pinMessageDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        pinMessageDTO.setUserId(userId);
        MessageResponseDTO unpinned = messageService.unpinMessage(pinMessageDTO);
        chatPublisher.broadcastToConversation(
                pinMessageDTO.getConversationId(),
                userId,
                new ChatEvent<>("MESSAGE_UNPINNED", unpinned)
        );
    }

    @MessageMapping("chat.updateAvatar")
    public void updateConvAvatarUrl(@Payload UploadAvatarUrlDTO avatarUrlDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
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
    public void addParticipant(@Payload AddParticipantDTO addParticipantDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        addParticipantDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.addParticipant(addParticipantDTO);
        chatPublisher.broadcastToConversation(
                addParticipantDTO.getConversationId(),
                userId,
                new ChatEvent<>("PARTICIPANT_ADDED", conv)
        );
    }

    @MessageMapping("chat.removeParticipant")
    public void removeParticipant(@Payload RemoveParticipantDTO removeParticipantDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        removeParticipantDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.removeParticipant(removeParticipantDTO);
        chatPublisher.broadcastToConversation(
                removeParticipantDTO.getConversationId(),
                userId,
                new ChatEvent<>("PARTICIPANT_REMOVED", conv)
        );
    }

    @MessageMapping("chat.leave")
    public void leaveConversation(@Payload LeaveConversationDTO leaveConversationDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        leaveConversationDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.leaveConversation(leaveConversationDTO);
        chatPublisher.broadcastToConversation(
                leaveConversationDTO.getConversationId(),
                userId,
                new ChatEvent<>("PARTICIPANT_LEFT", conv)
        );
    }

    @MessageMapping("chat.renameTitle")
    public void renameConversationTitle(@Payload RenameConversationTitleDTO renameConvTitleDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        renameConvTitleDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.renameConversationTitle(renameConvTitleDTO);
        chatPublisher.broadcastToConversation(
                renameConvTitleDTO.getConversationId(),
                userId,
                new ChatEvent<>("TITLE_RENAMED", conv)
        );
    }

    @MessageMapping("chat.description")
    public void updateConversationDescription(@Payload UpdateConversationDescriptionDTO descriptionDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        descriptionDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.updateConversationDescription(descriptionDTO);
        chatPublisher.broadcastToConversation(
                descriptionDTO.getConversationId(),
                userId,
                new ChatEvent<>("DESCRIPTION_UPDATED", conv)
        );
    }

    @MessageMapping("chat.changeRole")
    public void changeParticipantRole(@Payload ChangeParticipantRoleDTO changeRoleDTO, Principal principal) {
        int adminId = SocketSecurityUtils.getUserId(principal);
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
    public void muteConversation(@Payload MuteConvDTO muteConvDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        muteConvDTO.setUserId(userId);
        conversationService.muteConversation(muteConvDTO);
        chatPublisher.broadcastToConversation(
                muteConvDTO.getConversationId(),
                userId,
                "GROUP_MUTED"
        );
    }

    @MessageMapping("chat.unMute")
    public void UnMuteConversation(@Payload UnMuteConvDTO unMuteConvDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        unMuteConvDTO.setUserId(userId);
        conversationService.unMuteConversation(unMuteConvDTO);

        chatPublisher.broadcastToConversation(
                unMuteConvDTO.getConversationId(),
                userId,
                "GROUP_UN_MUTED"
        );
    }

    @MessageMapping("chat.favorite")
    public void addOrRemoveAsFavorites(@Payload FavoriteConvDTO favoriteConvDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        favoriteConvDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.addOrRemoveAsFavorites(favoriteConvDTO);
        chatPublisher.broadcastToConversation(
                favoriteConvDTO.getConversationId(),
                userId,
                new ChatEvent<>("ADDED_TO_FAV", conv)
        );
    }

    @MessageMapping("chat.archive")
    public void addOrRemoveAsArchive(@Payload ArchiveConvDTO archiveConvDTO, Principal principal) {
        int userId = SocketSecurityUtils.getUserId(principal);
        archiveConvDTO.setUserId(userId);
        ConversationResponseDTO conv = conversationService.addOrRemoveAsArchive(archiveConvDTO);
        chatPublisher.broadcastToConversation(
                archiveConvDTO.getConversationId(),
                userId,
                new ChatEvent<>("ADDED_TO_ARCHIVE", conv)
        );
    }

    @MessageMapping("chat.block")
    public void blockUser(@Payload BlockingDTO blockingDTO, Principal principal) {
        int blockerId = SocketSecurityUtils.getUserId(principal);
        blockingDTO.setBlockerId(blockerId);
        blockedService.blockUser(blockingDTO);

        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(blockerId),
                "/queue/block",
                "Blocked user Id: "+ blockingDTO.getBlockedId()
        );
    }

    @MessageMapping("chat.unBlock")
    public void unBlockUser(@Payload BlockingDTO blockingDTO, Principal principal) {
        int blockerId = SocketSecurityUtils.getUserId(principal);
        blockingDTO.setBlockerId(blockerId);
        blockedService.unBlockUser(blockingDTO);

        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(blockerId),
                "/queue/unblock",
                "Unblocked user Id: "+blockingDTO.getBlockedId()
        );
    }

    @MessageMapping("chat.moderate")
    public void moderateUser(@Payload GroupModerationDTO moderationDTO, Principal principal){
        int moderatorId = SocketSecurityUtils.getUserId(principal);
        moderationDTO.setModeratorId(moderatorId);
        conversationService.moderateGroupUser(moderationDTO);

        simpMessagingTemplate.convertAndSend(
                "/topic/conversation." + moderationDTO.getConversationId()
        );
    }

    // For listing the read users
    @MessageMapping("chat.read")
    public void markReadReceipt(@Payload ReadReceiptDTO readReceiptDTO, Principal principal){
        Message message = messageService.getMessageByIdWithDetails(readReceiptDTO.getMessageId());
        readReceiptService.markAsRead(readReceiptDTO);
        chatPublisher.broadcastToConversation(
                message.getConversation().getConversationId(),
                SecurityUtils.getUserId(),
                new ChatEvent<>("MSG_READ", readReceiptDTO)
        );
    }

    @MessageMapping("chat.typing")
    public void typing(ChatTypingDTO typingDTO, Principal principal) {
        chatPublisher.broadcastToConversation(
                typingDTO.getConversationId(),
                SecurityUtils.getUserId(),
                new ChatEvent<>("USER_TYPING", typingDTO)
        );
    }

}
