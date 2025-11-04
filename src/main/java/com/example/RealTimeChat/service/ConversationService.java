package com.example.RealTimeChat.service;

import com.example.RealTimeChat.DTO.ConversationDTO;
import com.example.RealTimeChat.DTO.ConversationResponseDTO;
import com.example.RealTimeChat.DTO.ParticipantResponseDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.*;
import com.example.RealTimeChat.exception.*;
import com.example.RealTimeChat.model.*;
import com.example.RealTimeChat.repository.BanRepository;
import com.example.RealTimeChat.repository.MessageRepository;
import com.example.RealTimeChat.repository.ParticipantRepository;
import com.example.RealTimeChat.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepo;
    private final ParticipantRepository participantRepo;
    private final UserService userService;
    private final MessageRepository messageRepo;
    private final BlockedService blockedService;
    private final BanRepository banRepo;
    private BanService banService;

    /* BanService caused cyclic dependency error, ConversationService -> BanService -> ConversationService
    So I used field injection/setter to workaround with circular dependency.
    */
    @Autowired
    public void setBanService(@Lazy BanService banService){
        this.banService = banService;
    }

    public BanService getBanService(){
        return banService;
    }

    public ConversationService(ConversationRepository conversationRepo, ParticipantRepository participantRepo, UserService userService, MessageRepository messageRepo, BlockedService blockedService, BanRepository banRepo) {
        this.conversationRepo = conversationRepo;
        this.participantRepo = participantRepo;
        this.userService = userService;
        this.messageRepo = messageRepo;
        this.blockedService = blockedService;
        this.banRepo = banRepo;
    }

    public List<Conversation> getAllConversation() {
        return conversationRepo.findAll();
    }

    public List<ConversationResponseDTO> getAllConversationAsDTO() {
        return conversationRepo.findAll().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public Conversation getConversationById(int convId) {
        return conversationRepo.findById(convId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation with id " + convId + " not found."));
    }

    public ConversationResponseDTO getConversationResponseById(int convId) {
        return convertToResponseDTO(getConversationById(convId));
    }

    public List<ConversationResponseDTO> getConversationByTitle(String title) {
        List<Conversation> conversations = conversationRepo.findByConversationName(title);
        return conversations.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // For getting all message Ids including reply messages
    public List<Integer> getAllMessageIdsInThisConv(int conversationId) {
        if (!conversationRepo.existsById(conversationId)) {
            throw new ConversationNotFoundException("No conversation found with id: " + conversationId);
        }
        return conversationRepo.findAllMessageIdsOfThisConvId(conversationId);
    }

    //Parent message ids / message that have replies but not replying to any message
    public List<Integer> getAllNoReplyToMessageIdsInConv(int conversationId) {
        if (!conversationRepo.existsById(conversationId)) {
            throw new ConversationNotFoundException("No conversation found with id: " + conversationId);
        }
        return messageRepo.findAllMessagesWithNoReplyInConv(conversationId);
    }

    // Permanent deletion of all messages in a conversation
    public void deleteAllMessagesInThisConv(int conversationId) {
        if (!conversationRepo.existsById(conversationId)) {
            throw new ConversationNotFoundException("No conversation found with id: " + conversationId);
        }

        // method to get all message Ids that are not replying to any msg
        getAllNoReplyToMessageIdsInConv(conversationId)
                .forEach(msgId -> {
                    // Normal check for msg existence
                    if (!messageRepo.existsById(msgId)) {
                        throw new GenericNotFoundException("Message not exists." + msgId);
                    }

                    // Method for deleting replies first
                    deleteMessageReplies(msgId);
                    // Then deletion of parent msg
                    messageRepo.permanentDeletionOfMessage(msgId);

                });
    }

    // For deleting msg's replies before deleting the parent msg
    // Note: It's not for deleting a parent message or an independent msg
    public void deleteMessageReplies(int messageId){
        Message parentMsg = messageRepo.findMessageByIdWithReplies(messageId);
        List<Message> replies = List.copyOf(parentMsg.getReplies());
        if(!parentMsg.getReplies().isEmpty()){
            replies.forEach(msg -> {
                if(msg.getReplies().isEmpty()) { // if reply msg doesn't have a reply , just delete
                    messageRepo.permanentDeletionOfMessage(msg.getMessageId());
                }
                else {
                    // if a reply has reply then recursively delete reply
                    deleteMessageReplies(msg.getMessageId());
                }
            });
        }
    }

    // Delete a group entirely
    public void deleteConvEntirely(int conversationId){
        if (!conversationRepo.existsById(conversationId)) {
            throw new ConversationNotFoundException("No conversation found with id: " + conversationId);
        }
        // First delete messages in con
        deleteAllMessagesInThisConv(conversationId);
        // Then delete conv
        conversationRepo.deleteById(conversationId);
    }

    public ConversationResponseDTO convertToResponseDTO(Conversation conversation) {
        ConversationResponseDTO responseDTO = new ConversationResponseDTO();
        responseDTO.setConversationId(conversation.getConversationId());
        responseDTO.setTitle(conversation.getTitle());
        responseDTO.setConversationType(conversation.getConversationType());
        responseDTO.setConvCreatorName(conversation.getConvCreator().getNickname());
        responseDTO.setCreatedAt(conversation.getCreatedAt());
        responseDTO.setDescription(conversation.getDescription());
        responseDTO.setAvatarUrl(conversation.getAvatarUrl() == null
                ? "No image"
                : conversation.getAvatarUrl());

        List<ConversationParticipant> participants = participantRepo.findByConversationId(conversation.getConversationId());
        responseDTO.setParticipants(participants.stream()
                .map(p -> {
                    ParticipantResponseDTO prDto = new ParticipantResponseDTO();
                    prDto.setPId(p.getCpId());
                    prDto.setUserId(p.getUser().getUserId());
                    prDto.setNickname(p.getUser().getNickname());
                    prDto.setRole(p.getParticipantRole().name());
                    return prDto;
                })
                .toList());

        return responseDTO;
    }

    public Conversation convertFromDtoToConversation(Conversation conversation, ConversationDTO conversationDTO, MultipartFile file) {
        conversation.setTitle(conversationDTO.getTitle());
        conversation.setDescription(conversationDTO.getDescription());
        conversation.setConversationType(conversationDTO.getConversationType());

        User creator = userService.getUserById(conversationDTO.getConvCreatorId());
        conversation.setConvCreator(creator);

        if(file != null && !file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                Path path = Paths.get(System.getProperty("user.dir") + "\\uploads\\" + fileName);
                Files.write(path, file.getBytes());
                System.out.println("\nFile written to uploads\n");

                String avatarUrl = path.toString();
                conversation.setAvatarUrl(avatarUrl);
            } catch (IOException io) {
                throw new RuntimeException("Error while uploading! "+io.getMessage());
            }
        }

        // Save conversation first (to get generated ID)
        Conversation savedConv = conversationRepo.save(conversation);

        // Add creator as first participant
        ConversationParticipant cp = new ConversationParticipant();
        cp.setConversation(savedConv);
        cp.setUser(creator);
        cp.setParticipantRole(ParticipantRole.ADMIN); // creator will always be an admin
        participantRepo.save(cp);

        // add participants next if exists
        if (conversationDTO.getParticipantIds() != null
                && !conversationDTO.getParticipantIds().isEmpty()) {
            for (Integer participantId : conversationDTO.getParticipantIds()) {
                User participant = userService.getUserById(participantId);
                ConversationParticipant member = new ConversationParticipant();
                member.setConversation(conversation);
                member.setUser(participant);
                member.setParticipantRole(ParticipantRole.MEMBER);
                member.setAddedBy(creator);
                participantRepo.save(member);
            }
        }

        return conversation;
    }

    // CREATE Conversation
    public ConversationResponseDTO createConversation(ConversationDTO conversationDTO, MultipartFile file) {
        Conversation conversation = new Conversation();
        Conversation savedConv = convertFromDtoToConversation(conversation, conversationDTO, file);
        return convertToResponseDTO(savedConv);
    }

    public String uploadAvatarUrl(UploadAvatarUrlDTO avatarUrlDTO) {
        Conversation conversation = getConversationById(avatarUrlDTO.getConversationId());
        ConversationParticipant cp = participantRepo
                .findByConversation_ConversationIdUser_UserId(avatarUrlDTO.getConversationId(), avatarUrlDTO.getUserId());
        if (cp == null){
            throw new UserNotFoundException("User not a participant of this group.");
        }
        if(cp.getParticipantRole() != ParticipantRole.ADMIN){
            throw new NotAllowedException("Only Admins can upload or change group avatar.");
        }
        try {
            String fileName = avatarUrlDTO.getFile().getOriginalFilename();
            Path path = Paths.get(System.getProperty("user.dir") + "\\uploads\\" + fileName);
            Files.write(path, avatarUrlDTO.getFile().getBytes());
            String avatarUrl = path.toString();
            conversation.setAvatarUrl(avatarUrl);
            conversationRepo.save(conversation);
            return avatarUrl;
        } catch (IOException io) {
            throw new GenericNotFoundException("Failed to upload: "+io.getMessage());
        }
    }

    // For Dev checking purpose
    public ConversationResponseDTO addParticipantWithoutAdmin(int conversationId,
                                                              int userId,
                                                              ParticipantRole role) {
        Conversation conversation = getConversationById(conversationId);
        User user = userService.getUserById(userId);

        if(conversation.getConversationType() == Conversation.ConversationType.PRIVATE){
            throw new BadRequestException("Can't add people to an ONE to ONE chat.");
        }

        if (participantRepo.findByConversation_ConversationIdUser_UserId(conversationId, userId) != null) {
            throw new BadRequestException("User already a participant of this group.");
        }

        ConversationParticipant cp = new ConversationParticipant();
        cp.setConversation(conversation);
        cp.setUser(user);
        cp.setParticipantRole(
                role == ParticipantRole.ADMIN
                        ? ParticipantRole.ADMIN
                        : ParticipantRole.MEMBER
        );

        participantRepo.save(cp);   // CASCADE take care of other mapping things
        return convertToResponseDTO(conversationRepo.save(conversation));
    }

    // For Dev checking purpose
    public ConversationResponseDTO removeParticipantWithoutAdmin(int conversationId,
                                                              int userId) {
        Conversation conversation = getConversationById(conversationId);
        if (!userService.isExists(userId)){
            throw new UserNotFoundException("User not found.");
        }

        if(conversation.getConversationType() == Conversation.ConversationType.PRIVATE){
            throw new BadRequestException("Can't remove people from an ONE to ONE chat.");
        }
        ConversationParticipant participant = participantRepo
                .findByConversation_ConversationIdUser_UserId(conversationId, userId);
        if (participant == null) {
            throw new BadRequestException("User not a participant in this conversation");
        }

        participantRepo.delete(participant); // explicit way to remove user from the db
        return convertToResponseDTO(conversation);
    }

    public ConversationResponseDTO addParticipant(AddParticipantDTO addParticipantDTO) {
        Conversation conversation = getConversationById(addParticipantDTO.getConversationId());
        User user = userService.getUserById(addParticipantDTO.getUserId());

        if(conversation.getConversationType() == Conversation.ConversationType.PRIVATE){
            throw new BadRequestException("Can't add people to an ONE to ONE chat.");
        }

        if(banRepo.existsByConvIdAndUserId(addParticipantDTO.getConversationId(), addParticipantDTO.getUserId())){
            throw new NotAllowedException("User is banned from this group. Can't add");
        }

        if(blockedService.isBlocked(addParticipantDTO.getAdminId(), addParticipantDTO.getUserId())
                || blockedService.isBlocked(addParticipantDTO.getUserId(), addParticipantDTO.getAdminId())){
            throw new ForbiddenException("Either one blocked the other one");
        }

        ConversationParticipant admin = participantRepo
                .findByConversation_ConversationIdUser_UserId(addParticipantDTO.getConversationId(), addParticipantDTO.getAdminId());
        if (admin.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new NotAllowedException("Only admins can add participants.");
        }

        if (participantRepo.findByConversation_ConversationIdUser_UserId(addParticipantDTO.getConversationId(),
                addParticipantDTO.getUserId()) != null) {
            throw new BadRequestException("User already a participant of this group.");
        }

        ConversationParticipant cp = new ConversationParticipant();
        cp.setConversation(conversation);
        cp.setUser(user);
        cp.setParticipantRole(
                addParticipantDTO.getRole() == ParticipantRole.ADMIN
                        ? ParticipantRole.ADMIN
                        : ParticipantRole.MEMBER
        );

        participantRepo.save(cp);   // Just saving cp will add the cp to the conversation because of CASCADE
//        conversation.getParticipants().add(cp); // this line is not needed  // JPA will manage the relationship after saving cp
        return convertToResponseDTO(conversationRepo.save(conversation));
    }

    public ConversationResponseDTO removeParticipant(RemoveParticipantDTO removeDTO) {
        Conversation conversation = getConversationById(removeDTO.getConversationId());
        User user = userService.getUserById(removeDTO.getUserId());

        ConversationParticipant admin = participantRepo
                .findByConversation_ConversationIdUser_UserId(removeDTO.getConversationId(), removeDTO.getAdminId());
        if (admin.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new NotAllowedException("Only admins can remove participants.");
        }

        ConversationParticipant participant = participantRepo
                .findByConversation_ConversationIdUser_UserId(removeDTO.getConversationId(), removeDTO.getUserId());
        if (participant == null) {
            throw new BadRequestException("User not a participant in this conversation");
        }

        // the below line works because of relationship mapping cascade = CascadeType.ALL, orphanRemoval = true
        // But it causes Lazy loading and serialization issue
        // conversation.getParticipants().remove(participant);
        participantRepo.delete(participant); // explicit way to remove user from the db
        return convertToResponseDTO(conversation);
    }

    public ConversationResponseDTO leaveConversation(LeaveConversationDTO leaveConvDTO) {
        if (!conversationRepo.existsById(leaveConvDTO.getConversationId())) {
            throw new ConversationNotFoundException("Conversation not found");
        }
        ConversationParticipant participant = participantRepo
                .findByConversation_ConversationIdUser_UserId(leaveConvDTO.getConversationId(), leaveConvDTO.getUserId());
        if (participant == null) {
            throw new NotAllowedException("User is a not part of this conversation");
        }

        if (participantRepo.countNoOfAdminsInConv(leaveConvDTO.getConversationId()) <= 1) {
            participantRepo.delete(participant);
            List<ConversationParticipant> participantList = participantRepo.findByConversationId(leaveConvDTO.getConversationId());
            participantList.sort((p1, p2) ->
                    p1.getCpId() < p2.getCpId() ? -1 : 1); // is this line actually important
            ConversationParticipant newAdmin = participantList.stream().iterator().next();
            newAdmin.setParticipantRole(ParticipantRole.ADMIN);
            participantRepo.save(newAdmin);

            return convertToResponseDTO(getConversationById(leaveConvDTO.getConversationId()));
        }

        // If only one participant left, delete conversation entirely
        if (participantRepo.countNoOfParticipantsInConv(leaveConvDTO.getConversationId()) <= 1) {
            participantRepo.delete(participant);

            getAllMessageIdsInThisConv(leaveConvDTO.getConversationId()).forEach(messageRepo::permanentDeletionOfMessage);

            conversationRepo.deleteById(leaveConvDTO.getConversationId());
            return new ConversationResponseDTO("Group is deleted because the last participant leaved the group.");
        }
        // else delete that particular member
        participantRepo.delete(participant); // delete participant from table. Same like above method

        Conversation updatedConv = getConversationById(leaveConvDTO.getConversationId());
        return convertToResponseDTO(updatedConv);
    }

    public ConversationResponseDTO renameConversationTitle(RenameConversationTitleDTO renameTitleDTO) {
        Conversation conversation = getConversationById(renameTitleDTO.getConversationId());
        ConversationParticipant cp = participantRepo.findByConversation_ConversationIdUser_UserId(renameTitleDTO.getConversationId(), renameTitleDTO.getUserId());

        if (cp.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new NotAllowedException("Only admins can change the group name.");
        }
        if (conversation.getConversationType() != Conversation.ConversationType.GROUP) {
            throw new NotAllowedException("Only Groups title can be renamed.");
        }
        conversation.setTitle(renameTitleDTO.getTitle());
        return convertToResponseDTO(conversationRepo.save(conversation));
    }


    public ConversationResponseDTO updateConversationDescription(UpdateConversationDescriptionDTO updateDescriptionDTO) {
        Conversation conversation = getConversationById(updateDescriptionDTO.getConversationId());
        ConversationParticipant cp = participantRepo.findByConversation_ConversationIdUser_UserId(updateDescriptionDTO.getConversationId(), updateDescriptionDTO.getUserId());

        if (cp.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new NotAllowedException("Only admins can update the group description.");
        }
        if (conversation.getConversationType() != Conversation.ConversationType.GROUP) {
            throw new NotAllowedException("Only Groups description can be renamed.");
        }
        conversation.setDescription(updateDescriptionDTO.getDescription());
        return convertToResponseDTO(conversationRepo.save(conversation));
    }

    public void changeParticipantRole(ChangeParticipantRoleDTO changeRoleDTO) {
        ConversationParticipant currentAdmin = participantRepo
                .findByConversation_ConversationIdUser_UserId(changeRoleDTO.getConversationId(),
                        changeRoleDTO.getCurrAdminId());

        if (currentAdmin == null || currentAdmin.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new NotAllowedException("Only admin can promote members to admin.");
        }

        ConversationParticipant targetUser = participantRepo
                .findByConversation_ConversationIdUser_UserId(changeRoleDTO.getConversationId(),
                        changeRoleDTO.getTargetUserId());

        if (targetUser == null) {
            throw new BadRequestException("This user is not a member of this group");
        }

        if (changeRoleDTO.isMakeAdmin()) {
            targetUser.setParticipantRole(ParticipantRole.ADMIN);
        } else {
            int adminCount = participantRepo.countNoOfAdminsInConversation(changeRoleDTO.getConversationId(), ParticipantRole.ADMIN);
            if (adminCount <= 1 && targetUser.getParticipantRole() == ParticipantRole.ADMIN) {
                throw new BadRequestException("Last admin cannot be demoted as member.");
            }
            targetUser.setParticipantRole(ParticipantRole.MEMBER);
        }
        participantRepo.save(targetUser);
    }

    public void swapOwnership(int conversationId, int currOwnerId, int newOwnerId) {
        ConversationParticipant currentOwner = participantRepo.findByConversation_ConversationIdUser_UserId(conversationId, currOwnerId);
        if (currentOwner == null) {
            throw new UserNotFoundException("current owner is not a participant in this group.");
        }
        ConversationParticipant newOwner = participantRepo.findByConversation_ConversationIdUser_UserId(conversationId, newOwnerId);
        if (newOwner == null) {
            throw new UserNotFoundException("new owner is not a participant in this group.");
        }

        if (currentOwner.getParticipantRole() == ParticipantRole.ADMIN
                && newOwner.getParticipantRole() == ParticipantRole.MEMBER) {

            newOwner.setParticipantRole(ParticipantRole.ADMIN);
            currentOwner.setParticipantRole(ParticipantRole.MEMBER);

            participantRepo.save(currentOwner);
            participantRepo.save(newOwner);
        } else {
            throw new NotAllowedException("Only an admin can transfer their ownership to a members.");
        }
    }


    public void muteConversation(MuteConvDTO muteConvDTO){
        if(conversationRepo.existsById(muteConvDTO.getConversationId())){
            throw new ConversationNotFoundException("Conversation not found.");
        }
        ConversationParticipant participant = participantRepo
                .findByConversation_ConversationIdUser_UserId(muteConvDTO.getConversationId(), muteConvDTO.getUserId());
        if(participant == null){
            throw new BadRequestException("Participant not found.");
        }
        participant.setMuted(true);
        participantRepo.save(participant);
    }

    public void unMuteConversation(UnMuteConvDTO unMuteConvDTO){
        if(conversationRepo.existsById(unMuteConvDTO.getConversationId())){
            throw new ConversationNotFoundException("Conversation not found.");
        }
        ConversationParticipant participant = participantRepo
                .findByConversation_ConversationIdUser_UserId(unMuteConvDTO.getConversationId(), unMuteConvDTO.getUserId());
        if(participant == null){
            throw new BadRequestException("Participant not found.");
        }
        participant.setMuted(false);
        participantRepo.save(participant);
    }

    public ConversationResponseDTO addOrRemoveAsFavorites(FavoriteConvDTO favoriteConvDTO) {
        if(conversationRepo.existsById(favoriteConvDTO.getConversationId())){
            throw new ConversationNotFoundException("Conversation not found.");
        }
        ConversationParticipant cp = participantRepo
                .findByConversation_ConversationIdUser_UserId(favoriteConvDTO.getConversationId(), favoriteConvDTO.getUserId());
        if(cp == null){
            throw new BadRequestException("Only members can add this group as favorite.");
        }
        cp.setFavorite(!cp.isFavorite());   // flips the boolean value
        participantRepo.save(cp);
        return convertToResponseDTO(cp.getConversation());
    }

    public ConversationResponseDTO addOrRemoveAsArchive(ArchiveConvDTO archiveConvDTO) {
        if(conversationRepo.existsById(archiveConvDTO.getConversationId())){
            throw new ConversationNotFoundException("Conversation not found.");
        }
        ConversationParticipant cp = participantRepo
                .findByConversation_ConversationIdUser_UserId(archiveConvDTO.getConversationId(), archiveConvDTO.getUserId());
        if(cp == null){
            throw new BadRequestException("Only members can add this group as archived.");
        }
        cp.setArchived(!cp.isArchived());   // flips the boolean value
        participantRepo.save(cp);
        return convertToResponseDTO(cp.getConversation());
    }

    public List<ConversationResponseDTO> getFavoriteConversations(int userId) {
        List<ConversationParticipant> favorites = participantRepo.findAllFavoritesByUserId(userId);
        return favorites.stream()
                .map(fav -> convertToResponseDTO(fav.getConversation()))
                .toList();
    }

    public List<ConversationResponseDTO> getArchivedConversations(int userId) {
        List<ConversationParticipant> archived = participantRepo.findAllArchivedByUserId(userId);
        return archived.stream()
                .map(fav -> convertToResponseDTO(fav.getConversation()))
                .toList();
    }

    public void moderateGroupUser(GroupModerationDTO moderationDTO){
        if(!conversationRepo.existsById(moderationDTO.getConversationId())){
            throw new ConversationNotFoundException("No conversation found for id "+moderationDTO.getConversationId());
        }
        switch (moderationDTO.getAction()){
            case "MUTE" -> muteConversation(new MuteConvDTO(moderationDTO.getConversationId(), moderationDTO.getTargetUserId()));
            case "KICK" -> removeParticipant(new RemoveParticipantDTO(moderationDTO.getConversationId(), moderationDTO.getTargetUserId(), moderationDTO.getModeratorId()));
            case "BAN" -> banService.banUser(new BanUserDTO(moderationDTO.getConversationId(), moderationDTO.getTargetUserId(), moderationDTO.getModeratorId(), moderationDTO.getAction()));
            default -> throw new BadRequestException("Action not specified.");
        }
    }

    public void enableDisappearingMessage(int convId, boolean disappearing, int days){
        Conversation conversation = getConversationById(convId);
        if(conversation.isDisappearingMsg()){
            throw new BadRequestException("Already enabled disappearing messages.");
        }
        conversation.setDisappearingMsg(disappearing);
        conversation.setMsgExpiryInDays(days);
        conversationRepo.save(conversation);
    }

    public void disableDisappearing(int convId, boolean disappearing){
        Conversation conversation = getConversationById(convId);
        if(!conversation.isDisappearingMsg()){ // just for check
            throw new BadRequestException("Already disabled disappearing messages.");
        }
        conversation.setDisappearingMsg(disappearing);
        conversationRepo.save(conversation);
    }

    // ------------------------------ ONE TO ONE Conversation services ---------------------------------

    public ConversationResponseDTO createPrivateConversation(int userId1, int userId2){
        // self messaging chat
        if(userId1 == userId2){
            Optional<Conversation> selfMsgConv = conversationRepo.findSelfConversation(userId1);
            if(selfMsgConv.isPresent()){
                return convertToResponseDTO(selfMsgConv.get());
            }
            User user = userService.getUserById(userId1);
            Conversation conversation = new Conversation();
            conversation.setConversationType(Conversation.ConversationType.PRIVATE);
            conversation.setTitle(user.getNickname());
            conversation.setDescription(user.getBio());
            conversation.setConvCreator(user);
            Conversation savedSelfConv = conversationRepo.save(conversation);

            ConversationParticipant self = new ConversationParticipant();
            self.setUser(user);
            self.setConversation(savedSelfConv);
            self.setParticipantRole(ParticipantRole.MEMBER);
            participantRepo.save(self);
            return convertToResponseDTO(savedSelfConv);
        }

        // 1 to 1
        Optional<Conversation> existing = conversationRepo.findPrivateConvBetweenTwoUsers(userId1, userId2);
        if(existing.isPresent()){
            return convertToResponseDTO(existing.get());
        }

        User u1 = userService.getUserById(userId1);
        User u2 = userService.getUserById(userId2);

        if (blockedService.isBlocked(userId1, userId2) || blockedService.isBlocked(userId2, userId1)){
            throw new ForbiddenException("Either one blocked the other one");
        }

        Conversation conversation = new Conversation();
        conversation.setConversationType(Conversation.ConversationType.PRIVATE);
        conversation.setTitle(u2.getNickname());
        conversation.setConvCreator(u1);
        Conversation saved = conversationRepo.save(conversation);

        ConversationParticipant p1 = new ConversationParticipant();
        p1.setConversation(saved);
        p1.setUser(u1);
        p1.setParticipantRole(ParticipantRole.MEMBER);
        ConversationParticipant p2 = new ConversationParticipant();
        p2.setConversation(saved);
        p2.setUser(u2);
        p2.setParticipantRole(ParticipantRole.MEMBER);
        participantRepo.save(p1);
        participantRepo.save(p2);
        return convertToResponseDTO(saved);
    }

    public int getReceiverId(int conversationId, int senderId) {
        Conversation conversation = getConversationById(conversationId);
        Optional<User> receiver = conversation.getParticipants().stream()
                .map(p -> p.getUser())
                .filter(u -> u.getUserId() != senderId)
                .findFirst();
        if (receiver.isEmpty()){
            throw new UserNotFoundException("Receiver not found");
        }
        return receiver.get().getUserId();
    }


}
