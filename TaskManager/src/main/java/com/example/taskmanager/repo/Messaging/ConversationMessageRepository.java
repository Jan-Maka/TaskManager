package com.example.taskmanager.repo.Messaging;

import com.example.taskmanager.domain.Messaging.ConversationMessage;
import org.springframework.data.repository.CrudRepository;

public interface ConversationMessageRepository extends CrudRepository<ConversationMessage, Long> {
}
