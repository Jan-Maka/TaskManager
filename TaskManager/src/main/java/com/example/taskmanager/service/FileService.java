package com.example.taskmanager.service;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.File.DTO.FileAttachmentDTO;
import com.example.taskmanager.domain.File.FileAttachment;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.repo.Assignment.AssignmentRepository;
import com.example.taskmanager.repo.Assignment.AssignmentTaskRepository;
import com.example.taskmanager.repo.File.FileRepository;
import com.example.taskmanager.repo.Task.GroupTaskRepository;
import com.example.taskmanager.repo.Task.TaskRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepo;

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private GroupTaskRepository groupTaskRepo;

    @Autowired
    private AssignmentRepository assignmentRepo;

    @Autowired
    private AssignmentTaskRepository assignmentTaskRepo;

    public void AttachFilesToTask(Task task, List<MultipartFile> files) throws IOException {
        for (MultipartFile file:files) {
            FileAttachment fileAttachment = new FileAttachment(file.getOriginalFilename(), file.getContentType(),file.getSize(),file.getBytes());
            fileAttachment.setTask(task);
            fileRepo.save(fileAttachment);
            task.getFileAttachments().add(fileAttachment);
        }
        taskRepo.save(task);
    }

    public void AttachFilesToGroupTask(GroupTask task, List<MultipartFile> files) throws IOException {
        for (MultipartFile file:files) {
            FileAttachment fileAttachment = new FileAttachment(file.getOriginalFilename(), file.getContentType(),file.getSize(),file.getBytes());
            fileAttachment.setGroupTask(task);
            fileRepo.save(fileAttachment);
            task.getFileAttachments().add(fileAttachment);
        }
        groupTaskRepo.save(task);
    }

    public void AttachFilesToAssignment(Assignment assignment, List<MultipartFile> files) throws IOException {
        for (MultipartFile file:files) {
            FileAttachment fileAttachment = new FileAttachment(file.getOriginalFilename(), file.getContentType(),file.getSize(),file.getBytes());
            fileAttachment.setAssignment(assignment);
            fileRepo.save(fileAttachment);
            assignment.getFileAttachments().add(fileAttachment);
        }
        assignmentRepo.save(assignment);
    }

    public void AttachFilesToAssignmentTask(AssignmentTask task, List<MultipartFile> files) throws IOException {
        for (MultipartFile file:files) {
            FileAttachment fileAttachment = new FileAttachment(file.getOriginalFilename(), file.getContentType(),file.getSize(),file.getBytes());
            fileAttachment.setAssignmentTask(task);
            fileRepo.save(fileAttachment);
            task.getFileAttachments().add(fileAttachment);
        }
        assignmentTaskRepo.save(task);
    }

    /**
     * Handles file downloading by sending information to browser to start the downlaod
     * @param id
     * @param response
     * @throws IOException
     */
    public void downloadFile(long id, HttpServletResponse response) throws IOException {
        Optional<FileAttachment> file = fileRepo.findById(id);
        if (file.isPresent()) {
            //Get ready for stream of data bytes
            response.setContentType("application/octet-stream");
            //What content to expect
            String headerKey = "Content-Disposition";
            //What the name of the downloaded file will be
            String headerValue = String.format("attachment; filename=\"%s\"", file.get().getTitle());
            response.setHeader(headerKey, headerValue);
            ServletOutputStream outputStream = response.getOutputStream();
            //Gives the data to the output stream to send to the user
            outputStream.write(file.get().getData());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Delets a file attachment via id and is removed from its associations
     * @param id
     */
    public void deleteFileAttachmentById(long id){
        FileAttachment file = fileRepo.findById(id).get();
        if (file.getTask() != null) {
            Task task = file.getTask();
            task.getFileAttachments().remove(file);
            taskRepo.save(task);
        }else if(file.getGroupTask() != null){
            GroupTask task = file.getGroupTask();
            task.getFileAttachments().remove(file);
            groupTaskRepo.save(task);
        }else if(file.getAssignment() != null){
            Assignment assignment = file.getAssignment();
            assignment.getFileAttachments().remove(file);
            assignmentRepo.save(assignment);
        }else if(file.getAssignmentTask() != null){
            AssignmentTask assignmentTask = file.getAssignmentTask();
            assignmentTask.getFileAttachments().remove(file);
            assignmentTaskRepo.save(assignmentTask);
        }
        fileRepo.delete(file);
    }

    /**
     * Deletes file attachment
     * @param fileAttachment
     */
    public void deleteFileAttachment(FileAttachment fileAttachment){
        fileRepo.delete(fileAttachment);
    }

    /**
     * Creates a file attachment DTO that is used within API calls
     * @param file
     * @return
     */
    public FileAttachmentDTO createFileAttachmentDTO(FileAttachment file){
        return new FileAttachmentDTO(file.getId(), file.getTitle(), file.getFileType(), file.getSize());
    }

    /**
     * Creates a file attachment entity and returns file attachment DTO
     * This method is mainly used for creating file message attachments
     * @param file
     * @return
     * @throws IOException
     */
    public FileAttachmentDTO createFileAttachment(MultipartFile file) throws IOException {
        FileAttachment fileAttachment = new FileAttachment(file.getOriginalFilename(), file.getContentType(), file.getSize(),file.getBytes());
        fileAttachment = fileRepo.save(fileAttachment);
        return createFileAttachmentDTO(fileAttachment);
    }

    /**
     * Gets file attachment by id
     * @return
     */
    public FileAttachment findFileAttachmentById(Long id){
        return fileRepo.findById(id).get();
    }

    /**
     * Checks the size of files submitted to be attached
     * @param files
     * @return
     */
    public long getFilesSize(List<MultipartFile> files){
        long size = 0;
        for (MultipartFile file: files) {
            size += file.getSize();
        }
        return size;
    }

    /**
     * Checks size of existing file attachments
     * @param files
     * @return
     */
    public long getFileSizeOfAttachedFiles(List<FileAttachment> files){
        long size = 0;
        for (FileAttachment file: files) {
            size += file.getSize();
        }
        return size;
    }

}
