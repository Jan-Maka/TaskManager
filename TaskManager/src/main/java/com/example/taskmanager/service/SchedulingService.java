package com.example.taskmanager.service;

import com.example.taskmanager.component.CommonUtils;
import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.Events.StudySession;
import com.example.taskmanager.domain.File.FileAttachment;
import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.File.FileRepository;
import com.example.taskmanager.repo.Task.TaskRepository;
import com.example.taskmanager.repo.User.PasswordResetTokenRepository;
import com.example.taskmanager.repo.User.RoleRepository;
import com.example.taskmanager.repo.User.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SchedulingService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepo;

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private FileRepository fileRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CommonUtils utils;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private EventsService eventsService;

    @Autowired
    private EmailService emailService;


    /**
     * Delete Tokens that haven't been used on the 1'st of every month at midnight.
     * **/
    @Scheduled(cron = "${purge.cron.expired-tokens}")
    public void PasswordResetTokenExpired(){
        final Calendar cal = Calendar.getInstance();
        Date dateNow = cal.getTime();
        passwordResetTokenRepo.deleteAllByExpiryDateBefore(dateNow);
    }

    /**
     * Delete tasks and their file attachments of database if they have been completed after 30 days
     * unless they are archived.
     * Scheduled to happen every day.
     */
    @Scheduled(cron = "${daily.cron.jobs}")
    public void deleteCompletedTasksOlderThan30days(){
        List<Task> toDelete = taskRepo.findByCompleteTrueAndFinishedNotNullAndFinishedBeforeAndArchiveFalseAndUnarchiveTimeStampNull(utils.get30daysAgo());
        toDelete.addAll(taskRepo.findByUnarchiveTimeStampBefore(LocalDate.now().minusDays(30)));
        toDelete.forEach((task) -> {
            for (FileAttachment file: task.getFileAttachments()) {
                fileRepo.delete(file);
            }
            MyUser user = task.getUser();
            user.getTasks().remove(task);

            Category category = task.getCategory();
            category.getTasks().remove(task);
            taskRepo.delete(task);
        });
    }

    /**
     * If a users membership has expired then this will remove users member role and will unarchive
     * any tasks that are archived
     */
    @Scheduled(cron = "${daily.cron.jobs}")
    public void removeUserMemberRoleIfStartOver30days(){
        LocalDateTime expiryDate = LocalDateTime.now().minusMonths(1);
        List<MyUser> users = userRepo.findByRolesNameAndSubscriptionStartBefore("MEMBER",expiryDate);
        users.forEach(user -> {
            String emailContent = emailService.subscriptionEndedReminder(user);
            emailService.sendMail(user.getEmail(),"Subscription Expired",emailContent);
            user.getRoles().remove(roleRepo.findByName("MEMBER"));
            userRepo.save(user);
            user.getTasks().forEach((task -> {
                if(task.isArchive()){
                    task.setArchive(false);
                    task.setUnarchiveTimeStamp(LocalDate.now());
                    taskRepo.save(task);
                }
            }));
        });
    }

    /**
     * Will send email reminders for Tasks/Assignments/Study-Sessions
     * Scheduled to run every day at 8 am
     */
    @Scheduled(cron = "${daily.cron.email}")
    public void emailReminders(){
        List<MyUser> usersToEmail = userRepo.findByAccountSettings_EmailTaskRemindersTrue();
        usersToEmail.addAll(userRepo.findByAccountSettings_EmailAssignmentRemindersTrue());
        usersToEmail.addAll(userRepo.findByAccountSettings_EmailStudySessionRemindersTrue());
        usersToEmail = usersToEmail.stream().distinct().collect(Collectors.toList());
        usersToEmail.forEach((user) -> {
            List<Task> emailTasksToStart = new ArrayList<>();
            List<Task> emailTasksToComplete = new ArrayList<>();

            List<GroupTask> emailGroupTasksToStart = new ArrayList<>();
            List<GroupTask> emailGroupTasksToComplete = new ArrayList<>();

            List<Assignment> emailAssignmentsToStart = new ArrayList<>();
            List<Assignment> emailAssignmentsToComplete = new ArrayList<>();

            List<AssignmentTask> emailAssignmentTasksToStart = new ArrayList<>();
            List<AssignmentTask> emailAssignmentTasksToComplete = new ArrayList<>();

            List<StudySession> emailStudySessions = new ArrayList<>();

            if(user.getAccountSettings().isEmailTaskReminders()){
                emailTasksToStart.addAll(taskService.getTasksNotStartedToStartToday(user));
                emailTasksToComplete.addAll(taskService.getTasksToCompleteToday(user));

                emailGroupTasksToStart.addAll(taskService.getGroupTasksNotStartedToStartToday(user));
                emailGroupTasksToComplete.addAll(taskService.getGroupTasksToCompleteToday(user));
            }
            if(user.getAccountSettings().isEmailAssignmentReminders()){
                emailAssignmentsToStart.addAll(assignmentService.getAssignmentsToStartToday(user));
                emailAssignmentsToComplete.addAll(assignmentService.getAssignmentsDueToday(user));

                emailAssignmentTasksToStart.addAll(assignmentService.getAssignmentTasksToBeStartedToday(user));
                emailAssignmentTasksToComplete.addAll(assignmentService.getAssignmentTasksToCompleteToday(user));
            }
            if(user.getAccountSettings().isEmailStudySessionReminders()){
                emailStudySessions.addAll(eventsService.getUserStudySessionsForCurrentDate(user.getStudySessions()));
            }
            String emailContent = emailService.EmailRemindersContent(emailTasksToStart,emailTasksToComplete,emailGroupTasksToStart,emailGroupTasksToComplete
            ,emailAssignmentsToStart,emailAssignmentsToComplete,emailAssignmentTasksToStart,emailAssignmentTasksToComplete, emailStudySessions,user);
            emailService.sendMail(user.getEmail(),"Task Manager reminders!:",emailContent);
        });
    }
}
