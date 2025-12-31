package com.abed.perfumeshop.Item.repo;

import com.abed.perfumeshop.Item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepo extends JpaRepository<Item,Long> {

    boolean existsByNameAndBrand(String name, String brand);

    boolean existsByNameAndBrandAndIdNot(String name, String brand, Long excludedItemId);

}
