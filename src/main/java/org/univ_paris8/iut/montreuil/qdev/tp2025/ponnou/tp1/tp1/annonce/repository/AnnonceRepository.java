package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.Annonce;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.model.AnnonceStatus;

import java.util.Optional;

public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {

    @Query("SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category WHERE a.id = :id")
    Optional<Annonce> findByIdWithRelations(@Param("id") Long id);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Annonce> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Annonce> findAll(org.springframework.data.jpa.domain.Specification<Annonce> spec, Pageable pageable);

    @Query("SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category WHERE a.status = :status")
    Page<Annonce> findByStatusWithRelations(@Param("status") AnnonceStatus status, Pageable pageable);

    @Query("SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category WHERE a.author.id = :authorId")
    Page<Annonce> findByAuthorIdWithRelations(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category " +
            "WHERE a.status = :status AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Annonce> searchByKeyword(@Param("keyword") String keyword, @Param("status") AnnonceStatus status, Pageable pageable);

    @Query("SELECT a FROM Annonce a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.category " +
            "WHERE a.category.id = :categoryId AND a.status = :status")
    Page<Annonce> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") AnnonceStatus status, Pageable pageable);

    long countByStatus(AnnonceStatus status);

    long countByAuthorId(Long authorId);

    long countByCategoryIdAndStatus(Long categoryId, AnnonceStatus status);
}
