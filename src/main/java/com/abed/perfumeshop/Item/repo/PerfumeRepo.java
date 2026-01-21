package com.abed.perfumeshop.Item.repo;

import com.abed.perfumeshop.Item.entity.Perfume;
import com.abed.perfumeshop.common.enums.PerfumeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerfumeRepo extends JpaRepository<Perfume, Long> {

    @Query("SELECT p FROM Perfume p " +
            "WHERE p.item.active = true " +
            "AND (:perfumeType IS NULL OR " +
            "     p.perfumeType = :perfumeType OR " +
            "     (p.perfumeType = 'UNISEX' AND :perfumeType IN ('MALE', 'FEMALE'))) " +
            "AND (COALESCE(:perfumeSeason, '') = '' OR CONCAT(',', p.perfumeSeasons, ',') LIKE CONCAT('%,', :perfumeSeason, ',%'))")
    Page<Perfume> findActiveWithFilters(
            @Param("perfumeType") PerfumeType perfumeType,
            @Param("perfumeSeason") String perfumeSeason,
            Pageable pageable
    );

    @Query("SELECT p FROM Perfume p " +
            "WHERE p.item.id IN :itemIds")
    List<Perfume> findByItemIds(@Param("itemIds") List<Long> itemIds);

    @Query("SELECT DISTINCT p FROM Perfume p " +
            "LEFT JOIN ItemTranslation t ON t.item = p.item " +
            "WHERE p.item.active = true " +
            "AND (" +
            "    LOWER(p.item.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "    OR LOWER(p.item.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "    OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            ")")
    Page<Perfume> searchActivePerfumes(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Perfume p " +
            "WHERE (:perfumeType IS NULL OR p.perfumeType = :perfumeType) " +
            "AND (COALESCE(:perfumeSeason, '') = '' OR  CONCAT(',', p.perfumeSeasons, ',') LIKE CONCAT('%,', :perfumeSeason, ',%'))")
    Page<Perfume> findAllWithFilters(
            @Param("perfumeType") PerfumeType perfumeType,
            @Param("perfumeSeason") String perfumeSeason,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Perfume p " +
            "LEFT JOIN ItemTranslation t ON t.item = p.item " +
            "WHERE " +
            "    LOWER(p.item.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "    OR LOWER(p.item.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "    OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Perfume> searchAllPerfumes(@Param("keyword") String keyword, Pageable pageable);

}
