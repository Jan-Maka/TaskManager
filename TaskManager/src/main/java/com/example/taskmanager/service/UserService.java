package com.example.taskmanager.service;

import com.example.taskmanager.domain.Messaging.Conversation;
import com.example.taskmanager.domain.User.*;
import com.example.taskmanager.domain.User.DTO.NotificationDTO;
import com.example.taskmanager.domain.User.DTO.UserDTO;
import com.example.taskmanager.repo.Messaging.ConversationRepository;
import com.example.taskmanager.repo.User.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder pe;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordTokenService passwordTokenService;

    @Autowired
    private FriendRequestRepository friendRequestRepo;

    @Autowired
    private ProfilePicRepository profilePicRepo;

    @Autowired
    private AccountSettingsRepository accountSettingsRepo;

    @Autowired
    private ConversationRepository conversationRepo;

    @Autowired
    private RoleRepository roleRepo;

    /**
     * Method that helps send user the password reset email.
     * **/
    public void sendResetPasswordEmail(HttpServletRequest request, MyUser user){
        PasswordResetToken token = passwordTokenService.createPasswordResetToken(user);
        String message = emailService.PasswordRestEmail(request, token);
        emailService.sendMail(user.getEmail(), "Password Reset Link:", message);
    }

    /**
     * Method for updating a users password
     * **/
    public void setNewPasswordForUser(MyUser user, String password){
        user.setPassword(pe.encode(password));
        userRepo.save(user);
    }

    /**
     * Creates a user DTO object
     * @param user
     * @return
     */
    public UserDTO getUserDTO(MyUser user){
        return new UserDTO(user.getId(), user.getUsername(), user.getBio(),user.getLocation(),user.getBase64ProfilePic());
    }

    /**
     * Gets a user by email
     * @param email
     * @return
     */
    public MyUser getUserByEmail(String email){return userRepo.findByEmail(email);}

    /**
     * Gets a user by their id
     * @param id
     * @return
     */
    public MyUser getUserById(Long id){return  userRepo.findById(id).get();}

    /**
     * Gets a user by their username
     * @param username
     * @return
     */
    public MyUser getUserByUsername(String username){return userRepo.findByUsername(username);}

    /**
     * Handles the creation of a new user account
     * @param user
     */
    public void createUserAccount(MyUser user){
        user.getRoles().add(roleRepo.findByName("USER"));
        user.setPassword(pe.encode(user.getPassword()));
        AccountSettings accountSettings = new AccountSettings();
        accountSettingsRepo.save(accountSettings);
        user.setAccountSettings(accountSettings);
        userRepo.save(user);
    }

    /**
     * Create a list of userDTO so that it can be used in API
     * @param users
     * @return
     */
    public List<UserDTO> getUsersDTO(List<MyUser> users){
        List<UserDTO> friendsDTO = new ArrayList<>();
        for (MyUser u: users) {
            UserDTO user = getUserDTO(u);
            friendsDTO.add(user);
        }
        return friendsDTO;
    }

    /**
     * Method for getting user friends
     **/
    public List<MyUser> getUserFriends(MyUser user){
        return user.getFriends();
    }

    /**
     * Method to serach through users friend based on search query
     * @param principal
     * @param search
     * @return
     */
    public List<MyUser> searchForUserFriends(Principal principal, String search){
        MyUser user = userRepo.findByEmail(principal.getName());
        List<MyUser> friendsMatchingSearch = new ArrayList<>();
        for (MyUser friend: user.getFriends()) {
            if (friend.getUsername().toLowerCase().startsWith(search.toLowerCase()) && friend.hasRole("MEMBER")) {
                friendsMatchingSearch.add(friend);
            }
        }
        return friendsMatchingSearch;
    }

    /**
     * Sends friend request to user from sender
     * @param principal
     * @param recipientId
     */
    public Long sendFriendRequest(Principal principal, Long recipientId){
        MyUser sender = userRepo.findByEmail(principal.getName());
        MyUser recipient = userRepo.findById(recipientId).get();
        FriendRequest friendRequest = new FriendRequest(sender, recipient);
        return friendRequestRepo.save(friendRequest).getId();
    }

    /**
     * Deletes a friend request when it is canceled, accepted or rejected
     * @param id
     */
    public void deleteFriendRequest(Long id){
        FriendRequest friendRequest = friendRequestRepo.findById(id).get();
        friendRequest.getSender().getSentFriendRequests().remove(friendRequest);
        friendRequest.getRecipient().getReceivedFriendRequests().remove(friendRequest);
        friendRequestRepo.delete(friendRequest);
    }

    /**
     * If a user accepts a friend request then the users are now friends and
     * also creates a conversation between the two users
     * @param id
     */
    public void acceptFriendRequest(Long id){
        FriendRequest request = friendRequestRepo.findById(id).get();
        MyUser sender = request.getSender();
        MyUser recipient = request.getRecipient();

        sender.getFriends().add(recipient);
        recipient.getFriends().add(sender);
        userRepo.save(sender);
        userRepo.save(recipient);

        //If a user adds other users then create an empty conversation
        boolean exists = false;
        for (Conversation chat: sender.getConversations()) {
            if(chat.getParticipants().contains(sender) && chat.getParticipants().contains(recipient)){
                exists = true;
            }
        }
        if(!exists) {
            Conversation conversation = new Conversation();
            conversation.getParticipants().add(sender);
            conversation.getParticipants().add(recipient);
            conversationRepo.save(conversation);
        }
        deleteFriendRequest(id);
    }

    /**
     * Checks if a friend request exists
     * @param id
     * @return
     */
    public boolean friendRequestExistsById(Long id){return friendRequestRepo.existsById(id);}

    /**
     * Used to check if the user has some sort of ownership over request for security reasons
     * @param id
     * @param principal
     * @return
     */
    public boolean userHasFriendRequest(Long id, Principal principal){
        FriendRequest friendRequest = friendRequestRepo.findById(id).get();
        MyUser user = getUserByEmail(principal.getName());
        return user.getSentFriendRequests().contains(friendRequest) || user.getReceivedFriendRequests().contains(friendRequest);
    }

    /**
     * Checks if current logged-in user has a friend request to another user
     * @param sender
     * @param recipient
     * @return
     */
    public boolean hasUserSentFriendRequestToUserOrReceived(MyUser sender, MyUser recipient){
        return friendRequestRepo.existsBySenderAndRecipient(sender,recipient);
    }

    /**
     * Used to get a friend request if it exists
     * @param user1
     * @param user2
     * @return
     */
    public FriendRequest getFriendRequestIfExists(MyUser user1, MyUser user2){
        if(hasUserSentFriendRequestToUserOrReceived(user1,user2)){
            return friendRequestRepo.findBySenderAndRecipient(user1,user2);
        }else if(hasUserSentFriendRequestToUserOrReceived(user2,user1)){
            return friendRequestRepo.findBySenderAndRecipient(user2,user1);
        }
        return null;
    }

    /**
     * Handles database actions for removing users as friends
     * @param principal
     * @param userId
     */
    public void removeUserFromFriendsList(Principal principal, Long userId){
        MyUser user = userRepo.findByEmail(principal.getName());
        MyUser userToRemove = userRepo.findById(userId).get();
        user.getFriends().remove(userToRemove);
        userToRemove.getFriends().remove(user);
        userRepo.save(user);
        userRepo.save(userToRemove);
    }


    /**
     * If a user decides to edit their profile then this method will handle the changes onto the database
     * @param userDTO
     * @param pfp
     * @throws IOException
     */
    public void saveUserProfileChanges(UserDTO userDTO, MultipartFile pfp) throws IOException {
        MyUser user = getUserById(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setBio(userDTO.getBio());
        user.setLocation(userDTO.getLocation());
        if(pfp != null){
            if(user.getProfilePic() != null){
                ProfilePic profilePic = user.getProfilePic();
                user.setProfilePic(null);
                userRepo.save(user);
                profilePicRepo.delete(profilePic);
            }
            ProfilePic profilePic = new ProfilePic(pfp.getName(),pfp.getContentType(),pfp.getBytes());
            profilePicRepo.save(profilePic);
            user.setProfilePic(profilePic);
        }
        userRepo.save(user);
    }

    /**
     * Returns users based on search on the username/first name/surname
     * @param search
     * @return
     */
    public List<MyUser> findAllMatchingSearch(String search){
        List<MyUser> results = new ArrayList<>();
        List<MyUser> usernames = userRepo.findAllByUsernameContainingIgnoreCase(search);
        List<MyUser> firstNames = userRepo.findAllByFirstNameContainingIgnoreCase(search);
        List<MyUser> surnames = userRepo.findAllBySurnameContainingIgnoreCase(search);
        results.addAll(usernames);
        results.addAll(firstNames);
        results.addAll(surnames);
        return new ArrayList<>(new HashSet<>(results));
    }

    /**
     * Used to check when a user is trying to update their username
     * @param username
     * @return
     */
    public boolean existsByUsername(String username){
        return userRepo.findByUsername(username) != null;
    }

    /**
     * Used to check if provided password is the same as the logged in users.
     * @param principal
     * @param password
     * @return
     */
    public boolean isUsersPassword(Principal principal, String password){
        MyUser user = getUserByEmail(principal.getName());
        return pe.matches(password, user.getPassword());
    }


    /**
     * If a user changes there account details then this method will do this
     * @param principal
     * @param updatedUser
     */
    public void changeAccountDetails(Principal principal, MyUser updatedUser){
        MyUser user = getUserByEmail(principal.getName());
        user.setFirstName(updatedUser.getFirstName());
        user.setSurname(updatedUser.getSurname());
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        user.setMoodRating(updatedUser.getMoodRating());
        user.getMoodRatingsPast7Days().set(user.getMoodRatingsPast7Days().size()-1, updatedUser.getMoodRating());
        userRepo.save(user);
    }


    /**
     * Used to update user privacy settings
     * @param updatedSettings
     * @param principal
     */
    public void updatePrivacySettings(AccountSettings updatedSettings, Principal principal){
        MyUser user = getUserByEmail(principal.getName());
        AccountSettings accountSettings = user.getAccountSettings();
        accountSettings.setAccountPrivate(updatedSettings.isAccountPrivate());
        accountSettings.setDisplayEmail(updatedSettings.getDisplayEmail());
        accountSettings.setDisplayName(updatedSettings.getDisplayName());
        accountSettings.setDisplayTasks(updatedSettings.getDisplayTasks());
        accountSettings.setDisplayNumber(updatedSettings.getDisplayNumber());
        accountSettings.setDisplayLocation(updatedSettings.getDisplayLocation());
        accountSettingsRepo.save(accountSettings);
    }


    /**
     * Used to update user notification settings
     * @param updateSettings
     * @param principal
     */
    public void updateNotificationSettings(AccountSettings updateSettings, Principal principal){
        MyUser user = getUserByEmail(principal.getName());
        AccountSettings accountSettings = user.getAccountSettings();
        accountSettings.setSendTaskNotifications(updateSettings.isSendTaskNotifications());
        accountSettings.setSendAssignmentNotifications(updateSettings.isSendAssignmentNotifications());
        accountSettings.setSendStudySessionNotifications(updateSettings.isSendStudySessionNotifications());
        accountSettings.setEmailTaskReminders(updateSettings.isEmailTaskReminders());
        accountSettings.setEmailAssignmentReminders(updateSettings.isEmailAssignmentReminders());
        accountSettings.setEmailStudySessionReminders(updateSettings.isEmailStudySessionReminders());
        accountSettingsRepo.save(accountSettings);
    }

    /**
     * Checks if it's the users first time logging in on a day
     * @return
     */
    public boolean isFirstLoginToday(MyUser user){
        LocalDate lastLogin = user.getLastLoginDate();
        LocalDate today = LocalDate.now();
        if(lastLogin == null || !lastLogin.equals(today)){
            user.setLastLoginDate(today);
            userRepo.save(user);
            return true;
        }
        return false;
    }

    /**
     * Updates a users mood on login
     * @param principal
     */
    public void updateCurrentUserMood(Principal principal, short moodRating){
        MyUser user = getUserByEmail(principal.getName());
        user.setMoodRating(moodRating);
        if(user.getMoodRatingsPast7Days().size() == 7){
            user.getMoodRatingsPast7Days().remove(0);
        }
        user.getMoodRatingsPast7Days().add(moodRating);
        userRepo.save(user);
    }
}
