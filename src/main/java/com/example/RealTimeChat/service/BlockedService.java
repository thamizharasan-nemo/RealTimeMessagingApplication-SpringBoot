package com.example.RealTimeChat.service;

import com.example.RealTimeChat.DTO.PayloadDTO.BlockingDTO;
import com.example.RealTimeChat.exception.BadRequestException;
import com.example.RealTimeChat.exception.GenericNotFoundException;
import com.example.RealTimeChat.exception.NotAllowedException;
import com.example.RealTimeChat.exception.UserNotFoundException;
import com.example.RealTimeChat.model.Block;
import com.example.RealTimeChat.model.Conversation;
import com.example.RealTimeChat.model.User;
import com.example.RealTimeChat.repository.BlockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockedService {

    private final BlockRepository blockRepo;
    private final UserService userService;

    public BlockedService(BlockRepository blockRepo, UserService userService) {
        this.blockRepo = blockRepo;
        this.userService = userService;
    }

    public void blockUser(BlockingDTO blockDto){
        if(blockDto.getBlockerId() == blockDto.getBlockedId()){
            throw new NotAllowedException("You can't block yourself.");
        }

        User blocker = userService.getUserById(blockDto.getBlockerId());
        User blocked = userService.getUserById(blockDto.getBlockedId());

        if (blockRepo.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new BadRequestException("User " + blocker.getNickname() + " already blocked " + blocked.getNickname() + ".");
        }
        Block block = new Block();
        block.setBlocker(blocker);
        block.setBlocked(blocked);
        blockRepo.save(block);
    }

    public void unBlockUser(BlockingDTO blockDto) {
        if (blockDto.getBlockerId() == blockDto.getBlockedId()) {
            throw new NotAllowedException("You can't block yourself.");

        }
        User blocker = userService.getUserById(blockDto.getBlockerId());
        User blocked = userService.getUserById(blockDto.getBlockedId());
        if(!blockRepo.existsByBlockerAndBlocked(blocker, blocked)){
            throw new BadRequestException("User " + blocker.getNickname() + " has not blocked " + blocked.getNickname() + ".");
        }
        blockRepo.deleteByBlockerAndBlocked(blockDto.getBlockerId(), blockDto.getBlockedId());
    }

    public boolean isBlocked(int blockerId, int blockedId){
        User blocker = userService.getUserById(blockerId);
        User blocked = userService.getUserById(blockedId);
        return blockRepo.existsByBlockerAndBlocked(blocker, blocked);
    }

    public List<Block> findAllBlockedUsers(int blockerId){
        if(!userService.isExists(blockerId)){
            throw new UserNotFoundException("User not found");
        }
        if(!blockRepo.existsById(blockerId)){
            throw new GenericNotFoundException("No blocked contacts.");
        }
        return blockRepo.findAllBlockedByBlockerId(blockerId);
    }
}
