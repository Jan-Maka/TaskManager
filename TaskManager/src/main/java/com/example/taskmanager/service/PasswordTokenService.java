package com.example.taskmanager.service;

import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.domain.User.PasswordResetToken;
import com.example.taskmanager.repo.User.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
@Transactional
public class PasswordTokenService {
    @Autowired
    private PasswordResetTokenRepository tokenRepo;

    public PasswordResetToken createPasswordResetToken(MyUser user){
        PasswordResetToken token = new PasswordResetToken(user);
        return tokenRepo.save(token);
    }

    /**
     * Gets a reset token via its unique identifier
     * @param token
     * @return
     */
    public PasswordResetToken getPasswordResetToken(String token){
        return tokenRepo.findByToken(token);
    }

    /**
     * Checks if token exists in the database
     * @param token
     * @return
     */
    public boolean resetTokenExistsByToken(String token){
        return tokenRepo.existsByToken(token);
    }

    /**
     * Checks if the token has expired
     * @param token
     * @return
     */
    public boolean isTokenExpired(String token){
        final Calendar cal = Calendar.getInstance();
        return getPasswordResetToken(token).getExpiryDate().before(cal.getTime());
    }

    /**
     * Gets the user associated with token
     * @param token
     * @return
     */
    public MyUser getUserByToken(String token){
        return tokenRepo.findByToken(token).getUser();
    }

    /**
     * Deletes the token via id if it has been used
     * @param userId
     */
    public void deleteTokenAfterUse(long userId){
        tokenRepo.deleteAllByUserId(userId);
    }


}
