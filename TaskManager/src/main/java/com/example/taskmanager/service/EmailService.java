package com.example.taskmanager.service;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.Events.StudySession;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.domain.User.PasswordResetToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;

@Service
public class EmailService {

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Method that will send all emails throughout the web application
     * **/
    public void sendMail(String to, String subject, String body){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(fromEmail);
        message.setSentDate(new Date());
        mailSender.send(message);
    }

    /**
     * Method used as template for sending a password reset link to the user's email.
     * **/
    public String PasswordRestEmail(HttpServletRequest request, PasswordResetToken token){
        String url = ServletUriComponentsBuilder.fromRequestUri(request).replacePath(null).build().toUriString();
        return "Good day, " + token.getUser().getFirstName() + ".\n\n"
                + "We have received a request to reset the password for your account associated with " + token.getUser().getEmail() +"\n"
                + "If you wish to reset your password you can click the link below:\n\n"
                + url + "/reset-password/check-token/" + token.getToken() +"\n\n"
                + "If you didn't request this simply ignore this email.\n\n"
                + "---- The Task Manager Team ----" ;
    }

    /**
     * Used as email template for when a user creates an account.
     * @param user
     * @param request
     * @return
     */
    public String SignUpEmail(MyUser user,HttpServletRequest request){
        String url = ServletUriComponentsBuilder.fromRequestUri(request).replacePath(null).build().toUriString();
        return "Good day, " + user.getFirstName()+ "\n\nThis is email confirmation that your account has been created!\n\n" +
                "To use the website login in here: "+url+"/login";
    }

    /**
     * Method used as template for sending email reminders to a certain email.
     * @param tasksToStart
     * @param tasksToComplete
     * @param groupTasksToStart
     * @param groupTasksToComplete
     * @param assignmentsToStart
     * @param assignmentsDue
     * @param assignmentTasksToStart
     * @param assignmentTasksToComplete
     * @param studySessions
     * @return
     */
    public String EmailRemindersContent(List<Task> tasksToStart, List<Task> tasksToComplete
    , List<GroupTask> groupTasksToStart, List<GroupTask> groupTasksToComplete
    , List<Assignment> assignmentsToStart, List<Assignment> assignmentsDue
    , List<AssignmentTask> assignmentTasksToStart, List<AssignmentTask> assignmentTasksToComplete
    , List<StudySession> studySessions, MyUser user){
        String content = "";
        if(user.getAccountSettings().isEmailTaskReminders()){
            content += "-------- Tasks To Start --------\n";
            if(!tasksToStart.isEmpty()){
                for (Task task : tasksToStart) {
                    content += task.getTitle()+": "+task.getStartDateString()+"\n\n";
                }
            }else{
                content += "No Tasks to start today!\n\n";
            }
            content += "-------- Tasks To Complete --------\n";
            if(!tasksToComplete.isEmpty()){
                for (Task task : tasksToComplete) {
                    content += task.getTitle()+": "+task.getEndDateString()+"\n\n";
                }
            }else{
                content += "No Tasks to complete today!\n\n";
            }
            content += "-------- Group Tasks To Start --------\n";
            if(!groupTasksToStart.isEmpty()){
                for (GroupTask task : groupTasksToStart) {
                    content += task.getTitle()+": "+task.getStartDateString()+"\n\n";
                }
            }else{
                content += "No Group Tasks to start today!\n\n";
            }
            content += "-------- Group Tasks To Start --------\n";
            if(!groupTasksToComplete.isEmpty()){
                for (GroupTask task : groupTasksToComplete) {
                    content += task.getTitle()+": "+task.getEndDateString()+"\n\n";
                }
            }else{
                content += "No Group Tasks to complete today!\n\n";
            }
        }

        if(user.getAccountSettings().isEmailAssignmentReminders()){
            content += "-------- Assignments To Start --------\n";
            if(!assignmentsToStart.isEmpty()){
                for (Assignment assignment : assignmentsToStart) {
                    content += assignment.getTitle()+": "+assignment.getStartDateString()+"\n\n";
                }
            }else{
                content += "No Assignments to start today!\n\n";
            }
            content += "-------- Assignments Due Today --------\n";
            if(!assignmentsDue.isEmpty()){
                for (Assignment assignment : assignmentsDue) {
                    content += assignment.getTitle()+": "+assignment.getDueDate()+"\n\n";
                }
            }else{
                content += "No Assignments due today!\n\n";
            }
            content += "-------- Assignment Tasks To Start --------\n";
            if(!assignmentTasksToStart.isEmpty()){
                for (AssignmentTask task : assignmentTasksToStart) {
                    content += task.getTitle()+": "+task.getEndDateString()+"\n\n";
                }
            }else{
                content += "No Assignment Tasks to start today!\n\n";
            }
            content += "-------- Assignment Tasks To Complete --------\n";
            if(!assignmentTasksToComplete.isEmpty()){
                for (AssignmentTask task : assignmentTasksToComplete) {
                    content += task.getTitle()+": "+task.getEndDateString()+"\n\n";
                }
            }else{
                content += "No Assignment Tasks to complete today!\n\n";
            }
        }
        if(user.getAccountSettings().isEmailStudySessionReminders()){
            content += "-------- Study Sessions for Today --------\n";
            if(!studySessions.isEmpty()){
                for(StudySession session: studySessions){
                    content += session.getTitle()+": "+session.getFormattedDateTimeEventString();
                }
            }else{
                content += "No Study Sessions for today!\n\n";
            }
        }

        return "Good day!, "+ user.getFirstName()+".\n This a daily reminder email sent for you to achieve total completion! \n\n"+
                content+"\n\n ---- The Task Manager Team ----";
    }

    /**
     * Whenever a user makes a payment this email will be sent to confirm payments gone through and the user is now subscribed
     * @param user
     * @return
     */
    public String subscriptionPaymentConfirmed(MyUser user){
        LocalDateTime expiryDate = LocalDateTime.now().plusMonths(1);
        return "Welcome!, "+user.getFirstName()+"\n This is a confirmation email to let you know that the payment has been processed and you have " +
                "become a member of the Task Manager Application!"+"\n Membership Status Valid Till: "+expiryDate.toString()+"\n\n---- The Task Manager Team ----";
    }

    /**
     * If a users membership has expired this email content is sent.
     * @param user
     * @return
     */
    public String subscriptionEndedReminder(MyUser user){
    return "Good day!, "+user.getFirstName()+"\n This email is sent to let you know that you membership has expired, to continue using"+
            " the premium features make another payment to unlock features again."+"\nNote: Tasks that were archived are now unarchived and will remain in storage for another 30 days, but if you"+
            " resubscribe they will be moved to be archived again.\n\n---- The Task Manager Team ----";
    }

}
