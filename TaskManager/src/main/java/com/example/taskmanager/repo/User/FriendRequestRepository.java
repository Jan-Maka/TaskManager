package com.example.taskmanager.repo.User;

import com.example.taskmanager.domain.User.FriendRequest;
import com.example.taskmanager.domain.User.MyUser;
import org.springframework.data.repository.CrudRepository;

public interface FriendRequestRepository extends CrudRepository<FriendRequest, Long> {
    boolean existsBySenderAndRecipient(MyUser sender,MyUser recipient);
    FriendRequest findBySenderAndRecipient(MyUser sender,MyUser recipient);
}
