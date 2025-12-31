package com.abed.perfumeshop.Item.repo;

import com.abed.perfumeshop.Item.entity.ItemPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemPriceRepo extends JpaRepository<ItemPrice, Long> {

    @Query("SELECT ip FROM ItemPrice ip " +
            "WHERE ip.item.id = :itemId AND ip.isActive = true " +
            "ORDER BY ip.effectiveFrom DESC")
    Optional<ItemPrice> findCurrentActivePriceByItemId(@Param("itemId") Long itemId);

    @Query("SELECT ip FROM ItemPrice ip " +
            "WHERE ip.item.id IN :itemIds AND ip.isActive = true " +
            "ORDER BY ip.effectiveFrom DESC")
    List<ItemPrice> findCurrentActivePricesByItemIds(@Param("itemIds") List<Long> itemIds);

}
