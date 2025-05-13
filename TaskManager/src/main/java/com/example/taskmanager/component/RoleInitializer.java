package com.example.taskmanager.component;

import com.example.taskmanager.domain.User.Privilege;
import com.example.taskmanager.domain.User.Role;
import com.example.taskmanager.repo.User.PrivilegeRepository;
import com.example.taskmanager.repo.User.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
public class RoleInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PrivilegeRepository privilegeRepo;

    /**
     * On start up create all user roles
     * @param event the event to respond to
     */
    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(roleRepo.count() == 0){

            //Creating Privileges
            Privilege tasksPrivilege = new Privilege("TASK");
            Privilege categoryPrivilege = new Privilege("CATEGORY");
            Privilege fileAttachPrivilege = new Privilege("FILE_ATTACH");
            Privilege createUnlimitedCategoryPrivilege = new Privilege("CREATE_CATEGORY_UNLIMITED");
            Privilege archivePrivilege = new Privilege("ARCHIVE");
            Privilege groupTaskPrivilege = new Privilege("GROUP_TASK");
            Privilege fileAttach10MBPrivilege = new Privilege("FILE_5MB_ATTACH");
            Privilege assignmentPrivilege = new Privilege("ASSIGNMENT");
            Privilege createUnlimitedAssignments = new Privilege("CREATE_ASSIGNMENT_UNLIMITED");
            Privilege studySessionPrivilege = new Privilege("STUDY_SESSION");
            Privilege groupChatPrivilege = new Privilege("GROUP_CHAT");
            Privilege collaborationPrivilege = new Privilege("COLLABORATION");

            //Save all privileges
            privilegeRepo.saveAll(Arrays.asList(
                    tasksPrivilege,
                    categoryPrivilege,
                    fileAttachPrivilege,
                    fileAttach10MBPrivilege,
                    createUnlimitedCategoryPrivilege,
                    archivePrivilege,
                    groupTaskPrivilege,
                    createUnlimitedAssignments,
                    studySessionPrivilege,
                    groupChatPrivilege,
                    collaborationPrivilege
            ));

            //Create roles
            Role userRole = new Role("USER");
            userRole.setPrivileges(Arrays.asList(
                    tasksPrivilege,
                    categoryPrivilege,
                    fileAttachPrivilege,
                    assignmentPrivilege
            ));

            Role memberRole = new Role("MEMBER");
            memberRole.setPrivileges(Arrays.asList(
                    tasksPrivilege,
                    categoryPrivilege,
                    fileAttachPrivilege,
                    fileAttach10MBPrivilege,
                    createUnlimitedCategoryPrivilege,
                    archivePrivilege,
                    groupTaskPrivilege,
                    assignmentPrivilege,
                    createUnlimitedAssignments,
                    studySessionPrivilege,
                    groupChatPrivilege,
                    collaborationPrivilege
            ));
            //Save roles
            roleRepo.saveAll(Arrays.asList(userRole,memberRole));
        }
    }
}
