package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.domain.User.PasswordResetToken;
import com.example.taskmanager.repo.User.PasswordResetTokenRepository;
import com.example.taskmanager.service.PasswordTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordTokenServiceTest {

    MyUser user;

    @InjectMocks
    PasswordTokenService passwordTokenService;

    @Mock
    PasswordResetTokenRepository passwordResetTokenRepo;


    @BeforeEach
    public void createUser() {
        this.user = new MyUser("foo@bar.com","foo", "bar", "password", "foofoo");
        this.user.setId(1L);
    }

    @DisplayName("Test Password Reset Token Has Valid UUID.")
    @Test
    public void testTokenGeneratesUuidPayload(){
        Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        when(passwordResetTokenRepo.findById(1L)).thenReturn(Optional.of(new PasswordResetToken(user)));
        Optional<PasswordResetToken> token = passwordResetTokenRepo.findById(1L);
        token.ifPresent(passwordResetToken ->
                assertTrue(UUID_REGEX.matcher(passwordResetToken.getToken()).matches()));
    }

    @DisplayName("Test Password Reset Token Is Attainable By Token.")
    @Test
    public void testGetTokenByTokenLoad(){
        String payload = UUID.randomUUID().toString();
        when(passwordResetTokenRepo.findByToken(payload)).thenReturn(new PasswordResetToken(user));
        PasswordResetToken token = passwordTokenService.getPasswordResetToken(payload);
        assertEquals(token.getClass(), PasswordResetToken.class);
    }

    @DisplayName("Test creating reset password token")
    @Test
    public void testCreatingPasswordResetToken(){
        PasswordResetToken test = new PasswordResetToken(user);
        when(passwordResetTokenRepo.save(any(PasswordResetToken.class))).thenReturn(test);
        PasswordResetToken result = passwordTokenService.createPasswordResetToken(user);
        assertEquals(test,result);
    }

    @DisplayName("Test password reset token exists by token load")
    @Test
    public void testIfTokenExistsByToken(){
        when(passwordResetTokenRepo.existsByToken(anyString())).thenReturn(true);
        assertTrue(passwordTokenService.resetTokenExistsByToken(anyString()));
    }

    @DisplayName("Test if a token is expired")
    @Test
    public void testIfTokenIsExpired(){
        PasswordResetToken test = new PasswordResetToken(user);
        when(passwordResetTokenRepo.findByToken(test.getToken())).thenReturn(test);
        assertTrue(!passwordTokenService.isTokenExpired(test.getToken()));
    }

    @DisplayName("Test getting user from token load")
    @Test
    public void testGetUserByToken(){
        PasswordResetToken test = new PasswordResetToken(user);
        when(passwordResetTokenRepo.findByToken(test.getToken())).thenReturn(test);
        assertEquals(user,passwordTokenService.getUserByToken(test.getToken()));
    }

    @DisplayName("Test deleting token")
    @Test
    public void testDeletingToken(){
        passwordTokenService.deleteTokenAfterUse(user.getId());
        verify(passwordResetTokenRepo,times(1)).deleteAllByUserId(user.getId());
    }
}
