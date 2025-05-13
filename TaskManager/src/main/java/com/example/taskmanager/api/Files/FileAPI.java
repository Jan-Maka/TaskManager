package com.example.taskmanager.api.Files;

import com.example.taskmanager.domain.File.DTO.FileAttachmentDTO;
import com.example.taskmanager.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileAPI {

    @Autowired
    private FileService fileService;

    /**
     * Helps creating a file attachment
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/create")
    public ResponseEntity<?> createFileAttachment(@RequestPart MultipartFile file) throws IOException {
        FileAttachmentDTO fileAttachmentDTO = fileService.createFileAttachment(file);
        return new ResponseEntity<>(fileAttachmentDTO,HttpStatus.CREATED);
    }

    /**
     * Deletes a file attachment via id
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFileAttachment(@PathVariable long id){
        fileService.deleteFileAttachmentById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Downloads the file into the user download directory from browser
     * @param id
     * @param response
     * @throws IOException
     */
    @GetMapping("/{id}/download")
    public void downloadFile(@PathVariable long id, HttpServletResponse response) throws IOException {
        fileService.downloadFile(id,response);
    }
}
