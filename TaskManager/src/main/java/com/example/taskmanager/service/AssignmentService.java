package com.example.taskmanager.service;

import com.example.taskmanager.component.CommonUtils;
import com.example.taskmanager.component.FileSizeExceededError;
import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.Assignment.DTO.AssignmentDTO;
import com.example.taskmanager.domain.Assignment.DTO.AssignmentTaskDTO;
import com.example.taskmanager.domain.Events.StudySession;
import com.example.taskmanager.domain.File.DTO.FileAttachmentDTO;
import com.example.taskmanager.domain.File.FileAttachment;
import com.example.taskmanager.domain.Messaging.GroupChat;
import com.example.taskmanager.domain.User.DTO.UserDTO;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.Assignment.AssignmentRepository;
import com.example.taskmanager.repo.Assignment.AssignmentTaskRepository;
import com.example.taskmanager.repo.Messaging.GroupChatRepository;
import com.example.taskmanager.repo.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.sort;

@Service
public class AssignmentService {

    @Autowired
    private CommonUtils utils;

    @Autowired
    private AssignmentRepository assignmentRepo;

    @Autowired
    private AssignmentTaskRepository assignmentTaskRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private GroupChatRepository groupChatRepo;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventsService eventsService;

    /**
     * Checks if assignment exists via id
     * @param id
     * @return
     */
    public boolean assignmentExistsById(long id){
        return assignmentRepo.existsById(id);
    }

    /**
     * Creates an assignment DTO from provided assignment object
     * @param assignment
     * @return
     */
    public AssignmentDTO createAssignmentDTO(Assignment assignment){
        AssignmentDTO assignmentDTO = new AssignmentDTO(assignment.getId(), assignment.getOwner().getId(),assignment.getOwner().getUsername(),assignment.getTitle(), assignment.getCourse(), assignment.getDescription(), assignment.getStartDate(),assignment.getEndDate(),assignment.getCreated(),assignment.isInProgress(),assignment.isComplete(), assignment.getProgress(), assignment.isImportant(),assignment.isUrgent());
        if(assignment.getFinished() != null){
            assignmentDTO.setFinished(assignment.getFinished());
        }
        if(!assignment.getFileAttachments().isEmpty()){
            for(FileAttachment f: assignment.getFileAttachments()){
                FileAttachmentDTO fileAttachmentDTO = fileService.createFileAttachmentDTO(f);
                assignmentDTO.getFileAttachments().add(fileAttachmentDTO);
            }
        }
        if(!assignment.getUsers().isEmpty()){
            assignmentDTO.setUsers(userService.getUsersDTO(assignment.getUsers()));
        }

        if(!assignment.getAssignmentTasks().isEmpty()){
            for (AssignmentTask task: assignment.getAssignmentTasks()){
                assignmentDTO.getTasks().add(createAssignmentTaskDTO(task));
            }
        }

        if(!assignment.getStudySessions().isEmpty()){
            for (StudySession session: assignment.getStudySessions()) {
                assignmentDTO.getSessions().add(eventsService.createStudySessionDTO(session));
            }}
        return assignmentDTO;
    }

    /**
     * Gets a list of assignment DTO's for user
     * @param assignments
     * @return
     */
    public List<AssignmentDTO> getUserAssignmentsDTO(List<Assignment> assignments){
        List<AssignmentDTO> assignmentDTOS = new ArrayList<>();
        assignments.forEach((assignment) -> {
            assignmentDTOS.add(createAssignmentDTO(assignment));
        });
        return assignmentDTOS;
    }

    /**
     * Creates an assignment task DTO based on the task object provided
     * @param task
     * @return
     */
    public AssignmentTaskDTO createAssignmentTaskDTO(AssignmentTask task){
        AssignmentTaskDTO taskDTO = new AssignmentTaskDTO(task.getId(),task.getOwner().getId(),task.getTitle(), task.isImportant(), task.isUrgent(), task.getWorkload(),task.getMood(),task.getDescription(),task.getStartDate(),task.getEndDate(),task.getProgress(),task.getCreated(),task.getModified(),task.isComplete(), task.isInProgress(), task.getAssignment().getId(), task.getAssignment().getTitle());
        if(task.getFinished() != null){
            taskDTO.setFinished(task.getFinished());
        }

        if(!task.getFileAttachments().isEmpty()){
            for(FileAttachment f: task.getFileAttachments()){
                FileAttachmentDTO fileAttachmentDTO = fileService.createFileAttachmentDTO(f);
                taskDTO.getFileAttachments().add(fileAttachmentDTO);
            }
        }
        taskDTO.setUsers(userService.getUsersDTO(task.getUsers()));
        return taskDTO;
    }

    /**
     * Creates the a list of assignment tasks DTO
     * @param tasks
     * @return
     */
    public List<AssignmentTaskDTO> createTaskDTOS(List<AssignmentTask> tasks){
        List<AssignmentTaskDTO> taskDTOS = new ArrayList<>();
        for (AssignmentTask task: tasks){
            taskDTOS.add(createAssignmentTaskDTO(task));
        }
        return taskDTOS;
    }

    /**
     * Creates assignment task based on the DTO given
     * @param task
     * @param files
     * @return
     * @throws IOException
     */
    public List<AssignmentTaskDTO> createAssignmentTask(AssignmentTaskDTO task, List<MultipartFile> files) throws IOException {
        Assignment assignment = assignmentRepo.findById(task.getAssignment()).get();
        MyUser owner = userRepo.findById(task.getOwner()).get();
        AssignmentTask assignmentTask = new AssignmentTask(owner,task.getTitle(), task.isImportant(), task.isUrgent(),task.getWorkload() ,task.getMood(),task.getDescription(),task.getStartDate(),task.getEndDate(),task.getProgress(),task.getModified(),task.isComplete(), task.isInProgress(), assignment);
        List<MyUser> usersOnTask = new ArrayList<>();
        usersOnTask.add(owner);
        if(owner.hasRole("MEMBER")){
            if(!task.getUsers().isEmpty()){
                for(UserDTO userDTO: task.getUsers()){
                    MyUser user = userRepo.findById(userDTO.getId()).get();
                    if(user != owner){
                        usersOnTask.add(user);
                    }
                }
            }
        }
        assignmentTask.setUsers(usersOnTask);
        assignmentTask = assignmentTaskRepo.save(assignmentTask);
        for (MyUser user: usersOnTask) {
            user.getAssignmentTasks().add(assignmentTask);
            userRepo.save(user);
        }
        if(files != null && !files.isEmpty()){
            if(owner.hasRole("MEMBER") &&(fileService.getFileSizeOfAttachedFiles(assignment.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*5){
                throw new FileSizeExceededError("File Attachments exceed limit of 5MB!");
            }else if(!owner.hasRole("MEMBER") && (fileService.getFileSizeOfAttachedFiles(assignment.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*2){
                throw new FileSizeExceededError("File Attachments exceed limit of 2MB!");
            }
            fileService.AttachFilesToAssignmentTask(assignmentTask,files);
        }
        assignment.getAssignmentTasks().add(assignmentTask);
        assignmentRepo.save(assignment);
        return createTaskDTOS(assignment.getAssignmentTasks());

    }

    /**
     * Creates an assigment based of details given in assignmentDTO
     * @param assignmentDTO
     * @param files
     * @throws IOException
     */
    public void createAssignment(AssignmentDTO assignmentDTO, List<MultipartFile> files) throws IOException {
        MyUser owner = userRepo.findById(assignmentDTO.getUser()).get();
        if(!owner.hasRole("MEMBER") && owner.getAssignments().size() >= 6){
            return;
        }
        Assignment assignment = new Assignment(owner, assignmentDTO.getTitle(), assignmentDTO.getCourse(), assignmentDTO.getDescription(), assignmentDTO.getStartDate(),assignmentDTO.getEndDate(),assignmentDTO.isInProgress(),assignmentDTO.isComplete(),assignmentDTO.getProgress(),assignmentDTO.isImportant(),assignmentDTO.isUrgent());
        List<MyUser> usersOnAssignment = new ArrayList<>();
        usersOnAssignment.add(owner);
        if(!assignmentDTO.getUsers().isEmpty()){
            for (UserDTO userDTO: assignmentDTO.getUsers()){
                MyUser user = userRepo.findById(userDTO.getId()).get();
                usersOnAssignment.add(user);
            }
        }
        assignment.setUsers(usersOnAssignment);
        assignmentRepo.save(assignment);
        owner.getAssignments().add(assignment);
        userRepo.save(owner);
        if(owner.hasRole("MEMBER")){
            for (MyUser user: usersOnAssignment) {
                user.getAssignments().add(assignment);
                userRepo.save(user);
            }
        }

        if(files != null && !files.isEmpty()){
            if(owner.hasRole("MEMBER") &&(fileService.getFileSizeOfAttachedFiles(assignment.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*5){
                throw new FileSizeExceededError("File Attachments exceed limit of 5MB!");
            }else if(!owner.hasRole("MEMBER") && (fileService.getFileSizeOfAttachedFiles(assignment.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*2){
                throw new FileSizeExceededError("File Attachments exceed limit of 2MB!");
            }
            fileService.AttachFilesToAssignment(assignment,files);
        }
    }

    /**
     * Gets an assignment via its id
     * @param id
     * @return
     */
    public Assignment getAssignmentById(long id){
        return assignmentRepo.findById(id).get();
    }

    /**
     * Gets all the assignments that a user has
     * @param principal
     * @return
     */
    public List<Assignment> getUserAssignments(Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        List<Assignment> assignments = user.getAssignments();
        Collections.reverse(assignments);
        return assignments;
    }

    /**
     * Gets list of userDTOs of user on an assignment
     * @param assignmentId
     * @return
     */
    public List<UserDTO> getUsersDTOOnAssignment(long assignmentId){
        Assignment assignment = getAssignmentById(assignmentId);
        return userService.getUsersDTO(assignment.getUsers());
    }

    /**
     * Gets assignments via title matching user search query
     * @param search
     * @param principal
     * @return
     */
    public List<AssignmentDTO> getAssignmentsFromSearch(String search, Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        List<Assignment> assignments = assignmentRepo.findByTitleContainingIgnoreCaseAndUsers(search,user);
        Collections.reverse(assignments);
        return getUserAssignmentsDTO(assignments);
    }

    /**
     * Deletes and assignment via its id
     * @param id
     */
    public void deleteAssignmentById(long id){
        Assignment assignment = assignmentRepo.findById(id).get();
        for (FileAttachment file: assignment.getFileAttachments()) {
            fileService.deleteFileAttachment(file);
        }
        assignment.getUsers().forEach((user) -> {
            user.getStudySessions().removeAll(assignment.getStudySessions());
        });
        assignmentRepo.delete(assignment);
    }

    /**
     * Checks if a user is on an assigment
     * @param id
     * @param principal
     * @return
     */

    public boolean isOnAssignment(long id,Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        return getAssignmentById(id).getUsers().contains(user);
    }

    /**
     * Checks if th user is the owner of an assignment
     * @param id
     * @param principal
     * @return
     */

    public boolean isOwnerOfAssignment(long id,Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        return getAssignmentById(id).getOwner() == user;
    }

    /**
     * Updates detials about an assignment based of the assignmentDTO provided
     * @param assignmentDTO
     * @param files
     * @throws IOException
     */
    public void updateAssignment(AssignmentDTO assignmentDTO, List<MultipartFile> files) throws IOException {
        Assignment assignment = getAssignmentById(assignmentDTO.getId());
        assignment.setTitle(assignmentDTO.getTitle());
        assignment.setCourse(assignmentDTO.getCourse());
        assignment.setDescription(assignmentDTO.getDescription());
        assignment.setStartDate(assignmentDTO.getStartDate());
        assignment.setEndDate(assignmentDTO.getEndDate());
        assignment.setProgress(assignmentDTO.getProgress());
        assignment.setImportant(assignmentDTO.isImportant());
        assignmentDTO.setUrgent(assignmentDTO.isUrgent());
        if(assignmentDTO.getProgress() == 100 && !assignment.isComplete()){
            assignment.setInProgress(false);
            assignment.setComplete(true);
        }else if(assignmentDTO.getProgress() > 0 && !assignment.isInProgress()){
            assignment.setInProgress(true);
            assignment.setComplete(false);
        }
        if(assignment.getOwner().hasRole("MEMBER")){
            GroupChat chat = assignment.getGroupChat();
            List<MyUser> existingUsers = assignment.getUsers();

            List<MyUser> usersToRemove = new ArrayList<>();
            existingUsers.removeIf(existingUser -> {
                boolean userNotInAssignment = assignmentDTO.getUsers().stream()
                        .noneMatch(userDTO -> userDTO.getId() == existingUser.getId());
                if(userNotInAssignment){
                    existingUser.getAssignments().remove(assignment);
                    if(chat != null){
                        existingUser.getGroupChats().remove(chat);
                        chat.getParticipants().remove(existingUser);
                        groupChatRepo.save(chat);
                    }
                    usersToRemove.add(existingUser);
                    userRepo.save(existingUser);
                }
                return userNotInAssignment;
            });
            assignment.getUsers().removeAll(usersToRemove);
            if(!assignmentDTO.getUsers().isEmpty()){
                assignmentDTO.getUsers().forEach((user) -> {
                    MyUser userToAdd = userRepo.findById(user.getId()).get();
                    if(!existingUsers.contains(userToAdd)){
                        assignment.getUsers().add(userToAdd);
                        if(chat != null){
                            chat.getParticipants().add(userToAdd);
                            groupChatRepo.save(chat);
                        }
                        userToAdd.getStudySessions().addAll(assignment.getStudySessions());
                        userRepo.save(userToAdd);
                    }
                });
            }
        }
        assignmentRepo.save(assignment);

        if(files != null && !files.isEmpty()){
            if(assignment.getOwner().hasRole("MEMBER") &&(fileService.getFileSizeOfAttachedFiles(assignment.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*5){
                throw new FileSizeExceededError("File Attachments exceed limit of 5MB!");
            }else if(!assignment.getOwner().hasRole("MEMBER") && (fileService.getFileSizeOfAttachedFiles(assignment.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*2){
                throw new FileSizeExceededError("File Attachments exceed limit of 2MB!");
            }
            fileService.AttachFilesToAssignment(assignment,files);
        }
    }

    /**
     * Checks if assignment task exists via id
     * @param id
     * @return
     */
    public boolean assignmentTaskExistsById(long id){
        return assignmentTaskRepo.existsById(id);
    }

    /**
     * Checks if a user is on a assignment task
     * @param id
     * @param principal
     * @return
     */
    public boolean isOnAssignmentTask(long id,Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        return getAssignmentTaskById(id).getUsers().contains(user);
    }

    /**
     * Checks if the current logged in user is the owner of an assignment task
     * @param id
     * @param principal
     * @return
     */
    public boolean isOwnerOfAssignmentTask(long id, Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        return getAssignmentTaskById(id).getOwner() == user;
    }

    /**
     * Gets assignment task via id
     * @param id
     * @return
     */
    public AssignmentTask getAssignmentTaskById(long id){
        return assignmentTaskRepo.findById(id).get();
    }

    /**
     * Deletes an assignment task via id
     * @param id
     */
    public void deleteAssignmentTask(long id){
        AssignmentTask task = assignmentTaskRepo.findById(id).get();
        task.getFileAttachments().forEach((file) -> {
            fileService.deleteFileAttachment(file);
        });
        assignmentTaskRepo.delete(task);
    }

    /**
     * Updates assignment task details based on the DTO provided
     * @param taskDTO
     * @param files
     * @throws IOException
     */
    public void updateAssignmentTask(AssignmentTaskDTO taskDTO, List<MultipartFile> files) throws IOException {
        AssignmentTask task = getAssignmentTaskById(taskDTO.getId());
        task.setTitle(taskDTO.getTitle());
        task.setWorkload(taskDTO.getWorkload());
        task.setMood(taskDTO.getMood());
        task.setDescription(taskDTO.getDescription());
        task.setUrgent(taskDTO.isUrgent());
        task.setImportant(taskDTO.isImportant());
        task.setStartDate(taskDTO.getStartDate());
        task.setEndDate(taskDTO.getEndDate());
        task.setProgress(taskDTO.getProgress());
        task.setModified(taskDTO.getModified());
        if(taskDTO.getProgress() > 0 && taskDTO.getProgress() != 0 && !task.isInProgress()){
            task.setInProgress(true);
        }else if(taskDTO.getProgress() == 100 && !task.isComplete()){
            task.setInProgress(false);
            task.setComplete(true);
        }
        if(task.getOwner().hasRole("MEMBER")){
            List<MyUser> existingUsers = task.getUsers();
            List<MyUser> usersToRemove = new ArrayList<>();
            existingUsers.removeIf((existingUser) -> {
                boolean userNotInTask = taskDTO.getUsers().stream()
                        .noneMatch(userDTO -> userDTO.getId() == existingUser.getId());
                if(userNotInTask){
                    existingUser.getAssignmentTasks().remove(task);
                    usersToRemove.add(existingUser);
                    userRepo.save(existingUser);
                }
                return userNotInTask;
            });
            task.getUsers().removeAll(usersToRemove);
            if(!taskDTO.getUsers().isEmpty()){
                taskDTO.getUsers().forEach((user) -> {
                    MyUser userToAdd = userRepo.findById(user.getId()).get();
                    if(!existingUsers.contains(userToAdd)){
                        task.getUsers().add(userToAdd);
                        userRepo.save(userToAdd);
                    }
                });
            }
        }
        assignmentTaskRepo.save(task);
        if(files != null && !files.isEmpty()){
            if(task.getOwner().hasRole("MEMBER") &&(fileService.getFileSizeOfAttachedFiles(task.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*5){
                throw new FileSizeExceededError("File Attachments exceed limit of 5MB!");
            }else if(!task.getOwner().hasRole("MEMBER") && (fileService.getFileSizeOfAttachedFiles(task.getFileAttachments()) + fileService.getFilesSize(files)) > (1024*1024)*2){
                throw new FileSizeExceededError("File Attachments exceed limit of 2MB!");
            }
            fileService.AttachFilesToAssignmentTask(task,files);
        }

    }

    /**
     * Removes a user from an assignment
     * @param id
     * @param principal
     */
    public void removeUserFromAssignment(long id, Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        Assignment assignment = getAssignmentById(id);
        assignment.getAssignmentTasks().forEach((task -> {
            //If the user trying to leave is the only user then give task to owner of assignment
            if(task.getOwner() == user && task.getUsers().size() == 1){
                task.setOwner(assignment.getOwner());
                task.getUsers().add(assignment.getOwner());
                task.getUsers().remove(user);
                assignmentTaskRepo.save(task);
            //Else assign the user next in the list to be the owner of the task
            }else if(task.getOwner() == user){
                task.getUsers().remove(user);
                task.setOwner(task.getUsers().get(0));
                assignmentTaskRepo.save(task);
            }
        }));
        if(assignment.getGroupChat() != null){
            GroupChat chat = assignment.getGroupChat();
            user.getGroupChats().remove(chat);
            chat.getParticipants().remove(user);
            groupChatRepo.save(chat);
        }
        user.getStudySessions().removeAll(assignment.getStudySessions());
        user.getAssignments().remove(assignment);
        assignment.getUsers().remove(user);
        userRepo.save(user);
    }

    /**
     * Removes a user from an assignment task
     * @param id
     * @param principal
     */
    public void removeUserFromAssignmentTask(long id, Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        AssignmentTask assignmentTask = getAssignmentTaskById(id);
        user.getAssignmentTasks().remove(assignmentTask);
        assignmentTask.getUsers().remove(user);
        userRepo.save(user);
    }

    /**
     * Sets an assignments status
     * @param id
     * @param inProgress
     * @param complete
     */
    public void setAssignmentStatus(long id, boolean inProgress, boolean complete){
        Assignment assignment = getAssignmentById(id);
        if(complete){
            assignment.setFinished(new Date());
            assignment.setProgress(100);
        }else if(!inProgress && !complete){
            assignment.setFinished(null);
            assignment.setProgress(0);
        }else if(inProgress && !complete && assignment.getProgress() == 100){
            assignment.setFinished(null);
            assignment.setProgress(0);
        }
        assignment.setInProgress(inProgress);
        assignment.setComplete(complete);
        assignmentRepo.save(assignment);
    }

    /**
     * Sets an assignment tasks status
     * @param id
     * @param inProgress
     * @param complete
     */
    public void setAssignmentTaskStatus(long id, boolean inProgress, boolean complete){
        AssignmentTask assignmentTask = getAssignmentTaskById(id);
        if(complete){
            assignmentTask.setFinished(new Date());
            assignmentTask.setProgress(100);
        }else if(!inProgress && !complete){
            assignmentTask.setFinished(null);
            assignmentTask.setProgress(0);
        }else if(inProgress && !complete && assignmentTask.getProgress() == 100){
            assignmentTask.setFinished(null);
            assignmentTask.setProgress(0);
        }
        assignmentTask.setInProgress(inProgress);
        assignmentTask.setComplete(complete);
        assignmentTaskRepo.save(assignmentTask);
    }

    /**
     * Gets user assignments that are in progress
     * @param user
     * @return
     */
    public List<Assignment> getUserAssignmentsInProgress(MyUser user){
        return assignmentRepo.findByInProgressTrueAndUsers(user);
    }

    /**
     * Gets the number of tasks completed for assignments in the last 7 days
     * @param userId
     * @return
     */
    public List<Integer> getNumberOfAssignmentTasksCompletedForWeek(Long userId){
        MyUser user = userRepo.findById(userId).get();
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        List<Date> dateRange = IntStream.range(0,7).mapToObj((i) -> {
            cal.setTime(currentDate);
            cal.add(Calendar.DAY_OF_MONTH, -i);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }).collect(Collectors.toList());
        Collections.reverse(dateRange);
        List<Integer> taskCounts = new ArrayList<>();
        dateRange.forEach((date) -> {
            List<AssignmentTask> tasks = user.getAssignmentTasks().stream().filter( task -> {
                Date finishedDate = task.getFinished();
                if (finishedDate != null) {
                    cal.setTime(finishedDate);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    return cal.getTime().equals(date);
                }
                return false;
            }).collect(Collectors.toList());
            taskCounts.add(tasks.size());
        });
        return taskCounts;
    }

    /**
     * Basic implementation of suggesting tasks based on the users perceived mood and
     * the ratings they gave for the tasks
     * @param user
     * @return
     */
    public List<AssignmentTask> getSuggestedAssignmentTasks(MyUser user){
        List<AssignmentTask> suggestedTasks = user.getAssignmentTasks().stream().filter((task) -> utils.shouldSuggestTask(task.isComplete(),user, task.getMood(), task.getWorkload(), task.getStartDate(),task.getEndDate())).toList();
        //Order by importance and urgency
        List<AssignmentTask> suggestedTasksCopy = new ArrayList<>(suggestedTasks);
        suggestedTasksCopy.sort((t1, t2) -> Integer.compare(utils.getPriority(t2.isImportant(),t2.isUrgent()), utils.getPriority(t1.isImportant(),t1.isUrgent())));
        return suggestedTasks;
    }

    /**
     * Gets assignments that are due within 3 days that are not complete
     * @param user
     * @return
     */
    public List<Assignment> getAssignmentsCloseToDeadline(MyUser user){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH,3);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endDate = cal.getTime();

        List<Assignment> assignments = new ArrayList<>(assignmentRepo.findByEndDateBetweenAndUsersAndCompleteIsFalse(utils.getStartDate(new Date()),endDate,user));
        assignments.sort((a1, a2) -> Integer.compare(utils.getPriority(a2.isImportant(), a2.isUrgent()), utils.getPriority(a1.isImportant(), a1.isUrgent())));
        return assignments;
    }

    public List<AssignmentTask> getTop6RecentlyCompletedTasks(MyUser user){
        return assignmentTaskRepo.findTop6ByUsersAndFinishedBeforeOrderByFinishedDesc(user,new Date());
    }

    /**
     * Gets all assignments that are available for group chat
     * @param query
     * @param principal
     * @return
     */
    public List<AssignmentDTO> getAssignmentsAvailableForChat(String query, Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        List<Assignment> results = assignmentRepo.findByGroupChatIsNullAndCompleteFalseAndTitleContainingIgnoreCaseAndUsers(query,user);
        return getUserAssignmentsDTO(results);
    }

    /**
     * If a user on calendar moves or resizes an assignment it will change the dates
     * @param id
     * @param start
     * @param end
     */
    public void updateAssignmentDates(Long id, Date start, Date end){
        Assignment assignment = getAssignmentById(id);
        assignment.setStartDate(start);
        assignment.setEndDate(end);
        assignmentRepo.save(assignment);
    }

    /**
     * If a user on calendar moves or resizes an assignment task it will change the dates
     * @param id
     * @param start
     * @param end
     */
    public void updateAssignmentTaskDates(Long id, Date start, Date end){
        AssignmentTask task = getAssignmentTaskById(id);
        task.setStartDate(start);
        task.setEndDate(end);
        assignmentTaskRepo.save(task);
    }

    /**
     * Gets a list of assignments to start today
     * @param user
     * @return
     */
    public List<Assignment> getAssignmentsToStartToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return assignmentRepo.findByUsersAndStartDateBetweenAndInProgressFalseAndCompleteFalse(user,startOfDay,endOfDay);
    }

    /**
     * Gets assignments that are due for a user today.
     * @param user
     * @return
     */
    public List<Assignment> getAssignmentsDueToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return assignmentRepo.findByEndDateBetweenAndUsersAndCompleteIsFalse(startOfDay,endOfDay,user);
    }

    /**
     * Gets important/urgent tasks for today
     * @param user
     * @return
     */
    public List<AssignmentTask> getImportantUrgentAssignmentTasksForToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return assignmentTaskRepo.findByUsersAndUrgentIsTrueAndImportantIsTrueAndCompleteFalseAndStartDateBetween(user, startOfDay,endOfDay);
    }

    /**
     * Gets all assignment tasks in progress
     * @param user
     * @return
     */
    public List<AssignmentTask> getAssignmentTasksInProgress(MyUser user){
        return assignmentTaskRepo.findByUsersAndInProgressTrue(user);
    }

    /**
     * Gets all assignments that have 70%+
     * @param user
     * @return
     */
    public List<AssignmentTask> getAssignmentTasksClosestToCompletion(MyUser user){
        return assignmentTaskRepo.findByUsersAndInProgressTrueAndProgressGreaterThanEqual(user, 70);
    }

    /**
     * Gets assignment tasks completed today
     * @param user
     * @return
     */
    public List<AssignmentTask> getAssignmentTasksCompletedToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return assignmentTaskRepo.findByUsersAndCompleteTrueAndFinishedBetween(user,startOfDay,endOfDay);
    }

    /**
     * Gets assignment tasks for user to complete today
     * @param user
     * @return
     */
    public List<AssignmentTask> getAssignmentTasksToCompleteToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return assignmentTaskRepo.findByUsersAndEndDateBetween(user,startOfDay,endOfDay);
    }

    /**
     * Gets assignment tasks need to be completed today and have not been started
     * @param user
     * @return
     */
    public List<AssignmentTask> getAssignmentTasksToBeStartedToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return assignmentTaskRepo.findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(user,startOfDay, endOfDay);
    }

    /**
     * Gets assignment tasks in progress to be completed today
     * @param user
     * @return
     */
    public List<AssignmentTask> getAssignmentTasksInProgressForToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return assignmentTaskRepo.findByUsersAndEndDateBetweenAndInProgressTrue(user,startOfDay,endOfDay);
    }

    /**
     * Gets assignment tasks meant to be completed that are not started
     * @param user
     * @return
     */
    public List<AssignmentTask> getAssignmentTasksNotStartedAndForToday(MyUser user){
        Date startOfDay = utils.getStartDate(new Date());
        Date endOfDay = utils.getEndDate(new Date());
        return assignmentTaskRepo.findByUsersAndEndDateBetweenAndCompleteFalseAndInProgressFalse(user,startOfDay,endOfDay);
    }

    /**
     * Gets assignments needed to be started in 30 minutes
     * Part of the notification system
     * @param user
     * @return
     */
    public List<AssignmentDTO> getAssignmentToStartWithin30Minutes(MyUser user){
        Date currentDate = new Date();
        Date thirtyMinutesFromNow = utils.get30MinutesFromNow(currentDate);
        return getUserAssignmentsDTO(assignmentRepo.findByUsersAndStartDateBetweenAndInProgressFalseAndCompleteFalse(user,currentDate,thirtyMinutesFromNow));
    }

    /**
     * Gets Assignments Tasks needed to be started in 30 minutes
     * Part of the notification system
     * @param user
     * @return
     */
    public List<AssignmentTaskDTO> getAssignmentTasksToStartWithin30Minutes(MyUser user){
        Date currentDate = new Date();
        Date thirtyMinutesFromNow = utils.get30MinutesFromNow(currentDate);
        return createTaskDTOS(assignmentTaskRepo.findByUsersAndStartDateBetweenAndCompleteFalseAndInProgressFalse(user,currentDate,thirtyMinutesFromNow));
    }
}
