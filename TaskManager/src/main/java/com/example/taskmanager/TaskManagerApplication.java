package com.example.taskmanager;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.Events.StudySession;
import com.example.taskmanager.domain.Messaging.Conversation;
import com.example.taskmanager.domain.Messaging.GroupChat;
import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.AccountSettings;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.domain.User.Role;
import com.example.taskmanager.repo.Assignment.AssignmentRepository;
import com.example.taskmanager.repo.Assignment.AssignmentTaskRepository;
import com.example.taskmanager.repo.Events.StudySessionRepository;
import com.example.taskmanager.repo.Messaging.ConversationMessageRepository;
import com.example.taskmanager.repo.Messaging.ConversationRepository;
import com.example.taskmanager.repo.Messaging.GroupChatRepository;
import com.example.taskmanager.repo.Messaging.GroupMessageRepository;
import com.example.taskmanager.repo.Task.CategoryRepository;
import com.example.taskmanager.repo.Task.GroupTaskRepository;
import com.example.taskmanager.repo.Task.TaskRepository;
import com.example.taskmanager.repo.User.AccountSettingsRepository;
import com.example.taskmanager.repo.User.PrivilegeRepository;
import com.example.taskmanager.repo.User.RoleRepository;
import com.example.taskmanager.repo.User.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@SpringBootApplication
public class TaskManagerApplication {


	private static UserRepository userRepo;
	private static RoleRepository roleRepo;

	private static PrivilegeRepository privilegeRepo;
	private static PasswordEncoder pe;
	private static CategoryRepository categoryRepo;
	private static TaskRepository taskRepo;

	private static GroupTaskRepository groupTaskRepo;
	private static AssignmentRepository assignmentRepo;

	private static AssignmentTaskRepository assignmentTaskRepo;
	private static StudySessionRepository studySessionRepo;

	private static AccountSettingsRepository accountSettingsRepo;

	private static ConversationRepository conversationRepo;

	private static ConversationMessageRepository conversationMessageRepo;

	private static GroupChatRepository groupChatRepo;

	private static GroupMessageRepository groupMessageRepo;


	public TaskManagerApplication(PasswordEncoder encoder, UserRepository userRepository,
								  RoleRepository roleRepository, CategoryRepository categoryRepository,
								  TaskRepository taskRepository, GroupTaskRepository groupTaskRepository, AssignmentRepository assignmentRepository,
								  AssignmentTaskRepository assignmentTaskRepository , StudySessionRepository studySessionRepository,
								  AccountSettingsRepository accountSettingsRepository, ConversationRepository conversationRepository,
								  ConversationMessageRepository conversationMessageRepository, GroupChatRepository groupChatRepository,
								  GroupMessageRepository groupMessageRepository, PrivilegeRepository privilegeRepository) {
		pe = encoder;
		userRepo = userRepository;
		roleRepo = roleRepository;
		privilegeRepo = privilegeRepository;
		categoryRepo = categoryRepository;
		taskRepo = taskRepository;
		groupTaskRepo = groupTaskRepository;
		assignmentRepo = assignmentRepository;
		studySessionRepo = studySessionRepository;
		assignmentTaskRepo = assignmentTaskRepository;
		accountSettingsRepo = accountSettingsRepository;
		conversationRepo = conversationRepository;
		conversationMessageRepo = conversationMessageRepository;
		groupChatRepo = groupChatRepository;
		groupMessageRepo = groupMessageRepository;

	}

	public static void main(String[] args) {
		SpringApplication.run(TaskManagerApplication.class, args);
		Role userRole = roleRepo.findByName("USER");
		Role memberRole = roleRepo.findByName("MEMBER");

		//For creating categories for users
		String[] categories = {"Work","Chores","Social"};
		Calendar calendar = Calendar.getInstance();

		MyUser user = new MyUser();
		user.setEmail("johnny@joestar.com");
		user.setFirstName("John");
		user.setSurname("Joestar");
		user.setUsername("jojo");
		user.setBio("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed.");
		user.setPassword(pe.encode("password"));
		user.getRoles().add(userRole);
		user.getRoles().add(memberRole);
		AccountSettings accountSettings = new AccountSettings();
		accountSettingsRepo.save(accountSettings);
		user.setAccountSettings(accountSettings);
		userRepo.save(user);

		for (String cat: categories) {
			Category category = new Category(cat,user);
			categoryRepo.save(category);
			user.getCategories().add(category);
			userRepo.save(user);
			for (int i = 0; i < 5; i++) {
				calendar.setTime(new Date());
				calendar.add(Calendar.DAY_OF_MONTH, i*2);
				Date endDate = calendar.getTime();
				Task task = new Task(user,category.getName()+": Task "+i,true,true, (short) i, (short) i,"\"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea .\"",category,new Date(),endDate,0,new Date(),false,false);
				taskRepo.save(task);
				user.getTasks().add(task);
				userRepo.save(user);
				category.getTasks().add(task);
				categoryRepo.save(category);
			}
		}

		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, 3);
		Date endDate = calendar.getTime();


		Assignment assignment = new Assignment(user,"Reverse Computing Web App","Web Applications","\"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea .\"",new Date(),endDate,true,false,69,true,false);
		assignment.getUsers().add(user);
		assignmentRepo.save(assignment);
		Assignment assignment2 = new Assignment(user,"Shooter Game","Game","\"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea .\"",new Date(), endDate,false,false,0,true,true);
		assignment2.getUsers().add(user);
		user.getAssignments().add(assignment2);
		assignmentRepo.save(assignment2);
		AssignmentTask assignmentTask = new AssignmentTask(user,"Task 1",true,true, (short) 5, (short) 3,"This is an example task for an assignment",new Date(),new Date(),100,new Date(),true,false,assignment);
		assignmentTask.setFinished(new Date());
		assignmentTask.getUsers().add(user);
		assignmentTaskRepo.save(assignmentTask);
		StudySession studySession = new StudySession("Study session 1","\"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea .\"",new Date(), endDate,new Date(),false,"Charles Wilson Building",user);
		studySession.setAssignment(assignment);
		studySessionRepo.save(studySession);
		user.getStudySessions().add(studySession);

		MyUser user2 = new MyUser();
		user2.setEmail("jwm22@student.le.ac.uk");
		user2.setFirstName("Jan");
		user2.setSurname("Makarewicz");
		user2.setUsername("jwm22");
		user2.setPassword(pe.encode("password"));
		user2.getRoles().add(userRole);
		user2.getRoles().add(memberRole);
		accountSettings = new AccountSettings();
		accountSettingsRepo.save(accountSettings);
		user2.setAccountSettings(accountSettings);

		user.getFriends().add(user2);
		user2.getFriends().add(user);
		userRepo.save(user2);
		Conversation conversation = new Conversation();
		conversation.getParticipants().add(user);
		conversation.getParticipants().add(user2);
		conversationRepo.save(conversation);

		MyUser user3 = new MyUser();
		user3.setEmail("john@wick.com");
		user3.setFirstName("John");
		user3.setSurname("Wick");
		user3.setUsername("Baba Yaga");
		user3.setPassword(pe.encode("password"));
		user3.getRoles().add(userRole);
		user3.getRoles().add(memberRole);
		accountSettings = new AccountSettings();
		accountSettingsRepo.save(accountSettings);
		user3.setAccountSettings(accountSettings);
		userRepo.save(user3);

		user.getFriends().add(user3);
		user3.getFriends().add(user);
		conversation = new Conversation();
		conversation.getParticipants().add(user);
		conversation.getParticipants().add(user3);
		conversationRepo.save(conversation);
		userRepo.save(user3);

		MyUser user4 = new MyUser();
		user4.setEmail("jan@pawel.com");
		user4.setFirstName("Jan");
		user4.setSurname("Pawel");
		user4.setUsername("JanPaw");
		user4.setPassword(pe.encode("password"));
		user4.getRoles().add(userRole);
		accountSettings = new AccountSettings();
		accountSettingsRepo.save(accountSettings);
		user4.setAccountSettings(accountSettings);
		userRepo.save(user4);

		user.getFriends().add(user4);
		user4.getFriends().add(user);
		conversation = new Conversation();
		conversation.getParticipants().add(user);
		conversation.getParticipants().add(user4);
		conversationRepo.save(conversation);
		userRepo.save(user4);
		userRepo.save(user);

		List<MyUser> userList = new ArrayList<>();
		userList.add(user);
		userList.add(user2);
		userList.add(user3);
		GroupTask groupTask = new GroupTask("Building the Lego Death-Star", true,true, (short) 5, (short) 0,"Building the lego death-star with the boys",new Date(),endDate,0,new Date(),false,false,user,userList);
		groupTaskRepo.save(groupTask);
		groupTask.setUsers(userList);
		groupTaskRepo.save(groupTask);

		GroupChat groupChat = new GroupChat(groupTask.getTitle(),groupTask);
		groupChat.setParticipants(groupTask.getUsers());
		groupChatRepo.save(groupChat);
	}
}
