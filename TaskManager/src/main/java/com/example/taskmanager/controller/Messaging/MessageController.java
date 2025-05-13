package com.example.taskmanager.controller.Messaging;

import com.example.taskmanager.domain.File.FileAttachment;
import com.example.taskmanager.domain.Messaging.Conversation;
import com.example.taskmanager.domain.Messaging.ConversationMessage;
import com.example.taskmanager.domain.Messaging.DTO.ConversationMessageDTO;
import com.example.taskmanager.domain.Messaging.DTO.GroupMessageDTO;
import com.example.taskmanager.domain.Messaging.GroupChat;
import com.example.taskmanager.domain.Messaging.GroupMessage;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.service.ChatService;
import com.example.taskmanager.service.FileService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;

@Controller
public class MessageController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Whenever a user sends a message to another user it will send it in real time if they
     * are connected to webSocket and also save that message to a conversation
     * @param chatMessageDTO
     * @param conversationId
     */
    @Transactional
    @MessageMapping("chat.sendMessage/conversation/{conversationId}")
    public void sendMessageToUser(@DestinationVariable Long conversationId, @Payload ConversationMessageDTO chatMessageDTO) throws IOException {
        MyUser sender = userService.getUserById(chatMessageDTO.getSender());
        MyUser recipient = userService.getUserById(chatMessageDTO.getRecipient());
        Conversation conversation = chatService.findConversationById(conversationId);
        ConversationMessage message = new ConversationMessage(chatMessageDTO.getContent(),conversation,sender);
        if(taskService.taskExistsById(chatMessageDTO.getTask())){
            message.setTask(taskService.getTaskById(chatMessageDTO.getTask()));
        }
        if(chatMessageDTO.getFile() != null){
            Long fileId = chatMessageDTO.getFile().getId();
            FileAttachment file = fileService.findFileAttachmentById(fileId);
            message.setFileAttachment(file);
        }
        chatService.saveConversationMessage(message);
        messagingTemplate.convertAndSendToUser(recipient.getUsername(), "/queue/messages", chatMessageDTO);
    }

    /**
     * Whenever a user sends a message to a group it will send it to all other users
     * in real time if they are connected to webSocket
     * @param chatMessageDTO
     * @param groupId
     */
    @Transactional
    @MessageMapping("chat.sendMessage/group/{groupId}")
    public void sendMessageToGroup(@DestinationVariable Long groupId, @Payload GroupMessageDTO chatMessageDTO){
        MyUser sender = userService.getUserById(chatMessageDTO.getSender());
        GroupChat groupChat = chatService.findGroupChatById(groupId);
        GroupMessage message = new GroupMessage(chatMessageDTO.getContent(), groupChat,sender);
        if(chatMessageDTO.getFile() != null){
            Long fileId = chatMessageDTO.getFile().getId();
            FileAttachment file = fileService.findFileAttachmentById(fileId);
            message.setFileAttachment(file);
        }
        chatService.saveGroupMessage(message);
        //Send to each user
        groupChat.getParticipants().forEach((user) -> {
            if(user.getId() != sender.getId()){
                messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/messages", chatMessageDTO);
            }
        });
    }

}
