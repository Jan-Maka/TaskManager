package com.example.taskmanager.repo.Messaging;

import com.example.taskmanager.domain.Messaging.Conversation;
import com.example.taskmanager.domain.User.MyUser;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ConversationRepository extends CrudRepository<Conversation, Long> {
    List<Conversation> findByParticipantsUsernameContainingIgnoreCaseAndParticipantsContaining(String username,MyUser user);
}
