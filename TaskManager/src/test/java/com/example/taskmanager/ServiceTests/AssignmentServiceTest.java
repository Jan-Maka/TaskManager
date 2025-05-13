package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.component.CommonUtils;
import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.Assignment.DTO.AssignmentDTO;
import com.example.taskmanager.domain.Assignment.DTO.AssignmentTaskDTO;
import com.example.taskmanager.domain.User.DTO.UserDTO;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.domain.User.Role;
import com.example.taskmanager.repo.Assignment.AssignmentRepository;
import com.example.taskmanager.repo.Assignment.AssignmentTaskRepository;
import com.example.taskmanager.repo.Messaging.GroupChatRepository;
import com.example.taskmanager.repo.User.*;
import com.example.taskmanager.service.AssignmentService;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;


import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceTest {

    MyUser user;
    MyUser user2;

    Role role1;
    Role role2;
    List<Role> roles = new ArrayList<>();
    Assignment assignment;
    AssignmentTask assignmentTask;
    List<UserDTO> users = new ArrayList<>();
    AssignmentDTO assignmentDTO;

    AssignmentTaskDTO assignmentTaskDTO;

    Principal mockPrincipal;

    @InjectMocks
    AssignmentService assignmentService;
    @Mock
    UserService userService;
    @Mock
    CommonUtils utils;

    @Mock
    AssignmentRepository assignmentRepository;

    @Mock
    AssignmentTaskRepository assignmentTaskRepository;
    @Mock
    UserRepository userRepository;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
        this.role1 = new Role("USER");
        this.role2 = new Role("MEMBER");
        roles.add(role1);
        roles.add(role2);
        this.user = new MyUser("foo@bar.com","foo", "bar","password", "foofoo");
        user.setId(1L);
        user.getRoles().addAll(roles);
        this.user2 = new MyUser("bar@foo.com","bar","foo","password","foobar");
        user2.getRoles().addAll(roles);
        this.assignment = new Assignment(user,"assignment","course","desc",new Date(),new Date(),false,false,0,true,true);
        assignment.setId(1L);
        user.getAssignments().add(assignment);
        assignment.getUsers().add(user);
        this.assignmentTask = new AssignmentTask(user,"task",true,true,(short)4,(short)4,"desc",new Date(),new Date(),0,new Date(),false,false,assignment);
        assignmentTask.setId(1L);
        assignmentTask.getUsers().add(user);
        assignment.getAssignmentTasks().add(assignmentTask);
        user.getAssignmentTasks().add(assignmentTask);
        this.mockPrincipal = new AssignmentServiceTest.MockPrincipal(user.getEmail());
        this.users.add(new UserDTO(1L,user.getUsername(),user.getBio(), user.getLocation(), user.getBase64ProfilePic()));
        this.assignmentDTO = new AssignmentDTO(1L,1L,user.getUsername(),"test2","course2","desc",new Date(),new Date(),new Date(),true,false,69,true,true);
        assignmentDTO.setUsers(users);
        this.assignmentTaskDTO = new AssignmentTaskDTO(1L,1L,"task1",true,true,(short)3,(short)4,"description",new Date(), new Date(),49,new Date(),new Date(),false,true,1L,assignment.getTitle());
        assignmentTaskDTO.setUsers(users);
    }

    @DisplayName("Test assignment exists via Id")
    @Test
    public void testAssignmentExistsById(){
        when(assignmentRepository.existsById(1L)).thenReturn(true);
        assertTrue(assignmentService.assignmentExistsById(1L));
        verify(assignmentRepository,times(1)).existsById(1L);
    }

    @DisplayName("Test assignment task DTO creation is valid")
    @Test
    public void testAssignmentTaskDTOCreation(){
        when(userService.getUsersDTO(anyList())).thenReturn(users);
        AssignmentTaskDTO test = assignmentService.createAssignmentTaskDTO(assignmentTask);
        assertEquals(assignmentTask.getId(), test.getId());
        assertEquals(assignmentTask.getAssignment().getId(),test.getAssignment());
        assertEquals(assignmentTask.getTitle(),test.getTitle());
        assertEquals(assignmentTask.getOwner().getId(),test.getOwner());
        assertEquals(assignmentTask.getDescription(),test.getDescription());
    }

    @DisplayName("Test assignment DTO creation is valid template")
    @Test
    public void testAssignmentDTOCreation(){
        when(userService.getUsersDTO(anyList())).thenReturn(users);
        AssignmentDTO test = assignmentService.createAssignmentDTO(assignment);
        assertEquals(assignment.getOwner().getId(),test.getUser());
        assertEquals(assignment.getTitle(),test.getTitle());
        assertEquals(assignment.getCourse(),test.getCourse());
        assertEquals(assignment.getDescription(),test.getDescription());
        assertEquals(assignment.isImportant(),test.isImportant());
        assertEquals(assignment.isUrgent(),test.isUrgent());
    }

    @DisplayName("Test creating assignment")
    @Test
    public void testCreateAssignment() throws IOException {
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(userRepository.save(user)).thenReturn(user);
        assignmentService.createAssignment(assignmentDTO,new ArrayList<>());
        verify(userRepository,times(3)).save(user);
        Assignment created = user.getAssignments().get(1);
        assertEquals(assignmentDTO.getUser(),created.getOwner().getId());
        assertEquals(assignmentDTO.getTitle(),created.getTitle());
        assertEquals(assignmentDTO.getCourse(),created.getCourse());
        assertEquals(assignmentDTO.getDescription(),created.getDescription());
        assertEquals(assignmentDTO.isImportant(),created.isImportant());
        assertEquals(assignmentDTO.isUrgent(),created.isUrgent());
        assertTrue(created.getUsers().contains(user));
    }

    @DisplayName("Test creating an assignment task")
    @Test
    public void testCreatingAssignmentTask() throws IOException {
        assignment.setId(1L);
        user.setId(1L);
        assignmentTask.setId(1L);
        List<MyUser> myUsers = new ArrayList<>();
        myUsers.add(user);
        List<UserDTO> users = new ArrayList<>();
        users.add(new UserDTO(1L,user.getUsername(),user.getBio(), user.getLocation(), user.getBase64ProfilePic()));
        AssignmentTaskDTO taskDTO = new AssignmentTaskDTO(1L,"task",true,true,(short)5,(short) 4,"desc",new Date(),new Date(),0,false,false,users,assignment.getId());
        AssignmentTask test = new AssignmentTask(user,"task",true,true,(short)5,(short) 4,"desc",new Date(),new Date(),0,new Date(),false,false,assignment);
        test.getUsers().add(user);
        test.setId(2L);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.ofNullable(assignment));
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(assignmentTaskRepository.save(any(AssignmentTask.class))).thenReturn(test);
        when(userService.getUsersDTO(myUsers)).thenReturn(users);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        assignmentService.createAssignmentTask(taskDTO,new ArrayList<>());
        verify(userRepository,times(1)).save(user);
        verify(assignmentRepository,times(1)).save(assignment);
        assertTrue(user.getAssignmentTasks().contains(test));
        assertTrue(assignment.getAssignmentTasks().contains(test));
    }

    @DisplayName("Test getting assignment via Id")
    @Test
    public void testGetAssignmentById(){
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        assertNotNull(assignmentService.getAssignmentById(1L));
    }

    @DisplayName("Test get users assignments")
    @Test
    public void testGetUserAssignments(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        List<Assignment> assignments = assignmentService.getUserAssignments(mockPrincipal);
        assertTrue(assignments.contains(assignment));
    }

    @DisplayName("Test getting assignments from database query based on user search ")
    @Test
    public void testGetAssignmentsFromSearch(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        assignmentService.getAssignmentsFromSearch(anyString(),mockPrincipal);
        verify(assignmentRepository,times(1)).findByTitleContainingIgnoreCaseAndUsers(anyString(),eq(user));
    }

    @DisplayName("Test delete assignment via Id")
    @Test
    public void testDeletingAssignmentById(){
        when(assignmentRepository.findById(assignment.getId())).thenReturn(Optional.ofNullable(assignment));
        assignmentService.deleteAssignmentById(assignment.getId());
        verify(assignmentRepository,times(1)).findById(assignment.getId());
        verify(assignmentRepository,times(1)).delete(assignment);
    }

    @DisplayName("Test user is on assignment")
    @Test
    public void testUserIsOnAssignment(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.ofNullable(assignment));
        assertTrue(assignmentService.isOnAssignment(1L,mockPrincipal));
    }

    @DisplayName("Test user is the owner of an assignment")
    @Test
    public void testUserIsOwnerOfAssignment(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.ofNullable(assignment));
        assertTrue(assignmentService.isOwnerOfAssignment(1L,mockPrincipal));
    }

    @DisplayName("Test updating assignment")
    @Test
    public void testUpdatingOfAssignment() throws IOException {
        assignmentDTO.getUsers().add(new UserDTO(2L,user2.getUsername(),user2.getBio(),user2.getLocation(),user2.getBase64ProfilePic()));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.ofNullable(assignment));
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(userRepository.findById(2L)).thenReturn(Optional.ofNullable(user2));
        when(userRepository.save(user2)).thenReturn(user2);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        assignmentService.updateAssignment(assignmentDTO,new ArrayList<>());
        verify(userRepository,times(1)).save(user2);
        verify(assignmentRepository,times(1)).save(assignment);
        //Test if user has been added
        assertTrue(assignment.getUsers().contains(user2));
        assertTrue(user.getAssignments().contains(assignment));
        //Test fields
        assertEquals(assignmentDTO.getTitle(),assignment.getTitle());
        assertEquals(assignmentDTO.getCourse(),assignment.getCourse());
        assertEquals(assignmentDTO.getDescription(),assignment.getDescription());
        assertEquals(assignmentDTO.getProgress(),assignment.getProgress());
        assertEquals(assignmentDTO.isImportant(),assignment.isImportant());
        assertEquals(assignmentDTO.isUrgent(),assignment.isUrgent());
    }

    @DisplayName("Test assignment task exists by Id")
    @Test
    public void testAssignmentTaskExistsById(){
        when(assignmentTaskRepository.existsById(assignmentTask.getId())).thenReturn(true);
        assertTrue(assignmentService.assignmentTaskExistsById(1L));
    }

    @DisplayName("Test that a user is on an assignment task")
    @Test
    public void testUserIsOnAssignmentTask(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(assignmentTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(assignmentTask));
        assertTrue(assignmentService.isOnAssignmentTask(1L,mockPrincipal));
    }

    @DisplayName("Test that a user is the owner of an assignment task")
    @Test
    public void testUserIsOwnerOfAssignmentTask(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(assignmentTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(assignmentTask));
        assertTrue(assignmentService.isOwnerOfAssignmentTask(1L,mockPrincipal));
    }

    @DisplayName("Test get assignment task via Id")
    @Test
    public void testGetAssignmentTaskById(){
        when(assignmentTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(assignmentTask));
        assertNotNull(assignmentService.getAssignmentTaskById(1L));
    }

    @DisplayName("Test deleting assignment task by Id")
    @Test
    public void testDeletingAssignmentTaskById(){
        when(assignmentTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(assignmentTask));
        assignmentService.deleteAssignmentTask(1L);
        verify(assignmentTaskRepository,times(1)).delete(assignmentTask);
    }

    @DisplayName("Test updating assignment task")
    @Test
    public void testUpdatingAssignmentTask() throws IOException {
        assignment.getUsers().add(user2);
        assignmentTaskDTO.getUsers().add(new UserDTO(2L,user2.getUsername(),user2.getBio(),user2.getLocation(), user2.getBase64ProfilePic()));
        when(assignmentTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(assignmentTask));
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(userRepository.findById(2L)).thenReturn(Optional.ofNullable(user2));
        when(userRepository.save(user2)).thenReturn(user2);
        when(assignmentTaskRepository.save(assignmentTask)).thenReturn(assignmentTask);
        assignmentService.updateAssignmentTask(assignmentTaskDTO,new ArrayList<>());
        verify(assignmentTaskRepository,times(1)).save(assignmentTask);
        //Test user has been added to assignment task
        assertTrue(assignmentTask.getUsers().contains(user2));
        //Test fields
        assertEquals(assignmentTaskDTO.getTitle(),assignmentTask.getTitle());
        assertEquals(assignmentTaskDTO.getWorkload(),assignmentTask.getWorkload());
        assertEquals(assignmentTaskDTO.getMood(),assignmentTask.getMood());
        assertEquals(assignmentTaskDTO.getDescription(),assignmentTask.getDescription());
        assertEquals(assignmentTaskDTO.isUrgent(),assignmentTask.isUrgent());
        assertEquals(assignmentTaskDTO.isImportant(),assignmentTask.isImportant());
        assertEquals(assignmentTaskDTO.getProgress(),assignmentTask.getProgress());
    }

    @DisplayName("Test removing user from assignment")
    @Test
    public void testRemovingUserFromAssignment(){
        assignment.getUsers().add(user2);
        user2.getAssignments().add(assignment);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.ofNullable(assignment));
        when(userRepository.save(user2)).thenReturn(user2);
        assignmentService.removeUserFromAssignment(1L,new MockPrincipal(user2.getEmail()));
        verify(userRepository,times(1)).save(user2);
        assertTrue(!assignment.getUsers().contains(user2));
        assertTrue(!user2.getAssignments().contains(assignment));
    }

    @DisplayName("Test removing user from assignment task")
    @Test
    public void testRemovingUserFromAssignmentTask(){
        assignment.getUsers().add(user2);
        user2.getAssignments().add(assignment);
        assignmentTask.getUsers().add(user2);
        user2.getAssignmentTasks().add(assignmentTask);
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);
        when(assignmentTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(assignmentTask));
        when(userRepository.save(user2)).thenReturn(user2);
        assignmentService.removeUserFromAssignmentTask(1L,new MockPrincipal(user2.getEmail()));
        verify(userRepository,times(1)).save(user2);
        assertTrue(!assignmentTask.getUsers().contains(user2));
        assertTrue(!user2.getAssignmentTasks().contains(assignmentTask));
    }

    @DisplayName("Test setting assignment status")
    @Test
    public void testSettingAssignmentStatus(){
        when(assignmentRepository.findById(1L)).thenReturn(Optional.ofNullable(assignment));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        assignmentService.setAssignmentStatus(1L,false,true);
        verify(assignmentRepository,times(1)).save(assignment);
        assertTrue(assignment.isComplete());
        assertTrue(assignment.getProgress() == 100);
    }

    @DisplayName("Test setting assignment task status")
    @Test
    public void testSettingAssignmentTaskStatus(){
        when(assignmentTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(assignmentTask));
        when(assignmentTaskRepository.save(assignmentTask)).thenReturn(assignmentTask);
        assignmentService.setAssignmentTaskStatus(1L,false,true);
        verify(assignmentTaskRepository,times(1)).save(assignmentTask);
        assertTrue(assignmentTask.isComplete());
        assertTrue(assignmentTask.getProgress() == 100);
    }
    @DisplayName("Test getting user assignments in progress")
    @Test
    public void testGettingUserAssignmentsInProgress(){
        assignment.setInProgress(true);
        when(assignmentRepository.findByInProgressTrueAndUsers(user)).thenReturn(user.getAssignments());
        List<Assignment> inProgress = assignmentService.getUserAssignmentsInProgress(user);
        verify(assignmentRepository,times(1)).findByInProgressTrueAndUsers(user);
        assertTrue(inProgress.contains(assignment));
    }

    @DisplayName("Test get the number of assignment tasks a user has completed for the week")
    @Test
    public void testGetNumberOfAssignmentTaskCompletedForWeek(){
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            AssignmentTask test = new AssignmentTask();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            if(i % 2 == 0){
                cal.add(Calendar.DAY_OF_MONTH, -i);
                test.setFinished(cal.getTime());
                test.setComplete(true);
            }
            testList.add(test);
        }
        user.getAssignmentTasks().addAll(testList);
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        List<Integer> data = assignmentService.getNumberOfAssignmentTasksCompletedForWeek(1L);
        Integer[] compare = {1,0,1,0,1,0,1};
        //Test size and if the results are what they are supposed to be
        assertTrue(data.size() == 7);
        assertEquals(Arrays.asList(compare),data);
    }

    @DisplayName("Test getting suggested Assignment Tasks")
    @Test
    public void testGettingSuggestedAssignmentTasks(){
        user.setMoodRating((short) 5);
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            AssignmentTask test = new AssignmentTask();
            test.setStartDate(new Date());
            test.setComplete(false);
            test.setWorkload((short) 4);
            test.setMood((short) 4);
            //Task that should be suggested based on affect on mood rating
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            if (i % 4 == 0) {
                cal.add(Calendar.DAY_OF_MONTH, 5);
                test.setEndDate(cal.getTime());
                test.setMood((short) 0);
                test.setWorkload((short) 0);
                testList.add(test);
            }
            //Should suggest task based on completion date
            else if (i % 5 == 0) {
                cal.add(Calendar.DAY_OF_MONTH, 3);
                test.setEndDate(cal.getTime());
                testList.add(test);
            }
            //Should suggest task that is overdue
            else if (i % 7 == 0) {
                cal.add(Calendar.DAY_OF_MONTH, -1);
                test.setEndDate(cal.getTime());
                testList.add(test);
            }
        }
        user.setAssignmentTasks(testList);
        for (AssignmentTask task: testList) {
            when(utils.shouldSuggestTask(task.isComplete(), user, task.getMood(), task.getWorkload(), task.getStartDate(), task.getEndDate())).thenReturn(true);
        }
        List<AssignmentTask> suggested = assignmentService.getSuggestedAssignmentTasks(user);
        assertTrue(suggested.size() == 7);
        assertTrue(suggested.containsAll(testList));
        List<AssignmentTask> testAgainst = testList.stream().filter((task) -> utils.shouldSuggestTask(task.isComplete(),user, task.getMood(), task.getWorkload(), task.getStartDate(),task.getEndDate())).toList();
        assertEquals(testAgainst,suggested);
        verify(utils, atLeastOnce()).shouldSuggestTask(anyBoolean(), any(MyUser.class), anyShort(), anyShort(), any(Date.class), any(Date.class));
    }

    @DisplayName("Test getting assignment closest to deadline")
    @Test
    public void testGettingAssignmentCloseToDeadline(){
        List<Assignment> testList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        Date start = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        for (int i = 0; i < 5; i++) {
            Assignment test = new Assignment();
            cal.add(Calendar.DAY_OF_MONTH, i);
            test.setComplete(false);
            test.setUrgent(true);
            test.setImportant(true);
            test.setEndDate(cal.getTime());
            testList.add(test);
        }
        when(assignmentRepository.findByEndDateBetweenAndUsersAndCompleteIsFalse(any(Date.class), any(Date.class), any(MyUser.class))).thenReturn(testList);
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getPriority(eq(true), eq(true))).thenReturn(3);
        List<Assignment> results = assignmentService.getAssignmentsCloseToDeadline(user);
        assertEquals(testList.size(), results.size());
        assertTrue(results.containsAll(testList));
        List<Assignment> sortedAssignments = new ArrayList<>(testList);
        sortedAssignments.sort((a1, a2) -> Integer.compare(utils.getPriority(a2.isImportant(), a2.isUrgent()), utils.getPriority(a1.isImportant(), a1.isUrgent())));
        assertEquals(sortedAssignments, results);
        for (Assignment assignment : results) {
            assertTrue(assignment.getEndDate().before(cal.getTime()) || assignment.getEndDate().equals(cal.getTime()));
        }
        assertFalse(results.stream().anyMatch(Assignment::isComplete));
    }

    @DisplayName("Test getting top 6 recently completed assignment tasks")
    @Test
    public void testGettingTop6RecentlyCompletedTasks(){
        List<AssignmentTask> testList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        for (int i = 6; i > 0 ; i--) {
            AssignmentTask test = new AssignmentTask();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            test.setFinished(cal.getTime());
            testList.add(test);
        }
        user.setAssignmentTasks(testList);
        Collections.reverse(testList);
        when(assignmentTaskRepository.findTop6ByUsersAndFinishedBeforeOrderByFinishedDesc(any(MyUser.class), any(Date.class))).thenReturn(testList);
        List<AssignmentTask> result = assignmentService.getTop6RecentlyCompletedTasks(user);
        verify(assignmentTaskRepository,times(1)).findTop6ByUsersAndFinishedBeforeOrderByFinishedDesc(user,new Date());
        assertTrue(result.containsAll(testList));
        assertEquals(testList,result);
    }

    @DisplayName("Test getting assignments available for chat")
    @Test
    public void testGettingAssignmentsAvailableForChat(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(assignmentRepository.findByGroupChatIsNullAndCompleteFalseAndTitleContainingIgnoreCaseAndUsers("assignment",user)).thenReturn(user.getAssignments());
        when(userService.getUsersDTO(assignment.getUsers())).thenReturn(users);
        List<AssignmentDTO> result = assignmentService.getAssignmentsAvailableForChat("assignment",mockPrincipal);
        verify(assignmentRepository,times(1)).findByGroupChatIsNullAndCompleteFalseAndTitleContainingIgnoreCaseAndUsers("assignment",user);
        assertEquals(result.get(0).getId(),assignment.getId());
    }

    @DisplayName("Test updating assignment dates")
    @Test
    public void testUpdatingAssignmentDates(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date start = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date end = cal.getTime();
        when(assignmentRepository.findById(1L)).thenReturn(Optional.ofNullable(assignment));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        assignmentService.updateAssignmentDates(1L,start,end);
        verify(assignmentRepository,times(1)).save(assignment);
        assertEquals(start,assignment.getStartDate());
        assertEquals(end,assignment.getEndDate());
    }

    @DisplayName("Test updating assignment task dates")
    @Test
    public void testUpdatingAssignmentTaskDates(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date start = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date end = cal.getTime();
        when(assignmentTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(assignmentTask));
        when(assignmentTaskRepository.save(assignmentTask)).thenReturn(assignmentTask);
        assignmentService.updateAssignmentTaskDates(1L,start,end);
        verify(assignmentTaskRepository,times(1)).save(assignmentTask);
        assertEquals(start,assignmentTask.getStartDate());
        assertEquals(end,assignmentTask.getEndDate());
    }

    @DisplayName("Test getting assignments to start today")
    @Test
    public void testGettingAssignmentsToStart(){
        List<Assignment> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Assignment test = new Assignment();
            test.setStartDate(new Date());
            test.setEndDate(new Date());
            test.setInProgress(false);
            test.setComplete(false);
            testList.add(test);
        }
        Date currentDate = new Date();
        Date start = getStartOfDay(currentDate);
        Date end = getEndOfDay(currentDate);
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        when(assignmentRepository.findByUsersAndStartDateBetweenAndInProgressFalseAndCompleteFalse(user,start,end)).thenReturn(testList);
        List<Assignment> result = assignmentService.getAssignmentsToStartToday(user);
        verify(assignmentRepository,times(1)).findByUsersAndStartDateBetweenAndInProgressFalseAndCompleteFalse(user,start,end);
        assertTrue(result.containsAll(testList));
        for (Assignment a: result) {
            assertTrue(!a.isInProgress());
            assertTrue(!a.isComplete());
            assertTrue(a.getStartDate().after(start) && a.getStartDate().before(end));
        }
    }

    @DisplayName("Test getting assignments due today")
    @Test
    public void testGettingAssignmentsDueToday(){
        List<Assignment> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Assignment test = new Assignment();
            test.setStartDate(new Date());
            test.setEndDate(new Date());
            test.setComplete(false);
            testList.add(test);
        }
        Date currentDate = new Date();
        Date start = getStartOfDay(currentDate);
        Date end = getEndOfDay(currentDate);
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        when(assignmentRepository.findByEndDateBetweenAndUsersAndCompleteIsFalse(start,end,user)).thenReturn(user.getAssignments());
        List<Assignment> result = assignmentService.getAssignmentsDueToday(user);
        verify(assignmentRepository,times(1)).findByEndDateBetweenAndUsersAndCompleteIsFalse(start,end,user);
        assertTrue(result.contains(assignment));
        for (Assignment a: result) {
            assertTrue(!a.isComplete());
            assertTrue(a.getEndDate().after(start) && a.getEndDate().before(end));
        }
    }

    @DisplayName("Test getting important/urgent assignment tasks")
    @Test
    public void testGettingImportantUrgentAssignmentTasks(){
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AssignmentTask test = new AssignmentTask();
            test.setComplete(false);
            test.setUrgent(true);
            test.setImportant(true);
            testList.add(test);
        }
        Date currentDate = new Date();
        Date start = getStartOfDay(currentDate);
        Date end = getEndOfDay(currentDate);
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        when(assignmentTaskRepository.findByUsersAndUrgentIsTrueAndImportantIsTrueAndCompleteFalseAndStartDateBetween(user,start,end)).thenReturn(testList);
        List<AssignmentTask> result = assignmentService.getImportantUrgentAssignmentTasksForToday(user);
        verify(assignmentTaskRepository,times(1)).findByUsersAndUrgentIsTrueAndImportantIsTrueAndCompleteFalseAndStartDateBetween(user,start,end);
        assertTrue(result.containsAll(testList));
        for (AssignmentTask task: result) {
            assertTrue(task.isImportant());
            assertTrue(task.isUrgent());
        }
    }

    @DisplayName("Test getting assignment tasks in progress")
    @Test
    public void testGettingAssignmentTasksInProgress(){
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AssignmentTask test = new AssignmentTask();
            test.setInProgress(true);
            testList.add(test);
        }
        when(assignmentTaskRepository.findByUsersAndInProgressTrue(user)).thenReturn(testList);
        List<AssignmentTask> result = assignmentService.getAssignmentTasksInProgress(user);
        verify(assignmentTaskRepository,times(1)).findByUsersAndInProgressTrue(user);
        assertTrue(result.containsAll(testList));
        for (AssignmentTask task: result) {
            assertTrue(task.isInProgress());
        }
    }

    @DisplayName("Test getting assignments tasks closet to completion")
    @Test
    public void testGetAssignmentTasksClosestToCompletion(){
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AssignmentTask test = new AssignmentTask();
            test.setInProgress(true);
            test.setProgress(75);
            testList.add(test);
        }
        when(assignmentTaskRepository.findByUsersAndInProgressTrueAndProgressGreaterThanEqual(user,70)).thenReturn(testList);
        List<AssignmentTask> result = assignmentService.getAssignmentTasksClosestToCompletion(user);
        verify(assignmentTaskRepository,times(1)).findByUsersAndInProgressTrueAndProgressGreaterThanEqual(user,70);
        assertTrue(result.containsAll(testList));
        for (AssignmentTask task: result) {
            assertTrue(task.isInProgress());
            assertTrue(task.getProgress() >= 70);
        }
    }

    @DisplayName("Test getting assignment tasks completed today")
    @Test
    public void testGetAssignmentTaskCompletedToday(){
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AssignmentTask test = new AssignmentTask();
            test.setComplete(true);
            test.setFinished(new Date());
            testList.add(test);
        }
        Date currentDate = new Date();
        Date start = getStartOfDay(currentDate);
        Date end = getEndOfDay(currentDate);
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        when(assignmentTaskRepository.findByUsersAndCompleteTrueAndFinishedBetween(user,start,end)).thenReturn(testList);
        List<AssignmentTask> result = assignmentService.getAssignmentTasksCompletedToday(user);
        verify(assignmentTaskRepository,times(1)).findByUsersAndCompleteTrueAndFinishedBetween(user,start,end);
        assertTrue(result.containsAll(testList));
        for (AssignmentTask task: result) {
            assertTrue(task.isComplete());
            assertTrue(task.getFinished().after(start) && task.getFinished().before(end));
        }
    }

    @DisplayName("Test getting assignment tasks to be completed today")
    @Test
    public void testGetAssignmentTaskToBeCompletedToday(){
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AssignmentTask test = new AssignmentTask();
            test.setComplete(false);
            test.setEndDate(new Date());
            testList.add(test);
        }
        Date currentDate = new Date();
        Date start = getStartOfDay(currentDate);
        Date end = getEndOfDay(currentDate);
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        when(assignmentTaskRepository.findByUsersAndEndDateBetween(user,start,end)).thenReturn(testList);
        List<AssignmentTask> result = assignmentService.getAssignmentTasksToCompleteToday(user);
        verify(assignmentTaskRepository,times(1)).findByUsersAndEndDateBetween(user,start,end);
        assertTrue(result.containsAll(testList));
        for (AssignmentTask task: result) {
            assertTrue(!task.isComplete());
            assertTrue(task.getEndDate().after(start) && task.getEndDate().before(end));
        }
    }

    @DisplayName("Test getting assignment task not started for today")
    @Test
    public void testGetAssignmentTasksToBeStartedToday(){
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AssignmentTask test = new AssignmentTask();
            test.setComplete(false);
            test.setInProgress(false);
            test.setStartDate(new Date());
            test.setEndDate(new Date());
            testList.add(test);
        }
        Date currentDate = new Date();
        Date start = getStartOfDay(currentDate);
        Date end = getEndOfDay(currentDate);
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        when(assignmentTaskRepository.findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(user,start,end)).thenReturn(testList);
        List<AssignmentTask> result = assignmentService.getAssignmentTasksToBeStartedToday(user);
        verify(assignmentTaskRepository,times(1)).findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(user,start,end);
        assertTrue(result.containsAll(testList));
        for (AssignmentTask task: result) {
            assertTrue(!task.isComplete());
            assertTrue(!task.isInProgress());
            assertTrue(task.getStartDate().after(start) && task.getEndDate().before(end));
        }
    }

    @DisplayName("Test getting assignment tasks in progress for today")
    @Test
    public void testGetAssignmentTasksInProgressForToday(){
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AssignmentTask test = new AssignmentTask();
            test.setInProgress(true);
            test.setEndDate(new Date());
            testList.add(test);
        }
        Date currentDate = new Date();
        Date start = getStartOfDay(currentDate);
        Date end = getEndOfDay(currentDate);
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        when(assignmentTaskRepository.findByUsersAndEndDateBetweenAndInProgressTrue(user,start,end)).thenReturn(testList);
        List<AssignmentTask> result = assignmentService.getAssignmentTasksInProgressForToday(user);
        verify(assignmentTaskRepository,times(1)).findByUsersAndEndDateBetweenAndInProgressTrue(user,start,end);
        assertTrue(result.containsAll(testList));
        for (AssignmentTask task: result) {
            assertTrue(task.isInProgress());
            assertTrue(task.getEndDate().after(start) && task.getEndDate().before(end));
        }
    }

    @Test
    @DisplayName("Test getting assignment tasks not started but to be completed today")
    public void testGetAssignmentTaskNotStartedForToday(){
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AssignmentTask test = new AssignmentTask();
            test.setInProgress(false);
            test.setComplete(false);
            test.setEndDate(new Date());
            testList.add(test);
        }
        Date currentDate = new Date();
        Date start = getStartOfDay(currentDate);
        Date end = getEndOfDay(currentDate);
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        when(assignmentTaskRepository.findByUsersAndEndDateBetweenAndCompleteFalseAndInProgressFalse(user,start,end)).thenReturn(testList);
        List<AssignmentTask> result = assignmentService.getAssignmentTasksNotStartedAndForToday(user);
        verify(assignmentTaskRepository,times(1)).findByUsersAndEndDateBetweenAndCompleteFalseAndInProgressFalse(user,start,end);
        assertTrue(result.containsAll(testList));
        for (AssignmentTask task: result) {
            assertTrue(!task.isInProgress());
            assertTrue(!task.isComplete());
            assertTrue(task.getEndDate().after(start) && task.getEndDate().before(end));
        }
    }

    @DisplayName("Test getting assignments to be started in 30 minutes")
    @Test
    public void testGetAssignmentsToStartIn30Minutes(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE,15);
        List<Assignment> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            assignment.setId((long) i);
            assignment.setStartDate(cal.getTime());
            assignment.setInProgress(false);
            assignment.setComplete(false);
            testList.add(assignment);
        }
        Date current = new Date();
        Date thirtyMinsFromNow = getThirtyMinutesFromNow(current);
        when(utils.get30MinutesFromNow(any(Date.class))).thenReturn(thirtyMinsFromNow);
        when(assignmentRepository.findByUsersAndStartDateBetweenAndInProgressFalseAndCompleteFalse(eq(user),any(Date.class),eq(thirtyMinsFromNow))).thenReturn(testList);
        when(userService.getUsersDTO(anyList())).thenReturn(users);
        List<AssignmentDTO> result = assignmentService.getAssignmentToStartWithin30Minutes(user);
        verify(assignmentRepository,times(1)).findByUsersAndStartDateBetweenAndInProgressFalseAndCompleteFalse(eq(user),any(Date.class),eq(thirtyMinsFromNow));
        for (AssignmentDTO a: result) {
            assertTrue(a.getStartDate().after(current) && a.getStartDate().before(thirtyMinsFromNow));
        }
    }

    @DisplayName("Test getting assignment tasks that start within 30 minutes")
    @Test
    public void testGetAssignmentTasksToStartWithin30Minutes(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE,15);
        List<AssignmentTask> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            assignmentTask.setId((long) i);
            assignmentTask.setStartDate(cal.getTime());
            assignmentTask.setInProgress(false);
            assignmentTask.setComplete(false);
            testList.add(assignmentTask);
        }
        Date current = new Date();
        Date thirtyMinsFromNow = getThirtyMinutesFromNow(current);
        when(utils.get30MinutesFromNow(any(Date.class))).thenReturn(thirtyMinsFromNow);
        when(assignmentTaskRepository.findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(eq(user),any(Date.class),eq(thirtyMinsFromNow))).thenReturn(testList);
        when(userService.getUsersDTO(anyList())).thenReturn(users);
        List<AssignmentTaskDTO> result = assignmentService.getAssignmentTasksToStartWithin30Minutes(user);
        verify(assignmentTaskRepository,times(1)).findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(eq(user),any(Date.class),eq(thirtyMinsFromNow));
        for (AssignmentTaskDTO a: result) {
            assertTrue(a.getStartDate().after(current) && a.getStartDate().before(thirtyMinsFromNow));
        }
    }


    //Helper method
    private Date getStartOfDay(Date current){
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        return  cal.getTime();
    }
    //Helper method
    private Date getEndOfDay(Date current){
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.set(Calendar.HOUR_OF_DAY,23);
        cal.set(Calendar.MINUTE,59);
        cal.set(Calendar.SECOND,59);
        return  cal.getTime();
    }

    //Helper method
    private Date getThirtyMinutesFromNow(Date current){
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.add(Calendar.MINUTE, 30);
        Date thirtyMinutesFromNow = cal.getTime();
        return thirtyMinutesFromNow;
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
