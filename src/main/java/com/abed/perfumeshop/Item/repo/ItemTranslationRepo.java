package com.abed.perfumeshop.Item.repo;

import com.abed.perfumeshop.Item.entity.ItemTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemTranslationRepo extends JpaRepository<ItemTranslation, Long> {

    Optional<ItemTranslation> findByItemIdAndLocale(Long itemId, String locale);

    @Query("SELECT it FROM ItemTranslation it " +
            "WHERE it.item.id IN :itemIds AND it.locale = :locale")
    List<ItemTranslation> findByItemIdsAndLocale(@Param("itemIds") List<Long> itemIds, @Param("locale") String locale);

}
