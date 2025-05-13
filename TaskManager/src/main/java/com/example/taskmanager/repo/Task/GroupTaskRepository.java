package com.example.taskmanager.repo.Task;

import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.User.MyUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface GroupTaskRepository extends CrudRepository<GroupTask,Long> {


    List<GroupTask> findByTitleContainingIgnoreCaseAndUsers(String title, MyUser user);

    List<GroupTask> findByGroupChatIsNullAndCompleteFalseAndTitleContainingIgnoreCaseAndUsers(String title,MyUser user);
    List<GroupTask> findByUsersAndUrgentIsTrueAndImportantIsTrueAndCompleteFalseAndStartDateBetween(MyUser user, Date date, Date date1);

    List<GroupTask> findByUsersAndInProgressTrue(MyUser user);
    List<GroupTask> findByUsersAndInProgressTrueAndProgressGreaterThanEqual(MyUser user,int num);

    List<GroupTask> findByUsersAndCompleteTrueAndFinishedBetween(MyUser user, Date date1, Date date2);

    List<GroupTask> findByUsersAndEndDateBetween(MyUser user, Date date1, Date date2);

    List<GroupTask> findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(MyUser user, Date date1, Date date2);

    List<GroupTask> findByUsersAndEndDateBetweenAndInProgressTrue(MyUser user, Date date1, Date date2);

    List<GroupTask> findByUsersAndEndDateBetweenAndCompleteFalseAndInProgressFalse(MyUser user, Date date1, Date date2);



}
