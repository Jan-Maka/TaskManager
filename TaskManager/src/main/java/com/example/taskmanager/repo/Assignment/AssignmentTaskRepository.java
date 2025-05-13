package com.example.taskmanager.repo.Assignment;

import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.User.MyUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface AssignmentTaskRepository extends CrudRepository<AssignmentTask,Long> {
    List<AssignmentTask> findTop6ByUsersAndFinishedBeforeOrderByFinishedDesc(MyUser user, Date date);

    List<AssignmentTask> findByUsersAndUrgentIsTrueAndImportantIsTrueAndCompleteFalseAndStartDateBetween(MyUser user, Date date, Date date1);
    List<AssignmentTask> findByUsersAndInProgressTrue(MyUser user);
    List<AssignmentTask> findByUsersAndInProgressTrueAndProgressGreaterThanEqual(MyUser user,int num);
    List<AssignmentTask> findByUsersAndCompleteTrueAndFinishedBetween(MyUser user, Date date1, Date date2);

    List<AssignmentTask> findByUsersAndEndDateBetween(MyUser user, Date date1, Date date2);

    List<AssignmentTask> findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(MyUser user, Date date1, Date date2);

    List<AssignmentTask> findByUsersAndEndDateBetweenAndInProgressTrue(MyUser user, Date date1, Date date2);

    List<AssignmentTask> findByUsersAndEndDateBetweenAndCompleteFalseAndInProgressFalse(MyUser user, Date date1, Date date2);



}
