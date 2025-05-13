package com.example.taskmanager.api.User;

import com.example.taskmanager.domain.User.DTO.NotificationDTO;
import com.example.taskmanager.domain.User.DTO.UserDTO;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.service.AssignmentService;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.ast.Assign;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserAPI {
    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private NotificationService notificationService;

    //Used to be sent for home page pie chart
    private record TaskCompletionInfo(int numForToday,int inProgress, int completed, int notStarted){}

    /**
     * Gets the current user logged in list of friends
     * @param principal
     * @return List of friendDTO which contains some information about the users
     * friends
     */
    @GetMapping("/friends")
    public ResponseEntity<?> getUserFriends(Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        List<MyUser> friends = userService.getUserFriends(user);
        if(!friends.isEmpty()){
            List<UserDTO> friendsDTO = userService.getUsersDTO(friends);
            return new ResponseEntity<>(friendsDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * In a search bar filters out friends and finds the one matching
     * search input based on username
     * @param search
     * @param principal
     * @return A list of friendsDTO if there are results matching search
     * input or returns a status code of 404 if there's no results matching
     * input
     */
    @GetMapping("/search/friends")
    public ResponseEntity<?> searchForUserFriends(@RequestParam String search, Principal principal){
        List<MyUser> friends = userService.searchForUserFriends(principal,search);
        if(!friends.isEmpty()){
            List<UserDTO> friendsDTO = userService.getUsersDTO(friends);
            return new ResponseEntity<>(friendsDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Used to create a friend request
     * @param userId
     * @param principal
     * @return
     */
    @PostMapping("/send/friend-request/{userId}")
    public ResponseEntity<?> sendFriendRequest(@PathVariable Long userId, Principal principal){
        if(userService.hasUserSentFriendRequestToUserOrReceived(userService.getUserByEmail(principal.getName()),userService.getUserById(userId))){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(userService.sendFriendRequest(principal,userId),HttpStatus.CREATED);
    }

    /**
     * If a user denys or cancels a request sent then delete it
     * @param id
     * @param principal
     * @return
     */
    @DeleteMapping("/friend-request/{id}")
    public ResponseEntity<?> deleteFriendRequest(@PathVariable Long id, Principal principal){
        if(!userService.friendRequestExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!userService.userHasFriendRequest(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        userService.deleteFriendRequest(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * It's a delete mapping as the friend request will vanish after it is accepted
     * @param id
     * @param principal
     * @return
     */
    @DeleteMapping("/friend-request/accept/{id}")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long id,Principal principal){
        if(!userService.friendRequestExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!userService.userHasFriendRequest(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        userService.acceptFriendRequest(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * If a user doesn't want to be friends with anyone anymore then this will remove them as
     * friends
     * @param userId
     * @param principal
     * @return
     */
    @DeleteMapping("/friends/remove/{userId}")
    public ResponseEntity<?> removeUserFromFriendsList(@PathVariable Long userId, Principal principal){
        userService.removeUserFromFriendsList(principal,userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Api call to change user profile fields by calling service method
     * @param userDTO
     * @param pfp
     * @return
     * @throws IOException
     */
    @PatchMapping("/edit/save-changes")
    public ResponseEntity<?> saveProfileChanges(@RequestPart UserDTO userDTO, @RequestPart(required = false) MultipartFile pfp, Principal principal) throws IOException {
        MyUser user = userService.getUserByEmail(principal.getName());
        if(!userService.existsByUsername(userDTO.getUsername()) || user.getUsername().equals(userDTO.getUsername())){
            userService.saveUserProfileChanges(userDTO,pfp);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    /**
     * Gets a users profile picture based on their id
     * @param id
     * @return
     */
    @GetMapping("{id}/pfp")
    public ResponseEntity<?> getUserProfilePicture(@PathVariable Long id){
        MyUser user = userService.getUserById(id);
        return new ResponseEntity<>(user.getBase64ProfilePic(),HttpStatus.OK);
    }

    /**
     * Used to update a users mood
     * @param principal
     * @param rating
     * @return
     */
    @PatchMapping("/update/mood-rating")
    public ResponseEntity<?> updateUserMoodRating(Principal principal, @RequestParam short rating){
        userService.updateCurrentUserMood(principal,rating);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Gets information for the graph about users mood rating for the passed 7 days
     * @param principal
     * @return
     */
    @GetMapping("/mood-ratings")
    public ResponseEntity<?> getUserMoodRatingsPast7days(Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        return new ResponseEntity<>(user.getMoodRatingsPast7Days(),HttpStatus.OK);
    }

    /**
     * Api used to update the user pie chart to show progress on tasks for today
     * @param principal
     * @return
     */
    @GetMapping("/task-completion-info")
    public ResponseEntity<?> getUserTaskCompletionInfo(Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        int numToComp = taskService.getTasksToCompleteToday(user).size()+taskService.getGroupTasksCompletedToday(user).size()+assignmentService.getAssignmentTasksToCompleteToday(user).size();
        int inProgress = taskService.getTasksInProgressForToday(user).size()+taskService.getGroupTasksInProgressForToday(user).size()+assignmentService.getAssignmentTasksInProgressForToday(user).size();
        int completed = taskService.getTasksCompletedToday(user).size()+taskService.getGroupTasksCompletedToday(user).size()+assignmentService.getAssignmentTasksCompletedToday(user).size();
        int notStarted = taskService.getTasksNotStartedAndForToday(user).size()+taskService.getGroupTasksNotStartedAndForToday(user).size()+assignmentService.getAssignmentTasksNotStartedAndForToday(user).size();
        TaskCompletionInfo info = new TaskCompletionInfo(numToComp,inProgress,completed,notStarted);
        return new ResponseEntity<>(info,HttpStatus.OK);
    }

    /**
     * Endpoint for getting notifications will return notifications DTO
     * @param principal
     * @return 200 status if there's any notifications found or 204 if there isn't any
     */
    @PostMapping("/notifications")
    public ResponseEntity<?> getUserNotifications(Principal principal){
        NotificationDTO notifications = notificationService.getUserNotifications(principal);
        if(notifications.getTasks().isEmpty() && notifications.getGroupTasks().isEmpty()
            && notifications.getAssignments().isEmpty() && notifications.getAssignmentTasks().isEmpty()
        && notifications.getStudySessions().isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(notifications,HttpStatus.OK);
    }
}
