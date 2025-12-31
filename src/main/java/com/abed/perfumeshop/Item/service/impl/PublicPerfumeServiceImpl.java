package com.abed.perfumeshop.Item.service.impl;

import com.abed.perfumeshop.Item.dto.PerfumeCardDTO;
import com.abed.perfumeshop.Item.dto.PerfumeDetailDTO;
import com.abed.perfumeshop.Item.entity.*;
import com.abed.perfumeshop.Item.repo.ItemPriceRepo;
import com.abed.perfumeshop.Item.repo.ItemTranslationRepo;
import com.abed.perfumeshop.Item.repo.PerfumeImageRepo;
import com.abed.perfumeshop.Item.repo.PerfumeRepo;
import com.abed.perfumeshop.Item.service.PublicPerfumeService;
import com.abed.perfumeshop.common.dto.PageResponse;
import com.abed.perfumeshop.common.exception.NotFoundException;
import com.abed.perfumeshop.common.exception.ValidationException;
import com.abed.perfumeshop.common.res.Response;
import com.abed.perfumeshop.common.service.EnumLocalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicPerfumeServiceImpl implements PublicPerfumeService {

    private static final String BASE_IMAGE_URL = "/api/public/perfumes";

    private final PerfumeRepo perfumeRepo;
    private final ItemPriceRepo itemPriceRepo;
    private final ItemTranslationRepo itemTranslationRepo;
    private final PerfumeImageRepo perfumeImageRepo;
    private final EnumLocalizationService enumLocalizationService;

    @Override
    @Transactional(readOnly = true)
    public Response<PageResponse<PerfumeCardDTO>> getAllPerfumes(int page, int size) {
        Page<Perfume> perfumesPage = perfumeRepo.findByItem_ActiveTrue(PageRequest.of(page, size));
        return buildPerfumeCardResponse(perfumesPage, "perfumes.retrieved");
    }

    @Override
    @Transactional(readOnly = true)
    public Response<PerfumeDetailDTO> getPerfumeById(Long id) {
        Perfume perfume = perfumeRepo.findById(id)
                .orElseThrow( () -> new NotFoundException("perfume.not.found"));

        Item item = perfume.getItem();

        List<String> imageUrls = perfumeImageRepo.findByPerfumeIdOrderByDisplayOrder(perfume.getId())
                .stream()
                .map(image -> BASE_IMAGE_URL + "/" + perfume.getId() + "/images/" + image.getId())
                .toList();

        PerfumeDetailDTO.PerfumeDetailDTOBuilder detailBuilder = PerfumeDetailDTO.builder()
                .id(perfume.getId())
                .name(item.getName())
                .quantity(item.getQuantity())
                .brand(item.getBrand())
                .active(item.getActive())
                .size(enumLocalizationService.getLocalizedName(perfume.getPerfumeSize()))
                .perfumeType(enumLocalizationService.getLocalizedName(perfume.getPerfumeType()))
                .perfumeSeason(enumLocalizationService.getLocalizedName(perfume.getPerfumeSeason()))
                .imageUrls(imageUrls);

        itemPriceRepo.findCurrentActivePriceByItemId(item.getId())
                .ifPresent(price -> detailBuilder.currentPrice(price.getPrice()));

        itemTranslationRepo.findByItemIdAndLocale(item.getId(), LocaleContextHolder.getLocale().getLanguage())
                .ifPresent(translation -> {
                    detailBuilder.translatedName(translation.getName());
                    detailBuilder.description(translation.getDescription());
                });

        PerfumeDetailDTO perfumeDetail = detailBuilder.build();

        return Response.<PerfumeDetailDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("perfume.retrieved")
                .data(perfumeDetail)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<PageResponse<PerfumeCardDTO>> searchPerfumes(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("perfume.search.keyword.required");
        }

        if (keyword.trim().length() < 2) {
            throw new ValidationException("perfume.search.keyword.too.short");
        }

        Page<Perfume> perfumesPage = perfumeRepo.searchPerfumes(keyword, PageRequest.of(page, size));
        return buildPerfumeCardResponse(perfumesPage, "perfumes.search.retrieved");
    }

    @Override
    @Transactional(readOnly = true)
    public PerfumeImage getImageById(Long perfumeId, Long imageId) {
        return perfumeImageRepo.findByIdAndPerfumeId(imageId, perfumeId)
                .orElseThrow(() -> new NotFoundException("image.not.found"));
    }

    // ========== Private Helper Methods ==========
    private Response<PageResponse<PerfumeCardDTO>> buildPerfumeCardResponse(Page<Perfume> perfumesPage, String message) {
        List<Perfume> perfumes = perfumesPage.getContent();

        List<Long> perfumeIds = perfumes.stream()
                .map(Perfume::getId)
                .toList();

        List<Long> itemIds = perfumes.stream()
                .map(p -> p.getItem().getId())
                .toList();

        List<PerfumeImage> primaryImages = perfumeImageRepo.findPrimaryImagesByPerfumeIds(perfumeIds);
        List<ItemPrice> allPrices = itemPriceRepo.findCurrentActivePricesByItemIds(itemIds);
        List<ItemTranslation> allTranslations = itemTranslationRepo.findByItemIdsAndLocale(
                itemIds,
                LocaleContextHolder.getLocale().getLanguage()
        );

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

        Map<Long, ItemTranslation> translationsByItem = allTranslations.stream()
                .collect(Collectors.toMap(
                        itemTranslation -> itemTranslation.getItem().getId(),
                        t -> t
                ));

        List<PerfumeCardDTO> perfumeCards = perfumes.stream()
                .map(perfume -> mapToPerfumeCard(
                        perfume,
                        primaryImageByPerfume.get(perfume.getId()),
                        pricesByItem.get(perfume.getItem().getId()),
                        translationsByItem.get(perfume.getItem().getId())
                ))
                .toList();

        PageResponse<PerfumeCardDTO> pageResponse = PageResponse.<PerfumeCardDTO>builder()
                .content(perfumeCards)
                .page(PageResponse.PageInfo.builder()
                        .size(perfumesPage.getSize())
                        .number(perfumesPage.getNumber())
                        .totalElements(perfumesPage.getTotalElements())
                        .totalPages(perfumesPage.getTotalPages())
                        .build())
                .build();

        return Response.<PageResponse<PerfumeCardDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .data(pageResponse)
                .build();
    }

    private PerfumeCardDTO mapToPerfumeCard(
            Perfume perfume,
            PerfumeImage primaryImage,
            ItemPrice price,
            ItemTranslation translation
    ) {
        Item item = perfume.getItem();

        if (primaryImage == null) {
            throw new NotFoundException("image.not.found");
        }

        PerfumeCardDTO.PerfumeCardDTOBuilder builder = PerfumeCardDTO.builder()
                .id(perfume.getId())
                .name(item.getName())
                .brand(item.getBrand())
                .active(item.getActive())
                .primaryImageUrl(BASE_IMAGE_URL + "/" + perfume.getId() + "/images/" + primaryImage.getId());

        Optional.ofNullable(price)
                .ifPresent(p -> builder.currentPrice(p.getPrice()));

        Optional.ofNullable(translation)
                .ifPresent(t -> builder.translatedName(t.getName()));

        return builder.build();
    }

}
