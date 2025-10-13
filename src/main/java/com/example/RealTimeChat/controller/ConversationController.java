package com.example.RealTimeChat.controller;


import com.example.RealTimeChat.DTO.ConversationDTO;
import com.example.RealTimeChat.DTO.ConversationResponseDTO;
import com.example.RealTimeChat.DTO.PayloadDTO.*;
import com.example.RealTimeChat.model.Conversation;
import com.example.RealTimeChat.model.ParticipantRole;
import com.example.RealTimeChat.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    ConversationService conversationService;

    @GetMapping("/admin/all")
    public ResponseEntity<List<Conversation>> getAllConversation() {
        return ResponseEntity.ok(conversationService.getAllConversation());
    }

    @GetMapping("/all")
    public ResponseEntity<List<ConversationResponseDTO>> getAllConversationAsDTO() {
        return ResponseEntity.ok(conversationService.getAllConversationAsDTO());
    }

    @GetMapping("/admin/id/{convId}")
    public ResponseEntity<Conversation> getConversationById(@PathVariable int convId) {
        return ResponseEntity.ok(conversationService.getConversationById(convId));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConversationResponseDTO> createConversation(@RequestPart("conversation") ConversationDTO conversationDTO,
                                                                    @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        return ResponseEntity.ok(conversationService.createConversation(conversationDTO, imageFile));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ConversationResponseDTO>> getConversationByName(@RequestParam String title) {
        return ResponseEntity.ok(conversationService.getConversationByTitle(title));
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<Integer>> getAllMessageIdsInConv(@PathVariable int conversationId) {
        return ResponseEntity.ok(conversationService.getAllMessageIdsInThisConv(conversationId));
    }

    @DeleteMapping("/delete/conv/{conversationId}")
    public ResponseEntity<?> deleteConvEntirely(@PathVariable int conversationId) {
        conversationService.deleteConvEntirely(conversationId);
        return ResponseEntity.ok().body("Chat group deleted successfully.");
    }

    @DeleteMapping("/messages/delete")
    public ResponseEntity<?> deleteMessagesInConv(@RequestParam int conversationId) {
        conversationService.deleteAllMessagesInThisConv(conversationId);
        return ResponseEntity.ok().body("Messages in conversation with id " + conversationId + " deleted successfully.");
    }

    @PutMapping("/add/participant")
    public ResponseEntity<ConversationResponseDTO> addParticipants(@RequestBody AddParticipantDTO addParticipantDTO) {
        return ResponseEntity.ok(conversationService.addParticipant(addParticipantDTO));
    }

    // dev check to add member to group
    @PutMapping("/no-admin/add/{conversationId}/user/{userId}")
    public ResponseEntity<ConversationResponseDTO> addParticipantsWithoutAdmin(@PathVariable int conversationId,
                                                                               @PathVariable int userId,
                                                                               @RequestParam(defaultValue = "MEMBER") ParticipantRole role) {
        return ResponseEntity.ok(conversationService.addParticipantWithoutAdmin(conversationId, userId, role));
    }

    @DeleteMapping("/no-admin/remove/{conversationId}/user/{userId}")
    public ResponseEntity<ConversationResponseDTO> addParticipantsWithoutAdmin(@PathVariable int conversationId,
                                                                               @PathVariable int userId) {
        return ResponseEntity.ok(conversationService.removeParticipantWithoutAdmin(conversationId, userId));
    }

    @DeleteMapping("/remove/participant")
    public ResponseEntity<ConversationResponseDTO> removeParticipants(@RequestBody RemoveParticipantDTO removeParticipantDTO) {
        return ResponseEntity.ok(conversationService.removeParticipant(removeParticipantDTO));
    }

    @DeleteMapping("/leave/conversation")
    public ResponseEntity<?> leaveFromConversation(@RequestBody LeaveConversationDTO leaveConversationDTO) {
        return ResponseEntity.ok(conversationService.leaveConversation(leaveConversationDTO));
    }

    @PutMapping("/title/rename")
    public ResponseEntity<ConversationResponseDTO> renameConversationTitle(@RequestBody RenameConversationTitleDTO renameConvTitleDTO) {
        return ResponseEntity.ok(conversationService.renameConversationTitle(renameConvTitleDTO));
    }

    @PutMapping("/update/description")
    public ResponseEntity<ConversationResponseDTO> updateConversationDescription(@RequestBody UpdateConversationDescriptionDTO updateDescriptionDTO) {
        return ResponseEntity.ok(conversationService.updateConversationDescription(updateDescriptionDTO));
    }

    @PostMapping("/promote/to/admin")
    public ResponseEntity<?> promoteToAdmin(@RequestBody ChangeParticipantRoleDTO changeRoleDTO) {
        conversationService.changeParticipantRole(changeRoleDTO);
        return ResponseEntity.ok().body("Member becomes an admin.");
    }

    @PostMapping("/demote/from/admin")
    public ResponseEntity<?> demoteFromAdmin(@RequestBody ChangeParticipantRoleDTO changeRoleDTO) {
        conversationService.changeParticipantRole(changeRoleDTO);
        return ResponseEntity.ok().body("Admin becomes member.");
    }

    @PostMapping("/mute")
    public ResponseEntity<?> muteParticipant(@RequestBody MuteConvDTO muteConvDTO){
        conversationService.muteConversation(muteConvDTO);
        return ResponseEntity.ok().body("Conversation muted");
    }

    @PostMapping("/un-mute")
    public ResponseEntity<?> UnMuteParticipant(@RequestBody UnMuteConvDTO unMuteConvDTO){
        conversationService.unMuteConversation(unMuteConvDTO);
        return ResponseEntity.ok().body("Conversation unMuted");
    }

    @PostMapping("/toggle/favorite")
    public ResponseEntity<?> toggleFavorite(@RequestBody FavoriteConvDTO favoriteConvDTO){
        return ResponseEntity.ok(conversationService.addOrRemoveAsFavorites(favoriteConvDTO));
    }

    @PostMapping("/toggle/archived")
    public ResponseEntity<?> toggleArchived(@RequestBody ArchiveConvDTO archiveConvDTO){
        return ResponseEntity.ok(conversationService.addOrRemoveAsArchive(archiveConvDTO));
    }

    @GetMapping("/all/favorites/{userId}")
    public ResponseEntity<List<ConversationResponseDTO>> getFavorites(@PathVariable int userId) {
        return ResponseEntity.ok(conversationService.getFavoriteConversations(userId));
    }

    @GetMapping("/all/archived/{userId}")
    public ResponseEntity<List<ConversationResponseDTO>> getArchived(@PathVariable int userId) {
        return ResponseEntity.ok(conversationService.getArchivedConversations(userId));
    }

    @PostMapping("/{conversationId}/profile/{userId}/upload-avatar")
    public ResponseEntity<String> uploadConvAvatarUrl(@RequestBody UploadAvatarUrlDTO avatarUrlDTO) {
        return ResponseEntity.ok(conversationService.uploadAvatarUrl(avatarUrlDTO));
    }

    @PostMapping("/{convId}/disappearing")
    public ResponseEntity<?> enableDisappearingMessage(@PathVariable int convId,
                                                       @RequestParam(value = "disappear", defaultValue = "true") boolean disappear,
                                                       @RequestParam(name = "days") int days) {
        conversationService.enableDisappearingMessage(convId, disappear, days);
        return ResponseEntity.ok().body("Message disappearing enabled");
    }

    @PostMapping("/{convId}/disable/disappearing")
    public ResponseEntity<?> disableDisappearingMessage(@PathVariable int convId,
                                                       @RequestParam(value = "disappear", defaultValue = "false") boolean disappear) {
        conversationService.disableDisappearing(convId, disappear);
        return ResponseEntity.ok().body("Message disappearing disabled");
    }

}
