package com.abed.perfumeshop.Item.repo;

import com.abed.perfumeshop.Item.entity.Perfume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfumeRepo extends JpaRepository<Perfume, Long> {

    Page<Perfume> findByItem_ActiveTrue(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Perfume p " +
            "LEFT JOIN ItemTranslation t ON t.item = p.item " +
            "WHERE p.item.active = true " +
            "AND (" +
            "    LOWER(p.item.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "    OR LOWER(p.item.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "    OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            ")")
    Page<Perfume> searchPerfumes(@Param("keyword") String keyword, Pageable pageable);

}
