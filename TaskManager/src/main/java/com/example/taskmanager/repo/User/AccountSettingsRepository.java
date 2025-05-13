package com.example.taskmanager.repo.User;

import com.example.taskmanager.domain.User.AccountSettings;
import org.springframework.data.repository.CrudRepository;

public interface AccountSettingsRepository extends CrudRepository<AccountSettings,Long> {
}
