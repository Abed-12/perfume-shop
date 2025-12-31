package com.abed.perfumeshop.Item.repo;

import com.abed.perfumeshop.Item.entity.PerfumeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfumeImageRepo extends JpaRepository<PerfumeImage, Long> {

    Optional<PerfumeImage> findByIdAndPerfumeId(Long imageId, Long perfumeId);

    int countByPerfumeId(Long perfumeId);

    List<PerfumeImage> findByPerfumeIdOrderByDisplayOrder(Long perfumeId);

    Optional<PerfumeImage> findByPerfumeIdAndIsPrimaryTrue(Long perfumeId);

    @Query("SELECT pi FROM PerfumeImage pi " +
            "WHERE pi.perfume.id IN :perfumeIds " +
            "AND pi.isPrimary = true")
    List<PerfumeImage> findPrimaryImagesByPerfumeIds(@Param("perfumeIds") List<Long> perfumeIds);

    @Query("SELECT COALESCE(MAX(pi.displayOrder), -1) FROM PerfumeImage pi" +
            " WHERE pi.perfume.id = :perfumeId")
    Integer findMaxDisplayOrderByPerfumeId(@Param("perfumeId") Long perfumeId);

}
