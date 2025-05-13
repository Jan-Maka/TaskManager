package com.example.taskmanager.repo.User;

import com.example.taskmanager.domain.User.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken,Long> {
    PasswordResetToken findByToken(String token);
    void deleteAllByUserId(long id);

    boolean existsByToken(String token);

    void deleteAllByExpiryDateBefore(Date date);
}
