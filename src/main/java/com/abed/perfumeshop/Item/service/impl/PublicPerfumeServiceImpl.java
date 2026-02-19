package com.abed.perfumeshop.Item.service.impl;

import com.abed.perfumeshop.Item.dto.response.PerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.response.PerfumeDetailDTO;
import com.abed.perfumeshop.Item.entity.*;
import com.abed.perfumeshop.Item.repo.ItemPriceRepo;
import com.abed.perfumeshop.Item.repo.ItemTranslationRepo;
import com.abed.perfumeshop.Item.repo.PerfumeImageRepo;
import com.abed.perfumeshop.Item.repo.PerfumeRepo;
import com.abed.perfumeshop.Item.service.PublicPerfumeService;
import com.abed.perfumeshop.common.dto.response.PageResponse;
import com.abed.perfumeshop.common.enums.PerfumeSeason;
import com.abed.perfumeshop.common.enums.PerfumeType;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicPerfumeServiceImpl implements PublicPerfumeService {

    private static final String BASE_IMAGE_URL = "/public/perfumes";

    private final PerfumeRepo perfumeRepo;
    private final ItemPriceRepo itemPriceRepo;
    private final ItemTranslationRepo itemTranslationRepo;
    private final PerfumeImageRepo perfumeImageRepo;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PerfumeCardDTO> getActivePerfumes(
            int page,
            int size,
            PerfumeType perfumeType,
            PerfumeSeason perfumeSeason
    ) {
        // Convert PerfumeSeason to String (or null)
        String perfumeSeasonString = perfumeSeason != null ? perfumeSeason.name() : null;
        Page<Perfume> perfumesPage = perfumeRepo.findActiveWithFilters(perfumeType, perfumeSeasonString, PageRequest.of(page, size));

        return buildPerfumeCardResponse(perfumesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PerfumeDetailDTO getPerfumeById(Long perfumeId) {
        Perfume perfume = perfumeRepo.findById(perfumeId)
                .orElseThrow( () -> new NotFoundException("perfume.not.found"));

        Item item = perfume.getItem();

        List<String> imageUrls = perfumeImageRepo.findByPerfumeIdOrderByDisplayOrder(perfume.getId())
                .stream()
                .map(image -> BASE_IMAGE_URL + "/" + perfume.getId() + "/images/" + image.getId() + "?v=" + image.getUpdatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .toList();

        String primaryImageUrl = perfumeImageRepo.findByPerfumeIdAndIsPrimaryTrue(perfume.getId())
                .map(image -> BASE_IMAGE_URL + "/" + perfume.getId() + "/images/" + image.getId() + "?v=" + image.getUpdatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .orElse(null);

        // Get all available sizes with prices
        List<PerfumeDetailDTO.SizeOptionDTO> availableSizes = itemPriceRepo
                .findByItemIdAndIsActiveTrue(item.getId())
                .stream()
                .map(itemPrice -> PerfumeDetailDTO.SizeOptionDTO.builder()
                        .size(itemPrice.getPerfumeSize())
                        .price(itemPrice.getPrice())
                        .quantity(itemPrice.getQuantity())
                        .available(itemPrice.getQuantity() > 0)
                        .build())
                .toList();

        // Get translation
        List<ItemTranslation> translations = itemTranslationRepo.findByItemIds(List.of(item.getId()));
        Map<String, String> namesByLocale = new HashMap<>();
        Map<String, String> descriptionsByLocale = new HashMap<>();

        translations.forEach(t -> {
            namesByLocale.put(t.getLocale(), t.getName());
            descriptionsByLocale.put(t.getLocale(), t.getDescription());
        });

        // Convert comma-separated perfume seasons to localized string
        List<PerfumeSeason> localizedSeasons = Arrays.stream(perfume.getPerfumeSeasons().split(","))
                .map(String::trim)
                .map(PerfumeSeason::valueOf)
                .collect(Collectors.toList());

        return PerfumeDetailDTO.builder()
                .id(perfume.getId())
                .name(item.getName())
                .brand(item.getBrand())
                .active(item.getActive())
                .translatedName(namesByLocale)
                .description(descriptionsByLocale)
                .perfumeType(perfume.getPerfumeType())
                .perfumeSeason(localizedSeasons)
                .primaryImageUrl(primaryImageUrl)
                .imageUrls(imageUrls)
                .availableSizes(availableSizes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PerfumeCardDTO> searchPerfumes(int page, int size, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("perfume.search.keyword.required");
        }

        if (keyword.trim().length() < 2) {
            throw new ValidationException("perfume.search.keyword.too.short");
        }

        Page<Perfume> perfumesPage = perfumeRepo.searchActivePerfumes(keyword, PageRequest.of(page, size));

        return buildPerfumeCardResponse(perfumesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PerfumeImage getImageById(Long perfumeId, Long imageId) {
        return perfumeImageRepo.findByIdAndPerfumeId(imageId, perfumeId)
                .orElseThrow(() -> new NotFoundException("image.not.found"));
    }

    // ========== Private Helper Methods ==========
    private PageResponse<PerfumeCardDTO> buildPerfumeCardResponse(Page<Perfume> perfumesPage) {
        List<Perfume> perfumes = perfumesPage.getContent();

        List<Long> perfumeIds = perfumes.stream()
                .map(Perfume::getId)
                .toList();

        List<Long> itemIds = perfumes.stream()
                .map(p -> p.getItem().getId())
                .toList();

        List<PerfumeImage> primaryImages = perfumeImageRepo.findPrimaryImagesByPerfumeIds(perfumeIds);
        List<ItemPrice> allPrices = itemPriceRepo.findCurrentActivePricesByItemIds(itemIds);
        List<ItemTranslation> allTranslations = itemTranslationRepo.findByItemIds(itemIds);

        Map<Long, PerfumeImage> primaryImageByPerfume = primaryImages.stream()
                .collect(Collectors.toMap(
                        img -> img.getPerfume().getId(),
                        img -> img
                ));

        Map<Long, ItemPrice> pricesByItem = allPrices.stream()
                .collect(Collectors.toMap(
                        itemPrice -> itemPrice.getItem().getId(),
                        p -> p
                ));

        Map<Long, Map<String, String>> translationsByItem = allTranslations.stream()
                .collect(Collectors.groupingBy(
                        itemTranslation -> itemTranslation.getItem().getId(),
                        Collectors.toMap(
                                ItemTranslation::getLocale,
                                ItemTranslation::getName
                        )
                ));

        List<PerfumeCardDTO> perfumeCards = perfumes.stream()
                .map(perfume -> mapToPerfumeCard(
                        perfume,
                        primaryImageByPerfume.get(perfume.getId()),
                        pricesByItem.get(perfume.getItem().getId()),
                        translationsByItem.get(perfume.getItem().getId())
                ))
                .toList();

        return PageResponse.<PerfumeCardDTO>builder()
                .content(perfumeCards)
                .page(PageResponse.PageInfo.builder()
                        .size(perfumesPage.getSize())
                        .number(perfumesPage.getNumber())
                        .totalElements(perfumesPage.getTotalElements())
                        .totalPages(perfumesPage.getTotalPages())
                        .build())
                .build();
    }

    private PerfumeCardDTO mapToPerfumeCard(
            Perfume perfume,
            PerfumeImage primaryImage,
            ItemPrice lowestPrice,
            Map<String, String> translations
    ) {
        Item item = perfume.getItem();

        if (primaryImage == null) {
            throw new NotFoundException("image.not.found");
        }

        return PerfumeCardDTO.builder()
                .id(perfume.getId())
                .name(item.getName())
                .brand(item.getBrand())
                .active(item.getActive())
                .lowestPrice(lowestPrice.getPrice())
                .translatedName(translations)
                .primaryImageUrl(BASE_IMAGE_URL + "/" + perfume.getId() + "/images/" + primaryImage.getId() + "?v=" + primaryImage.getUpdatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
    }

}
