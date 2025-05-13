package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Messaging.Conversation;
import com.example.taskmanager.domain.Messaging.ConversationMessage;
import com.example.taskmanager.domain.Messaging.DTO.ConversationDTO;
import com.example.taskmanager.domain.Messaging.DTO.ConversationMessageDTO;
import com.example.taskmanager.domain.Messaging.DTO.GroupChatDTO;
import com.example.taskmanager.domain.Messaging.DTO.GroupMessageDTO;
import com.example.taskmanager.domain.Messaging.GroupChat;
import com.example.taskmanager.domain.Messaging.GroupMessage;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.User.DTO.UserDTO;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.Messaging.ConversationMessageRepository;
import com.example.taskmanager.repo.Messaging.ConversationRepository;
import com.example.taskmanager.repo.Messaging.GroupChatRepository;
import com.example.taskmanager.repo.Messaging.GroupMessageRepository;
import com.example.taskmanager.service.ChatService;
import com.example.taskmanager.service.FileService;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    MyUser user1;
    MyUser user2;
    Conversation conversation;
    GroupChat groupChat;
    GroupChat groupChat2;
    GroupTask groupTask;
    Assignment assignment;
    List<MyUser> users = new ArrayList<>();

    List<UserDTO> userDTOS = new ArrayList<>();

    MockMultipartFile file;

    MockPrincipal mockPrincipal;

    @InjectMocks
    ChatService chatService;

    @Mock
    UserService userService;

    @Mock
    FileService fileService;

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    GroupChatRepository groupChatRepository;

    @Mock
    ConversationMessageRepository conversationMessageRepository;

    @Mock
    GroupMessageRepository groupMessageRepository;

    @BeforeEach
    public void setUp(){
        this.user1 = new MyUser("foo@bar.com","foo", "bar", "password", "foofoo");
        this.user2 = new MyUser("bar@foo.com","bar","foo","password","foobar");
        this.conversation = new Conversation();
        users.add(user1);
        users.add(user2);
        for (MyUser user:users) {
            userDTOS.add(new UserDTO(user.getId(),user.getUsername(),user.getBio(),user.getLocation(),user.getBase64ProfilePic()));
        }
        conversation.setId(1L);
        conversation.getParticipants().addAll(users);
        conversation.updateLatestTimeStamp();
        user1.getConversations().add(conversation);
        user2.getConversations().add(conversation);
        this.groupTask = new GroupTask("task",true,true,(short)4,(short)4,"desc",new Date(),new Date(),0,new Date(),false,false,user1,users);
        groupTask.setId(1L);
        this.groupChat = new GroupChat(groupTask.getTitle(),groupTask);
        groupTask.setGroupChat(groupChat);
        this.assignment = new Assignment(user2,"assignment","course","desc",new Date(),new Date(),false,false,0,true,true);
        assignment.setId(1L);
        this.groupChat2 = new GroupChat(assignment.getTitle(),assignment);
        assignment.setGroupChat(groupChat2);
        assignment.setUsers(users);
        groupChat.setId(1L);
        groupChat.getParticipants().addAll(users);
        groupChat.updateLatestTimeStamp();
        user1.getGroupChats().add(groupChat);
        user2.getGroupChats().add(groupChat);
        groupChat2.setId(2L);
        groupChat2.getParticipants().addAll(users);
        groupChat2.updateLatestTimeStamp();
        user1.getGroupChats().add(groupChat2);
        user2.getGroupChats().add(groupChat2);
        for (int i = 0; i < 5; i++) {
            MyUser sender;
            if(i %2 ==0){
                sender = user1;
            }else{
                sender = user2;
            }
            conversation.getMessages().add(new ConversationMessage("hello",conversation,sender));
            GroupMessage groupMessage1 = new GroupMessage("hello",groupChat,sender);
            groupChat.getMessages().add(groupMessage1);
            GroupMessage groupMessage2 = new GroupMessage("hello",groupChat2,sender);
            groupChat2.getMessages().add(groupMessage2);
        }
        this.file = new MockMultipartFile("file","test-file.pdf","application/pdf","File test".getBytes());
        this.mockPrincipal = new MockPrincipal(user1.getEmail());
    }

    @DisplayName("Test getting conversation by id")
    @Test
    public void testFindConversationById(){
        when(conversationRepository.findById(1L)).thenReturn(Optional.ofNullable(conversation));
        Conversation result = chatService.findConversationById(1L);
        verify(conversationRepository,times(1)).findById(1L);
        assertEquals(conversation,result);
    }

    @DisplayName("Test if conversation exists by id")
    @Test
    public void testIfConversationExistsById(){
        when(conversationRepository.existsById(1L)).thenReturn(true);
        assertTrue(chatService.conversationExistsById(1L));
        verify(conversationRepository,times(1)).existsById(1L);
    }

    @DisplayName("Test saving conversation message")
    @Test
    public void testSavingConversationMessage(){
        ConversationMessage test = new ConversationMessage("test",conversation,user1);
        chatService.saveConversationMessage(test);
        verify(conversationMessageRepository,times(1)).save(test);
        verify(conversationRepository,times(1)).save(conversation);
    }

    @DisplayName("Test saving group message")
    @Test
    public void testSavingGroupMessage(){
        GroupMessage test = new GroupMessage("test",groupChat,user2);
        chatService.saveGroupMessage(test);
        verify(groupMessageRepository,times(1)).save(test);
        verify(groupChatRepository,times(1)).save(groupChat);
    }

    @DisplayName("Test if user is part of a conversation via id")
    @Test
    public void testIfUserIsPartOfConversation(){
        when(userService.getUserByEmail(user1.getEmail())).thenReturn(user1);
        when(conversationRepository.findById(1L)).thenReturn(Optional.ofNullable(conversation));
        assertTrue(chatService.userIsPartOfConversation(1L,mockPrincipal));
    }

    @DisplayName("Test creating conversation message DTO")
    @Test
    public void testCreatingConversationMessageDTO(){
        ConversationMessage test = conversation.getMessages().get(0);
        ConversationMessageDTO result = chatService.createConversationMessageDTO(test);
        assertEquals(test.getConversation().getId(),result.getConversation());
        assertEquals(test.getSender().getId(),result.getSender());
        assertEquals(test.getContent(),result.getContent());
        assertEquals(test.getDateSent(),result.getSent());
        assertEquals(test.getSender().getUsername(),result.getSenderUsername());
    }

    @DisplayName("Test creating conversation DTO")
    @Test
    public void testCreatingConversationDTO(){
        when(userService.getUsersDTO(conversation.getParticipants())).thenReturn(userDTOS);
        ConversationDTO result = chatService.createConversationDTO(conversation);
        assertEquals(conversation.getId(),result.getId());
        assertEquals(conversation.getLatestMessageTimeStamp(),result.getLatestMessageTimeStamp());
    }

    @DisplayName("Test getting group chat by id")
    @Test
    public void testFindGroupChatById(){
        when(groupChatRepository.findById(1L)).thenReturn(Optional.ofNullable(groupChat));
        GroupChat result = chatService.findGroupChatById(1L);
        verify(groupChatRepository,times(1)).findById(1L);
        assertEquals(groupChat,result);
    }

    @DisplayName("Test if group chat exists by id")
    @Test
    public void testGroupChatExistsById(){
        when(groupChatRepository.existsById(1L)).thenReturn(true);
        assertTrue(chatService.groupChatExistsById(1L));
        verify(groupChatRepository,times(1)).existsById(1L);
    }

    @DisplayName("Test if user is a part of group chat")
    @Test
    public void testUserIsPartOfGroupChat(){
        when(userService.getUserByEmail(user1.getEmail())).thenReturn(user1);
        when(groupChatRepository.findById(1L)).thenReturn(Optional.ofNullable(groupChat));
        assertTrue(chatService.userIsPartOfGroupChat(1L,mockPrincipal));
    }

    @DisplayName("Test creating group message DTO")
    @Test
    public void testCreatingGroupMessageDTO(){
        GroupMessage test = groupChat.getMessages().get(0);
        GroupMessageDTO result = chatService.createGroupMessageDTO(test);
        assertEquals(test.getGroupChat().getId(),result.getGroupChat());
        assertEquals(test.getSender().getId(),result.getSender());
        assertEquals(test.getContent(),result.getContent());
        assertEquals(test.getDateSent(),result.getSent());
        assertEquals(test.getSender().getUsername(),result.getSenderUsername());
    }


    @DisplayName("Test creating group chat DTO")
    @Test
    public void testCreatingGroupChatDTO(){
        when(userService.getUsersDTO(groupChat.getParticipants())).thenReturn(userDTOS);
        GroupChatDTO result = chatService.createGroupChatDTO(groupChat);
        assertEquals(groupChat.getId(),result.getId());
        assertEquals(groupChat.getGroupTask().getId(),result.getGroupTask());
        assertEquals(groupChat.getGroupName(),result.getGroupName());
        assertEquals(groupChat.getLatestMessageTimeStamp(),result.getLatestMessageTimeStamp());
    }

    @DisplayName("Test getting a users chat")
    @Test
    public void testGetUserChats(){
        when(userService.getUserByEmail(user1.getEmail())).thenReturn(user1);
        List<Object> result = chatService.getUserChats(mockPrincipal);
        assertTrue(result.contains(conversation));
        assertTrue(result.contains(groupChat));
        assertTrue(result.contains(groupChat2));
    }

    @DisplayName("Test creating group chat for group task")
    @Test
    public void testCreatingGroupChatForGroupTask(){
        GroupTask test = new GroupTask();
        test.setId(2L);
        test.setTitle("Testing");
        test.setUsers(users);
        when(userService.getUsersDTO(users)).thenReturn(userDTOS);
        GroupChatDTO result = chatService.createGroupChatForGroupTask(test);
        verify(groupChatRepository,times(1)).save(any(GroupChat.class));
        assertEquals(test.getId(),result.getGroupTask());
        assertEquals(test.getTitle(),result.getGroupName());
    }

    @DisplayName("Test creating group chat for an assignment")
    @Test
    public void testCreatingGroupChatForAssignment(){
        Assignment test = new Assignment();
        test.setId(2L);
        test.setTitle("Testing");
        test.setUsers(users);
        when(userService.getUsersDTO(users)).thenReturn(userDTOS);
        GroupChatDTO result = chatService.createGroupChatForAssignment(test);
        verify(groupChatRepository,times(1)).save(any(GroupChat.class));
        assertEquals(test.getId(),result.getAssignment());
        assertEquals(test.getTitle(),result.getGroupName());
    }

    @DisplayName("Test getting user chat list DTO")
    @Test
    public void testGetUserChatsDTO(){
        when(userService.getUserByEmail(user1.getEmail())).thenReturn(user1);
        List<Object> result = chatService.getUserChatsDTO(mockPrincipal);
        assertEquals(3,result.size());
    }

    @DisplayName("Test getting chats from search")
    @Test
    public void testGetChatsFromSearch(){
        when(userService.getUserByEmail(user1.getEmail())).thenReturn(user1);
        chatService.getChatsFromSearchDTO(anyString(),mockPrincipal);
        verify(groupChatRepository,times(1)).findByGroupNameContainingIgnoreCaseAndParticipants(anyString(),eq(user1));
        verify(conversationRepository,times(1)).findByParticipantsUsernameContainingIgnoreCaseAndParticipantsContaining(anyString(),eq(user1));
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
