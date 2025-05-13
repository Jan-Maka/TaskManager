package com.example.taskmanager.repo.Task;

import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TaskRepository extends CrudRepository<Task,Long> {

    List<Task> findByTitleContainingIgnoreCaseAndUserAndArchiveFalse(String title, MyUser user);

    List<Task> findByTitleContainingIgnoreCaseAndCategoryAndArchiveFalse(String title, Category category);

    List<Task> findByCompleteTrueAndFinishedNotNullAndFinishedBeforeAndArchiveFalseAndUnarchiveTimeStampNull(Date date);

    List<Task> findByUnarchiveTimeStampBefore(LocalDate dateTime);

    List<Task> findTop6ByUserAndFinishedBeforeOrderByFinishedDesc(MyUser user, Date date);

    List<Task> findByUserAndUrgentIsTrueAndImportantIsTrueAndCompleteFalseAndStartDateBetween(MyUser user, Date date, Date date1);

    List<Task> findByUserAndInProgressTrue(MyUser user);
    List<Task> findByUserAndInProgressTrueAndProgressGreaterThanEqual(MyUser user,int num);

    List<Task> findByUserAndCompleteTrueAndFinishedBetween(MyUser user,Date date1, Date date2);

    List<Task> findByUserAndEndDateBetween(MyUser user, Date date1, Date date2);

    List<Task> findByUserAndStartDateBetweenAndCompleteFalseAndInProgressFalse(MyUser user, Date date1, Date date2);

    List<Task> findByUserAndEndDateBetweenAndInProgressTrue(MyUser user, Date date1, Date date2);

    List<Task> findByUserAndEndDateBetweenAndCompleteFalseAndInProgressFalse(MyUser user, Date date1, Date date2);

    List<Task> findByUserAndArchiveTrue(MyUser user);

    List<Task> findByUserAndTitleContainingIgnoreCaseAndArchiveTrue(MyUser user, String query);

}
