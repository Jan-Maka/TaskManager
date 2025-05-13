package com.example.taskmanager.repo.File;

import com.example.taskmanager.domain.File.FileAttachment;
import org.springframework.data.repository.CrudRepository;

public interface FileRepository extends CrudRepository<FileAttachment,Long> {
}
