package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.service.SchedulingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = {"purge.cron.expired-tokens=*/1 * * * * *","daily.cron.jobs=*/1 * * * * *","daily.cron.email=*/1 * * * * *"})
public class SchedulingServiceTest {

    @SpyBean
    private SchedulingService schedulingService;

    @DisplayName("Test scheduler triggers to delete expired tokens")
    @Test
    public void testPurgingOfTokensBeingTriggered(){
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(
                () -> verify(schedulingService, atLeastOnce()).PasswordResetTokenExpired());
    }

    @DisplayName("Test scheduler triggers to delete tasks older than 30 days")
    @Test
    public void testDeletionOfCompletedTasksIsTriggered(){
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(
                () -> verify(schedulingService, atLeastOnce()).deleteCompletedTasksOlderThan30days());
    }

    @DisplayName("Test scheduler triggers to remove users member role after 30 days")
    @Test
    public void testRemovingUserRolesIsTriggered(){
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(
                () -> verify(schedulingService, atLeastOnce()).removeUserMemberRoleIfStartOver30days());
    }

    @DisplayName("Test scheduler triggers to send reminder emails")
    @Test
    public void testSendingOfReminderEmailsIsTriggered(){
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(
                () -> verify(schedulingService, atLeastOnce()).emailReminders());
    }
}
