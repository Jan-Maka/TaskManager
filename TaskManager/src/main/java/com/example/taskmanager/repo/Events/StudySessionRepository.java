package com.example.taskmanager.repo.Events;

import com.example.taskmanager.domain.Events.StudySession;
import org.springframework.data.repository.CrudRepository;

public interface StudySessionRepository extends CrudRepository<StudySession, Long> {
}
