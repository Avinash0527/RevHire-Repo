package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.model.JobSeekerProfile;
import com.example.revhirehiringplatform.model.ResumeFiles;
import com.example.revhirehiringplatform.repository.ResumeFilesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSeekerResumeService {

    private final ResumeFilesRepository resumeFilesRepository;
    private final Path fileStorageLocation = Paths.get("uploads/resumes").toAbsolutePath().normalize();

    public void init() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public ResumeFiles getResumeFile(Long profileId) {
        List<ResumeFiles> files = resumeFilesRepository.findByJobSeekerId(profileId);
        if (files != null && !files.isEmpty()) {
            return files.get(0);
        }
        return null;
    }

    public ResumeFiles storeFile(MultipartFile file, JobSeekerProfile profile) {
        try {
            if (Files.notExists(fileStorageLocation)) {
                init();
            }
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            ResumeFiles resumeFile = new ResumeFiles();
            resumeFile.setJobSeeker(profile);
            resumeFile.setFileName(originalFileName);
            resumeFile.setFileType(fileExtension.replace(".", "").toUpperCase());
            resumeFile.setFileSize(file.getSize());
            resumeFile.setFilePath(fileName);
            resumeFile.setActive(true);

            return resumeFilesRepository.save(resumeFile);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }
}
