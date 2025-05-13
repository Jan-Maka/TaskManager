package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.domain.Messaging.Conversation;
import com.example.taskmanager.domain.User.*;
import com.example.taskmanager.domain.User.DTO.UserDTO;
import com.example.taskmanager.repo.Messaging.ConversationRepository;
import com.example.taskmanager.repo.User.*;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    MyUser user;
    MyUser friend1;
    MyUser friend2;
    MyUser unknown;
    Role role1;
    Role role2;
    List<MyUser> friends = new ArrayList<>();
    FriendRequest request;
    ProfilePic pfp;
    MockMultipartFile file;
    Principal mockPrincipal;

    AccountSettings accountSettings;

    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    ConversationRepository conversationRepository;
    @Mock
    FriendRequestRepository friendRequestRepository;
    @Mock
    ProfilePicRepository profilePicRepository;
    @Mock
    AccountSettingsRepository accountSettingsRepository;
    @InjectMocks
    UserService userService;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        this.user = new MyUser("foo@bar.com","foo", "bar", passwordEncoder.encode("password"), "foofoo");
        this.friend1 = new MyUser("bar@foo.com","bar","foo","password","foobar");
        this.friend2 = new MyUser("test1@foo.com","test","ing","password","testing1");
        this.unknown = new MyUser("testing2@foo.com","test","ing1","password","test2");
        friends.add(friend1);
        friends.add(friend2);
        user.getFriends().addAll(friends);
        this.role1 = new Role("USER");
        this.role2 = new Role("MEMBER");
        List<Role> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        user.getRoles().addAll(roles);
        friend1.getRoles().addAll(roles);
        friend2.getRoles().addAll(roles);
        unknown.getRoles().addAll(roles);
        request = new FriendRequest(user,unknown);
        request.setId(1L);
        this.accountSettings = new AccountSettings();
        user.setAccountSettings(accountSettings);
        this.file = new MockMultipartFile("file","test-file.pdf","application/pdf","File test".getBytes());
        this.pfp = new ProfilePic(file.getOriginalFilename(),file.getContentType(), file.getBytes());
        this.mockPrincipal = new MockPrincipal(user.getEmail());
    }

    @DisplayName("Test getting user by Id")
    @Test
    public void testGettingUserById(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        MyUser result = userService.getUserById(1L);
        assertEquals(user,result);
    }

    @DisplayName("Test getting user by email")
    @Test
    public void testGetUserByEmail(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        MyUser result = userService.getUserByEmail("foo@bar.com");
        assertEquals(user,result);
    }

    @DisplayName("Test getting user by username")
    @Test
    public void testGetUserByUsername(){
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        MyUser result = userService.getUserByUsername("foofoo");
        assertEquals(user,result);
    }

    @DisplayName("Test creating user account")
    @Test
    public void testCreateUserAccount(){
        String encodedPass = passwordEncoder.encode("pass");
        MyUser newUser = new MyUser("joe@gomez.com","Joe","Gomez","pass","Joe");
        when(roleRepository.findByName("USER")).thenReturn(role1);
        when(passwordEncoder.encode("pass")).thenReturn(encodedPass);
        userService.createUserAccount(newUser);
        verify(roleRepository,times(1)).findByName("USER");
        verify(passwordEncoder, times(2)).encode("pass");
        verify(accountSettingsRepository,times(1)).save(any(AccountSettings.class));
        verify(userRepository,times(1)).save(newUser);
        assertEquals(encodedPass,newUser.getPassword());
    }


    @DisplayName("Test user password is secure hash")
    @Test
    public void testSetHashedPassword(){
        String plainTextPass = "password";
        when(userRepository.save(user)).thenReturn(user);
        userService.setNewPasswordForUser(user,plainTextPass);
        assertNotSame(user.getPassword(),plainTextPass);
    }

    @DisplayName("Test creating user DTO")
    @Test
    public void testCreatingUserDTO(){
        UserDTO userDTO = userService.getUserDTO(user);
        assertEquals(user.getId(),userDTO.getId());
        assertEquals(user.getUsername(), userDTO.getUsername());
        assertEquals(user.getBio(),userDTO.getBio());
        assertEquals(user.getLocation(), userDTO.getLocation());
        assertEquals(user.getBase64ProfilePic(),userDTO.getProfilePic());
    }

    @DisplayName("Test user has friends")
    @Test
    public void testGetUserFriends(){
        List<MyUser> friends = userService.getUserFriends(user);
        assertEquals(2, friends.size());
        assertTrue(friends.contains(friend1));
        assertTrue(friends.contains(friend2));
    }

    @DisplayName("Test search for user friends")
    @Test
    public void testUserFriendSearch(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        List<MyUser> results = userService.searchForUserFriends(mockPrincipal,friend2.getUsername());
        assertEquals(1,results.size());
        assertTrue(results.contains(friend2));
    }

    @DisplayName("Test sending friend request")
    @Test
    public void testUserSendingFriendRequest(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(unknown));
        when(friendRequestRepository.save(Mockito.any(FriendRequest.class))).thenReturn(request);
        Long id = userService.sendFriendRequest(mockPrincipal, 1L);
        assertEquals(1L,id);
    }

    @DisplayName("Test delete friend request")
    @Test
    public void testDeleteFriendRequest(){
        when(friendRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        userService.deleteFriendRequest(request.getId());
        verify(friendRequestRepository,times(1)).findById(1L);
        verify(friendRequestRepository, times(1)).delete(request);
    }

    @DisplayName("Test accept friend request")
    @Test
    public void testAcceptingFriendRequest(){
        when(friendRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.save(unknown)).thenReturn(unknown);
        when(conversationRepository.save(Mockito.any(Conversation.class))).thenReturn(new Conversation());
        when(friendRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        userService.acceptFriendRequest(1L);
        verify(friendRequestRepository,times(2)).findById(1L);
        verify(userRepository, times(1)).save(user);
        verify(userRepository, times(1)).save(unknown);
        assertTrue(user.getFriends().contains(unknown));
        assertTrue(unknown.getFriends().contains(user));
    }

    @DisplayName("Test friend request exists by Id")
    @Test
    public void testFriendRequestExistsById(){
        when(friendRequestRepository.existsById(1L)).thenReturn(true);
        assertTrue(userService.friendRequestExistsById(request.getId()));
        verify(friendRequestRepository,times(1)).existsById(1L);
    }

    @DisplayName("Test if user has friend request")
    @Test
    public void testUserHasFriendRequest(){
        when(friendRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        user.getReceivedFriendRequests().add(request);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        assertTrue(userService.userHasFriendRequest(1L,mockPrincipal));
    }

    @DisplayName("Test if user has sent request or received from other user")
    @Test
    public void testUserHasSentOrReceivedRequestToUser(){
        when(friendRequestRepository.existsBySenderAndRecipient(user,unknown)).thenReturn(true);
        assertTrue(userService.hasUserSentFriendRequestToUserOrReceived(user,unknown));
        verify(friendRequestRepository,times(1)).existsBySenderAndRecipient(user,unknown);
    }

    @DisplayName("Test getting request if it exists between users")
    @Test
    public void testFriendRequestIfItExists(){
        when(friendRequestRepository.existsBySenderAndRecipient(user,unknown)).thenReturn(true);
        when(friendRequestRepository.findBySenderAndRecipient(user,unknown)).thenReturn(request);
        FriendRequest exists = userService.getFriendRequestIfExists(user,unknown);
        verify(friendRequestRepository,times(1)).findBySenderAndRecipient(user,unknown);
        assertSame(request,exists);
    }

    @DisplayName("Test removing users from each others friend list")
    @Test
    public void testRemovingUserFromFriendList(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(friend1));
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.save(friend1)).thenReturn(friend1);
        userService.removeUserFromFriendsList(mockPrincipal,1L);
        verify(userRepository, times(1)).save(user);
        verify(userRepository, times(1)).save(friend1);
        assertTrue(!user.getFriends().contains(friend1));
        assertTrue(!friend1.getFriends().contains(user));
    }

    @DisplayName("Test user profile changes")
    @Test
    public void testUserProfileChanges() throws IOException {
        user.setProfilePic(pfp);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserDTO userDTO = new UserDTO(1L,"wazza","bio","leicester",null);
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        userService.saveUserProfileChanges(userDTO,file);
        verify(profilePicRepository, times(1)).delete(pfp);
        verify(userRepository, times(2)).save(user);
        assertEquals(user.getUsername(),userDTO.getUsername());
        assertEquals(user.getBio(),userDTO.getBio());
        assertEquals(user.getLocation(),userDTO.getLocation());
        assertEquals(user.getProfilePic().getData(),pfp.getData());
    }

    @DisplayName("Test user finding users matching search")
    @Test
    public void testFindingUsersMatchingSearch(){
        List<MyUser> users = new ArrayList<>();
        when(userRepository.findAllByUsernameContainingIgnoreCase("foo")).thenReturn(new ArrayList<>());
        when(userRepository.findAllByFirstNameContainingIgnoreCase("foo")).thenReturn(new ArrayList<>());
        when(userRepository.findAllBySurnameContainingIgnoreCase("foo")).thenReturn(new ArrayList<>());
        List<MyUser> results = userService.findAllMatchingSearch("foo");
        assertTrue(results.isEmpty());

        users.add(user);
        users.add(friend1);
        when(userRepository.findAllByUsernameContainingIgnoreCase("foo")).thenReturn(users);
        results = userService.findAllMatchingSearch("foo");
        assertTrue(results.contains(user));
        assertTrue(results.contains(friend1));
    }

    @DisplayName("Test if user exists by username")
    @Test
    public void testIfUserExistsByUsername(){
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        assertTrue(userService.existsByUsername(user.getUsername()));
        verify(userRepository,times(1)).findByUsername(user.getUsername());
    }

    @DisplayName("Test if password matches with logged in user")
    @Test
    public void testIfPasswordIsTheUsersPassword(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(passwordEncoder.matches("password",user.getPassword())).thenReturn(true);
        assertTrue(userService.isUsersPassword(mockPrincipal, "password"));
    }

    @DisplayName("Test if account details has changed")
    @Test
    public void testAccountDetailsChange(){
        user.getMoodRatingsPast7Days().add((short) 0);
        MyUser update = new MyUser();
        update.setFirstName("Moe");
        update.setSurname("Lester");
        update.setPhoneNumber("07888888888");
        update.setMoodRating((short) 3);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        userService.changeAccountDetails(mockPrincipal,update);
        verify(userRepository,times(1)).save(user);
        assertEquals(user.getFirstName(),update.getFirstName());
        assertEquals(user.getSurname(),update.getSurname());
        assertEquals(user.getPhoneNumber(),update.getPhoneNumber());
        assertEquals(user.getMoodRating(),update.getMoodRating());
        assertEquals(user.getMoodRatingsPast7Days().get(0),user.getMoodRating());
    }

    @DisplayName("Test updating of privacy settings")
    @Test
    public void testUpdatingOfPrivacySettings(){
        AccountSettings updateSettings = new AccountSettings();
        updateSettings.setAccountPrivate(true);
        updateSettings.setDisplayNumber(PrivacyAccess.FRIEND);
        updateSettings.setDisplayName(PrivacyAccess.FRIEND);
        updateSettings.setDisplayEmail(PrivacyAccess.FRIEND);
        updateSettings.setDisplayLocation(PrivacyAccess.FRIEND);
        updateSettings.setDisplayTasks(PrivacyAccess.FRIEND);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(accountSettingsRepository.save(accountSettings)).thenReturn(accountSettings);
        userService.updatePrivacySettings(updateSettings,mockPrincipal);
        verify(accountSettingsRepository,times(1)).save(accountSettings);
        assertEquals(updateSettings.isAccountPrivate(),accountSettings.isAccountPrivate());
        assertEquals(updateSettings.getDisplayEmail(),accountSettings.getDisplayEmail());
        assertEquals(updateSettings.getDisplayLocation(),accountSettings.getDisplayLocation());
        assertEquals(updateSettings.getDisplayName(),accountSettings.getDisplayName());
        assertEquals(updateSettings.getDisplayNumber(),accountSettings.getDisplayNumber());
        assertEquals(updateSettings.getDisplayTasks(),accountSettings.getDisplayTasks());
    }

    @DisplayName("Test updating of notification settings")
    @Test
    public void testUpdatingOfNotificationSettings(){
        AccountSettings updateSettings = new AccountSettings();
        updateSettings.setSendTaskNotifications(true);
        updateSettings.setSendAssignmentNotifications(true);
        updateSettings.setSendTaskNotifications(true);
        updateSettings.setEmailStudySessionReminders(true);
        updateSettings.setEmailAssignmentReminders(true);
        updateSettings.setEmailTaskReminders(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(accountSettingsRepository.save(accountSettings)).thenReturn(accountSettings);
        userService.updateNotificationSettings(updateSettings,mockPrincipal);
        verify(accountSettingsRepository,times(1)).save(accountSettings);
        assertEquals(updateSettings.isSendTaskNotifications(),accountSettings.isSendTaskNotifications());
        assertEquals(updateSettings.isSendAssignmentNotifications(),accountSettings.isSendAssignmentNotifications());
        assertEquals(updateSettings.isSendStudySessionNotifications(),accountSettings.isSendStudySessionNotifications());
        assertEquals(updateSettings.isEmailTaskReminders(),accountSettings.isEmailTaskReminders());
        assertEquals(updateSettings.isEmailAssignmentReminders(),accountSettings.isEmailAssignmentReminders());
        assertEquals(updateSettings.isEmailStudySessionReminders(),accountSettings.isEmailStudySessionReminders());
    }

    @DisplayName("Test if user first login")
    @Test
    public void testIsUserFirstLogin(){
        when(userRepository.save(user)).thenReturn(user);
        assertTrue(userService.isFirstLoginToday(user));
    }

    @DisplayName("Test if updating user mood works")
    @Test
    public void testUpdatingOfUserCurrentMood(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        userService.updateCurrentUserMood(mockPrincipal, (short) 3);
        verify(userRepository,times(1)).save(user);
        assertEquals((short) 3,user.getMoodRating());
    }

    //Since principal is used quite a lot to retrieve logged in user
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
