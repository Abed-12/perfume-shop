package com.abed.perfumeshop.Item.repo;

import com.abed.perfumeshop.Item.entity.ItemPrice;
import com.abed.perfumeshop.common.enums.PerfumeSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemPriceRepo extends JpaRepository<ItemPrice, Long> {

    @Query("SELECT ip FROM ItemPrice ip " +
            "WHERE ip.item.id IN :itemIds AND ip.isActive = true " +
            "AND ip.price = (" +
            "  SELECT MIN(ip2.price) FROM ItemPrice ip2 " +
            "  WHERE ip2.item.id = ip.item.id " +
            "  AND ip2.isActive = true" +
            ")")
    List<ItemPrice> findCurrentActivePricesByItemIds(@Param("itemIds") List<Long> itemIds);

    List<ItemPrice> findByItemIdAndIsActiveTrue(Long itemId);

    Optional<ItemPrice> findByItemIdAndPerfumeSizeAndIsActiveTrue(Long itemId, PerfumeSize perfumeSize);

    Optional<ItemPrice> findFirstByItemIdAndPerfumeSizeOrderByEffectiveFromDesc(Long itemId, PerfumeSize perfumeSize);
}
