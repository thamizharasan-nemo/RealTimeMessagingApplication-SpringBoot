package com.example.RealTimeChat.service;

import com.example.RealTimeChat.DTO.ChatMessageDTO;
import com.example.RealTimeChat.DTO.MessageResponseDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.DeleteMessageDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.EditMessageDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.PinMessageDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.RestoreMessageDTO;
import com.example.RealTimeChat.exception.*;
import com.example.RealTimeChat.model.*;
import com.example.RealTimeChat.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class MessageService {

    private final ConversationRepository conversationRepo;
    private final ConversationService conversationService;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final UserService userService;
    private final ParticipantRepository participantRepo;
    private final BlockRepository blockRepo;
    private final BanRepository banRepo;

    public MessageService(ConversationRepository conversationRepo, ConversationService conversationService, MessageRepository messageRepo, UserRepository userRepo, UserService userService, ParticipantRepository participantRepo, BlockRepository blockRepo, BanRepository banRepo) {
        this.conversationRepo = conversationRepo;
        this.conversationService = conversationService;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.userService = userService;
        this.participantRepo = participantRepo;
        this.blockRepo = blockRepo;
        this.banRepo = banRepo;
    }

    public Message getMessageById(int messageId){
        return messageRepo.findById(messageId)
                .orElseThrow(() -> new GenericNotFoundException("Message not found."));
    }

    public Message getDeletedMessageById(int messageId){
        return messageRepo.findDeletedMessage(messageId)
                .orElseThrow(() -> new GenericNotFoundException("Message is not deleted or not found."));
    }

    public Message getMessageByIdWithDetails(int messageId){
        Message msg = messageRepo.findByMessageIdWithDetails(messageId)
                .orElseThrow(() -> new GenericNotFoundException("Message not found."));
        // msg.setReplies might cause a hibernate exception
        msg.setReplies(new HashSet<>(msg.getReplies()));
        return msg;
    }

    public MessageResponseDTO getMessageResponseById(int messageId) {
        Message message = messageRepo.findByMessageIdWithDetails(messageId)
                .orElseThrow(() -> new GenericNotFoundException("Message not found."));
        MessageResponseDTO responseDTO = new MessageResponseDTO();
        return convertToMessageResponseDTO(responseDTO, message, 1);
    }

    public List<MessageResponseDTO> getAllMessagesByConvId(int conversationId){
        if(!conversationRepo.existsById(conversationId)){
            throw new ConversationNotFoundException("Conversation not found. Wrong Id: " + conversationId);
        }
        return messageRepo.findAllMessagesByConversationId(conversationId).stream()
                .map(msg -> {
                    MessageResponseDTO responseDTO = new MessageResponseDTO();
                    convertToMessageResponseDTO(responseDTO, msg, 1);
                    return responseDTO;
                }).toList();
    }

    public void deleteAllMessagesInConversation(List<Message> messageList){
        messageRepo.deleteAll(messageList);
    }

    public List<MessageResponseDTO> getRepliesAsPage(int messageId, int offset, int limit){
        if(!messageRepo.existsById(messageId)){
            throw new GenericNotFoundException("Message not found. msgId is wrong!");
        }
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("createdAt").ascending());
        Page<Message> replies = messageRepo.findReplies(messageId, pageable);
        if(replies.isEmpty()){
            throw new GenericNotFoundException("No replies found.");
        }
        return replies.stream()
                .map(reply -> {
                    MessageResponseDTO responseDTO = new MessageResponseDTO();
                    convertToMessageResponseDTO(responseDTO, reply, 1);
                    return responseDTO;
                })
                .toList();
    }


    public MessageResponseDTO convertToMessageResponseDTO(MessageResponseDTO outgoing, Message message, int depth) {
        outgoing.setMessageId(message.getMessageId());
        outgoing.setConversationId(message.getConversation().getConversationId());
        outgoing.setSenderId(message.getSender().getUserId());
        outgoing.setContent(message.isDeleted() ? "This message was deleted" : message.getContent());
        outgoing.setMessageType(message.getMessageType());
        outgoing.setCreatedAt(message.getCreatedAt());
        outgoing.setSenderName(message.getSender().getNickname());
        // Not a good approach for pinning change it in future
        outgoing.setPinned(message.isPinned() ? "Pinned" : "Not pinned");

        if (message.getReplyTo() != null && depth < 2) {
            outgoing.setReplayToMessageId(message.getReplyTo().getMessageId());
            outgoing.setReplayToContent(
                    message.getReplyTo().isDeleted()
                            ? "This message was deleted"
                            : message.getReplyTo().getContent()
            );
        }

        if (!message.getReplies().isEmpty() && depth < 2) {
            List<MessageResponseDTO> repliesDTO = message.getReplies().stream()
                    .map(reply -> convertToMessageResponseDTO(new MessageResponseDTO(), reply, depth + 1))
                    .toList();

            outgoing.setRepliesResponse(repliesDTO);
        }
        else {
            // If msg have no replies instead of returning an empty list, make it as null
            outgoing.setRepliesResponse(null);
        }
        return outgoing;
    }


    public MessageResponseDTO saveMessage(ChatMessageDTO messageDTO) {
        Conversation conv = conversationService.getConversationById(messageDTO.getConversationId());
        User sender = userService.getUserById(messageDTO.getSenderId());

        ConversationParticipant participant = participantRepo.
                findByConversation_ConversationIdUser_UserId(messageDTO.getConversationId(),
                        messageDTO.getSenderId()
                );
        if (participant == null) {
            throw new BadRequestException("User " + sender.getNickname() + " is not a participant of this conversation.");
        }

        if(conv.getConversationType() == Conversation.ConversationType.PRIVATE){
            Optional<User> receiver = conv.getParticipants().stream()
                    .map(ConversationParticipant::getUser)
                    .filter(u -> u.getUserId() != sender.getUserId())
                    .findFirst();
            if (receiver.isEmpty()){
                throw new UserNotFoundException("No receiver found.");
            }
            if (blockRepo.existsByBlockerAndBlocked(receiver.get(), sender)){
                throw new NotAllowedException("You are blocked by this user");
            }
        }
        // Check if a user banned or not
        if (banRepo.existsByConvIdAndUserId(messageDTO.getConversationId(), messageDTO.getSenderId())){
            throw new NotAllowedException("You're banned from this chat.");
        }

        Message message = new Message();
        message.setConversation(conv);
        message.setSender(sender);

        // For now one can send File or Text, not both
        if(messageDTO.getMessageType() != Message.MessageType.TEXT && messageDTO.getFile() != null){
            try {
                InputStream fileStream = messageDTO.getFile().getInputStream();
                String fileName = System.currentTimeMillis() +"_"+messageDTO.getFile().getOriginalFilename();
                // file name in Path - It essentially creates a full path object pointing to the final file location
                Path filePath = Paths.get(System.getProperty("user.dir") + "\\attachments\\" + fileName);
                // copies the files to the path or use Files.write()
                Files.copy(fileStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                message.setContent(filePath.toString());
            } catch (IOException io){
                throw new RuntimeException("Failed to send file " +io.getMessage());
            }
        }
        // Persist message if text
        else{
            message.setContent(messageDTO.getContent());
        }
        message.setMessageType(messageDTO.getMessageType());
        message.setCreatedAt(Instant.now());

        if(messageDTO.getReplyToMessageId() != 0){
            Message repliedMessage = getMessageByIdWithDetails(messageDTO.getReplyToMessageId());
            repliedMessage.addReply(message);
        }

        if (conv.isDisappearingMsg() && conv.getMsgExpiryInDays() > 0){
            message.setExpiresAt(LocalDateTime.now().plusDays(conv.getMsgExpiryInDays()));
        }

        // setting lastSeen after user sends an message
        sender.setLastSeen(Instant.now());
        userRepo.save(sender);
        Message saved = messageRepo.save(message);
        MessageResponseDTO messageResponse = new MessageResponseDTO();
        convertToMessageResponseDTO(messageResponse, saved, 1);
        return messageResponse;
    }

    public MessageResponseDTO editMessage(EditMessageDTO editMessageDTO) {
        if (conversationService.getConversationById(editMessageDTO.getConversationId()) == null) {
            throw new GenericNotFoundException("Conversation not found");
        }
        if (userService.getUserById(editMessageDTO.getSenderId()) == null) {
            throw new UserNotFoundException("User not found");
        }

        Message messageToEdit = getMessageByIdWithDetails(editMessageDTO.getMessageId());

        if(messageToEdit.getMessageType() != Message.MessageType.TEXT){
            throw new NotAllowedException("Only Text messages can be edited.");
        }

        if (messageToEdit.getConversation().getConversationId() != editMessageDTO.getConversationId()) {
            throw new BadRequestException("Wrong conversation group. ConversationId doesn't match.");
        }
        if (messageToEdit.getSender().getUserId() != editMessageDTO.getSenderId()) {
            throw new BadRequestException("Only the sender can edit their message.");
        }
        ConversationParticipant participant = participantRepo
                .findByConversation_ConversationIdUser_UserId(
                        editMessageDTO.getConversationId(),
                        editMessageDTO.getSenderId()
                );
        if (participant == null) {
            throw new NotAllowedException("User is not a participant of this conversation.");
        }

        if (banRepo.existsByConvIdAndUserId(editMessageDTO.getConversationId(), editMessageDTO.getSenderId())){
            throw new NotAllowedException("You're banned from this chat.");
        }

        messageToEdit.setContent(editMessageDTO.getNewContent());
        messageToEdit.setEditedAt(Instant.now());
        messageRepo.save(messageToEdit);

        MessageResponseDTO messageResponse = new MessageResponseDTO();
        convertToMessageResponseDTO(messageResponse, messageToEdit, 1);
        messageResponse.setEditedAt(messageToEdit.getEditedAt());
        return messageResponse;
    }

    public Page<MessageResponseDTO> getMessages(int conversationId, int requesterId, int pageNo, int pageSize) {
        if (conversationService.getConversationById(conversationId) == null) {
            throw new GenericNotFoundException("Conversation not found");
        }
        if (userService.getUserById(requesterId) == null) {
            throw new UserNotFoundException("User not found");
        }

        ConversationParticipant participant = participantRepo.
                findByConversation_ConversationIdUser_UserId(conversationId, requesterId);
        if (participant == null) {
            throw new BadRequestException("User " + requesterId + " is not a participant of this conversation.");
        }
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Message> chatMessages = messageRepo.findByConversationId(conversationId, pageable);

        return chatMessages.map(message -> {
            MessageResponseDTO messageResponse = new MessageResponseDTO();
            convertToMessageResponseDTO(messageResponse, message, 1);
            return messageResponse;
        });
    }

    public List<Message> getMessageReplies(int messageId){
        return messageRepo.findMessageReplies(messageId);
    }

    public void softDeleteMessage(DeleteMessageDTO deleteMessageDTO) {
        Message message = getMessageByIdWithDetails(deleteMessageDTO.getMessageId());
        User deleter = userService.getUserById(deleteMessageDTO.getUserId());

        ConversationParticipant participant = participantRepo
                .findByConversation_ConversationIdUser_UserId(
                        message.getConversation().getConversationId(),
                        deleteMessageDTO.getUserId());

        if (message.getSender().getUserId() != deleteMessageDTO.getUserId()
                && participant.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new NotAllowedException("Only the sender or admins can delete messages.");
        }

        message.setDeleted(true);
        message.setDeletedBy(deleter.getNickname());
        messageRepo.save(message);
    }

    public void deleteMessagePermanently(DeleteMessageDTO deleteMessageDTO) {
        Message message = getMessageByIdWithDetails(deleteMessageDTO.getMessageId());
        userService.getUserById(deleteMessageDTO.getUserId());

        ConversationParticipant participant = participantRepo
                .findByConversation_ConversationIdUser_UserId(
                        message.getConversation().getConversationId(),
                        deleteMessageDTO.getUserId());
        if (message.getSender().getUserId() != deleteMessageDTO.getUserId()
                && participant.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new NotAllowedException("Only the sender or admins can delete messages.");
        }

        messageRepo.permanentDeletionOfMessage(deleteMessageDTO.getMessageId());
    }

    public MessageResponseDTO restoringMessage(RestoreMessageDTO restoreMessageDTO) {
        Message deletedMessage = messageRepo.findDeletedMessage(restoreMessageDTO.getMessageId())
                .orElseThrow(() -> new GenericNotFoundException("Message not found."));
        if (userService.isExists(restoreMessageDTO.getUserId())){
            throw new UserNotFoundException("User not found");
        }

        ConversationParticipant participant = participantRepo
                .findByConversation_ConversationIdUser_UserId(
                        deletedMessage.getConversation().getConversationId(),
                        restoreMessageDTO.getUserId());
        if (participant == null) {
            throw new NotAllowedException("User has to be the participant of this conversation to restore the message.");
        }
        if (deletedMessage.getSender().getUserId() != restoreMessageDTO.getUserId()) {
            throw new NotAllowedException("Only the sender can undo their deleted messages.");
        }

        deletedMessage.setDeleted(false);
        deletedMessage.setDeletedBy(null);
        messageRepo.save(deletedMessage);
        MessageResponseDTO messageResponse = new MessageResponseDTO();
        return convertToMessageResponseDTO(messageResponse, deletedMessage, 1);
    }

    public MessageResponseDTO pinMessage(PinMessageDTO pinMessageDTO){
        if (!conversationRepo.existsById(pinMessageDTO.getConversationId())){
            throw new ConversationNotFoundException("No conversation found with Id "+pinMessageDTO.getConversationId());
        }
        Message message = messageRepo.findByMessageIdWithDetails(pinMessageDTO.getMessageId())
                .orElseThrow(() -> new GenericNotFoundException("Message not found with id "+ pinMessageDTO.getMessageId()));

        if (conversationRepo.countNoOfPinnedMessageInConv(pinMessageDTO.getConversationId()) > 10) {
            throw new NotAllowedException("Maximum 10 messages can be pinned.");
        }
        if (message.getSender().getUserId() != pinMessageDTO.getUserId()) {
            throw new NotAllowedException("Only sender can pin their messages.");
        }

        boolean alreadyPinned = messageRepo.findPinnedMessagesInConv(pinMessageDTO.getConversationId())
                .stream()
                .anyMatch(pMsg -> pMsg.getMessageId() == pinMessageDTO.getMessageId());
        if(alreadyPinned){
            throw new BadRequestException("Message already pinned!");
        }
        message.setPinned(true);
        messageRepo.save(message);
        return convertToMessageResponseDTO(new MessageResponseDTO(), message, 1);
    }

    public MessageResponseDTO unpinMessage(PinMessageDTO pinMessageDTO) {
        if (!conversationRepo.existsById(pinMessageDTO.getConversationId())){
            throw new ConversationNotFoundException("No conversation found with Id "+pinMessageDTO.getConversationId());
        }
        Message message = messageRepo.findByMessageIdWithDetails(pinMessageDTO.getMessageId())
                .orElseThrow(() -> new GenericNotFoundException("Message not found with id "+ pinMessageDTO.getMessageId()));

        if(!message.isPinned()){
            throw new GenericNotFoundException("Message is not pinned.");
        }
        if (message.getSender().getUserId() != pinMessageDTO.getUserId()) {
            throw new NotAllowedException("Only sender can unpin their messages.");
        }
        message.setPinned(false);
        messageRepo.save(message);
        return convertToMessageResponseDTO(new MessageResponseDTO(), message, 1);
    }
}
