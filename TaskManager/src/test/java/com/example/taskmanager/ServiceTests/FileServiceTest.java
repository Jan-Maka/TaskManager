package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.File.DTO.FileAttachmentDTO;
import com.example.taskmanager.domain.File.FileAttachment;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.Assignment.AssignmentRepository;
import com.example.taskmanager.repo.Assignment.AssignmentTaskRepository;
import com.example.taskmanager.repo.File.FileRepository;
import com.example.taskmanager.repo.Task.GroupTaskRepository;
import com.example.taskmanager.repo.Task.TaskRepository;
import com.example.taskmanager.service.FileService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class FileServiceTest {
    MyUser user;

    FileAttachment fileAttachment;
    MockMultipartFile file;
    FileAttachmentDTO fileAttachmentDTO;
    List<MultipartFile> files = new ArrayList<>();

    @BeforeEach
    public void createUser() throws IOException {
        this.user = new MyUser("foo@bar.com","foo", "bar", "password", "foofoo");
        this.user.setId(1L);
        this.file = new MockMultipartFile("file","test-file.pdf","application/pdf","File test".getBytes());
        this.fileAttachment = new FileAttachment(file.getOriginalFilename(),file.getContentType(),file.getSize(), file.getBytes());
        fileAttachment.setId(1L);
        this.fileAttachmentDTO = new FileAttachmentDTO(fileAttachment.getId(),fileAttachment.getTitle(),fileAttachment.getFileType(),fileAttachment.getSize());
        for (int i = 0; i < 5; i++) {
            MockMultipartFile mockFile = new MockMultipartFile("file","test-file.pdf","application/pdf","File test".getBytes());
            this.files.add(mockFile);
        }
    }

    @InjectMocks
    FileService fileService;

    @Mock
    FileRepository fileRepo;

    @Mock
    TaskRepository taskRepository;

    @Mock
    GroupTaskRepository groupTaskRepository;

    @Mock
    AssignmentRepository assignmentRepository;

    @Mock
    AssignmentTaskRepository assignmentTaskRepository;

    @Mock
    HttpServletResponse response;
    @Mock
    ServletOutputStream outputStream;

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("Test creating file attachment")
    public void testCreatingFileAttachment() throws IOException {
        when(fileRepo.save(any(FileAttachment.class))).thenReturn(fileAttachment);
        FileAttachmentDTO fileDTO = fileService.createFileAttachment(file);
        verify(fileRepo,times(1)).save(any(FileAttachment.class));
        assertEquals(fileAttachmentDTO.getId(),fileDTO.getId());
        assertEquals(fileAttachmentDTO.getFileType(),fileDTO.getFileType());
        assertEquals(fileAttachmentDTO.getSize(),fileDTO.getSize());
        assertEquals(fileAttachmentDTO.getTitle(),fileDTO.getTitle());
    }

    @DisplayName("Test creating file attachment DTO")
    @Test
    public void  testCreateFileAttachmentDTO(){
        FileAttachmentDTO result = fileService.createFileAttachmentDTO(fileAttachment);
        assertEquals(fileAttachmentDTO.getId(),result.getId());
        assertEquals(fileAttachmentDTO.getFileType(),result.getFileType());
        assertEquals(fileAttachmentDTO.getSize(),result.getSize());
        assertEquals(fileAttachmentDTO.getTitle(),result.getTitle());
    }

    @DisplayName("Test attaching files to task")
    @Test
    public void testAttachingFilesToTask() throws IOException {
        Task test = new Task();
        when(taskRepository.save(test)).thenReturn(test);
        fileService.AttachFilesToTask(test,files);
        verify(fileRepo,times(5)).save(any(FileAttachment.class));
        verify(taskRepository,times(1)).save(test);
        assertTrue(test.getFileAttachments().size() == 5);
        for (int i = 0; i < 5; i++) {
            assertEquals(files.get(i).getOriginalFilename(),test.getFileAttachments().get(i).getTitle());
            assertEquals(files.get(i).getContentType(),test.getFileAttachments().get(i).getFileType());
            assertEquals(files.get(i).getBytes(),test.getFileAttachments().get(i).getData());
            assertEquals(files.get(i).getSize(),test.getFileAttachments().get(i).getSize());
        }
    }

    @DisplayName("Test attaching files to group task")
    @Test
    public void testAttachingFilesToGroupTask() throws IOException {
        GroupTask test = new GroupTask();
        when(groupTaskRepository.save(test)).thenReturn(test);
        fileService.AttachFilesToGroupTask(test,files);
        verify(fileRepo,times(5)).save(any(FileAttachment.class));
        verify(groupTaskRepository,times(1)).save(test);
        assertTrue(test.getFileAttachments().size() == 5);
        for (int i = 0; i < 5; i++) {
            assertEquals(files.get(i).getOriginalFilename(),test.getFileAttachments().get(i).getTitle());
            assertEquals(files.get(i).getContentType(),test.getFileAttachments().get(i).getFileType());
            assertEquals(files.get(i).getBytes(),test.getFileAttachments().get(i).getData());
            assertEquals(files.get(i).getSize(),test.getFileAttachments().get(i).getSize());
        }
    }

    @DisplayName("Test attaching files to assignment")
    @Test
    public void testAttachingFilesToAssignment() throws IOException {
        Assignment test = new Assignment();
        when(assignmentRepository.save(test)).thenReturn(test);
        fileService.AttachFilesToAssignment(test,files);
        verify(fileRepo,times(5)).save(any(FileAttachment.class));
        verify(assignmentRepository,times(1)).save(test);
        assertTrue(test.getFileAttachments().size() == 5);
        for (int i = 0; i < 5; i++) {
            assertEquals(files.get(i).getOriginalFilename(),test.getFileAttachments().get(i).getTitle());
            assertEquals(files.get(i).getContentType(),test.getFileAttachments().get(i).getFileType());
            assertEquals(files.get(i).getBytes(),test.getFileAttachments().get(i).getData());
            assertEquals(files.get(i).getSize(),test.getFileAttachments().get(i).getSize());
        }
    }

    @DisplayName("Test attaching files to assignment task")
    @Test
    public void testAttachingFilesToAssignmentTask() throws IOException {
        AssignmentTask test = new AssignmentTask();
        when(assignmentTaskRepository.save(test)).thenReturn(test);
        fileService.AttachFilesToAssignmentTask(test,files);
        verify(fileRepo,times(5)).save(any(FileAttachment.class));
        verify(assignmentTaskRepository,times(1)).save(test);
        assertTrue(test.getFileAttachments().size() == 5);
        for (int i = 0; i < 5; i++) {
            assertEquals(files.get(i).getOriginalFilename(),test.getFileAttachments().get(i).getTitle());
            assertEquals(files.get(i).getContentType(),test.getFileAttachments().get(i).getFileType());
            assertEquals(files.get(i).getBytes(),test.getFileAttachments().get(i).getData());
            assertEquals(files.get(i).getSize(),test.getFileAttachments().get(i).getSize());
        }
    }

    @DisplayName("Test deleting file attachment by id")
    @Test
    public void testDeletingFileAttachmentById(){
        Task test = new Task();
        test.getFileAttachments().add(fileAttachment);
        fileAttachment.setTask(test);
        when(fileRepo.findById(1L)).thenReturn(Optional.ofNullable(fileAttachment));
        when(taskRepository.save(test)).thenReturn(test);
        fileService.deleteFileAttachmentById(1L);
        verify(taskRepository,times(1)).save(test);
        verify(fileRepo,times(1)).delete(fileAttachment);
        assertTrue(!test.getFileAttachments().contains(fileAttachment));
    }

    @DisplayName("Test deleting file attachment")
    @Test
    public void testDeletingFileAttachment(){
        fileService.deleteFileAttachment(fileAttachment);
        verify(fileRepo,times(1)).delete(fileAttachment);
    }

    @DisplayName("Test getting file attachment by id")
    @Test
    public void testGetFileAttachmentById(){
        when(fileRepo.findById(1L)).thenReturn(Optional.ofNullable(fileAttachment));
        FileAttachment result = fileService.findFileAttachmentById(1L);
        verify(fileRepo,times(1)).findById(1L);
        assertEquals(fileAttachment,result);
    }

    @DisplayName("Test getting total file size of files")
    @Test
    public void testGetFilesSize(){
        long size = 0;
        for (MultipartFile f:files) {
            size += f.getSize();
        }
        long result = fileService.getFilesSize(files);
        assertEquals(size,result);
    }

    @DisplayName("Test getting total file size of attachments")
    @Test
    public void testGetFileSizeOfAttachedFiles() throws IOException {
        List<FileAttachment> testList = new ArrayList<>();
        long size = 0;
        for (MultipartFile f:files) {
            testList.add(new FileAttachment(f.getOriginalFilename(),f.getContentType(),f.getSize(),f.getBytes()));
            size += f.getSize();
        }
        long result = fileService.getFileSizeOfAttachedFiles(testList);
        assertEquals(size,result);
    }

    @DisplayName("Test downloading of file attachment by id")
    @Test
    public void testDownloadingOfFileById() throws IOException {
        when(fileRepo.findById(1L)).thenReturn(Optional.ofNullable(fileAttachment));
        when(response.getOutputStream()).thenReturn(outputStream);
        fileService.downloadFile(1L, response);
        ArgumentCaptor<String> headerKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(headerKeyCaptor.capture(), headerValueCaptor.capture());
        assertEquals("Content-Disposition", headerKeyCaptor.getValue());
        assertEquals(String.format("attachment; filename=\"%s\"", fileAttachment.getTitle()), headerValueCaptor.getValue());

        // Verify file data is written to the output stream
        verify(outputStream).write(fileAttachment.getData());
    }



}
