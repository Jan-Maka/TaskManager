package com.example.taskmanager.api.User;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Messaging.Conversation;
import com.example.taskmanager.domain.Messaging.DTO.GroupChatDTO;
import com.example.taskmanager.domain.Messaging.GroupChat;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.service.AssignmentService;
import com.example.taskmanager.service.ChatService;
import com.example.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user/chat")
public class ChatAPI {

    @Autowired
    private ChatService chatService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AssignmentService assignmentService;

    /**
     * Endpoint for retrieving messages of a conversation
     * @param id
     * @param principal
     * @return
     */
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<?> getConversationDetails(@PathVariable Long id, Principal principal){
        if(!chatService.conversationExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(chatService.conversationExistsById(id) && chatService.userIsPartOfConversation(id, principal)){
            Conversation conversation = chatService.findConversationById(id);
            return new ResponseEntity<>(chatService.createConversationDTO(conversation),HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * Endpoint for getting messages of a group chat
     * @param id
     * @param principal
     * @return
     */
    @GetMapping("/group-chat/{id}/messages")
    public ResponseEntity<?> getGroupChatDetails(@PathVariable Long id, Principal principal){
        if(!chatService.groupChatExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(chatService.groupChatExistsById(id) && chatService.userIsPartOfGroupChat(id, principal)){
            GroupChat groupChat = chatService.findGroupChatById(id);
            return new ResponseEntity<>(chatService.createGroupChatDTO(groupChat),HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * Endpoint to create a group chat for a group task, given it exists via id and the user is a part of the task
     * @param id
     * @param principal
     * @return
     */
    @PostMapping("/group-chat/group-task/{id}")
    public ResponseEntity<?> createGroupChatForGroupTask(@PathVariable Long id, Principal principal){
        if(taskService.groupTaskExistsById(id)){
            GroupTask groupTask = taskService.getGroupTaskById(id);
            if(taskService.isInGroupTask(id,principal)){
                GroupChatDTO groupChatDTO = chatService.createGroupChatForGroupTask(groupTask);
                return new ResponseEntity<>(groupChatDTO,HttpStatus.CREATED);
            }
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Endpoint to create a group chat for a assignment, given it exists via id and the user is a part of the assignment
     * @param id
     * @param principal
     * @return
     */
    @PostMapping("/group-chat/assignment/{id}")
    public ResponseEntity<?> createGroupChatForAssignment(@PathVariable Long id, Principal principal){
        if(assignmentService.assignmentExistsById(id)){
            if(assignmentService.isOnAssignment(id,principal)){
                Assignment assignment = assignmentService.getAssignmentById(id);
                GroupChatDTO groupChatDTO = chatService.createGroupChatForAssignment(assignment);
                return new ResponseEntity<>(groupChatDTO,HttpStatus.CREATED);
            }
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    /**
     * Endpoint used to get all the logged-in users chats
     * @param principal
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity<?> getUserChats(Principal principal){
        return new ResponseEntity<>(chatService.getUserChatsDTO(principal),HttpStatus.OK);
    }

    /**
     * Endpoint for getting chats based of a users search
     * @param query
     * @param principal
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<?> getChatsFromSearch(@RequestParam String query, Principal principal){
        List<Object> results = chatService.getChatsFromSearchDTO(query,principal);
        if(!results.isEmpty()){
            return new ResponseEntity<>(results,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
