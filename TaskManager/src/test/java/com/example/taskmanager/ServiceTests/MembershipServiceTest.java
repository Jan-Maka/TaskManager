package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.domain.User.Role;
import com.example.taskmanager.repo.User.RoleRepository;
import com.example.taskmanager.repo.User.UserRepository;
import com.example.taskmanager.service.EmailService;
import com.example.taskmanager.service.MembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class MembershipServiceTest {
    MyUser user;
    Role role;

    @InjectMocks
    MembershipService membershipService;

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    EmailService emailService;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        this.role = new Role("MEMBER");
        this.user = new MyUser("foo@bar.com","foo", "bar","password", "foofoo");
        user.setId(1L);
    }

    @DisplayName("Test subscribing user and adding MEMBER role")
    @Test
    public void testSubscribeUser(){
        when(roleRepository.findByName("MEMBER")).thenReturn(role);
        when(emailService.subscriptionPaymentConfirmed(user)).thenReturn(anyString());
        membershipService.subscribeUser(user);
        verify(userRepository,times(1)).save(user);
        assertTrue(user.getRoles().contains(role));
        assertTrue(user.getSubscriptionStart() != null);
    }

}
