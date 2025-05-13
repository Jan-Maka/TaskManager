package com.example.taskmanager.repo.Messaging;

import com.example.taskmanager.domain.Messaging.GroupChat;
import com.example.taskmanager.domain.User.MyUser;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface GroupChatRepository extends CrudRepository<GroupChat, Long> {
    List<GroupChat> findByGroupNameContainingIgnoreCaseAndParticipants(String query, MyUser user);
}
