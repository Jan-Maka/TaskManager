package com.example.taskmanager.repo.User;

import com.example.taskmanager.domain.User.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role,String> {
    Role findByName(String name);
}
