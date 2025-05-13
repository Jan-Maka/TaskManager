package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.component.CommonUtils;
import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.Task.DTO.GroupTaskDTO;
import com.example.taskmanager.domain.Task.DTO.TaskDTO;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.DTO.UserDTO;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.domain.User.Role;
import com.example.taskmanager.repo.Task.CategoryRepository;
import com.example.taskmanager.repo.Task.GroupTaskRepository;
import com.example.taskmanager.repo.Task.TaskRepository;
import com.example.taskmanager.repo.User.UserRepository;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    MyUser user;
    MyUser user2;
    Task task;
    TaskDTO taskDTO;
    Category category;
    GroupTask groupTask;
    GroupTaskDTO groupTaskDTO;
    Role role1;
    Role role2;
    List<Role> roles = new ArrayList<>();
    List<MyUser> users = new ArrayList<>();
    List<UserDTO> usersDTO = new ArrayList<>();
    Date start;
    Date end;

    MockPrincipal mockPrincipal;

    @InjectMocks
    TaskService taskService;
    @Mock
    UserService userService;
    @Mock
    CommonUtils utils;
    @Mock
    TaskRepository taskRepository;
    @Mock
    GroupTaskRepository groupTaskRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CategoryRepository categoryRepository;



    @BeforeEach
    public void setUp() {
        Date currentDate = new Date();
        this.start = getStartOfDay(currentDate);
        this.end = getEndOfDay(currentDate);
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
        user2.setId(2L);
        users.add(user);
        users.add(user2);
        for (MyUser user:users) {
            usersDTO.add(new UserDTO(user.getId(),user.getUsername(),user.getBio(),user.getLocation(),user.getBase64ProfilePic()));
        }
        this.category = new Category("Test",user);
        this.task = new Task(user,"test",false,false,(short)3,(short) 3,"desc",category,new Date(),new Date(),0,new Date(),false,false);
        task.setId(1L);
        category.getTasks().add(task);
        category.setId(1L);
        user.getCategories().add(category);
        user.getTasks().add(task);
        this.groupTask = new GroupTask("test",false,false,(short)0,(short)0,"desc",new Date(),new Date(),0,new Date(),false,false,user,users);
        groupTask.setId(1L);
        user.getGroupTasks().add(groupTask);
        user2.getGroupTasks().add(groupTask);
        this.taskDTO = new TaskDTO(1L,user.getId(),"update",true,true,(short) 0,(short) 0,"updated!", category.getId(), category.getName(), new Date(),new Date(),50,new Date(),new Date(),false,true,false);
        this.groupTaskDTO = new GroupTaskDTO(1L,user.getId(),"update",true,true,(short) 0,(short) 0,"updated!", new Date(),new Date(),50,new Date(),new Date(),false,true,user.getUsername());
        groupTaskDTO.setUsers(usersDTO);
        this.mockPrincipal = new MockPrincipal(user.getEmail());
    }

    @DisplayName("Test task exists by id")
    @Test
    public void testTaskExistsById(){
        when(taskRepository.existsById(1L)).thenReturn(true);
        assertTrue(taskService.taskExistsById(1L));
        verify(taskRepository,times(1)).existsById(1L);
    }

    @DisplayName("Test user is owner of task")
    @Test
    public void testUserIsOwnerOfTask(){
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(task));
        assertTrue(taskService.isOwner(1L,mockPrincipal));
    }

    @DisplayName("Test getting a users tasks")
    @Test
    public void testGetUserTasks(){
        Task test = new Task();
        test.setArchive(true);
        user.getTasks().add(test);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        List<Task> result = taskService.getUserTasks(mockPrincipal);
        assertTrue(!result.contains(test));
        assertTrue(result.contains(task));
    }

    @DisplayName("Test creating task DTO")
    @Test
    public void testCreatingTaskDTO(){
        TaskDTO result = taskService.createTaskDTO(task);
        assertEquals(task.getId(),result.getId());
        assertEquals(task.getUser().getId(),result.getUser());
        assertEquals(task.getTitle(),result.getTitle());
        assertEquals(task.getDescription(),result.getDescription());
        assertEquals(task.getMood(),result.getMood());
        assertEquals(task.getWorkload(),result.getWorkload());
        assertEquals(task.isImportant(),result.isImportant());
        assertEquals(task.isUrgent(),result.isUrgent());
        assertEquals(task.getCategory().getId(),result.getCategory());
        assertEquals(task.getCategory().getName(),result.getCategoryName());
        assertEquals(task.getProgress(),result.getProgress());
        assertEquals(task.isInProgress(),result.isInProgress());
        assertEquals(task.isComplete(),result.isComplete());
    }

    @DisplayName("Test getting task by id")
    @Test
    public void testGetTaskById(){
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(task));
        assertEquals(task,taskService.getTaskById(1L));
        verify(taskRepository,times(1)).findById(1L);
    }

    @DisplayName("Test creating task from DTO")
    @Test
    public void testCreatingTask() throws IOException {
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.ofNullable(category));
        taskService.CreateTask(taskDTO,new ArrayList<>());
        verify(taskRepository,times(1)).save(any(Task.class));
    }

    @DisplayName("Test deleting task by id")
    @Test
    public void testDeletingTaskById(){
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(task));
        taskService.deleteTaskById(1L);
        verify(taskRepository,times(1)).delete(task);
        assertTrue(!user.getTasks().contains(task));
    }

    @DisplayName("Test updating task")
    @Test
    public void testUpdatingTask() throws IOException {
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(task));
        when(categoryRepository.findById(1L)).thenReturn(Optional.ofNullable(category));
        taskService.updateTask(taskDTO,new ArrayList<>());
        verify(taskRepository,times(1)).save(task);
        assertEquals(taskDTO.getTitle(),task.getTitle());
        assertEquals(taskDTO.getWorkload(),task.getWorkload());
        assertEquals(taskDTO.getMood(),task.getMood());
        assertEquals(taskDTO.getDescription(),task.getDescription());
        assertEquals(taskDTO.isUrgent(),task.isUrgent());
        assertEquals(taskDTO.isImportant(),task.isImportant());
        assertEquals(taskDTO.getProgress(),task.getProgress());
    }

    @DisplayName("Test archiving a task")
    @Test
    public void testArchivingTask(){
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(task));
        taskService.setArchiveTask(1L,true);
        verify(taskRepository,times(1)).save(task);
        assertTrue(task.isArchive());
    }

    @DisplayName("Test getting user archived tasks")
    @Test
    public void testGetUserArchivedTasks(){
        List<Task> archivedTest = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Task test = new Task();
            test.setArchive(true);
            archivedTest.add(test);
        }
        user.getTasks().addAll(archivedTest);
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(taskRepository.findByUserAndArchiveTrue(user)).thenReturn(archivedTest);
        List<Task> result = taskService.getUserArchivedTasks(mockPrincipal);
        verify(taskRepository,times(1)).findByUserAndArchiveTrue(user);
        assertEquals(archivedTest,result);
    }

    @DisplayName("Test getting archived tasks from database query based on user search")
    @Test
    public void testGetArchivedTaskSearchResult(){
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        taskService.getArchivedTaskSearchResult(anyString(),mockPrincipal);
        verify(taskRepository,times(1)).findByUserAndTitleContainingIgnoreCaseAndArchiveTrue(eq(user),anyString());
    }

    @DisplayName("Test getting tasks from database based on user search query")
    @Test
    public void testSearchingAllTasks(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        taskService.searchAllTasks(anyString(),mockPrincipal);
        verify(taskRepository,times(1)).findByTitleContainingIgnoreCaseAndUserAndArchiveFalse(anyString(),eq(user));
    }
    @DisplayName("Test getting tasks from category database based on user search query")
    @Test
    public void testSearchTasksByCategory(){
        when(categoryRepository.findById(1L)).thenReturn(Optional.ofNullable(category));
        taskService.searchTasksByCategory(anyString(), category.getId());
        verify(taskRepository,times(1)).findByTitleContainingIgnoreCaseAndCategoryAndArchiveFalse(anyString(),eq(category));
    }

    @DisplayName("Test group task exists by id")
    @Test
    public void testGroupTaskExistsById(){
        when(groupTaskRepository.existsById(1L)).thenReturn(true);
        assertTrue(taskService.groupTaskExistsById(1L));
        verify(groupTaskRepository,times(1)).existsById(1L);
    }

    @DisplayName("Test getting group task by id")
    @Test
    public void testGetGroupTaskById(){
        when(groupTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(groupTask));
        assertEquals(groupTask,taskService.getGroupTaskById(1L));
        verify(groupTaskRepository,times(1)).findById(1L);
    }

    @DisplayName("Test if user is group task owner")
    @Test
    public void testUserIsGroupTaskOwner(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(groupTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(groupTask));
        assertTrue(taskService.isGroupTaskOwner(1L,mockPrincipal));
    }

    @DisplayName("Test user is in group task")
    @Test
    public void testUserIsInGroupTask(){
        when(groupTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(groupTask));
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);
        assertTrue(taskService.isInGroupTask(1L,new MockPrincipal(user2.getEmail())));
    }

    @DisplayName("Test creating group task from DTO")
    @Test
    public void testCreatingGroupTask() throws IOException {
        groupTaskDTO.getUsers().remove(0);
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(userRepository.findById(2L)).thenReturn(Optional.ofNullable(user2));
        taskService.createGroupTask(groupTaskDTO,new ArrayList<>());
        verify(groupTaskRepository,times(1)).save(any(GroupTask.class));
    }

    @DisplayName("Test creating group task DTO")
    @Test
    public void testCreatingGroupTaskDTO(){
        when(userService.getUsersDTO(groupTask.getUsers())).thenReturn(usersDTO);
        GroupTaskDTO result = taskService.createGroupTaskDTO(groupTask);
        assertEquals(groupTask.getId(),result.getId());
        assertEquals(groupTask.getOwner().getId(),result.getUser());
        assertEquals(groupTask.getTitle(),result.getTitle());
        assertEquals(groupTask.getDescription(),result.getDescription());
        assertEquals(groupTask.getMood(),result.getMood());
        assertEquals(groupTask.getWorkload(),result.getWorkload());
        assertEquals(groupTask.isImportant(),result.isImportant());
        assertEquals(groupTask.isUrgent(),result.isUrgent());
        assertEquals(groupTask.getProgress(),result.getProgress());
        assertEquals(groupTask.isInProgress(),result.isInProgress());
        assertEquals(groupTask.isComplete(),result.isComplete());
        assertEquals(usersDTO,result.getUsers());
    }

    @DisplayName("Test getting users group tasks")
    @Test
    public void testGetUserGroupTasks(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        List<GroupTask> result = taskService.getUserGroupTasks(mockPrincipal);
        assertEquals(user.getGroupTasks(),result);
    }

    @DisplayName("Test deleting group task by id")
    @Test
    public void testDeletingGroupTaskById(){
        when(groupTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(groupTask));
        taskService.deleteGroupTaskById(1L);
        verify(groupTaskRepository,times(1)).delete(groupTask);
        assertTrue(!user.getGroupTasks().contains(groupTask));
        assertTrue(!user2.getGroupTasks().contains(groupTask));
    }

    @DisplayName("Test getting group tasks from database query based on user search query")
    @Test
    public void testSearchGroupTasks(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        taskService.searchGroupTasks(anyString(),mockPrincipal);
        verify(groupTaskRepository,times(1)).findByTitleContainingIgnoreCaseAndUsers(anyString(),eq(user));
    }

    @DisplayName("Test updating a group task from DTO")
    @Test
    public void testUpdatingGroupTask() throws IOException {
        groupTaskDTO.getUsers().remove(1);
        when(groupTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(groupTask));
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        taskService.updateGroupTask(groupTaskDTO,new ArrayList<>());
        verify(groupTaskRepository,times(1)).save(groupTask);
        assertEquals(groupTaskDTO.getTitle(),groupTask.getTitle());
        assertEquals(groupTaskDTO.getWorkload(),groupTask.getWorkload());
        assertEquals(groupTaskDTO.getMood(),groupTask.getMood());
        assertEquals(groupTaskDTO.getDescription(),groupTask.getDescription());
        assertEquals(groupTaskDTO.isUrgent(),groupTask.isUrgent());
        assertEquals(groupTaskDTO.isImportant(),groupTask.isImportant());
        assertEquals(groupTaskDTO.getProgress(),groupTask.getProgress());
        assertTrue(!groupTask.getUsers().contains(user2));
    }

    @DisplayName("Test removing a user from a group task")
    @Test
    public void testRemoveUserFromGroupTask(){
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(user2);
        when(groupTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(groupTask));
        taskService.removeUserFromGroupTask(1L,new MockPrincipal(user2.getEmail()));
        verify(userRepository,times(1)).save(user2);
        verify(groupTaskRepository,times(1)).save(groupTask);
        assertTrue(!groupTask.getUsers().contains(user2));
        assertTrue(!user2.getGroupTasks().contains(groupTask));
    }

    @DisplayName("Test getting 6 most recently complete tasks database query")
    @Test
    public void testGetTop6RecentlyCompletedTasks(){
        List<Task> testList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        for (int i = 6; i > 0 ; i--) {
            Task test = new Task();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            test.setFinished(cal.getTime());
            testList.add(test);
        }
        user.setTasks(testList);
        Collections.reverse(testList);
        when(taskRepository.findTop6ByUserAndFinishedBeforeOrderByFinishedDesc(any(MyUser.class), any(Date.class))).thenReturn(testList);
        List<Task> result = taskService.getTop6RecentlyCompletedTasks(user);
        verify(taskRepository,times(1)).findTop6ByUserAndFinishedBeforeOrderByFinishedDesc(eq(user),any(Date.class));
        assertTrue(result.containsAll(testList));
        assertEquals(testList,result);
    }

//    @DisplayName("Test getting the number of completed tasks for past 7 days")
//    @Test
//    public void testGetNumberOfTaskCompletedForWeek(){
//        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
//    }
//
//    @DisplayName("Test getting the number of completed group tasks for past 7 days")
//    @Test
//    public void testGetNumberOfGroupTaskCompletedForWeek(){
//        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
//    }

    @DisplayName("Test getting group tasks available for chat")
    @Test
    public void testGetGroupTasksAvailableForChat(){
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(groupTaskRepository.findByGroupChatIsNullAndCompleteFalseAndTitleContainingIgnoreCaseAndUsers(anyString(),eq(user))).thenReturn(user.getGroupTasks());
        when(userService.getUsersDTO(groupTask.getUsers())).thenReturn(usersDTO);
        List<GroupTaskDTO> result = taskService.getGroupTasksAvailableForChat(anyString(),mockPrincipal);
        verify(groupTaskRepository,times(1)).findByGroupChatIsNullAndCompleteFalseAndTitleContainingIgnoreCaseAndUsers(anyString(),eq(user));
        assertEquals(result.get(0).getId(),groupTask.getId());
    }

    @DisplayName("Test updating task dates")
    @Test
    public void testUpdatingTaskDates(){
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(task));
        taskService.updateTaskDates(1L,start,end);
        verify(taskRepository,times(1)).save(task);
        assertEquals(start,task.getStartDate());
        assertEquals(end,task.getEndDate());
    }

    @DisplayName("Test updating group task dates")
    @Test
    public void testUpdatingGroupTaskDates(){
        when(groupTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(groupTask));
        taskService.updateGroupTaskDates(1L,start,end);
        verify(groupTaskRepository,times(1)).save(groupTask);
        assertEquals(start,groupTask.getStartDate());
        assertEquals(end,groupTask.getEndDate());
    }

    @DisplayName("Test getting suggested tasks")
    @Test
    public void testGetSuggestedTasks(){
        user.setMoodRating((short) 5);
        List<Task> testList = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            Task test = new Task();
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
        user.setTasks(testList);
        for (Task task: testList) {
            when(utils.shouldSuggestTask(task.isComplete(), user, task.getMood(), task.getWorkload(), task.getStartDate(), task.getEndDate())).thenReturn(true);
        }
        List<Task> suggested = taskService.getSuggestedTasks(user);
        assertTrue(suggested.size() == 7);
        assertTrue(suggested.containsAll(testList));
        List<Task> testAgainst = testList.stream().filter((task) -> utils.shouldSuggestTask(task.isComplete(),user, task.getMood(), task.getWorkload(), task.getStartDate(),task.getEndDate())).toList();
        assertEquals(testAgainst,suggested);
        verify(utils, atLeastOnce()).shouldSuggestTask(anyBoolean(), any(MyUser.class), anyShort(), anyShort(), any(Date.class), any(Date.class));
    }

    @DisplayName("Test getting suggested group tasks")
    @Test
    public void testGetSuggestedGroupTasks() {
        user.setMoodRating((short) 5);
        List<GroupTask> testList = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            GroupTask test = new GroupTask();
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
        user.setGroupTasks(testList);
        for (GroupTask task : testList) {
            when(utils.shouldSuggestTask(task.isComplete(), user, task.getMood(), task.getWorkload(), task.getStartDate(), task.getEndDate())).thenReturn(true);
        }
        List<GroupTask> suggested = taskService.getSuggestedGroupTask(user);
        assertTrue(suggested.size() == 7);
        assertTrue(suggested.containsAll(testList));
        List<GroupTask> testAgainst = testList.stream().filter((task) -> utils.shouldSuggestTask(task.isComplete(), user, task.getMood(), task.getWorkload(), task.getStartDate(), task.getEndDate())).toList();
        assertEquals(testAgainst, suggested);
        verify(utils, atLeastOnce()).shouldSuggestTask(anyBoolean(), any(MyUser.class), anyShort(), anyShort(), any(Date.class), any(Date.class));
    }

    @DisplayName("Test getting urgent important tasks for current date")
    @Test
    public void testGetUrgentImportantTaskForToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getUrgentImportantTaskForToday(user);
        verify(taskRepository,times(1)).findByUserAndUrgentIsTrueAndImportantIsTrueAndCompleteFalseAndStartDateBetween(user,start,end);
    }

    @DisplayName("Test getting urgent important group tasks for current date")
    @Test
    public void testGetUrgentImportantGroupTasksForToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getUrgentImportantGroupTasksForToday(user);
        verify(groupTaskRepository,times(1)).findByUsersAndUrgentIsTrueAndImportantIsTrueAndCompleteFalseAndStartDateBetween(user,start,end);
    }

    @DisplayName("Test getting tasks in progress")
    @Test
    public void testGetTasksInProgress(){
        taskService.getTasksInProgress(user);
        verify(taskRepository,times(1)).findByUserAndInProgressTrue(user);
    }

    @DisplayName("Test getting group tasks in progress")
    @Test
    public void testGetGroupTasksInProgress(){
        taskService.getGroupTasksInProgress(user);
        verify(groupTaskRepository,times(1)).findByUsersAndInProgressTrue(user);
    }

    @DisplayName("Test getting tasks closest to completion")
    @Test
    public void testGetTasksClosestToCompletion(){
        taskService.getTasksCloseToCompletion(user);
        verify(taskRepository,times(1)).findByUserAndInProgressTrueAndProgressGreaterThanEqual(user,70);
    }
    @DisplayName("Test getting group tasks closest to completion")
    @Test
    public void testGetGroupTasksClosestToCompletion(){
        taskService.getGroupTasksCloseToCompletion(user);
        verify(groupTaskRepository,times(1)).findByUsersAndInProgressTrueAndProgressGreaterThanEqual(user,70);
    }

    @DisplayName("Test getting tasks completed today")
    @Test
    public void testGetTasksCompletedToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getTasksCompletedToday(user);
        verify(taskRepository,times(1)).findByUserAndCompleteTrueAndFinishedBetween(user,start,end);
    }

    @DisplayName("Test getting group tasks completed today")
    @Test
    public void testGetGroupTasksCompletedToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getGroupTasksCompletedToday(user);
        verify(groupTaskRepository,times(1)).findByUsersAndCompleteTrueAndFinishedBetween(user,start,end);
    }

    @DisplayName("Test getting tasks to complete today")
    @Test
    public void testGetTasksToCompleteToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getTasksToCompleteToday(user);
        verify(taskRepository,times(1)).findByUserAndEndDateBetween(user,start,end);
    }

    @DisplayName("Test getting group tasks to complete today")
    @Test
    public void testGetGroupTasksToCompleteToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getGroupTasksToCompleteToday(user);
        verify(groupTaskRepository,times(1)).findByUsersAndEndDateBetween(user,start,end);
    }

    @DisplayName("Test getting tasks not started to start today")
    @Test
    public void testGetTasksNotStartedForToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getTasksNotStartedToStartToday(user);
        verify(taskRepository,times(1)).findByUserAndStartDateBetweenAndCompleteFalseAndInProgressFalse(user,start,end);
    }

    @DisplayName("Test getting groups tasks not started to start today")
    @Test
    public void testGetGroupTasksNotStartedForToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getGroupTasksNotStartedToStartToday(user);
        verify(groupTaskRepository,times(1)).findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(user,start,end);
    }

    @DisplayName("Test getting tasks in progress for today")
    @Test
    public void testGetTasksInProgressForToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getTasksInProgressForToday(user);
        verify(taskRepository,times(1)).findByUserAndEndDateBetweenAndInProgressTrue(user,start,end);
    }

    @DisplayName("Test getting group tasks in progress for today")
    @Test
    public void testGetGroupTasksInProgressForToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getGroupTasksInProgressForToday(user);
        verify(groupTaskRepository,times(1)).findByUsersAndEndDateBetweenAndInProgressTrue(user,start,end);
    }

    @DisplayName("Test getting tasks not started due for today")
    @Test
    public void testGetTasksNotStartedAndForToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getTasksNotStartedAndForToday(user);
        verify(taskRepository,times(1)).findByUserAndEndDateBetweenAndCompleteFalseAndInProgressFalse(user,start,end);
    }

    @DisplayName("Test getting group tasks not started due for today")
    @Test
    public void testGetGroupTasksNotStartedAndForToday(){
        when(utils.getStartDate(any(Date.class))).thenReturn(start);
        when(utils.getEndDate(any(Date.class))).thenReturn(end);
        taskService.getGroupTasksNotStartedAndForToday(user);
        verify(groupTaskRepository,times(1)).findByUsersAndEndDateBetweenAndCompleteFalseAndInProgressFalse(user,start,end);
    }

    @DisplayName("Test setting task status")
    @Test
    public void testSettingTaskStatus(){
        when(taskRepository.findById(1L)).thenReturn(Optional.ofNullable(task));
        when(taskRepository.save(task)).thenReturn(task);
        taskService.setTaskStatus(1L,false,true);
        verify(taskRepository,times(1)).save(task);
        assertTrue(task.isComplete());
        assertEquals(100,task.getProgress());
    }

    @DisplayName("Test setting group task status")
    @Test
    public void testSettingGroupTaskStatus(){
        when(groupTaskRepository.findById(1L)).thenReturn(Optional.ofNullable(groupTask));
        when(groupTaskRepository.save(groupTask)).thenReturn(groupTask);
        taskService.setGroupTaskStatus(1L,false,true);
        verify(groupTaskRepository,times(1)).save(groupTask);
        assertTrue(groupTask.isComplete());
        assertEquals(100,groupTask.getProgress());
    }

    @DisplayName("Test getting tasks that start within 30 minutes")
    @Test
    public void testGetTasksToStartWithin30Minutes(){
        Date thirtyMinutesFromNow = getThirtyMinutesFromNow(new Date());
        when(utils.get30MinutesFromNow(any(Date.class))).thenReturn(thirtyMinutesFromNow);
        taskService.getTasksToStartIn30Minutes(user);
        verify(taskRepository,times(1)).findByUserAndStartDateBetweenAndCompleteFalseAndInProgressFalse(eq(user),any(Date.class),eq(thirtyMinutesFromNow));
    }

    @DisplayName("Test getting group tasks that start within 30 minutes")
    @Test
    public void testGetGroupTasksToStartWithin30Minutes(){
        Date thirtyMinutesFromNow = getThirtyMinutesFromNow(new Date());
        when(utils.get30MinutesFromNow(any(Date.class))).thenReturn(thirtyMinutesFromNow);
        taskService.getGroupTasksToStartIn30Minutes(user);
        verify(groupTaskRepository,times(1)).findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(eq(user),any(Date.class),eq(thirtyMinutesFromNow));
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
