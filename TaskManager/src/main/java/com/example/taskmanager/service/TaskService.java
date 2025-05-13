package com.example.taskmanager.service;

import com.example.taskmanager.component.CommonUtils;
import com.example.taskmanager.component.FileSizeExceededError;
import com.example.taskmanager.domain.File.DTO.FileAttachmentDTO;
import com.example.taskmanager.domain.File.FileAttachment;
import com.example.taskmanager.domain.Messaging.GroupChat;
import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.Task.DTO.GroupTaskDTO;
import com.example.taskmanager.domain.Task.DTO.TaskDTO;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.DTO.UserDTO;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.Messaging.GroupChatRepository;
import com.example.taskmanager.repo.Task.CategoryRepository;
import com.example.taskmanager.repo.Task.GroupTaskRepository;
import com.example.taskmanager.repo.Task.TaskRepository;
import com.example.taskmanager.repo.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private GroupTaskRepository groupTaskRepo;

    @Autowired
    private GroupChatRepository groupChatRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private FileService fileService;

    @Autowired
    private CategoryRepository catRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private CommonUtils utils;

    /**
     * Checks if a task exists by id within database
     * @param id
     * @return
     */
    public boolean taskExistsById(long id){
        return taskRepo.existsById(id);
    }

    /**
     * Checks if a user owns a specific task entity
     * @param id
     * @param principal
     * @return
     */
    public boolean isOwner(Long id, Principal principal){
        Task task = getTaskById(id);
        return task.getUser().getEmail().equals(principal.getName());
    }

    /**
     * Gets all of the users personal tasks
     * @param principal
     * @return
     */
    public List<Task> getUserTasks(Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        List<Task> tasks = user.getTasks().stream().filter(task -> !task.isArchive()).collect(Collectors.toList());
        Collections.reverse(tasks);
        return tasks;
    }

    /**
     * Used to create a taskDTO used mainly used for api calls
     * for front-end displaying via jQuery javascript
     * @param task
     * @return
     */
    public TaskDTO createTaskDTO(Task task){
        TaskDTO taskDTO = new TaskDTO(task.getId(),task.getUser().getId(),task.getTitle(),task.isImportant(),task.isUrgent(),task.getWorkload(),task.getMood(),task.getDescription(),task.getCategory().getId(),task.getCategory().getName(),task.getStartDate(),task.getEndDate(),task.getProgress(),task.getCreated(),task.getModified(),task.isComplete(),task.isInProgress(), task.isArchive());
        if(task.getFinished() != null){
            taskDTO.setFinished(task.getFinished());
        }
        if(!task.getFileAttachments().isEmpty()){
            for(FileAttachment f: task.getFileAttachments()){
                FileAttachmentDTO fileAttachmentDTO = fileService.createFileAttachmentDTO(f);
                taskDTO.getFileAttachments().add(fileAttachmentDTO);
            }
        }
        return taskDTO;
    }

    /**
     * Gets a list of a users personal tasks but in DTO form
     * @param tasks
     * @return
     */
    public List<TaskDTO> getUserTasksDTO(List<Task> tasks){
        List<TaskDTO> taskDTOS = new ArrayList<>();
        for (Task t: tasks) {
            TaskDTO taskDTO = createTaskDTO(t);
            taskDTOS.add(taskDTO);
        }
        return taskDTOS;
    }

    /**
     * Gets a task entity via its id in database
     * @param id
     * @return
     */
    public Task getTaskById(long id){
        return taskRepo.findById(id).get();
    }


    /**
     * Handles mainly post request for creating a task entity for the database
     * by populating entity with information provided via taskDTO and using file service
     * to attach files to the entity
     * @param taskDTO
     * @param files
     * @throws IOException
     */
    public void CreateTask(TaskDTO taskDTO, List<MultipartFile> files) throws IOException {
        MyUser user = userRepo.findById(taskDTO.getUser()).get();
        Category cat = catRepo.findById(taskDTO.getCategory()).get();
        Task task = new Task(user, taskDTO.getTitle(), taskDTO.isImportant(), taskDTO.isUrgent(), taskDTO.getWorkload(),taskDTO.getMood(), taskDTO.getDescription(),cat,taskDTO.getStartDate(),taskDTO.getEndDate(), taskDTO.getProgress(),taskDTO.getModified(),taskDTO.isComplete(), taskDTO.isInProgress());
        task = taskRepo.save(task);
        if(files != null && !files.isEmpty()){
            //Checks if user roles and cumulative size of files in a task
            if(user.hasRole("MEMBER") && fileService.getFilesSize(files) > (1024*1024)*5){
                throw new FileSizeExceededError("File Attachments exceed limit of 5MB!");
            }else if(!user.hasRole("MEMBER") && fileService.getFilesSize(files) > (1024*1024)*2){
                throw new FileSizeExceededError("File Attachments exceed limit of 2MB!");
            }
            fileService.AttachFilesToTask(task,files);

        }
    }

    /**
     * Deletes a task entity in database via id
     * @param id
     */
    public void deleteTaskById(long id){
        Task task = getTaskById(id);
        // Delete associated file attachments
        List<FileAttachment> fileAttachments = task.getFileAttachments();
        for (FileAttachment fileAttachment : fileAttachments) {
            fileService.deleteFileAttachment(fileAttachment);
        }
        // Remove the task from user and category
        MyUser user = task.getUser();
        user.getTasks().remove(task);

        Category category = task.getCategory();
        category.getTasks().remove(task);
        taskRepo.delete(task);
    }

    /**
     * Updates a task using information from taskDTO and also takes files
     * that were added on as attachments
     * @param taskDTO
     * @param files
     * @throws IOException
     */
    @Transactional
    public void updateTask(TaskDTO taskDTO,List<MultipartFile> files) throws IOException {
        Task task = getTaskById(taskDTO.getId());
        Category category = catRepo.findById(taskDTO.getCategory()).get();
        if (!task.getCategory().equals(category)) {
            Category oldCat = task.getCategory();
            oldCat.getTasks().remove(task);
            catRepo.save(oldCat);

            task.setCategory(category);
            taskRepo.save(task);

            category.getTasks().add(task);
            catRepo.save(category);
        }
        task.setTitle(taskDTO.getTitle());
        task.setWorkload(taskDTO.getWorkload());
        task.setMood(taskDTO.getMood());
        task.setDescription(taskDTO.getDescription());
        task.setUrgent(taskDTO.isUrgent());
        task.setImportant(taskDTO.isImportant());
        task.setStartDate(taskDTO.getStartDate());
        task.setEndDate(taskDTO.getEndDate());
        task.setProgress(taskDTO.getProgress());
        task.setModified(new Date());
        if(taskDTO.getProgress() == 100 & !task.isComplete()){
            task.setComplete(true);
            task.setInProgress(false);
        }else if(taskDTO.getProgress() > 0 && !task.isInProgress()){
            task.setInProgress(true);
            task.setComplete(false);
        }
        taskRepo.save(task);
        if(files != null && !files.isEmpty()){
            if(task.getUser().hasRole("MEMBER") &&(fileService.getFileSizeOfAttachedFiles(task.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*5){
                throw new FileSizeExceededError("File Attachments exceed limit of 5MB!");
            }else if(!task.getUser().hasRole("MEMBER") && (fileService.getFileSizeOfAttachedFiles(task.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*2){
                throw new FileSizeExceededError("File Attachments exceed limit of 2MB!");
            }
            fileService.AttachFilesToTask(task,files);
            List<FileAttachmentDTO> fileAttachments = new ArrayList<>();
            for (FileAttachment f:task.getFileAttachments()){
                FileAttachmentDTO fileAttachmentDTO = fileService.createFileAttachmentDTO(f);
                fileAttachments.add(fileAttachmentDTO);
            }
        }
    }

    /**
     * Makes a task set to be archived or unarchived meaning it will not be deleted in 30 days
     * @param id
     */
    public void setArchiveTask(Long id, boolean archive){
        Task task = getTaskById(id);
        task.setArchive(archive);
        taskRepo.save(task);
    }


    /**
     * Gets all the tasks that a user has archived
     * @param principal
     * @return
     */
    public List<Task> getUserArchivedTasks(Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        return taskRepo.findByUserAndArchiveTrue(user);
    }

    /**
     * Gets search results for archived task search which trys to find a title matching query search
     * @param search
     * @param principal
     * @return
     */
    public List<TaskDTO> getArchivedTaskSearchResult(String search,Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        List<Task> results = taskRepo.findByUserAndTitleContainingIgnoreCaseAndArchiveTrue(user,search);
        return getUserTasksDTO(results);
    }


    /**
     * Used to search through all tasks by their title based on search input
     * gets results via database query
     * @param search
     * @param principal
     * @return
     */
    public List<TaskDTO> searchAllTasks(String search, Principal principal){
        List<TaskDTO> tasks = getUserTasksDTO(taskRepo.findByTitleContainingIgnoreCaseAndUserAndArchiveFalse(search, userRepo.findByEmail(principal.getName())));
        return tasks;
    }

    /**
     * Searches for tasks within category based on search
     * gets results via databse query
     * @param search
     * @param catId
     * @return
     */
    public List<TaskDTO> searchTasksByCategory(String search, long catId){
        List<TaskDTO> tasks = getUserTasksDTO(taskRepo.findByTitleContainingIgnoreCaseAndCategoryAndArchiveFalse(search,catRepo.findById(catId).get()));
        return tasks;
    }

    /**
     * Used to check if a group entity exists by id in the database
     * @param id
     * @return
     */
    public boolean groupTaskExistsById(long id){
        return groupTaskRepo.existsById(id);
    }

    /**
     * Gets a group task from database via id
     * @param id
     * @return
     */
    public GroupTask getGroupTaskById(long id){
        return groupTaskRepo.findById(id).get();
    }

    public boolean isGroupTaskOwner(Long id, Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        GroupTask task = getGroupTaskById(id);
        return user.equals(task.getOwner());
    }


    /**
     * Used to check if a logged in user is a part of a group task
     * @param id
     * @param principal
     * @return
     */
    public boolean isInGroupTask(Long id, Principal principal){
        GroupTask task = getGroupTaskById(id);
        return task.getUsers().contains(userRepo.findByEmail(principal.getName()));
    }

    /**
     * Handles the creation of a group task by using DTO information and uses
     * file service to get all files attached to it
     * @param groupTaskDTO
     * @param files
     * @throws IOException
     */
    public void createGroupTask(GroupTaskDTO groupTaskDTO, List<MultipartFile> files) throws IOException {
        MyUser owner = userRepo.findById(groupTaskDTO.getUser()).get();
        List<MyUser> usersOnTask = new ArrayList<>();
        usersOnTask.add(owner);
        for (UserDTO userDTO: groupTaskDTO.getUsers()) {
            MyUser user = userRepo.findById(userDTO.getId()).get();
            usersOnTask.add(user);
        }
        GroupTask task = new GroupTask(groupTaskDTO.getTitle(), groupTaskDTO.isImportant(), groupTaskDTO.isUrgent(), groupTaskDTO.getWorkload(), groupTaskDTO.getMood(), groupTaskDTO.getDescription(), groupTaskDTO.getStartDate(),groupTaskDTO.getEndDate(), groupTaskDTO.getProgress(), groupTaskDTO.getModified(), groupTaskDTO.isComplete(), groupTaskDTO.isInProgress(), owner,usersOnTask);
        groupTaskRepo.save(task);

        if(files != null && !files.isEmpty()){
            if(fileService.getFilesSize(files) > (1024*1024)*5){
                throw new FileSizeExceededError("File Attachments exceed limit of 5MB!");
            }
            fileService.AttachFilesToGroupTask(task,files);

        }
    }

    /**
     * Creates a group task DTO which is used to be sent via api for get requests
     * @param task
     * @return
     */
    public GroupTaskDTO createGroupTaskDTO(GroupTask task){
        GroupTaskDTO groupTaskDTO = new GroupTaskDTO(task.getId(),task.getOwner().getId(),task.getTitle(),task.isImportant(),task.isUrgent(),task.getWorkload(),task.getMood(),task.getDescription(),task.getStartDate(),task.getEndDate(),task.getProgress(),task.getCreated(),task.getModified(),task.isComplete(),task.isInProgress(), task.getOwner().getUsername());
        groupTaskDTO.setUsers(userService.getUsersDTO(task.getUsers()));
        if(task.getFinished() != null){
            groupTaskDTO.setFinished(task.getFinished());
        }
        if(!task.getFileAttachments().isEmpty()){
            for(FileAttachment f: task.getFileAttachments()){
                FileAttachmentDTO fileAttachmentDTO = fileService.createFileAttachmentDTO(f);
                groupTaskDTO.getFileAttachments().add(fileAttachmentDTO);
            }
        }
        return groupTaskDTO;
    }

    /**
     * Gets a logged in users group tasks
     * @param principal
     * @return
     */
    public List<GroupTask> getUserGroupTasks(Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        return user.getGroupTasks();
    }

    /**
     * Gets a list of group tasks DTO
     * @param groupTasks
     * @return
     */
    public List<GroupTaskDTO> getUserGroupTasksDTO(List<GroupTask> groupTasks){
        List<GroupTaskDTO> groupTaskDTOS = new ArrayList<>();
        for (GroupTask task: groupTasks){
            GroupTaskDTO groupTaskDTO = createGroupTaskDTO(task);
            groupTaskDTOS.add(groupTaskDTO);
        }
        return groupTaskDTOS;
    }

    /**
     * Deletes a group chat via id and for every user removes
     * the task from their list
     * @param id
     */
    public void deleteGroupTaskById(long id){
        GroupTask task = groupTaskRepo.findById(id).get();
        List<FileAttachment> fileAttachments = task.getFileAttachments();
        for (FileAttachment fileAttachment : fileAttachments) {
            fileService.deleteFileAttachment(fileAttachment);
        }
        MyUser owner = task.getOwner();
        userRepo.save(owner);

        for (MyUser user: task.getUsers()){
            user.getGroupTasks().remove(task);
            userRepo.save(user);
        }
        groupTaskRepo.delete(task);
    }

    /**
     * Gets group tasks based on search and returns a List<GroupTaskDTO> since it's done via api
     * @param search
     * @param principal
     * @return
     */
    public List<GroupTaskDTO> searchGroupTasks(String search, Principal principal){
        List<GroupTask> results = groupTaskRepo.findByTitleContainingIgnoreCaseAndUsers(search,userRepo.findByEmail(principal.getName()));
        return getUserGroupTasksDTO(results);
    }

    /**
     * Handles the editing a user has done on a group task which will update all fields
     * if users have been remove they will be removed from the chat (if it exists) and if a user
     * has been added then they are added to the group task (and group chat if it exists)
     * @param groupTaskDTO
     * @param files
     * @throws IOException
     */
    public void updateGroupTask(GroupTaskDTO groupTaskDTO, List<MultipartFile> files) throws IOException {
        GroupTask task = groupTaskRepo.findById(groupTaskDTO.getId()).get();
        task.setTitle(groupTaskDTO.getTitle());
        task.setWorkload(groupTaskDTO.getWorkload());
        task.setMood(groupTaskDTO.getMood());
        task.setDescription(groupTaskDTO.getDescription());
        task.setUrgent(groupTaskDTO.isUrgent());
        task.setImportant(groupTaskDTO.isImportant());
        task.setStartDate(groupTaskDTO.getStartDate());
        task.setEndDate(groupTaskDTO.getEndDate());
        task.setProgress(groupTaskDTO.getProgress());
        task.setModified(new Date());
        if(groupTaskDTO.getProgress() == 100 & !task.isComplete()){
            task.setComplete(true);
            task.setInProgress(false);
        }else if(groupTaskDTO.getProgress() > 0 && !task.isInProgress()){
            task.setInProgress(true);
            task.setComplete(false);
        }
        GroupChat chat = task.getGroupChat();
        List<MyUser> existingUsers = task.getUsers();

        List<MyUser> usersToRemove = new ArrayList<>();
        existingUsers.removeIf(existingUser -> {
            boolean userNotInGroupTask = groupTaskDTO.getUsers().stream()
                    .noneMatch(userDTO -> userDTO.getId() == existingUser.getId());
            if (userNotInGroupTask) {
                existingUser.getGroupTasks().remove(task);
                if(chat != null){
                    existingUser.getGroupChats().remove(chat);
                    chat.getParticipants().remove(existingUser);
                    groupChatRepo.save(chat);
                }
                usersToRemove.add(existingUser);
                userRepo.save(existingUser);
            }
            return userNotInGroupTask;
        });
        task.getUsers().removeAll(usersToRemove);
        if(!groupTaskDTO.getUsers().isEmpty()){
            for (UserDTO user: groupTaskDTO.getUsers()) {
                MyUser userToAdd = userRepo.findById(user.getId()).get();
                if(!existingUsers.contains(userToAdd)){
                    task.getUsers().add(userToAdd);
                    if(chat != null){
                        chat.getParticipants().add(userToAdd);
                        groupChatRepo.save(chat);
                    }
                    userRepo.save(userToAdd);
                }
            }
        }
        groupTaskRepo.save(task);

        if(files != null && !files.isEmpty()){
            if((fileService.getFileSizeOfAttachedFiles(task.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*5){
                throw new FileSizeExceededError("File Attachments exceed limit of 5MB!");
            }
            fileService.AttachFilesToGroupTask(task,files);
        }
    }

    /**
     * If a user decides to leave a group task then this method will remove
     * them from the task and any group chat that may be associated to it
     * @param id
     * @param principal
     */
    public void removeUserFromGroupTask(long id, Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        GroupTask task = getGroupTaskById(id);
        user.getGroupTasks().remove(task);
        if(task.getGroupChat() != null){
            GroupChat groupChat = task.getGroupChat();
            user.getGroupChats().remove(groupChat);
            groupChat.getParticipants().remove(user);
            groupChatRepo.save(groupChat);
        }
        userRepo.save(user);
        task.getUsers().remove(user);
        groupTaskRepo.save(task);
    }

    /**
     * Gets a users 6 most recently completed tasks
     * @param user
     * @return
     */
    public List<Task> getTop6RecentlyCompletedTasks(MyUser user){
        return taskRepo.findTop6ByUserAndFinishedBeforeOrderByFinishedDesc(user, new Date());
    }

    /**
     * Gets number of tasks completed over the past 7 days
     * @param userId
     * @return
     */
    public List<Integer> getNumberOfTasksCompletedForWeek(Long userId){
        MyUser user = userRepo.findById(userId).get();
        Date currentDate = new Date();
        List<Date> dateRange = utils.getDateRange(currentDate);
        List<Integer> taskCounts = new ArrayList<>();
        dateRange.forEach((date) -> {
            List<Task> tasks = user.getTasks().stream().filter(task -> {
                Date finishedDate = task.getFinished();
                if (finishedDate != null) {
                    return utils.getStartDate(finishedDate).equals(date);
                }
                return false;
            }).collect(Collectors.toList());
            taskCounts.add(tasks.size());
        });
        return taskCounts;
    }

    /**
     * Gets number of group tasks completed over the past 7 days
     * @param userId
     * @return
     */
    public List<Integer> getNumberOfGroupTasksCompletedOverWeek(Long userId){
        MyUser user = userService.getUserById(userId);
        Date currentDate = new Date();
        List<Date> dateRange = utils.getDateRange(currentDate);
        List<Integer> taskCounts = new ArrayList<>();
        dateRange.forEach((date) -> {
            List<GroupTask> tasks = user.getGroupTasks().stream().filter(task -> {
                Date finishedDate = task.getFinished();
                if (finishedDate != null) {
                    return utils.getStartDate(finishedDate).equals(date);
                }
                return false;
            }).collect(Collectors.toList());
            taskCounts.add(tasks.size());
        });
        return taskCounts;

    }

    /**
     * Gets group tasks that are available for the user to create a chat for
     * @param query
     * @param principal
     * @return
     */
    public List<GroupTaskDTO> getGroupTasksAvailableForChat(String query,Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        List<GroupTask> results = groupTaskRepo.findByGroupChatIsNullAndCompleteFalseAndTitleContainingIgnoreCaseAndUsers(query,user);
        return getUserGroupTasksDTO(results);
    }

    /**
     * If a user on calendar moves or resizes a task it will change the dates
     * @param id
     * @param start
     * @param end
     */
    public void updateTaskDates(Long id,Date start, Date end){
        Task task = getTaskById(id);
        task.setStartDate(start);
        task.setEndDate(end);
        taskRepo.save(task);
    }

    /**
     * If a user on calendar moves or resizes a group task it will change the dates
     * @param id
     * @param start
     * @param end
     */
    public void updateGroupTaskDates(Long id, Date start, Date end){
        GroupTask task = getGroupTaskById(id);
        task.setStartDate(start);
        task.setEndDate(end);
        groupTaskRepo.save(task);
    }

    /**
     * Helps get suggest tasks a user should complete based on their mood and the workload of task and completion date.
     * @param user
     * @return
     */
    public List<Task> getSuggestedTasks(MyUser user){
        List<Task> suggestedTasks = user.getTasks().stream().filter((task) -> utils.shouldSuggestTask(task.isComplete(),user, task.getMood(), task.getWorkload(), task.getStartDate(),task.getEndDate())).toList();
        List<Task> suggestedTasksCopy = new ArrayList<>(suggestedTasks);
        suggestedTasksCopy.sort((t1, t2) -> Integer.compare(utils.getPriority(t2.isImportant(),t2.isUrgent()), utils.getPriority(t1.isImportant(),t1.isUrgent())));
        return suggestedTasks;
    }

    /**
     * Same as the task suggestion method just for group tasks
     * @param user
     * @return
     */
    public List<GroupTask> getSuggestedGroupTask(MyUser user){
        List<GroupTask> suggestedTasks = user.getGroupTasks().stream().filter((task) -> utils.shouldSuggestTask(task.isComplete(),user, task.getMood(), task.getWorkload(), task.getStartDate(),task.getEndDate())).toList();
        List<GroupTask> suggestedTasksCopy = new ArrayList<>(suggestedTasks);
        suggestedTasksCopy.sort((t1, t2) -> Integer.compare(utils.getPriority(t2.isImportant(),t2.isUrgent()), utils.getPriority(t1.isImportant(),t1.isUrgent())));
        return suggestedTasks;
    }

    /**
     * Gets all important/urgent tasks for the current date
     * @param user
     * @return
     */
    public List<Task> getUrgentImportantTaskForToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return taskRepo.findByUserAndUrgentIsTrueAndImportantIsTrueAndCompleteFalseAndStartDateBetween(user, startOfDay,endOfDay);
    }

    /**
     * Gets all important/urgent group tasks for the current date
     * @param user
     * @return
     */
    public List<GroupTask> getUrgentImportantGroupTasksForToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return groupTaskRepo.findByUsersAndUrgentIsTrueAndImportantIsTrueAndCompleteFalseAndStartDateBetween(user,startOfDay, endOfDay);
    }

    /**
     * Gets all the users task in progress
     * @param user
     * @return
     */
    public List<Task> getTasksInProgress(MyUser user){
        return taskRepo.findByUserAndInProgressTrue(user);
    }

    /**
     * Gets all the users group tasks in progress
     * @param user
     * @return
     */
    public List<GroupTask> getGroupTasksInProgress(MyUser user){
        return groupTaskRepo.findByUsersAndInProgressTrue(user);
    }

    /**
     * Gets tasks that have progress of 70%+
     * @param user
     * @return
     */
    public List<Task> getTasksCloseToCompletion(MyUser user){
        return taskRepo.findByUserAndInProgressTrueAndProgressGreaterThanEqual(user,70);
    }

    /**
     * Gets group tasks that have progress of 70%+
     * @param user
     * @return
     */
    public List<GroupTask> getGroupTasksCloseToCompletion(MyUser user){
        return groupTaskRepo.findByUsersAndInProgressTrueAndProgressGreaterThanEqual(user,70);
    }

    /**
     * Gets tasks a user has completed today
     * @param user
     * @return
     */
    public List<Task> getTasksCompletedToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return taskRepo.findByUserAndCompleteTrueAndFinishedBetween(user,startOfDay,endOfDay);
    }

    /**
     * Gets group tasks a user has completed today
     * @param user
     * @return
     */
    public List<GroupTask> getGroupTasksCompletedToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return groupTaskRepo.findByUsersAndCompleteTrueAndFinishedBetween(user,startOfDay,endOfDay);
    }

    /**
     * Gets tasks for user to complete today
     * @param user
     * @return
     */
    public List<Task> getTasksToCompleteToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return taskRepo.findByUserAndEndDateBetween(user,startOfDay,endOfDay);
    }

    /**
     * Gets group tasks for user to complete today
     * @param user
     * @return
     */
    public List<GroupTask> getGroupTasksToCompleteToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return groupTaskRepo.findByUsersAndEndDateBetween(user,startOfDay,endOfDay);
    }

    /**
     * Gets tasks need to be completed today and have not been started
     * @param user
     * @return
     */
    public List<Task> getTasksNotStartedToStartToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return taskRepo.findByUserAndStartDateBetweenAndCompleteFalseAndInProgressFalse(user,startOfDay,endOfDay);
    }

    /**
     * Gets group tasks need to be completed today and have not been started
     * @param user
     * @return
     */
    public List<GroupTask> getGroupTasksNotStartedToStartToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return groupTaskRepo.findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(user,startOfDay, endOfDay);
    }

    /**
     * Gets tasks in progress to be completed today
     * @param user
     * @return
     */
    public List<Task> getTasksInProgressForToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return taskRepo.findByUserAndEndDateBetweenAndInProgressTrue(user,startOfDay,endOfDay);
    }

    /**
     * Gets assignment tasks in progress to be completed today
     * @param user
     * @return
     */
    public List<GroupTask> getGroupTasksInProgressForToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return groupTaskRepo.findByUsersAndEndDateBetweenAndInProgressTrue(user,startOfDay,endOfDay);
    }

    /**
     * Gets tasks meant to be completed that are not started
     * @param user
     * @return
     */
    public List<Task> getTasksNotStartedAndForToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return taskRepo.findByUserAndEndDateBetweenAndCompleteFalseAndInProgressFalse(user,startOfDay,endOfDay);
    }

    /**
     * Gets group tasks meant to be completed that are not started
     * @param user
     * @return
     */
    public List<GroupTask> getGroupTasksNotStartedAndForToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return groupTaskRepo.findByUsersAndEndDateBetweenAndCompleteFalseAndInProgressFalse(user,startOfDay,endOfDay);
    }

    /**
     * Updates the status of a task to be either complete/in progress or not started
     * @param id
     * @param inProgress
     * @param complete
     */
    public void setTaskStatus(long id, boolean inProgress, boolean complete){
        Task task = getTaskById(id);
        if(complete){
            task.setFinished(new Date());
            task.setProgress(100);
        }else if(!inProgress && !complete){
            task.setFinished(null);
            task.setProgress(0);
        }else if(inProgress && !complete && task.getProgress() == 100){
            task.setFinished(null);
            task.setProgress(0);
        }
        task.setInProgress(inProgress);
        task.setComplete(complete);
        taskRepo.save(task);
    }

    /**
     * Updates the status of a group task to be either complete/in progress or not started
     * @param id
     * @param inProgress
     * @param complete
     */
    public void setGroupTaskStatus(long id, boolean inProgress, boolean complete){
        GroupTask task = getGroupTaskById(id);
        if(complete){
            task.setFinished(new Date());
            task.setProgress(100);
        }else if(!inProgress && !complete){
            task.setFinished(null);
            task.setProgress(0);
        }else if(inProgress && !complete && task.getProgress() == 100){
            task.setFinished(null);
            task.setProgress(0);
        }
        task.setInProgress(inProgress);
        task.setComplete(complete);
        groupTaskRepo.save(task);
    }

    /**
     * Gets tasks that need to be started within 30 minutes.
     * A part of the notification system
     * @param user
     * @return TaskDTOs to be included in notification
     */
    public List<TaskDTO> getTasksToStartIn30Minutes(MyUser user){
        Date currentDate = new Date();
        Date thirtyMinutesFromNow = utils.get30MinutesFromNow(currentDate);
        return getUserTasksDTO(taskRepo.findByUserAndStartDateBetweenAndCompleteFalseAndInProgressFalse(user, currentDate,thirtyMinutesFromNow));
    }

    /**
     * Gets group tasks needed to be started within 30 minutes.
     * A part of the notification system
     * @param user
     * @return
     */
    public List<GroupTaskDTO> getGroupTasksToStartIn30Minutes(MyUser user){
        Date currentDate = new Date();
        Date thirtyMinutesFromNow = utils.get30MinutesFromNow(currentDate);
        return getUserGroupTasksDTO(groupTaskRepo.findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(user,currentDate,thirtyMinutesFromNow));
    }

}