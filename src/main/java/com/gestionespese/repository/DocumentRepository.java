package com.gestionespese.repository;

import com.gestionespese.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, String> {
    Page<Document> findByUserId(Long userId, Pageable pageable);
}
