package com.example.taskmanager.repo.User;


import com.example.taskmanager.domain.User.Privilege;
import org.springframework.data.repository.CrudRepository;

public interface PrivilegeRepository extends CrudRepository<Privilege,Long> {
}
