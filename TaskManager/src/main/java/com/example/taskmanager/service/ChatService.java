package com.example.taskmanager.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.Principal;
import java.util.*;

@Service
public class ChatService {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Autowired
    private ConversationMessageRepository conversationMessageRepo;

    @Autowired
    private GroupMessageRepository groupMessageRepo;

    /**
     * Simple query method to get a conversation by its id
     * @param id
     * @return
     */
    public Conversation findConversationById(Long id){return conversationRepository.findById(id).get();}

    /**
     * Checks if a conversation exists
     * @param id
     * @return
     */
    public boolean conversationExistsById(Long id){return  conversationRepository.existsById(id);}

    /**
     * Saves message to conversation and to the database
     * @param message
     */
    public void saveConversationMessage(ConversationMessage message){
        conversationMessageRepo.save(message);
        Conversation conversation = message.getConversation();
        conversation.updateLatestTimeStamp();
        conversationRepository.save(conversation);
    }

    /**
     * Saves group message to groupChat and to database
     * @param message
     */
    public void saveGroupMessage(GroupMessage message){
        groupMessageRepo.save(message);
        GroupChat groupChat = message.getGroupChat();
        groupChat.updateLatestTimeStamp();
        groupChatRepository.save(groupChat);
    }

    /**
     * Used as a check to verify that a logged in user making the api call is indeed
     * the user
     * @param id
     * @param principal
     * @return
     */
    public boolean userIsPartOfConversation(Long id,Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        Conversation conversation = findConversationById(id);
        return conversation.getParticipants().contains(user);
    }

    /**
     * Creates DTO for conversation messages so that when a user opens
     * @param message
     * @return
     */
    public ConversationMessageDTO createConversationMessageDTO(ConversationMessage message){
        Conversation conversation = message.getConversation();
        MyUser recipient = null;
        for (MyUser participant: conversation.getParticipants()) {
            if(participant != message.getSender()){
                recipient = participant;
                break;
            }
        }
        ConversationMessageDTO messageDTO = new ConversationMessageDTO(message.getContent(),message.getSender().getId(),message.getSender().getUsername(),message.getDateSent() ,recipient.getId(),message.getConversation().getId());
        if(message.getTask() != null){
            messageDTO.setTask(message.getTask().getId());
        }

        if(message.getFileAttachment() != null){
            messageDTO.setFile(fileService.createFileAttachmentDTO(message.getFileAttachment()));
        }

        return messageDTO;
    }

    /**
     * Gets a conversation messagesDTO used for displaying whenever a user opens
     * a conversation
     * @param messages
     * @return
     */
    public List<ConversationMessageDTO> getConversationMessagesDTO(List<ConversationMessage> messages){
        List<ConversationMessageDTO> messagesDTO = new ArrayList<>();
        messages.forEach((msg) -> messagesDTO.add(createConversationMessageDTO(msg)));
        return messagesDTO;
    }

    /**
     * Gets details about the conversation
     * @param conversation
     * @return
     */
    public ConversationDTO createConversationDTO(Conversation conversation){
        return new ConversationDTO(conversation.getId(), userService.getUsersDTO(conversation.getParticipants()), getConversationMessagesDTO(conversation.getMessages()), conversation.getLatestMessageTimeStamp());
    }

    /**
     * Helper method to get a chat via its id
     * @param id
     * @return
     */
    public GroupChat findGroupChatById(Long id) {
        return groupChatRepository.findById(id).get();
    }

    /**
     * Used to check whether a chat exists via id
     * @param id
     * @return
     */
    public boolean groupChatExistsById(Long id){
        return groupChatRepository.existsById(id);
    }

    /**
     * Checks if a user is apart if a groupChat
     * @param principal
     * @param id
     * @return
     */
    public boolean userIsPartOfGroupChat(Long id,Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        GroupChat groupChat = findGroupChatById(id);
        return groupChat.getParticipants().contains(user);
    }

    /**
     * Creates a DTO of a group chat
     * @param message
     * @return
     */
    public GroupMessageDTO createGroupMessageDTO(GroupMessage message){
        GroupMessageDTO messageDTO = new GroupMessageDTO(message.getContent(), message.getSender().getId(),message.getSender().getUsername(),message.getDateSent(),message.getGroupChat().getId());
        if(message.getFileAttachment() != null){
            messageDTO.setFile(fileService.createFileAttachmentDTO(message.getFileAttachment()));
        }
        return messageDTO;
    }

    /**
     * Creates list of group message DTOs
     * @param messages
     * @return
     */
    public List<GroupMessageDTO> getGroupMessagesDTOs(List<GroupMessage> messages){
        List<GroupMessageDTO> messagesDTO = new ArrayList<>();
        messages.forEach((msg) -> messagesDTO.add(createGroupMessageDTO(msg)));

        return messagesDTO;
    }

    /**
     * Creates and returns a group chat DTO
     * @param groupChat
     * @return
     */
    public GroupChatDTO createGroupChatDTO(GroupChat groupChat){
        List<UserDTO> users = userService.getUsersDTO(groupChat.getParticipants());
        GroupChatDTO groupChatDTO = new GroupChatDTO(groupChat.getId(),groupChat.getGroupName(),users,getGroupMessagesDTOs(groupChat.getMessages()),groupChat.getLatestMessageTimeStamp());
        if(groupChat.getAssignment() != null){
            groupChatDTO.setAssignment(groupChat.getAssignment().getId());
        }else{
            groupChatDTO.setGroupTask(groupChat.getGroupTask().getId());
        }
        return groupChatDTO;
    }

    /**
     * Helper method using comparator to order objects based on latest message timestamp
     * @param chats
     * @return
     */
    private List<Object> orderChatsByLatestMessageTimeStamp(List<Object> chats){
        //Comparator used to compare two dates of different object types and
        // will sort it based on the latestMessageTimeStamp object field
        Comparator<Object> comparator = Comparator.comparing(obj -> {
            if(obj instanceof Conversation){
                return ((Conversation) obj).getLatestMessageTimeStamp();
            }else if(obj instanceof GroupChat){
                return ((GroupChat) obj).getLatestMessageTimeStamp();
            }
            return null;
        }, Comparator.reverseOrder());
        chats.sort(comparator);

        return chats;
    }

    /**
     * Gets a list of a users chats (conversations or group chat)
     * List<Object> to store both entity types in the same list
     * @param principal
     * @return
     */
    public List<Object> getUserChats(Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        List<Object> chats = new ArrayList<>();
        chats.addAll(user.getGroupChats());
        chats.addAll(user.getConversations());

        return orderChatsByLatestMessageTimeStamp(chats);
    }

    /**
     * Used to create a group chat for a group task and saves it to database
     * @param groupTask
     */
    public GroupChatDTO createGroupChatForGroupTask(GroupTask groupTask){
        GroupChat groupChat = new GroupChat(groupTask.getTitle(),groupTask);
        groupChat.getParticipants().addAll(groupTask.getUsers());
        groupChatRepository.save(groupChat);
        return createGroupChatDTO(groupChat);
    }

    /**
     * Used to create a group chat for an assignment and saves it to database
     * @param assignment
     */
    public GroupChatDTO createGroupChatForAssignment(Assignment assignment){
        GroupChat groupChat = new GroupChat(assignment.getTitle(), assignment);
        groupChat.getParticipants().addAll(assignment.getUsers());
        groupChatRepository.save(groupChat);
        return createGroupChatDTO(groupChat);
    }

    /**
     * Helper method used to convert a list of chats into the DTOs
     * @param chats
     * @return
     */
    private List<Object> convertChatListToChatDTOList(List<Object> chats){
        List<Object> chatDTOs = new ArrayList<>();
        chats.forEach((chat) -> {
            if(chat instanceof Conversation){
                chatDTOs.add(createConversationDTO((Conversation) chat));
            }else if(chat instanceof GroupChat){
                chatDTOs.add(createGroupChatDTO((GroupChat) chat));
            }
        });
        return chatDTOs;
    }

    /**
     * Used to get user chats DTO for api
     * @param principal
     * @return
     */
    public List<Object> getUserChatsDTO(Principal principal){
        List<Object> chats = getUserChats(principal);
        chats = orderChatsByLatestMessageTimeStamp(chats);
        return convertChatListToChatDTOList(chats);
    }

    /**
     * Gets a list of chats DTO based of user search for filtering and orders by latest message timestamp
     * @param query
     * @param principal
     * @return
     */
    public List<Object> getChatsFromSearchDTO(String query, Principal principal){
        List<Object> chats = new ArrayList<>();
        MyUser user = userService.getUserByEmail(principal.getName());
        List<GroupChat> groupChats = groupChatRepository.findByGroupNameContainingIgnoreCaseAndParticipants(query,user);
        List<Conversation> conversations = conversationRepository.findByParticipantsUsernameContainingIgnoreCaseAndParticipantsContaining(query,user);
        chats.addAll(groupChats);
        chats.addAll(conversations);
        chats = orderChatsByLatestMessageTimeStamp(chats);
        List<Object> results = convertChatListToChatDTOList(chats);
        return results;
    }
}
