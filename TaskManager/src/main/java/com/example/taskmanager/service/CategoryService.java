package com.example.taskmanager.service;

import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.Task.DTO.CategoryDTO;
import com.example.taskmanager.domain.Task.DTO.TaskDTO;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.Task.CategoryRepository;
import com.example.taskmanager.repo.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository catRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TaskService taskService;

    /**
     * Checks if the current user is the owner of a category
     * @param category
     * @param principal
     * @return
     */
    public boolean isOwner(Category category, Principal principal){
        return category.getUser().getEmail().equals(principal.getName());
    }

    /**
     * Checks if a category of the same name already exists for a user
     * @param cat
     * @param principal
     * @return
     */
    public boolean categoryExistsForUser(String cat,Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        return catRepo.existsByUserAndName(user,cat);
    }

    /**
     * Checks if a category exists via id
     * @param id
     * @return
     */
    public boolean categoryExistsById(long id){
        return catRepo.existsById(id);
    }

    /**
     * Gets a category via its id
     * @param id
     * @return
     */
    public Category getCategoryById(long id){
        return catRepo.findById(id).get();
    }

    /**
     * Gets all of the users categories
     * @param principal
     * @return
     */
    public List<Category> getUserCategories(Principal principal){
        List<Category> categories = userRepo.findByEmail(principal.getName()).getCategories();
        return categories;
    }

    /**
     * Gets all tasks within a category
     * @param id
     * @param principal
     * @return
     */
    public List<Task> getCategoryTasks(Long id,Principal principal){
        Category category = getCategoryById(id);
        List<Task> tasks = category.getTasks().stream().filter(task -> !task.isArchive()).collect(Collectors.toList());
        Collections.reverse(tasks);
        return tasks;
    }

    /**
     * Used to create a category DTO based on the category object provided
     * @param cat
     * @return
     */
    public CategoryDTO createCategoryDTO(Category cat){
        CategoryDTO categoryDTO = new CategoryDTO(cat.getId(),cat.getName(),cat.getUser().getId());
        categoryDTO.getTasks().addAll(taskService.getUserTasksDTO(cat.getTasks()));
        return categoryDTO;
    }

    /**
     * Gets a list of category DTO's
     * @param categories
     * @return
     */
    public List<CategoryDTO> getUserCategoriesDTO(List<Category> categories){
        List<CategoryDTO> categoryDTOS = new ArrayList<>();
        for(Category cat: categories){
            CategoryDTO categoryDTO = createCategoryDTO(cat);
            categoryDTOS.add(categoryDTO);
        }
        return categoryDTOS;
    }

    /**
     * Creates a category with a certain name
     * @param cat
     * @param principal
     * @return
     */
    public CategoryDTO createCategory(String cat, Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        if(!user.hasRole("MEMBER") && user.getCategories().size() >= 4){
            return null;
        }
        Category category = new Category(cat,user);
        category = catRepo.save(category);
        user.getCategories().add(category);
        userRepo.save(user);
        return createCategoryDTO(category);
    }

    /**
     * Deletes a category via its id
     * @param id
     */
    public void deleteCategoryById(long id){
        Category category = getCategoryById(id);
        List<Task> tasks = category.getTasks();
        category.getTasks().clear(); // Clear the tasks from the category
        for (Task task : tasks) {
            task.getUser().getTasks().remove(task);
        }
        catRepo.delete(category);
    }
}
