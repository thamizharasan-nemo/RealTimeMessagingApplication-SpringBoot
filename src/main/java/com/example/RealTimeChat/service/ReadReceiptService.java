package com.example.RealTimeChat.service;

import com.example.RealTimeChat.DTO.PayloadDTO.ReadReceiptDTO;
import com.example.RealTimeChat.DTO.ReadReceiptResponseDTO;
import com.example.RealTimeChat.exception.BadRequestException;
import com.example.RealTimeChat.exception.GenericNotFoundException;
import com.example.RealTimeChat.model.Message;
import com.example.RealTimeChat.model.ReadReceipt;
import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.repository.MessageRepository;
import com.example.RealTimeChat.repository.ReadReceiptRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReadReceiptService {

    private final ReadReceiptRepository readReceiptRepo;
    private final MessageRepository messageRepo;
    private final UserService userService;

    public ReadReceiptService(ReadReceiptRepository readReceiptRepo, MessageRepository messageRepo, UserService userService) {
        this.readReceiptRepo = readReceiptRepo;
        this.messageRepo = messageRepo;
        this.userService = userService;
    }

    public ReadReceiptResponseDTO convertToResponse(ReadReceipt readReceipt){
        ReadReceiptResponseDTO responseDTO = new ReadReceiptResponseDTO();
        responseDTO.setReadReceiptId(readReceipt.getReadReceiptId());
        responseDTO.setMessageId(readReceipt.getMessage().getMessageId());
        responseDTO.setReaderName(readReceipt.getReader().getUsername());
        responseDTO.setReadAt(readReceipt.getReadAt());
        return responseDTO;
    }

    public ReadReceiptResponseDTO markAsRead(ReadReceiptDTO readDTO){
        Message message = messageRepo.findByMessageIdWithDetails(readDTO.getMessageId())
                .orElseThrow(() -> new GenericNotFoundException("Message not found."));
        User reader = userService.getUserById(readDTO.getReaderId());

        ReadReceipt read = new ReadReceipt();
        read.setMessage(message);
        read.setReader(reader);
        read.setReadAt(Instant.now());
        readReceiptRepo.save(read);

        if(readReceiptRepo.existsByMessageIdAndReaderId(readDTO.getMessageId(), readDTO.getReaderId())){
            throw new BadRequestException("Already read by this user");
        }
        return convertToResponse(read);
    }

    public List<ReadReceiptResponseDTO> allReadersForMessage(int messageId){
        Message message = messageRepo.findByMessageIdWithDetails(messageId)
                .orElseThrow(() -> new GenericNotFoundException("Message not found or deleted."));

        List<ReadReceipt> readReceiptList = readReceiptRepo.findAllReadersForMessage(messageId);
        return readReceiptList.stream()
                .map(this::convertToResponse)
                .toList();
    }
}
