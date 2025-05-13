package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.Task.DTO.CategoryDTO;
import com.example.taskmanager.domain.Task.DTO.TaskDTO;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.domain.User.Role;
import com.example.taskmanager.repo.Task.CategoryRepository;
import com.example.taskmanager.repo.User.UserRepository;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class CategoryServiceTest {

    MyUser user;
    Category category;
    List<TaskDTO> taskDTOS;

    Role role1;
    Role role2;

    MockPrincipal mockPrincipal;

    @InjectMocks
    CategoryService categoryService;

    @Mock
    TaskService taskService;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    UserRepository userRepository;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
        this.user = new MyUser("foo@bar.com","foo", "bar","password", "foofoo");
        user.setId(1L);
        this.role1 = new Role("USER");
        this.role2 = new Role("MEMBER");
        List<Role> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        user.getRoles().addAll(roles);
        this.category = new Category("Work",user);
        category.setId(1L);
        user.getCategories().add(category);
        this.taskDTOS = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Task task = new Task(user,"test"+i,true,true,(short)2,(short) 3,"Desc",category,new Date(),new Date(),0,new Date(),false,false);
            TaskDTO taskDTO = new TaskDTO((long)i,user.getId(),"test"+i,true,true,(short)2,(short) 3,"Desc", category.getId(), category.getName(), new Date(),new Date(),0,new Date(),new Date(),false,false,false);
            taskDTOS.add(taskDTO);
            category.getTasks().add(task);
            Category cat = new Category("test"+i,user);
            cat.setId((long)i+1);
            user.getCategories().add(cat);
        }
        this.mockPrincipal = new MockPrincipal(user.getEmail());
    }

    @DisplayName("Test user is category owner")
    @Test
    public void testUserIsCategoryOwner(){
        assertTrue(categoryService.isOwner(category,mockPrincipal));
    }

    @DisplayName("Test if category with name exists for user")
    @Test
    public void testCategoryExistsForUser(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(categoryRepository.existsByUserAndName(user,"Work")).thenReturn(true);
        boolean result = categoryService.categoryExistsForUser("Work",mockPrincipal);
        verify(categoryRepository,times(1)).existsByUserAndName(user,"Work");
        assertTrue(result);
    }

    @DisplayName("Tests if category exists by id")
    @Test
    public void testCategoryExistsById(){
        when(categoryRepository.existsById(1L)).thenReturn(true);
        boolean result = categoryService.categoryExistsById(1L);
        verify(categoryRepository,times(1)).existsById(1L);
        assertTrue(result);
    }

    @DisplayName("Test getting category by id")
    @Test
    public void testGetCategoryById(){
        when(categoryRepository.findById(1L)).thenReturn(Optional.ofNullable(category));
        Category cat = categoryService.getCategoryById(1L);
        verify(categoryRepository,times(1)).findById(1L);
        assertEquals(category,cat);
    }

    @DisplayName("Test getting user categories")
    @Test
    public void testGetUserCategories(){
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        List<Category> result = categoryService.getUserCategories(mockPrincipal);
        assertTrue(result.size() == 6);
        assertTrue(result.containsAll(user.getCategories()));
    }

    @DisplayName("Test getting category tasks")
    @Test
    public void testGetCategoryTasks(){
        when(categoryRepository.findById(1L)).thenReturn(Optional.ofNullable(category));
        List<Task> result = categoryService.getCategoryTasks(1L,mockPrincipal);
        assertTrue(category.getTasks().containsAll(result));
    }

    @DisplayName("Test creating category DTO template")
    @Test
    public void testCreatingCategoryDTO(){
        when(taskService.getUserTasksDTO(category.getTasks())).thenReturn(taskDTOS);
        CategoryDTO result = categoryService.createCategoryDTO(category);
        assertTrue(result.getTasks().containsAll(taskDTOS));
        assertEquals(category.getId(),result.getId());
        assertEquals(category.getName(),result.getName());
        assertEquals(category.getUser().getId(),result.getUser_id());
    }

    @DisplayName("Test creating category")
    @Test
    public void testCreatingCategory() {
        Category newCat = new Category("Social", user);
        newCat.setId(7L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(categoryRepository.save(any(Category.class))).thenReturn(newCat);
        when(taskService.getUserTasksDTO(new ArrayList<>())).thenReturn(new ArrayList<>());
        CategoryDTO result = categoryService.createCategory("Social", mockPrincipal);
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertTrue(user.getCategories().contains(newCat));
        assertEquals(newCat.getId(), result.getId());
        assertEquals(newCat.getUser().getId(), result.getUser_id());
        assertEquals(newCat.getName(), result.getName());
    }

    @DisplayName("Test deleting category by id")
    @Test
    public void testDeletingCategoryById(){
        when(categoryRepository.findById(1L)).thenReturn(Optional.ofNullable(category));
        categoryService.deleteCategoryById(1L);
        verify(categoryRepository,times(1)).findById(1L);
        verify(categoryRepository,times(1)).delete(category);
    }

    private static class MockPrincipal implements java.security.Principal {
        private String name;

        public MockPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
