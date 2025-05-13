package com.example.taskmanager.repo.Assignment;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.User.MyUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface AssignmentRepository extends CrudRepository<Assignment,Long> {

    List<Assignment> findByTitleContainingIgnoreCaseAndUsers(String title, MyUser user);
    List<Assignment> findByInProgressTrueAndUsers(MyUser user);
    List<Assignment> findByEndDateBetweenAndUsersAndCompleteIsFalse(Date date1, Date date2, MyUser user);
    List<Assignment> findByGroupChatIsNullAndCompleteFalseAndTitleContainingIgnoreCaseAndUsers(String title, MyUser user);

    List<Assignment> findByUsersAndStartDateBetweenAndInProgressFalseAndCompleteFalse(MyUser user, Date date1, Date date2);
}
