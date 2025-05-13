package com.example.taskmanager.ServiceTests;
import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.DTO.AssignmentDTO;
import com.example.taskmanager.domain.Assignment.DTO.AssignmentTaskDTO;
import com.example.taskmanager.domain.Events.DTO.StudySessionDTO;
import com.example.taskmanager.domain.Task.DTO.GroupTaskDTO;
import com.example.taskmanager.domain.Task.DTO.TaskDTO;
import com.example.taskmanager.domain.User.AccountSettings;
import com.example.taskmanager.domain.User.DTO.NotificationDTO;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    MyUser user;
    AccountSettings accountSettings;
    MockPrincipal mockPrincipal;

    @InjectMocks
    NotificationService notificationService;

    @Mock
    UserService userService;

    @Mock
    TaskService taskService;

    @Mock
    AssignmentService assignmentService;

    @Mock
    EventsService eventsService;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        this.user = new MyUser("foo@bar.com","foo", "bar","password", "foofoo");
        user.setId(1L);
        this.accountSettings = new AccountSettings();
        accountSettings.setSendTaskNotifications(true);
        accountSettings.setSendAssignmentNotifications(true);
        accountSettings.setSendStudySessionNotifications(true);
        user.setAccountSettings(accountSettings);
        this.mockPrincipal = new MockPrincipal(user.getEmail());
    }

    @DisplayName("Test creation of notification DTO")
    @Test
    public void testCreatingNotificationDTO(){
        List<TaskDTO> taskDTOs = new ArrayList<>();
        List<GroupTaskDTO> groupTaskDTOS = new ArrayList<>();
        List<AssignmentDTO> assignmentDTOS = new ArrayList<>();
        List<AssignmentTaskDTO> assignmentTaskDTOS = new ArrayList<>();
        List<StudySessionDTO> studySessionDTOS = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            taskDTOs.add(new TaskDTO((long) i,user.getId(),"test",true,true,(short) 0,(short) 0,"testing!", 3L, "cat", new Date(),new Date(),50,new Date(),new Date(),false,true,false));
            groupTaskDTOS.add(new GroupTaskDTO((long) i,user.getId(),"test",true,true,(short) 0,(short) 0,"testing!", new Date(),new Date(),50,new Date(),new Date(),false,true,user.getUsername()));
            assignmentDTOS.add(new AssignmentDTO((long) i,user.getId(),user.getUsername(),"test2","course2","desc",new Date(),new Date(),new Date(),true,false,69,true,true));
            assignmentTaskDTOS.add(new AssignmentTaskDTO((long) i,user.getId(),"task",true,true,(short)3,(short)4,"test",new Date(), new Date(),49,new Date(),new Date(),false,true,1L,"test"));
            studySessionDTOS.add(new StudySessionDTO((long) i,"test","testing",new Date(),new Date(),new Date(),false,"location",user.getId(),user.getUsername()));
        }
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(taskService.getTasksToStartIn30Minutes(user)).thenReturn(taskDTOs);
        when(taskService.getGroupTasksToStartIn30Minutes(user)).thenReturn(groupTaskDTOS);
        when(assignmentService.getAssignmentToStartWithin30Minutes(user)).thenReturn(assignmentDTOS);
        when(assignmentService.getAssignmentTasksToStartWithin30Minutes(user)).thenReturn(assignmentTaskDTOS);
        when(eventsService.getStudySessionsThatBeginIn30Minutes(user)).thenReturn(studySessionDTOS);
        NotificationDTO result = notificationService.getUserNotifications(mockPrincipal);
        assertTrue(result.getTasks().containsAll(taskDTOs));
        assertTrue(result.getGroupTasks().containsAll(groupTaskDTOS));
        assertTrue(result.getAssignments().containsAll(assignmentDTOS));
        assertTrue(result.getAssignmentTasks().containsAll(assignmentTaskDTOS));
        assertTrue(result.getStudySessions().containsAll(studySessionDTOS));
    }

    private static class MockPrincipal implements java.security.Principal {
        private String name;

        public MockPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }


}
