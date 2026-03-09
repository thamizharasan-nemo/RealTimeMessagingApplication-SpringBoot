package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.ChatEvent;
import com.example.RealTimeChat.DTO.ChatMessageDTO;
import com.example.RealTimeChat.DTO.ConversationResponseDTO;
import com.example.RealTimeChat.DTO.MessageResponseDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.*;
import com.example.RealTimeChat.component.ChatEventPublisher;
import com.example.RealTimeChat.model.Conversation;
import com.example.RealTimeChat.model.Message;
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


    private int resolveUserId(Principal principal){
        return userService.findByUsername(principal.getName()).getUserId();
    }

    @MessageMapping("chat.send")
    public void processGroupMessage(@Payload ChatMessageDTO chatMessageDTO, Principal principal) {
        MessageResponseDTO savedMessage = messageService.saveMessage(chatMessageDTO);
        Conversation conversation = conversationService.getConversationById(chatMessageDTO.getConversationId());
        chatPublisher.broadcastToConversation(
                chatMessageDTO.getConversationId(),
                chatMessageDTO.getSenderId(),
                new ChatEvent<>("MESSAGE_EDITED", savedMessage)
        );

        notificationService.notifyParticipants(conversation, savedMessage);
    }

    @MessageMapping("chat.edit")
    public void editMessage(@Payload EditMessageDTO editMessageDTO, Principal principal){
        MessageResponseDTO updated = messageService.editMessage(editMessageDTO);
        chatPublisher.broadcastToConversation(
                editMessageDTO.getConversationId(),
                editMessageDTO.getSenderId(),
                new ChatEvent<>("MESSAGE_EDITED", updated)
        );
    }

    @MessageMapping("chat.softDelete")
    public void deleteMessage(@Payload DeleteMessageDTO deleteMessageDTO, Principal principal) {
       messageService.softDeleteMessage(deleteMessageDTO);
       MessageResponseDTO softDeleted = messageService.getMessageResponseById(deleteMessageDTO.getMessageId());
        chatPublisher.broadcastToConversation(
                deleteMessageDTO.getConversationId(),
                deleteMessageDTO.getUserId(),
                softDeleted
        );
    }

    @MessageMapping("chat.hardDelete")
    public void hardDeleteMessage(@Payload DeleteMessageDTO deleteMessageDTO, Principal principal) {
        messageService.deleteMessagePermanently(deleteMessageDTO);
        chatPublisher.broadcastToConversation(
                deleteMessageDTO.getConversationId(),
                deleteMessageDTO.getUserId(),
                "Message deleted successfully"
        );
    }

    @MessageMapping("chat.pin")
    public void pinMessage(@Payload PinMessageDTO pinMessageDTO, Principal principal) {
        MessageResponseDTO pinned = messageService.pinMessage(pinMessageDTO);
        chatPublisher.broadcastToConversation(
                pinMessageDTO.getConversationId(),
                pinMessageDTO.getUserId(),
                pinned
        );
    }

    @MessageMapping("chat.unpin")
    public void unPinMessage(@Payload PinMessageDTO pinMessageDTO, Principal principal) {
        MessageResponseDTO unpinned = messageService.unpinMessage(pinMessageDTO);
        chatPublisher.broadcastToConversation(
                pinMessageDTO.getConversationId(),
                pinMessageDTO.getUserId(),
                unpinned
        );
    }

    @MessageMapping("chat.updateAvatar")
    public void updateConvAvatarUrl(@Payload UploadAvatarUrlDTO avatarUrlDTO, Principal principal) {
        conversationService.uploadAvatarUrl(avatarUrlDTO);
        ConversationResponseDTO conv = conversationService.getConversationResponseById(avatarUrlDTO.getConversationId());

        chatPublisher.broadcastToConversation(
                avatarUrlDTO.getConversationId(),
                avatarUrlDTO.getUserId(),
                conv
        );
    }

    @MessageMapping("chat.addParticipant")
    public void addParticipant(@Payload AddParticipantDTO addParticipantDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.addParticipant(addParticipantDTO);
        chatPublisher.broadcastToConversation(
                addParticipantDTO.getConversationId(),
                addParticipantDTO.getUserId(),
                conv
        );
    }

    @MessageMapping("chat.removeParticipant")
    public void removeParticipant(@Payload RemoveParticipantDTO removeParticipantDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.removeParticipant(removeParticipantDTO);
        chatPublisher.broadcastToConversation(
                removeParticipantDTO.getConversationId(),
                removeParticipantDTO.getUserId(),
                conv
        );
    }

    @MessageMapping("chat.leave")
    public void leaveConversation(@Payload LeaveConversationDTO leaveConversationDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.leaveConversation(leaveConversationDTO);
        chatPublisher.broadcastToConversation(
                leaveConversationDTO.getConversationId(),
                leaveConversationDTO.getUserId(),
                conv
        );
    }

    @MessageMapping("chat.renameTitle")
    public void renameConversationTitle(@Payload RenameConversationTitleDTO renameConvTitleDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.renameConversationTitle(renameConvTitleDTO);
        chatPublisher.broadcastToConversation(
                renameConvTitleDTO.getConversationId(),
                renameConvTitleDTO.getUserId(),
                conv
        );
    }

    @MessageMapping("chat.description")
    public void updateConversationDescription(@Payload UpdateConversationDescriptionDTO descriptionDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.updateConversationDescription(descriptionDTO);
        chatPublisher.broadcastToConversation(
                descriptionDTO.getConversationId(),
                descriptionDTO.getUserId(),
                conv
        );
    }

    @MessageMapping("chat.changeRole")
    public void changeParticipantRole(@Payload ChangeParticipantRoleDTO changeRoleDTO, Principal principal) {
        conversationService.changeParticipantRole(changeRoleDTO);
        ConversationResponseDTO conv = conversationService.getConversationResponseById(changeRoleDTO.getConversationId());
        chatPublisher.broadcastToConversation(
                changeRoleDTO.getConversationId(),
                changeRoleDTO.getCurrAdminId(),
                conv
        );
    }

    @MessageMapping("chat.mute")
    public void muteConversation(@Payload MuteConvDTO muteConvDTO, Principal principal) {
        conversationService.muteConversation(muteConvDTO);
        chatPublisher.broadcastToConversation(
                muteConvDTO.getConversationId(),
                muteConvDTO.getUserId(),
                "Group muted"
        );
    }

    @MessageMapping("chat.unMute")
    public void UnMuteConversation(@Payload UnMuteConvDTO unMuteConvDTO, Principal principal) {
        conversationService.unMuteConversation(unMuteConvDTO);

        chatPublisher.broadcastToConversation(
                unMuteConvDTO.getConversationId(),
                unMuteConvDTO.getUserId(),
                "GROUP UN MUTED"
        );
    }

    @MessageMapping("chat.favorite")
    public void addOrRemoveAsFavorites(@Payload FavoriteConvDTO favoriteConvDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.addOrRemoveAsFavorites(favoriteConvDTO);
        chatPublisher.broadcastToConversation(
                favoriteConvDTO.getConversationId(),
                favoriteConvDTO.getUserId(),
                conv
        );
    }

    @MessageMapping("chat.archive")
    public void addOrRemoveAsArchive(@Payload ArchiveConvDTO archiveConvDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.addOrRemoveAsArchive(archiveConvDTO);
        chatPublisher.broadcastToConversation(
                archiveConvDTO.getConversationId(),
                archiveConvDTO.getUserId(),
                conv
        );
    }

    @MessageMapping("chat.block")
    public void blockUser(@Payload BlockingDTO blockingDTO, Principal principal) {
        blockedService.blockUser(blockingDTO);

        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(blockingDTO.getBlockerId()),
                "/queue/block",
                "Blocked user Id: "+ blockingDTO.getBlockedId()
        );
    }

    @MessageMapping("chat.unBlock")
    public void unBlockUser(@Payload BlockingDTO blockingDTO, Principal principal) {
        blockedService.unBlockUser(blockingDTO);

        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(blockingDTO.getBlockerId()),
                "/queue/unblock",
                "Unblocked user Id: "+blockingDTO.getBlockedId()
        );
    }

    @MessageMapping("chat.moderate")
    public void moderateUser(@Payload GroupModerationDTO moderationDTO){
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
