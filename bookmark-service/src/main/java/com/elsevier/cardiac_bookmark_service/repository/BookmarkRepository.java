package com.elsevier.cardiac_bookmark_service.repository;

import com.elsevier.cardiac_bookmark_service.document.Bookmark;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends MongoRepository<Bookmark, String> {

    List<Bookmark> findByUserId(String userId);

    Optional<Bookmark> findByUserIdAndDiagnosisId(String userId, String diagnosisId);

    Optional<Bookmark> findByIdAndUserId(String id, String userId);
}
