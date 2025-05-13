package com.example.taskmanager.repo.Messaging;

import com.example.taskmanager.domain.Messaging.GroupMessage;
import org.springframework.data.repository.CrudRepository;

public interface GroupMessageRepository extends CrudRepository<GroupMessage, Long> {
}
