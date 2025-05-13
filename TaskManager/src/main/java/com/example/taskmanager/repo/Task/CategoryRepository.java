package com.example.taskmanager.repo.Task;

import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.User.MyUser;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Long> {
    boolean existsByUserAndName(MyUser user, String name);
    Category findByName(String name);
}
