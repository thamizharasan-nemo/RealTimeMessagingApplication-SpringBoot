package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.DTO.ChatMessageDTO;
import com.example.RealTimeChat.DTO.ConversationResponseDTO;
import com.example.RealTimeChat.DTO.MessageResponseDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.*;
import com.example.RealTimeChat.model.Conversation;
import com.example.RealTimeChat.model.Message;
import com.example.RealTimeChat.service.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;
    private final ConversationService conversationService;
    private final BlockedService blockedService;
    private final ReadReceiptService readReceiptService;
    private final NotificationService notificationService;


    public ChatController(SimpMessagingTemplate simpMessagingTemplate, MessageService messageService, ConversationService conversationService, BlockedService blockedService, ReadReceiptService readReceiptService, NotificationService notificationService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.messageService = messageService;
        this.conversationService = conversationService;
        this.blockedService = blockedService;
        this.readReceiptService = readReceiptService;
        this.notificationService = notificationService;
    }


    @MessageMapping("chat.send")
    public void processGroupMessage(@Payload ChatMessageDTO chatMessageDTO, Principal principal) {
        MessageResponseDTO savedMessage = messageService.saveMessage(chatMessageDTO);
        Conversation conversation = conversationService.getConversationById(chatMessageDTO.getConversationId());
        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessageDTO.getConversationId(),
                    savedMessage);
        } else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(chatMessageDTO.getConversationId(), chatMessageDTO.getSenderId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + chatMessageDTO.getConversationId(),
                    savedMessage
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(chatMessageDTO.getSenderId()),
                    "/queue/conversation." + chatMessageDTO.getConversationId(),
                    savedMessage
            );
        }

        notificationService.notifyParticipants(conversation, savedMessage);
    }

    @MessageMapping("chat.sendPrivate")
    public void processPrivateMessage(@Payload ChatMessageDTO chatMessageDTO, Principal principal) {
        MessageResponseDTO savedMessage = messageService.saveMessage(chatMessageDTO);
        simpMessagingTemplate.convertAndSend("/queue/conversation." + chatMessageDTO.getConversationId(),
                savedMessage);
    }

    @MessageMapping("chat.edit")
    public void editMessage(@Payload EditMessageDTO editMessageDTO, Principal principal){
        MessageResponseDTO updated = messageService.editMessage(editMessageDTO);
        Conversation conversation = conversationService.getConversationById(editMessageDTO.getConversationId());
        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + editMessageDTO.getConversationId(),
                    updated);
        } else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(editMessageDTO.getConversationId(), editMessageDTO.getSenderId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + editMessageDTO.getConversationId(),
                    updated
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(editMessageDTO.getSenderId()),
                    "/queue/conversation." + editMessageDTO.getConversationId(),
                    updated
            );
        }
    }

    @MessageMapping("chat.softDelete")
    public void deleteMessage(@Payload DeleteMessageDTO deleteMessageDTO, Principal principal) {
       messageService.softDeleteMessage(deleteMessageDTO);
       MessageResponseDTO softDeleted = messageService.getMessageResponseById(deleteMessageDTO.getMessageId());
       Conversation conversation = conversationService.getConversationById(deleteMessageDTO.getConversationId());
        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + deleteMessageDTO.getConversationId(),
                    softDeleted);
        } else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(deleteMessageDTO.getConversationId(), deleteMessageDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + deleteMessageDTO.getConversationId(),
                    softDeleted
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(deleteMessageDTO.getUserId()),
                    "/queue/conversation." + deleteMessageDTO.getConversationId(),
                    softDeleted
            );
        }
    }

    @MessageMapping("chat.hardDelete")
    public void hardDeleteMessage(@Payload DeleteMessageDTO deleteMessageDTO, Principal principal) {
        messageService.deleteMessagePermanently(deleteMessageDTO);
        Conversation conversation = conversationService.getConversationById(deleteMessageDTO.getConversationId());
        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + deleteMessageDTO.getConversationId());
        } else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(deleteMessageDTO.getConversationId(), deleteMessageDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + deleteMessageDTO.getConversationId(),
                    String.valueOf(deleteMessageDTO.getUserId() )
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(deleteMessageDTO.getUserId()),
                    "/queue/conversation." + deleteMessageDTO.getConversationId(),
                    String.valueOf(receiverId)
            );
        }
    }

    @MessageMapping("chat.pin")
    public void pinMessage(@Payload PinMessageDTO pinMessageDTO, Principal principal) {
        MessageResponseDTO pinned = messageService.pinMessage(pinMessageDTO);
        Conversation conversation = conversationService.getConversationById(pinMessageDTO.getConversationId());
        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + pinMessageDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(pinMessageDTO.getConversationId(), pinMessageDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + pinMessageDTO.getConversationId(),
                    pinned
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(pinMessageDTO.getUserId()),
                    "/queue/conversation." + pinMessageDTO.getConversationId(),
                    pinned
            );
        }
    }

    @MessageMapping("chat.unpin")
    public void unPinMessage(@Payload PinMessageDTO pinMessageDTO, Principal principal) {
        MessageResponseDTO unpinned = messageService.unpinMessage(pinMessageDTO);
        Conversation conversation = conversationService.getConversationById(pinMessageDTO.getConversationId());
        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + pinMessageDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(pinMessageDTO.getConversationId(), pinMessageDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + pinMessageDTO.getConversationId(),
                    unpinned
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(pinMessageDTO.getUserId()),
                    "/queue/conversation." + pinMessageDTO.getConversationId(),
                    unpinned
            );
        }
    }

    @MessageMapping("chat.updateAvatar")
    public void updateConvAvatarUrl(@Payload UploadAvatarUrlDTO avatarUrlDTO, Principal principal) {
        conversationService.uploadAvatarUrl(avatarUrlDTO);
        ConversationResponseDTO conv = conversationService.getConversationResponseById(avatarUrlDTO.getConversationId());
        Conversation conversation = conversationService.getConversationById(avatarUrlDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + avatarUrlDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(avatarUrlDTO.getConversationId(), avatarUrlDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + avatarUrlDTO.getConversationId(),
                    conv
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(avatarUrlDTO.getUserId()),
                    "/queue/conversation." + avatarUrlDTO.getConversationId(),
                    conv
            );
        }
    }

    @MessageMapping("chat.addParticipant")
    public void addParticipant(@Payload AddParticipantDTO addParticipantDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.addParticipant(addParticipantDTO);
        Conversation conversation = conversationService.getConversationById(addParticipantDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + addParticipantDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(addParticipantDTO.getConversationId(), addParticipantDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + addParticipantDTO.getConversationId(),
                    conv
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(addParticipantDTO.getUserId()),
                    "/queue/conversation." + addParticipantDTO.getConversationId(),
                    conv
            );
        }
    }

    @MessageMapping("chat.removeParticipant")
    public void removeParticipant(@Payload RemoveParticipantDTO removeParticipantDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.removeParticipant(removeParticipantDTO);
        Conversation conversation = conversationService.getConversationById(removeParticipantDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + removeParticipantDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(removeParticipantDTO.getConversationId(), removeParticipantDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + removeParticipantDTO.getConversationId(),
                    conv
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(removeParticipantDTO.getUserId()),
                    "/queue/conversation." + removeParticipantDTO.getConversationId(),
                    conv
            );
        }
    }

    @MessageMapping("chat.leave")
    public void leaveConversation(@Payload LeaveConversationDTO leaveConversationDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.leaveConversation(leaveConversationDTO);
        Conversation conversation = conversationService.getConversationById(leaveConversationDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + leaveConversationDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(leaveConversationDTO.getConversationId(), leaveConversationDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + leaveConversationDTO.getConversationId(),
                    conv
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(leaveConversationDTO.getUserId()),
                    "/queue/conversation." + leaveConversationDTO.getConversationId(),
                    conv
            );
        }
    }

    @MessageMapping("chat.renameTitle")
    public void renameConversationTitle(@Payload RenameConversationTitleDTO renameConvTitleDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.renameConversationTitle(renameConvTitleDTO);
        Conversation conversation = conversationService.getConversationById(renameConvTitleDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + renameConvTitleDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(renameConvTitleDTO.getConversationId(), renameConvTitleDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + renameConvTitleDTO.getConversationId(),
                    conv
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(renameConvTitleDTO.getUserId()),
                    "/queue/conversation." + renameConvTitleDTO.getConversationId(),
                    conv
            );
        }
    }

    @MessageMapping("chat.description")
    public void updateConversationDescription(@Payload UpdateConversationDescriptionDTO descriptionDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.updateConversationDescription(descriptionDTO);
        Conversation conversation = conversationService.getConversationById(descriptionDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + descriptionDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(descriptionDTO.getConversationId(), descriptionDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + descriptionDTO.getConversationId(),
                    conv
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(descriptionDTO.getUserId()),
                    "/queue/conversation." + descriptionDTO.getConversationId(),
                    conv
            );
        }
    }

    @MessageMapping("chat.changeRole")
    public void changeParticipantRole(@Payload ChangeParticipantRoleDTO changeRoleDTO, Principal principal) {
        conversationService.changeParticipantRole(changeRoleDTO);
        ConversationResponseDTO conv = conversationService.getConversationResponseById(changeRoleDTO.getConversationId());
        Conversation conversation = conversationService.getConversationById(changeRoleDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + changeRoleDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = changeRoleDTO.getTargetUserId();
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + changeRoleDTO.getConversationId(),
                    conv
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(changeRoleDTO.getCurrAdminId()),
                    "/queue/conversation." + changeRoleDTO.getConversationId(),
                    conv
            );
        }
    }

    @MessageMapping("chat.mute")
    public void muteConversation(@Payload MuteConvDTO muteConvDTO, Principal principal) {
        conversationService.muteConversation(muteConvDTO);
        Conversation conversation = conversationService.getConversationById(muteConvDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + muteConvDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(muteConvDTO.getConversationId(), muteConvDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + muteConvDTO.getConversationId(),
                    String.valueOf(muteConvDTO.getUserId())
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(muteConvDTO.getUserId()),
                    "/queue/conversation." + muteConvDTO.getConversationId(),
                    String.valueOf(receiverId)
            );
        }
    }

    @MessageMapping("chat.unMute")
    public void UnMuteConversation(@Payload UnMuteConvDTO unMuteConvDTO, Principal principal) {
        conversationService.unMuteConversation(unMuteConvDTO);
        Conversation conversation = conversationService.getConversationById(unMuteConvDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + unMuteConvDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(unMuteConvDTO.getConversationId(), unMuteConvDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + unMuteConvDTO.getConversationId(),
                    String.valueOf(unMuteConvDTO.getUserId())
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(unMuteConvDTO.getUserId()),
                    "/queue/conversation." + unMuteConvDTO.getConversationId(),
                    String.valueOf(receiverId)
            );
        }
    }

    @MessageMapping("chat.favorite")
    public void addOrRemoveAsFavorites(@Payload FavoriteConvDTO favoriteConvDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.addOrRemoveAsFavorites(favoriteConvDTO);
        Conversation conversation = conversationService.getConversationById(favoriteConvDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + favoriteConvDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(favoriteConvDTO.getConversationId(), favoriteConvDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + favoriteConvDTO.getConversationId(),
                    conv
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(favoriteConvDTO.getUserId()),
                    "/queue/conversation." + favoriteConvDTO.getConversationId(),
                    conv
            );
        }
    }

    @MessageMapping("chat.archive")
    public void addOrRemoveAsArchive(@Payload ArchiveConvDTO archiveConvDTO, Principal principal) {
        ConversationResponseDTO conv = conversationService.addOrRemoveAsArchive(archiveConvDTO);
        Conversation conversation = conversationService.getConversationById(archiveConvDTO.getConversationId());

        if (conversation.getConversationType() == Conversation.ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation." + archiveConvDTO.getConversationId());
        }
        else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            int receiverId = conversationService.getReceiverId(archiveConvDTO.getConversationId(), archiveConvDTO.getUserId());
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/conversation." + archiveConvDTO.getConversationId(),
                    conv
            );

            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(archiveConvDTO.getUserId()),
                    "/queue/conversation." + archiveConvDTO.getConversationId(),
                    conv
            );
        }
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
                "/topic/conversation." + moderationDTO.getModeratorId()
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
