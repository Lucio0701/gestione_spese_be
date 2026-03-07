package com.gestionespese.service;

import com.gestionespese.dto.document.*;
import com.gestionespese.model.Document;
import com.gestionespese.model.User;
import com.gestionespese.repository.DocumentRepository;
import com.gestionespese.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    public PagedDocuments getDocuments(String email, int page, int size) {
        User user = userRepository.findByEmail(email).orElseThrow();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Document> docPage = documentRepository.findByUserId(user.getId(), pageRequest);

        List<DocumentDto> dtos = docPage.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
        return new PagedDocuments(dtos, page, size, docPage.getTotalElements(), docPage.getTotalPages());
    }

    public DocumentDto uploadDocument(String email, MultipartFile file, String type, LocalDate dueDate, String notes) {
        User user = userRepository.findByEmail(email).orElseThrow();

        // TODO: Save file to S3 or local storage. For now, just simulated.
        String fileUrl = "http://localhost:8080/files/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        Document document = new Document(
                user,
                file.getOriginalFilename(),
                fileUrl,
                type,
                dueDate,
                notes);
        return mapToDto(documentRepository.save(document));
    }

    public void deleteDocument(String id) {
        documentRepository.deleteById(id);
    }

    public DocumentDto getDocument(String id) {
        return mapToDto(documentRepository.findById(id).orElseThrow());
    }

    private DocumentDto mapToDto(Document d) {
        return new DocumentDto(
                d.getId(),
                d.getUser().getId().toString(),
                d.getFileUrl(),
                d.getType(),
                d.getExtractedText(),
                d.getDueDate(),
                null // CreatedAt
        );
    }
}
