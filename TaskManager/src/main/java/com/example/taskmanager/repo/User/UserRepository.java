package com.example.taskmanager.repo.User;

import com.example.taskmanager.domain.User.MyUser;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends CrudRepository<MyUser,Long> {
    MyUser findByEmail(String email);

    MyUser findByUsername(String username);

    List<MyUser> findAllByUsernameContainingIgnoreCase(String search);

    List<MyUser> findAllByFirstNameContainingIgnoreCase(String search);

    List<MyUser> findAllBySurnameContainingIgnoreCase(String search);

    List<MyUser> findByRolesNameAndSubscriptionStartBefore(String role, LocalDateTime time);

    List<MyUser> findByAccountSettings_EmailTaskRemindersTrue();
    List<MyUser> findByAccountSettings_EmailAssignmentRemindersTrue();
    List<MyUser> findByAccountSettings_EmailStudySessionRemindersTrue();

    boolean existsByEmail(String email);
}